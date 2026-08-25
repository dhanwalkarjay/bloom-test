package com.bloom.agent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.ImageButton;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

import com.bloom.R;
import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAPI;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryDetailActivity extends AppCompatActivity {

    private String orderId;
    private SupabaseAPI api;
    private boolean isPhotoCaptured = false;
    private ImageView ivProofPreview;
    private MaterialButton btnCaptureProof;
    private SeekBar sbSwipeToDeliver;
    private TextView tvSwipeHint;
    private TextView tvOfflineBanner;
    private MaterialCheckBox cbBypassOtp;
    
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        ivProofPreview.setImageBitmap(imageBitmap);
                        ivProofPreview.setVisibility(View.VISIBLE);
                        isPhotoCaptured = true;
                        sbSwipeToDeliver.setEnabled(true);
                        btnCaptureProof.setText("Retake Proof");
                    }
                }
            }
    );

    private String expectedOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_detail);

        api = RetrofitClient.getClient(this).create(SupabaseAPI.class);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        orderId = getIntent().getStringExtra("ORDER_ID");
        expectedOtp = getIntent().getStringExtra("DELIVERY_OTP");
        if (orderId == null) {
            Toast.makeText(this, "No Order ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ivProofPreview = findViewById(R.id.ivProofPreview);
        btnCaptureProof = findViewById(R.id.btnCaptureProof);
        sbSwipeToDeliver = findViewById(R.id.sbSwipeToDeliver);
        tvSwipeHint = findViewById(R.id.tvSwipeHint);
        cbBypassOtp = findViewById(R.id.cbBypassOtp);

        MaterialButton btnIssue = findViewById(R.id.btnIssue);
        btnIssue.setOnClickListener(v -> showIssueBottomSheet());

        ImageButton btnCallCustomer = findViewById(R.id.btnCallCustomer);
        btnCallCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+919876543210"));
            startActivity(intent);
        });

        ImageButton btnTextCustomer = findViewById(R.id.btnTextCustomer);
        btnTextCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:+919876543210"));
            startActivity(intent);
        });

        setupOtpFields();
        setupSwipeToDeliver();
        setupNetworkMonitoring();

        MaterialButton btnNavigate = findViewById(R.id.btnNavigate);
        btnNavigate.setOnClickListener(v -> {
            String uri = "google.navigation:q=28.6139,77.2090";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show();
            }
        });

        btnCaptureProof.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(takePictureIntent);
            } else {
                Toast.makeText(this, "Camera not available. Mocking photo...", Toast.LENGTH_SHORT).show();
                isPhotoCaptured = true;
                sbSwipeToDeliver.setEnabled(true);
                ivProofPreview.setVisibility(View.VISIBLE);
                ivProofPreview.setImageResource(android.R.drawable.ic_menu_gallery);
                btnCaptureProof.setText("Proof Captured (Mock)");
            }
        });
    }

    private void setupSwipeToDeliver() {
        sbSwipeToDeliver.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 10) {
                    tvSwipeHint.setAlpha(1f - (progress / 100f));
                } else {
                    tvSwipeHint.setAlpha(1f);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() >= 85) {
                    seekBar.setProgress(100);
                    markDelivered();
                } else {
                    seekBar.setProgress(0);
                    tvSwipeHint.setAlpha(1f);
                }
            }
        });
    }

    private void setupOtpFields() {
        EditText etOtp1 = findViewById(R.id.etOtp1);
        EditText etOtp2 = findViewById(R.id.etOtp2);
        EditText etOtp3 = findViewById(R.id.etOtp3);
        EditText etOtp4 = findViewById(R.id.etOtp4);

        if (etOtp1 == null) return;

        etOtp1.addTextChangedListener(new OtpTextWatcher(etOtp1, etOtp2));
        etOtp2.addTextChangedListener(new OtpTextWatcher(etOtp2, etOtp3));
        etOtp3.addTextChangedListener(new OtpTextWatcher(etOtp3, etOtp4));
        etOtp4.addTextChangedListener(new OtpTextWatcher(etOtp4, null));

        if (cbBypassOtp != null) {
            cbBypassOtp.setOnCheckedChangeListener((buttonView, isChecked) -> {
                etOtp1.setEnabled(!isChecked);
                etOtp2.setEnabled(!isChecked);
                etOtp3.setEnabled(!isChecked);
                etOtp4.setEnabled(!isChecked);
                if (isChecked) {
                    etOtp1.setAlpha(0.5f);
                    etOtp2.setAlpha(0.5f);
                    etOtp3.setAlpha(0.5f);
                    etOtp4.setAlpha(0.5f);
                } else {
                    etOtp1.setAlpha(1f);
                    etOtp2.setAlpha(1f);
                    etOtp3.setAlpha(1f);
                    etOtp4.setAlpha(1f);
                }
            });
        }
    }

    private class OtpTextWatcher implements TextWatcher {
        private View currentView;
        private View nextView;

        public OtpTextWatcher(View currentView, View nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            if (text.length() == 1 && nextView != null) {
                nextView.requestFocus();
            }
        }
    }

    private void setupNetworkMonitoring() {
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return;

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                runOnUiThread(() -> tvOfflineBanner.setVisibility(View.GONE));
            }

            @Override
            public void onLost(@NonNull Network network) {
                runOnUiThread(() -> tvOfflineBanner.setVisibility(View.VISIBLE));
            }
        };

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(request, networkCallback);
        
        // Initial state
        if (!com.bloom.customer.util.ConnectivityHelper.isConnected(this)) {
            tvOfflineBanner.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    private void showIssueBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        // We'll just construct a simple view programmatically to avoid needing a new xml file right now
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(60, 60, 60, 80);
        layout.setBackgroundColor(getColor(R.color.white)); // Or ?attr/colorSurface

        TextView title = new TextView(this);
        title.setText("Report an Issue");
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 40);
        layout.addView(title);

        String[] issues = {"Customer Not Available", "Wrong Address", "Damaged Item", "Other"};
        for (String issue : issues) {
            TextView btn = new TextView(this);
            btn.setText(issue);
            btn.setTextSize(16);
            btn.setPadding(0, 40, 0, 40);
            android.util.TypedValue outValue = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            btn.setBackgroundResource(outValue.resourceId);
            btn.setOnClickListener(v -> {
                dialog.dismiss();
                Toast.makeText(this, "Reported: " + issue, Toast.LENGTH_SHORT).show();
                // Handle API call for reporting issue
            });
            layout.addView(btn);
        }

        dialog.setContentView(layout);
        dialog.show();
    }

    private void markDelivered() {
        if (!isPhotoCaptured) {
            Toast.makeText(this, "Please capture proof of delivery first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate OTP
        if (cbBypassOtp == null || !cbBypassOtp.isChecked()) {
            EditText etOtp1 = findViewById(R.id.etOtp1);
            EditText etOtp2 = findViewById(R.id.etOtp2);
            EditText etOtp3 = findViewById(R.id.etOtp3);
            EditText etOtp4 = findViewById(R.id.etOtp4);
            
            if (etOtp1 != null && expectedOtp != null && !expectedOtp.isEmpty()) {
                String enteredOtp = etOtp1.getText().toString() +
                                    etOtp2.getText().toString() +
                                    etOtp3.getText().toString() +
                                    etOtp4.getText().toString();
                
                if (enteredOtp.length() < 4) {
                    Toast.makeText(this, "Please enter all 4 digits of the OTP", Toast.LENGTH_SHORT).show();
                    sbSwipeToDeliver.setProgress(0);
                    return;
                }
                if (!enteredOtp.equals(expectedOtp)) {
                    Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
                    sbSwipeToDeliver.setProgress(0);
                    return;
                }
            }
        }
        
        if (!com.bloom.customer.util.ConnectivityHelper.isConnected(this)) {
            android.content.SharedPreferences prefs = getSharedPreferences("agent_offline_sync", MODE_PRIVATE);
            String pending = prefs.getString("pending_deliveries", "");
            if (!pending.contains(orderId)) {
                pending = pending.isEmpty() ? orderId : pending + "," + orderId;
                prefs.edit().putString("pending_deliveries", pending).apply();
            }
            Toast.makeText(this, "Saved Offline. Will sync when network is restored.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("status", "delivered");
        // Assume photo is uploaded to Supabase Storage and we got a URL
        body.put("proof_image_url", "https://mock.storage.supabase.co/storage/v1/object/public/proofs/" + orderId + ".jpg");

        api.updateOrder("eq." + orderId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DeliveryDetailActivity.this, "Marked as Delivered!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(DeliveryDetailActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DeliveryDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
