package com.bloom.customer.data.api;

import com.bloom.customer.data.model.Addon;
import com.bloom.customer.data.model.Address;
import com.bloom.customer.data.model.FeatureFlag;
import com.bloom.customer.data.model.Notification;
import com.bloom.customer.data.model.Order;
import com.bloom.customer.data.model.OrderItem;
import com.bloom.customer.data.model.Product;
import com.bloom.customer.data.model.ProductSearchResult;
import com.bloom.customer.data.model.Profile;
import com.bloom.customer.data.model.Review;
import com.bloom.customer.data.model.Shop;
import com.bloom.customer.util.Constants;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Retrofit interface for Supabase REST and RPC endpoints.
 * Principle: Interface Segregation - specifically for shop and product data.
 */
public interface SupabaseAPI {

    /**
     * Call the nearby_shops RPC.
     * Expects lat, lng, and optional radius.
     */
    @POST(Constants.REST_ENDPOINT + "rpc/nearby_shops")
    Call<List<Shop>> getNearbyShops(@Body Map<String, Object> body);

    @POST(Constants.REST_ENDPOINT + "rpc/check_role")
    Call<Boolean> checkRole(@Body Map<String, Object> body);

    @GET(Constants.REST_ENDPOINT + "shops")
    Call<List<Shop>> getShopById(@Query("id") String id);

    @GET(Constants.REST_ENDPOINT + "shops")
    Call<List<Shop>> getShopByOwnerId(@Query("owner_id") String ownerId);

    @GET(Constants.REST_ENDPOINT + "shop_inventory")
    Call<List<com.bloom.customer.data.model.ShopInventoryItem>> getShopInventory(@Query("shop_id") String shopId);

    @PATCH(Constants.REST_ENDPOINT + "shop_inventory")
    Call<Void> updateShopInventory(@Query("id") String id, @Body Map<String, Object> body);

    /**
     * Get products for a specific florist/shop.
     */
    @GET(Constants.REST_ENDPOINT + "products")
    Call<List<Product>> getProductsByShop(@Query("shop_id") String shopId);

    /**
     * Search products by title or category.
     */
    @GET(Constants.REST_ENDPOINT + "products")
    Call<List<Product>> searchProducts(
        @Query("title") String title,
        @Query("category") String category,
        @Query("is_lux") Boolean isLux
    );

    /**
     * Get products by category.
     */
    @GET(Constants.REST_ENDPOINT + "products")
    Call<List<Product>> getProductsByCategory(@Query("category") String category);

    /**
     * Search products nearby using RPC.
     */
    @POST(Constants.REST_ENDPOINT + "rpc/search_products_nearby")
    Call<List<ProductSearchResult>> searchProductsNearby(@Body Map<String, Object> body);

    /**
     * Get featured products (seasonal or bestseller).
     */
    @GET(Constants.REST_ENDPOINT + "products")
    Call<List<Product>> getFeaturedProducts(
        @Query("is_seasonal") String isSeasonal,
        @Query("is_bestseller") String isBestseller,
        @Query("is_new_arrival") String isNewArrival
    );

    /**
     * Get user profile.
     */
    @GET(Constants.REST_ENDPOINT + "profiles")
    Call<List<Profile>> getProfile(@Query("id") String id);

    /**
     * Get all available addons.
     */
    @GET(Constants.REST_ENDPOINT + "addons")
    Call<List<Addon>> getAddons();

    /**
     * Get saved addresses for the current user.
     * RLS on the server will automatically filter these to the authenticated user.
     */
    @GET(Constants.REST_ENDPOINT + "addresses")
    Call<List<Address>> getAddresses();

    @GET(Constants.REST_ENDPOINT + "addresses")
    Call<List<Address>> getAddressById(@Query("id") String id);

    /**
     * Add a new address.
     */
    @POST(Constants.REST_ENDPOINT + "addresses")
    Call<Void> addAddress(@Body Address address);

    /**
     * Update an address.
     */
    @PATCH(Constants.REST_ENDPOINT + "addresses")
    Call<Void> updateAddress(@Query("id") String id, @Body Map<String, Object> body);

    /**
     * Delete an address.
     */
    @retrofit2.http.DELETE(Constants.REST_ENDPOINT + "addresses")
    Call<Void> deleteAddress(@Query("id") String id);

    /**
     * Create a new order atomically (order + items).
     */
    @POST(Constants.REST_ENDPOINT + "rpc/place_order")
    Call<Order> placeOrderAtomic(@Body java.util.Map<String, Object> body);

    /**
     * Get orders for the current user.
     */
    @GET(Constants.REST_ENDPOINT + "orders")
    Call<List<Order>> getOrders(
        @Query("user_id") String userId,
        @Query("select") String select,
        @Query("order") String order
    );

    @GET(Constants.REST_ENDPOINT + "orders")
    Call<List<Order>> getOrdersForShop(
        @Query("shop_id") String shopId,
        @Query("select") String select,
        @Query("order") String order
    );

    /**
     * Update an order.
     */
    @PATCH(Constants.REST_ENDPOINT + "orders")
    Call<Void> updateOrder(@Query("id") String filter, @Body java.util.Map<String, Object> body);

    /**
     * Occasions
     */
    @GET(Constants.REST_ENDPOINT + "user_occasions")
    Call<List<com.bloom.customer.data.model.Occasion>> getOccasions(@Query("user_id") String userIdFilter);

    @POST(Constants.REST_ENDPOINT + "user_occasions")
    Call<Void> createOccasion(@Body com.bloom.customer.data.model.Occasion occasion);

    @PATCH(Constants.REST_ENDPOINT + "user_occasions")
    Call<Void> updateOccasion(@Query("id") String filter, @Body java.util.Map<String, Object> body);

    @retrofit2.http.DELETE(Constants.REST_ENDPOINT + "user_occasions")
    Call<Void> deleteOccasion(@Query("id") String filter);

    /**
     * Submit a review for an order.
     */
    @Headers("Prefer: resolution=merge-duplicates")
    @POST(Constants.REST_ENDPOINT + "reviews")
    Call<Void> postReview(@Body Review review);

    /**
     * Get all feature flags.
     */
    @GET(Constants.REST_ENDPOINT + "feature_flags")
    Call<List<FeatureFlag>> getFeatureFlags();

    /**
     * Get user notifications.
     */
    @GET(Constants.REST_ENDPOINT + "notifications")
    Call<List<Notification>> getNotifications(
        @Query("user_id") String userId,
        @Query("order") String order
    );

    /**
     * Check if a user has a specific role.
     */
    @POST(Constants.REST_ENDPOINT + "rpc/has_role")
    Call<Boolean> hasRole(@Body Map<String, Object> body);
}
