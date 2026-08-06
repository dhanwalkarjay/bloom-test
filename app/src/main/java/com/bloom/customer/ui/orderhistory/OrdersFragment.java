package com.bloom.customer.ui.orderhistory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.R;
import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.ui.cart.CartActivity;
import com.bloom.customer.ui.common.FragmentStatusBar;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.FragmentOrdersBinding;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private OrderHistoryViewModel viewModel;
    private OrderHistoryAdapter adapter;
    private boolean isPastSelected = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentStatusBar.applyTopInset(this, binding.topBar);
        
        viewModel = new ViewModelProvider(this).get(OrderHistoryViewModel.class);
        setupRecyclerView();
        setupListeners();
        fetchOrders();
    }

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter();
        binding.rvOrderHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOrderHistory.setAdapter(adapter);

        adapter.setListener(new OrderHistoryAdapter.OnOrderHistoryClickListener() {
            @Override
            public void onReviewClick(com.bloom.customer.data.model.Order order) {
                Intent intent = new Intent(requireContext(), RateOrderActivity.class);
                intent.putExtra("order_id", order.getId());
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    intent.putExtra("product_name", order.getItems().get(0).getProduct().getTitle());
                }
                startActivity(intent);
            }

            @Override
            public void onOrderClick(com.bloom.customer.data.model.Order order) {
                Intent intent = new Intent(requireContext(), com.bloom.customer.ui.ordertracking.OrderTrackingActivity.class);
                intent.putExtra("order_id", order.getId());
                startActivity(intent);
            }
        });
    }

    private void setupListeners() {
        binding.btnCart.setOnClickListener(v -> startActivity(new Intent(requireContext(), CartActivity.class)));

        binding.tabActive.setOnClickListener(v -> {
            if (isPastSelected) {
                isPastSelected = false;
                updateSegmentedControl();
                fetchOrders();
            }
        });

        binding.tabPast.setOnClickListener(v -> {
            if (!isPastSelected) {
                isPastSelected = true;
                updateSegmentedControl();
                fetchOrders();
            }
        });

        binding.swipeRefresh.setOnRefreshListener(this::fetchOrders);
    }

    private void fetchOrders() {
        String userId = SessionManager.getInstance(requireContext()).getUserId();
        if (userId == null) {
            binding.swipeRefresh.setRefreshing(false);
            binding.emptyState.setVisibility(View.VISIBLE);
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvOrderHistory.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.GONE);

        viewModel.getOrderHistory(userId).observe(getViewLifecycleOwner(), result -> {
            binding.swipeRefresh.setRefreshing(false);
            binding.progressBar.setVisibility(View.GONE);
            if (result.status == NetworkResult.Status.SUCCESS) {
                if (result.data != null && !result.data.isEmpty()) {
                    java.util.List<com.bloom.customer.data.model.Order> filtered = new java.util.ArrayList<>();
                    for (com.bloom.customer.data.model.Order o : result.data) {
                        String status = o.getStatus() != null ? o.getStatus() : "placed";
                        boolean isDelivered = "Delivered".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status);
                        if (isPastSelected) {
                            if (isDelivered) filtered.add(o);
                        } else {
                            if (!isDelivered) filtered.add(o);
                        }
                    }
                    
                    if (filtered.isEmpty()) {
                        binding.rvOrderHistory.setVisibility(View.GONE);
                        binding.emptyState.setVisibility(View.VISIBLE);
                    } else {
                        adapter.setOrders(filtered);
                        binding.rvOrderHistory.setVisibility(View.VISIBLE);
                        binding.emptyState.setVisibility(View.GONE);
                    }
                } else {
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
            } else {
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateSegmentedControl() {
        if (isPastSelected) {
            binding.tabPast.setBackgroundResource(R.drawable.bg_orders_segment_active);
            binding.tabPast.setTextColor(ContextCompat.getColor(requireContext(), R.color.orders_primary));
            binding.tabPast.setElevation(2f);

            binding.tabActive.setBackgroundResource(android.R.color.transparent);
            binding.tabActive.setTextColor(ContextCompat.getColor(requireContext(), R.color.orders_on_surface_variant));
            binding.tabActive.setElevation(0f);
        } else {
            binding.tabActive.setBackgroundResource(R.drawable.bg_orders_segment_active);
            binding.tabActive.setTextColor(ContextCompat.getColor(requireContext(), R.color.orders_primary));
            binding.tabActive.setElevation(2f);

            binding.tabPast.setBackgroundResource(android.R.color.transparent);
            binding.tabPast.setTextColor(ContextCompat.getColor(requireContext(), R.color.orders_on_surface_variant));
            binding.tabPast.setElevation(0f);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
