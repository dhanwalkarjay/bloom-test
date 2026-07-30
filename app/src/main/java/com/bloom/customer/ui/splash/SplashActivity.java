package com.bloom.customer.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.ui.auth.LoginActivity;
import com.bloom.customer.ui.home.HomeActivity;
import com.bloom.databinding.ActivitySplashBinding;

/**
 * Entry point Activity. Handles session routing.
 * Principle: Single Responsibility - determines initial navigation flow.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1500; // 1.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Handler(Looper.getMainLooper()).postDelayed(this::checkSession, SPLASH_DELAY);
    }

    private void checkSession() {
        // We always route to HomeActivity to support Guest Mode.
        // HomeActivity and its fragments will handle UI state based on SessionManager.isLoggedIn()
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
