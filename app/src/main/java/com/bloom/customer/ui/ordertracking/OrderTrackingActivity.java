package com.bloom.customer.ui.ordertracking;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.customer.data.api.RealtimeService;
import com.bloom.databinding.ActivityOrderTrackingBinding;

public class OrderTrackingActivity extends AppCompatActivity {

    private ActivityOrderTrackingBinding binding;
    private RealtimeService realtimeService;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderId = getIntent().getStringExtra("order_id");
        String shopName = getIntent().getStringExtra("shop_name");

        binding.tvOrderId.setText("Order #" + orderId);
        binding.tvShopName.setText(shopName);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        realtimeService = new RealtimeService();
        startTracking();
    }

    private void startTracking() {
        realtimeService.startTracking(orderId, (id, newStatus) -> {
            runOnUiThread(() -> updateUI(newStatus));
        });
    }

    private void updateUI(String status) {
        // Reset all dots to gray and alpha 0.5
        // This is a simplified logic to highlight steps up to the current status
        highlightStatus(status);
    }

    private void highlightStatus(String status) {
        // Reset logic
        binding.statusConfirmed.setAlpha(0.5f);
        binding.statusPreparing.setAlpha(0.5f);
        binding.statusOut.setAlpha(0.5f);
        binding.statusDelivered.setAlpha(0.5f);

        if ("Confirmed".equalsIgnoreCase(status)) {
            binding.statusConfirmed.setAlpha(1.0f);
        } else if ("Preparing".equalsIgnoreCase(status)) {
            binding.statusConfirmed.setAlpha(1.0f);
            binding.statusPreparing.setAlpha(1.0f);
        } else if ("Out for Delivery".equalsIgnoreCase(status)) {
            binding.statusConfirmed.setAlpha(1.0f);
            binding.statusPreparing.setAlpha(1.0f);
            binding.statusOut.setAlpha(1.0f);
        } else if ("Delivered".equalsIgnoreCase(status)) {
            binding.statusConfirmed.setAlpha(1.0f);
            binding.statusPreparing.setAlpha(1.0f);
            binding.statusOut.setAlpha(1.0f);
            binding.statusDelivered.setAlpha(1.0f);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realtimeService.stopTracking();
    }
}
