package com.bloom.customer.util;

import android.util.Patterns;

/**
 * Utility for input validation.
 * Principle: Single Responsibility - validation logic only.
 */
public class ValidationUtil {

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        // Basic phone validation for 10-digit numbers
        return phone != null && phone.length() >= 10 && Patterns.PHONE.matcher(phone).matches();
    }

    public static boolean isValidPassword(String password) {
        // At least 6 characters
        return password != null && password.length() >= 6;
    }
}
