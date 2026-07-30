package com.bloom.customer.ui.lux;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAPI;
import com.bloom.customer.data.model.Product;
import com.bloom.customer.ui.product.ProductDetailActivity;
import com.bloom.customer.ui.shop.ProductGridAdapter;
import com.bloom.databinding.ActivityLuxBinding;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LuxActivity extends AppCompatActivity {

    private ActivityLuxBinding binding;
    private ProductGridAdapter adapter;
    private SupabaseAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLuxBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        api = RetrofitClient.getClient(this).create(SupabaseAPI.class);

        setupRecyclerView();
        setupListeners();
        fetchLuxProducts();
    }

    private void setupRecyclerView() {
        adapter = new ProductGridAdapter();
        binding.rvLuxProducts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvLuxProducts.setAdapter(adapter);

        adapter.setOnProductClickListener((product, isOpen) -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_json", new Gson().toJson(product));
            intent.putExtra("is_shop_open", true);
            startActivity(intent);
        });
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void fetchLuxProducts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvLuxProducts.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.GONE);

        // Fetch products where is_lux = true
        api.searchProducts(null, null, true).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    adapter.setProducts(response.body(), true);
                    binding.rvLuxProducts.setVisibility(View.VISIBLE);
                } else {
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}