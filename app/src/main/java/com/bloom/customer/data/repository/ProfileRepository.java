package com.bloom.customer.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAPI;
import com.bloom.customer.data.model.Profile;
import com.bloom.customer.util.NetworkResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for User Profile data.
 */
public class ProfileRepository {

    private final SupabaseAPI api;

    public ProfileRepository(Context context) {
        this.api = RetrofitClient.getClient(context).create(SupabaseAPI.class);
    }

    public LiveData<NetworkResult<Profile>> getProfile(String userId) {
        MutableLiveData<NetworkResult<Profile>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        api.getProfile("eq." + userId).enqueue(new Callback<List<Profile>>() {
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    result.setValue(NetworkResult.success(response.body().get(0)));
                } else {
                    String errorMsg = "HTTP " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {}
                    result.setValue(NetworkResult.error(errorMsg, null));
                }
            }

            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }
}
