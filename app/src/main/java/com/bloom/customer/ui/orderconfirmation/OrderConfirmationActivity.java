package com.bloom.customer.ui.orderconfirmation;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.customer.ui.home.HomeActivity;
import com.bloom.customer.ui.ordertracking.OrderTrackingActivity;
import com.bloom.databinding.ActivityOrderConfirmationBinding;

public class OrderConfirmationActivity extends AppCompatActivity {

    private ActivityOrderConfirmationBinding binding;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderConfirmationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderId = getIntent().getStringExtra("order_id");
        
        String displayId = "BLM-892415";
        if (orderId != null && !orderId.isEmpty()) {
            displayId = orderId.length() > 8 ? orderId.substring(0, 8) : orderId;
        }
        binding.tvOrderId.setText("Order ID: #" + displayId);

        binding.btnClose.setOnClickListener(v -> goHome());
        binding.btnGoHome.setOnClickListener(v -> goHome());
        
        binding.btnTrackOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderTrackingActivity.class);
            intent.putExtra("order_id", orderId);
            intent.putExtra("shop_name", "Rose Garden Florist"); // Mock
            startActivity(intent);
        });
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
