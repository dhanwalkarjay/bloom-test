package com.bloom.customer.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bloom.customer.data.model.Product;
import com.bloom.customer.data.model.ProductSearchResult;
import com.bloom.customer.data.repository.ProductRepository;
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

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private MainSharedViewModel sharedViewModel;
    private HomeViewModel homeViewModel;
    private ProductRepository productRepository;
    private ProductGridAdapter productAdapter;

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
        productRepository = new ProductRepository(requireContext());

        FragmentStatusBar.applyTopInset(this, binding.topBar);
        
        setupRecyclerView();
        setupListeners();
        setupSearchObservation();
    }

    private void setupRecyclerView() {
        productAdapter = new ProductGridAdapter();
        binding.rvResults.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvResults.setAdapter(productAdapter);

        productAdapter.setOnProductClickListener((product, isOpen) -> {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra("product_json", new Gson().toJson(product));
            intent.putExtra("is_shop_open", isOpen);
            startActivity(intent);
        });
    }

    private void setupSearchObservation() {
        sharedViewModel.getSearchCategory().observe(getViewLifecycleOwner(), category -> {
            if (category != null) {
                binding.etSearch.setText(category);
                performSearch(null, category);
            }
        });
        
        sharedViewModel.getSearchQuery().observe(getViewLifecycleOwner(), query -> {
            if (query != null) {
                binding.etSearch.setText(query);
                performSearch(query, null);
            }
        });
    }

    private void setupListeners() {
        binding.btnCart.setOnClickListener(v -> startActivity(new Intent(requireContext(), CartActivity.class)));

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = binding.etSearch.getText().toString().trim();
                performSearch(query, null);
                return true;
            }
            return false;
        });

        binding.chipShops.setOnClickListener(v -> {
            binding.chipShops.setBackgroundResource(com.bloom.R.drawable.bg_search_chip_active);
            binding.chipBouquets.setBackgroundResource(com.bloom.R.drawable.bg_search_chip_inactive);
            binding.chipShops.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), com.bloom.R.color.search_primary));
            binding.chipBouquets.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), com.bloom.R.color.search_on_surface_variant));
            
            // Re-run search if there's text
            String query = binding.etSearch.getText().toString().trim();
            performSearch(query, null);
        });

        binding.chipBouquets.setOnClickListener(v -> {
            binding.chipBouquets.setBackgroundResource(com.bloom.R.drawable.bg_search_chip_active);
            binding.chipShops.setBackgroundResource(com.bloom.R.drawable.bg_search_chip_inactive);
            binding.chipBouquets.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), com.bloom.R.color.search_primary));
            binding.chipShops.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), com.bloom.R.color.search_on_surface_variant));
            
            String query = binding.etSearch.getText().toString().trim();
            performSearch(query, null);
        });
    }

    private void performSearch(String query, String category) {
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

        productRepository.searchProductsNearby(lat, lng, query, category).observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(View.GONE);
            if (result.status == NetworkResult.Status.SUCCESS) {
                if (result.data != null && !result.data.isEmpty()) {
                    List<Product> products = new ArrayList<>();
                    for (ProductSearchResult res : result.data) {
                        products.add(res.toProduct());
                    }
                    productAdapter.setProducts(products, true);
                    binding.resultsSection.setVisibility(View.VISIBLE);
                    binding.emptyState.setVisibility(View.GONE);
                } else {
                    binding.resultsSection.setVisibility(View.GONE);
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.resultsSection.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
