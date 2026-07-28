package com.bloom.customer.util;

/**
 * Constants used throughout the app.
 * Principle: Single Responsibility - centralizing all configuration keys.
 */
public final class Constants {

    // TEST number and otp
    // 18005550123=789012
    // 919876543210=123456

    private Constants() {
        // Private constructor to prevent instantiation
    }

    // Supabase Configuration
    public static final String SUPABASE_URL = "https://ddelmbcqxuwminhjyemz.supabase.co";
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRkZWxtYmNxeHV3bWluaGp5ZW16Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODUyMjUwMzUsImV4cCI6MjEwMDgwMTAzNX0.deso9CqFu1n1tS6k_zc7qGwk3RSjOgA0nOVnFM4gcWo";
    // Razorpay Configuration
    public static final String RAZORPAY_KEY_ID = "rzp_test_TIsA2hNZPtgwQT";
    public static final String RAZORPAY_KEY_SECRET = "Tfovoe6CT4JDz753fNjVxFO5";
    // API Endpoints
    public static final String AUTH_ENDPOINT = "auth/v1/";
    public static final String REST_ENDPOINT = "rest/v1/";
    
    // Shared Preferences Keys
    public static final String PREFS_NAME = "bloom_secure_prefs";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_USER_ID = "user_id";

    // Feature Flags (Driven by DB)
    public static final String FLAG_MIDNIGHT_DELIVERY = "midnight_delivery";
    public static final String FLAG_CREATE_YOUR_OWN = "create_your_own";
}
