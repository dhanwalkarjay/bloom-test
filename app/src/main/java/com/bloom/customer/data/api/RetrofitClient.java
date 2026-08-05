package com.bloom.customer.data.api;

import android.content.Context;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.util.Constants;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Singleton class for Retrofit configuration.
 * Enhanced with: Token Refresh (Authenticator), Retry policy, and Timeouts.
 */
public class RetrofitClient {

    private static Retrofit retrofit = null;

    private RetrofitClient() {
    }

    public static synchronized Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            Interceptor headerInterceptor = chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("apikey", Constants.SUPABASE_ANON_KEY)
                        .header("Content-Type", "application/json");

                String token = SessionManager.getInstance(context).getAccessToken();
                if (token != null) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }

                return chain.proceed(requestBuilder.build());
            };

            Authenticator authenticator = (route, response) -> {
                if (responseCount(response) >= 2) {
                    // Give up after 2 attempts
                    logout(context);
                    return null;
                }

                String refreshToken = SessionManager.getInstance(context).getRefreshToken();
                if (refreshToken == null) {
                    logout(context);
                    return null;
                }

                // Synchronously refresh token
                String newToken = refreshAccessToken(context, refreshToken);
                if (newToken == null) {
                    logout(context);
                    return null;
                }

                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + newToken)
                        .build();
            };

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .addInterceptor(logging)
                    .addInterceptor(headerInterceptor)
                    .authenticator(authenticator)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.SUPABASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }

    private static int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    private static String refreshAccessToken(Context context, String refreshToken) {
        // We need a fresh Retrofit instance to avoid Authenticator recursion
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request r = chain.request().newBuilder()
                            .header("apikey", Constants.SUPABASE_ANON_KEY)
                            .header("Content-Type", "application/json")
                            .build();
                    return chain.proceed(r);
                }).build();

        Retrofit r = new Retrofit.Builder()
                .baseUrl(Constants.SUPABASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        SupabaseAuthApi authApi = r.create(SupabaseAuthApi.class);
        Map<String, Object> body = new HashMap<>();
        body.put("refresh_token", refreshToken);

        try {
            retrofit2.Response<com.bloom.customer.data.model.AuthResponse> response = 
                authApi.refreshToken("refresh_token", body).execute();
            if (response.isSuccessful() && response.body() != null) {
                com.bloom.customer.data.model.AuthResponse auth = response.body();
                SessionManager.getInstance(context).saveSession(
                        auth.getAccessToken(),
                        auth.getRefreshToken(),
                        auth.getUser().getId()
                );
                return auth.getAccessToken();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void logout(Context context) {
        SessionManager.getInstance(context).clearSession();
        android.content.Intent intent = new android.content.Intent(context, com.bloom.customer.ui.auth.LoginActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static synchronized void resetClient() {
        retrofit = null;
    }
}
