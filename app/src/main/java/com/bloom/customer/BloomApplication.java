package com.bloom.customer;

import android.app.Application;

/**
 * Main application class.
 * Principle: Single Responsibility - entry point for global app configuration.
 */
public class BloomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Global initializations can go here
    }
}
