package com.bloom.customer.ui.shop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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
        cartRepository = new CartRepository(this);

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
    }

    private void setupRecyclerView() {
        adapter = new ProductGridAdapter();
        binding.rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvProducts.setAdapter(adapter);

        adapter.setOnProductClickListener(product -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_json", new Gson().toJson(product));
            startActivity(intent);
        });
    }

    private void setupObservers() {
        viewModel.getProducts().observe(this, result -> {
            if (result.status == NetworkResult.Status.SUCCESS) {
                adapter.setProducts(result.data);
            } else if (result.status == NetworkResult.Status.ERROR) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });

        cartRepository.getCartItems().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                binding.btnViewCart.setVisibility(View.VISIBLE);
                binding.btnViewCart.setText("View Cart (" + items.size() + " items)");
            } else {
                binding.btnViewCart.setVisibility(View.GONE);
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
