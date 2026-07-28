package com.bloom.customer.ui.home;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.local.LocationHelper;
import com.bloom.customer.data.model.Shop;
import com.bloom.customer.data.repository.ShopRepository;
import com.bloom.customer.util.NetworkResult;

import java.util.List;

/**
 * ViewModel for the Home screen.
 * Principle: Single Responsibility - handles logic for shop discovery.
 */
public class HomeViewModel extends AndroidViewModel {

    private final ShopRepository shopRepository;
    private final LocationHelper locationHelper;
    private final MutableLiveData<Location> userLocation = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.shopRepository = new ShopRepository(application);
        this.locationHelper = new LocationHelper(application);
    }

    public LiveData<Location> getUserLocation() {
        return userLocation;
    }

    public void refreshLocation() {
        locationHelper.getLastLocation(location -> {
            if (location != null) {
                userLocation.setValue(location);
            }
        });
    }

    public LiveData<NetworkResult<List<Shop>>> getNearbyShops(double lat, double lng) {
        return shopRepository.getNearbyShops(lat, lng);
    }
}
