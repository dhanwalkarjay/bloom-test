package com.bloom.customer.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Data Model for a Shop (shops table).
 */
public class Shop {
    @SerializedName("id")
    private String id;

    @SerializedName("shop_name")
    private String shopName;

    @SerializedName("name")
    private String name;

    @SerializedName("rating")
    private double rating;

    @SerializedName("is_open")
    private boolean isOpen;

    @SerializedName("preparation_minutes")
    private int preparationMinutes;

    @SerializedName("prep_time")
    private String prepTime;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("opens_at")
    private String opensAt;

    @SerializedName("tier")
    private String tier;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("delivery_radius_km")
    private double deliveryRadiusKm;

    @SerializedName("closes_at")
    private String closesAt;

    // Note: location is geography in DB, returned distance via RPC
    @SerializedName("distance")
    private double distance;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() {
        if (name != null) return name;
        return shopName;
    }
    public void setName(String name) { this.name = name; this.shopName = name; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { isOpen = open; }

    public String getPrepTime() { return prepTime; }
    public void setPrepTime(String prepTime) { this.prepTime = prepTime; }

    public int getPreparationMinutes() { return preparationMinutes; }
    public void setPreparationMinutes(int preparationMinutes) { this.preparationMinutes = preparationMinutes; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOpensAt() { return opensAt; }
    public void setOpensAt(String opensAt) { this.opensAt = opensAt; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getDeliveryRadiusKm() { return deliveryRadiusKm; }
    public void setDeliveryRadiusKm(double deliveryRadiusKm) { this.deliveryRadiusKm = deliveryRadiusKm; }

    public String getClosesAt() { return closesAt; }
    public void setClosesAt(String closesAt) { this.closesAt = closesAt; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public String getFormattedDistance() {
        if (distance < 1000) {
            return (int) distance + " m";
        } else {
            return String.format("%.1f km", distance / 1000.0);
        }
    }
}
