package com.bloom.customer.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

public class NetworkResultTest {

    @Test
    public void testSuccess_ReturnsCorrectStatusAndData() {
        String data = "Test Data";
        NetworkResult<String> result = NetworkResult.success(data);

        assertEquals(NetworkResult.Status.SUCCESS, result.status);
        assertEquals(data, result.data);
        assertNull(result.message);
    }

    @Test
    public void testError_ReturnsCorrectStatusMessageAndData() {
        String errorMessage = "Network Error";
        String fallbackData = "Fallback Data";

        NetworkResult<String> result = NetworkResult.error(errorMessage, fallbackData);

        assertEquals(NetworkResult.Status.ERROR, result.status);
        assertEquals(fallbackData, result.data);
        assertEquals(errorMessage, result.message);
    }

    @Test
    public void testError_WithNullData_ReturnsCorrectStatusAndMessage() {
        String errorMessage = "Network Error";

        NetworkResult<String> result = NetworkResult.error(errorMessage, null);

        assertEquals(NetworkResult.Status.ERROR, result.status);
        assertNull(result.data);
        assertEquals(errorMessage, result.message);
    }

    @Test
    public void testLoading_ReturnsCorrectStatusAndData() {
        String cachedData = "Cached Data";

        NetworkResult<String> result = NetworkResult.loading(cachedData);

        assertEquals(NetworkResult.Status.LOADING, result.status);
        assertEquals(cachedData, result.data);
        assertNull(result.message);
    }

    @Test
    public void testLoading_WithNullData_ReturnsCorrectStatus() {
        NetworkResult<String> result = NetworkResult.loading(null);

        assertEquals(NetworkResult.Status.LOADING, result.status);
        assertNull(result.data);
        assertNull(result.message);
    }
}
