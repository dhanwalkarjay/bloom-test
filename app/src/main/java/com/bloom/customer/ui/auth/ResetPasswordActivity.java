package com.bloom.customer.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bloom.databinding.ActivityResetPasswordBinding;
import com.bloom.customer.util.NetworkResult;
import com.bloom.customer.util.ValidationUtil;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;
    private AuthViewModel viewModel;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.bloom.customer.util.SystemBarInsets.apply(this);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        phone = getIntent().getStringExtra("phone");

        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> finish());
        }
        binding.btnReset.setOnClickListener(v -> attemptReset());
    }

    private void attemptReset() {
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (!ValidationUtil.isValidPassword(password)) {
            binding.tilPassword.setError("Password too short");
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            return;
        }

        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        viewModel.updatePassword(phone, password).observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                setLoading(true);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                setLoading(false);
                Toast.makeText(this, "Password reset successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (result.status == NetworkResult.Status.ERROR) {
                setLoading(false);
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnReset.setEnabled(!isLoading);
    }
}
