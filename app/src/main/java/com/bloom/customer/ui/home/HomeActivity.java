package com.bloom.customer.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.ui.auth.LoginActivity;
import com.bloom.R;
import com.bloom.customer.ui.lux.LuxActivity;
import com.bloom.customer.ui.orderhistory.OrdersFragment;
import com.bloom.customer.ui.profile.ProfileFragment;
import com.bloom.customer.ui.search.SearchActivity;
import com.bloom.databinding.ActivityHomeBinding;

/**
 * Main Activity for discovering nearby shops.
 * Principle: Separation of Concerns - UI logic only.
 */
public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNav();

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupBottomNav() {
        selectNavItem(binding.navHome);

        binding.navHome.setOnClickListener(v -> {
            selectNavItem(binding.navHome);
            loadFragment(new HomeFragment());
        });

        binding.navLux.setOnClickListener(v -> {
            startActivity(new Intent(this, LuxActivity.class));
        });

        binding.navSearch.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
        });

        binding.navOrders.setOnClickListener(v -> {
            if (SessionManager.getInstance(this).isLoggedIn()) {
                selectNavItem(binding.navOrders);
                loadFragment(new OrdersFragment());
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        binding.navProfile.setOnClickListener(v -> {
            if (SessionManager.getInstance(this).isLoggedIn()) {
                selectNavItem(binding.navProfile);
                loadFragment(new ProfileFragment());
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }

    private void selectNavItem(View selectedItem) {
        setNavItem(binding.navHome, binding.ivNavHome, binding.tvNavHome, selectedItem == binding.navHome);
        setNavItem(binding.navLux, binding.ivNavLux, binding.tvNavLux, selectedItem == binding.navLux);
        setNavItem(binding.navSearch, binding.ivNavSearch, binding.tvNavSearch, selectedItem == binding.navSearch);
        setNavItem(binding.navOrders, binding.ivNavOrders, binding.tvNavOrders, selectedItem == binding.navOrders);
        setNavItem(binding.navProfile, binding.ivNavProfile, binding.tvNavProfile, selectedItem == binding.navProfile);
    }

    private void setNavItem(LinearLayout container, ImageView icon, TextView label, boolean selected) {
        int selectedColor = ContextCompat.getColor(this, android.R.color.white);
        int defaultColor = ContextCompat.getColor(this, R.color.home_on_surface_variant);

        container.setBackgroundResource(selected ? R.drawable.bg_home_nav_active : 0);
        icon.setColorFilter(selected ? selectedColor : defaultColor);
        label.setTextColor(selected ? selectedColor : defaultColor);
        label.setTypeface(label.getTypeface(), selected ? Typeface.BOLD : Typeface.NORMAL);
    }
}
