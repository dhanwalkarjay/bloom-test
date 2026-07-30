package com.bloom.customer.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAuthApi;
import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.model.AuthResponse;
import com.bloom.customer.util.NetworkResult;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final SupabaseAuthApi authApi;
    private final SessionManager sessionManager;

    public AuthRepository(Context context){
        this.authApi = RetrofitClient.getClient(context).create(SupabaseAuthApi.class);
        this.sessionManager = SessionManager.getInstance(context);
    }

    public LiveData<NetworkResult<AuthResponse>> login(String phone, String password){
        MutableLiveData<NetworkResult<AuthResponse>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        Map<String, Object> body = new HashMap<>();
        body.put("phone", phone);
        body.put("password", password);

        authApi.login("password", body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    AuthResponse authResponse = response.body();
                    // Save session securely: access_token, refresh_token, user_id
                    sessionManager.saveSession(
                            authResponse.getAccessToken(),
                            authResponse.getRefreshToken(),
                            authResponse.getUser().getId()
                    );
                    result.setValue(NetworkResult.success(authResponse));
                } else {
                    result.setValue(NetworkResult.error("Login Failed: " + response.message(), null));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<NetworkResult<AuthResponse>> signup(String name, String phone, String password){
        MutableLiveData<NetworkResult<AuthResponse>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("full_name", name);

        Map<String, Object> body = new HashMap<>();
        body.put("phone", phone);
        body.put("password", password);
        body.put("data", metadata);

        authApi.signup(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    result.setValue(NetworkResult.success(response.body()));
                } else {
                    result.setValue(NetworkResult.error("Signup failed", null));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<NetworkResult<Void>> sendOtp(String phone) {
        MutableLiveData<NetworkResult<Void>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        Map<String, Object> body = new HashMap<>();
        body.put("phone", phone);

        authApi.sendOtp(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(NetworkResult.success(null));
                } else {
                    result.setValue(NetworkResult.error("Failed to send OTP", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<NetworkResult<AuthResponse>> verifyOtp(String phone, String token) {
        MutableLiveData<NetworkResult<AuthResponse>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        Map<String, Object> body = new HashMap<>();
        body.put("phone", phone);
        body.put("token", token);
        body.put("type", "sms");

        authApi.verifyOtp(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    sessionManager.saveSession(
                            authResponse.getAccessToken(),
                            authResponse.getRefreshToken(),
                            authResponse.getUser().getId()
                    );
                    result.setValue(NetworkResult.success(authResponse));
                } else {
                    result.setValue(NetworkResult.error("Verification failed", null));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });
        return result;
    }
}
