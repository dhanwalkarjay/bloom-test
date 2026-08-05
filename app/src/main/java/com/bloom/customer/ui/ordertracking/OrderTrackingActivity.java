package com.bloom.customer.ui.ordertracking;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bloom.databinding.ActivityOrderTrackingBinding;

public class OrderTrackingActivity extends AppCompatActivity {

    private ActivityOrderTrackingBinding binding;
    private OrderTrackingViewModel viewModel;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderId = getIntent().getStringExtra("order_id");

        viewModel = new ViewModelProvider(this).get(OrderTrackingViewModel.class);

        binding.btnBack.setOnClickListener(v -> finish());

        setupObservers();
        
        if (orderId != null) {
            viewModel.startTracking(orderId);
        }
    }

    private void setupObservers() {
        viewModel.getOrderStatus().observe(this, status -> {
            if (status != null) {
                updateUI(status);
            }
        });
    }

    private void updateUI(String status) {
        // Simplified highlight logic
        if ("Delivered".equalsIgnoreCase(status)) {
            binding.ivStatus5.setAlpha(1.0f);
        } else if (status.toLowerCase().contains("out")) {
            binding.ivStatus4.setAlpha(1.0f);
        }
    }
}
