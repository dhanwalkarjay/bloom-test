package com.bloom.customer.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

        // Use the explicit toolbar container for top inset to ensure it's not hidden
        FragmentStatusBar.applyTopInset(this, binding.profileToolbar);

        profileRepository = new ProfileRepository(requireContext());
        setupListeners();
        fetchProfileData();
    }

    private void setupListeners() {
        binding.cvLogout.setOnClickListener(v -> logout());
        binding.llSavedAddresses.setOnClickListener(v -> Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show());
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
                binding.tvEmail.setText(result.data.getPhone()); // using phone as email is not in Profile model currently
                
                Glide.with(this)
                        .load(result.data.getAvatarUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .circleCrop()
                        .into(binding.ivAvatar);
            } else if (result.status == NetworkResult.Status.ERROR) {
                // If profile not found, maybe show basic info from session if available
                binding.tvFullName.setText("Bloom User");
                binding.tvEmail.setText(SessionManager.getInstance(requireContext()).getUserId());
                if (result.message != null) {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show();
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
