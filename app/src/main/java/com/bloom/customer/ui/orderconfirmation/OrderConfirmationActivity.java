package com.bloom.customer.ui.orderconfirmation;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.customer.ui.home.HomeActivity;
import com.bloom.customer.ui.ordertracking.OrderTrackingActivity;
import com.bloom.databinding.ActivityOrderConfirmationBinding;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.OvershootInterpolator;

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

        binding.btnClose.setOnClickListener(v -> goHome());
        
        binding.btnTrackOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderTrackingActivity.class);
            intent.putExtra("order_id", orderId);
            startActivity(intent);
        });

        binding.btnDownloadInvoice.setOnClickListener(v -> handleDownloadInvoice());

        binding.btnSendToAnother.setOnClickListener(v -> handleSendToAnother());

        fetchOrderDetails();
        startPremiumFloralAnimations();

        // Status bar transparent to let floral bleed
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void startPremiumFloralAnimations() {
        // Initial States
        binding.ivFloralHeader.setAlpha(0f);
        binding.ivFloralHeader.setScaleX(1.1f);
        binding.ivFloralHeader.setScaleY(1.1f);

        binding.topBar.setAlpha(0f);
        binding.topBar.setTranslationY(-30f);

        binding.tvHeroTitle.setAlpha(0f);
        binding.tvHeroTitle.setTranslationY(30f);

        binding.tvThankYou.setAlpha(0f);
        binding.tvThankYou.setTranslationY(30f);

        binding.ivCheck.setAlpha(0f);
        binding.ivCheck.setScaleX(0.5f);
        binding.ivCheck.setScaleY(0.5f);

        // Animate Buttons
        binding.btnTrackOrder.setAlpha(0f);
        binding.btnTrackOrder.setTranslationY(80f);
        
        binding.btnDownloadInvoice.setAlpha(0f);
        binding.btnDownloadInvoice.setTranslationY(80f);

        long duration = 800;
        android.view.animation.DecelerateInterpolator smoothDecelerator = new android.view.animation.DecelerateInterpolator(2f);

        // 1. Header image smooth reveal & scale down
        binding.ivFloralHeader.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(1500).setInterpolator(smoothDecelerator).start();

        // 2. Top Bar drops in
        binding.topBar.animate().alpha(1f).translationY(0f)
                .setDuration(600).setStartDelay(200).setInterpolator(smoothDecelerator).start();

        // 3. Wreath Checkmark pops in playfully
        binding.ivCheck.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(600).setStartDelay(400)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f)).start();

        // 4. Hero & Thank you text softly slides up
        binding.tvHeroTitle.animate().alpha(1f).translationY(0f)
                .setDuration(duration).setStartDelay(400).setInterpolator(smoothDecelerator).start();

        binding.tvThankYou.animate().alpha(1f).translationY(0f)
                .setDuration(duration).setStartDelay(500).setInterpolator(smoothDecelerator).start();

        // 5. Buttons glide in at the end
        binding.btnTrackOrder.animate().alpha(1f).translationY(0f)
                .setDuration(duration).setStartDelay(700).setInterpolator(smoothDecelerator).start();
                
        binding.btnDownloadInvoice.animate().alpha(1f).translationY(0f)
                .setDuration(duration).setStartDelay(800).setInterpolator(smoothDecelerator).start();
        
        // 6. Animate the scrollview container for the order details
        binding.svContent.setAlpha(0f);
        binding.svContent.setTranslationY(60f);
        binding.svContent.animate().alpha(1f).translationY(0f)
            .setDuration(800).setStartDelay(500).setInterpolator(smoothDecelerator).start();
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
                
                binding.tvOrderId.setText("Order #" + orderIdStr + " / " + formattedDate);

                // Financials
                double total = order.getTotalAmount();
                double subtotal = order.getBouquetSubtotal() + order.getAddonsSubtotal();
                double tax = order.getTaxAmount();
                double delivery = order.getDeliveryFee();
                
                binding.tvTotalPaid.setText(com.bloom.customer.util.CurrencyFormatter.format(total));
                binding.tvSubtotal.setText("(" + com.bloom.customer.util.CurrencyFormatter.format(subtotal) + ")");
                binding.tvTax.setText("(" + com.bloom.customer.util.CurrencyFormatter.format(tax) + ")");
                binding.tvDeliveryFee.setText("(" + com.bloom.customer.util.CurrencyFormatter.format(delivery) + ")");
                
                // Address
                if (order.getAddress() != null) {
                    String name = order.getAddress().getRecipientName() != null ? order.getAddress().getRecipientName() : "Customer";
                    String fullAddress = order.getAddress().getFullAddress() != null ? order.getAddress().getFullAddress() : "";
                    binding.tvShippingAddress.setText(name + "\n" + fullAddress);
                    
                    binding.tvThankYou.setText("Thank you for your floral\nselection, " + name.split(" ")[0] + "!");
                } else {
                    binding.tvThankYou.setText("Thank you for your floral\nselection!");
                }
                
                // Payment Method (fallback to Razorpay if paymentStatus exists)
                if (order.getPaymentStatus() != null && order.getPaymentStatus().equals("paid")) {
                    binding.tvPaymentMethod.setText("Paid via Razorpay\n" + (order.getRazorpayPaymentId() != null ? order.getRazorpayPaymentId() : ""));
                } else {
                    binding.tvPaymentMethod.setText("Pending Payment");
                }
                
                // Product
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    com.bloom.customer.data.model.OrderItem firstItem = order.getItems().get(0);
                    binding.tvProductName.setText(firstItem.getProduct() != null ? firstItem.getProduct().getTitle() : "Bouquet");
                    binding.tvProductSubtitle.setText("Luxury Collection"); // Hardcode subtitle as ProductInfo doesn't have category
                    binding.tvQty.setText("Qty: " + firstItem.getQuantity());
                    
                    double unitPrice = subtotal / Math.max(1, firstItem.getQuantity());
                    binding.tvProductPrice.setText("Price: " + com.bloom.customer.util.CurrencyFormatter.format(unitPrice));
                    
                    if (firstItem.getProduct() != null && firstItem.getProduct().getImages() != null) {
                        com.bumptech.glide.Glide.with(this)
                            .load(firstItem.getProduct().getImages())
                            .into(binding.ivProduct);
                    }
                }
            }
        });
    }

    private void handleDownloadInvoice() {
        if (currentOrder == null) {
            android.widget.Toast.makeText(this, "Order details not ready", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            android.net.Uri pdfUri = com.bloom.customer.util.InvoiceGenerator.generateAndSaveInvoice(this, currentOrder);
            
            // Phase 4: Corporate Assistant One-Tap PDF sharing
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Invoice for Order #" + currentOrder.getId());
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Please find attached the invoice for my recent Bloom purchase.");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Share Invoice"));
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Failed to generate invoice", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSendToAnother() {
        if (currentOrder == null || currentOrder.getItems() == null || currentOrder.getItems().isEmpty()) {
            android.widget.Toast.makeText(this, "Order details not fully loaded yet.", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        com.bloom.customer.data.repository.CartRepository cartRepository = 
                com.bloom.customer.data.repository.CartRepository.getInstance(this);
        cartRepository.clearCart();
        
        for (com.bloom.customer.data.model.OrderItem item : currentOrder.getItems()) {
            com.bloom.customer.data.model.Product p = new com.bloom.customer.data.model.Product();
            p.setId(item.getProductId());
            p.setShopId(currentOrder.getShopId());
            p.setPrice(item.getUnitPrice());
            if (item.getProduct() != null) {
                p.setName(item.getProduct().getTitle());
                p.setImageUrl(item.getProduct().getImages());
            } else {
                p.setName("Bouquet");
            }
            
            com.bloom.customer.data.model.CartItem cartItem = new com.bloom.customer.data.model.CartItem(p);
            cartItem.setQuantity(item.getQuantity());
            cartItem.setSize(item.getSize());
            cartItem.setCardMessage(item.getCardMessage());
            
            cartRepository.addToCart(cartItem);
        }
        
        // Skip cart and go directly to checkout
        Intent intent = new Intent(this, com.bloom.customer.ui.checkout.AddressSelectActivity.class);
        startActivity(intent);
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
