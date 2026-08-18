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
import com.bloom.customer.util.SystemBarInsets;
import com.bloom.databinding.ActivityAddressSelectBinding;

import java.util.List;

public class AddressSelectActivity extends AppCompatActivity {

    private ActivityAddressSelectBinding binding;
    private CheckoutViewModel viewModel;
    private AddressAdapter adapter;
    private boolean isSelectionMode = true;
    private List<Address> allAddresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddressSelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBarInsets.apply(this);

        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);
        
        setupToolbar();
        setupRecyclerView();
        
        isSelectionMode = getIntent().getBooleanExtra("selection_mode", true);
        setupSearch();

        // Always ensure bottomContainer is hidden initially
        binding.bottomContainer.setVisibility(View.GONE);

        fetchAddresses();

        binding.btnAddAddressCard.setOnClickListener(v -> {
            if (allAddresses != null && allAddresses.size() >= 5) {
                Toast.makeText(this, "You have reached the limit of 5 addresses.", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, AddAddressActivity.class));
        });

        binding.btnContinue.setOnClickListener(v -> {
            Address selected = adapter.getSelectedAddress();
            if (selected != null) {
                if (!isSelectionMode) {
                    // Profile mode: Save as default and return
                    SessionManager.getInstance(this).setDefaultAddressId(selected.getId());
                    Toast.makeText(this, "Default address updated", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // Checkout mode: Validate delivery radius before proceeding
                    String shopId = viewModel.getCartShopId();
                    if (shopId == null || shopId.isEmpty()) {
                        Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Disable button while checking
                    binding.btnContinue.setEnabled(false);
                    binding.btnContinue.setText("Validating address...");

                    viewModel.getShopById(shopId).observe(this, result -> {
                        binding.btnContinue.setEnabled(true);
                        binding.btnContinue.setText("Deliver Here");
                        
                        if (result.status == NetworkResult.Status.SUCCESS && result.data != null) {
                            com.bloom.customer.data.model.Shop shop = result.data;
                            
                            // Calculate distance
                            float[] results = new float[1];
                            android.location.Location.distanceBetween(
                                    shop.getLatitude(), shop.getLongitude(),
                                    selected.getLatitude(), selected.getLongitude(),
                                    results
                            );
                            double distanceKm = results[0] / 1000.0;
                            double allowedRadius = shop.getDeliveryRadiusKm() > 0 ? shop.getDeliveryRadiusKm() : 5.0;

                            if (distanceKm > allowedRadius) {
                                // Show error dialog
                                new androidx.appcompat.app.AlertDialog.Builder(this)
                                        .setTitle("Out of Delivery Zone")
                                        .setMessage(String.format("This address is %.1f km away. The florist only delivers within %.1f km. Please select a different address or clear your cart.", distanceKm, allowedRadius))
                                        .setPositiveButton("OK", null)
                                        .show();
                            } else {
                                // Valid address, proceed!
                                Intent intent = new Intent(this, DeliverySlotActivity.class);
                                intent.putExtra("address_id", selected.getId());
                                intent.putExtra("shop_json", new com.google.gson.Gson().toJson(shop));
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(this, "Failed to validate shop location", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
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
                if (allAddresses != null) {
                    String query = s.toString().toLowerCase().trim();
                    if (query.isEmpty()) {
                        adapter.setAddresses(allAddresses, SessionManager.getInstance(AddressSelectActivity.this).getDefaultAddressId());
                    } else {
                        List<Address> filtered = new java.util.ArrayList<>();
                        for (Address addr : allAddresses) {
                            String label = addr.getLabel() != null ? addr.getLabel().toLowerCase() : "";
                            String addressLine = addr.getAddressLine() != null ? addr.getAddressLine().toLowerCase() : "";
                            
                            if (label.contains(query) || addressLine.contains(query)) {
                                filtered.add(addr);
                            }
                        }
                        adapter.setAddresses(filtered, SessionManager.getInstance(AddressSelectActivity.this).getDefaultAddressId());
                    }
                }
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

        adapter.setListener(new AddressAdapter.OnAddressInteractionListener() {
            @Override
            public void onAddressSelected(Address address) {
                if (binding.bottomContainer.getVisibility() != View.VISIBLE) {
                    // Use a hyper-premium custom slide-up animation
                    binding.bottomContainer.setAlpha(0f);
                    binding.bottomContainer.setVisibility(View.INVISIBLE); // Make it invisible first to measure its final layout bounds
                    
                    binding.bottomContainer.post(() -> {
                        int height = binding.bottomContainer.getHeight();
                        // Start completely off-screen
                        binding.bottomContainer.setTranslationY(height + 150f); 
                        binding.bottomContainer.setVisibility(View.VISIBLE);
                        
                        binding.bottomContainer.animate()
                                .translationY(0f)
                                .alpha(1f)
                                .setDuration(600) // Smooth, luxurious duration
                                .setInterpolator(new androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                                .start();
                    });
                }
            }

            @Override
            public void onAddressDelete(Address address) {
                deleteAddress(address);
            }

            @Override
            public void onAddressEdit(Address address) {
                Intent intent = new Intent(AddressSelectActivity.this, AddAddressActivity.class);
                intent.putExtra("edit_address_id", address.getId());
                // Also pass JSON for easy population if needed, but normally ID is enough
                startActivity(intent);
            }
        });
    }

    private void deleteAddress(Address address) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Address")
                .setMessage("Are you sure you want to delete this address?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteAddress(address.getId()).observe(this, result -> {
                        if (result.status == com.bloom.customer.util.NetworkResult.Status.SUCCESS) {
                            Toast.makeText(this, "Address deleted", Toast.LENGTH_SHORT).show();
                            fetchAddresses();
                        } else if (result.status == com.bloom.customer.util.NetworkResult.Status.ERROR) {
                            Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void fetchAddresses() {
        if (!SessionManager.getInstance(this).isLoggedIn()) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.rvAddresses.setVisibility(View.GONE);
            return;
        }

        viewModel.getAddresses().observe(this, result -> {
            if (result.status == com.bloom.customer.util.NetworkResult.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.rvAddresses.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.GONE);
            } else if (result.status == com.bloom.customer.util.NetworkResult.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                if (result.data != null && !result.data.isEmpty()) {
                    allAddresses = result.data;
                    String query = binding.etSearch.getText().toString().toLowerCase().trim();
                    if (query.isEmpty()) {
                        adapter.setAddresses(allAddresses, SessionManager.getInstance(this).getDefaultAddressId());
                    } else {
                        // Re-apply filter if needed
                        List<Address> filtered = new java.util.ArrayList<>();
                        for (Address addr : allAddresses) {
                            String label = addr.getLabel() != null ? addr.getLabel().toLowerCase() : "";
                            String addressLine = addr.getAddressLine() != null ? addr.getAddressLine().toLowerCase() : "";
                            
                            if (label.contains(query) || addressLine.contains(query)) {
                                filtered.add(addr);
                            }
                        }
                        adapter.setAddresses(filtered, SessionManager.getInstance(this).getDefaultAddressId());
                    }
                    binding.rvAddresses.setVisibility(View.VISIBLE);
                    binding.emptyState.setVisibility(View.GONE);
                } else {
                    allAddresses = null;
                    binding.rvAddresses.setVisibility(View.GONE);
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
            } else if (result.status == com.bloom.customer.util.NetworkResult.Status.ERROR) {
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
