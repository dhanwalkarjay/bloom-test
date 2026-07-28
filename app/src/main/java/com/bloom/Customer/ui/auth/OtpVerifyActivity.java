package com.bloom.customer.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bloom.customer.ui.home.HomeActivity;
import com.bloom.databinding.ActivityOtpVerifyBinding;
import com.bloom.customer.util.NetworkResult;

/**
 * Activity for OTP verification.
 * Principle: Separation of Concerns - UI logic only.
 */
public class OtpVerifyActivity extends AppCompatActivity {

    private ActivityOtpVerifyBinding binding;
    private AuthViewModel viewModel;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpVerifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        phone = getIntent().getStringExtra("phone");

        binding.btnVerify.setOnClickListener(v -> attemptVerify());
    }

    private void attemptVerify() {
        String otp = binding.etOtp.getText().toString().trim();

        if (otp.length() < 6) {
            binding.tilOtp.setError("Enter 6-digit code");
            return;
        }

        binding.tilOtp.setError(null);

        viewModel.verifyOtp(phone, otp).observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                setLoading(true);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                setLoading(false);
                Toast.makeText(this, "Verification successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (result.status == NetworkResult.Status.ERROR) {
                setLoading(false);
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnVerify.setEnabled(!isLoading);
    }
}
