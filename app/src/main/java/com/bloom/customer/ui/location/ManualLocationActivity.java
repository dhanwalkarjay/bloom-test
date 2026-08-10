package com.bloom.customer.ui.location;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.databinding.ActivityManualLocationBinding;

/**
 * Fallback Activity when GPS is denied or manual entry is needed.
 * Upgraded to a premium Search-First Quick Commerce Location experience.
 */
public class ManualLocationActivity extends AppCompatActivity {

    private ActivityManualLocationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManualLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupMap();
        setupListeners();
    }
    
    private void setupMap() {
        WebSettings webSettings = binding.mapWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        
        String html = "<html><head>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.7.1/dist/leaflet.css' />" +
                "<script src='https://unpkg.com/leaflet@1.7.1/dist/leaflet.js'></script>" +
                "<style>#map { height: 100%; width: 100%; margin: 0; padding: 0; }</style>" +
                "</head><body>" +
                "<div id='map'></div>" +
                "<script>" +
                "var map = L.map('map', {zoomControl: false}).setView([18.5204, 73.8567], 15);" +
                "L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png').addTo(map);" +
                "</script></body></html>";

        binding.mapWebView.setWebViewClient(new WebViewClient());
        binding.mapWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.ivClear.setOnClickListener(v -> binding.etManualAddress.setText(""));

        binding.etManualAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.length() > 0;
                binding.ivClear.setVisibility(hasText ? View.VISIBLE : View.GONE);
                binding.btnSetLocation.setVisibility(hasText ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Quick Action Chips (Mock Data)
        binding.llCurrentLocation.setOnClickListener(v -> returnMockLocation("Current Location", 18.5204, 73.8567));
        binding.cvHome.setOnClickListener(v -> returnMockLocation("Home", 18.5362, 73.8968));
        binding.cvWork.setOnClickListener(v -> returnMockLocation("Work", 18.5523, 73.9143));
        
        // Recent Searches (Mock Data)
        binding.llRecent1.setOnClickListener(v -> returnMockLocation("Koregaon Park, Pune", 18.5362, 73.8968));
        binding.llRecent2.setOnClickListener(v -> returnMockLocation("Bandra West, Mumbai", 19.0596, 72.8295));

        binding.btnSetLocation.setOnClickListener(v -> {
            String addressText = binding.etManualAddress.getText().toString().trim();
            if (!addressText.isEmpty()) {
                geocodeAndReturn(addressText);
            } else {
                Toast.makeText(this, "Please enter an address or select a saved location", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void returnMockLocation(String areaName, double lat, double lng) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("lat", lat);
        resultIntent.putExtra("lng", lng);
        resultIntent.putExtra("area_name", areaName);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void geocodeAndReturn(String addressText) {
        android.location.Geocoder geocoder = new android.location.Geocoder(this, java.util.Locale.getDefault());
        try {
            java.util.List<android.location.Address> addresses = geocoder.getFromLocationName(addressText, 1);
            if (addresses != null && !addresses.isEmpty()) {
                android.location.Address address = addresses.get(0);
                double lat = address.getLatitude();
                double lng = address.getLongitude();
                String displayName = address.getAddressLine(0);
                if (displayName == null || displayName.isEmpty()) {
                    displayName = addressText;
                }

                returnMockLocation(displayName, lat, lng);
            } else {
                // If geocoding fails, still return the text as area_name for UX fluidity in demo mode
                returnMockLocation(addressText, 18.5204, 73.8567);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Fallback for demo
            returnMockLocation(addressText, 18.5204, 73.8567);
        }
    }
}
