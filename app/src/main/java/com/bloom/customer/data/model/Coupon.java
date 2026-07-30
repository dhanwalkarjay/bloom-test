package com.bloom.customer.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Data Model for a Coupon (coupons table).
 * Scoped per florist/shop.
 */
public class Coupon {
    @SerializedName("id")
    private String id;

    @SerializedName("florist_id")
    private String floristId;

    @SerializedName("code")
    private String code;

    @SerializedName("discount_type")
    private String discountType; // "percentage" or "flat"

    @SerializedName("value")
    private double value;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("end_date")
    private String endDate;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFloristId() { return floristId; }
    public void setFloristId(String floristId) { this.floristId = floristId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
