package com.bloom.customer.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.customer.data.model.CartItem;
import com.bloom.customer.util.CurrencyFormatter;
import com.bloom.databinding.ActivityCartBinding;
import com.bloom.R;

import android.view.Window;
import android.view.WindowManager;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.List;

/**
 * Activity for displaying and managing the shopping cart.
 * Principle: Separation of Concerns - UI logic only.
 */
public class CartActivity extends AppCompatActivity {

    private static final double DELIVERY_FEE = 5.00;

    private ActivityCartBinding binding;
    private CartViewModel viewModel;
    private CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.cart_background));
        WindowCompat.setDecorFitsSystemWindows(window, true);
        WindowCompat.getInsetsController(window, window.getDecorView()).setAppearanceLightStatusBars(true);

        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CartViewModel.class);

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

    @Override
    protected void onResume() {
        super.onResume();
        fetchLivePrices();
    }

    private void fetchLivePrices() {
        String shopId = viewModel.getCartShopId();
        if (shopId != null && !shopId.isEmpty()) {
            viewModel.getProductsByShop(shopId).observe(this, result -> {
                if (result.status == com.bloom.customer.util.NetworkResult.Status.SUCCESS && result.data != null) {
                    List<com.bloom.customer.data.model.Product> liveProducts = result.data;
                    List<CartItem> cartItems = viewModel.getCartItems().getValue();
                    if (cartItems != null && !cartItems.isEmpty()) {
                        boolean pricesChanged = false;
                        for (CartItem item : cartItems) {
                            for (com.bloom.customer.data.model.Product liveProduct : liveProducts) {
                                if (item.getProduct().getId().equals(liveProduct.getId())) {
                                    if (item.getProduct().getPrice() != liveProduct.getPrice()) {
                                        item.getProduct().setPrice(liveProduct.getPrice());
                                        pricesChanged = true;
                                    }
                                    break;
                                }
                            }
                        }
                        if (pricesChanged) {
                            viewModel.updateCart(cartItems);
                            adapter.setItems(cartItems);
                            updateSummary();
                            Toast.makeText(this, "Prices for some items have been updated.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter();
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCartItems.setAdapter(adapter);

        adapter.setListener(new CartAdapter.OnCartItemInteractionListener() {
            @Override
            public void onRemove(int position) {
                viewModel.removeFromCart(position);
            }

            @Override
            public void onUpdateQuantity(int position, int newQuantity) {
                List<CartItem> currentItems = viewModel.getCartItems().getValue();
                if (currentItems != null && position < currentItems.size()) {
                    currentItems.get(position).setQuantity(newQuantity);
                    viewModel.updateCart(currentItems);
                }
            }
        });
    }

    private void setupObservers() {
        viewModel.getCartItems().observe(this, items -> {
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

        if (binding.tilPromo != null) {
            binding.tilPromo.setEndIconOnClickListener(v -> {
                String code = binding.etPromoCode.getText() != null ? binding.etPromoCode.getText().toString() : "";
                if (!code.isEmpty()) {
                    Toast.makeText(this, "Promo code applied", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        binding.btnCheckout.setOnClickListener(v -> {
            com.bloom.customer.util.HapticUtil.performSuccess(this);
            DeliveryOptionsBottomSheet bottomSheet = new DeliveryOptionsBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "DeliveryOptions");
        });
    }

    private void updateSummary() {
        double subtotal = viewModel.getCartTotal();
        double total = subtotal + DELIVERY_FEE;

        binding.tvSubtotalAmount.setText(CurrencyFormatter.format(subtotal));
        binding.tvDeliveryFeeAmount.setText(CurrencyFormatter.format(DELIVERY_FEE));
        binding.tvTotalAmount.setText(CurrencyFormatter.format(total));
    }
}
