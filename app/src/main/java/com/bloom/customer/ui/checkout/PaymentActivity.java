package com.bloom.customer.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bloom.R;
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
    private String selectedMethod = "CARD";

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
        setupMethods();
        fetchDetailsAndCalculate();

        binding.btnPayNow.setOnClickListener(v -> handlePlaceOrder());
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupMethods() {
        binding.cardCard.setOnClickListener(v -> selectMethod("CARD"));
        binding.cardWallet.setOnClickListener(v -> selectMethod("WALLET"));
        binding.cardBank.setOnClickListener(v -> selectMethod("BANK"));
        binding.cardCod.setOnClickListener(v -> selectMethod("COD"));
    }

    private void selectMethod(String method) {
        selectedMethod = method;
        
        resetMethodUI(binding.cardCard, binding.rbCard);
        resetMethodUI(binding.cardWallet, binding.rbWallet);
        resetMethodUI(binding.cardBank, binding.rbBank);
        resetMethodUI(binding.cardCod, binding.rbCod);
        
        switch (method) {
            case "CARD":
                highlightMethod(binding.cardCard, binding.rbCard);
                break;
            case "WALLET":
                highlightMethod(binding.cardWallet, binding.rbWallet);
                break;
            case "BANK":
                highlightMethod(binding.cardBank, binding.rbBank);
                break;
            case "COD":
                highlightMethod(binding.cardCod, binding.rbCod);
                break;
        }
    }

    private void resetMethodUI(com.google.android.material.card.MaterialCardView card, android.widget.RadioButton rb) {
        card.setStrokeColor(ContextCompat.getColor(this, R.color.orders_outline_variant));
        card.setStrokeWidth(2);
        rb.setChecked(false);
        rb.setButtonTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orders_on_surface_variant)));
    }

    private void highlightMethod(com.google.android.material.card.MaterialCardView card, android.widget.RadioButton rb) {
        card.setStrokeColor(ContextCompat.getColor(this, R.color.orders_primary));
        card.setStrokeWidth(4);
        rb.setChecked(true);
        rb.setButtonTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orders_primary)));
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
        deliveryFee = Math.max(20.0, distanceKm * 10.0);
        updateSummary();
    }

    private void updateSummary() {
        double subtotal = cartRepository.getCartTotal();
        totalAmount = subtotal + deliveryFee + 10.0; // Including platform fee
        binding.tvTotalAmount.setText(String.format("₹%.2f", totalAmount));
        
        // Breakdown in accordion
        View breakdown = binding.getRoot().findViewById(R.id.llOrderDetails);
        if (breakdown != null) {
            TextView tvSub = breakdown.findViewById(R.id.tvSubtotal);
            TextView tvDel = breakdown.findViewById(R.id.tvDeliveryFee);
            if (tvSub != null) tvSub.setText(String.format("₹%.2f", subtotal));
            if (tvDel != null) tvDel.setText(String.format("₹%.2f", deliveryFee));
        }

        List<CartItem> items = cartRepository.getCartItems().getValue();
        if (items != null && !items.isEmpty()) {
            String briefText = items.size() + "x '" + items.get(0).getProduct().getName() + "'";
            if (items.size() > 1) briefText += " + " + (items.size() - 1) + " items";
            binding.tvOrderSummaryBrief.setText(briefText);
        }

        binding.llOrderSummaryClickable.setOnClickListener(v -> {
            boolean isVisible = binding.llOrderDetails.getVisibility() == View.VISIBLE;
            binding.llOrderDetails.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            binding.ivSummaryArrow.animate().rotation(isVisible ? 0 : 180).start();
        });
    }

    private void handlePlaceOrder() {
        Order order = new Order();
        order.setUserId(SessionManager.getInstance(this).getUserId());
        order.setFloristId(cartRepository.getCartShopId());
        order.setAddressId(addressId);
        order.setTotalAmount(totalAmount);
        order.setDeliverySlot(deliverySlot);
        order.setStatus("placed");
        order.setPaymentStatus(selectedMethod.equals("COD") ? "pending" : "paid");
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

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnPayNow.setEnabled(false);

        orderRepository.placeOrder(order).observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            if (result.status == NetworkResult.Status.SUCCESS) {
                cartRepository.clearCart();
                Intent intent = new Intent(this, OrderConfirmationActivity.class);
                intent.putExtra("order_id", result.data != null ? result.data.getId() : "");
                intent.putExtra("shop_name", "Florist");
                startActivity(intent);
                finishAffinity();
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.btnPayNow.setEnabled(true);
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
