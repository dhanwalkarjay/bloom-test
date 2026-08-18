package com.bloom.customer.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.ui.checkout.AddressAdapter;
import com.bloom.customer.util.SystemBarInsets;
import com.bloom.databinding.ActivityOccasionsBinding;

public class OccasionsActivity extends AppCompatActivity {

    private ActivityOccasionsBinding binding;
    private OccasionAdapter adapter;
    private com.bloom.customer.data.repository.OccasionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOccasionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false); // White icons over hero image
        androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(true);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        repository = new com.bloom.customer.data.repository.OccasionRepository(this);

        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnAdd.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, AddOccasionActivity.class));
        });
        
        binding.btnEmptyAdd.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, AddOccasionActivity.class));
        });

        adapter = new OccasionAdapter(new OccasionAdapter.OnOccasionClickListener() {
            @Override
            public void onDeleteClick(com.bloom.customer.data.model.Occasion occasion) {
                binding.progressBar.setVisibility(View.VISIBLE);
                repository.deleteOccasion(occasion.getId()).observe(OccasionsActivity.this, result -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (result.status == com.bloom.customer.util.NetworkResult.Status.SUCCESS) {
                        Toast.makeText(OccasionsActivity.this, "Occasion deleted", Toast.LENGTH_SHORT).show();
                        loadOccasions();
                    } else {
                        Toast.makeText(OccasionsActivity.this, "Failed to delete: " + result.message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onEditClick(com.bloom.customer.data.model.Occasion occasion) {
                android.content.Intent intent = new android.content.Intent(OccasionsActivity.this, AddOccasionActivity.class);
                intent.putExtra("occasion_id", occasion.getId());
                intent.putExtra("occasion_title", occasion.getTitle());
                intent.putExtra("occasion_date", occasion.getTargetDate());
                intent.putExtra("occasion_recipient", occasion.getRecipientName());
                intent.putExtra("occasion_relation", occasion.getRecipientRelation());
                startActivity(intent);
            }
        });

        binding.rvOccasions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOccasions.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOccasions();
    }

    private void loadOccasions() {
        String userId = SessionManager.getInstance(this).getUserId();
        binding.progressBar.setVisibility(View.VISIBLE);
        
        repository.getOccasions(userId).observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            if (result.status == com.bloom.customer.util.NetworkResult.Status.SUCCESS && result.data != null) {
                if (result.data.isEmpty()) {
                    binding.rvOccasions.setVisibility(View.GONE);
                    binding.emptyState.setVisibility(View.VISIBLE);
                } else {
                    binding.emptyState.setVisibility(View.GONE);
                    binding.rvOccasions.setVisibility(View.VISIBLE);
                    adapter.setOccasions(result.data);
                }
            } else if (result.status == com.bloom.customer.util.NetworkResult.Status.ERROR) {
                Toast.makeText(this, "Failed to load: " + result.message, Toast.LENGTH_SHORT).show();
                binding.rvOccasions.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}
