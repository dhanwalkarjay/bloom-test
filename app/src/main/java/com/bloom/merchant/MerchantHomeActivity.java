package com.bloom.merchant;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bloom.R;
import com.bloom.customer.data.model.Order;
import com.bloom.customer.data.model.ShopInventoryItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.List;

public class MerchantHomeActivity extends AppCompatActivity {

    private MerchantOrderAdapter orderAdapter;
    private MerchantInventoryAdapter inventoryAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewFlipper viewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_home);
        
        viewFlipper = findViewById(R.id.viewFlipper);
        
        setupOrdersView();
        setupInventoryView();
        setupBottomNavigation();
        
        // Initial load
        fetchOrders();
    }
    
    private void setupOrdersView() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::fetchOrders);
        
        RecyclerView rvOrders = findViewById(R.id.rvActiveOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new MerchantOrderAdapter();
        rvOrders.setAdapter(orderAdapter);
        
        orderAdapter.setListener(new MerchantOrderAdapter.OnOrderActionListener() {
            @Override
            public void onAccept(Order order) {
                // Mock progression logic
                if (order.getStatus().equals("placed")) {
                    order.setStatus("preparing");
                    Toast.makeText(MerchantHomeActivity.this, "Order marked as Preparing", Toast.LENGTH_SHORT).show();
                } else if (order.getStatus().equals("preparing")) {
                    order.setStatus("out_for_delivery");
                    Toast.makeText(MerchantHomeActivity.this, "Order Out for Delivery!", Toast.LENGTH_SHORT).show();
                } else if (order.getStatus().equals("out_for_delivery")) {
                    order.setStatus("delivered");
                    Toast.makeText(MerchantHomeActivity.this, "Order Delivered!", Toast.LENGTH_SHORT).show();
                }
                orderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDecline(Order order) {
                order.setStatus("cancelled");
                Toast.makeText(MerchantHomeActivity.this, "Order Cancelled", Toast.LENGTH_SHORT).show();
                orderAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setupInventoryView() {
        RecyclerView rvInventory = findViewById(R.id.rvInventory);
        rvInventory.setLayoutManager(new LinearLayoutManager(this));
        inventoryAdapter = new MerchantInventoryAdapter();
        rvInventory.setAdapter(inventoryAdapter);
        
        inventoryAdapter.setListener((item, isAvailable) -> {
            String status = isAvailable ? "Available" : "Sold Out";
            Toast.makeText(MerchantHomeActivity.this, item.getName() + " marked as " + status, Toast.LENGTH_SHORT).show();
            // TODO: Push to Supabase shop_inventory table
        });
        
        loadMockInventory();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_orders) {
                viewFlipper.setDisplayedChild(0);
                return true;
            } else if (id == R.id.nav_inventory) {
                viewFlipper.setDisplayedChild(1);
                return true;
            } else if (id == R.id.nav_settings) {
                Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
                return false;
            }
            return false;
        });
    }
    
    private void fetchOrders() {
        swipeRefreshLayout.setRefreshing(true);
        // Mocking network delay
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            List<Order> mockOrders = new ArrayList<>();
            
            // Mock some orders if no setters are accessible via Reflection or just skip it for now and use GSON if needed.
            // Let's assume we have OrderRepository
            Order mockOrder = new com.google.gson.Gson().fromJson("{\"id\":\"ord_1234\", \"status\":\"placed\"}", Order.class);
            Order mockOrder2 = new com.google.gson.Gson().fromJson("{\"id\":\"ord_5678\", \"status\":\"preparing\"}", Order.class);
            
            mockOrders.add(mockOrder);
            mockOrders.add(mockOrder2);
            
            orderAdapter.setOrders(mockOrders);
            swipeRefreshLayout.setRefreshing(false);
        }, 1000);
    }
    
    private void loadMockInventory() {
        List<ShopInventoryItem> inventory = new ArrayList<>();
        
        ShopInventoryItem item1 = new ShopInventoryItem();
        item1.setId("stem1"); item1.setType("STEM"); item1.setName("Red Rose");
        item1.setPricePerUnit(4.50); item1.setStockQuantity(50); item1.setColorHex("🌹");
        inventory.add(item1);

        ShopInventoryItem item2 = new ShopInventoryItem();
        item2.setId("wrap1"); item2.setType("WRAPPER"); item2.setName("Matte Black Paper");
        item2.setPricePerUnit(5.00); item2.setStockQuantity(100); item2.setColorHex("🖤");
        inventory.add(item2);
        
        ShopInventoryItem item3 = new ShopInventoryItem();
        item3.setId("ribbon1"); item3.setType("RIBBON"); item3.setName("Silk Red Ribbon");
        item3.setPricePerUnit(2.00); item3.setStockQuantity(0); item3.setColorHex("🎀"); // Sold out
        inventory.add(item3);

        inventoryAdapter.setItems(inventory);
    }
}
