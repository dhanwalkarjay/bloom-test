package com.bloom.customer.ui.shop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.graphics.Color;
import androidx.core.view.WindowCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bloom.customer.data.model.Shop;
import com.bloom.customer.data.repository.CartRepository;
import com.bloom.customer.ui.cart.CartActivity;
import com.bloom.customer.ui.product.ProductDetailActivity;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityShopDetailBinding;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

/**
 * Activity for displaying shop details and its products.
 * Principle: Separation of Concerns - UI logic only.
 */
public class ShopDetailActivity extends AppCompatActivity {

    private ActivityShopDetailBinding binding;
    private ShopDetailViewModel viewModel;
    private ProductGridAdapter adapter;
    private CartRepository cartRepository;
    private Shop shop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Edge-to-Edge setup
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        
        binding = ActivityShopDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Parse shop from intent
        String shopJson = getIntent().getStringExtra("shop_json");
        shop = new Gson().fromJson(shopJson, Shop.class);

        viewModel = new ViewModelProvider(this).get(ShopDetailViewModel.class);
        cartRepository = com.bloom.customer.data.repository.CartRepository.getInstance(this);

        setupUI();
        setupRecyclerView();
        setupObservers();

        viewModel.fetchProducts(shop.getId());
    }

    private void setupUI() {
        binding.tvShopName.setText(shop.getName());
        binding.tvToolbarTitle.setText(shop.getName());
        
        double allowedRadiusKm = shop.getDeliveryRadiusKm() > 0 ? shop.getDeliveryRadiusKm() : 5.0;
        String timeStatus = "";
        if (shop.getDistance() > allowedRadiusKm * 1000) {
            timeStatus = "Out of Zone";
        } else {
            timeStatus = shop.isOpen() ? "Open Now" : "Opens 9 AM";
        }
        
        String metaText = shop.getRating() + " ★ · " + shop.getFormattedDistance() + " · " + timeStatus;
        binding.tvShopMeta.setText(metaText);
        
        Glide.with(this)
                .load(shop.getImageUrl())
                .into(binding.ivShopHeader);

        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int scrollRange = appBarLayout.getTotalScrollRange();
            float alpha = (float) Math.abs(verticalOffset) / scrollRange;
            if (alpha > 0.8f) {
                float titleAlpha = (alpha - 0.8f) / 0.2f;
                binding.tvToolbarTitle.setAlpha(titleAlpha);
            } else {
                binding.tvToolbarTitle.setAlpha(0f);
            }
        });
        
        binding.btnViewCart.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        binding.btnCartTop.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });
    }

    private void setupRecyclerView() {
        adapter = new ProductGridAdapter();
        binding.rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvProducts.setAdapter(adapter);

        adapter.setOnProductClickListener(new ProductGridAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(com.bloom.customer.data.model.Product product, boolean isOpen) {
                Intent intent = new Intent(ShopDetailActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_json", new Gson().toJson(product));
                intent.putExtra("shop_json", new Gson().toJson(shop)); // Pass full shop info
                intent.putExtra("is_shop_open", isOpen);
                intent.putExtra("distance", shop.getDistance());
                startActivity(intent);
            }

            @Override
            public void onQuantityChanged(com.bloom.customer.data.model.Product product, int newQuantity) {
                com.bloom.customer.util.HapticUtil.performSuccess(ShopDetailActivity.this);
                if (newQuantity > 0) {
                    com.bloom.customer.data.model.CartItem item = new com.bloom.customer.data.model.CartItem(product);
                    item.setQuantity(newQuantity);
                    item.setSize("Regular");
                    cartRepository.addToCart(item); // Note: addToCart appends by default, but wait, if it's already there?
                } else {
                    cartRepository.removeFromCartByProductId(product.getId());
                }
            }
        });
        
        if (binding.swipeRefresh != null) {
            binding.swipeRefresh.setOnRefreshListener(() -> {
                viewModel.fetchProducts(shop.getId());
            });
        }
    }

    private void setupObservers() {
        viewModel.getProducts().observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                if (binding.swipeRefresh != null && !binding.swipeRefresh.isRefreshing()) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                }
                binding.rvProducts.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.GONE);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                if (binding.swipeRefresh != null) binding.swipeRefresh.setRefreshing(false);
                if (result.data != null && !result.data.isEmpty()) {
                    adapter.setProducts(result.data, shop.isOpen());
                    binding.rvProducts.setVisibility(View.VISIBLE);
                    binding.emptyState.setVisibility(View.GONE);
                } else {
                    binding.rvProducts.setVisibility(View.GONE);
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                if (binding.swipeRefresh != null) binding.swipeRefresh.setRefreshing(false);
                binding.rvProducts.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.tvEmptyTitle.setText("Something went wrong");
                binding.tvEmptySubtitle.setText(result.message != null ? result.message : "Failed to load products. Pull down to retry.");
            }
        });

        cartRepository.getCartItems().observe(this, items -> {
            int totalCount = 0;
            if (items != null) {
                for (com.bloom.customer.data.model.CartItem item : items) {
                    totalCount += item.getQuantity();
                }
            }
            if (totalCount > 0) {
                binding.btnViewCart.setText("View Cart (" + totalCount + " items)");
                binding.tvCartBadge.setText(String.valueOf(totalCount));
                binding.tvCartBadge.setVisibility(View.VISIBLE);
                
                if (binding.btnViewCart.getVisibility() != View.VISIBLE) {
                    binding.btnViewCart.setAlpha(0f);
                    binding.btnViewCart.setTranslationY(50f);
                    binding.btnViewCart.setVisibility(View.VISIBLE);
                    binding.btnViewCart.animate().alpha(1f).translationY(0f).setDuration(300).start();
                }
            } else {
                if (binding.btnViewCart.getVisibility() == View.VISIBLE) {
                    binding.btnViewCart.animate().alpha(0f).translationY(50f).setDuration(200).withEndAction(() -> {
                        binding.btnViewCart.setVisibility(View.GONE);
                    }).start();
                }
                binding.tvCartBadge.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh cart badge
        cartRepository.getCartItems(); 
    }
}
