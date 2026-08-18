package com.bloom.customer.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.customer.util.SystemBarInsets;
import com.bloom.databinding.ActivityAddresslessGiftingBinding;

public class AddresslessGiftingActivity extends AppCompatActivity {

    private ActivityAddresslessGiftingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddresslessGiftingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBarInsets.apply(this);

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnContinue.setOnClickListener(v -> {
            String name = binding.etRecipientName.getText().toString().trim();
            String phone = binding.etRecipientPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please enter recipient details", Toast.LENGTH_SHORT).show();
                return;
            }

            // We bypass AddressSelectActivity and DeliverySlotActivity normally requires an address ID.
            // For address-less, we might bypass delivery slot entirely, or just jump to payment.
            // Since we don't know the address, the recipient will pick the slot when they enter the address.
            // So we jump straight to PaymentActivity!

            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("is_addressless", true);
            intent.putExtra("recipient_name", name);
            intent.putExtra("recipient_phone", phone);
            // Dummy values for payment screen requirements
            intent.putExtra("address_id", "addressless_dummy_id");
            intent.putExtra("delivery_distance_km", 0.0); // Flat rate will apply
            intent.putExtra("delivery_fee", 100.0); // Flat rate
            intent.putExtra("delivery_slot", "To be selected by recipient");
            startActivity(intent);
        });
    }
}
