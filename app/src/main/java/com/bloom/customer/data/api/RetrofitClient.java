package com.bloom.customer.data.api;

import android.content.Context;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.util.Constants;
import com.bloom.BuildConfig;

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
            logging.setLevel(BuildConfig.DEBUG
                    ? HttpLoggingInterceptor.Level.BODY
                    : HttpLoggingInterceptor.Level.NONE);

            Interceptor networkInterceptor = chain -> {
                if (!com.bloom.customer.util.ConnectivityHelper.isConnected(context)) {
                    throw new com.bloom.customer.util.NoConnectivityException();
                }
                return chain.proceed(chain.request());
            };

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
                if (responseCount(response) >= 3) {
                    return null;
                }

                // If the request was for an auth endpoint, don't try to refresh/logout
                if (response.request().url().encodedPath().contains("/api/auth/")) {
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
                    .addInterceptor(networkInterceptor)
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

    private static String refreshAccessToken(Context context, String refreshToken) throws IOException {
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

        retrofit2.Response<com.bloom.customer.data.model.AuthResponse> response = 
            authApi.refreshToken("refresh_token", body).execute();
            
        if (response.isSuccessful() && response.body() != null) {
            com.bloom.customer.data.model.AuthResponse auth = response.body();
            String userId = auth.getUser() != null ? auth.getUser().getId() : SessionManager.getInstance(context).getUserId();
            SessionManager.getInstance(context).saveSession(
                    auth.getAccessToken(),
                    auth.getRefreshToken(),
                    userId
            );
            return auth.getAccessToken();
        } else if (response.code() == 400 || response.code() == 401) {
            return null; // Signals invalid refresh token, trigger logout
        } else {
            throw new IOException("Failed to refresh token: " + response.code());
        }
    }

    private static void logout(Context context) {
        // DEV MODE: Suppress forceful logout when testing with fake backend JWTs.
        // Supabase will reject the fake JWT with a 401, but we don't want to throw 
        // the user into an infinite login loop. 
        // 
        // SessionManager.getInstance(context).clearSession();
        // android.content.Intent intent = new android.content.Intent(context, com.bloom.customer.ui.auth.LoginActivity.class);
        // intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // context.startActivity(intent);
        
        android.util.Log.e("RetrofitClient", "Ignored 401 Unauthorized - Suppressing logout in Dev Mode.");
    }

    public static synchronized void resetClient() {
        retrofit = null;
    }
}
