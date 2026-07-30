package com.bloom.customer.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.model.CartItem;
import com.bloom.customer.data.model.Order;
import com.bloom.customer.data.model.OrderItem;
import com.bloom.customer.data.repository.CartRepository;
import com.bloom.customer.data.repository.OrderRepository;
import com.bloom.customer.ui.orderconfirmation.OrderConfirmationActivity;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityPaymentBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for order payment.
 * Implements Cash on Delivery (COD) logic.
 */
public class PaymentActivity extends AppCompatActivity {

    private ActivityPaymentBinding binding;
    private CartRepository cartRepository;
    private OrderRepository orderRepository;
    private double totalAmount;
    private String addressId;
    private String deliverySlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartRepository = new CartRepository(this);
        orderRepository = new OrderRepository(this);

        addressId = getIntent().getStringExtra("address_id");
        deliverySlot = getIntent().getStringExtra("delivery_slot");

        setupToolbar();
        calculateSummary();

        binding.btnPayOnline.setVisibility(View.GONE); // Razorpay skipped for now
        binding.btnPayCod.setOnClickListener(v -> handlePlaceOrder());
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void calculateSummary() {
        double subtotal = cartRepository.getCartTotal();
        double deliveryFee = 50.0; // Flat fee for now
        totalAmount = subtotal + deliveryFee;

        binding.tvSubtotal.setText(String.format("₹%.2f", subtotal));
        binding.tvDeliveryFee.setText(String.format("₹%.2f", deliveryFee));
        binding.tvTotalAmount.setText(String.format("₹%.2f", totalAmount));
    }

    private void handlePlaceOrder() {
        Order order = new Order();
        order.setUserId(SessionManager.getInstance(this).getUserId());
        order.setFloristId(cartRepository.getCartShopId());
        order.setAddressId(addressId);
        order.setTotalAmount(totalAmount);
        order.setDeliverySlot(deliverySlot);
        order.setStatus("placed");
        order.setPaymentStatus("pending");
        order.setBouquetSubtotal(cartRepository.getCartTotal());
        order.setDeliveryFee(50.0);

        List<CartItem> cartItems = cartRepository.getCartItems().getValue();
        List<OrderItem> orderItems = new ArrayList<>();

        if (cartItems != null) {
            for (CartItem ci : cartItems) {
                OrderItem oi = new OrderItem();
                oi.setProductId(ci.getProduct().getId());
                oi.setQuantity(ci.getQuantity());
                oi.setUnitPrice(ci.getProduct().getPrice());
                oi.setSize(ci.getSize());
                oi.setCardMessage(ci.getCardMessage());
                orderItems.add(oi);
            }
        }
        order.setItems(orderItems);

        orderRepository.placeOrder(order).observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                setLoading(true);
                binding.tvError.setVisibility(View.GONE);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                setLoading(false);
                binding.tvError.setVisibility(View.GONE);
                cartRepository.clearCart();
                Intent intent = new Intent(this, OrderConfirmationActivity.class);
                intent.putExtra("order_id", result.data != null ? result.data.getId() : "");
                intent.putExtra("shop_name", "Florist");
                startActivity(intent);
                finishAffinity();
            } else if (result.status == NetworkResult.Status.ERROR) {
                setLoading(false);
                binding.tvError.setText(result.message != null ? result.message : "Failed to place order. Please try again.");
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnPayCod.setEnabled(!isLoading);
    }
}
