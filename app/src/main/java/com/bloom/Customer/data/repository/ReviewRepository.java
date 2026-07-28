package com.bloom.customer.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAPI;
import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.model.Review;
import com.bloom.customer.util.NetworkResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for managing order reviews.
 */
public class ReviewRepository {

    private final SupabaseAPI api;
    private final Context context;

    public ReviewRepository(Context context) {
        this.context = context;
        this.api = RetrofitClient.getClient(context).create(SupabaseAPI.class);
    }

    public LiveData<NetworkResult<Void>> submitReview(String orderId, int rating, String comment) {
        Review review = new Review();
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setComment(comment);
        review.setUserId(SessionManager.getInstance(context).getUserId());

        return postReview(review);
    }

    public LiveData<NetworkResult<Void>> postReview(Review review) {
        MutableLiveData<NetworkResult<Void>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        api.postReview(review).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(NetworkResult.success(null));
                } else {
                    result.setValue(NetworkResult.error("Failed to submit review", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }
}
