package com.bloom.customer.ui.checkout;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new AddressRepository(this);

        setupWebView();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSaveAddress.setOnClickListener(v -> saveAddress());
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
                "var map = L.map('map').setView([21.1458, 79.0882], 13);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                "    attribution: '&copy; OpenStreetMap contributors'" +
                "}).addTo(map);" +
                "var marker = L.marker([21.1458, 79.0882], {draggable: true}).addTo(map);" +
                "marker.on('dragend', function(event) {" +
                "    var position = marker.getLatLng();" +
                "    Android.onLocationChanged(position.lat, position.lng);" +
                "});" +
                "map.on('click', function(e) {" +
                "    marker.setLatLng(e.latlng);" +
                "    Android.onLocationChanged(e.latlng.lat, e.latlng.lng);" +
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
                finish();
            } else if (result.status == NetworkResult.Status.ERROR) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
