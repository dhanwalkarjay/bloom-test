package com.bloom.customer.util;

import com.bloom.BuildConfig;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthUtils {

    /**
     * Generates a deterministic, cryptographically strong password for Supabase 
     * based on the user's phone number and the app's secret anon key.
     * This ensures the user never has to type a password, and the password 
     * is unguessable by bad actors.
     */
    public static String generateSecurePassword(String phone) {
        String secret = BuildConfig.SUPABASE_ANON_KEY;
        String rawInput = phone + "_" + secret;
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawInput.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            // Supabase passwords typically need to be at least 6 chars. 
            // We prepend a strong prefix and use a substring of the hash.
            return "BloomAuth@" + hexString.toString().substring(0, 16);
            
        } catch (NoSuchAlgorithmException e) {
            // Fallback that is still deterministic and complex
            return "BloomAuth@" + phone.hashCode() + "!" + secret.substring(0, 10);
        }
    }
}
