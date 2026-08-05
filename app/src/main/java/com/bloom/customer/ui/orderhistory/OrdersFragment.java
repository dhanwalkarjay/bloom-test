package com.bloom.customer.ui.orderhistory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bloom.R;
import com.bloom.customer.ui.cart.CartActivity;
import com.bloom.customer.ui.common.FragmentStatusBar;
import com.bloom.customer.ui.ordertracking.OrderTrackingActivity;
import com.bloom.databinding.FragmentOrdersBinding;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private boolean isPastSelected = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Warm-white (#FCF9F8) background fills transparent status bar; dark icons set by HomeActivity.
        FragmentStatusBar.applyTopInset(this, binding.topBar);
        setupListeners();
    }

    private void setupListeners() {
        binding.btnFavorites.setOnClickListener(v -> Toast.makeText(requireContext(), "Favorites clicked", Toast.LENGTH_SHORT).show());
        binding.btnCart.setOnClickListener(v -> startActivity(new Intent(requireContext(), CartActivity.class)));

        binding.tabActive.setOnClickListener(v -> {
            if (isPastSelected) {
                isPastSelected = false;
                updateSegmentedControl();
                showActiveOrders();
            }
        });

        binding.tabPast.setOnClickListener(v -> {
            if (!isPastSelected) {
                isPastSelected = true;
                updateSegmentedControl();
                showPastOrders();
            }
        });

        binding.orderCardOne.setOnClickListener(v -> showOrderDetails("#98122"));
        binding.orderCardTwo.setOnClickListener(v -> showOrderDetails("#98121"));
        binding.orderCardThree.setOnClickListener(v -> showOrderDetails("#98120"));
    }

    private void updateSegmentedControl() {
        if (isPastSelected) {
            binding.tabPast.setBackgroundResource(R.drawable.bg_orders_segment_active);
            binding.tabPast.setTextColor(ContextCompat.getColor(requireContext(), R.color.orders_primary));
            binding.tabPast.setElevation(3f);

            binding.tabActive.setBackgroundResource(android.R.color.transparent);
            binding.tabActive.setTextColor(ContextCompat.getColor(requireContext(), R.color.orders_on_surface_variant));
            binding.tabActive.setElevation(0f);
        } else {
            binding.tabActive.setBackgroundResource(R.drawable.bg_orders_segment_active);
            binding.tabActive.setTextColor(ContextCompat.getColor(requireContext(), R.color.orders_primary));
            binding.tabActive.setElevation(3f);

            binding.tabPast.setBackgroundResource(android.R.color.transparent);
            binding.tabPast.setTextColor(ContextCompat.getColor(requireContext(), R.color.orders_on_surface_variant));
            binding.tabPast.setElevation(0f);
        }
    }

    private void showActiveOrders() {
        binding.orderCardOne.setVisibility(View.VISIBLE);
        binding.orderCardTwo.setVisibility(View.VISIBLE);
        binding.orderCardThree.setVisibility(View.VISIBLE);
    }

    private void showPastOrders() {
        // Just hiding these to simulate "Past Orders" empty state or something
        binding.orderCardOne.setVisibility(View.GONE);
        binding.orderCardTwo.setVisibility(View.GONE);
        binding.orderCardThree.setVisibility(View.GONE);
        Toast.makeText(requireContext(), "No past orders yet", Toast.LENGTH_SHORT).show();
    }

    private void showOrderDetails(String orderId) {
        Toast.makeText(requireContext(), "Order details for " + orderId, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getContext(), OrderTrackingActivity.class));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
