package com.bloom.customer.ui.orderhistory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.model.Order;
import com.bloom.customer.data.repository.OrderRepository;
import com.bloom.customer.ui.reviews.ReviewActivity;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityOrderHistoryBinding;

public class OrderHistoryActivity extends AppCompatActivity {

    private ActivityOrderHistoryBinding binding;
    private OrderRepository orderRepository;
    private OrderHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderRepository = new OrderRepository(this);
        adapter = new OrderHistoryAdapter();

        setupToolbar();
        setupRecyclerView();
        fetchOrderHistory();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        binding.rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrderHistory.setAdapter(adapter);

        adapter.setListener(order -> {
            Intent intent = new Intent(this, ReviewActivity.class);
            intent.putExtra("order_id", order.getId());
            startActivity(intent);
        });
    }

    private void fetchOrderHistory() {
        String userId = SessionManager.getInstance(this).getUserId();
        if (userId == null) return;

        orderRepository.getOrderHistory(userId).observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.tvEmpty.setVisibility(View.GONE);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                if (result.data != null && !result.data.isEmpty()) {
                    adapter.setOrders(result.data);
                    binding.tvEmpty.setVisibility(View.GONE);
                } else {
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                }
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
