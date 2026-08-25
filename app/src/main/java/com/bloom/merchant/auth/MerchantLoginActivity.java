package com.bloom.merchant.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bloom.customer.ui.auth.AuthViewModel;
import com.bloom.customer.util.InputValidator;
import com.bloom.databinding.ActivityMerchantLoginBinding;
import com.bloom.merchant.MerchantHomeActivity;
import com.bloom.customer.data.local.SessionManager;

public class MerchantLoginActivity extends AppCompatActivity {

    private ActivityMerchantLoginBinding binding;
    private AuthViewModel viewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMerchantLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        sessionManager = SessionManager.getInstance(this);

        binding.btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        if (!InputValidator.isValidPhone(phone)) {
            binding.tilPhone.setError("Enter a valid 10-digit phone number");
            return;
        } else {
            binding.tilPhone.setError(null);
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError("Password is required");
            return;
        } else {
            binding.tilPassword.setError(null);
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);

        viewModel.login(phone, password).observe(this, result -> {
            switch (result.status) {
                case SUCCESS:
                    checkMerchantRole();
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                    break;
                case LOADING:
                    // Already handled
                    break;
            }
        });
    }

    private void checkMerchantRole() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);
            Toast.makeText(this, "Session invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.checkRole(userId, "florist").observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);
            
            switch (result.status) {
                case SUCCESS:
                    Boolean isFlorist = result.data;
                    if (Boolean.TRUE.equals(isFlorist)) {
                        startActivity(new Intent(this, MerchantHomeActivity.class));
                        finish();
                    } else {
                        // Not a florist
                        sessionManager.clearSession(); // Log them out
                        Toast.makeText(this, "You don't have a merchant account", Toast.LENGTH_LONG).show();
                    }
                    break;
                case ERROR:
                    Toast.makeText(this, "Failed to verify role: " + result.message, Toast.LENGTH_SHORT).show();
                    break;
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.btnLogin.setEnabled(false);
                    break;
            }
        });
    }
}
