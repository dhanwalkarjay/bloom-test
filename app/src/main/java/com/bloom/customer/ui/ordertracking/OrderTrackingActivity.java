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

        binding.btnBack.setOnClickListener(v -> finish());

        realtimeService = new RealtimeService();
        startTracking();
    }

    private void startTracking() {
        realtimeService.startTracking(orderId, (id, newStatus) -> {
            runOnUiThread(() -> updateUI(newStatus));
        });
    }

    private void updateUI(String status) {
        // UI is statically mocked to "Out for Delivery" per design requirements
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realtimeService.stopTracking();
    }
}
