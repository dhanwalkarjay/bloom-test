package com.bloom.customer.ui.shop;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.model.Product;
import com.bloom.customer.data.repository.ProductRepository;
import com.bloom.customer.util.NetworkResult;

import java.util.List;

/**
 * ViewModel for Shop Detail screen.
 * Principle: Single Responsibility - handles logic for shop products.
 */
public class ShopDetailViewModel extends AndroidViewModel {

    private final ProductRepository productRepository;
    private final MutableLiveData<NetworkResult<List<Product>>> products = new MutableLiveData<>();

    public ShopDetailViewModel(@NonNull Application application) {
        super(application);
        this.productRepository = new ProductRepository(application);
    }

    public LiveData<NetworkResult<List<Product>>> getProducts() {
        return products;
    }

    public void fetchProducts(String shopId) {
        productRepository.getProductsByShop(shopId).observeForever(result -> {
            products.setValue(result);
        });
    }
}
