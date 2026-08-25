package com.bloom.customer.ui.orderconfirmation;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.customer.ui.home.HomeActivity;
import com.bloom.customer.ui.ordertracking.OrderTrackingActivity;
import com.bloom.databinding.ActivityOrderConfirmationBinding;

import androidx.core.view.WindowCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.DecelerateInterpolator;

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
        binding.tvOrderId.setText("#" + displayId);
        binding.tvTotalPaid.setText(com.bloom.customer.util.CurrencyFormatter.format(0.00)); // Default placeholder

        // Setup Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> goHome());

        binding.btnBackHome.setOnClickListener(v -> goHome());
        
        binding.btnTrackOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderTrackingActivity.class);
            intent.putExtra("order_id", orderId);
            startActivity(intent);
        });

        fetchOrderDetails();
        startPremiumFloralAnimations();

        // Edge-to-Edge handling
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            binding.appBarLayout.setPadding(0, insets.top, 0, 0);
            binding.llActions.setPadding(
                    binding.llActions.getPaddingLeft(),
                    binding.llActions.getPaddingTop(),
                    binding.llActions.getPaddingRight(),
                    insets.bottom + 20
            );
            return windowInsets;
        });
    }

    private void startPremiumFloralAnimations() {
        // Initial States
        binding.toolbar.setAlpha(0f);
        binding.vGlow.setAlpha(0f);
        binding.vGlow.setScaleX(0.8f);
        binding.vGlow.setScaleY(0.8f);

        binding.ivBadgeOuter.setAlpha(0f);
        binding.ivBadgeOuter.setScaleX(0.8f);
        binding.ivBadgeOuter.setScaleY(0.8f);

        binding.ivBadgeInner.setAlpha(0f);
        binding.ivBadgeInner.setScaleX(0.8f);
        binding.ivBadgeInner.setScaleY(0.8f);

        binding.tvHeroTitle.setAlpha(0f);
        binding.tvOrderId.setAlpha(0f);
        binding.cvDelivery.setAlpha(0f);
        binding.cvSummary.setAlpha(0f);
        binding.llActions.setAlpha(0f);

        long duration = 600;
        DecelerateInterpolator smoothDecelerator = new DecelerateInterpolator(1.5f);

        binding.toolbar.animate().alpha(1f)
                .setDuration(400).setStartDelay(100).setInterpolator(smoothDecelerator).start();

        binding.vGlow.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(800).setStartDelay(200).setInterpolator(smoothDecelerator).start();

        binding.ivBadgeOuter.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(800).setStartDelay(250)
                .setInterpolator(smoothDecelerator).withEndAction(() -> {
                    android.animation.ObjectAnimator rotation = android.animation.ObjectAnimator.ofFloat(binding.ivBadgeOuter, "rotation", 0f, 360f);
                    rotation.setDuration(20000);
                    rotation.setRepeatCount(android.animation.ValueAnimator.INFINITE);
                    rotation.setInterpolator(new android.view.animation.LinearInterpolator());
                    rotation.start();
                }).start();

        binding.ivBadgeInner.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(800).setStartDelay(300)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f)).start();

        binding.tvHeroTitle.animate().alpha(1f)
                .setDuration(duration).setStartDelay(300).setInterpolator(smoothDecelerator).start();
                
        binding.tvOrderId.animate().alpha(1f)
                .setDuration(duration).setStartDelay(350).setInterpolator(smoothDecelerator).start();

        binding.cvDelivery.animate().alpha(1f)
                .setDuration(duration).setStartDelay(400).setInterpolator(smoothDecelerator).start();
                
        binding.cvSummary.animate().alpha(1f)
                .setDuration(duration).setStartDelay(450).setInterpolator(smoothDecelerator).start();

        binding.llActions.animate().alpha(1f)
                .setDuration(duration).setStartDelay(500).setInterpolator(smoothDecelerator).start();
    }
    
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        menu.add(0, 1, 0, "Download Invoice").setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, 2, 0, "Send to Another").setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == 1) {
            com.google.android.material.snackbar.Snackbar.make(binding.getRoot(), "Downloading Invoice...", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == 2) {
            com.google.android.material.snackbar.Snackbar.make(binding.getRoot(), "Opening Contact Picker...", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private com.bloom.customer.data.model.Order currentOrder;

    private void fetchOrderDetails() {
        if (orderId == null) return;
        
        com.bloom.customer.data.repository.OrderRepository repository = 
            new com.bloom.customer.data.repository.OrderRepository(this);
            
        repository.getOrderById(orderId).observe(this, result -> {
            if (result.status == com.bloom.customer.util.NetworkResult.Status.SUCCESS && result.data != null) {
                com.bloom.customer.data.model.Order order = result.data;
                currentOrder = order;
                
                // Format Dates & ID
                String formattedDate = order.getCreatedAt();
                if (formattedDate != null && formattedDate.length() >= 10) {
                    formattedDate = formattedDate.substring(0, 10); // Simple fallback YYYY-MM-DD
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                        java.util.Date date = sdf.parse(order.getCreatedAt());
                        if (date != null) {
                            java.text.SimpleDateFormat outSdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
                            formattedDate = outSdf.format(date);
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
                
                String orderIdStr = order.getId() != null ? order.getId().toUpperCase() : "";
                if (orderIdStr.length() > 8) orderIdStr = orderIdStr.substring(0, 8); // Shorten UUID
                
                binding.tvOrderId.setText("#" + orderIdStr);

                // Financials
                double total = order.getTotalAmount();
                binding.tvTotalPaid.setText(com.bloom.customer.util.CurrencyFormatter.format(total));
                
                // Address & Name
                if (order.getAddress() != null) {
                    String name = order.getAddress().getRecipientName() != null ? order.getAddress().getRecipientName() : "Customer";
                }
                
                // Payment Method
                String paymentMethod = getIntent().getStringExtra("payment_method");
                if (paymentMethod == null || paymentMethod.isEmpty()) {
                    paymentMethod = "Apple Pay"; // Default for visual consistency if null
                }
                
                if (order.getPaymentStatus() != null && order.getPaymentStatus().equals("paid")) {
                    binding.tvPaymentMethod.setText("Payment via " + paymentMethod);
                } else if ("COD".equalsIgnoreCase(paymentMethod) || "Cash on Delivery".equalsIgnoreCase(paymentMethod)) {
                    binding.tvPaymentMethod.setText("Cash on Delivery");
                } else {
                    binding.tvPaymentMethod.setText("Pending Payment");
                }
                
                // Product
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    com.bloom.customer.data.model.OrderItem firstItem = order.getItems().get(0);
                    binding.tvProductName.setText(firstItem.getProduct() != null ? firstItem.getProduct().getTitle() : "Bouquet");
                    binding.tvProductSubtitle.setText("Artisanal Bouquet"); 
                    
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
