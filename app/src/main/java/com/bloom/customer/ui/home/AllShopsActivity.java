package com.bloom.customer.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.R;
import com.bloom.customer.data.model.Shop;
import com.bloom.customer.ui.shop.ShopDetailActivity;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityAllShopsBinding;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllShopsActivity extends AppCompatActivity {

    private ActivityAllShopsBinding binding;
    private HomeViewModel viewModel;
    private ShopVerticalListAdapter adapter;

    private List<Shop> allShops = new ArrayList<>();
    
    // Filter states
    private boolean isSortOpen = false;
    private boolean isFilterOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#111111"));
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);

        binding = ActivityAllShopsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupRecyclerView();
        setupListeners();
        fetchShops();
    }

    private void setupRecyclerView() {
        adapter = new ShopVerticalListAdapter();
        binding.rvAllShops.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAllShops.setAdapter(adapter);

        adapter.setOnShopClickListener(shop -> {
            Intent intent = new Intent(this, ShopDetailActivity.class);
            intent.putExtra("shop_json", new Gson().toJson(shop));
            startActivity(intent);
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnSortBy.setOnClickListener(v -> toggleSortDropdown());
        binding.btnFilter.setOnClickListener(v -> toggleFilterDropdown());
        
        binding.dimOverlay.setOnClickListener(v -> {
            if (isSortOpen) toggleSortDropdown();
            if (isFilterOpen) toggleFilterDropdown();
        });
        
        binding.btnApplySort.setOnClickListener(v -> {
            applySortLogic();
            toggleSortDropdown();
        });

        binding.btnApplyFilter.setOnClickListener(v -> {
            applyFilterLogic();
            toggleFilterDropdown();
        });
        
        binding.rsPriceRange.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            int min = values.get(0).intValue();
            int max = values.get(1).intValue();
            String maxText = max >= 300 ? "$300+" : "$" + max;
            binding.tvPriceRangeValue.setText("$" + min + " - " + maxText);
        });

        binding.etSearchShop.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                applyFilters();
            }
        });

        binding.btnClearFilters.setOnClickListener(v -> {
            binding.etSearchShop.setText("");
            // Other filters could be reset here
            applyFilters();
        });
    }

    private void toggleSortDropdown() {
        if (isFilterOpen) {
            closeDropdown(binding.filterDropdownPanel);
            isFilterOpen = false;
        }

        isSortOpen = !isSortOpen;
        if (isSortOpen) {
            openDropdown(binding.sortDropdownPanel);
            updateButtonState(binding.btnSortBy, true);
        } else {
            closeDropdown(binding.sortDropdownPanel);
            updateButtonState(binding.btnSortBy, false);
        }
    }

    private void toggleFilterDropdown() {
        if (isSortOpen) {
            closeDropdown(binding.sortDropdownPanel);
            isSortOpen = false;
        }

        isFilterOpen = !isFilterOpen;
        if (isFilterOpen) {
            openDropdown(binding.filterDropdownPanel);
            updateButtonState(binding.btnFilter, true);
        } else {
            closeDropdown(binding.filterDropdownPanel);
            updateButtonState(binding.btnFilter, false);
        }
    }

    private void openDropdown(View panel) {
        binding.dimOverlay.setVisibility(View.VISIBLE);
        panel.setVisibility(View.VISIBLE);
        
        if (binding.dimOverlay.getAlpha() == 0f) {
            binding.dimOverlay.animate().alpha(1f).setDuration(300).start();
        }

        panel.post(() -> {
            panel.setTranslationY(-panel.getHeight());
            panel.animate().translationY(0).setDuration(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
        });
    }

    private void closeDropdown(View panel) {
        if (!isSortOpen && !isFilterOpen) {
            binding.dimOverlay.animate().alpha(0f).setDuration(300).withEndAction(() -> 
                binding.dimOverlay.setVisibility(View.GONE)).start();
        }
        
        panel.animate().translationY(-panel.getHeight()).setDuration(300)
            .setInterpolator(new android.view.animation.AccelerateInterpolator()).withEndAction(() -> 
            panel.setVisibility(View.GONE)).start();
    }

    private void updateButtonState(TextView button, boolean isActive) {
        if (isActive) {
            button.setBackgroundResource(R.drawable.bg_chip_premium_active);
            button.setTextColor(Color.parseColor("#111111"));
            // Keep the icon proper tint
        } else {
            button.setBackgroundResource(R.drawable.bg_chip_premium_inactive);
            button.setTextColor(Color.WHITE);
        }
    }

    private void applySortLogic() {
        int checkedId = binding.rgSortCriteria.getCheckedRadioButtonId();
        int orderId = binding.rgSortOrder.getCheckedRadioButtonId();
        boolean isAsc = (orderId == R.id.rbSortAsc);
        
        if (checkedId == R.id.rbSortRating) {
            Collections.sort(allShops, (s1, s2) -> isAsc ? Double.compare(s1.getRating(), s2.getRating()) : Double.compare(s2.getRating(), s1.getRating()));
        } else if (checkedId == R.id.rbSortDistance) {
            Collections.sort(allShops, (s1, s2) -> isAsc ? Double.compare(s1.getDistance(), s2.getDistance()) : Double.compare(s2.getDistance(), s1.getDistance()));
        } else {
            // Relevance
            Collections.sort(allShops, (s1, s2) -> isAsc ? s2.getName().compareTo(s1.getName()) : s1.getName().compareTo(s2.getName())); 
        }
        
        applyFilters();
    }

    private void applyFilterLogic() {
        // Just mock apply for UI validation
        applyFilters();
    }

    private void fetchShops() {
        viewModel.getUserLocation().observe(this, location -> {
            if (location != null && !viewModel.hasManualLocation()) {
                fetchShopsFromApi(location.getLatitude(), location.getLongitude());
            }
        });
        
        if (viewModel.hasManualLocation()) {
            fetchShopsFromApi(viewModel.getManualLat(), viewModel.getManualLng());
        } else {
            viewModel.refreshLocation();
        }
    }

    private void fetchShopsFromApi(double lat, double lng) {
        viewModel.getNearbyShops(lat, lng).observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.rvAllShops.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.GONE);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                if (result.data != null && !result.data.isEmpty()) {
                    allShops.clear();
                    allShops.addAll(result.data);
                    binding.rvAllShops.setVisibility(View.VISIBLE);
                    applyFilters();
                } else {
                    binding.rvAllShops.setVisibility(View.GONE);
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                binding.rvAllShops.setVisibility(View.GONE);
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        String query = binding.etSearchShop.getText() != null ? binding.etSearchShop.getText().toString().toLowerCase().trim() : "";
        List<Shop> filteredList = new ArrayList<>();
        
        for (Shop shop : allShops) {
            if (query.isEmpty() || shop.getName().toLowerCase().contains(query)) {
                filteredList.add(shop);
            }
        }
        
        if (filteredList.isEmpty() && !allShops.isEmpty()) {
            binding.rvAllShops.setVisibility(View.GONE);
            binding.emptyState.setVisibility(View.VISIBLE);
        } else if (!filteredList.isEmpty()) {
            binding.rvAllShops.setVisibility(View.VISIBLE);
            binding.emptyState.setVisibility(View.GONE);
        }
        
        adapter.setShops(filteredList);
    }
}
