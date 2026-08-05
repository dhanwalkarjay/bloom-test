package com.bloom.customer.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.bloom.customer.util.Constants;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Singleton class to manage user sessions and tokens securely.
 * Pattern: Singleton Pattern - ensures only one instance of session management exists.
 * Principle: Single Responsibility - handles persistence of auth tokens only.
 * NFR-8: Auth tokens are stored using Android's EncryptedSharedPreferences.
 */
public class SessionManager {

    private static SessionManager instance;
    private SharedPreferences sharedPreferences;
    private final Context context;

    private SessionManager(Context context) {
        this.context = context.getApplicationContext();
        initPrefs();
    }

    private void initPrefs() {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    Constants.PREFS_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Could not create EncryptedSharedPreferences", e);
        }
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveSession(String accessToken, String refreshToken, String userId) {
        try {
            sharedPreferences.edit()
                    .putString(Constants.KEY_ACCESS_TOKEN, accessToken)
                    .putString(Constants.KEY_REFRESH_TOKEN, refreshToken)
                    .putString(Constants.KEY_USER_ID, userId)
                    .apply();
        } catch (Exception e) {
            forceClearPrefs();
            try {
                sharedPreferences.edit()
                        .putString(Constants.KEY_ACCESS_TOKEN, accessToken)
                        .putString(Constants.KEY_REFRESH_TOKEN, refreshToken)
                        .putString(Constants.KEY_USER_ID, userId)
                        .apply();
            } catch (Exception ignored) {}
        }
    }

    public String getAccessToken() {
        try {
            return sharedPreferences.getString(Constants.KEY_ACCESS_TOKEN, null);
        } catch (Exception e) {
            forceClearPrefs();
            return null;
        }
    }

    public String getRefreshToken() {
        try {
            return sharedPreferences.getString(Constants.KEY_REFRESH_TOKEN, null);
        } catch (Exception e) {
            forceClearPrefs();
            return null;
        }
    }

    public String getUserId() {
        try {
            return sharedPreferences.getString(Constants.KEY_USER_ID, null);
        } catch (Exception e) {
            forceClearPrefs();
            return null;
        }
    }

    public void clearSession() {
        try {
            sharedPreferences.edit().clear().apply();
        } catch (Exception e) {
            forceClearPrefs();
        }
    }

    private void forceClearPrefs() {
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
        initPrefs();
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null;
    }
}

