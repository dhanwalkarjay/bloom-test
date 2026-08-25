package com.bloom.customer.data.api;


// Retrofit interface: auth-specific endpoints

import com.bloom.customer.data.model.AuthResponse;
import com.bloom.customer.util.Constants;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface SupabaseAuthApi {

    @POST(Constants.AUTH_ENDPOINT + "signup")
    Call<AuthResponse> signup(@Body Map<String, Object> body);

    @POST(Constants.AUTH_ENDPOINT + "otp")
    Call<Void> sendOtp(@Body Map<String, Object> body);

    @POST(Constants.AUTH_ENDPOINT + "verify")
    Call<AuthResponse> verifyOtp(@Body Map<String, Object> body);

    @POST(Constants.AUTH_ENDPOINT + "token")
    Call<AuthResponse> login(
            @Query("grant_type") String grantType,
            @Body Map<String, Object> body
    );

    @POST(Constants.AUTH_ENDPOINT + "token")
    Call<AuthResponse> refreshToken(
            @Query("grant_type") String grantType,
            @Body Map<String, Object> body
    );

    @POST(Constants.AUTH_ENDPOINT + "user")
    Call<Void> updateUserPhone(@Body Map<String, Object> body);

    @POST
    Call<AuthResponse> verifyTruecaller(@retrofit2.http.Url String url, @Body Map<String, Object> body);

    @POST
    Call<Map<String, String>> sendBackendOtp(@retrofit2.http.Url String url, @Body Map<String, Object> body);

    @POST
    Call<AuthResponse> verifyBackendOtp(@retrofit2.http.Url String url, @Body Map<String, Object> body);

    @PUT(Constants.AUTH_ENDPOINT + "user")
    Call<Void> updateUser(@Body Map<String, Object> body);
}
