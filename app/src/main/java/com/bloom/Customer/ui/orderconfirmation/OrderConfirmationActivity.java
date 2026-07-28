package com.bloom.customer.ui.orderconfirmation;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.customer.ui.home.HomeActivity;
import com.bloom.databinding.ActivityOrderConfirmationBinding;

public class OrderConfirmationActivity extends AppCompatActivity {

    private ActivityOrderConfirmationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderConfirmationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.btnTrackOrder.setOnClickListener(v -> {
            // TODO: Navigate to OrderTrackingActivity
            finish();
        });
    }
}
