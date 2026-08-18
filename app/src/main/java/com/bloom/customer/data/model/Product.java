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

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private double price;

    @SerializedName("images")
    private String imageUrl;

    @SerializedName("category")
    private String category;

    @SerializedName("is_lux")
    private boolean isLux;

    @SerializedName("occasion_tags")
    private String[] occasionTags;

    @SerializedName("is_bestseller")
    private boolean isBestseller;

    @SerializedName("stock_count")
    private int stockCount;

    @SerializedName("created_at")
    private String createdAt;
    
    // Transient field for local cart quantity tracking in UI
    private int cartQuantity = 0;
    public int getCartQuantity() { return cartQuantity; }
    public void setCartQuantity(int cartQuantity) { this.cartQuantity = cartQuantity; }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }

    // Keep backward compatibility for code using florist_id
    public String getFloristId() { return shopId; }
    public void setFloristId(String floristId) { this.shopId = floristId; }

    public String getName() { return title; }
    public void setName(String name) { this.title = name; }

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

    public String[] getOccasionTags() { return occasionTags; }
    public void setOccasionTags(String[] occasionTags) { this.occasionTags = occasionTags; }

    public boolean isBestseller() { return isBestseller; }
    public void setBestseller(boolean bestseller) { isBestseller = bestseller; }

    public int getStockCount() { return stockCount; }
    public void setStockCount(int stockCount) { this.stockCount = stockCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
