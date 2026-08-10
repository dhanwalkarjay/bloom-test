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
        // Inject 3 premium sample shops for the UI demonstration
        binding.progressBar.setVisibility(View.GONE);
        binding.rvAllShops.setVisibility(View.VISIBLE);
        allShops.clear();

        Shop shop1 = new Shop();
        shop1.setId("s1");
        shop1.setName("Luxe Florals & Co.");
        shop1.setRating(4.9);
        shop1.setDistance(800);
        shop1.setPrepTime("20-30 mins");
        shop1.setOpen(true);
        shop1.setImageUrl("https://images.unsplash.com/photo-1563241527-3004b7be0ffd?w=800&q=80"); // Elegant premium bouquet
        
        Shop shop2 = new Shop();
        shop2.setId("s2");
        shop2.setName("Midnight Rose Studio");
        shop2.setRating(4.8);
        shop2.setDistance(1500);
        shop2.setPrepTime("15-25 mins");
        shop2.setOpen(true);
        shop2.setImageUrl("https://images.unsplash.com/photo-1582794543139-8ac9cb4f5544?w=800&q=80"); // Dark, moody, luxurious roses
        
        Shop shop3 = new Shop();
        shop3.setId("s3");
        shop3.setName("The Artisan Bloom");
        shop3.setRating(4.6);
        shop3.setDistance(2100);
        shop3.setPrepTime("30-45 mins");
        shop3.setOpen(true);
        shop3.setImageUrl("https://images.unsplash.com/photo-1591886960571-74d43a9d4166?w=800&q=80"); // Bright artisanal shop

        allShops.add(shop1);
        allShops.add(shop2);
        allShops.add(shop3);
        
        applyFilters();
    }

    private void applyFilters() {
        String query = binding.etSearchShop.getText() != null ? binding.etSearchShop.getText().toString().toLowerCase().trim() : "";
        List<Shop> filteredList = new ArrayList<>();
        
        for (Shop shop : allShops) {
            if (query.isEmpty() || shop.getName().toLowerCase().contains(query)) {
                filteredList.add(shop);
            }
        }
        
        adapter.setShops(filteredList);
    }
}
