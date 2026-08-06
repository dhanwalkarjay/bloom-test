package com.bloom.customer.ui.cart;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.model.CartItem;
import com.bloom.customer.data.repository.CartRepository;

import java.util.List;

public class CartViewModel extends AndroidViewModel {

    private final CartRepository repository;

    public CartViewModel(@NonNull Application application) {
        super(application);
        this.repository = CartRepository.getInstance(application);
    }

    public LiveData<List<CartItem>> getCartItems() {
        return repository.getCartItems();
    }

    public void removeFromCart(int position) {
        repository.removeFromCart(position);
    }

    public void removeFromCart(String productId) {
        repository.removeFromCartByProductId(productId);
    }

    public void updateCart(List<CartItem> items) {
        repository.updateCart(items);
    }

    public void addToCart(CartItem item) {
        repository.addToCart(item);
    }

    public void clearCart() {
        repository.clearCart();
    }

    public double getCartTotal() {
        return repository.getCartTotal();
    }

    public String getCartShopId() {
        return repository.getCartShopId();
    }
}
