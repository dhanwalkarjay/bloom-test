package com.bloom.customer.ui.home;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bloom.customer.ui.orderhistory.OrdersFragment;
import com.bloom.customer.ui.profile.ProfileFragment;
import com.bloom.databinding.ActivityHomeBinding;
import androidx.fragment.app.Fragment;

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
        binding.bottomNavigation.setSelectedItemId(com.bloom.R.id.nav_home);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == com.bloom.R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == com.bloom.R.id.nav_orders) {
                loadFragment(new OrdersFragment());
                return true;
            } else if (id == com.bloom.R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(com.bloom.R.id.nav_host_fragment, fragment)
                .commit();
    }
}
