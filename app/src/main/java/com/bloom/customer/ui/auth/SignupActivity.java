package com.bloom.customer.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bloom.databinding.ActivitySignupBinding;
import com.bloom.customer.util.NetworkResult;
import com.bloom.customer.util.ValidationUtil;

/**
 * Activity for user signup.
 * Principle: Separation of Concerns - UI logic only.
 */
public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private com.bloom.customer.ui.auth.AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(com.bloom.customer.ui.auth.AuthViewModel.class);

        binding.btnSignup.setOnClickListener(v -> attemptSignup());
        binding.tvLoginLink.setOnClickListener(v -> finish());
    }

    private void attemptSignup() {
        String name = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (name.isEmpty()) {
            binding.tilName.setError("Name required");
            return;
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            binding.tilPhone.setError("Invalid phone number");
            return;
        }
        if (!ValidationUtil.isValidPassword(password)) {
            binding.tilPassword.setError("Password too short");
            return;
        }

        binding.tilName.setError(null);
        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);

        viewModel.signup(name, phone, password).observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                setLoading(true);
                binding.tvError.setVisibility(View.GONE);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                setLoading(false);
                binding.tvError.setVisibility(View.GONE);
                // After signup, we might need to send OTP explicitly or Supabase does it automatically
                // assuming automatic for now, navigating to verify
                Intent intent = new Intent(this, com.bloom.customer.ui.auth.OtpVerifyActivity.class);
                intent.putExtra("phone", phone);
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
        binding.btnSignup.setEnabled(!isLoading);
    }
}
