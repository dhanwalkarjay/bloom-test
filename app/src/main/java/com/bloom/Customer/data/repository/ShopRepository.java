package com.bloom.customer.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAPI;
import com.bloom.customer.data.model.Shop;
import com.bloom.customer.util.NetworkResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for Shop-related data.
 * Pattern: Repository Pattern - abstracts data sources.
 * Principle: Single Responsibility - handles shop data flow.
 */
public class ShopRepository {

    private final SupabaseAPI shopApi;

    public ShopRepository(Context context) {
        this.shopApi = RetrofitClient.getClient(context).create(SupabaseAPI.class);
    }

    public LiveData<NetworkResult<List<Shop>>> getNearbyShops(double lat, double lng) {
        MutableLiveData<NetworkResult<List<Shop>>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        Map<String, Object> body = new HashMap<>();
        body.put("lat", lat);
        body.put("lng", lng);
        body.put("radius_km", 10.0);

        shopApi.getNearbyShops(body).enqueue(new Callback<List<Shop>>() {
            @Override
            public void onResponse(Call<List<Shop>> call, Response<List<Shop>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(NetworkResult.success(response.body()));
                } else {
                    result.setValue(NetworkResult.error("Failed to fetch nearby shops", null));
                }
            }

            @Override
            public void onFailure(Call<List<Shop>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }
}
