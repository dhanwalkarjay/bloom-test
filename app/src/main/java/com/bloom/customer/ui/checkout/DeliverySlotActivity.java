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
    private com.bloom.customer.data.model.Shop shop;
    private View lastSelectedDateView = null;
    private int opensAtHour = 8;
    private int closesAtHour = 20;
    private int opensAtMin = 0;
    private int closesAtMin = 0;
    private int prepTimeMinutes = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeliverySlotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        // Parse shop details
        String shopJson = getIntent().getStringExtra("shop_json");
        if (shopJson != null) {
            shop = new com.google.gson.Gson().fromJson(shopJson, com.bloom.customer.data.model.Shop.class);
            if (shop != null) {
                parseShopTimes();
                prepTimeMinutes = shop.getPreparationMinutes() > 0 ? shop.getPreparationMinutes() : 60;
            }
        }

        setupTabs();
        setupPickers();
        
        binding.btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("address_id", getIntent().getStringExtra("address_id"));
            
            String finalSlot = selectedSlot;
            if ("SCHEDULED".equals(selectedSlot)) {
                String time = "";
                String ampm = "";
                if (binding.tvSelectedTime != null) time = binding.tvSelectedTime.getText().toString();
                if (binding.tvAmPm != null) ampm = binding.tvAmPm.getText().toString();
                
                String date = "";
                if (lastSelectedDateView != null) {
                    TextView tvDayName = lastSelectedDateView.findViewById(R.id.tvDayName);
                    TextView tvDayNum = lastSelectedDateView.findViewById(R.id.tvDayNum);
                    date = tvDayName.getText().toString() + " " + tvDayNum.getText().toString();
                }
                finalSlot = "SCHEDULED: " + date + " at " + time + " " + ampm;
            }
            
            intent.putExtra("delivery_slot", finalSlot);
            startActivity(intent);
        });

        binding.cardChangeTime.setOnClickListener(v -> showTimePicker());
    }

    private void parseShopTimes() {
        if (shop.getOpensAt() != null) {
            try {
                String[] parts = shop.getOpensAt().split(":");
                opensAtHour = Integer.parseInt(parts[0]);
                opensAtMin = Integer.parseInt(parts[1]);
            } catch (Exception e) { e.printStackTrace(); }
        }
        if (shop.getClosesAt() != null) {
            try {
                String[] parts = shop.getClosesAt().split(":");
                closesAtHour = Integer.parseInt(parts[0]);
                closesAtMin = Integer.parseInt(parts[1]);
            } catch (Exception e) { e.printStackTrace(); }
        }
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
            card.setStrokeWidth((int)dpToPx(1));
            card.setStrokeColor(Color.parseColor("#DEBFC1"));
            ((TextView)card.findViewById(R.id.tvDayName)).setTextColor(Color.parseColor("#574143"));
            ((TextView)card.findViewById(R.id.tvDayNum)).setTextColor(Color.parseColor("#24181A"));
        }

        com.google.android.material.card.MaterialCardView card = (com.google.android.material.card.MaterialCardView) view;
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.home_primary));
        card.setCardElevation(0f);
        card.setStrokeWidth(0);
        ((TextView)card.findViewById(R.id.tvDayName)).setTextColor(Color.parseColor("#E6FFFFFF"));
        ((TextView)card.findViewById(R.id.tvDayNum)).setTextColor(Color.WHITE);
        
        lastSelectedDateView = view;
    }

    private boolean isSameDayPossible() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        int currentMin = calendar.get(java.util.Calendar.MINUTE);
        
        int currentTotalMins = (currentHour * 60) + currentMin;
        int closesAtTotalMins = (closesAtHour * 60) + closesAtMin;
        
        // If current time + prep time is past closing time, no same day delivery
        if (currentTotalMins + prepTimeMinutes >= closesAtTotalMins) {
            return false;
        }
        return true;
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
            
            // Validate bounds
            int selectedMins = (hour * 60) + min;
            int openMins = (opensAtHour * 60) + opensAtMin;
            int closeMins = (closesAtHour * 60) + closesAtMin;
            
            if (selectedMins < openMins || selectedMins > closeMins) {
                String openTime = formatTime(opensAtHour, opensAtMin);
                String closeTime = formatTime(closesAtHour, closesAtMin);
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Invalid Time")
                        .setMessage("This shop is open from " + openTime + " to " + closeTime + ". Please select a time within operating hours.")
                        .setPositiveButton("OK", null)
                        .show();
                return; // Do not update UI
            }
            
            String ampm = hour >= 12 ? "PM" : "AM";
            int hour12 = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
            binding.tvSelectedTime.setText(String.format("%d:%02d", hour12, min));
            binding.tvAmPm.setText(ampm);
        });

        picker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

    private String formatTime(int hour, int min) {
        String ampm = hour >= 12 ? "PM" : "AM";
        int hour12 = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
        return String.format("%d:%02d %s", hour12, min, ampm);
    }

    private void setupTabs() {
        if (binding.cardSameDay == null) return;
        
        binding.cardSameDay.setOnClickListener(v -> {
            if (isSameDayPossible()) selectTab("SAME-DAY");
            else android.widget.Toast.makeText(this, "Same-Day Delivery cutoff (6 PM) has passed.", android.widget.Toast.LENGTH_SHORT).show();
        });
        binding.cardScheduled.setOnClickListener(v -> selectTab("SCHEDULED"));
        
        if (isSameDayPossible()) {
            selectTab("SAME-DAY");
        } else {
            selectTab("SCHEDULED");
            binding.cardSameDay.setAlpha(0.5f);
        }
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
                String openTime = formatTime(opensAtHour, opensAtMin);
                binding.btnContinue.setText("This shop is sleeping. Earliest delivery Tomorrow at " + openTime);
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
