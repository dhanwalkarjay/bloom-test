package com.bloom.customer.ui.location;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.databinding.ActivityManualLocationBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Fallback Activity when GPS is denied or manual entry is needed.
 */
public class ManualLocationActivity extends AppCompatActivity {

    private ActivityManualLocationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManualLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        binding.btnRetryGps.setOnClickListener(v -> finish());

        binding.btnSetLocation.setOnClickListener(v -> {
            String addressText = binding.etManualAddress.getText().toString().trim();
            if (!addressText.isEmpty()) {
                geocodeAndReturn(addressText);
            } else {
                Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show();
            }
        });
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

                Intent resultIntent = new Intent();
                resultIntent.putExtra("lat", lat);
                resultIntent.putExtra("lng", lng);
                resultIntent.putExtra("area_name", displayName);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Location not found. Try a different name.", Toast.LENGTH_SHORT).show();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not find location. Check your connection.", Toast.LENGTH_SHORT).show();
        }
    }
}
