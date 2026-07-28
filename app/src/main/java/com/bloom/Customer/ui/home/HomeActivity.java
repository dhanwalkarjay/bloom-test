package com.bloom.customer.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.customer.data.local.LocationHelper;
import com.bloom.customer.ui.location.ManualLocationActivity;
import com.bloom.customer.ui.shop.ShopDetailActivity;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityHomeBinding;
import com.google.gson.Gson;

/**
 * Main Activity for discovering nearby shops.
 * Principle: Separation of Concerns - UI logic only.
 */
public class HomeActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private ActivityHomeBinding binding;
    private HomeViewModel viewModel;
    private ShopListAdapter adapter;
    private LocationHelper locationHelper;

    private final ActivityResultLauncher<Intent> manualLocationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    double lat = result.getData().getDoubleExtra("lat", 0);
                    double lng = result.getData().getDoubleExtra("lng", 0);
                    String area = result.getData().getStringExtra("area_name");
                    
                    binding.tvCurrentLocation.setText(area);
                    fetchShops(lat, lng);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        locationHelper = new LocationHelper(this);

        setupRecyclerView();
        setupObservers();
        setupListeners();

        checkLocationPermission();
    }

    private void setupRecyclerView() {
        adapter = new ShopListAdapter();
        binding.rvShops.setLayoutManager(new LinearLayoutManager(this));
        binding.rvShops.setAdapter(adapter);

        adapter.setOnShopClickListener(shop -> {
            Intent intent = new Intent(this, ShopDetailActivity.class);
            intent.putExtra("shop_json", new Gson().toJson(shop));
            startActivity(intent);
        });
    }

    private void setupObservers() {
        viewModel.getUserLocation().observe(this, location -> {
            if (location != null) {
                updateLocationName(location.getLatitude(), location.getLongitude());
                fetchShops(location.getLatitude(), location.getLongitude());
            }
        });
    }

    private void updateLocationName(double lat, double lng) {
        // Show coordinates as immediate fallback
        binding.tvCurrentLocation.setText(String.format(java.util.Locale.getDefault(), "%.4f, %.4f", lat, lng));

        android.location.Geocoder geocoder = new android.location.Geocoder(this, java.util.Locale.getDefault());
        // Use a background thread for geocoding to avoid blocking main thread (potential source of delay)
        new Thread(() -> {
            try {
                java.util.List<android.location.Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address address = addresses.get(0);
                    String city = address.getLocality();
                    String area = address.getSubLocality();
                    
                    String displayName = "";
                    if (area != null && !area.isEmpty()) displayName += area + ", ";
                    if (city != null && !city.isEmpty()) displayName += city;
                    
                    if (!displayName.isEmpty()) {
                        String finalName = displayName;
                        runOnUiThread(() -> binding.tvCurrentLocation.setText(finalName));
                    }
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshLocation();
            binding.swipeRefresh.setRefreshing(false);
        });

        binding.toolbar.setOnClickListener(v -> {
            manualLocationLauncher.launch(new Intent(this, ManualLocationActivity.class));
        });
    }

    private void checkLocationPermission() {
        if (locationHelper.hasLocationPermission()) {
            viewModel.refreshLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.refreshLocation();
            } else {
                // Permission denied, redirect to Manual Location entry
                startActivity(new Intent(this, ManualLocationActivity.class));
                finish();
            }
        }
    }

    private void fetchShops(double lat, double lng) {
        viewModel.getNearbyShops(lat, lng).observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.emptyState.setVisibility(View.GONE);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                if (result.data != null && !result.data.isEmpty()) {
                    adapter.setShops(result.data);
                    binding.emptyState.setVisibility(View.GONE);
                } else {
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
