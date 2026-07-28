package com.bloom.customer.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility for date and time formatting.
 * Principle: Single Responsibility - date-related operations only.
 */
public class DateTimeUtil {

    private static final String DEFAULT_DATE_FORMAT = "dd MMM yyyy, hh:mm a";

    public static String formatToString(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }

    public static String formatRelative(long timestamp) {
        // Simple implementation for now
        return formatToString(new Date(timestamp));
    }
}
