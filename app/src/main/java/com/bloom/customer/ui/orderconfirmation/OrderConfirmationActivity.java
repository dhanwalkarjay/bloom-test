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
        binding.tvTotalPaid.setText(com.bloom.customer.util.CurrencyFormatter.format(0.00)); // Default placeholder

        String paymentMethod = getIntent().getStringExtra("payment_method");
        if (paymentMethod != null) {
            binding.tvPaymentMethod.setText("Payment via " + paymentMethod);
        }

        binding.btnClose.setOnClickListener(v -> goHome());
        binding.btnGoHome.setOnClickListener(v -> goHome());
        
        applyInsets();
        
        binding.btnTrackOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderTrackingActivity.class);
            intent.putExtra("order_id", orderId);
            startActivity(intent);
        });

        fetchOrderDetails();
    }

    private void applyInsets() {
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            androidx.core.graphics.Insets insets = windowInsets.getInsets(
                    androidx.core.view.WindowInsetsCompat.Type.systemBars()
            );
            
            binding.llBottomActions.setPadding(
                    binding.llBottomActions.getPaddingLeft(),
                    binding.llBottomActions.getPaddingTop(),
                    binding.llBottomActions.getPaddingRight(),
                    insets.bottom + 48 // Extra padding for Back home button
            );
            
            return windowInsets;
        });
    }

    private void fetchOrderDetails() {
        if (orderId == null) return;
        
        com.bloom.customer.data.repository.OrderRepository repository = 
            new com.bloom.customer.data.repository.OrderRepository(this);
            
        repository.getOrderById(orderId).observe(this, result -> {
            if (result.status == com.bloom.customer.util.NetworkResult.Status.SUCCESS && result.data != null) {
                com.bloom.customer.data.model.Order order = result.data;
                binding.tvTotalPaid.setText(com.bloom.customer.util.CurrencyFormatter.format(order.getTotalAmount()));
                
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    com.bloom.customer.data.model.OrderItem firstItem = order.getItems().get(0);
                    binding.tvProductName.setText(firstItem.getProduct() != null ? firstItem.getProduct().getTitle() : "Bouquet");
                    
                    if (firstItem.getProduct() != null && firstItem.getProduct().getImages() != null) {
                        com.bumptech.glide.Glide.with(this)
                            .load(firstItem.getProduct().getImages())
                            .into(binding.ivProduct);
                    }
                }
            }
        });
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
