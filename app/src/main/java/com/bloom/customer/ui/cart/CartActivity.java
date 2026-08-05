package com.bloom.customer.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.customer.data.repository.CartRepository;
import com.bloom.databinding.ActivityCartBinding;

import java.util.List;

/**
 * Activity for displaying and managing the shopping cart.
 * Principle: Separation of Concerns - UI logic only.
 */
public class CartActivity extends AppCompatActivity {

    private static final double DELIVERY_FEE = 5.00;

    private ActivityCartBinding binding;
    private CartRepository cartRepository;
    private CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartRepository = CartRepository.getInstance(this);

        setupToolbar();
        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter();
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCartItems.setAdapter(adapter);

        adapter.setListener(new CartAdapter.OnCartItemInteractionListener() {
            @Override
            public void onRemove(int position) {
                cartRepository.removeFromCart(position);
            }

            @Override
            public void onUpdateQuantity(int position, int newQuantity) {
                List<com.bloom.customer.data.model.CartItem> currentItems = cartRepository.getCartItems().getValue();
                if (currentItems != null && position < currentItems.size()) {
                    currentItems.get(position).setQuantity(newQuantity);
                    cartRepository.updateCart(currentItems);
                }
            }
        });
    }

    private void setupObservers() {
        cartRepository.getCartItems().observe(this, items -> {
            if (items == null || items.isEmpty()) {
                binding.cartScrollView.setVisibility(View.GONE);
                binding.bottomBar.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            } else {
                binding.cartScrollView.setVisibility(View.VISIBLE);
                binding.bottomBar.setVisibility(View.VISIBLE);
                binding.emptyState.setVisibility(View.GONE);
                adapter.setItems(items);
                updateSummary();
            }
        });
    }

    private void setupListeners() {
        binding.btnStartShopping.setOnClickListener(v -> finish());

        binding.btnApplyPromo.setOnClickListener(v ->
                Toast.makeText(this, "Promo code applied", Toast.LENGTH_SHORT).show());
        
        binding.btnCheckout.setOnClickListener(v -> {
            startActivity(new Intent(this, com.bloom.customer.ui.checkout.AddressSelectActivity.class));
        });
    }

    private void updateSummary() {
        double subtotal = cartRepository.getCartTotal();
        double total = subtotal + DELIVERY_FEE;

        binding.tvSubtotalAmount.setText(formatCurrency(subtotal));
        binding.tvDeliveryFeeAmount.setText(formatCurrency(DELIVERY_FEE));
        binding.tvTotalAmount.setText(formatCurrency(total));
    }

    private String formatCurrency(double amount) {
        return "₹" + String.format("%.2f", amount);
    }
}
