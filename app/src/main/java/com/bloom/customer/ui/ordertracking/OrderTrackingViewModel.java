package com.bloom.customer.ui.ordertracking;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.api.RealtimeService;
import com.bloom.customer.util.NetworkResult;

public class OrderTrackingViewModel extends AndroidViewModel {

    private final RealtimeService realtimeService;
    private final MutableLiveData<String> orderStatus = new MutableLiveData<>();
    private android.os.Handler pollingHandler;
    private Runnable pollingRunnable;
    private boolean isPolling = false;

    public OrderTrackingViewModel(@NonNull Application application) {
        super(application);
        this.realtimeService = new RealtimeService();
    }

    public LiveData<String> getOrderStatus() {
        return orderStatus;
    }

    public void startTracking(String orderId) {
        if (orderId == null) return;
        realtimeService.startTracking(orderId, new RealtimeService.StatusUpdateListener() {
            @Override
            public void onStatusUpdate(String id, String newStatus) {
                orderStatus.postValue(newStatus);
            }

            @Override
            public void onConnectionError() {
                startPolling(orderId);
            }
        });
    }

    private void startPolling(String orderId) {
        if (isPolling) return;
        isPolling = true;
        
        if (pollingHandler == null) {
            pollingHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        }
        
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                LiveData<NetworkResult<com.bloom.customer.data.model.Order>> liveData = 
                    new com.bloom.customer.data.repository.OrderRepository(getApplication()).getOrderById(orderId);
                
                androidx.lifecycle.Observer<NetworkResult<com.bloom.customer.data.model.Order>> observer = 
                    new androidx.lifecycle.Observer<NetworkResult<com.bloom.customer.data.model.Order>>() {
                    @Override
                    public void onChanged(NetworkResult<com.bloom.customer.data.model.Order> result) {
                        if (result.status != NetworkResult.Status.LOADING) {
                            if (result.status == NetworkResult.Status.SUCCESS && result.data != null) {
                                orderStatus.postValue(result.data.getStatus());
                            }
                            liveData.removeObserver(this);
                        }
                    }
                };
                liveData.observeForever(observer);
                
                pollingHandler.postDelayed(this, 10000); // 10 seconds polling
            }
        };
        pollingHandler.post(pollingRunnable);
    }

    public LiveData<NetworkResult<Void>> cancelOrder(String orderId) {
        return new com.bloom.customer.data.repository.OrderRepository(getApplication()).cancelOrder(orderId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        realtimeService.stopTracking();
        isPolling = false;
        if (pollingHandler != null && pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
        }
    }
}
