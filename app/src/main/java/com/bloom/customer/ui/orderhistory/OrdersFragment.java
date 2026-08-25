package com.bloom.customer.ui.orderhistory;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bloom.R;
import com.bloom.customer.ui.cart.CartActivity;
import com.bloom.customer.ui.common.FragmentStatusBar;
import com.bloom.databinding.FragmentOrdersBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentStatusBar.applyTopInset(this, binding.topBar);

        setupViewPager();
        setupListeners();
    }

    private void setupViewPager() {
        OrdersPagerAdapter adapter = new OrdersPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Active");
                    } else {
                        tab.setText("Past");
                    }
                }
        ).attach();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTabTypeface(tab, Typeface.BOLD);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                updateTabTypeface(tab, Typeface.NORMAL);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Initialize first tab
        binding.tabLayout.post(() -> {
            if (binding.tabLayout.getTabAt(0) != null) {
                updateTabTypeface(binding.tabLayout.getTabAt(0), Typeface.BOLD);
            }
        });
    }

    private void updateTabTypeface(TabLayout.Tab tab, int typeface) {
        if (tab != null && tab.view != null) {
            View tabView = tab.view;
            for (int j = 0; j < ((ViewGroup) tabView).getChildCount(); j++) {
                View child = ((ViewGroup) tabView).getChildAt(j);
                if (child instanceof TextView) {
                    ((TextView) child).setTypeface(null, typeface);
                }
            }
        }
    }

    private void setupListeners() {
        binding.btnCart.setOnClickListener(v -> startActivity(new Intent(requireContext(), CartActivity.class)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
