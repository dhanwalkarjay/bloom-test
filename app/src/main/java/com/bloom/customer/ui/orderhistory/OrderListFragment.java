package com.bloom.customer.ui.orderhistory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.FragmentOrderListBinding;

public class OrderListFragment extends Fragment {

    private static final String ARG_IS_PAST = "arg_is_past";

    private FragmentOrderListBinding binding;
    private OrderHistoryViewModel viewModel;
    private OrderHistoryAdapter adapter;
    private boolean isPast;

    public static OrderListFragment newInstance(boolean isPast) {
        OrderListFragment fragment = new OrderListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_PAST, isPast);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isPast = getArguments().getBoolean(ARG_IS_PAST, false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Use the activity's ViewModel so it's shared across tabs
        viewModel = new ViewModelProvider(requireActivity()).get(OrderHistoryViewModel.class);
        
        setupRecyclerView();
        
        binding.swipeRefresh.setOnRefreshListener(this::fetchOrders);
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
                        
                        if (isPast) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
