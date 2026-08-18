package com.bloom.customer.ui.profile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.model.Occasion;
import com.bloom.customer.data.repository.OccasionRepository;
import com.bloom.customer.util.NetworkResult;
import com.bloom.customer.util.SystemBarInsets;
import com.bloom.databinding.ActivityAddOccasionBinding;

import java.util.Calendar;
import java.util.Locale;

public class AddOccasionActivity extends AppCompatActivity {

    private ActivityAddOccasionBinding binding;
    private OccasionRepository repository;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddOccasionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBarInsets.apply(this);

        repository = new OccasionRepository(this);
        calendar = Calendar.getInstance();

        binding.btnBack.setOnClickListener(v -> finish());

        binding.etDate.setOnClickListener(v -> showDatePicker());

        binding.btnSave.setOnClickListener(v -> saveOccasion());

        // Check if editing
        if (getIntent().hasExtra("occasion_id")) {
            binding.tvHeader.setText("Edit Occasion");
            binding.etTitle.setText(getIntent().getStringExtra("occasion_title"));
            binding.etDate.setText(getIntent().getStringExtra("occasion_date"));
            binding.etRecipientName.setText(getIntent().getStringExtra("occasion_recipient"));
            binding.etRelation.setText(getIntent().getStringExtra("occasion_relation"));
            binding.btnSave.setText("Save Changes");
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            binding.etDate.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveOccasion() {
        String title = binding.etTitle.getText().toString().trim();
        String date = binding.etDate.getText().toString().trim();
        String recipient = binding.etRecipientName.getText().toString().trim();
        String relation = binding.etRelation.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty() || recipient.isEmpty()) {
            Toast.makeText(this, "Please fill in the required details", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSave.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        Occasion occasion = new Occasion();
        occasion.setUserId(SessionManager.getInstance(this).getUserId());
        occasion.setTitle(title);
        occasion.setTargetDate(date);
        occasion.setRecipientName(recipient);
        occasion.setRecipientRelation(relation);

        String occasionId = getIntent().getStringExtra("occasion_id");

        androidx.lifecycle.LiveData<NetworkResult<Void>> resultLiveData;
        if (occasionId != null) {
            resultLiveData = repository.updateOccasion(occasionId, occasion);
        } else {
            resultLiveData = repository.addOccasion(occasion);
        }

        resultLiveData.observe(this, result -> {
            if (result.status == NetworkResult.Status.SUCCESS) {
                Toast.makeText(this, "Occasion saved! We'll remind you.", Toast.LENGTH_SHORT).show();
                finish();
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.btnSave.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
