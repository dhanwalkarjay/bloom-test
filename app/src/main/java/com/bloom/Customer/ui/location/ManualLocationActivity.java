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
        // TODO: Implement real Geocoding (Google Maps Geocoding API or Geocoder)
        // For now, using placeholder coordinates to verify workflow
        double placeholderLat = 28.6139; // Delhi
        double placeholderLng = 77.2090;

        Intent resultIntent = new Intent();
        resultIntent.putExtra("lat", placeholderLat);
        resultIntent.putExtra("lng", placeholderLng);
        resultIntent.putExtra("area_name", addressText);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
