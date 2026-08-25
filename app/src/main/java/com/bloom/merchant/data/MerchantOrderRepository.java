package com.bloom.merchant.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.api.RetrofitClient;
import com.bloom.customer.data.api.SupabaseAPI;
import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.model.Order;
import com.bloom.customer.data.model.Shop;
import com.bloom.customer.util.NetworkResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MerchantOrderRepository {

    private final SupabaseAPI api;
    private final SessionManager sessionManager;
    private String shopIdCache = null;

    public MerchantOrderRepository(Context context) {
        this.api = RetrofitClient.getClient(context).create(SupabaseAPI.class);
        this.sessionManager = SessionManager.getInstance(context);
    }

    public LiveData<NetworkResult<List<Order>>> fetchOrdersForMerchant() {
        MutableLiveData<NetworkResult<List<Order>>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        String userId = sessionManager.getUserId();
        if (userId == null) {
            result.setValue(NetworkResult.error("User not logged in", null));
            return result;
        }

        if (shopIdCache != null) {
            fetchOrdersByShopId(shopIdCache, result);
        } else {
            api.getShopByOwnerId("eq." + userId).enqueue(new Callback<List<Shop>>() {
                @Override
                public void onResponse(Call<List<Shop>> call, Response<List<Shop>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        shopIdCache = response.body().get(0).getId();
                        fetchOrdersByShopId(shopIdCache, result);
                    } else {
                        result.setValue(NetworkResult.error("No shop found for this merchant", null));
                    }
                }

                @Override
                public void onFailure(Call<List<Shop>> call, Throwable t) {
                    result.setValue(NetworkResult.error(t.getMessage(), null));
                }
            });
        }
        return result;
    }

    private void fetchOrdersByShopId(String shopId, MutableLiveData<NetworkResult<List<Order>>> result) {
        // Fetch all orders for this shop, ordered by created_at descending
        api.getOrdersForShop("eq." + shopId, "*", "created_at.desc").enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(NetworkResult.success(response.body()));
                } else {
                    result.setValue(NetworkResult.error("Failed to fetch orders", null));
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });
    }

    public String getShopIdCache() {
        return shopIdCache;
    }

    public LiveData<NetworkResult<Void>> updateOrderStatus(String orderId, String newStatus) {
        MutableLiveData<NetworkResult<Void>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        Map<String, Object> body = new HashMap<>();
        body.put("status", newStatus);

        api.updateOrder("eq." + orderId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(NetworkResult.success(null));
                } else {
                    result.setValue(NetworkResult.error("Failed to update status", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<NetworkResult<List<com.bloom.customer.data.model.ShopInventoryItem>>> fetchInventoryForMerchant() {
        MutableLiveData<NetworkResult<List<com.bloom.customer.data.model.ShopInventoryItem>>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        if (shopIdCache != null) {
            fetchInventoryByShopId(shopIdCache, result);
        } else {
            result.setValue(NetworkResult.error("Shop ID not available yet", null));
        }
        return result;
    }

    private void fetchInventoryByShopId(String shopId, MutableLiveData<NetworkResult<List<com.bloom.customer.data.model.ShopInventoryItem>>> result) {
        api.getShopInventory("eq." + shopId).enqueue(new Callback<List<com.bloom.customer.data.model.ShopInventoryItem>>() {
            @Override
            public void onResponse(Call<List<com.bloom.customer.data.model.ShopInventoryItem>> call, Response<List<com.bloom.customer.data.model.ShopInventoryItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(NetworkResult.success(response.body()));
                } else {
                    result.setValue(NetworkResult.error("Failed to fetch inventory", null));
                }
            }

            @Override
            public void onFailure(Call<List<com.bloom.customer.data.model.ShopInventoryItem>> call, Throwable t) {
                result.setValue(NetworkResult.error(t.getMessage(), null));
            }
        });
    }

    public LiveData<NetworkResult<Void>> updateInventoryStock(String inventoryId, int newQuantity) {
        MutableLiveData<NetworkResult<Void>> result = new MutableLiveData<>();
        result.setValue(NetworkResult.loading(null));

        Map<String, Object> body = new HashMap<>();
        body.put("stock_quantity", newQuantity);

        api.updateShopInventory("eq." + inventoryId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.setValue(NetworkResult.success(null));
                } else {
                    result.setValue(NetworkResult.error("Failed to update stock", null));
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
