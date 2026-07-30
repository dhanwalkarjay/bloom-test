package com.bloom.customer.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.bloom.customer.data.model.AuthResponse;
import com.bloom.customer.data.repository.AuthRepository;
import com.bloom.customer.util.NetworkResult;

/**
 * ViewModel for Authentication screens.
 * Pattern: Observer Pattern - UI observes data changes.
 * Principle: Separation of Concerns - UI logic is separated from data handling.
 */
public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    public LiveData<NetworkResult<AuthResponse>> login(String phone, String password) {
        return authRepository.login(phone, password);
    }

    public LiveData<NetworkResult<AuthResponse>> signup(String name, String phone, String password) {
        return authRepository.signup(name, phone, password);
    }

    public LiveData<NetworkResult<AuthResponse>> verifyOtp(String phone, String token) {
        return authRepository.verifyOtp(phone, token);
    }

    public LiveData<NetworkResult<Void>> sendOtp(String phone) {
        return authRepository.sendOtp(phone);
    }
}
