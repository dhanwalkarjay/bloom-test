package com.bloom.customer.ui.orderhistory;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.bloom.customer.data.model.Order;
import com.bloom.customer.data.repository.OrderRepository;
import com.bloom.customer.util.NetworkResult;

import java.util.List;

public class OrderHistoryViewModel extends AndroidViewModel {

    private final OrderRepository repository;

    public OrderHistoryViewModel(@NonNull Application application) {
        super(application);
        this.repository = new OrderRepository(application);
    }

    public LiveData<NetworkResult<List<Order>>> getOrderHistory(String userId) {
        return repository.getOrderHistory(userId);
    }
}
