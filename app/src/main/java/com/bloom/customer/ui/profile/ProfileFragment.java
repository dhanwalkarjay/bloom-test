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
import androidx.core.content.ContextCompat;
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
        // Authenticated Menu Items
        setupMenuItem(binding.itemOccasions.getRoot(), android.R.drawable.ic_menu_my_calendar, "Smart Occasions", "Never forget a date", v -> {
            startActivity(new Intent(requireContext(), com.bloom.customer.ui.profile.OccasionsActivity.class));
        }, false);

        setupMenuItem(binding.itemAddresses.getRoot(), R.drawable.ic_home_location, "Saved Addresses", null, v -> {
            Intent intent = new Intent(requireContext(), com.bloom.customer.ui.checkout.AddressSelectActivity.class);
            intent.putExtra("selection_mode", false);
            startActivity(intent);
        }, false);

        setupMenuItem(binding.itemPayment.getRoot(), R.drawable.ic_orders_payments, "Payment Methods", null, null, true);

        setupMenuItem(binding.itemLanguage.getRoot(), R.drawable.ic_search_tune, "Language", "English", null, true);

        setupMenuItem(binding.itemCurrency.getRoot(), R.drawable.ic_orders_payments, "Currency", "INR", null, true);

        setupMenuItem(binding.itemNotifications.getRoot(), R.drawable.ic_home_notification, "Notifications", null, v -> {
            startActivity(new Intent(requireContext(), com.bloom.customer.ui.notifications.NotificationActivity.class));
        }, false);

        setupMenuItem(binding.itemHistory.getRoot(), R.drawable.ic_orders_receipt, "Order History", null, v -> {
            if (requireActivity() instanceof com.bloom.customer.ui.home.HomeActivity) {
                View navOrders = requireActivity().findViewById(R.id.navOrders);
                if (navOrders != null) navOrders.performClick();
            }
        }, false);

        setupMenuItem(binding.itemHelp.getRoot(), R.drawable.ic_lux_menu, "Help & Support", null, v -> {
            startActivity(new Intent(requireContext(), com.bloom.customer.ui.support.HelpCenterActivity.class));
        }, false);

        // Guest Menu Items
        setupMenuItem(binding.itemGuestLanguage.getRoot(), R.drawable.ic_search_tune, "Language", "English", null, true);
        setupMenuItem(binding.itemGuestCurrency.getRoot(), R.drawable.ic_orders_payments, "Currency", "INR", null, true);
        setupMenuItem(binding.itemGuestHelp.getRoot(), R.drawable.ic_lux_menu, "Help & Support", null, v -> {
            startActivity(new Intent(requireContext(), com.bloom.customer.ui.support.HelpCenterActivity.class));
        }, false);

        binding.cvLogout.setOnClickListener(v -> logout());

        // Guest Actions
        View.OnClickListener loginListener = v -> startActivity(new Intent(requireContext(), LoginActivity.class));
        binding.btnGuestSignIn.setOnClickListener(loginListener);
        binding.btnJoinNow.setOnClickListener(loginListener);
    }

    private void setupMenuItem(View itemView, int iconRes, String title, String subtitle, View.OnClickListener listener, boolean isSoon) {
        ImageView icon = itemView.findViewById(R.id.ivMenuIcon);
        TextView tvTitle = itemView.findViewById(R.id.tvMenuTitle);
        TextView tvSubtitle = itemView.findViewById(R.id.tvMenuSubtitle);
        TextView tvSoonBadge = itemView.findViewById(R.id.tvSoonBadge);

        icon.setImageResource(iconRes);
        tvTitle.setText(title);
        
        if (subtitle != null) {
            tvSubtitle.setText(subtitle);
            tvSubtitle.setVisibility(View.VISIBLE);
        } else {
            tvSubtitle.setVisibility(View.GONE);
        }

        if (isSoon && tvSoonBadge != null) {
            tvSoonBadge.setVisibility(View.VISIBLE);
            itemView.setOnClickListener(v -> Toast.makeText(requireContext(), title + " coming soon", Toast.LENGTH_SHORT).show());
        } else if (tvSoonBadge != null) {
            tvSoonBadge.setVisibility(View.GONE);
            itemView.setOnClickListener(listener);
        } else {
            itemView.setOnClickListener(listener);
        }
    }

    private void fetchProfileData() {
        if (!SessionManager.getInstance(requireContext()).isLoggedIn()) {
            // Guest State
            binding.clGuestHeader.setVisibility(View.VISIBLE);
            binding.llGuestMenu.setVisibility(View.VISIBLE);
            
            binding.clAuthenticatedHeader.setVisibility(View.GONE);
            binding.llAuthenticatedMenu.setVisibility(View.GONE);
            binding.cvLogout.setVisibility(View.GONE);
            return;
        } else {
            // Authenticated State
            binding.clGuestHeader.setVisibility(View.GONE);
            binding.llGuestMenu.setVisibility(View.GONE);
            
            binding.clAuthenticatedHeader.setVisibility(View.VISIBLE);
            binding.llAuthenticatedMenu.setVisibility(View.VISIBLE);
            binding.cvLogout.setVisibility(View.VISIBLE);

            // Shimmer Loading State
            binding.shimmerName.setVisibility(View.VISIBLE);
            binding.shimmerEmail.setVisibility(View.VISIBLE);
            binding.tvFullName.setVisibility(View.INVISIBLE);
            binding.tvEmail.setVisibility(View.INVISIBLE);
        }

        String userId = SessionManager.getInstance(requireContext()).getUserId();
        if (userId == null) return;

        profileRepository.getProfile(userId).observe(getViewLifecycleOwner(), result -> {
            if (result.status == NetworkResult.Status.SUCCESS && result.data != null) {
                // Remove Shimmer
                binding.shimmerName.setVisibility(View.GONE);
                binding.shimmerEmail.setVisibility(View.GONE);
                binding.tvFullName.setVisibility(View.VISIBLE);
                binding.tvEmail.setVisibility(View.VISIBLE);

                String name = result.data.getFullName();
                binding.tvFullName.setText((name != null && !name.trim().isEmpty()) ? name : "User");

                String email = result.data.getEmail();
                String phone = result.data.getPhone();
                if (email != null && !email.trim().isEmpty()) {
                    binding.tvEmail.setText(email);
                } else if (phone != null && !phone.trim().isEmpty()) {
                    binding.tvEmail.setText(phone);
                } else {
                    binding.tvEmail.setText("No contact info");
                }
                
                if (result.data.getAvatarUrl() != null && !result.data.getAvatarUrl().isEmpty()) {
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
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
        Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
        fetchProfileData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
