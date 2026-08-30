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
    private final com.bloom.customer.data.api.SupabaseAPI supabaseApi;
    private final SessionManager sessionManager;

    public AuthRepository(Context context){
        this.authApi = RetrofitClient.getClient(context).create(SupabaseAuthApi.class);
        this.supabaseApi = RetrofitClient.getClient(context).create(com.bloom.customer.data.api.SupabaseAPI.class);
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

    public LiveData<NetworkResult<Void>> updatePassword(String phone, String password) {
        MutableLiveData<NetworkResult<Void>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        Map<String, Object> body = new HashMap<>();
        body.put("password", password);

        authApi.updateUser(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(NetworkResult.success(null));
                } else {
                    result.setValue(NetworkResult.error("Update failed", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<NetworkResult<AuthResponse>> loginWithGoogle(String idToken) {
        MutableLiveData<NetworkResult<AuthResponse>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));
        Map<String, Object> body = new HashMap<>();
        body.put("id_token", idToken);
        body.put("provider", "google");

        authApi.login("id_token", body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.getAccessToken() != null) {
                        sessionManager.saveSession(
                                authResponse.getAccessToken(),
                                authResponse.getRefreshToken(),
                                authResponse.getUser() != null ? authResponse.getUser().getId() : null
                        );
                    }
                    result.setValue(NetworkResult.success(authResponse));
                } else {
                    result.setValue(NetworkResult.error("Google Login failed", null));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<NetworkResult<Void>> updateUserPhone(String phone) {
        MutableLiveData<NetworkResult<Void>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        Map<String, Object> body = new HashMap<>();
        body.put("phone", phone);

        authApi.updateUserPhone(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(NetworkResult.success(null));
                } else {
                    result.setValue(NetworkResult.error("Failed to update phone", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<NetworkResult<AuthResponse>> verifyTruecaller(String authorizationCode, String codeVerifier) {
        MutableLiveData<NetworkResult<AuthResponse>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        Map<String, Object> body = new HashMap<>();
        body.put("authorizationCode", authorizationCode);
        body.put("codeVerifier", codeVerifier);

        String backendUrl = com.bloom.customer.util.Constants.BACKEND_URL + "/api/auth/truecaller/verify";
        
        authApi.verifyTruecaller(backendUrl, body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.getToken() != null) {
                        String userId = authResponse.getUser() != null ? authResponse.getUser().getId() : extractUserIdFromJwt(authResponse.getToken());
                        sessionManager.saveSession(
                                authResponse.getToken(),
                                null,
                                userId
                        );
                    }
                    result.setValue(NetworkResult.success(authResponse));
                } else {
                    result.setValue(NetworkResult.error("Truecaller verification failed: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                result.setValue(NetworkResult.error("Network error: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<NetworkResult<String>> sendBackendOtp(String phone) {
        MutableLiveData<NetworkResult<String>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        Map<String, Object> body = new HashMap<>();
        body.put("phone", phone);

        String backendUrl = com.bloom.customer.util.Constants.BACKEND_URL + "/api/auth/otp/send";
        
        authApi.sendBackendOtp(backendUrl, body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String devOtp = response.body().get("dev_otp");
                    result.setValue(NetworkResult.success(devOtp));
                } else {
                    result.setValue(NetworkResult.error("Failed to send OTP", null));
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<NetworkResult<AuthResponse>> verifyBackendOtp(String phone, String otp) {
        MutableLiveData<NetworkResult<AuthResponse>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        Map<String, Object> body = new HashMap<>();
        body.put("phone", phone);
        body.put("token", otp);

        String backendUrl = com.bloom.customer.util.Constants.BACKEND_URL + "/api/auth/otp/verify";
        
        authApi.verifyBackendOtp(backendUrl, body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.getToken() != null) {
                        String userId = authResponse.getUser() != null ? authResponse.getUser().getId() : extractUserIdFromJwt(authResponse.getToken());
                        sessionManager.saveSession(
                                authResponse.getToken(),
                                null,
                                userId
                        );
                    }
                    result.setValue(NetworkResult.success(authResponse));
                } else {
                    result.setValue(NetworkResult.error("Invalid OTP", null));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<NetworkResult<Boolean>> checkRole(String userId, String role) {
        MutableLiveData<NetworkResult<Boolean>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("role_name", role);

        supabaseApi.checkRole(body).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(NetworkResult.success(response.body()));
                } else {
                    result.setValue(NetworkResult.error("Role check failed", false));
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), false));
            }
        });

        return result;
    }

    private String extractUserIdFromJwt(String token) {
        try {
            String[] split = token.split("\\.");
            if (split.length > 1) {
                String payload = new String(android.util.Base64.decode(split[1], android.util.Base64.URL_SAFE), "UTF-8");
                org.json.JSONObject json = new org.json.JSONObject(payload);
                return json.optString("sub", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}


