package com.bloom.customer.ui.orderhistory;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.customer.data.repository.ReviewRepository;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityRateOrderBinding;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;

public class RateOrderActivity extends AppCompatActivity {

    private ActivityRateOrderBinding binding;
    private ReviewRepository repository;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRateOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        orderId = getIntent().getStringExtra("order_id");
        String productName = getIntent().getStringExtra("product_name");
        
        binding.tvProductName.setText(productName != null ? productName : "Bouquet");
        binding.tvOrderId.setText("Order #" + (orderId != null ? orderId.substring(0, 8) : "---"));

        repository = new ReviewRepository(this);

        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnSubmit.setOnClickListener(v -> {
            int rating = (int) binding.ratingBar.getRating();
            String comment = binding.etFeedback.getText().toString().trim();
            
            if (rating == 0) {
                Snackbar.make(binding.getRoot(), "Please select a rating", Snackbar.LENGTH_SHORT).show();
                return;
            }
            
            submitReview(rating, comment);
        });
    }

    private void submitReview(int rating, String comment) {
        binding.btnSubmit.setEnabled(false);
        binding.btnSubmit.setText("Submitting...");
        repository.submitReview(orderId, rating, comment).observe(this, result -> {
            if (result.status == NetworkResult.Status.SUCCESS) {
                // Success Animation
                binding.btnSubmit.animate().alpha(0f).setDuration(400).start();
                binding.llForm.animate().alpha(0f).setDuration(400).withEndAction(() -> {
                    binding.llForm.setVisibility(View.GONE);
                    binding.btnSubmit.setVisibility(View.GONE);
                    
                    binding.llSuccessState.setAlpha(0f);
                    binding.llSuccessState.setVisibility(View.VISIBLE);
                    binding.llSuccessState.animate().alpha(1f).setDuration(400).start();
                    
                    binding.getRoot().postDelayed(this::finish, 1500);
                }).start();
                
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.btnSubmit.setEnabled(true);
                binding.btnSubmit.setText("Submit Review");
                Snackbar.make(binding.getRoot(), result.message != null ? result.message : "Error submitting review", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
