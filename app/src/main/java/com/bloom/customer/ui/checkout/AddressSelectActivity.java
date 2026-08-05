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
    private CheckoutViewModel viewModel;
    private AddressAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddressSelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);
        
        setupToolbar();
        setupRecyclerView();
        setupSearch();
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

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Simplified filtering: would normally filter adapter list
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AddressAdapter();
        binding.rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAddresses.setAdapter(adapter);
    }

    private void fetchAddresses() {
        if (!SessionManager.getInstance(this).isLoggedIn()) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.rvAddresses.setVisibility(View.GONE);
            return;
        }

        viewModel.getAddresses().observe(this, result -> {
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
                
                String error = result.message != null ? result.message : "Error loading addresses";
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAddresses();
    }
}
