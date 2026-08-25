package com.bloom.customer.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.bloom.customer.data.model.CartItem;
import com.bloom.customer.data.model.Product;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class CartRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Context mockContext;

    @Mock
    private SharedPreferences mockPrefs;

    @Mock
    private SharedPreferences.Editor mockEditor;

    private CartRepository cartRepository;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), org.mockito.ArgumentMatchers.nullable(String.class))).thenReturn(mockEditor);
        
        // Reset singleton instance via reflection
        Field instanceField = CartRepository.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        cartRepository = CartRepository.getInstance(mockContext);
    }

    @Test
    public void testAddToCart_Success() {
        Product product = new Product();
        product.setId("p1");
        product.setShopId("shop1");
        product.setPrice(10.0);
        
        CartItem item = new CartItem(product);
        item.setQuantity(2);

        boolean result = cartRepository.addToCart(item);

        assertTrue(result);
        List<CartItem> items = cartRepository.getCartItems().getValue();
        assertEquals(1, items.size());
        assertEquals(20.0, cartRepository.getCartTotal(), 0.001);
    }

    @Test
    public void testAddToCart_DifferentShop_Fails() {
        // Setup initial item
        Product product1 = new Product();
        product1.setId("p1");
        product1.setShopId("shop1");
        CartItem item1 = new CartItem(product1);
        
        // Mock getCartShopId to return shop1 (as if we already saved it)
        when(mockPrefs.getString("cart_shop_id", null)).thenReturn("shop1");
        
        cartRepository.addToCart(item1);

        // Try adding from different shop
        Product product2 = new Product();
        product2.setId("p2");
        product2.setShopId("shop2");
        CartItem item2 = new CartItem(product2);

        boolean result = cartRepository.addToCart(item2);

        assertFalse(result); // Should fail due to single-vendor constraint
    }

    @Test
    public void testUpdateQuantity() {
        Product product = new Product();
        product.setId("p1");
        product.setShopId("shop1");
        CartItem item = new CartItem(product);
        item.setQuantity(1);

        cartRepository.addToCart(item);
        
        // Same product, different quantity -> should update existing
        CartItem updateItem = new CartItem(product);
        updateItem.setQuantity(3);
        
        cartRepository.addToCart(updateItem);

        List<CartItem> items = cartRepository.getCartItems().getValue();
        assertEquals(1, items.size());
        assertEquals(3, items.get(0).getQuantity());
    }

    @Test
    public void testRemoveFromCart() {
        Product product = new Product();
        product.setId("p1");
        product.setShopId("shop1");
        CartItem item = new CartItem(product);
        
        cartRepository.addToCart(item);
        assertEquals(1, cartRepository.getCartItems().getValue().size());
        
        cartRepository.removeFromCart(0);
        assertEquals(0, cartRepository.getCartItems().getValue().size());
    }
}
