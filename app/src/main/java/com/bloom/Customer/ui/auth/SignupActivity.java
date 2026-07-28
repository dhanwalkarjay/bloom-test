package com.bloom.customer.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

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
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                setLoading(false);
                Toast.makeText(this, "Account created. Verifying phone...", Toast.LENGTH_SHORT).show();
                // After signup, we might need to send OTP explicitly or Supabase does it automatically
                // assuming automatic for now, navigating to verify
                Intent intent = new Intent(this, OtpVerifyActivity.class);
                intent.putExtra("phone", phone);
                startActivity(intent);
            } else if (result.status == NetworkResult.Status.ERROR) {
                setLoading(false);
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSignup.setEnabled(!isLoading);
    }
}
