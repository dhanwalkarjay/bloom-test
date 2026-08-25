package com.bloom.customer.data.local;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Helper class for GPS and Location permissions.
 */
public class LocationHelper {

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void getLastLocation(OnSuccessListener<Location> listener) {
        if (!hasLocationPermission()) return;

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    listener.onSuccess(location);
                } else {
                    requestFreshLocation(listener);
                }
            });
        } catch (SecurityException e) {
            timber.log.Timber.e(e, "Location permission missing or denied");
        }
    }

    private void requestFreshLocation(OnSuccessListener<Location> listener) {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMaxUpdates(1)
                .build();

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    listener.onSuccess(locationResult.getLastLocation());
                }
            }, Looper.getMainLooper());
        } catch (SecurityException e) {
            timber.log.Timber.e(e, "Location permission missing or denied");
        }
    }
}
