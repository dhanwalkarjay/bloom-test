package com.bloom.customer.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.local.LocationHelper;
import com.bloom.customer.data.model.Product;
import com.bloom.customer.ui.auth.LoginActivity;
import com.bloom.customer.ui.location.ManualLocationActivity;
import com.bloom.customer.ui.lux.LuxActivity;
import com.bloom.customer.ui.notifications.NotificationActivity;
import com.bloom.customer.ui.product.ProductDetailActivity;
import com.bloom.customer.ui.search.SearchActivity;
import com.bloom.customer.ui.shop.ShopDetailActivity;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.FragmentHomeBinding;
import com.google.gson.Gson;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private ShopListAdapter shopAdapter;
    private FeaturedProductAdapter seasonalAdapter;
    private FeaturedProductAdapter bestsellerAdapter;
    private LocationHelper locationHelper;

    private final ActivityResultLauncher<Intent> manualLocationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    double lat = result.getData().getDoubleExtra("lat", 0);
                    double lng = result.getData().getDoubleExtra("lng", 0);
                    String area = result.getData().getStringExtra("area_name");
                    
                    // Save manual location to ViewModel so it persists across refreshes
                    viewModel.setManualLocation(lat, lng, area);
                    binding.tvCurrentLocation.setText(area);
                    fetchShops(lat, lng);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        locationHelper = new LocationHelper(requireContext());

        ViewCompat.setOnApplyWindowInsetsListener(binding.topBar, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    v.getPaddingLeft(),
                    insets.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );

            return windowInsets;
        });

        setupRecyclerView();
        setupObservers();
        setupListeners();

        updateGuestUI();
    }

    private void updateGuestUI() {
        boolean isLoggedIn = SessionManager.getInstance(requireContext()).isLoggedIn();
        if (isLoggedIn) {
            binding.llLoginPrompt.setVisibility(View.GONE);
            binding.rvShops.setVisibility(View.VISIBLE);
            if (viewModel.hasManualLocation()) {
                binding.tvCurrentLocation.setText(viewModel.getManualAreaName());
                fetchShops(viewModel.getManualLat(), viewModel.getManualLng());
            } else {
                checkLocationPermission();
            }
        } else {
            binding.llLoginPrompt.setVisibility(View.VISIBLE);
            binding.rvShops.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
            binding.emptyState.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        shopAdapter = new ShopListAdapter();
        binding.rvShops.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvShops.setAdapter(shopAdapter);

        shopAdapter.setOnShopClickListener(shop -> {
            Intent intent = new Intent(requireContext(), ShopDetailActivity.class);
            intent.putExtra("shop_json", new Gson().toJson(shop));
            startActivity(intent);
        });

        seasonalAdapter = new FeaturedProductAdapter();
        binding.rvSeasonal.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvSeasonal.setAdapter(seasonalAdapter);
        seasonalAdapter.setOnProductClickListener(this::openProductDetail);

        bestsellerAdapter = new FeaturedProductAdapter();
        binding.rvBestsellers.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvBestsellers.setAdapter(bestsellerAdapter);
        bestsellerAdapter.setOnProductClickListener(this::openProductDetail);
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra("product_json", new Gson().toJson(product));
        // For seasonal/bestseller, we assume they are from available shops or handled by proximity
        intent.putExtra("is_shop_open", true); 
        startActivity(intent);
    }

    private void setupObservers() {
        viewModel.getUserLocation().observe(getViewLifecycleOwner(), location -> {
            // Only use GPS location if user hasn't set a manual location
            if (location != null && !viewModel.hasManualLocation()) {
                updateLocationName(location.getLatitude(), location.getLongitude());
                fetchShops(location.getLatitude(), location.getLongitude());
            }
        });

        viewModel.getSeasonalProducts().observe(getViewLifecycleOwner(), result -> {
            if (result.status == NetworkResult.Status.SUCCESS) {
                seasonalAdapter.setProducts(result.data);
            }
        });

        viewModel.getBestsellerProducts().observe(getViewLifecycleOwner(), result -> {
            if (result.status == NetworkResult.Status.SUCCESS) {
                bestsellerAdapter.setProducts(result.data);
            }
        });
    }

    private void updateLocationName(double lat, double lng) {
        binding.tvCurrentLocation.setText(String.format(java.util.Locale.getDefault(), "%.4f, %.4f", lat, lng));

        android.location.Geocoder geocoder = new android.location.Geocoder(requireContext(), java.util.Locale.getDefault());
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
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> binding.tvCurrentLocation.setText(finalName));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (viewModel.hasManualLocation()) {
                // If manual location is set, re-fetch shops with manual coords
                fetchShops(viewModel.getManualLat(), viewModel.getManualLng());
            } else {
                viewModel.refreshLocation();
            }
            binding.swipeRefresh.setRefreshing(false);
        });

        binding.llLocation.setOnClickListener(v -> {
            manualLocationLauncher.launch(new Intent(requireContext(), ManualLocationActivity.class));
        });

        binding.ivCart.setOnClickListener(v -> {
            if (SessionManager.getInstance(requireContext()).isLoggedIn()) {
                startActivity(new Intent(requireContext(), NotificationActivity.class));
            } else {
                startActivity(new Intent(requireContext(), LoginActivity.class));
            }
        });

        binding.luxPromo.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), LuxActivity.class));
        });

        binding.chipAll.setOnClickListener(v -> openSearch(null));
        binding.chipBirthday.setOnClickListener(v -> openSearch("Birthday"));
        binding.chipAnniversary.setOnClickListener(v -> openSearch("Anniversary"));
        binding.chipSympathy.setOnClickListener(v -> openSearch("Sympathy"));
        binding.chipCongratulations.setOnClickListener(v -> openSearch("Congratulations"));

        binding.tvViewAllSeasonal.setOnClickListener(v -> openSearch(null));
        binding.tvViewAllBestsellers.setOnClickListener(v -> openSearch(null));

        binding.btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), LoginActivity.class));
        });
    }

    private void openSearch(String category) {
        Intent intent = new Intent(requireContext(), SearchActivity.class);
        if (category != null) {
            intent.putExtra("category", category);
        }
        if (viewModel.hasManualLocation()) {
            intent.putExtra("lat", viewModel.getManualLat());
            intent.putExtra("lng", viewModel.getManualLng());
        } else if (viewModel.getUserLocation().getValue() != null) {
            intent.putExtra("lat", viewModel.getUserLocation().getValue().getLatitude());
            intent.putExtra("lng", viewModel.getUserLocation().getValue().getLongitude());
        }
        startActivity(intent);
    }

    private void checkLocationPermission() {
        if (locationHelper.hasLocationPermission()) {
            viewModel.refreshLocation();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
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
                manualLocationLauncher.launch(new Intent(requireContext(), ManualLocationActivity.class));
            }
        }
    }

    private void fetchShops(double lat, double lng) {
        viewModel.getNearbyShops(lat, lng).observe(getViewLifecycleOwner(), result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.rvShops.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.GONE);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                if (result.data != null && !result.data.isEmpty()) {
                    shopAdapter.setShops(result.data);
                    binding.rvShops.setVisibility(View.VISIBLE);
                    binding.emptyState.setVisibility(View.GONE);
                } else {
                    binding.rvShops.setVisibility(View.GONE);
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                binding.rvShops.setVisibility(View.GONE);
                binding.tvEmptyTitle.setText("Couldn't load shops");
                binding.tvEmptySubtitle.setText(result.message != null ? result.message : "Pull down to retry.");
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
