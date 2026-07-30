package com.bloom.customer.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAPI;
import com.bloom.customer.util.Constants;
import com.bloom.customer.util.NetworkResult;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Repository for payment operations via Supabase Edge Functions.
 * Wraps Razorpay order creation and verification.
 */
public class PaymentRepository {

    private final PaymentApi paymentApi;

    public PaymentRepository(Context context) {
        this.paymentApi = RetrofitClient.getClient(context).create(PaymentApi.class);
    }

    public LiveData<NetworkResult<Map<String, Object>>> createRazorpayOrder(double amount, String currency) {
        MutableLiveData<NetworkResult<Map<String, Object>>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        Map<String, Object> body = new HashMap<>();
        body.put("amount", (long) (amount * 100)); // Razorpay expects amount in paise
        body.put("currency", currency != null ? currency : "INR");

        paymentApi.createRazorpayOrder(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(NetworkResult.success(response.body()));
                } else {
                    result.setValue(NetworkResult.error("Failed to create payment order", null));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }

    private interface PaymentApi {
        @POST(Constants.FUNCTIONS_ENDPOINT + "create-razorpay-order")
        Call<Map<String, Object>> createRazorpayOrder(@Body Map<String, Object> body);
    }
}
