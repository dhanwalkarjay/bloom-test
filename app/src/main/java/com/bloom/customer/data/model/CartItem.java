package com.bloom.customer.data.model;

/**
 * Data Model for an item in the shopping cart.
 * Plain local class (not mapped to a DB table).
 */
public class CartItem {
    private Product product;
    private int quantity;
    private String size; // Regular, Large
    private String cardMessage;

    public CartItem(Product product) {
        this.product = product;
        this.quantity = 1;
        this.size = "Regular";
    }

    // Getters and Setters
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getCardMessage() { return cardMessage; }
    public void setCardMessage(String cardMessage) { this.cardMessage = cardMessage; }

    private java.util.List<Addon> addons = new java.util.ArrayList<>();

    public java.util.List<Addon> getAddons() { return addons; }
    public void setAddons(java.util.List<Addon> addons) { this.addons = addons; }

    /**
     * Calculates the total price for this cart item.
     */
    public double getTotalPrice() {
        double unitPrice = product.getPrice();
        
        // Size pricing logic
        if ("Large".equals(size)) unitPrice *= 2.0;

        double addonsPrice = 0;
        if (addons != null) {
            for (Addon addon : addons) {
                addonsPrice += addon.getPrice();
            }
        }

        return (unitPrice + addonsPrice) * quantity;
    }
}
