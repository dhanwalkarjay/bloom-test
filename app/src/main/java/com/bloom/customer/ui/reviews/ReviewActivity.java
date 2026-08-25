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
import android.graphics.Color;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.graphics.Insets;

public class ReviewActivity extends AppCompatActivity {

    private ActivityReviewBinding binding;
    private ReviewRepository repository;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Premium Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        
        binding = ActivityReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new ReviewRepository(this);
        orderId = getIntent().getStringExtra("order_id");

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSubmit.setOnClickListener(v -> submitReview());
        
        binding.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser && rating > 0) {
                binding.tvError.setVisibility(View.GONE);
            }
        });
    }

    private void submitReview() {
        int rating = (int) binding.ratingBar.getRating();
        String comment = binding.etComment.getText().toString().trim();

        if (rating == 0) {
            binding.tvError.setText("Please select a rating");
            binding.tvError.setVisibility(View.VISIBLE);
            binding.ratingBar.animate().translationX(10f).setDuration(50).withEndAction(() -> {
                binding.ratingBar.animate().translationX(-10f).setDuration(50).withEndAction(() -> {
                    binding.ratingBar.animate().translationX(0f).setDuration(50).start();
                }).start();
            }).start();
            return;
        }

        repository.submitReview(orderId, rating, comment).observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                binding.btnSubmit.setEnabled(false);
                binding.tvError.setVisibility(View.GONE);
                binding.btnSubmit.setText("Submitting...");
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                binding.tvError.setVisibility(View.GONE);
                
                if (rating >= 4) {
                    binding.tvSuccessTitle.setText("We're Thrilled!");
                    binding.tvSuccessSubtitle.setText("Thank you for sharing your wonderful experience.");
                    binding.ivSuccessIcon.setImageResource(com.bloom.R.drawable.ic_search_heart_filled);
                } else if (rating == 3) {
                    binding.tvSuccessTitle.setText("Thank You!");
                    binding.tvSuccessSubtitle.setText("Your feedback is invaluable to us.");
                    binding.ivSuccessIcon.setImageResource(com.bloom.R.drawable.ic_premium_check_circle);
                } else {
                    binding.tvSuccessTitle.setText("We're So Sorry");
                    binding.tvSuccessSubtitle.setText("We appreciate your honesty and will work to make this right.");
                    binding.ivSuccessIcon.setImageResource(com.bloom.R.drawable.ic_phone);
                }
                
                // Success Animation
                binding.cvReviewForm.animate().alpha(0f).setDuration(400).withEndAction(() -> {
                    binding.cvReviewForm.setVisibility(View.GONE);
                    
                    binding.llSuccessState.setAlpha(0f);
                    binding.llSuccessState.setVisibility(View.VISIBLE);
                    binding.llSuccessState.animate().alpha(1f).setDuration(400).start();
                    
                    binding.getRoot().postDelayed(this::finish, 1500);
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
