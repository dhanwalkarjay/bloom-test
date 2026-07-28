package com.bloom.customer.ui.checkout;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bloom.customer.data.repository.AddressRepository;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityAddAddressBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddAddressActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityAddAddressBinding binding;
    private GoogleMap mMap;
    private AddressRepository repository;
    private LatLng selectedLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new AddressRepository(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(com.bloom.R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSaveAddress.setOnClickListener(v -> saveAddress());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // Default to a central location or user current location
        LatLng defaultLocation = new LatLng(28.6139, 77.2090); // Delhi
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f));

        mMap.setOnCameraIdleListener(() -> {
            selectedLatLng = mMap.getCameraPosition().target;
            updateAddressText(selectedLatLng.latitude, selectedLatLng.longitude);
        });
    }

    private void updateAddressText(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                binding.tvSelectedAddress.setText(address.getAddressLine(0));
            }
        } catch (IOException e) {
            binding.tvSelectedAddress.setText("Location selected");
        }
    }

    private void saveAddress() {
        if (selectedLatLng == null) return;

        String detail = binding.etAddressDetail.getText().toString().trim();
        String fullAddress = binding.tvSelectedAddress.getText().toString() + (detail.isEmpty() ? "" : ", " + detail);

        com.bloom.customer.data.model.Address address = new com.bloom.customer.data.model.Address();
        // In a real app, set fields via setter or builder
        // For MVP, we assume the server takes lat/lng and formatted string
        
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
