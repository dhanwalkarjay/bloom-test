package com.bloom.customer.ui.support;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.databinding.ActivitySupportBinding;

public class SupportActivity extends AppCompatActivity {

    private ActivitySupportBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySupportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnContactEmail.setOnClickListener(v -> 
            Toast.makeText(this, "Opening Email Client", Toast.LENGTH_SHORT).show());
        
        binding.btnContactPhone.setOnClickListener(v -> 
            Toast.makeText(this, "Calling Support", Toast.LENGTH_SHORT).show());
        
        binding.cardFaq1.setOnClickListener(v -> toggleFaq(binding.tvFaqAnswer1));
        binding.cardFaq2.setOnClickListener(v -> toggleFaq(binding.tvFaqAnswer2));
    }

    private void toggleFaq(android.view.View answerView) {
        if (answerView.getVisibility() == android.view.View.VISIBLE) {
            answerView.setVisibility(android.view.View.GONE);
        } else {
            answerView.setVisibility(android.view.View.VISIBLE);
        }
    }
}
