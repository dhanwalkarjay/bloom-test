package com.bloom.customer.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAPI;
import com.bloom.customer.data.model.Order;
import com.bloom.customer.data.model.OrderItem;
import com.bloom.customer.util.NetworkResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for managing orders.
 */
public class OrderRepository {

    private final SupabaseAPI api;

    public OrderRepository(Context context) {
        this.api = RetrofitClient.getClient(context).create(SupabaseAPI.class);
    }

    public LiveData<NetworkResult<Order>> placeOrder(Order order) {
        MutableLiveData<NetworkResult<Order>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        api.createOrder(order).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Order createdOrder = response.body().get(0);
                    
                    // Assign the returned ID to all items
                    for (OrderItem item : order.getItems()) {
                        item.setOrderId(createdOrder.getId());
                    }
                    
                    // Now push the items
                    createOrderItems(order.getItems(), result, createdOrder);
                } else {
                    String error = "Failed to create order: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            error += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    result.setValue(NetworkResult.error(error, null));
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<NetworkResult<List<Order>>> getOrderHistory(String userId) {
        MutableLiveData<NetworkResult<List<Order>>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        String select = "*,order_items(*,products(title)),florists(name)";
        String order = "created_at.desc";

        api.getOrders("eq." + userId, select, order).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(NetworkResult.success(response.body()));
                } else {
                    result.setValue(NetworkResult.error("Failed to fetch order history", null));
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }

    private void createOrderItems(List<OrderItem> items, MutableLiveData<NetworkResult<Order>> result, Order createdOrder) {
        api.createOrderItems(items).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(NetworkResult.success(createdOrder));
                } else {
                    String error = "Order created, but items failed: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            error += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    result.setValue(NetworkResult.error(error, null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(NetworkResult.error("Failed to save order items: " + t.getMessage(), null));
            }
        });
    }
}
