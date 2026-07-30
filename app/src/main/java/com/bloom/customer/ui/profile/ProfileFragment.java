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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.repository.AddressRepository;
import com.bloom.customer.data.repository.ProfileRepository;
import com.bloom.customer.ui.auth.LoginActivity;
import com.bloom.customer.ui.checkout.AddressAdapter;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.FragmentProfileBinding;
import com.bumptech.glide.Glide;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileRepository profileRepository;
    private AddressRepository addressRepository;
    private AddressAdapter addressAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileRepository = new ProfileRepository(requireContext());
        addressRepository = new AddressRepository(requireContext());

        setupRecyclerView();
        setupListeners();
        fetchProfileData();
    }

    private void setupRecyclerView() {
        addressAdapter = new AddressAdapter();
        binding.rvAddresses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAddresses.setAdapter(addressAdapter);
    }

    private void setupListeners() {
        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void fetchProfileData() {
        String userId = SessionManager.getInstance(requireContext()).getUserId();
        if (userId == null) return;

        profileRepository.getProfile(userId).observe(getViewLifecycleOwner(), result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.tvError.setVisibility(View.GONE);
            } else if (result.status == NetworkResult.Status.SUCCESS && result.data != null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvFullName.setText(result.data.getFullName());
                binding.tvPhone.setText(result.data.getPhone());
                
                Glide.with(this)
                        .load(result.data.getAvatarUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .circleCrop()
                        .into(binding.ivAvatar);
                binding.tvError.setVisibility(View.GONE);
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvError.setText(result.message != null ? result.message : "Failed to load profile.");
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });

        addressRepository.getAddresses(userId).observe(getViewLifecycleOwner(), result -> {
            if (result.status == NetworkResult.Status.SUCCESS) {
                addressAdapter.setAddresses(result.data);
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
