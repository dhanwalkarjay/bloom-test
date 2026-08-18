package com.bloom.customer.data.model;

import com.google.gson.annotations.SerializedName;

public class ShopInventoryItem {
    @SerializedName("id")
    private String id;

    @SerializedName("shop_id")
    private String shopId;

    @SerializedName("type")
    private String type; // STEM, FILLER, WRAPPER, VASE

    @SerializedName("name")
    private String name;

    @SerializedName("price_per_unit")
    private double pricePerUnit;

    @SerializedName("stock_quantity")
    private int stockQuantity;

    @SerializedName("color_hex")
    private String colorHex;

    @SerializedName("model_url")
    private String modelUrl;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public String getModelUrl() { return modelUrl; }
    public void setModelUrl(String modelUrl) { this.modelUrl = modelUrl; }
}
