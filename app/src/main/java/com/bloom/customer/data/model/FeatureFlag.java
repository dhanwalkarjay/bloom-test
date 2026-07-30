package com.bloom.customer.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Data Model for Feature Flags (feature_flags table).
 */
public class FeatureFlag {
    @SerializedName("key")
    private String key;

    @SerializedName("enabled")
    private boolean enabled;

    @SerializedName("label")
    private String label;

    // Getters and Setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
