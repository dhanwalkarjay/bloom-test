package com.bloom.customer.util;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Pattern;

/**
 * Utility class for centralized input validation.
 */
public class InputValidator {

    // Example constraints:
    // Password must be at least 8 characters, contain one uppercase, one lowercase, one number
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$");

    // Phone must be exactly 10 digits
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{10}$");

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static boolean isValidPhone(CharSequence target) {
        return (!TextUtils.isEmpty(target) && PHONE_PATTERN.matcher(target).matches());
    }

    public static boolean isValidPassword(CharSequence target) {
        return (!TextUtils.isEmpty(target) && PASSWORD_PATTERN.matcher(target).matches());
    }

    public static boolean isValidName(CharSequence target) {
        return !TextUtils.isEmpty(target) && target.length() >= 2;
    }
}
