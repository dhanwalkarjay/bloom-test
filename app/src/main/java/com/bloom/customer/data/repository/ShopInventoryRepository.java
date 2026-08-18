package com.bloom.customer.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAPI;
import com.bloom.customer.data.model.ShopInventoryItem;
import com.bloom.customer.util.NetworkResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopInventoryRepository {

    private final SupabaseAPI api;

    public ShopInventoryRepository(Context context) {
        this.api = RetrofitClient.getClient(context).create(SupabaseAPI.class);
    }

    public LiveData<NetworkResult<List<ShopInventoryItem>>> getInventoryForShop(String shopId) {
        MutableLiveData<NetworkResult<List<ShopInventoryItem>>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        api.getShopInventory("eq." + shopId).enqueue(new Callback<List<ShopInventoryItem>>() {
            @Override
            public void onResponse(Call<List<ShopInventoryItem>> call, Response<List<ShopInventoryItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(NetworkResult.success(response.body()));
                } else {
                    result.setValue(NetworkResult.error("Error fetching inventory: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<List<ShopInventoryItem>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }
}
