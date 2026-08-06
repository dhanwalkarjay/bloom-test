package com.bloom.customer.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.content.ContextCompat;
import android.graphics.Color;

import com.bloom.R;
import com.bloom.databinding.ActivityDeliverySlotBinding;

public class DeliverySlotActivity extends AppCompatActivity {

    private ActivityDeliverySlotBinding binding;
    private CheckoutViewModel viewModel;
    private String selectedSlot = "SAME-DAY";
    private com.bloom.customer.data.model.Shop currentShop = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeliverySlotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);

        binding.btnBack.setOnClickListener(v -> finish());

        fetchShopInfo();
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

    private void fetchShopInfo() {
        String shopId = viewModel.getCartShopId();
        if (shopId != null) {
            viewModel.getShopById(shopId).observe(this, result -> {
                if (result.status == com.bloom.customer.util.NetworkResult.Status.SUCCESS) {
                    currentShop = result.data;
                }
            });
        }
    }

    private boolean isSameDayPossible() {
        if (currentShop == null) return true; // Optimistic fallback
        
        try {
            java.util.Calendar now = java.util.Calendar.getInstance();
            int currentMin = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE);
            
            String[] closeParts = currentShop.getClosesAt().split(":");
            int closeMin = Integer.parseInt(closeParts[0]) * 60 + Integer.parseInt(closeParts[1]);
            
            // If less than 2 hours before closing, same-day is too tight
            return (closeMin - currentMin) >= 120;
        } catch (Exception e) { return true; }
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
            
            if (isValidTime(hour, min)) {
                String ampm = hour >= 12 ? "PM" : "AM";
                int hour12 = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
                binding.tvSelectedTime.setText(String.format("%d:%02d %s", hour12, min, ampm));
            } else {
                Toast.makeText(this, "Please select a valid future time within shop hours", Toast.LENGTH_LONG).show();
            }
        });

        picker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

    private boolean isValidTime(int hour, int min) {
        java.util.Calendar selected = java.util.Calendar.getInstance();
        selected.set(java.util.Calendar.HOUR_OF_DAY, hour);
        selected.set(java.util.Calendar.MINUTE, min);

        // Same-day check: must be in future (e.g. 1 hour prep time)
        if ("SAME-DAY".equals(selectedSlot)) {
            java.util.Calendar prepLimit = java.util.Calendar.getInstance();
            prepLimit.add(java.util.Calendar.MINUTE, 60); 
            if (selected.before(prepLimit)) return false;
        }

        // Shop hours check
        int openH = 8, openM = 0;
        int closeH = 22, closeM = 0;

        if (currentShop != null) {
            try {
                if (currentShop.getOpensAt() != null) {
                    String[] parts = currentShop.getOpensAt().split(":");
                    openH = Integer.parseInt(parts[0]);
                    openM = Integer.parseInt(parts[1]);
                }
                if (currentShop.getClosesAt() != null) {
                    String[] parts = currentShop.getClosesAt().split(":");
                    closeH = Integer.parseInt(parts[0]);
                    closeM = Integer.parseInt(parts[1]);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        int selectedTotalMin = hour * 60 + min;
        int openTotalMin = openH * 60 + openM;
        int closeTotalMin = closeH * 60 + closeM;

        if (selectedTotalMin < openTotalMin || selectedTotalMin > closeTotalMin) {
            return false;
        }

        return true;
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
                
                // Task 3: Check if shop closing time is near for same-day
                if (!isSameDayPossible()) {
                    binding.btnContinue.setEnabled(false);
                    binding.btnContinue.setText("Same-day closed. Use Scheduled.");
                } else {
                    // Ensure a reasonable default time for same-day
                    java.util.Calendar deliveryTime = java.util.Calendar.getInstance();
                    deliveryTime.add(java.util.Calendar.HOUR_OF_DAY, 1); 
                    java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
                    binding.tvSelectedTime.setText(timeFormat.format(deliveryTime.getTime()));
                    
                    binding.btnContinue.setEnabled(true);
                    binding.btnContinue.setText("Confirm & Continue");
                }
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
