package com.bloom.customer.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.graphics.Color;

import com.bloom.R;
import com.bloom.databinding.ActivityDeliverySlotBinding;

public class DeliverySlotActivity extends AppCompatActivity {

    private ActivityDeliverySlotBinding binding;
    private String selectedSlot = "SAME-DAY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeliverySlotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        setupTabs();
        setupPickers();
        
        binding.btnContinue.setOnClickListener(v -> {
            if ("MIDNIGHT".equals(selectedSlot)) return;
            
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("address_id", getIntent().getStringExtra("address_id"));
            intent.putExtra("delivery_slot", selectedSlot);
            startActivity(intent);
        });
    }

    private void setupPickers() {
        binding.tvSelectedTime.setOnClickListener(v -> showTimePicker());
        
        generateMonthDates();
    }

    private void generateMonthDates() {
        binding.llDates.removeAllViews();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault());
        java.text.SimpleDateFormat fullFormat = new java.text.SimpleDateFormat("EEEE, MMMM d", java.util.Locale.getDefault());

        // Show next 30 days
        for (int i = 0; i < 30; i++) {
            final int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
            final String dayName = dayFormat.format(calendar.getTime()).toUpperCase();
            final String fullDate = fullFormat.format(calendar.getTime());

            View dateView = getLayoutInflater().inflate(R.layout.item_date_picker, binding.llDates, false);
            TextView tvDay = dateView.findViewById(R.id.tvDayName);
            TextView tvNum = dateView.findViewById(R.id.tvDayNum);

            tvDay.setText(dayName);
            tvNum.setText(String.valueOf(day));

            dateView.setOnClickListener(v -> {
                highlightDateView(dateView);
                binding.tvDeliverySubLabel.setText(fullDate);
            });

            binding.llDates.addView(dateView);
            
            // Default select today
            if (i == 0) {
                highlightDateView(dateView);
            }

            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
    }

    private View lastSelectedDateView = null;

    private void highlightDateView(View view) {
        if (lastSelectedDateView != null) {
            lastSelectedDateView.setBackgroundResource(0);
            lastSelectedDateView.setBackgroundColor(Color.TRANSPARENT);
            ((TextView)lastSelectedDateView.findViewById(R.id.tvDayName)).setTextColor(Color.parseColor("#999999"));
            ((TextView)lastSelectedDateView.findViewById(R.id.tvDayNum)).setTextColor(Color.parseColor("#333333"));
        }

        view.setBackgroundResource(R.drawable.bg_orders_segment_active);
        ((TextView)view.findViewById(R.id.tvDayName)).setTextColor(Color.parseColor("#FFCDD2"));
        ((TextView)view.findViewById(R.id.tvDayNum)).setTextColor(Color.WHITE);
        
        lastSelectedDateView = view;
    }

    private void showTimePicker() {
        com.google.android.material.timepicker.MaterialTimePicker picker =
                new com.google.android.material.timepicker.MaterialTimePicker.Builder()
                        .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_12H)
                        .setHour(12)
                        .setMinute(30)
                        .setTitleText("Select Delivery Time")
                        .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int hour = picker.getHour();
            int min = picker.getMinute();
            String ampm = hour >= 12 ? "PM" : "AM";
            int hour12 = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
            binding.tvSelectedTime.setText(String.format("%d:%02d %s", hour12, min, ampm));
        });

        picker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

    private void setupTabs() {
        binding.tabInstant.setOnClickListener(v -> selectTab("INSTANT"));
        binding.tabSameDay.setOnClickListener(v -> selectTab("SAME-DAY"));
        binding.tabScheduled.setOnClickListener(v -> selectTab("SCHEDULED"));
        binding.tabMidnight.setOnClickListener(v -> selectTab("MIDNIGHT"));
        
        // Initial state
        selectTab("SAME-DAY");
    }

    private void selectTab(String tab) {
        selectedSlot = tab;
        
        resetTabUI(binding.tabInstant);
        resetTabUI(binding.tabSameDay);
        resetTabUI(binding.tabScheduled);
        resetTabUI(binding.tabMidnight);
        
        java.util.Calendar now = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("EEEE, MMMM d", java.util.Locale.getDefault());
        String formattedDate = dateFormat.format(now.getTime()).toUpperCase();

        TextView selectedView;
        switch (tab) {
            case "INSTANT":
                selectedView = binding.tabInstant;
                binding.llInstantContent.setVisibility(View.VISIBLE);
                binding.llTimeSelection.setVisibility(View.GONE);
                binding.llMidnightContent.setVisibility(View.GONE);
                binding.llDateSelector.setVisibility(View.GONE);
                binding.btnContinue.setEnabled(true);
                binding.btnContinue.setText("Confirm & Continue");
                break;
            case "SCHEDULED":
                selectedView = binding.tabScheduled;
                binding.llInstantContent.setVisibility(View.GONE);
                binding.llTimeSelection.setVisibility(View.VISIBLE);
                binding.llMidnightContent.setVisibility(View.GONE);
                binding.llDateSelector.setVisibility(View.VISIBLE);
                binding.tvDeliveryMainLabel.setText("Scheduled Arrival");
                binding.tvDeliverySubLabel.setText("Pick a Date above");
                binding.btnContinue.setEnabled(true);
                binding.btnContinue.setText("Confirm & Continue");
                break;
            case "MIDNIGHT":
                selectedView = binding.tabMidnight;
                binding.llInstantContent.setVisibility(View.GONE);
                binding.llTimeSelection.setVisibility(View.GONE);
                binding.llMidnightContent.setVisibility(View.VISIBLE);
                binding.llDateSelector.setVisibility(View.GONE);
                binding.btnContinue.setEnabled(false);
                binding.btnContinue.setText("Coming Soon");
                break;
            default: // SAME-DAY
                selectedView = binding.tabSameDay;
                binding.llInstantContent.setVisibility(View.GONE);
                binding.llTimeSelection.setVisibility(View.VISIBLE);
                binding.llMidnightContent.setVisibility(View.GONE);
                binding.llDateSelector.setVisibility(View.GONE);
                binding.tvDeliveryMainLabel.setText("Today's Delivery");
                binding.tvDeliverySubLabel.setText(formattedDate);
                
                // Ensure a reasonable default time for same-day
                java.util.Calendar deliveryTime = java.util.Calendar.getInstance();
                deliveryTime.add(java.util.Calendar.HOUR_OF_DAY, 2); // At least 2 hours from now
                java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
                binding.tvSelectedTime.setText(timeFormat.format(deliveryTime.getTime()));
                
                binding.btnContinue.setEnabled(true);
                binding.btnContinue.setText("Confirm & Continue");
                break;
        }
        
        selectedView.setBackgroundResource(R.drawable.bg_orders_segment_active);
        selectedView.setTextColor(ContextCompat.getColor(this, R.color.orders_primary));
        selectedView.setElevation(4f);
    }

    private void resetTabUI(TextView tab) {
        tab.setBackgroundResource(android.R.color.transparent);
        tab.setTextColor(ContextCompat.getColor(this, R.color.orders_on_surface_variant));
        tab.setElevation(0f);
    }
}
