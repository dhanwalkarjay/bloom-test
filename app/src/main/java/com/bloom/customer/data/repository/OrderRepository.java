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

        final List<OrderItem> itemsToSave = order.getItems();
        final com.bloom.customer.data.model.Order.ShopInfo savedShop = order.getShop();

        // Convert Order + Items into a Map for the RPC
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        
        // Use Gson to serialize to Maps
        com.google.gson.Gson gson = new com.google.gson.Gson();
        String orderJson = gson.toJson(order);
        java.util.Map<String, Object> orderMap = gson.fromJson(orderJson, new com.google.gson.reflect.TypeToken<java.util.Map<String, Object>>(){}.getType());
        
        // Remove items and shop from the order_data map
        orderMap.remove("items");
        orderMap.remove("shop");
        
        body.put("order_data", orderMap);
        body.put("items_data", itemsToSave);

        api.placeOrderAtomic(body).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                // Restore items for local use
                order.setItems(itemsToSave);
                order.setShop(savedShop);

                if (response.isSuccessful() && response.body() != null) {
                    Order createdOrder = response.body();
                    result.setValue(NetworkResult.success(createdOrder));
                } else {
                    String error = "Failed to create order: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            error += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        timber.log.Timber.e(e, "Error parsing orders from json");
                    }
                    result.setValue(NetworkResult.error(error, null));
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<NetworkResult<List<Order>>> getOrderHistory(String userId) {
        MutableLiveData<NetworkResult<List<Order>>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        // Use correct table names for joins in Supabase/PostgREST
        String select = "*,order_items(*,products(*)),shops(*),addresses(*)";
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

    public LiveData<NetworkResult<Order>> getOrderById(String orderId) {
        MutableLiveData<NetworkResult<Order>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        String select = "*,order_items(*,products(*)),shops(*),addresses(*)";

        api.getOrders(null, select, "id.eq." + orderId).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    result.setValue(NetworkResult.success(response.body().get(0)));
                } else {
                    result.setValue(NetworkResult.error("Order not found", null));
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });
        return result;
    }



    public LiveData<NetworkResult<Void>> cancelOrder(String orderId) {
        MutableLiveData<NetworkResult<Void>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("status", "cancelled");

        api.updateOrder("id.eq." + orderId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(NetworkResult.success(null));
                } else {
                    result.setValue(NetworkResult.error("Failed to cancel order", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<NetworkResult<Void>> updateOrderPaymentStatus(String orderId, String status) {
        MutableLiveData<NetworkResult<Void>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("payment_status", status);

        api.updateOrder("id.eq." + orderId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(NetworkResult.success(null));
                } else {
                    result.setValue(NetworkResult.error("Failed to update payment status", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });
        return result;
    }
}
