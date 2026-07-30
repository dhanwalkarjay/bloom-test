package com.bloom.customer.ui.home;

import android.os.Bundle;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bloom.R;
import com.bloom.customer.ui.orderhistory.OrdersFragment;
import com.bloom.customer.ui.profile.ProfileFragment;
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
            selectNavItem(binding.navHome);
            Toast.makeText(this, "LUX experience coming soon", Toast.LENGTH_SHORT).show();
        });

        binding.navSearch.setOnClickListener(v -> {
            selectNavItem(binding.navHome);
            Toast.makeText(this, "Search coming soon", Toast.LENGTH_SHORT).show();
        });

        binding.navOrders.setOnClickListener(v -> {
            selectNavItem(binding.navOrders);
            loadFragment(new OrdersFragment());
        });

        binding.navProfile.setOnClickListener(v -> {
            selectNavItem(binding.navProfile);
            loadFragment(new ProfileFragment());
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
