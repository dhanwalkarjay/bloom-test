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

    public LiveData<NetworkResult<Void>> updatePassword(String phone, String password) {
        return authRepository.updatePassword(phone, password);
    }

    public LiveData<NetworkResult<AuthResponse>> loginWithGoogle(String idToken) {
        return authRepository.loginWithGoogle(idToken);
    }

    public LiveData<NetworkResult<AuthResponse>> verifyTruecaller(String authorizationCode, String codeVerifier) {
        return authRepository.verifyTruecaller(authorizationCode, codeVerifier);
    }

    public LiveData<NetworkResult<Void>> updateUserPhone(String phone) {
        return authRepository.updateUserPhone(phone);
    }

    public LiveData<NetworkResult<Boolean>> checkRole(String userId, String role) {
        return authRepository.checkRole(userId, role);
    }

    public LiveData<NetworkResult<String>> sendBackendOtp(String phone) {
        return authRepository.sendBackendOtp(phone);
    }

    public LiveData<NetworkResult<AuthResponse>> verifyBackendOtp(String phone, String otp) {
        return authRepository.verifyBackendOtp(phone, otp);
    }
}
