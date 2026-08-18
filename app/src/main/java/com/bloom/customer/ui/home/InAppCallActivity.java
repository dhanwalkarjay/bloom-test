package com.bloom.customer.ui.home;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.R;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class InAppCallActivity extends AppCompatActivity {

    private boolean isMuted = false;
    private boolean isSpeaker = false;
    private int callDurationSeconds = 0;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        setContentView(R.layout.activity_in_app_call);

        String shopName = getIntent().getStringExtra("shop_name");
        String shopImage = getIntent().getStringExtra("shop_image");

        TextView tvShopName = findViewById(R.id.tvShopName);
        ImageView ivShopProfile = findViewById(R.id.ivShopProfile);
        TextView tvStatus = findViewById(R.id.tvStatus);
        
        if (shopName != null) tvShopName.setText(shopName);
        if (shopImage != null) {
            Glide.with(this).load(shopImage).into(ivShopProfile);
        }

        View pulseRing1 = findViewById(R.id.pulseRing1);
        View pulseRing2 = findViewById(R.id.pulseRing2);

        startPulseAnimation(pulseRing1, 0);
        startPulseAnimation(pulseRing2, 500);

        // Simulate connecting after 3 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvStatus.setText("00:00");
            pulseRing1.setVisibility(View.GONE);
            pulseRing2.setVisibility(View.GONE);
            startCallTimer(tvStatus);
        }, 3000);

        findViewById(R.id.btnEndCall).setOnClickListener(v -> finish());

        FloatingActionButton btnMute = findViewById(R.id.btnMute);
        btnMute.setOnClickListener(v -> {
            isMuted = !isMuted;
            btnMute.setImageAlpha(isMuted ? 128 : 255);
        });

        FloatingActionButton btnSpeaker = findViewById(R.id.btnSpeaker);
        btnSpeaker.setOnClickListener(v -> {
            isSpeaker = !isSpeaker;
            btnSpeaker.setImageAlpha(isSpeaker ? 255 : 128);
        });
        btnSpeaker.setImageAlpha(128);
    }

    private void startPulseAnimation(View view, int startDelay) {
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.5f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.5f),
                PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f)
        );
        scaleDown.setDuration(1500);
        scaleDown.setStartDelay(startDelay);
        scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
        scaleDown.start();
    }

    private void startCallTimer(TextView tvStatus) {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                callDurationSeconds++;
                int minutes = callDurationSeconds / 60;
                int seconds = callDurationSeconds % 60;
                tvStatus.setText(String.format("%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}
