package com.bloom.customer.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bloom.databinding.ActivityLoginBinding;
import com.bloom.customer.ui.home.HomeActivity;
import com.bloom.customer.util.NetworkResult;
import com.bloom.customer.util.ValidationUtil;

/**
 * Activity for user login.
 * Principle: Separation of Concerns - UI logic only.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.tvSignupLink.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
        binding.tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });
    }

    private void attemptLogin() {
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (!ValidationUtil.isValidPhone(phone)) {
            binding.tilPhone.setError("Invalid phone number");
            return;
        }
        if (password.isEmpty()) {
            binding.tilPassword.setError("Password required");
            return;
        }

        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);

        viewModel.login(phone, password).observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                setLoading(true);
                binding.tvError.setVisibility(View.GONE);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                setLoading(false);
                binding.tvError.setVisibility(View.GONE);
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (result.status == NetworkResult.Status.ERROR) {
                setLoading(false);
                binding.tvError.setText(result.message);
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
    }
}
