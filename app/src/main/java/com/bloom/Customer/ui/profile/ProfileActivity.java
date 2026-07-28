package com.bloom.customer.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.repository.AddressRepository;
import com.bloom.customer.data.repository.ProfileRepository;
import com.bloom.customer.ui.auth.LoginActivity;
import com.bloom.customer.ui.checkout.AddressAdapter;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityProfileBinding;
import com.bumptech.glide.Glide;

/**
 * Activity for user profile and account settings.
 */
public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private ProfileRepository profileRepository;
    private AddressRepository addressRepository;
    private AddressAdapter addressAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        profileRepository = new ProfileRepository(this);
        addressRepository = new AddressRepository(this);

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        fetchProfileData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        addressAdapter = new AddressAdapter();
        binding.rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAddresses.setAdapter(addressAdapter);
        
        // Read-only view in profile for now
        addressAdapter.setListener(address -> {
            // Optional: edit address logic
        });
    }

    private void setupListeners() {
        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void fetchProfileData() {
        String userId = SessionManager.getInstance(this).getUserId();
        if (userId == null) return;

        // Fetch User Info
        profileRepository.getProfile(userId).observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (result.status == NetworkResult.Status.SUCCESS && result.data != null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvFullName.setText(result.data.getFullName());
                binding.tvPhone.setText(result.data.getPhone());
                
                Glide.with(this)
                        .load(result.data.getAvatarUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .circleCrop()
                        .into(binding.ivAvatar);
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch Addresses
        addressRepository.getAddresses(userId).observe(this, result -> {
            if (result.status == NetworkResult.Status.SUCCESS) {
                addressAdapter.setAddresses(result.data);
            }
        });
    }

    private void logout() {
        SessionManager.getInstance(this).clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
