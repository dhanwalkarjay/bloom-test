package com.bloom.customer.ui.lux;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.ui.auth.LoginActivity;
import com.bloom.customer.ui.cart.CartActivity;
import com.bloom.customer.ui.common.FragmentStatusBar;
import com.bloom.customer.ui.product.ProductDetailActivity;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.FragmentLuxBinding;
import com.google.gson.Gson;

public class LuxFragment extends Fragment {

    private FragmentLuxBinding binding;
    private LuxViewModel viewModel;
    private LuxProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLuxBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Dark (#1A1A1A) background fills transparent status bar; white icons set by HomeActivity.
        FragmentStatusBar.applyTopInset(this, binding.toolbar);
        
        viewModel = new ViewModelProvider(this).get(LuxViewModel.class);
        
        setupRecyclerView();
        setupListeners();
        observeViewModel();
        
        // Trigger fetch
        viewModel.fetchLuxCollection();
    }

    private void setupRecyclerView() {
        adapter = new LuxProductAdapter();
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvProducts.setAdapter(adapter);
        
        adapter.setListener(product -> {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra("product_json", new Gson().toJson(product));
            intent.putExtra("is_shop_open", true); // Lux atelier is always "open"
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        viewModel.getLuxProducts().observe(getViewLifecycleOwner(), result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                binding.shimmerView.setVisibility(View.VISIBLE);
                binding.rvProducts.setVisibility(View.GONE);
                binding.quoteSection.setVisibility(View.GONE);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                binding.shimmerView.setVisibility(View.GONE);
                if (result.data != null) {
                    adapter.setProducts(result.data);
                    binding.rvProducts.setVisibility(View.VISIBLE);
                    binding.quoteSection.setVisibility(View.VISIBLE);
                }
            } else {
                binding.shimmerView.setVisibility(View.GONE);
                binding.rvProducts.setVisibility(View.GONE);
                binding.quoteSection.setVisibility(View.GONE);
                // Could show an error state here in a real production app
                Toast.makeText(requireContext(), "Failed to load Lux collection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.btnCart.setOnClickListener(v -> startActivity(new Intent(requireContext(), CartActivity.class)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
