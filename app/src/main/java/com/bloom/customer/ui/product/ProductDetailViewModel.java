package com.bloom.customer.ui.product;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.bloom.customer.data.model.Addon;
import com.bloom.customer.data.model.CartItem;
import com.bloom.customer.data.repository.CartRepository;
import com.bloom.customer.data.repository.ProductRepository;
import com.bloom.customer.util.NetworkResult;

import java.util.List;

public class ProductDetailViewModel extends AndroidViewModel {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    public ProductDetailViewModel(@NonNull Application application) {
        super(application);
        this.productRepository = new ProductRepository(application);
        this.cartRepository = CartRepository.getInstance(application);
    }

    public LiveData<NetworkResult<List<Addon>>> getAddons() {
        return productRepository.getAddons();
    }

    public LiveData<List<CartItem>> getCartItems() {
        return cartRepository.getCartItems();
    }

    public void addToCart(CartItem item) {
        cartRepository.addToCart(item);
    }

    public void removeFromCart(String productId) {
        cartRepository.removeFromCartByProductId(productId);
    }

    public void clearCart() {
        cartRepository.clearCart();
    }

    public String getCartShopId() {
        return cartRepository.getCartShopId();
    }
}
