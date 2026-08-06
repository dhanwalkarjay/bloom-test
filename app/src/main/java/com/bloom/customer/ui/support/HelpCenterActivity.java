package com.bloom.customer.ui.support;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.databinding.ActivityHelpCenterBinding;

public class HelpCenterActivity extends AppCompatActivity {

    private ActivityHelpCenterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpCenterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.topicMyOrder.setOnClickListener(v -> {
             if (requireActivityInstance() != null) {
                // Switching to orders tab would be ideal, but for now just Toast
                Toast.makeText(this, "Check your My Orders section", Toast.LENGTH_SHORT).show();
             }
        });

        binding.btnChat.setOnClickListener(v -> {
            Toast.makeText(this, "Starting chat with support...", Toast.LENGTH_SHORT).show();
        });
        
        // Help Topics logic can be added here
    }

    private AppCompatActivity requireActivityInstance() {
        return this;
    }
}
