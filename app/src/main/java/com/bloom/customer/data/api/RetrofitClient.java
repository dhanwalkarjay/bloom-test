package com.bloom.customer.data.api;

import android.content.Context;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.util.Constants;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton class for Retrofit configuration.
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

                okhttp3.Response response = chain.proceed(requestBuilder.build());

                // Handle 401 JWT Expired
                if (response.code() == 401) {
                    SessionManager.getInstance(context).clearSession();
                    // Redirect to Login (simplest way is to clear stack)
                    android.content.Intent intent = new android.content.Intent(context, com.bloom.customer.ui.auth.LoginActivity.class);
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                }

                return response;
            };

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(headerInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.SUPABASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }

    public static synchronized void resetClient() {
        retrofit = null;
    }
}
