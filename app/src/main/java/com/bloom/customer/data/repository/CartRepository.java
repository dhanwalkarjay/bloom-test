package com.bloom.customer.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.model.CartItem;
import com.bloom.customer.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing the shopping cart locally.
 * Pattern: Repository Pattern - abstracts persistence logic.
 */
public class CartRepository {

    private static final String KEY_CART_ITEMS = "cart_items";
    private static final String KEY_CART_SHOP_ID = "cart_shop_id";

    private static CartRepository instance;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    private final MutableLiveData<List<CartItem>> cartLiveData = new MutableLiveData<>(new ArrayList<>());

    public static synchronized CartRepository getInstance(Context context) {
        if (instance == null) {
            instance = new CartRepository(context.getApplicationContext());
        }
        return instance;
    }

    private CartRepository(Context context) {
        this.sharedPreferences = context.getSharedPreferences("bloom_cart_prefs", Context.MODE_PRIVATE);
        this.gson = new Gson();
        loadCart();
    }

    public LiveData<List<CartItem>> getCartItems() {
        return cartLiveData;
    }

    public String getCartShopId() {
        return sharedPreferences.getString(KEY_CART_SHOP_ID, null);
    }

    private void loadCart() {
        try {
            String json = sharedPreferences.getString(KEY_CART_ITEMS, null);
            if (json != null) {
                Type type = new TypeToken<ArrayList<CartItem>>() {}.getType();
                List<CartItem> items = gson.fromJson(json, type);
                cartLiveData.setValue(items != null ? items : new ArrayList<>());
            } else {
                cartLiveData.setValue(new ArrayList<>());
            }
        } catch (Exception e) {
            timber.log.Timber.e(e, "Error converting cart to JSON");
            cartLiveData.setValue(new ArrayList<>());
        }
    }

    private void saveCart(List<CartItem> items, String shopId) {
        String json = gson.toJson(items);
        sharedPreferences.edit()
                .putString(KEY_CART_ITEMS, json)
                .putString(KEY_CART_SHOP_ID, shopId)
                .apply();
        cartLiveData.setValue(items);
    }

    public boolean addToCart(CartItem item) {
        List<CartItem> currentItems = cartLiveData.getValue();
        if (currentItems == null) currentItems = new ArrayList<>();

        String currentShopId = getCartShopId();
        String itemShopId = item.getProduct().getShopId();

        // Single Vendor Check
        if (currentShopId != null && !currentShopId.equals(itemShopId)) {
            // UI should show prompt to clear cart
            return false;
        }

        // Check if item already exists in cart (same product and size)
        boolean found = false;
        for (CartItem existingItem : currentItems) {
            if (existingItem.getProduct().getId().equals(item.getProduct().getId())) {
                existingItem.setQuantity(item.getQuantity());
                found = true;
                break;
            }
        }

        if (!found) {
            currentItems.add(item);
        }
        saveCart(currentItems, itemShopId);
        return true;
    }

    public void removeFromCart(int position) {
        List<CartItem> currentItems = cartLiveData.getValue();
        if (currentItems != null && position < currentItems.size()) {
            currentItems.remove(position);
            String shopId = currentItems.isEmpty() ? null : getCartShopId();
            saveCart(currentItems, shopId);
        }
    }

    public void removeFromCartByProductId(String productId) {
        List<CartItem> currentItems = cartLiveData.getValue();
        if (currentItems != null) {
            for (int i = 0; i < currentItems.size(); i++) {
                if (currentItems.get(i).getProduct().getId().equals(productId)) {
                    currentItems.remove(i);
                    break;
                }
            }
            String shopId = currentItems.isEmpty() ? null : getCartShopId();
            saveCart(currentItems, shopId);
        }
    }

    public void clearCart() {
        saveCart(new ArrayList<>(), null);
    }

    public void updateCart(List<CartItem> items) {
        String shopId = (items == null || items.isEmpty()) ? null : getCartShopId();
        saveCart(items, shopId);
    }

    public double getCartTotal() {
        List<CartItem> items = cartLiveData.getValue();
        double total = 0;
        if (items != null) {
            for (CartItem item : items) {
                total += item.getTotalPrice();
            }
        }
        return total;
    }

    public int getCartCount() {
        List<CartItem> items = cartLiveData.getValue();
        int count = 0;
        if (items != null) {
            for (CartItem item : items) {
                count += item.getQuantity();
            }
        }
        return count;
    }
}
