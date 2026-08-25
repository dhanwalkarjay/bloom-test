package com.bloom.customer.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

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
        
        // Modern immersive mode
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            windowInsetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Prepare initial state for animation
        binding.logoContainer.setAlpha(0f);
        binding.logoContainer.setTranslationY(100f);
        binding.ivSplashBackground.setScaleX(1.0f);
        binding.ivSplashBackground.setScaleY(1.0f);

        // Animate background (Ken Burns zoom)
        binding.ivSplashBackground.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(2500)
                .start();

        // Animate logo (Fade in and float up)
        binding.logoContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(1200)
                .setStartDelay(300)
                .withEndAction(this::checkSession)
                .start();
    }

    private void checkSession() {
        SessionManager sessionManager = SessionManager.getInstance(this);
        if (sessionManager.isFirstLaunch()) {
            startActivity(new Intent(this, com.bloom.customer.ui.onboarding.OnboardingActivity.class));
        } else {
            // We always route to HomeActivity to support Guest Mode.
            // HomeActivity and its fragments will handle UI state based on SessionManager.isLoggedIn()
            startActivity(new Intent(this, HomeActivity.class));
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
