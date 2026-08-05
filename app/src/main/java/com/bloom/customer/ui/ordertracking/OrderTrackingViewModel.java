package com.bloom.customer.ui.ordertracking;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.api.RealtimeService;

public class OrderTrackingViewModel extends AndroidViewModel {

    private final RealtimeService realtimeService;
    private final MutableLiveData<String> orderStatus = new MutableLiveData<>();

    public OrderTrackingViewModel(@NonNull Application application) {
        super(application);
        this.realtimeService = new RealtimeService();
    }

    public LiveData<String> getOrderStatus() {
        return orderStatus;
    }

    public void startTracking(String orderId) {
        if (orderId == null) return;
        realtimeService.startTracking(orderId, (id, newStatus) -> {
            orderStatus.postValue(newStatus);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        realtimeService.stopTracking();
    }
}
