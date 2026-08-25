package com.bloom.customer.ui.profile;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.model.Occasion;
import com.bloom.customer.data.repository.OccasionRepository;
import com.bloom.customer.util.NetworkResult;
import com.bloom.customer.util.SystemBarInsets;
import com.bloom.databinding.ActivityAddOccasionBinding;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.R.attr;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddOccasionActivity extends AppCompatActivity {

    private ActivityAddOccasionBinding binding;
    private OccasionRepository repository;
    private String selectedOccasionType = "Birthday"; // Default
    private MaterialCardView[] cards;

    private final ActivityResultLauncher<Intent> contactPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri contactUri = result.getData().getData();
                    if (contactUri != null) {
                        retrieveContactName(contactUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddOccasionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBarInsets.apply(this);

        repository = new OccasionRepository(this);

        binding.btnBack.setOnClickListener(v -> finish());

        setupOccasionCards();
        setupContactPicker();

        binding.etDate.setOnClickListener(v -> showMaterialDatePicker());

        binding.btnSave.setOnClickListener(v -> saveOccasion());

        // Watch recipient name to auto-update title if not custom
        binding.etRecipientName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateAutoTitle();
            }
        });

        // Check if editing
        if (getIntent().hasExtra("occasion_id")) {
            binding.tvCustomTitleHeader.setVisibility(View.VISIBLE);
            binding.tilCustomTitle.setVisibility(View.VISIBLE);
            binding.tvCustomTitleHeader.setText("Edit Occasion");
            binding.etTitle.setText(getIntent().getStringExtra("occasion_title"));
            binding.etDate.setText(getIntent().getStringExtra("occasion_date"));
            binding.etRecipientName.setText(getIntent().getStringExtra("occasion_recipient"));
            binding.etRelation.setText(getIntent().getStringExtra("occasion_relation"));
            binding.btnSave.setText("Save Changes");
        }
    }

    private void setupOccasionCards() {
        cards = new MaterialCardView[]{
                binding.cardBirthday, binding.cardAnniversary, binding.cardValentine, binding.cardMother, binding.cardCustom
        };

        View.OnClickListener cardClickListener = v -> {
            for (MaterialCardView card : cards) {
                card.setStrokeColor(getColor(com.bloom.R.color.cart_outline_variant));
                card.setCardBackgroundColor(getColor(com.bloom.R.color.cart_surface));
            }
            MaterialCardView clicked = (MaterialCardView) v;
            // The theme attributes ?attr/colorPrimary are handled slightly differently in code,
            // but we can use our primary colors.
            clicked.setStrokeColor(getColor(com.bloom.R.color.bloom_primary));
            clicked.setCardBackgroundColor(0xFFFFF0F1); // Tinted surface

            if (v.getId() == binding.cardBirthday.getId()) selectedOccasionType = "Birthday";
            else if (v.getId() == binding.cardAnniversary.getId()) selectedOccasionType = "Anniversary";
            else if (v.getId() == binding.cardValentine.getId()) selectedOccasionType = "Valentine's";
            else if (v.getId() == binding.cardMother.getId()) selectedOccasionType = "Mother's Day";
            else if (v.getId() == binding.cardCustom.getId()) selectedOccasionType = "Custom";

            if (selectedOccasionType.equals("Custom")) {
                binding.tvCustomTitleHeader.setVisibility(View.VISIBLE);
                binding.tilCustomTitle.setVisibility(View.VISIBLE);
                binding.etTitle.setText(""); // Clear auto-title
            } else {
                binding.tvCustomTitleHeader.setVisibility(View.GONE);
                binding.tilCustomTitle.setVisibility(View.GONE);
                updateAutoTitle();
            }
        };

        for (MaterialCardView card : cards) {
            card.setOnClickListener(cardClickListener);
        }
    }

    private void updateAutoTitle() {
        if (selectedOccasionType.equals("Custom")) return;
        
        String name = binding.etRecipientName.getText().toString().trim();
        if (!name.isEmpty()) {
            if (name.toLowerCase().endsWith("s")) {
                binding.etTitle.setText(name + "' " + selectedOccasionType);
            } else {
                binding.etTitle.setText(name + "'s " + selectedOccasionType);
            }
        } else {
            binding.etTitle.setText(selectedOccasionType);
        }
    }

    private void setupContactPicker() {
        binding.tilRecipientName.setEndIconOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            contactPickerLauncher.launch(intent);
        });
    }

    private void retrieveContactName(Uri contactUri) {
        String[] projection = new String[]{ContactsContract.Contacts.DISPLAY_NAME};
        try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                if (nameIndex != -1) {
                    String name = cursor.getString(nameIndex);
                    binding.etRecipientName.setText(name);
                    updateAutoTitle();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to get contact info", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMaterialDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Format the selected date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            binding.etDate.setText(sdf.format(new Date(selection)));
        });

        datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }

    private void saveOccasion() {
        String title = binding.etTitle.getText().toString().trim();
        String date = binding.etDate.getText().toString().trim();
        String recipient = binding.etRecipientName.getText().toString().trim();
        String relation = binding.etRelation.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty() || recipient.isEmpty()) {
            Toast.makeText(this, "Please fill in the required details", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSave.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        Occasion occasion = new Occasion();
        occasion.setUserId(SessionManager.getInstance(this).getUserId());
        occasion.setTitle(title);
        occasion.setTargetDate(date);
        occasion.setRecipientName(recipient);
        occasion.setRecipientRelation(relation);

        String occasionId = getIntent().getStringExtra("occasion_id");

        androidx.lifecycle.LiveData<NetworkResult<Void>> resultLiveData;
        if (occasionId != null) {
            resultLiveData = repository.updateOccasion(occasionId, occasion);
        } else {
            resultLiveData = repository.addOccasion(occasion);
        }

        resultLiveData.observe(this, result -> {
            if (result.status == NetworkResult.Status.SUCCESS) {
                Toast.makeText(this, "Occasion saved! We'll remind you.", Toast.LENGTH_SHORT).show();
                finish();
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.btnSave.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
