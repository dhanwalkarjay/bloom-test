package com.bloom.customer.ui.checkout;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bloom.customer.util.SystemBarInsets;
import com.bloom.databinding.ActivityAddresslessGiftingBinding;
import com.google.android.material.textfield.TextInputLayout;
import com.bloom.R;

public class AddresslessGiftingActivity extends AppCompatActivity {

    private ActivityAddresslessGiftingBinding binding;

    private final ActivityResultLauncher<Intent> contactPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri contactUri = result.getData().getData();
                    if (contactUri != null) {
                        retrieveContactNumber(contactUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddresslessGiftingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBarInsets.apply(this);

        binding.btnBack.setOnClickListener(v -> finish());

        // Setup phone number formatting
        binding.etRecipientPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        // Setup contact picker icon click
        binding.tilPhone.setEndIconOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            contactPickerLauncher.launch(intent);
        });

        binding.btnContinue.setOnClickListener(v -> {
            String name = binding.etRecipientName.getText().toString().trim();
            String phone = binding.etRecipientPhone.getText().toString().trim();
            String note = binding.etPersonalNote != null ? binding.etPersonalNote.getText().toString().trim() : "";
            
            boolean sendNow = binding.cgSchedule != null && binding.cgSchedule.getCheckedChipId() == R.id.chipNow;
            String schedule = sendNow ? "now" : "tomorrow_morning";

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please enter recipient details", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("is_addressless", true);
            intent.putExtra("recipient_name", name);
            intent.putExtra("recipient_phone", phone);
            intent.putExtra("personal_note", note);
            intent.putExtra("schedule_time", schedule);
            // Dummy values for payment screen requirements
            intent.putExtra("address_id", "addressless_dummy_id");
            intent.putExtra("delivery_distance_km", 0.0); // Flat rate will apply
            intent.putExtra("delivery_fee", 100.0); // Flat rate
            intent.putExtra("delivery_slot", "To be selected by recipient");
            startActivity(intent);
        });
    }

    private void retrieveContactNumber(Uri contactUri) {
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        };
        try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                if (numberIndex != -1) {
                    String number = cursor.getString(numberIndex);
                    binding.etRecipientPhone.setText(number);
                }
                if (nameIndex != -1) {
                    String name = cursor.getString(nameIndex);
                    if (binding.etRecipientName.getText().toString().isEmpty()) {
                        binding.etRecipientName.setText(name);
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to get contact info", Toast.LENGTH_SHORT).show();
        }
    }
}
