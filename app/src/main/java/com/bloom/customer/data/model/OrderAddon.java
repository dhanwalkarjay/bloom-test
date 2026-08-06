package com.bloom.customer.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Data Model for Order Addons (order_addons table).
 * Links an order/item with specific selected addons.
 */
public class OrderAddon {
    @SerializedName("id")
    private String id;

    @SerializedName("order_id")
    private String orderId;

    @SerializedName("addon_id")
    private String addonId;

    @SerializedName("price")
    private double price;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getAddonId() { return addonId; }
    public void setAddonId(String addonId) { this.addonId = addonId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
