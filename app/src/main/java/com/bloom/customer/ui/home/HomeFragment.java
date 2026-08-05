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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.R;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.local.LocationHelper;
import com.bloom.customer.data.model.Product;
import com.bloom.customer.ui.auth.LoginActivity;
import com.bloom.customer.ui.cart.CartActivity;
import com.bloom.customer.ui.common.FragmentStatusBar;
import com.bloom.customer.ui.location.ManualLocationActivity;
import com.bloom.customer.ui.notifications.NotificationActivity;
import com.bloom.customer.ui.product.ProductDetailActivity;
import com.bloom.customer.ui.shop.ShopDetailActivity;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.FragmentHomeBinding;
import com.google.gson.Gson;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private MainSharedViewModel sharedViewModel;
    private ShopListAdapter shopAdapter;
    private FeaturedProductAdapter seasonalAdapter;
    private FeaturedProductAdapter bestsellerAdapter;
    private FeaturedProductAdapter newArrivalsAdapter;
    private com.bloom.customer.data.repository.CartRepository cartRepository;
    private com.bloom.customer.data.repository.FeatureFlagRepository featureFlagRepository;
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
        sharedViewModel = new ViewModelProvider(requireActivity()).get(MainSharedViewModel.class);
        cartRepository = com.bloom.customer.data.repository.CartRepository.getInstance(requireContext());
        featureFlagRepository = new com.bloom.customer.data.repository.FeatureFlagRepository(requireContext());
        locationHelper = new LocationHelper(requireContext());

        // Push the topBar down by the status bar height so the cream background
        // (#FFF8F7) fills the transparent status bar area seamlessly.
        FragmentStatusBar.applyTopInset(this, binding.topBar);

        setupRecyclerView();
        setupObservers();
        setupListeners();
        fetchFeatureFlags();

        updateGuestUI();
    }

    private void fetchFeatureFlags() {
        featureFlagRepository.getFeatureFlags().observe(getViewLifecycleOwner(), result -> {
            if (result.status == NetworkResult.Status.SUCCESS && result.data != null) {
                for (com.bloom.customer.data.model.FeatureFlag flag : result.data) {
                    if ("create_your_own".equals(flag.getKey())) {
                        boolean isEnabled = flag.isEnabled();
                        binding.cvCreateOwn.setAlpha(isEnabled ? 1.0f : 0.6f);
                        binding.tvComingSoon.setVisibility(isEnabled ? View.GONE : View.VISIBLE);
                        binding.cvCreateOwn.setOnClickListener(v -> {
                            if (isEnabled) {
                                Toast.makeText(requireContext(), "Opening Creation Studio", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Coming Soon", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
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
        setupFeaturedListener(seasonalAdapter);

        bestsellerAdapter = new FeaturedProductAdapter();
        binding.rvBestsellers.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvBestsellers.setAdapter(bestsellerAdapter);
        setupFeaturedListener(bestsellerAdapter);

        newArrivalsAdapter = new FeaturedProductAdapter();
        binding.rvNewArrivals.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvNewArrivals.setAdapter(newArrivalsAdapter);
        setupFeaturedListener(newArrivalsAdapter);
    }

    private void setupFeaturedListener(FeaturedProductAdapter adapter) {
        adapter.setOnProductClickListener(new FeaturedProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                openProductDetail(product);
            }

            @Override
            public void onAddClick(Product product) {
                cartRepository.addToCart(new com.bloom.customer.data.model.CartItem(product));
                Toast.makeText(requireContext(), "Added to cart: " + product.getName(), Toast.LENGTH_SHORT).show();
            }
        });
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

        viewModel.getNewArrivalProducts().observe(getViewLifecycleOwner(), result -> {
            if (result.status == NetworkResult.Status.SUCCESS) {
                newArrivalsAdapter.setProducts(result.data);
            }
        });

        cartRepository.getCartItems().observe(getViewLifecycleOwner(), items -> {
            int count = cartRepository.getCartCount();
            if (count > 0) {
                binding.tvCartBadge.setVisibility(View.VISIBLE);
                binding.tvCartBadge.setText(String.valueOf(count));
            } else {
                binding.tvCartBadge.setVisibility(View.GONE);
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

        binding.ivNotifications.setOnClickListener(v -> {
            if (SessionManager.getInstance(requireContext()).isLoggedIn()) {
                startActivity(new Intent(requireContext(), NotificationActivity.class));
            } else {
                startActivity(new Intent(requireContext(), LoginActivity.class));
            }
        });

        binding.ivCart.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), CartActivity.class));
        });

        binding.luxPromo.setOnClickListener(v -> {
            if (requireActivity() != null) {
                View navLux = requireActivity().findViewById(R.id.navLux);
                if (navLux != null) navLux.performClick();
            }
        });

        binding.chipAll.setOnClickListener(v -> openSearch(null));
        binding.chipBirthday.setOnClickListener(v -> openSearch("Birthday"));
        binding.chipAnniversary.setOnClickListener(v -> openSearch("Anniversary"));
        binding.chipSympathy.setOnClickListener(v -> openSearch("Sympathy"));
        binding.chipCongratulations.setOnClickListener(v -> openSearch("Congratulations"));

        binding.tvViewAllSeasonal.setOnClickListener(v -> openSearch(null));
        binding.tvViewAllBestsellers.setOnClickListener(v -> openSearch(null));
        binding.tvViewAllNewArrivals.setOnClickListener(v -> openSearch(null));

        binding.btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), LoginActivity.class));
        });
    }

    private void openSearch(String category) {
        if (requireActivity() instanceof HomeActivity) {
            sharedViewModel.setSearchParams(null, category);
            View navSearch = requireActivity().findViewById(R.id.navSearch);
            if (navSearch != null) navSearch.performClick();
        }
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
