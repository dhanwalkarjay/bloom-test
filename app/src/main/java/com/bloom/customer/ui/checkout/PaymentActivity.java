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
import com.bloom.customer.data.model.Shop;
import com.bloom.customer.data.model.Address;
import com.bloom.customer.data.repository.CartRepository;
import com.bloom.customer.data.repository.OrderRepository;
import com.bloom.customer.data.repository.ShopRepository;
import com.bloom.customer.data.repository.AddressRepository;
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
    private ShopRepository shopRepository;
    private AddressRepository addressRepository;
    private double totalAmount;
    private double deliveryFee = 50.0;
    private String addressId;
    private String deliverySlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartRepository = com.bloom.customer.data.repository.CartRepository.getInstance(this);
        orderRepository = new OrderRepository(this);
        shopRepository = new ShopRepository(this);
        addressRepository = new AddressRepository(this);

        addressId = getIntent().getStringExtra("address_id");
        deliverySlot = getIntent().getStringExtra("delivery_slot");

        setupToolbar();
        fetchDetailsAndCalculate();

        binding.btnPayOnline.setVisibility(View.GONE); // Razorpay skipped for now
        binding.btnPayCod.setOnClickListener(v -> handlePlaceOrder());
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void fetchDetailsAndCalculate() {
        String shopId = cartRepository.getCartShopId();
        if (shopId == null) return;

        shopRepository.getShopById(shopId).observe(this, shopResult -> {
            if (shopResult.status == NetworkResult.Status.SUCCESS && shopResult.data != null) {
                Shop shop = shopResult.data;
                addressRepository.getAddressById(addressId).observe(this, addrResult -> {
                    if (addrResult.status == NetworkResult.Status.SUCCESS && addrResult.data != null) {
                        calculateDeliveryFee(shop, addrResult.data);
                    }
                });
            }
        });
    }

    private void calculateDeliveryFee(Shop shop, Address address) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(
                shop.getLatitude(), shop.getLongitude(),
                address.getLatitude(), address.getLongitude(),
                results
        );
        double distanceKm = results[0] / 1000.0;
        deliveryFee = Math.max(20.0, distanceKm * 10.0); // ₹10 per km, min ₹20

        updateSummary();
    }

    private void updateSummary() {
        double subtotal = cartRepository.getCartTotal();
        totalAmount = subtotal + deliveryFee;

        binding.tvSubtotal.setText(String.format("₹%.2f", subtotal));
        binding.tvDeliveryFee.setText(String.format("₹%.2f", deliveryFee));
        binding.tvTotalAmount.setText(String.format("₹%.2f", totalAmount));
    }

    private void calculateSummary() {
        updateSummary();
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
        order.setDeliveryFee(deliveryFee);

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
