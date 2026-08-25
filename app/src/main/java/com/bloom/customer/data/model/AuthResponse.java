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
    
    @SerializedName("token")
    private String token;

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public User getUser() { return user; }
    public String getToken() { return token != null ? token : accessToken; }
}
