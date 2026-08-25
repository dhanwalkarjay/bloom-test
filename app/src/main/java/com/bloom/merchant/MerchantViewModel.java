package com.bloom.merchant;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.bloom.customer.data.model.Order;
import com.bloom.customer.util.NetworkResult;
import com.bloom.merchant.data.MerchantOrderRepository;

import java.util.List;

public class MerchantViewModel extends AndroidViewModel {

    private final MerchantOrderRepository orderRepository;

    public MerchantViewModel(@NonNull Application application) {
        super(application);
        this.orderRepository = new MerchantOrderRepository(application);
    }

    public LiveData<NetworkResult<List<Order>>> fetchOrders() {
        return orderRepository.fetchOrdersForMerchant();
    }

    public String getShopIdCache() {
        return orderRepository.getShopIdCache();
    }

    public LiveData<NetworkResult<Void>> updateOrderStatus(String orderId, String newStatus) {
        return orderRepository.updateOrderStatus(orderId, newStatus);
    }

    public LiveData<NetworkResult<List<com.bloom.customer.data.model.ShopInventoryItem>>> fetchInventory() {
        return orderRepository.fetchInventoryForMerchant();
    }

    public LiveData<NetworkResult<Void>> updateInventoryStock(String inventoryId, int newQuantity) {
        return orderRepository.updateInventoryStock(inventoryId, newQuantity);
    }
}
