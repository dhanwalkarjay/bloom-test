package com.bloom.customer.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Data Model for an Order (orders table).
 */
public class Order {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("shop_id")
    private String shopId;

    @SerializedName("address_id")
    private String addressId;

    @SerializedName("bouquet_subtotal")
    private double bouquetSubtotal;

    @SerializedName("addons_subtotal")
    private double addonsSubtotal;

    @SerializedName("delivery_fee")
    private double deliveryFee;

    @SerializedName("platform_fee")
    private double platformFee;

    @SerializedName("tax_amount")
    private double taxAmount;

    @SerializedName("discount_amount")
    private double discountAmount;

    @SerializedName("total_amount")
    private double totalAmount;

    @SerializedName("status")
    private String status; // default 'placed'

    @SerializedName("payment_status")
    private String paymentStatus; // default 'pending'

    @SerializedName("delivery_slot")
    private String deliverySlot;

    @SerializedName("razorpay_order_id")
    private String razorpayOrderId;

    @SerializedName("razorpay_payment_id")
    private String razorpayPaymentId;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("is_anonymous")
    private boolean isAnonymous;

    @SerializedName("is_addressless")
    private boolean isAddressless;

    @SerializedName("recipient_name")
    private String recipientName;

    @SerializedName("recipient_phone")
    private String recipientPhone;

    @SerializedName("shops")
    private ShopInfo shop;

    @SerializedName("order_items")
    private List<OrderItem> items;

    @SerializedName("addresses")
    private Address address;

    @SerializedName("delivery_distance_km")
    private double deliveryDistanceKm;

    @SerializedName("commission_rate")
    private double commissionRate;

    @SerializedName("commission_amount")
    private double commissionAmount;

    @SerializedName("florist_earning")
    private double floristEarning;

    @SerializedName("payout_eligible_at")
    private String payoutEligibleAt;

    @SerializedName("delivery_otp")
    private String deliveryOtp;

    @SerializedName("rider_name")
    private String riderName;

    @SerializedName("rider_phone")
    private String riderPhone;

    @SerializedName("rider_lat")
    private double riderLat;

    @SerializedName("rider_lng")
    private double riderLng;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }

    public String getFloristId() { return shopId; }
    public void setFloristId(String floristId) { this.shopId = floristId; }

    public String getAddressId() { return addressId; }
    public void setAddressId(String addressId) { this.addressId = addressId; }

    public double getBouquetSubtotal() { return bouquetSubtotal; }
    public void setBouquetSubtotal(double bouquetSubtotal) { this.bouquetSubtotal = bouquetSubtotal; }

    public double getAddonsSubtotal() { return addonsSubtotal; }
    public void setAddonsSubtotal(double addonsSubtotal) { this.addonsSubtotal = addonsSubtotal; }

    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }

    public double getPlatformFee() { return platformFee; }
    public void setPlatformFee(double platformFee) { this.platformFee = platformFee; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getDeliverySlot() { return deliverySlot; }
    public void setDeliverySlot(String deliverySlot) { this.deliverySlot = deliverySlot; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public ShopInfo getShop() { return shop; }
    public void setShop(ShopInfo shop) { this.shop = shop; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public String getDeliveryOtp() { return deliveryOtp; }
    public void setDeliveryOtp(String deliveryOtp) { this.deliveryOtp = deliveryOtp; }

    public String getRiderName() { return riderName; }
    public void setRiderName(String riderName) { this.riderName = riderName; }

    public String getRiderPhone() { return riderPhone; }
    public void setRiderPhone(String riderPhone) { this.riderPhone = riderPhone; }

    public double getRiderLat() { return riderLat; }
    public void setRiderLat(double riderLat) { this.riderLat = riderLat; }

    public double getRiderLng() { return riderLng; }
    public void setRiderLng(double riderLng) {
        this.riderLng = riderLng;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public boolean isAddressless() {
        return isAddressless;
    }

    public void setAddressless(boolean addressless) {
        isAddressless = addressless;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    /**
     * Nested class to capture shop info from join.
     */
    public static class ShopInfo {
        @SerializedName("name")
        private String name;

        @SerializedName("latitude")
        private double latitude;

        @SerializedName("longitude")
        private double longitude;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
    }
}
