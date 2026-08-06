package com.bloom.customer.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for formatting currency values.
 * Standardizes the display of prices across the app.
 */
public class CurrencyFormatter {

    private static final Locale INDIA_LOCALE = new Locale("en", "IN");

    /**
     * Formats a double value as INR currency string.
     * Example: 500.0 -> ₹500.00
     */
    public static String format(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(INDIA_LOCALE);
        String result = formatter.format(amount);
        // Sometimes getCurrencyInstance returns INR instead of symbol, or has extra spaces.
        // Let's ensure it uses the symbol.
        if (result.startsWith("INR")) {
            result = result.replace("INR", "₹").trim();
        }
        return result;
    }

    /**
     * Formats a double value as INR currency string without decimals if whole.
     */
    public static String formatCompact(double amount) {
        if (amount == (long) amount) {
            return String.format("₹%d", (long) amount);
        } else {
            return String.format("₹%.2f", amount);
        }
    }
}
