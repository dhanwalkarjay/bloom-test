package com.bloom.customer.ui.explore;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bloom.R;
import com.bloom.customer.data.model.Product;
import com.bloom.customer.data.model.ProductSearchResult;
import com.bloom.customer.ui.cart.CartActivity;
import com.bloom.customer.ui.common.FragmentStatusBar;
import com.bloom.customer.ui.home.HomeViewModel;
import com.bloom.customer.ui.home.MainSharedViewModel;
import com.bloom.customer.ui.product.ProductDetailActivity;
import com.bloom.customer.ui.shop.ProductGridAdapter;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.FragmentSearchBinding;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private FragmentSearchBinding binding;
    private MainSharedViewModel sharedViewModel;
    private HomeViewModel homeViewModel;
    private ExploreViewModel viewModel;
    private ProductGridAdapter productAdapter;

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private String currentCategory = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(MainSharedViewModel.class);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);

        FragmentStatusBar.applyTopInset(this, binding.topBar);
        
        setupRecyclerView();
        setupListeners();
        setupSearchObservation();
        
        binding.swipeRefresh.setOnRefreshListener(() -> {
            String query = binding.etSearch.getText().toString().trim();
            performSearch(query, currentCategory);
        });
    }

    private void setupRecyclerView() {
        productAdapter = new ProductGridAdapter();
        binding.rvResults.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvResults.setAdapter(productAdapter);

        productAdapter.setOnProductClickListener(new ProductGridAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(com.bloom.customer.data.model.Product product, boolean isOpen) {
                Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
                intent.putExtra("product_json", new Gson().toJson(product));
                intent.putExtra("is_shop_open", isOpen);
                startActivity(intent);
            }

            @Override
            public void onQuantityChanged(com.bloom.customer.data.model.Product product, int newQuantity) {
                com.bloom.customer.util.HapticUtil.performSuccess(requireContext());
                com.bloom.customer.data.repository.CartRepository cartRepository = com.bloom.customer.data.repository.CartRepository.getInstance(requireContext());
                if (newQuantity > 0) {
                    com.bloom.customer.data.model.CartItem item = new com.bloom.customer.data.model.CartItem(product);
                    item.setQuantity(newQuantity);
                    item.setSize("Regular");
                    cartRepository.addToCart(item);
                } else {
                    cartRepository.removeFromCartByProductId(product.getId());
                }
            }
        });
    }

    private void setupSearchObservation() {
        sharedViewModel.getSearchCategory().observe(getViewLifecycleOwner(), category -> {
            if (category != null && !category.isEmpty()) {
                // If it's a category click from home, update our currentCategory and chips
                currentCategory = category;
                binding.tvResultsLabel.setText("FOR " + category.toUpperCase());
                
                // Set text but prevent it from triggering the watcher instantly if we want to run our own
                binding.etSearch.setText("");
                
                // Update chips visually
                if ("shop".equalsIgnoreCase(category)) {
                    updateChipUI(binding.chipShops, binding.chipAll, binding.chipBouquets);
                } else if ("bouquet".equalsIgnoreCase(category)) {
                    updateChipUI(binding.chipBouquets, binding.chipAll, binding.chipShops);
                }
                
                performSearch(null, currentCategory);
            }
        });

        sharedViewModel.getSearchQuery().observe(getViewLifecycleOwner(), query -> {
            if (query != null && !query.isEmpty()) {
                currentCategory = null;
                updateChipUI(binding.chipAll, binding.chipShops, binding.chipBouquets);
                
                binding.etSearch.setText(query);
                binding.tvResultsLabel.setText("SEARCH RESULTS");
                
                // The watcher will handle the actual search since we set text, but just in case
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                performSearch(query, currentCategory);
            }
        });
    }

    private void setupListeners() {
        binding.btnCart.setOnClickListener(v -> startActivity(new Intent(requireContext(), CartActivity.class)));

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                String query = s.toString().trim();
                
                // If the user completely clears the search and has no category, instantly show discover state
                if (query.isEmpty() && (currentCategory == null || currentCategory.isEmpty())) {
                    performSearch(query, currentCategory);
                    return;
                }
                
                // Debounce network call by 500ms
                searchRunnable = () -> performSearch(query, currentCategory);
                searchHandler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                String query = binding.etSearch.getText().toString().trim();
                performSearch(query, currentCategory);
                return true;
            }
            return false;
        });

        binding.chipAll.setOnClickListener(v -> {
            currentCategory = null;
            updateChipUI(binding.chipAll, binding.chipShops, binding.chipBouquets);
            triggerSearchWithCurrentState();
        });

        binding.chipShops.setOnClickListener(v -> {
            currentCategory = "shop";
            updateChipUI(binding.chipShops, binding.chipAll, binding.chipBouquets);
            triggerSearchWithCurrentState();
        });

        binding.chipBouquets.setOnClickListener(v -> {
            currentCategory = "bouquet";
            updateChipUI(binding.chipBouquets, binding.chipAll, binding.chipShops);
            triggerSearchWithCurrentState();
        });
    }

    private void updateChipUI(TextView active, TextView inactive1, TextView inactive2) {
        active.setBackgroundResource(R.drawable.bg_search_chip_lux_active);
        active.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        
        inactive1.setBackgroundResource(R.drawable.bg_search_chip_lux_inactive);
        inactive1.setTextColor(ContextCompat.getColor(requireContext(), R.color.home_lux_dark));
        
        inactive2.setBackgroundResource(R.drawable.bg_search_chip_lux_inactive);
        inactive2.setTextColor(ContextCompat.getColor(requireContext(), R.color.home_lux_dark));
    }
    
    private void triggerSearchWithCurrentState() {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        String query = binding.etSearch.getText().toString().trim();
        performSearch(query, currentCategory);
    }

    private void performSearch(String query, String category) {
        if ((query == null || query.isEmpty()) && (category == null || category.isEmpty())) {
            if (productAdapter != null) {
                productAdapter.setProducts(new ArrayList<>(), true);
            }
            binding.resultsSection.setVisibility(View.GONE);
            binding.emptyState.setVisibility(View.GONE);
            binding.discoverState.setVisibility(View.VISIBLE);
            return;
        }

        double lat = 0, lng = 0;
        if (homeViewModel.hasManualLocation()) {
            lat = homeViewModel.getManualLat();
            lng = homeViewModel.getManualLng();
        } else if (homeViewModel.getUserLocation().getValue() != null) {
            lat = homeViewModel.getUserLocation().getValue().getLatitude();
            lng = homeViewModel.getUserLocation().getValue().getLongitude();
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.resultsSection.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.GONE);
        binding.discoverState.setVisibility(View.GONE);

        viewModel.searchProducts(lat, lng, query, category).observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            if (result.status == NetworkResult.Status.SUCCESS) {
                if (result.data != null && !result.data.isEmpty()) {
                    List<Product> products = new ArrayList<>();
                    for (ProductSearchResult res : result.data) {
                        products.add(res.toProduct());
                    }
                    productAdapter.setProducts(products, true);
                    binding.resultsSection.setVisibility(View.VISIBLE);
                    binding.emptyState.setVisibility(View.GONE);
                    binding.discoverState.setVisibility(View.GONE);
                } else {
                    binding.resultsSection.setVisibility(View.GONE);
                    binding.emptyState.setVisibility(View.VISIBLE);
                    binding.discoverState.setVisibility(View.GONE);
                }
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.resultsSection.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.discoverState.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        binding = null;
    }
}
