package com.bloom.customer.ui.orderhistory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.R;
import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.repository.OrderRepository;
import com.bloom.customer.ui.reviews.ReviewActivity;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.FragmentOrdersBinding;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private OrderRepository orderRepository;
    private OrderHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderRepository = new OrderRepository(requireContext());
        adapter = new OrderHistoryAdapter();

        setupRecyclerView();
        fetchOrderHistory();
    }

    private void setupRecyclerView() {
        binding.rvOrderHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOrderHistory.setAdapter(adapter);

        adapter.setListener(order -> {
            Intent intent = new Intent(requireContext(), ReviewActivity.class);
            intent.putExtra("order_id", order.getId());
            startActivity(intent);
        });
    }

    private void fetchOrderHistory() {
        String userId = SessionManager.getInstance(requireContext()).getUserId();
        if (userId == null) return;

        orderRepository.getOrderHistory(userId).observe(getViewLifecycleOwner(), result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.emptyState.setVisibility(View.GONE);
                binding.rvOrderHistory.setVisibility(View.GONE);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                if (result.data != null && !result.data.isEmpty()) {
                    adapter.setOrders(result.data);
                    binding.emptyState.setVisibility(View.GONE);
                    binding.rvOrderHistory.setVisibility(View.VISIBLE);
                } else {
                    binding.tvEmptyTitle.setText(R.string.no_orders_placed);
                    binding.tvEmptySubtitle.setText(R.string.no_orders_subtitle);
                    binding.emptyState.setVisibility(View.VISIBLE);
                    binding.rvOrderHistory.setVisibility(View.GONE);
                }
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                binding.rvOrderHistory.setVisibility(View.GONE);
                binding.tvEmptyTitle.setText(R.string.error_load_orders);
                binding.tvEmptySubtitle.setText(result.message != null ? result.message : "Pull down to retry.");
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
