package com.bloom.customer.util;

import com.bloom.BuildConfig;

/**
 * Constants used throughout the app.
 * Principle: Single Responsibility - centralizing all configuration keys.
 * API keys are injected via BuildConfig from local.properties (git-ignored).
 */
public final class Constants {

    private Constants() {
        // Private constructor to prevent instantiation
    }

    // Supabase Configuration (injected from local.properties via BuildConfig)
    public static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    public static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;
    // Razorpay Configuration (KEY_ID is publishable — safe client-side)
    public static final String RAZORPAY_KEY_ID = BuildConfig.RAZORPAY_KEY_ID;
    // API Endpoints
    public static final String AUTH_ENDPOINT = "auth/v1/";
    public static final String REST_ENDPOINT = "rest/v1/";
    public static final String FUNCTIONS_ENDPOINT = "functions/v1/";
    
    // Custom Backend URL (Update this to your Railway URL once deployed)
    public static final String BACKEND_URL = "http://localhost:8080";
    
    // Shared Preferences Keys
    public static final String PREFS_NAME = "bloom_secure_prefs";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_USER_ID = "user_id";

    // Feature Flags (Driven by DB)
    public static final String FLAG_MIDNIGHT_DELIVERY = "midnight_delivery";
    public static final String FLAG_CREATE_YOUR_OWN = "create_your_own";
}
