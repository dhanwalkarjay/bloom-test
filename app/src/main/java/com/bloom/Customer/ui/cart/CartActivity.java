package com.bloom.customer.ui.cart;

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

    private ActivityCartBinding binding;
    private CartRepository cartRepository;
    private CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartRepository = new CartRepository(this);

        setupToolbar();
        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
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
                binding.rvCartItems.setVisibility(View.GONE);
                binding.bottomBar.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            } else {
                binding.rvCartItems.setVisibility(View.VISIBLE);
                binding.bottomBar.setVisibility(View.VISIBLE);
                binding.emptyState.setVisibility(View.GONE);
                adapter.setItems(items);
                binding.tvTotalAmount.setText("$" + String.format("%.2f", cartRepository.getCartTotal()));
            }
        });
    }

    private void setupListeners() {
        binding.btnStartShopping.setOnClickListener(v -> finish());
        
        binding.btnCheckout.setOnClickListener(v -> {
            // TODO: Navigate to AddressSelectActivity
            Toast.makeText(this, "Proceeding to checkout...", Toast.LENGTH_SHORT).show();
        });
    }
}
