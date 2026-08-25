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
import androidx.recyclerview.widget.GridLayoutManager;

import com.bloom.R;
import com.bloom.databinding.ActivityDeliverySlotBinding;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DeliverySlotActivity extends AppCompatActivity {

    private ActivityDeliverySlotBinding binding;
    private String selectedSlotType = "SAME-DAY";
    private String selectedTimeSlot = "";
    private String selectedDate = "";
    private com.bloom.customer.data.model.Shop shop;
    private View lastSelectedDateView = null;
    private int opensAtHour = 8;
    private int closesAtHour = 20;
    private int opensAtMin = 0;
    private int closesAtMin = 0;
    private int prepTimeMinutes = 60;
    private TimeSlotAdapter timeSlotAdapter;

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

        setupRecyclerView();
        setupTabs();
        setupPickers();
        
        binding.btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("address_id", getIntent().getStringExtra("address_id"));
            
            String finalSlot = "";
            if ("SCHEDULED".equals(selectedSlotType)) {
                finalSlot = "SCHEDULED: " + selectedDate + " at " + selectedTimeSlot;
            } else {
                finalSlot = "SAME-DAY: " + selectedTimeSlot;
            }
            
            intent.putExtra("delivery_slot", finalSlot);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        timeSlotAdapter = new TimeSlotAdapter(slot -> selectedTimeSlot = slot);
        binding.rvTimeSlots.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvTimeSlots.setAdapter(timeSlotAdapter);
    }

    private void parseShopTimes() {
        if (shop.getOpensAt() != null) {
            try {
                String[] parts = shop.getOpensAt().split(":");
                opensAtHour = Integer.parseInt(parts[0]);
                opensAtMin = Integer.parseInt(parts[1]);
            } catch (Exception e) { timber.log.Timber.e(e, "Error finding Instant slot"); }
        }
        if (shop.getClosesAt() != null) {
            try {
                String[] parts = shop.getClosesAt().split(":");
                closesAtHour = Integer.parseInt(parts[0]);
                closesAtMin = Integer.parseInt(parts[1]);
            } catch (Exception e) { timber.log.Timber.e(e, "Error finding closes slot"); }
        }
    }

    private void setupPickers() {
        // Horizontal edge-to-edge Date Picker
        Calendar calendar = Calendar.getInstance();
        java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("dd", java.util.Locale.getDefault());
        java.text.SimpleDateFormat nameFormat = new java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault());

        for (int i = 0; i < 14; i++) {
            // Mock Closed Days (e.g. Sunday)
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                continue;
            }

            View dateView = getLayoutInflater().inflate(R.layout.item_date_pill, binding.llDates, false);
            
            TextView tvDay = dateView.findViewById(R.id.tvDayName);
            TextView tvNum = dateView.findViewById(R.id.tvDayNum);
            
            String day = dayFormat.format(calendar.getTime());
            String dayName = nameFormat.format(calendar.getTime()).toUpperCase();
            
            tvDay.setText(dayName);
            tvNum.setText(day);

            String fullDateStr = dayName + " " + day;

            dateView.setOnClickListener(v -> {
                highlightDateView(dateView);
                selectedDate = fullDateStr;
                generateTimeSlots(false); // Scheduled
            });

            binding.llDates.addView(dateView);
            
            if (i == 0) {
                highlightDateView(dateView);
                selectedDate = fullDateStr;
            }
            
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void highlightDateView(View view) {
        if (lastSelectedDateView != null) {
            MaterialCardView card = (MaterialCardView) lastSelectedDateView;
            card.setCardBackgroundColor(com.google.android.material.color.MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, Color.WHITE));
            card.setStrokeWidth((int)dpToPx(1));
            card.setStrokeColor(com.google.android.material.color.MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutline, Color.LTGRAY));
            ((TextView)card.findViewById(R.id.tvDayName)).setTextColor(com.google.android.material.color.MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY));
            ((TextView)card.findViewById(R.id.tvDayNum)).setTextColor(com.google.android.material.color.MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, Color.BLACK));
        }

        MaterialCardView card = (MaterialCardView) view;
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.home_primary));
        card.setStrokeWidth(0);
        ((TextView)card.findViewById(R.id.tvDayName)).setTextColor(Color.WHITE);
        ((TextView)card.findViewById(R.id.tvDayNum)).setTextColor(Color.WHITE);
        
        lastSelectedDateView = view;
    }

    private boolean isSameDayPossible() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMin = calendar.get(Calendar.MINUTE);
        
        int currentTotalMins = (currentHour * 60) + currentMin;
        int closesAtTotalMins = (closesAtHour * 60) + closesAtMin;
        
        // If current time + prep time is past closing time, no same day delivery
        if (currentTotalMins + prepTimeMinutes >= closesAtTotalMins) {
            return false;
        }
        return true;
    }

    private void generateTimeSlots(boolean isSameDay) {
        List<String> slots = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int currentTotalMins = (calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE);
        
        int slotStartMins = (opensAtHour * 60) + opensAtMin;
        int closeMins = (closesAtHour * 60) + closesAtMin;

        if (isSameDay) {
            if (currentTotalMins >= slotStartMins && currentTotalMins < closeMins) {
                slots.add("Priority ASAP");
            }
            slotStartMins = Math.max(slotStartMins, currentTotalMins + prepTimeMinutes);
            // Round to next 30 min block
            slotStartMins = (int) (Math.ceil(slotStartMins / 30.0) * 30);
        }

        while (slotStartMins + 120 <= closeMins) {
            String startStr = formatTime(slotStartMins / 60, slotStartMins % 60);
            String endStr = formatTime((slotStartMins + 120) / 60, (slotStartMins + 120) % 60);
            slots.add(startStr + " - " + endStr);
            slotStartMins += 120; // 2 hour windows
        }

        if (slots.isEmpty()) {
            binding.btnContinue.setEnabled(false);
            binding.btnContinue.setText("No slots available");
        } else {
            binding.btnContinue.setEnabled(true);
            binding.btnContinue.setText("Confirm Delivery");
        }

        timeSlotAdapter.setSlots(slots);
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
            else Toast.makeText(this, "Same-Day Delivery cutoff has passed.", Toast.LENGTH_SHORT).show();
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
        selectedSlotType = tab;
        
        if (binding.cardSameDay == null) return;
        
        resetCardUI(binding.cardSameDay);
        resetCardUI(binding.cardScheduled);

        MaterialCardView selectedView;
        
        if ("SCHEDULED".equals(tab)) {
            selectedView = binding.cardScheduled;
            binding.llDates.setVisibility(View.VISIBLE);
            binding.tvDeliveryMainLabel.setText("Scheduled Arrival");
            generateTimeSlots(false);
        } else {
            selectedView = binding.cardSameDay;
            binding.llDates.setVisibility(View.GONE);
            binding.tvDeliveryMainLabel.setText("Today's Delivery");
            generateTimeSlots(true);
        }
        
        selectedView.setCardElevation(dpToPx(6));
        selectedView.setStrokeWidth((int)dpToPx(2));
        selectedView.setStrokeColor(ContextCompat.getColor(this, R.color.home_primary));
        
        LinearLayout ll = (LinearLayout) selectedView.getChildAt(0);
        ((android.widget.ImageView) ll.getChildAt(0)).setColorFilter(ContextCompat.getColor(this, R.color.home_primary));
        ((TextView) ll.getChildAt(1)).setTextColor(ContextCompat.getColor(this, R.color.home_primary));
    }

    private void resetCardUI(MaterialCardView card) {
        card.setCardElevation(dpToPx(2));
        card.setStrokeWidth(0);
        
        LinearLayout ll = (LinearLayout) card.getChildAt(0);
        ((android.widget.ImageView) ll.getChildAt(0)).setColorFilter(ContextCompat.getColor(this, R.color.home_on_surface_variant));
        ((TextView) ll.getChildAt(1)).setTextColor(ContextCompat.getColor(this, R.color.home_on_surface_variant));
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
