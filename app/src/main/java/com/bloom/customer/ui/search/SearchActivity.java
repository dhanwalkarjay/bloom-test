package com.bloom.customer.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bloom.R;
import com.bloom.R;
import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAPI;
import com.bloom.customer.data.model.Product;
import com.bloom.customer.data.model.ProductSearchResult;
import com.bloom.customer.ui.product.ProductDetailActivity;
import com.bloom.customer.ui.shop.ProductGridAdapter;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivitySearchBinding;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private ProductGridAdapter adapter;
    private SupabaseAPI api;
    private String category;
    private double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        api = RetrofitClient.getClient(this).create(SupabaseAPI.class);
        category = getIntent().getStringExtra("category");
        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);

        setupRecyclerView();
        setupListeners();

        if (category != null) {
            binding.etSearch.setText(category);
            fetchProducts(null, category);
        }
    }

    private void setupRecyclerView() {
        adapter = new ProductGridAdapter();
        binding.rvSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvSearchResults.setAdapter(adapter);

        adapter.setOnProductClickListener((product, isOpen) -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_json", new Gson().toJson(product));
            intent.putExtra("is_shop_open", true); // Search results are global, assume open or handle per item
            startActivity(intent);
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = binding.etSearch.getText().toString().trim();
                fetchProducts(query, null);
                return true;
            }
            return false;
        });
    }

    private void fetchProducts(String query, String cat) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.GONE);

        Map<String, Object> body = new HashMap<>();
        body.put("lat", lat);
        body.put("lng", lng);
        if (query != null && !query.isEmpty()) body.put("search_query", query);
        if (cat != null && !cat.isEmpty()) body.put("cat_filter", cat);

        api.searchProductsNearby(body).enqueue(new Callback<List<ProductSearchResult>>() {
            @Override
            public void onResponse(Call<List<ProductSearchResult>> call, Response<List<ProductSearchResult>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<Product> products = new ArrayList<>();
                    for (ProductSearchResult res : response.body()) {
                        products.add(res.toProduct());
                    }
                    adapter.setProducts(products, true); // Search results already filter open shops if needed, or we can use shop_open flag
                    binding.rvSearchResults.setVisibility(View.VISIBLE);
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<ProductSearchResult>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        binding.emptyState.setVisibility(View.VISIBLE);
        // "Currently Unavailable" message if it was a valid location search
        if (lat != 0 && lng != 0) {
            binding.tvEmptyTitle.setText(R.string.currently_unavailable);
            binding.tvEmptySubtitle.setText(R.string.no_matches_location);
        } else {
            binding.tvEmptyTitle.setText(R.string.no_products_found);
            binding.tvEmptySubtitle.setText(R.string.try_different_search);
        }
    }
}