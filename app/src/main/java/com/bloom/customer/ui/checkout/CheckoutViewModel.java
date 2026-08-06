package com.bloom.customer.ui.checkout;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.customer.data.model.Address;
import com.bloom.customer.data.model.CartItem;
import com.bloom.customer.data.model.Order;
import com.bloom.customer.data.model.Shop;
import com.bloom.customer.data.repository.AddressRepository;
import com.bloom.customer.data.repository.CartRepository;
import com.bloom.customer.data.repository.OrderRepository;
import com.bloom.customer.data.repository.ShopRepository;
import com.bloom.customer.util.NetworkResult;

import java.util.List;

public class CheckoutViewModel extends AndroidViewModel {

    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;

    private final MutableLiveData<String> selectedAddressId = new MutableLiveData<>();
    private final MutableLiveData<String> selectedDeliverySlot = new MutableLiveData<>();
    private final MutableLiveData<String> selectedPaymentMethod = new MutableLiveData<>("CARD");

    public CheckoutViewModel(@NonNull Application application) {
        super(application);
        this.addressRepository = new AddressRepository(application);
        this.cartRepository = CartRepository.getInstance(application);
        this.orderRepository = new OrderRepository(application);
        this.shopRepository = new ShopRepository(application);
    }

    public LiveData<NetworkResult<List<Address>>> getAddresses() {
        return addressRepository.getAddresses();
    }

    public LiveData<NetworkResult<Address>> getAddressById(String id) {
        return addressRepository.getAddressById(id);
    }

    public LiveData<NetworkResult<Void>> deleteAddress(String id) {
        return addressRepository.deleteAddress(id);
    }

    public LiveData<NetworkResult<Shop>> getShopById(String id) {
        return shopRepository.getShopById(id);
    }

    public LiveData<NetworkResult<Order>> placeOrder(Order order) {
        return orderRepository.placeOrder(order);
    }

    public LiveData<List<CartItem>> getCartItems() {
        return cartRepository.getCartItems();
    }

    public void clearCart() {
        cartRepository.clearCart();
    }

    public String getCartShopId() {
        return cartRepository.getCartShopId();
    }

    public double getCartTotal() {
        return cartRepository.getCartTotal();
    }

    public void setSelectedAddressId(String id) {
        selectedAddressId.setValue(id);
    }

    public LiveData<String> getSelectedAddressId() {
        return selectedAddressId;
    }

    public void setSelectedDeliverySlot(String slot) {
        selectedDeliverySlot.setValue(slot);
    }

    public LiveData<String> getSelectedDeliverySlot() {
        return selectedDeliverySlot;
    }

    public void setSelectedPaymentMethod(String method) {
        selectedPaymentMethod.setValue(method);
    }

    public LiveData<String> getSelectedPaymentMethod() {
        return selectedPaymentMethod;
    }
}
