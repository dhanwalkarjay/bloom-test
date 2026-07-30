package com.bloom.customer.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Data Model for an Addon (addons table).
 * Global scope, not scoped per shop.
 */
public class Addon {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private double price;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("type")
    private String type;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
