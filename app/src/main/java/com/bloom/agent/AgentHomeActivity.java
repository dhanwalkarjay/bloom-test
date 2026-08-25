package com.bloom.agent;

import android.os.Bundle;
import android.widget.TextView;
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
import com.google.android.material.switchmaterial.SwitchMaterial;

import android.content.Intent;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgentHomeActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private AgentDeliveryAdapter deliveryAdapter;
    private SwitchMaterial switchDuty;
    private TextView tvStatus;
    
    private SupabaseAPI api;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_home);

        api = RetrofitClient.getClient(this).create(SupabaseAPI.class);
        sessionManager = SessionManager.getInstance(this);

        tvStatus = findViewById(R.id.tvStatus);
        switchDuty = findViewById(R.id.switchDuty);

        setupRecyclerView();
        setupBottomNavigation();

        switchDuty.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvStatus.setText("On Duty");
                tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Green
                fetchDeliveries();
            } else {
                tvStatus.setText("Offline");
                tvStatus.setTextColor(android.graphics.Color.GRAY);
                deliveryAdapter.setDeliveries(new ArrayList<>());
            }
        });

        // Initialize state
        switchDuty.setChecked(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncOfflineDeliveries();
        if (switchDuty.isChecked()) {
            fetchDeliveries();
        }
    }

    private void syncOfflineDeliveries() {
        if (!com.bloom.customer.util.ConnectivityHelper.isConnected(this)) return;
        android.content.SharedPreferences prefs = getSharedPreferences("agent_offline_sync", MODE_PRIVATE);
        String pending = prefs.getString("pending_deliveries", "");
        if (pending.isEmpty()) return;

        String[] orderIds = pending.split(",");
        for (String orderId : orderIds) {
            if (orderId.trim().isEmpty()) continue;
            
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("status", "delivered");
            body.put("proof_image_url", "https://mock.storage.supabase.co/storage/v1/object/public/proofs/" + orderId + ".jpg");

            api.updateOrder("eq." + orderId, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        String current = prefs.getString("pending_deliveries", "");
                        String updated = current.replace(orderId, "").replace(",,", ",");
                        if (updated.startsWith(",")) updated = updated.substring(1);
                        if (updated.endsWith(",")) updated = updated.substring(0, updated.length() - 1);
                        prefs.edit().putString("pending_deliveries", updated).apply();
                        Toast.makeText(AgentHomeActivity.this, "Offline order synced!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {}
            });
        }
    }

    private void setupRecyclerView() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        RecyclerView rvDeliveries = findViewById(R.id.rvDeliveries);

        rvDeliveries.setLayoutManager(new LinearLayoutManager(this));
        deliveryAdapter = new AgentDeliveryAdapter();
        rvDeliveries.setAdapter(deliveryAdapter);

        deliveryAdapter.setListener(order -> {
            Intent intent = new Intent(this, DeliveryDetailActivity.class);
            intent.putExtra("ORDER_ID", order.getId());
            intent.putExtra("DELIVERY_OTP", order.getDeliveryOtp());
            startActivity(intent);
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (switchDuty.isChecked()) {
                fetchDeliveries();
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_agent_home) {
                return true;
            } else if (id == R.id.nav_agent_history) {
                Intent intent = new Intent(this, AgentHistoryActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_agent_profile) {
                Toast.makeText(this, "Profile coming soon", Toast.LENGTH_SHORT).show();
                return false;
            }
            return false;
        });
    }

    private void fetchDeliveries() {
        if (sessionManager.getUserId() == null) return;
        swipeRefreshLayout.setRefreshing(true);

        // Fetch orders where status is 'preparing' or 'out_for_delivery'
        // For simplicity, we just fetch all for now and filter, or use PostgREST filters.
        api.getOrders(null, "*", "created_at.desc").enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> assigned = new ArrayList<>();
                    for (Order order : response.body()) {
                        if ("preparing".equals(order.getStatus()) || "out_for_delivery".equals(order.getStatus())) {
                            assigned.add(order);
                        }
                    }
                    deliveryAdapter.setDeliveries(assigned);
                } else {
                    Toast.makeText(AgentHomeActivity.this, "Failed to load deliveries", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(AgentHomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
