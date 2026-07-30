package com.bloom.customer.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.model.Address;
import com.bloom.customer.data.repository.AddressRepository;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityAddressSelectBinding;

import java.util.List;

public class AddressSelectActivity extends AppCompatActivity {

    private ActivityAddressSelectBinding binding;
    private AddressRepository repository;
    private AddressAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddressSelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new AddressRepository(this);
        
        setupToolbar();
        setupRecyclerView();
        fetchAddresses();

        binding.btnAddAddress.setOnClickListener(v -> {
            startActivity(new Intent(this, AddAddressActivity.class));
        });

        binding.btnContinue.setOnClickListener(v -> {
            Address selected = adapter.getSelectedAddress();
            if (selected != null) {
                Intent intent = new Intent(this, DeliverySlotActivity.class);
                intent.putExtra("address_id", selected.getId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select an address", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AddressAdapter();
        binding.rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAddresses.setAdapter(adapter);
    }

    private void fetchAddresses() {
        String userId = SessionManager.getInstance(this).getUserId();
        
        if (userId == null) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.rvAddresses.setVisibility(View.GONE);
            return;
        }

        repository.getAddresses(userId).observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.rvAddresses.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.GONE);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                if (result.data != null && !result.data.isEmpty()) {
                    adapter.setAddresses(result.data);
                    binding.rvAddresses.setVisibility(View.VISIBLE);
                    binding.emptyState.setVisibility(View.GONE);
                } else {
                    binding.rvAddresses.setVisibility(View.GONE);
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                binding.rvAddresses.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAddresses();
    }
}
