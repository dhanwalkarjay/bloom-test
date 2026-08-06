package com.bloom.customer.ui.shop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
        binding.tvShopDetails.setText("★ " + shop.getRating() + " • " + shop.getFormattedDistance());
        
        Glide.with(this)
                .load(shop.getImageUrl())
                .into(binding.ivShopHeader);

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
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

        adapter.setOnProductClickListener((product, isOpen) -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_json", new Gson().toJson(product));
            intent.putExtra("shop_json", new Gson().toJson(shop)); // Pass full shop info
            intent.putExtra("is_shop_open", isOpen);
            intent.putExtra("distance", shop.getDistance());
            startActivity(intent);
        });
    }

    private void setupObservers() {
        viewModel.getProducts().observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.rvProducts.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.GONE);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
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
                binding.btnViewCart.setVisibility(View.VISIBLE);
                binding.btnViewCart.setText("View Cart (" + totalCount + " items)");
                
                binding.tvCartBadge.setVisibility(View.VISIBLE);
                binding.tvCartBadge.setText(String.valueOf(totalCount));
            } else {
                binding.btnViewCart.setVisibility(View.GONE);
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
