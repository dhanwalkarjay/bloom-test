package com.bloom.customer.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAPI;
import com.bloom.customer.data.model.Address;
import com.bloom.customer.util.NetworkResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for managing delivery addresses.
 * Pattern: Repository Pattern.
 */
public class AddressRepository {

    private final SupabaseAPI api;

    public AddressRepository(Context context) {
        this.api = RetrofitClient.getClient(context).create(SupabaseAPI.class);
    }

    /**
     * Fetches all addresses for the authenticated user.
     * Server-side RLS automatically filters results based on the session token.
     */
    public LiveData<NetworkResult<List<Address>>> getAddresses() {
        MutableLiveData<NetworkResult<List<Address>>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        api.getAddresses().enqueue(new Callback<List<Address>>() {
            @Override
            public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(NetworkResult.success(response.body()));
                } else {
                    result.setValue(NetworkResult.error("Failed to fetch addresses", null));
                }
            }

            @Override
            public void onFailure(Call<List<Address>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<NetworkResult<Void>> addAddress(Address address) {
        MutableLiveData<NetworkResult<Void>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        api.addAddress(address).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(NetworkResult.success(null));
                } else {
                    String errorMsg = "Failed to add address: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    result.setValue(NetworkResult.error(errorMsg, null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<NetworkResult<Void>> setDefault(String addressId) {
        MutableLiveData<NetworkResult<Void>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        Map<String, Object> body = new HashMap<>();
        body.put("is_default", true);

        api.updateAddress("eq." + addressId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(NetworkResult.success(null));
                } else {
                    result.setValue(NetworkResult.error("Failed to set default address", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<NetworkResult<Address>> getAddressById(String addressId) {
        MutableLiveData<NetworkResult<Address>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        api.getAddressById("eq." + addressId).enqueue(new Callback<List<Address>>() {
            @Override
            public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    result.setValue(NetworkResult.success(response.body().get(0)));
                } else {
                    result.setValue(NetworkResult.error("Address not found", null));
                }
            }

            @Override
            public void onFailure(Call<List<Address>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }
}
