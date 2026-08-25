package com.bloom.customer;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bloom.customer.util.SystemBarInsets;
import com.bloom.BuildConfig;

/**
 * Main application class.
 * Principle: Single Responsibility - entry point for global app configuration.
 */
public class BloomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        if (BuildConfig.DEBUG) {
            timber.log.Timber.plant(new timber.log.Timber.DebugTree());
        } else {
            // Uncomment when Firebase Crashlytics is added
            // timber.log.Timber.plant(new CrashReportingTree());
        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                SystemBarInsets.apply(activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });
    }
}
