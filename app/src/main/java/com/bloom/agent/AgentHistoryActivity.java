package com.bloom.agent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bloom.R;
import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAPI;
import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.model.Order;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgentHistoryActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private AgentDeliveryAdapter deliveryAdapter;
    private SupabaseAPI api;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_history);

        api = RetrofitClient.getClient(this).create(SupabaseAPI.class);
        sessionManager = SessionManager.getInstance(this);

        setupRecyclerView();
        setupBottomNavigation();
        
        fetchHistory();
    }

    private void setupRecyclerView() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        RecyclerView rvHistory = findViewById(R.id.rvHistory);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        deliveryAdapter = new AgentDeliveryAdapter();
        rvHistory.setAdapter(deliveryAdapter);

        // History items don't need to be clicked to mark delivered, but we can view details
        deliveryAdapter.setListener(order -> {
            Intent intent = new Intent(this, DeliveryDetailActivity.class);
            intent.putExtra("ORDER_ID", order.getId());
            startActivity(intent);
        });

        swipeRefreshLayout.setOnRefreshListener(this::fetchHistory);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_agent_history);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_agent_home) {
                Intent intent = new Intent(this, AgentHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_agent_history) {
                return true;
            } else if (id == R.id.nav_agent_profile) {
                Toast.makeText(this, "Profile coming soon", Toast.LENGTH_SHORT).show();
                return false;
            }
            return false;
        });
    }

    private void fetchHistory() {
        if (sessionManager.getUserId() == null) return;
        swipeRefreshLayout.setRefreshing(true);

        // Fetch orders where status is 'delivered'
        api.getOrders(null, "*", "created_at.desc").enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> history = new ArrayList<>();
                    for (Order order : response.body()) {
                        if ("delivered".equals(order.getStatus())) {
                            history.add(order);
                        }
                    }
                    deliveryAdapter.setDeliveries(history);
                } else {
                    Toast.makeText(AgentHistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(AgentHistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
