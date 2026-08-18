package com.bloom.customer.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bloom.R;
import com.bloom.customer.ui.home.HomeActivity;
import com.bloom.customer.util.NetworkResult;
import com.bloom.customer.util.SystemBarInsets;
import com.bloom.databinding.ActivityOtpVerifyBinding;
import com.google.android.material.card.MaterialCardView;

public class OtpVerifyActivity extends AppCompatActivity {

    private ActivityOtpVerifyBinding binding;
    private AuthViewModel viewModel;
    private String phone;
    private CountDownTimer resendTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpVerifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBarInsets.apply(this);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        phone = getIntent().getStringExtra("phone");
        
        if (phone != null && !phone.isEmpty()) {
            binding.tvSubtitle.setText("Please enter the 6-digit code sent to " + phone + ".");
        }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnVerify.setOnClickListener(v -> attemptVerify());
        
        setupOtpInput();
        startResendTimer();
        
        binding.tvResend.setOnClickListener(v -> {
            if (binding.tvResend.getText().toString().equals("Resend Code")) {
                // Here we would call the ViewModel's resend OTP method.
                // For now, simply restart the timer and clear input to simulate resend.
                binding.etOtpInvisible.setText("");
                startResendTimer();
                Toast.makeText(this, "OTP Resent successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupOtpInput() {
        binding.etOtpInvisible.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateOtpUI(s.toString());
                if (s.length() == 6) {
                    attemptVerify();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Show keyboard immediately by requesting focus
        binding.etOtpInvisible.requestFocus();
        updateOtpUI("");
    }

    private void updateOtpUI(String otp) {
        TextView[] textViews = {binding.tvOtp1, binding.tvOtp2, binding.tvOtp3, binding.tvOtp4, binding.tvOtp5, binding.tvOtp6};
        MaterialCardView[] cards = {binding.cardOtp1, binding.cardOtp2, binding.cardOtp3, binding.cardOtp4, binding.cardOtp5, binding.cardOtp6};
        
        int colorPrimary = ContextCompat.getColor(this, R.color.bloom_primary);
        int colorOutline = ContextCompat.getColor(this, R.color.cart_outline);

        for (int i = 0; i < 6; i++) {
            if (i < otp.length()) {
                textViews[i].setText(String.valueOf(otp.charAt(i)));
                cards[i].setStrokeColor(colorPrimary);
                cards[i].setStrokeWidth(dpToPx(1));
            } else {
                textViews[i].setText("");
                if (i == otp.length()) {
                    cards[i].setStrokeColor(colorPrimary);
                    cards[i].setStrokeWidth(dpToPx(2));
                } else {
                    cards[i].setStrokeColor(colorOutline);
                    cards[i].setStrokeWidth(dpToPx(1));
                }
            }
        }
        binding.tvError.setVisibility(View.GONE);
    }
    
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void startResendTimer() {
        binding.tvResend.setEnabled(false);
        if (resendTimer != null) resendTimer.cancel();
        
        resendTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                binding.tvResend.setText("Resend in 0:" + (seconds < 10 ? "0" + seconds : seconds));
                
                int colorPrimary = ContextCompat.getColor(OtpVerifyActivity.this, R.color.bloom_primary);
                binding.tvResend.setTextColor(colorPrimary); 
            }

            @Override
            public void onFinish() {
                binding.tvResend.setText("Resend Code");
                binding.tvResend.setEnabled(true);
            }
        }.start();
    }

    private void attemptVerify() {
        String otp = binding.etOtpInvisible.getText().toString().trim();

        if (otp.length() < 6) {
            binding.tvError.setText("Please enter the complete 6-digit code.");
            binding.tvError.setVisibility(View.VISIBLE);
            return;
        }

        viewModel.verifyOtp(phone, otp).observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                setLoading(true);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                setLoading(false);
                
                boolean isRecovery = getIntent().getBooleanExtra("is_recovery", false);
                if (isRecovery) {
                    Intent intent = new Intent(this, ResetPasswordActivity.class);
                    intent.putExtra("phone", phone);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                finish();
            } else if (result.status == NetworkResult.Status.ERROR) {
                setLoading(false);
                binding.tvError.setText(result.message);
                binding.tvError.setVisibility(View.VISIBLE);
                
                // Shake or clear OTP here if desired
                binding.etOtpInvisible.setText("");
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnVerify.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        binding.etOtpInvisible.setEnabled(!isLoading);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) resendTimer.cancel();
    }
}
