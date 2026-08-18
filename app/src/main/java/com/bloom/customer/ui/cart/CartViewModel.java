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
    private final com.bloom.customer.data.repository.ProductRepository productRepository;

    public CartViewModel(@NonNull Application application) {
        super(application);
        this.repository = CartRepository.getInstance(application);
        this.productRepository = new com.bloom.customer.data.repository.ProductRepository(application);
    }

    public LiveData<com.bloom.customer.util.NetworkResult<List<com.bloom.customer.data.model.Product>>> getProductsByShop(String shopId) {
        return productRepository.getProductsByShop(shopId);
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

    public boolean addToCart(CartItem item) {
        return repository.addToCart(item);
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
