package com.bloom.customer.ui.orderhistory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.R;
import com.bloom.customer.ui.cart.CartActivity;
import com.bloom.customer.ui.home.HomeActivity;
import com.bloom.databinding.ActivityOrdersBinding;

public class OrdersActivity extends AppCompatActivity {

    private ActivityOrdersBinding binding;
    private boolean showingActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        updateSegmentedControl();
    }

    private void setupListeners() {
        binding.btnFavorites.setOnClickListener(v -> Toast.makeText(this, "Favorites coming soon", Toast.LENGTH_SHORT).show());
        binding.btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        binding.tabActive.setOnClickListener(v -> {
            showingActive = true;
            updateSegmentedControl();
        });
        binding.tabPast.setOnClickListener(v -> {
            showingActive = false;
            updateSegmentedControl();
        });

        binding.orderCardOne.setOnClickListener(v -> Toast.makeText(this, "Order #98122", Toast.LENGTH_SHORT).show());
        binding.orderCardTwo.setOnClickListener(v -> Toast.makeText(this, "Order #98121", Toast.LENGTH_SHORT).show());
        binding.orderCardThree.setOnClickListener(v -> Toast.makeText(this, "Order #98120", Toast.LENGTH_SHORT).show());

        binding.navDashboard.setOnClickListener(v -> openHome());
        binding.navOrders.setOnClickListener(v -> binding.ordersScroll.smoothScrollTo(0, 0));
        binding.navInventory.setOnClickListener(v -> Toast.makeText(this, "Inventory coming soon", Toast.LENGTH_SHORT).show());
        binding.navEarnings.setOnClickListener(v -> Toast.makeText(this, "Earnings coming soon", Toast.LENGTH_SHORT).show());
        binding.navProfile.setOnClickListener(v -> Toast.makeText(this, "Profile coming soon", Toast.LENGTH_SHORT).show());
    }

    private void updateSegmentedControl() {
        binding.tabActive.setBackgroundResource(showingActive ? R.drawable.bg_orders_segment_active : 0);
        binding.tabPast.setBackgroundResource(showingActive ? 0 : R.drawable.bg_orders_segment_active);
        binding.tabActive.setTextColor(getColor(showingActive ? R.color.orders_primary : R.color.orders_on_surface_variant));
        binding.tabPast.setTextColor(getColor(showingActive ? R.color.orders_on_surface_variant : R.color.orders_primary));

        int visibility = showingActive ? View.VISIBLE : View.GONE;
        binding.orderCardOne.setVisibility(visibility);
        binding.orderCardTwo.setVisibility(visibility);
        binding.orderCardThree.setVisibility(visibility);

        if (!showingActive) {
            Toast.makeText(this, "No past orders yet", Toast.LENGTH_SHORT).show();
        }
    }

    private void openHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
