package com.bloom.customer.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bloom.R;
import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.repository.ProfileRepository;
import com.bloom.customer.ui.auth.LoginActivity;
import com.bloom.customer.ui.common.FragmentStatusBar;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.FragmentProfileBinding;
import com.bumptech.glide.Glide;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileRepository profileRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Apply top inset to the scroll container so content clears the status bar
        FragmentStatusBar.applyTopInset(this, binding.scrollContainer);

        profileRepository = new ProfileRepository(requireContext());
        setupMenu();
        fetchProfileData();
    }

    private void setupMenu() {
        // First Group
        setupMenuItem(binding.itemOccasions.getRoot(), android.R.drawable.ic_menu_my_calendar, "Smart Occasions", "Never forget a date", v -> {
            startActivity(new Intent(requireContext(), com.bloom.customer.ui.profile.OccasionsActivity.class));
        });

        setupMenuItem(binding.itemAddresses.getRoot(), R.drawable.ic_home_location, "Saved Addresses", null, v -> {
            Intent intent = new Intent(requireContext(), com.bloom.customer.ui.checkout.AddressSelectActivity.class);
            intent.putExtra("selection_mode", false);
            startActivity(intent);
        });

        setupMenuItem(binding.itemPayment.getRoot(), R.drawable.ic_orders_payments, "Payment Methods", null, v -> {
            Toast.makeText(requireContext(), "Payment Methods coming soon", Toast.LENGTH_SHORT).show();
        });

        setupMenuItem(binding.itemLanguage.getRoot(), R.drawable.ic_search_tune, "Language", "English", v -> {
            Toast.makeText(requireContext(), "Language selection coming soon", Toast.LENGTH_SHORT).show();
        });

        setupMenuItem(binding.itemCurrency.getRoot(), R.drawable.ic_orders_payments, "Currency", "INR", v -> {
             Toast.makeText(requireContext(), "Currency selection coming soon", Toast.LENGTH_SHORT).show();
        });

        // Second Group
        setupMenuItem(binding.itemNotifications.getRoot(), R.drawable.ic_home_notification, "Notifications", null, v -> {
            startActivity(new Intent(requireContext(), com.bloom.customer.ui.notifications.NotificationActivity.class));
        });

        setupMenuItem(binding.itemHistory.getRoot(), R.drawable.ic_orders_receipt, "Order History", null, v -> {
            if (requireActivity() instanceof com.bloom.customer.ui.home.HomeActivity) {
                View navOrders = requireActivity().findViewById(R.id.navOrders);
                if (navOrders != null) navOrders.performClick();
            }
        });

        setupMenuItem(binding.itemHelp.getRoot(), R.drawable.ic_lux_menu, "Help & Support", null, v -> {
            startActivity(new Intent(requireContext(), com.bloom.customer.ui.support.HelpCenterActivity.class));
        });

        binding.cvLogout.setOnClickListener(v -> logout());


    }

    private void setupMenuItem(View itemView, int iconRes, String title, String subtitle, View.OnClickListener listener) {
        ImageView icon = itemView.findViewById(R.id.ivMenuIcon);
        TextView tvTitle = itemView.findViewById(R.id.tvMenuTitle);
        TextView tvSubtitle = itemView.findViewById(R.id.tvMenuSubtitle);

        icon.setImageResource(iconRes);
        tvTitle.setText(title);
        
        if (subtitle != null) {
            tvSubtitle.setText(subtitle);
            tvSubtitle.setVisibility(View.VISIBLE);
        } else {
            tvSubtitle.setVisibility(View.GONE);
        }

        itemView.setOnClickListener(listener);
    }

    private void fetchProfileData() {
        if (!SessionManager.getInstance(requireContext()).isLoggedIn()) {
            binding.tvFullName.setText("Guest User");
            binding.tvEmail.setText("Log in to see your profile");
            binding.cvLogout.setVisibility(View.GONE);
            return;
        } else {
            binding.cvLogout.setVisibility(View.VISIBLE);
        }

        String userId = SessionManager.getInstance(requireContext()).getUserId();
        if (userId == null) return;

        profileRepository.getProfile(userId).observe(getViewLifecycleOwner(), result -> {
            if (result.status == NetworkResult.Status.SUCCESS && result.data != null) {
                binding.tvFullName.setText(result.data.getFullName());
                binding.tvEmail.setText(result.data.getPhone());
                
                if (result.data.getAvatarUrl() != null) {
                    Glide.with(this)
                            .load(result.data.getAvatarUrl())
                            .circleCrop()
                            .into(binding.ivAvatar);
                }
            }
        });
    }

    private void logout() {
        SessionManager.getInstance(requireContext()).clearSession();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
