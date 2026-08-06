package com.bloom.customer.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Data Model for a User (auth.users mapping).
 * Top-level model as per planned structure.
 */
public class User {
    @SerializedName("id")
    private String id;

    @SerializedName("phone")
    private String phone;

    @SerializedName("email")
    private String email;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
