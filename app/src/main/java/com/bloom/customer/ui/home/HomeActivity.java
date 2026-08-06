package com.bloom.customer.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.ui.auth.LoginActivity;
import com.bloom.R;
import com.bloom.customer.ui.explore.ExploreFragment;
import com.bloom.customer.ui.lux.LuxFragment;
import com.bloom.customer.ui.orderhistory.OrdersFragment;
import com.bloom.customer.ui.profile.ProfileFragment;
import com.bloom.databinding.ActivityHomeBinding;

/**
 * Single-Activity host for all navigation fragments.
 * 
 * Fragments are lazy-loaded on demand and cached. 
 * This fixes the lag issue by preventing all 5 fragments from 
 * being inflated simultaneously on app start.
 */
public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private WindowInsetsControllerCompat insetsController;

    private int statusBarHeight = 0;
    private int navBarHeight = 0;

    private String activeFragmentTag = "home";

    private static final String TAG_HOME = "home";
    private static final String TAG_LUX = "lux";
    private static final String TAG_SEARCH = "search";
    private static final String TAG_ORDERS = "orders";
    private static final String TAG_PROFILE = "profile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applySystemBarInsets();

        if (savedInstanceState == null) {
            // Load only Home Fragment initially
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_host_fragment, new HomeFragment(), TAG_HOME)
                    .commit();
            activeFragmentTag = TAG_HOME;
        } else {
            activeFragmentTag = savedInstanceState.getString("active_tab", TAG_HOME);
        }

        setupBottomNav();
        restoreBottomNavState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("active_tab", activeFragmentTag);
    }

    private void applySystemBarInsets() {
        int originalNavHeight = binding.bottomNavigation.getLayoutParams().height;

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );

            statusBarHeight = insets.top;
            navBarHeight = insets.bottom;

            binding.navHostFragment.setPadding(0, 0, 0, 0);

            binding.bottomNavigation.setPadding(
                    binding.bottomNavigation.getPaddingLeft(),
                    binding.bottomNavigation.getPaddingTop(),
                    binding.bottomNavigation.getPaddingRight(),
                    insets.bottom
            );
            if (originalNavHeight > 0) {
                binding.bottomNavigation.getLayoutParams().height = originalNavHeight + insets.bottom;
                binding.bottomNavigation.requestLayout();
            }

            return windowInsets;
        });

        ViewCompat.requestApplyInsets(binding.getRoot());
    }

    public int getStatusBarHeight() {
        return statusBarHeight;
    }

    public void setStatusBarIconStyle(boolean isLightBackground) {
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(isLightBackground);
        }
    }

    private void setupBottomNav() {
        binding.navHome.setOnClickListener(v -> switchFragment(TAG_HOME, binding.navHome, true));
        binding.navLux.setOnClickListener(v -> switchFragment(TAG_LUX, binding.navLux, false));
        binding.navSearch.setOnClickListener(v -> switchFragment(TAG_SEARCH, binding.navSearch, true));
        binding.navOrders.setOnClickListener(v -> switchFragment(TAG_ORDERS, binding.navOrders, true));
        
        binding.navProfile.setOnClickListener(v -> {
            switchFragment(TAG_PROFILE, binding.navProfile, true);
        });
    }

    private void switchFragment(String targetTag, View selectedItem, boolean isLightBackground) {
        if (activeFragmentTag.equals(targetTag)) return;

        final String previousTag = activeFragmentTag;
        activeFragmentTag = targetTag;

        selectNavItem(selectedItem);
        setStatusBarIconStyle(isLightBackground);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment current = fm.findFragmentByTag(previousTag);
        if (current != null) {
            ft.hide(current);
        }

        Fragment target = fm.findFragmentByTag(targetTag);
        if (target == null) {
            target = createFragmentByTag(targetTag);
            ft.add(R.id.nav_host_fragment, target, targetTag);
        } else {
            ft.show(target);
        }

        ft.commitAllowingStateLoss();
    }

    private Fragment createFragmentByTag(String tag) {
        switch (tag) {
            case TAG_LUX: return new LuxFragment();
            case TAG_SEARCH: return new ExploreFragment();
            case TAG_ORDERS: return new OrdersFragment();
            case TAG_PROFILE: return new ProfileFragment();
            default: return new HomeFragment();
        }
    }

    private void restoreBottomNavState() {
        if (TAG_HOME.equals(activeFragmentTag)) {
            selectNavItem(binding.navHome);
            setStatusBarIconStyle(true);
        } else if (TAG_LUX.equals(activeFragmentTag)) {
            selectNavItem(binding.navLux);
            setStatusBarIconStyle(false);
        } else if (TAG_SEARCH.equals(activeFragmentTag)) {
            selectNavItem(binding.navSearch);
            setStatusBarIconStyle(true);
        } else if (TAG_ORDERS.equals(activeFragmentTag)) {
            selectNavItem(binding.navOrders);
            setStatusBarIconStyle(true);
        } else if (TAG_PROFILE.equals(activeFragmentTag)) {
            selectNavItem(binding.navProfile);
            setStatusBarIconStyle(true);
        }
    }

    private void selectNavItem(View selectedItem) {
        boolean isLuxActive = TAG_LUX.equals(activeFragmentTag);
        
        // Darken nav background if LUX is active
        binding.bottomNavigation.setBackgroundColor(isLuxActive ? 
                ContextCompat.getColor(this, R.color.home_lux_dark) : 
                ContextCompat.getColor(this, android.R.color.white));

        setNavItem(binding.navHomeBg, binding.ivNavHome, binding.tvNavHome, selectedItem == binding.navHome, isLuxActive);
        setNavItem(binding.navLuxBg, binding.ivNavLux, binding.tvNavLux, selectedItem == binding.navLux, isLuxActive);
        setNavItem(binding.navSearchBg, binding.ivNavSearch, binding.tvNavSearch, selectedItem == binding.navSearch, isLuxActive);
        setNavItem(binding.navOrdersBg, binding.ivNavOrders, binding.tvNavOrders, selectedItem == binding.navOrders, isLuxActive);
        setNavItem(binding.navProfileBg, binding.ivNavProfile, binding.tvNavProfile, selectedItem == binding.navProfile, isLuxActive);
    }

    private void setNavItem(View bgView, ImageView icon, TextView label, boolean selected, boolean isLuxActive) {
        int activeColor = ContextCompat.getColor(this, isLuxActive ? R.color.home_lux_active : R.color.home_primary_container);
        int inactiveColor = ContextCompat.getColor(this, isLuxActive ? R.color.lux_text_muted : R.color.home_on_surface_variant);
        int selectedIconColor = ContextCompat.getColor(this, android.R.color.white);

        // Update pill background color
        if (bgView.getBackground() instanceof android.graphics.drawable.GradientDrawable) {
            ((android.graphics.drawable.GradientDrawable) bgView.getBackground()).setColor(activeColor);
        }

        // Premium motion
        if (selected) {
            bgView.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(400)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f)).start();
            icon.animate().scaleX(1.15f).scaleY(1.15f).setDuration(300).start();
        } else {
            bgView.animate().alpha(0f).scaleX(0.4f).scaleY(0.4f).setDuration(250)
                    .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator()).start();
            icon.animate().scaleX(1f).scaleY(1f).setDuration(300).start();
        }

        icon.setColorFilter(selected ? selectedIconColor : inactiveColor);
        label.setTextColor(selected ? selectedIconColor : inactiveColor);
        label.setTypeface(label.getTypeface(), selected ? Typeface.BOLD : Typeface.NORMAL);
    }
}
