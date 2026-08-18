package com.bloom.customer.ui.checkout;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.bloom.customer.data.repository.AddressRepository;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityAddAddressBinding;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddAddressActivity extends AppCompatActivity {

    private ActivityAddAddressBinding binding;
    private AddressRepository repository;
    private LatLng selectedLatLng;
    private String selectedCity = "Unknown";
    private FusedLocationProviderClient fusedLocationClient;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    fetchCurrentLocation();
                } else {
                    Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new AddressRepository(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupWebView();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSaveAddress.setOnClickListener(v -> saveAddress());
        
        if (binding.fabMyLocation != null) {
            binding.fabMyLocation.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fetchCurrentLocation();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            });
        }
        
        binding.etMapSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performMapSearch(v.getText().toString());
                
                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });
    }

    private void performMapSearch(String query) {
        if (query == null || query.trim().isEmpty()) return;
        
        Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(query, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    double lat = address.getLatitude();
                    double lng = address.getLongitude();
                    
                    runOnUiThread(() -> {
                        binding.webView.evaluateJavascript(
                            String.format(Locale.US, "map.flyTo([%f, %f], 16, {animate: true, duration: 2.0});", lat, lng), 
                            null
                        );
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Search failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void fetchCurrentLocation() {
        Toast.makeText(this, "Fetching current location...", Toast.LENGTH_SHORT).show();
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            String js = String.format(Locale.US, "map.flyTo([%f, %f], 16, {animate: true, duration: 2.0});", location.getLatitude(), location.getLongitude());
                            binding.webView.evaluateJavascript(js, null);
                        } else {
                            Toast.makeText(this, "Could not determine location", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void setupWebView() {
        WebSettings webSettings = binding.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        binding.webView.addJavascriptInterface(new MapInterface(), "Android");
        
        String html = "<html><head>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.7.1/dist/leaflet.css' />" +
                "<script src='https://unpkg.com/leaflet@1.7.1/dist/leaflet.js'></script>" +
                "<style>#map { height: 100%; width: 100%; margin: 0; padding: 0; }</style>" +
                "</head><body>" +
                "<div id='map'></div>" +
                "<script>" +
                "var map = L.map('map', {zoomControl: false}).setView([21.1458, 79.0882], 15);" +
                "L.tileLayer('https://{s}.basemaps.cartocdn.com/light_nolabels/{z}/{x}/{y}{r}.png', {" +
                "    attribution: '&copy; CARTO'" +
                "}).addTo(map);" +
                "map.on('moveend', function(e) {" +
                "    var center = map.getCenter();" +
                "    Android.onLocationChanged(center.lat, center.lng);" +
                "});" +
                "</script></body></html>";

        binding.webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        binding.webView.setWebViewClient(new WebViewClient());
        
        // Default
        selectedLatLng = new LatLng(21.1458, 79.0882);
        updateAddressText(21.1458, 79.0882);
    }

    private class MapInterface {
        @JavascriptInterface
        public void onLocationChanged(double lat, double lng) {
            runOnUiThread(() -> {
                selectedLatLng = new LatLng(lat, lng);
                updateAddressText(lat, lng);
            });
        }
    }

    private void updateAddressText(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                binding.tvSelectedAddress.setText(address.getAddressLine(0));
                
                // Extract city
                String locality = address.getLocality();
                if (locality != null) {
                    selectedCity = locality;
                } else if (address.getSubAdminArea() != null) {
                    selectedCity = address.getSubAdminArea();
                } else {
                    selectedCity = "Unknown";
                }
            } else {
                binding.tvSelectedAddress.setText(String.format("%.4f, %.4f", lat, lng));
                selectedCity = "Unknown";
            }
        } catch (IOException e) {
            binding.tvSelectedAddress.setText(String.format("%.4f, %.4f", lat, lng));
            selectedCity = "Unknown";
        }
    }

    private void saveAddress() {
        if (selectedLatLng == null) {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            return;
        }

        String detail = binding.etAddressDetail.getText().toString().trim();
        String fullAddress = binding.tvSelectedAddress.getText().toString() + (detail.isEmpty() ? "" : ", " + detail);

        com.bloom.customer.data.model.Address address = new com.bloom.customer.data.model.Address();
        address.setFullAddress(fullAddress);
        address.setLatitude(selectedLatLng.latitude);
        address.setLongitude(selectedLatLng.longitude);
        address.setLabel(detail.isEmpty() ? "Home" : detail);
        address.setCity(selectedCity);
        address.setUserId(com.bloom.customer.data.local.SessionManager.getInstance(this).getUserId());
        
        // Ensure required fields are not null for RLS/Constraints if any
        address.setRecipientName("Me"); // Default for now
        address.setRecipientPhone(""); 

        repository.addAddress(address).observe(this, result -> {
            if (result.status == NetworkResult.Status.SUCCESS) {
                Toast.makeText(this, "Address saved", Toast.LENGTH_SHORT).show();
                
                android.content.Intent returnIntent = new android.content.Intent();
                returnIntent.putExtra("address", fullAddress);
                returnIntent.putExtra("lat", selectedLatLng.latitude);
                returnIntent.putExtra("lng", selectedLatLng.longitude);
                setResult(RESULT_OK, returnIntent);
                
                finish();
            } else if (result.status == NetworkResult.Status.ERROR) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
