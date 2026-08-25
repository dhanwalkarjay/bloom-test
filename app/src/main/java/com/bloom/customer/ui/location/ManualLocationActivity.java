package com.bloom.customer.ui.location;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.Arrays;
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
    
    // Search debounce
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DEBOUNCE_MS = 350;
    
    // Recent searches
    private static final String PREFS_RECENT = "bloom_location_recent";
    private static final String KEY_RECENT_LIST = "recent_searches";
    private static final int MAX_RECENT = 3;
    
    // Map state
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
        startPinPulseAnimation();
        loadSavedAddresses();
        loadRecentSearches();
    }
    
    private void startPinPulseAnimation() {
        android.view.animation.Animation pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse_ring);
        binding.vPinPulse.setAlpha(0.5f);
        binding.vPinPulse.startAnimation(pulseAnim);
    }
    
    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void setupMap() {
        WebSettings webSettings = binding.mapWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        binding.mapWebView.addJavascriptInterface(new WebAppInterface(), "Android");

        // Detect dark mode for tile choice
        boolean isDark = (getResources().getConfiguration().uiMode & 
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
                android.content.res.Configuration.UI_MODE_NIGHT_YES;
        String tileLayer = isDark 
                ? "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
                : "https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png";
        String bgColor = isDark ? "#1a1a2e" : "#f8f5f2";
        
        String html = "<html><head>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.7.1/dist/leaflet.css' />" +
                "<script src='https://unpkg.com/leaflet@1.7.1/dist/leaflet.js'></script>" +
                "<style>body{margin:0;background:" + bgColor + ";}#map { height: 100%; width: 100%; margin: 0; padding: 0; }</style>" +
                "</head><body>" +
                "<div id='map'></div>" +
                "<script>" +
                "var map = L.map('map', {zoomControl: false}).setView([20.5937, 78.9629], 5);" +
                "L.tileLayer('" + tileLayer + "').addTo(map);" +
                "map.on('moveend', function() { " +
                "  var center = map.getCenter();" +
                "  Android.onMapMoved(center.lat, center.lng);" +
                "});" +
                "</script></body></html>";

        binding.mapWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                runOnUiThread(() -> {
                    binding.llOfflineFallback.setVisibility(View.VISIBLE);
                    binding.mapWebView.setVisibility(View.INVISIBLE);
                });
            }
        });
        binding.mapWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    /**
     * Load the user's saved addresses using stable ViewBinding IDs (not fragile getChildAt).
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

                // P0 Fix: Use stable ViewBinding IDs instead of fragile getChildAt()
                if (homeAddr != null) {
                    final Address finalHomeAddr = homeAddr;
                    binding.tvHomeSubtitle.setText(finalHomeAddr.getFullAddress());
                    binding.cvHome.setOnClickListener(v -> returnLocation(
                            finalHomeAddr.getLabel(), finalHomeAddr.getLatitude(), finalHomeAddr.getLongitude()));
                    binding.cvHome.setVisibility(View.VISIBLE);
                } else {
                    binding.cvHome.setVisibility(View.GONE);
                }

                if (workAddr != null) {
                    final Address finalWorkAddr = workAddr;
                    binding.tvWorkSubtitle.setText(finalWorkAddr.getFullAddress());
                    binding.cvWork.setOnClickListener(v -> returnLocation(
                            finalWorkAddr.getLabel(), finalWorkAddr.getLatitude(), finalWorkAddr.getLongitude()));
                    binding.cvWork.setVisibility(View.VISIBLE);
                } else {
                    binding.cvWork.setVisibility(View.GONE);
                }
                
                // Add New always shown — removed conditional hiding
                binding.cvAddNew.setVisibility(View.VISIBLE);
            }
        });
    }
    
    /**
     * Load real recent searches from SharedPreferences.
     */
    private void loadRecentSearches() {
        SharedPreferences prefs = getSharedPreferences(PREFS_RECENT, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_RECENT_LIST, "");
        if (raw.isEmpty()) return;
        
        String[] items = raw.split("\\|\\|");
        List<android.widget.LinearLayout> rows = Arrays.asList(binding.llRecent1, binding.llRecent2, binding.llRecent3);
        List<android.widget.TextView> labels = Arrays.asList(binding.tvRecent1, binding.tvRecent2, binding.tvRecent3);
        
        binding.tvRecentSearches.setVisibility(items.length > 0 ? View.VISIBLE : View.GONE);
        
        for (int i = 0; i < items.length && i < MAX_RECENT; i++) {
            final String location = items[i];
            labels.get(i).setText(location);
            rows.get(i).setVisibility(View.VISIBLE);
            rows.get(i).setOnClickListener(v -> geocodeAndProceedToStep2(location));
        }
    }
    
    /**
     * Save a searched address to SharedPreferences recent list.
     */
    private void saveRecentSearch(String areaName) {
        SharedPreferences prefs = getSharedPreferences(PREFS_RECENT, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_RECENT_LIST, "");
        List<String> items = new ArrayList<>(Arrays.asList(raw.isEmpty() ? new String[0] : raw.split("\\|\\|")));
        items.remove(areaName);
        items.add(0, areaName);
        if (items.size() > MAX_RECENT) items = items.subList(0, MAX_RECENT);
        prefs.edit().putString(KEY_RECENT_LIST, String.join("||", items)).apply();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());

        binding.ivClear.setOnClickListener(v -> binding.etManualAddress.setText(""));

        binding.etManualAddress.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.length() > 0;
                binding.ivClear.setVisibility(hasText ? View.VISIBLE : View.GONE);
                binding.btnConfirmMapLocation.setEnabled(hasText);
                binding.btnConfirmMapLocation.setAlpha(hasText ? 1.0f : 0.5f);
                
                // Debounced live suggestions
                if (searchHandler != null && searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                if (hasText && s.length() >= 3) {
                    searchRunnable = () -> fetchLiveSuggestions(s.toString().trim());
                    searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
                } else {
                    binding.rvSuggestions.setVisibility(View.GONE);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });
        
        binding.etManualAddress.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                // Half-expand instead of staying full, so map stays visible
                if (binding.vfBottomSheetSteps.getDisplayedChild() == 0) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                }
                binding.rvSuggestions.setVisibility(View.GONE);
            }
        });
        
        binding.etManualAddress.setOnClickListener(v ->
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));
        
        binding.etManualAddress.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                String addressText = binding.etManualAddress.getText().toString().trim();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                binding.rvSuggestions.setVisibility(View.GONE);
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

        binding.llCurrentLocation.setOnClickListener(v -> requestCurrentLocation());

        binding.btnConfirmMapLocation.setOnClickListener(v -> {
            String addressText = binding.etManualAddress.getText().toString().trim();
            if (!addressText.isEmpty() && !addressText.equals(lastSearchedText)) {
                binding.btnConfirmMapLocation.setEnabled(false);
                binding.btnConfirmMapLocation.setText("Confirming...");
                geocodeAndProceedToStep2(addressText);
            } else {
                binding.btnConfirmMapLocation.setEnabled(false);
                binding.btnConfirmMapLocation.setText("Confirming...");
                reverseGeocodeAndProceedToStep2(currentMapLat, currentMapLng);
            }
        });

        // Change Area back link in Step 2
        binding.tvChangeArea.setOnClickListener(v -> {
            binding.vfBottomSheetSteps.setDisplayedChild(0);
            binding.btnConfirmMapLocation.setVisibility(View.VISIBLE);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        });

        binding.cvAddNew.setOnClickListener(v ->
                Toast.makeText(this, "Add new address — coming soon!", Toast.LENGTH_SHORT).show());
        
        binding.btnSaveAddressDetails.setOnClickListener(v -> {
            String recipientName = binding.etRecipientName.getText() != null ? binding.etRecipientName.getText().toString().trim() : "";
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
            if (!landmark.isEmpty()) addressText += " (Near " + landmark + ")";
            final String finalFullAddress = addressText;
            
            binding.btnSaveAddressDetails.setEnabled(false);
            binding.btnSaveAddressDetails.setText("Saving...");
            
            Address newAddress = new Address();
            // P0 Fix: RLS handles user_id server-side via session token. No mock ID needed.
            newAddress.setFullAddress(finalFullAddress);
            newAddress.setLabel(label);
            newAddress.setLatitude(finalLat);
            newAddress.setLongitude(finalLng);
            newAddress.setDefault(true);
            if (!recipientName.isEmpty()) {
                newAddress.setRecipientName(recipientName);
            }
            
            addressRepository.addAddress(newAddress).observe(this, result -> {
                if (result.status == NetworkResult.Status.SUCCESS) {
                    returnLocation(finalFullAddress, finalLat, finalLng);
                } else if (result.status == NetworkResult.Status.ERROR) {
                    binding.btnSaveAddressDetails.setEnabled(true);
                    binding.btnSaveAddressDetails.setText("Save Address");
                    Snackbar.make(binding.getRoot(), "Failed to save address. Please try again.", Snackbar.LENGTH_LONG)
                            .setAction("Retry", sv -> binding.btnSaveAddressDetails.performClick()).show();
                }
            });
        });
    }

    /**
     * Request real GPS. If permission is missing, ask for it.
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
    
    private void setGpsLoading(boolean isLoading) {
        binding.ivGpsIcon.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        binding.progressGps.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.tvGpsSubtitle.setText(isLoading ? "Detecting your location..." : "Enable GPS for precise delivery");
        binding.llCurrentLocation.setEnabled(!isLoading);
    }

    /**
     * Fetch current GPS, showing a proper loading indicator in the card.
     */
    private void fetchRealCurrentLocation() {
        setGpsLoading(true);
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                setGpsLoading(false);
                if (location != null) {
                    reverseGeocodeAndProceedToStep2(location.getLatitude(), location.getLongitude());
                } else {
                    requestFreshLocationFix();
                }
            }).addOnFailureListener(e -> {
                setGpsLoading(false);
                Snackbar.make(binding.getRoot(), "Could not get location. Please type your area.", Snackbar.LENGTH_LONG)
                        .setAction("Retry", sv -> fetchRealCurrentLocation()).show();
            });
        } catch (SecurityException e) {
            setGpsLoading(false);
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
                        runOnUiThread(() -> {
                            setGpsLoading(false);
                            Snackbar.make(binding.getRoot(), "Could not detect location. Please type your area.", Snackbar.LENGTH_LONG)
                                    .setAction("Retry", v -> requestCurrentLocation()).show();
                        });
                    }
                }
            }, Looper.getMainLooper());
        } catch (SecurityException e) {
            setGpsLoading(false);
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
        
        // Save to recent searches
        saveRecentSearch(areaName);
    }
    
    /**
     * Fetch live suggestions for the typed query using debounced Geocoder.
     */
    private void fetchLiveSuggestions(String query) {
        binding.progressSearch.setVisibility(View.VISIBLE);
        binding.ivClear.setVisibility(View.GONE);
        new Thread(() -> {
            try {
                android.location.Geocoder geocoder = new android.location.Geocoder(this, java.util.Locale.getDefault());
                List<android.location.Address> results = geocoder.getFromLocationName(query, 5);
                runOnUiThread(() -> {
                    binding.progressSearch.setVisibility(View.GONE);
                    binding.ivClear.setVisibility(View.VISIBLE);
                    if (results != null && !results.isEmpty()) {
                        showSuggestions(results, query);
                    } else {
                        binding.rvSuggestions.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    binding.progressSearch.setVisibility(View.GONE);
                    binding.ivClear.setVisibility(View.VISIBLE);
                    binding.rvSuggestions.setVisibility(View.GONE);
                });
            }
        }).start();
    }
    
    private void showSuggestions(List<android.location.Address> addresses, String query) {
        List<String[]> items = new ArrayList<>();
        for (android.location.Address addr : addresses) {
            String title = addr.getSubLocality() != null ? addr.getSubLocality() 
                    : addr.getLocality() != null ? addr.getLocality() : query;
            String subtitle = addr.getLocality() != null && addr.getSubLocality() != null
                    ? addr.getLocality() + (addr.getAdminArea() != null ? ", " + addr.getAdminArea() : "")
                    : (addr.getAdminArea() != null ? addr.getAdminArea() : "");
            items.add(new String[]{title, subtitle, String.valueOf(addr.getLatitude()), String.valueOf(addr.getLongitude())});
        }
        
        androidx.recyclerview.widget.RecyclerView.Adapter<?> adapter = new androidx.recyclerview.widget.RecyclerView.Adapter<>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
                android.view.View v = android.view.LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_location_suggestion, parent, false);
                return new RecyclerView.ViewHolder(v) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                String[] item = items.get(position);
                android.widget.TextView tvTitle = holder.itemView.findViewById(R.id.tvSuggestionTitle);
                android.widget.TextView tvSub = holder.itemView.findViewById(R.id.tvSuggestionSubtitle);
                tvTitle.setText(item[0]);
                if (!item[1].isEmpty()) {
                    tvSub.setText(item[1]);
                    tvSub.setVisibility(View.VISIBLE);
                }
                holder.itemView.setOnClickListener(v -> {
                    binding.rvSuggestions.setVisibility(View.GONE);
                    binding.etManualAddress.setText(item[0]);
                    binding.etManualAddress.clearFocus();
                    double lat = Double.parseDouble(item[2]);
                    double lng = Double.parseDouble(item[3]);
                    binding.mapWebView.evaluateJavascript("map.setView([" + lat + ", " + lng + "], 15);", null);
                    currentMapLat = lat;
                    currentMapLng = lng;
                    lastSearchedText = item[0];
                    proceedToAddressDetails(item[0], lat, lng);
                });
            }

            @Override public int getItemCount() { return items.size(); }
        };
        
        binding.rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSuggestions.setAdapter(adapter);
        binding.rvSuggestions.setVisibility(View.VISIBLE);
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
            binding.vfBottomSheetSteps.setDisplayedChild(0);
            binding.btnConfirmMapLocation.setVisibility(View.VISIBLE);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}
