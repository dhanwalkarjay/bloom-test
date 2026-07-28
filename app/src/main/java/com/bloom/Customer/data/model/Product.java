package com.bloom.customer.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Data Model for a Product (products table).
 */
public class Product {
    @SerializedName("id")
    private String id;

    @SerializedName("shop_id")
    private String shopId;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private double price;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("category")
    private String category;

    @SerializedName("is_lux")
    private boolean isLux;

    @SerializedName("created_at")
    private String createdAt;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isLux() { return isLux; }
    public void setLux(boolean lux) { isLux = lux; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
