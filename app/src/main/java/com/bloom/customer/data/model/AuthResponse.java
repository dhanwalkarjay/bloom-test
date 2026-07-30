package com.bloom.customer.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Data Model for Supabase Auth Response.
 * Wraps tokens and a nested user object.
 */
public class AuthResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("user")
    private User user;

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public User getUser() { return user; }

    /**
     * Nested User object returned by Supabase Auth.
     */
    public static class User {
        @SerializedName("id")
        private String id;

        @SerializedName("phone")
        private String phone;

        public String getId() { return id; }
        public String getPhone() { return phone; }
    }
}
