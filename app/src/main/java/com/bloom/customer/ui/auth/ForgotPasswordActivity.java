package com.bloom.customer.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bloom.customer.util.NetworkResult;
import com.bloom.customer.util.ValidationUtil;
import com.bloom.databinding.ActivityForgotPasswordBinding;

/**
 * Activity for password recovery (Forgot Password flow).
 * Sends an OTP to the user's phone for verification.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);
        androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(true);
        
        // Add padding to the topBar to accommodate the status bar
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.topBar, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top + v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        // Add padding to main view to accommodate the navigation bar
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnSendOtp.setOnClickListener(v -> attemptSendOtp());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void attemptSendOtp() {
        String phone = binding.etPhone.getText().toString().trim();

        if (!ValidationUtil.isValidPhone(phone)) {
            binding.tilPhone.setError("Enter a valid phone number");
            return;
        }

        binding.tilPhone.setError(null);

        viewModel.sendOtp(phone).observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                setLoading(true);
                binding.tvError.setVisibility(View.GONE);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                setLoading(false);
                binding.tvError.setVisibility(View.GONE);
                // Navigate to OTP verify with recovery flag
                android.content.Intent intent = new android.content.Intent(this, OtpVerifyActivity.class);
                intent.putExtra("phone", phone);
                intent.putExtra("is_recovery", true);
                startActivity(intent);
            } else if (result.status == NetworkResult.Status.ERROR) {
                setLoading(false);
                binding.tvError.setText(result.message);
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSendOtp.setEnabled(!isLoading);
    }
}
