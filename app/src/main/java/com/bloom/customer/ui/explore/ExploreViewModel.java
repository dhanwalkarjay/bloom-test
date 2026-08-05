package com.bloom.customer.ui.explore;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.bloom.customer.data.model.ProductSearchResult;
import com.bloom.customer.data.repository.ProductRepository;
import com.bloom.customer.util.NetworkResult;

import java.util.List;

public class ExploreViewModel extends AndroidViewModel {

    private final ProductRepository repository;

    public ExploreViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ProductRepository(application);
    }

    public LiveData<NetworkResult<List<ProductSearchResult>>> searchProducts(double lat, double lng, String query, String category) {
        return repository.searchProductsNearby(lat, lng, query, category);
    }
}
