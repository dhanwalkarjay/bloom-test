package com.bloom.customer.ui.checkout;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.databinding.ActivityDeliverySlotBinding;

public class DeliverySlotActivity extends AppCompatActivity {

    private ActivityDeliverySlotBinding binding;
    private String selectedSlot = "Instant";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeliverySlotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.cardInstant.setOnClickListener(v -> selectSlot("Instant"));
        binding.cardSameDay.setOnClickListener(v -> selectSlot("Same-Day"));
        binding.cardScheduled.setOnClickListener(v -> selectSlot("Scheduled"));

        // Midnight is disabled by default in layout (alpha 0.5, rb enabled false)

        binding.btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("address_id", getIntent().getStringExtra("address_id"));
            intent.putExtra("delivery_slot", selectedSlot);
            startActivity(intent);
        });
    }

    private void selectSlot(String slot) {
        selectedSlot = slot;
        binding.rbInstant.setChecked("Instant".equals(slot));
        binding.rbSameDay.setChecked("Same-Day".equals(slot));
        binding.rbScheduled.setChecked("Scheduled".equals(slot));
        binding.rbMidnight.setChecked(false);
    }
}
