package com.bloom.agent.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bloom.agent.AgentHomeActivity;
import com.bloom.customer.ui.auth.AuthViewModel;
import com.bloom.customer.util.InputValidator;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityLoginBinding;

public class AgentLoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.llBottomLinks.setVisibility(View.GONE);

        // Customize UI for Agent
        updateUIForLocale();
        
        binding.tvLanguageToggle.setVisibility(View.VISIBLE);
        binding.tvLanguageToggle.setOnClickListener(v -> {
            boolean isHindi = "🌐 English".equals(binding.tvLanguageToggle.getText().toString());
            setLocale(isHindi ? "hi" : "en");
            updateUIForLocale();
        });

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
    }
    
    private void setLocale(String languageCode) {
        java.util.Locale locale = new java.util.Locale(languageCode);
        java.util.Locale.setDefault(locale);
        android.content.res.Resources resources = getResources();
        android.content.res.Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        // Save to preferences ideally
    }

    private void updateUIForLocale() {
        binding.tvTitle.setText(getString(com.bloom.R.string.agent_login_title));
        ((android.widget.TextView)findViewById(com.bloom.R.id.tvSubtitle)).setText(getString(com.bloom.R.string.agent_login_title) + " Dashboard");
        binding.tilPhone.setHint(getString(com.bloom.R.string.agent_phone_hint));
        binding.btnLogin.setText(getString(com.bloom.R.string.agent_login));
        
        String currentLang = getResources().getConfiguration().locale.getLanguage();
        if ("hi".equals(currentLang)) {
            binding.tvLanguageToggle.setText("🌐 English");
        } else {
            binding.tvLanguageToggle.setText("🌐 हिंदी");
        }
    }

    private void attemptLogin() {
        String phone = binding.etPhone.getText().toString().trim();

        if (!InputValidator.isValidPhone(phone)) {
            binding.tilPhone.setError("Invalid phone number");
            return;
        }

        binding.tilPhone.setError(null);
        
        // Mocking login for Agent using deterministic password since UI doesn't have password anymore
        String password = com.bloom.customer.util.AuthUtils.generateSecurePassword(phone);

        viewModel.login(phone, password).observe(this, result -> {
            if (result.status == NetworkResult.Status.LOADING) {
                setLoading(true);
                binding.tvError.setVisibility(View.GONE);
            } else if (result.status == NetworkResult.Status.SUCCESS) {
                // Should check role == 'agent' here ideally via checkRole RPC
                // For MVP, if login succeeds, we proceed to AgentHomeActivity
                setLoading(false);
                binding.tvError.setVisibility(View.GONE);
                Intent intent = new Intent(this, AgentHomeActivity.class);
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
