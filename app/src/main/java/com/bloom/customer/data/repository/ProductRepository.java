package com.bloom.customer.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAPI;
import com.bloom.customer.data.model.Addon;
import com.bloom.customer.data.model.Product;
import com.bloom.customer.util.NetworkResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for Product and Add-on data.
 * Pattern: Repository Pattern - abstracts data sources.
 */
public class ProductRepository {

    private final SupabaseAPI api;

    public ProductRepository(Context context) {
        this.api = RetrofitClient.getClient(context).create(SupabaseAPI.class);
    }

    public LiveData<NetworkResult<List<Product>>> getProductsByShop(String shopId) {
        MutableLiveData<NetworkResult<List<Product>>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        // Fix: Prepend "eq." for Supabase PostgREST filtering
        String filter = "eq." + shopId;

        api.getProductsByShop(filter).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(NetworkResult.success(response.body()));
                } else {
                    result.setValue(NetworkResult.error("Failed to fetch products", null));
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<NetworkResult<List<Addon>>> getAddons() {
        MutableLiveData<NetworkResult<List<Addon>>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        api.getAddons().enqueue(new Callback<List<Addon>>() {
            @Override
            public void onResponse(Call<List<Addon>> call, Response<List<Addon>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(NetworkResult.success(response.body()));
                } else {
                    result.setValue(NetworkResult.error("Failed to fetch addons", null));
                }
            }

            @Override
            public void onFailure(Call<List<Addon>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }
}
