package com.bloom.customer.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Data Model for a Product search result with shop info and distance.
 */
public class ProductSearchResult {
    @SerializedName("product_id")
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

    @SerializedName("distance")
    private double distance;

    @SerializedName("shop_name")
    private String shopName;

    @SerializedName("is_shop_open")
    private boolean isShopOpen;

    // Getters
    public String getId() { return id; }
    public String getShopId() { return shopId; }
    public String getName() { return title; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }
    public boolean isLux() { return isLux; }
    public double getDistance() { return distance; }
    public String getShopName() { return shopName; }
    public boolean isShopOpen() { return isShopOpen; }

    // Helper to convert to Product
    public Product toProduct() {
        Product p = new Product();
        p.setId(id);
        p.setShopId(shopId);
        p.setName(title);
        p.setDescription(description);
        p.setPrice(price);
        p.setImageUrl(imageUrl);
        p.setCategory(category);
        p.setLux(isLux);
        return p;
    }
}
