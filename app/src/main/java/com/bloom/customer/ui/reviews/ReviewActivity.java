package com.bloom.customer.ui.reviews;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.customer.data.model.Review;
import com.bloom.customer.data.repository.ReviewRepository;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityReviewBinding;

public class ReviewActivity extends AppCompatActivity {

    private ActivityReviewBinding binding;
    private ReviewRepository repository;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ReviewRepository(this);
        orderId = getIntent().getStringExtra("order_id");

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        int rating = (int) binding.ratingBar.getRating();
        String comment = binding.etComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.submitReview(orderId, rating, comment).observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                binding.btnSubmit.setEnabled(false);
                binding.tvError.setVisibility(View.GONE);
                binding.btnSubmit.setText("Submitting...");
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                binding.tvError.setVisibility(View.GONE);
                
                // Success Animation
                binding.cvReviewForm.animate().alpha(0f).setDuration(400).withEndAction(() -> {
                    binding.cvReviewForm.setVisibility(View.GONE);
                    
                    binding.llSuccessState.setAlpha(0f);
                    binding.llSuccessState.setVisibility(View.VISIBLE);
                    binding.llSuccessState.animate().alpha(1f).setDuration(400).start();
                    
                    new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1500);
                }).start();
                
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.btnSubmit.setEnabled(true);
                binding.btnSubmit.setText("Submit Review");
                binding.tvError.setText(result.message != null ? result.message : "Failed to submit review. Please try again.");
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });
    }
}
