package com.bloom.customer.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAPI;
import com.bloom.customer.data.model.Occasion;
import com.bloom.customer.util.NetworkResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OccasionRepository {

    private final SupabaseAPI api;

    public OccasionRepository(Context context) {
        this.api = RetrofitClient.getClient(context).create(SupabaseAPI.class);
    }

    public LiveData<NetworkResult<List<Occasion>>> getOccasions(String userId) {
        MutableLiveData<NetworkResult<List<Occasion>>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        api.getOccasions("eq." + userId).enqueue(new Callback<List<Occasion>>() {
            @Override
            public void onResponse(Call<List<Occasion>> call, Response<List<Occasion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(NetworkResult.success(response.body()));
                } else {
                    result.setValue(NetworkResult.error("Failed to load occasions", null));
                }
            }

            @Override
            public void onFailure(Call<List<Occasion>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<NetworkResult<Void>> addOccasion(Occasion occasion) {
        MutableLiveData<NetworkResult<Void>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        api.createOccasion(occasion).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(NetworkResult.success(null));
                } else {
                    result.setValue(NetworkResult.error("Failed to add occasion", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<NetworkResult<Void>> updateOccasion(String occasionId, Occasion occasion) {
        MutableLiveData<NetworkResult<Void>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("title", occasion.getTitle());
        body.put("target_date", occasion.getTargetDate());
        body.put("recipient_name", occasion.getRecipientName());
        body.put("recipient_relation", occasion.getRecipientRelation());

        api.updateOccasion("eq." + occasionId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(NetworkResult.success(null));
                } else {
                    result.setValue(NetworkResult.error("Failed to update occasion", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<NetworkResult<Void>> deleteOccasion(String occasionId) {
        MutableLiveData<NetworkResult<Void>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        api.deleteOccasion("eq." + occasionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(NetworkResult.success(null));
                } else {
                    result.setValue(NetworkResult.error("Failed to delete occasion", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });
        return result;
    }
}
