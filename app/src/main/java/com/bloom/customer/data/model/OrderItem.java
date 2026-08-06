package com.bloom.customer.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Data Model for an Order Item (order_items table).
 */
public class OrderItem {
    @SerializedName("id")
    private String id;

    @SerializedName("order_id")
    private String orderId;

    @SerializedName("product_id")
    private String productId;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("unit_price")
    private double unitPrice;

    @SerializedName("size")
    private String size; // default 'Regular'

    @SerializedName("card_message")
    private String cardMessage;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getCardMessage() { return cardMessage; }
    public void setCardMessage(String cardMessage) { this.cardMessage = cardMessage; }

    @SerializedName("products")
    private ProductInfo product;

    public ProductInfo getProduct() { return product; }
    public void setProduct(ProductInfo product) { this.product = product; }

    public static class ProductInfo {
        @SerializedName("title")
        private String title;

        @SerializedName("images")
        private String images;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getImages() { return images; }
        public void setImages(String images) { this.images = images; }
    }
}
