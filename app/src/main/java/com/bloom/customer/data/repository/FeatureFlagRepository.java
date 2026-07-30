package com.bloom.customer.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAPI;
import com.bloom.customer.data.model.FeatureFlag;
import com.bloom.customer.util.NetworkResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for feature flag data from Supabase.
 */
public class FeatureFlagRepository {

    private final SupabaseAPI api;

    public FeatureFlagRepository(Context context) {
        this.api = RetrofitClient.getClient(context).create(SupabaseAPI.class);
    }

    public LiveData<NetworkResult<List<FeatureFlag>>> getFeatureFlags() {
        MutableLiveData<NetworkResult<List<FeatureFlag>>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        api.getFeatureFlags().enqueue(new Callback<List<FeatureFlag>>() {
            @Override
            public void onResponse(Call<List<FeatureFlag>> call, Response<List<FeatureFlag>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(NetworkResult.success(response.body()));
                } else {
                    result.setValue(NetworkResult.error("Failed to fetch feature flags", null));
                }
            }

            @Override
            public void onFailure(Call<List<FeatureFlag>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }
}
