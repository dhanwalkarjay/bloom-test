package com.bloom.customer.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A generic class that holds a value with its loading status.
 * @param <T> The type of data being returned.
 */
public class NetworkResult<T> {

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    @NonNull
    public final Status status;

    @Nullable
    public final T data;

    @Nullable
    public final String message;

    private NetworkResult(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> NetworkResult<T> success(@NonNull T data) {
        return new NetworkResult<>(Status.SUCCESS, data, null);
    }

    public static <T> NetworkResult<T> error(@NonNull String msg, @Nullable T data) {
        return new NetworkResult<>(Status.ERROR, data, msg);
    }

    public static <T> NetworkResult<T> loading(@Nullable T data) {
        return new NetworkResult<>(Status.LOADING, data, null);
    }
}
