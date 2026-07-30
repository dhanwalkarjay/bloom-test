package com.bloom.customer.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAPI;
import com.bloom.customer.data.model.Addon;
import com.bloom.customer.data.model.Product;
import com.bloom.customer.data.model.ProductSearchResult;
import com.bloom.customer.util.NetworkResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public LiveData<NetworkResult<List<Product>>> getFeaturedProducts(boolean isSeasonal, boolean isBestseller) {
        MutableLiveData<NetworkResult<List<Product>>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        api.getFeaturedProducts(isSeasonal ? true : null, isBestseller ? true : null)
                .enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(NetworkResult.success(response.body()));
                } else {
                    result.setValue(NetworkResult.error("Failed to fetch featured products", null));
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<NetworkResult<List<ProductSearchResult>>> searchProductsNearby(double lat, double lng, String query, String cat) {
        MutableLiveData<NetworkResult<List<ProductSearchResult>>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        Map<String, Object> body = new HashMap<>();
        body.put("lat", lat);
        body.put("lng", lng);
        if (query != null && !query.isEmpty()) body.put("search_query", query);
        if (cat != null && !cat.isEmpty()) body.put("cat_filter", cat);

        api.searchProductsNearby(body).enqueue(new Callback<List<ProductSearchResult>>() {
            @Override
            public void onResponse(Call<List<ProductSearchResult>> call, Response<List<ProductSearchResult>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(NetworkResult.success(response.body()));
                } else {
                    result.setValue(NetworkResult.error("Search failed", null));
                }
            }

            @Override
            public void onFailure(Call<List<ProductSearchResult>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }
}
