package com.bloom.customer.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bloom.R;
import com.bloom.databinding.ActivityDeliverySlotBinding;

public class DeliverySlotActivity extends AppCompatActivity {

    private ActivityDeliverySlotBinding binding;
    private String selectedSlot = "SAME-DAY";
    private View lastSelectedDateView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeliverySlotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        setupTabs();
        setupPickers();
        
        binding.btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("address_id", getIntent().getStringExtra("address_id"));
            intent.putExtra("delivery_slot", selectedSlot);
            startActivity(intent);
        });

        binding.cardChangeTime.setOnClickListener(v -> showTimePicker());
    }

    private void setupPickers() {
        // Horizontal edge-to-edge Date Picker
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("dd", java.util.Locale.getDefault());
        java.text.SimpleDateFormat nameFormat = new java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            View dateView = getLayoutInflater().inflate(R.layout.item_date_picker, binding.llDates, false);
            
            TextView tvDay = dateView.findViewById(R.id.tvDayName);
            TextView tvNum = dateView.findViewById(R.id.tvDayNum);
            
            String day = dayFormat.format(calendar.getTime());
            String dayName = nameFormat.format(calendar.getTime()).toUpperCase();
            
            tvDay.setText(dayName);
            tvNum.setText(day);

            dateView.setOnClickListener(v -> {
                highlightDateView(dateView);
            });

            binding.llDates.addView(dateView);
            
            if (i == 0) {
                highlightDateView(dateView);
            }
            
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void highlightDateView(View view) {
        if (lastSelectedDateView != null) {
            com.google.android.material.card.MaterialCardView card = (com.google.android.material.card.MaterialCardView) lastSelectedDateView;
            card.setCardBackgroundColor(Color.TRANSPARENT);
            card.setCardElevation(0f);
            ((TextView)card.findViewById(R.id.tvDayName)).setTextColor(Color.parseColor("#AAAAAA"));
            ((TextView)card.findViewById(R.id.tvDayNum)).setTextColor(ContextCompat.getColor(this, R.color.home_lux_dark));
        }

        com.google.android.material.card.MaterialCardView card = (com.google.android.material.card.MaterialCardView) view;
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.home_primary));
        card.setCardElevation(dpToPx(8));
        ((TextView)card.findViewById(R.id.tvDayName)).setTextColor(Color.parseColor("#E6FFFFFF"));
        ((TextView)card.findViewById(R.id.tvDayNum)).setTextColor(Color.WHITE);
        
        lastSelectedDateView = view;
    }

    private boolean isSameDayPossible() {
        return true; // Simplified for UI branch
    }

    private void showTimePicker() {
        com.google.android.material.timepicker.MaterialTimePicker picker =
                new com.google.android.material.timepicker.MaterialTimePicker.Builder()
                        .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_12H)
                        .setHour(12)
                        .setMinute(0)
                        .setTitleText("Select Delivery Time")
                        .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int hour = picker.getHour();
            int min = picker.getMinute();
            
            String ampm = hour >= 12 ? "PM" : "AM";
            int hour12 = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
            binding.tvSelectedTime.setText(String.format("%d:%02d", hour12, min));
            binding.tvAmPm.setText(ampm);
        });

        picker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

    private void setupTabs() {
        if (binding.cardSameDay == null) return;
        
        binding.cardSameDay.setOnClickListener(v -> selectTab("SAME-DAY"));
        binding.cardScheduled.setOnClickListener(v -> selectTab("SCHEDULED"));
        
        selectTab("SAME-DAY");
    }

    private void selectTab(String tab) {
        selectedSlot = tab;
        
        if (binding.cardSameDay == null) return;
        
        resetCardUI(binding.cardSameDay);
        resetCardUI(binding.cardScheduled);

        com.google.android.material.card.MaterialCardView selectedView;
        
        if ("SCHEDULED".equals(tab)) {
            selectedView = binding.cardScheduled;
            binding.llDates.setVisibility(View.VISIBLE);
            binding.tvDeliveryMainLabel.setText("Scheduled Arrival");
            binding.btnContinue.setEnabled(true);
            binding.btnContinue.setText("Confirm Delivery");
        } else {
            selectedView = binding.cardSameDay;
            binding.tvDeliveryMainLabel.setText("Today's Delivery");
            
            if (!isSameDayPossible()) {
                binding.btnContinue.setEnabled(false);
                binding.btnContinue.setText("Same-day closed");
            } else {
                binding.btnContinue.setEnabled(true);
                binding.btnContinue.setText("Confirm Delivery");
            }
        }
        
        selectedView.setCardElevation(dpToPx(6));
        selectedView.setStrokeWidth((int)dpToPx(2));
        selectedView.setStrokeColor(ContextCompat.getColor(this, R.color.home_primary));
        
        LinearLayout ll = (LinearLayout) selectedView.getChildAt(0);
        ((android.widget.ImageView) ll.getChildAt(0)).setColorFilter(ContextCompat.getColor(this, R.color.home_primary));
        ((TextView) ll.getChildAt(1)).setTextColor(ContextCompat.getColor(this, R.color.home_primary));
    }

    private void resetCardUI(com.google.android.material.card.MaterialCardView card) {
        card.setCardElevation(dpToPx(2));
        card.setStrokeWidth(0);
        
        LinearLayout ll = (LinearLayout) card.getChildAt(0);
        ((android.widget.ImageView) ll.getChildAt(0)).setColorFilter(Color.parseColor("#AAAAAA"));
        ((TextView) ll.getChildAt(1)).setTextColor(Color.parseColor("#999999"));
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
