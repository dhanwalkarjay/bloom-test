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

                return chain.proceed(requestBuilder.build());
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
}
