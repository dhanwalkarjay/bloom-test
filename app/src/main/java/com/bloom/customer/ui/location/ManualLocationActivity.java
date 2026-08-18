package com.bloom.customer.ui.location;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bloom.R;
import com.bloom.customer.data.model.Address;
import com.bloom.customer.data.repository.AddressRepository;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityManualLocationBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.List;

/**
 * Location picker Activity.
 * - "Use Current Location" triggers real GPS via FusedLocationProviderClient.
 * - "Home" and "Work" cards are populated from the user's saved addresses in
 * Supabase.
 * - Typed address search is geocoded on-device via Android Geocoder.
 */
public class ManualLocationActivity extends AppCompatActivity {

    private ActivityManualLocationBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private AddressRepository addressRepository;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    
    private double currentMapLat = 20.5937;
    private double currentMapLng = 78.9629;
    private String lastSearchedText = "";
    
    private String finalAreaName;
    private double finalLat;
    private double finalLng;

    public class WebAppInterface {
        @android.webkit.JavascriptInterface
        public void onMapMoved(double lat, double lng) {
            currentMapLat = lat;
            currentMapLng = lng;
        }
    }

    private final ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarse = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (Boolean.TRUE.equals(fine) || Boolean.TRUE.equals(coarse)) {
                    fetchRealCurrentLocation();
                } else {
                    Toast.makeText(this, "Location permission denied. Please type your area.", Toast.LENGTH_SHORT)
                            .show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManualLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        addressRepository = new AddressRepository(this);

        setupMap();
        setupBottomSheet();
        setupListeners();
        loadSavedAddresses();
    }
    
    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
        // Ensure it starts in the collapsed state (1st stage)
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void setupMap() {
        WebSettings webSettings = binding.mapWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        binding.mapWebView.addJavascriptInterface(new WebAppInterface(), "Android");

        String html = "<html><head>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.7.1/dist/leaflet.css' />" +
                "<script src='https://unpkg.com/leaflet@1.7.1/dist/leaflet.js'></script>" +
                "<style>#map { height: 100%; width: 100%; margin: 0; padding: 0; }</style>" +
                "</head><body>" +
                "<div id='map'></div>" +
                "<script>" +
                "var map = L.map('map', {zoomControl: false}).setView([20.5937, 78.9629], 5);" +
                "L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png').addTo(map);" +
                "map.on('moveend', function() { " +
                "  var center = map.getCenter();" +
                "  Android.onMapMoved(center.lat, center.lng);" +
                "});" +
                "</script></body></html>";

        binding.mapWebView.setWebViewClient(new WebViewClient());
        binding.mapWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    /**
     * Load the user's real saved addresses from Supabase and populate
     * the Home and Work cards with actual data.
     */
    private void loadSavedAddresses() {
        addressRepository.getAddresses().observe(this, result -> {
            if (result.status == NetworkResult.Status.SUCCESS && result.data != null) {
                List<Address> addresses = result.data;

                Address homeAddr = null;
                Address workAddr = null;

                for (Address addr : addresses) {
                    String label = addr.getLabel() != null ? addr.getLabel().toLowerCase() : "";
                    if (homeAddr == null && label.contains("home")) {
                        homeAddr = addr;
                    } else if (workAddr == null && (label.contains("work") || label.contains("office"))) {
                        workAddr = addr;
                    }
                }

                // Populate Home card
                if (homeAddr != null) {
                    final Address finalHomeAddr = homeAddr;
                    // Find the subtitle TextView inside cvHome — index 0 is LinearLayout, [1] is
                    // subtitle TextView
                    android.widget.LinearLayout homeLL = (android.widget.LinearLayout) binding.cvHome.getChildAt(0);
                    if (homeLL != null && homeLL.getChildCount() >= 3) {
                        ((android.widget.TextView) homeLL.getChildAt(2)).setText(finalHomeAddr.getFullAddress());
                    }
                    binding.cvHome.setOnClickListener(v -> returnLocation(finalHomeAddr.getLabel(),
                            finalHomeAddr.getLatitude(), finalHomeAddr.getLongitude()));
                    binding.cvHome.setVisibility(View.VISIBLE);
                } else {
                    binding.cvHome.setVisibility(View.GONE);
                }

                // Populate Work card
                if (workAddr != null) {
                    final Address finalWorkAddr = workAddr;
                    android.widget.LinearLayout workLL = (android.widget.LinearLayout) binding.cvWork.getChildAt(0);
                    if (workLL != null && workLL.getChildCount() >= 3) {
                        ((android.widget.TextView) workLL.getChildAt(2)).setText(finalWorkAddr.getFullAddress());
                    }
                    binding.cvWork.setOnClickListener(v -> returnLocation(finalWorkAddr.getLabel(),
                            finalWorkAddr.getLatitude(), finalWorkAddr.getLongitude()));
                    binding.cvWork.setVisibility(View.VISIBLE);
                } else {
                    binding.cvWork.setVisibility(View.GONE);
                }
                
                if (homeAddr == null && workAddr == null) {
                    binding.cvAddNew.setVisibility(View.VISIBLE);
                } else {
                    binding.cvAddNew.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());

        binding.ivClear.setOnClickListener(v -> binding.etManualAddress.setText(""));

        binding.etManualAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.length() > 0;
                binding.ivClear.setVisibility(hasText ? View.VISIBLE : View.GONE);
                binding.btnConfirmMapLocation.setEnabled(hasText);
                binding.btnConfirmMapLocation.setAlpha(hasText ? 1.0f : 0.5f);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        
        binding.etManualAddress.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        
        binding.etManualAddress.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        
        binding.etManualAddress.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                String addressText = binding.etManualAddress.getText().toString().trim();
                
                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                
                // Drop down to 1st stage (Collapsed)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                
                if (!addressText.isEmpty()) {
                    lastSearchedText = addressText;
                    geocodeAndMoveMap(addressText);
                } else {
                    Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        // "Use Current Location" — now requests REAL GPS
        binding.llCurrentLocation.setOnClickListener(v -> requestCurrentLocation());

        // "Confirm Address" button for typed text
        binding.btnConfirmMapLocation.setOnClickListener(v -> {
            String addressText = binding.etManualAddress.getText().toString().trim();
            if (!addressText.isEmpty() && !addressText.equals(lastSearchedText)) {
                // Typed a new search but pressed confirm without hitting keyboard search
                binding.btnConfirmMapLocation.setEnabled(false);
                binding.btnConfirmMapLocation.setText("Confirming...");
                geocodeAndProceedToStep2(addressText);
            } else {
                // Confirm the map pin
                binding.btnConfirmMapLocation.setEnabled(false);
                binding.btnConfirmMapLocation.setText("Confirming...");
                reverseGeocodeAndProceedToStep2(currentMapLat, currentMapLng);
            }
        });

        // Setup Recent Searches
        binding.tvRecentSearches.setVisibility(View.VISIBLE);
        binding.llRecent1.setVisibility(View.VISIBLE);
        binding.llRecent2.setVisibility(View.VISIBLE);
        
        binding.llRecent1.setOnClickListener(v -> proceedToAddressDetails("Koregaon Park, Pune", 18.5362, 73.8939));
        binding.llRecent2.setOnClickListener(v -> proceedToAddressDetails("Bandra West, Mumbai", 19.0596, 72.8295));
        
        // Add new address placeholder
        binding.cvAddNew.setOnClickListener(v -> Toast.makeText(this, "Add new address coming soon!", Toast.LENGTH_SHORT).show());
        
        // Step 2: Save Address button
        binding.btnSaveAddressDetails.setOnClickListener(v -> {
            String houseNo = binding.etHouseNo.getText().toString().trim();
            if (houseNo.isEmpty()) {
                binding.etHouseNo.setError("House No. is required");
                return;
            }
            String landmark = binding.etLandmark.getText().toString().trim();
            
            String label = "Home";
            int checkedId = binding.cgSaveAs.getCheckedChipId();
            if (checkedId == R.id.chipWork) label = "Work";
            else if (checkedId == R.id.chipOther) label = "Other";
            
            String addressText = houseNo + ", " + finalAreaName;
            if (!landmark.isEmpty()) {
                addressText += " (Near " + landmark + ")";
            }
            final String finalFullAddress = addressText;
            
            binding.btnSaveAddressDetails.setEnabled(false);
            binding.btnSaveAddressDetails.setText("Saving...");
            
            Address newAddress = new Address();
            newAddress.setUserId("user_id_mock");
            newAddress.setFullAddress(finalFullAddress);
            newAddress.setLabel(label);
            newAddress.setLatitude(finalLat);
            newAddress.setLongitude(finalLng);
            newAddress.setDefault(true);
            
            addressRepository.addAddress(newAddress).observe(this, result -> {
                returnLocation(finalFullAddress, finalLat, finalLng);
            });
        });
    }

    /**
     * Request real GPS. If permission is missing, ask for it.
     * If already granted, directly get the fused last location.
     */
    private void requestCurrentLocation() {
        boolean hasFine = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarse = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasFine || hasCoarse) {
            fetchRealCurrentLocation();
        } else {
            locationPermissionLauncher.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    /**
     * Actually fetches the device's current GPS position using
     * FusedLocationProviderClient.
     * Falls back to a fresh location request if last known location is null (e.g.
     * device just started).
     */
    private void fetchRealCurrentLocation() {
        binding.llCurrentLocation.setAlpha(0.6f);
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                binding.llCurrentLocation.setAlpha(1f);
                if (location != null) {
                    reverseGeocodeAndProceedToStep2(location.getLatitude(), location.getLongitude());
                } else {
                    // Last location was null, request a fresh fix
                    requestFreshLocationFix();
                }
            }).addOnFailureListener(e -> {
                binding.llCurrentLocation.setAlpha(1f);
                Toast.makeText(this, "Could not get location. Please type your area.", Toast.LENGTH_SHORT).show();
            });
        } catch (SecurityException e) {
            binding.llCurrentLocation.setAlpha(1f);
            Toast.makeText(this, "Location permission required.", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestFreshLocationFix() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMaxUpdates(1)
                .build();
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    android.location.Location loc = locationResult.getLastLocation();
                    if (loc != null) {
                        reverseGeocodeAndProceedToStep2(loc.getLatitude(), loc.getLongitude());
                    } else {
                        runOnUiThread(() -> Toast.makeText(ManualLocationActivity.this,
                                "Could not detect location. Please type your area.", Toast.LENGTH_SHORT).show());
                    }
                }
            }, Looper.getMainLooper());
        } catch (SecurityException e) {
            Toast.makeText(this, "Location permission required.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Reverse geocode a lat/lng to a human-readable area name, then return it.
     */
    private void reverseGeocodeAndProceedToStep2(double lat, double lng) {
        new Thread(() -> {
            try {
                android.location.Geocoder geocoder = new android.location.Geocoder(this, java.util.Locale.getDefault());
                List<android.location.Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                String displayName = "Selected Location";
                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address addr = addresses.get(0);
                    String area = addr.getSubLocality();
                    String city = addr.getLocality();
                    if (area != null && !area.isEmpty())
                        displayName = area + (city != null ? ", " + city : "");
                    else if (city != null && !city.isEmpty())
                        displayName = city;
                }
                final String finalName = displayName;
                runOnUiThread(() -> proceedToAddressDetails(finalName, lat, lng));
            } catch (Exception e) {
                runOnUiThread(() -> proceedToAddressDetails("Selected Location", lat, lng));
            }
        }).start();
    }
    
    private void geocodeAndProceedToStep2(String addressText) {
        new Thread(() -> {
            try {
                android.location.Geocoder geocoder = new android.location.Geocoder(this, java.util.Locale.getDefault());
                List<android.location.Address> addresses = geocoder.getFromLocationName(addressText, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address address = addresses.get(0);
                    double lat = address.getLatitude();
                    double lng = address.getLongitude();
                    String displayName = address.getLocality() != null ? address.getLocality() : addressText;
                    runOnUiThread(() -> proceedToAddressDetails(displayName, lat, lng));
                } else {
                    runOnUiThread(() -> {
                        binding.btnConfirmMapLocation.setEnabled(true);
                        binding.btnConfirmMapLocation.setText("Confirm");
                        Toast.makeText(this, "Area not found. Try a more specific name.", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    binding.btnConfirmMapLocation.setEnabled(true);
                    binding.btnConfirmMapLocation.setText("Confirm");
                    Toast.makeText(this, "Search failed. Check your connection.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void proceedToAddressDetails(String areaName, double lat, double lng) {
        this.finalAreaName = areaName;
        this.finalLat = lat;
        this.finalLng = lng;

        binding.tvSelectedArea.setText(areaName);
        binding.vfBottomSheetSteps.setDisplayedChild(1);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        
        binding.btnConfirmMapLocation.setEnabled(true);
        binding.btnConfirmMapLocation.setText("Confirm");
        binding.btnConfirmMapLocation.setVisibility(View.GONE);
    }
    
    /**
     * Forward-geocode a typed address string, move the map, and update current pin location.
     */
    private void geocodeAndMoveMap(String addressText) {
        new Thread(() -> {
            try {
                android.location.Geocoder geocoder = new android.location.Geocoder(this, java.util.Locale.getDefault());
                List<android.location.Address> addresses = geocoder.getFromLocationName(addressText, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address address = addresses.get(0);
                    double lat = address.getLatitude();
                    double lng = address.getLongitude();
                    runOnUiThread(() -> {
                        binding.mapWebView.evaluateJavascript("map.setView([" + lat + ", " + lng + "], 15);", null);
                        currentMapLat = lat;
                        currentMapLng = lng;
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Area not found. Try a more specific name.", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Search failed. Check your connection.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * Forward-geocode a typed address string to lat/lng and return.
     */
    private void geocodeAndReturn(String addressText) {
        binding.btnConfirmMapLocation.setEnabled(false);
        binding.btnConfirmMapLocation.setText("Searching...");
        new Thread(() -> {
            try {
                android.location.Geocoder geocoder = new android.location.Geocoder(this, java.util.Locale.getDefault());
                List<android.location.Address> addresses = geocoder.getFromLocationName(addressText, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address address = addresses.get(0);
                    double lat = address.getLatitude();
                    double lng = address.getLongitude();
                    String displayName = address.getLocality() != null ? address.getLocality() : addressText;
                    runOnUiThread(() -> returnLocation(displayName, lat, lng));
                } else {
                    runOnUiThread(() -> {
                        binding.btnConfirmMapLocation.setEnabled(true);
                        binding.btnConfirmMapLocation.setText("Confirm");
                        Toast.makeText(this, "Area not found. Try a more specific name.", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    binding.btnConfirmMapLocation.setEnabled(true);
                    binding.btnConfirmMapLocation.setText("Confirm");
                    Toast.makeText(this, "Search failed. Check your connection.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * Return a confirmed location result to HomeFragment.
     */
    private void returnLocation(String areaName, double lat, double lng) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("lat", lat);
        resultIntent.putExtra("lng", lng);
        resultIntent.putExtra("area_name", areaName);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        if (binding.vfBottomSheetSteps.getDisplayedChild() == 1) {
            // If in Step 2, go back to Step 1
            binding.vfBottomSheetSteps.setDisplayedChild(0);
            binding.btnConfirmMapLocation.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
