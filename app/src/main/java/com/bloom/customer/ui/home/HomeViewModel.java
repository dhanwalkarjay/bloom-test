package com.bloom.customer.ui.home;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.local.LocationHelper;
import com.bloom.customer.data.model.Product;
import com.bloom.customer.data.model.Shop;
import com.bloom.customer.data.repository.ProductRepository;
import com.bloom.customer.data.repository.ShopRepository;
import com.bloom.customer.util.NetworkResult;
import com.bloom.customer.data.repository.ShopRepository;
import com.bloom.customer.util.NetworkResult;

import java.util.List;

/**
 * ViewModel for the Home screen.
 * Principle: Single Responsibility - handles logic for shop discovery.
 */
public class HomeViewModel extends AndroidViewModel {

    private static final String PREFS_NAME = "bloom_prefs";
    private static final String KEY_HAS_MANUAL = "has_manual_location";
    private static final String KEY_LAT = "manual_lat";
    private static final String KEY_LNG = "manual_lng";
    private static final String KEY_AREA = "manual_area";

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final LocationHelper locationHelper;
    private final SharedPreferences prefs;
    private final MutableLiveData<Location> userLocation = new MutableLiveData<>();
    
    private final MutableLiveData<NetworkResult<List<Product>>> seasonalProducts = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<List<Product>>> bestsellerProducts = new MutableLiveData<>();
    private final MutableLiveData<NetworkResult<List<Product>>> newArrivalProducts = new MutableLiveData<>();

    // Manual location state - persists across refreshes until user explicitly changes it
    private boolean hasManualLocation = false;
    private double manualLat;
    private double manualLng;
    private String manualAreaName;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.shopRepository = new ShopRepository(application);
        this.productRepository = new ProductRepository(application);
        this.locationHelper = new LocationHelper(application);
        this.prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        loadPersistedLocation();
        fetchFeaturedProducts();
    }

    private void loadPersistedLocation() {
        hasManualLocation = prefs.getBoolean(KEY_HAS_MANUAL, false);
        if (hasManualLocation) {
            manualLat = Double.longBitsToDouble(prefs.getLong(KEY_LAT, 0));
            manualLng = Double.longBitsToDouble(prefs.getLong(KEY_LNG, 0));
            manualAreaName = prefs.getString(KEY_AREA, "");
        }
    }

    public LiveData<Location> getUserLocation() {
        return userLocation;
    }

    public void refreshLocation() {
        // If user manually set a location, don't override it with GPS on refresh
        if (hasManualLocation) return;

        locationHelper.getLastLocation(location -> {
            if (location != null) {
                userLocation.setValue(location);
            }
        });
    }

    public void setManualLocation(double lat, double lng, String areaName) {
        this.hasManualLocation = true;
        this.manualLat = lat;
        this.manualLng = lng;
        this.manualAreaName = areaName;

        prefs.edit()
                .putBoolean(KEY_HAS_MANUAL, true)
                .putLong(KEY_LAT, Double.doubleToRawLongBits(lat))
                .putLong(KEY_LNG, Double.doubleToRawLongBits(lng))
                .putString(KEY_AREA, areaName)
                .apply();
    }

    public boolean hasManualLocation() {
        return hasManualLocation;
    }

    public double getManualLat() {
        return manualLat;
    }

    public double getManualLng() {
        return manualLng;
    }

    public String getManualAreaName() {
        return manualAreaName;
    }

    public void clearManualLocation() {
        this.hasManualLocation = false;
        prefs.edit().remove(KEY_HAS_MANUAL).remove(KEY_LAT).remove(KEY_LNG).remove(KEY_AREA).apply();
    }

    public LiveData<NetworkResult<List<Shop>>> getNearbyShops(double lat, double lng) {
        return shopRepository.getNearbyShops(lat, lng);
    }

    public LiveData<NetworkResult<List<Product>>> getSeasonalProducts() {
        return seasonalProducts;
    }

    public LiveData<NetworkResult<List<Product>>> getBestsellerProducts() {
        return bestsellerProducts;
    }

    public LiveData<NetworkResult<List<Product>>> getNewArrivalProducts() {
        return newArrivalProducts;
    }

    public void fetchFeaturedProducts() {
        productRepository.getFeaturedProducts(true, false, false).observeForever(result -> seasonalProducts.setValue(result));
        productRepository.getFeaturedProducts(false, true, false).observeForever(result -> bestsellerProducts.setValue(result));
        productRepository.getFeaturedProducts(false, false, true).observeForever(result -> newArrivalProducts.setValue(result));
    }
}
