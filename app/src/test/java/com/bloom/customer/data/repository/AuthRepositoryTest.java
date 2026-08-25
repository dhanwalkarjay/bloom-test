package com.bloom.customer.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.bloom.customer.data.api.SupabaseAuthApi;
import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.model.AuthResponse;
import com.bloom.customer.data.model.User;
import com.bloom.customer.util.NetworkResult;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Context mockContext;

    @Mock
    private SharedPreferences mockPrefs;

    @Mock
    private SharedPreferences.Editor mockEditor;

    @Mock
    private SupabaseAuthApi mockAuthApi;

    @Mock
    private Call<AuthResponse> mockCall;

    @Mock
    private SessionManager mockSessionManager;

    @Mock
    private Observer<NetworkResult<AuthResponse>> mockObserver;

    private AuthRepository authRepository;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), org.mockito.ArgumentMatchers.nullable(String.class))).thenReturn(mockEditor);

        // Reset RetrofitClient singleton to avoid bleeding
        Class<?> retrofitClass = Class.forName("com.bloom.customer.data.api.RetrofitClient");
        Field retrofitField = retrofitClass.getDeclaredField("retrofit");
        retrofitField.setAccessible(true);
        retrofitField.set(null, null);

        // We must mock static getInstance methods because they rely on Android framework (EncryptedSharedPreferences, etc.)
        try (org.mockito.MockedStatic<SessionManager> mockedSessionManager = org.mockito.Mockito.mockStatic(SessionManager.class);
             org.mockito.MockedStatic<com.bloom.customer.data.api.RetrofitClient> mockedRetrofitClient = org.mockito.Mockito.mockStatic(com.bloom.customer.data.api.RetrofitClient.class)) {
             
            mockedSessionManager.when(() -> SessionManager.getInstance(any())).thenReturn(mockSessionManager);
            
            retrofit2.Retrofit mockRetrofit = org.mockito.Mockito.mock(retrofit2.Retrofit.class);
            when(mockRetrofit.create(SupabaseAuthApi.class)).thenReturn(mockAuthApi);
            mockedRetrofitClient.when(() -> com.bloom.customer.data.api.RetrofitClient.getClient(any())).thenReturn(mockRetrofit);
            
            authRepository = new AuthRepository(mockContext);
        }
    }

    @Test
    public void testLogin_Success() {
        String json = "{\"access_token\":\"test_access_token\",\"refresh_token\":\"test_refresh_token\",\"user\":{\"id\":\"user_123\"}}";
        AuthResponse fakeResponse = new com.google.gson.Gson().fromJson(json, AuthResponse.class);

        when(mockAuthApi.login(anyString(), any())).thenReturn(mockCall);

        doAnswer(invocation -> {
            Callback<AuthResponse> callback = invocation.getArgument(0);
            callback.onResponse(mockCall, Response.success(fakeResponse));
            return null;
        }).when(mockCall).enqueue(any());

        authRepository.login("1234567890", "password").observeForever(mockObserver);

        verify(mockSessionManager).saveSession("test_access_token", "test_refresh_token", "user_123");
        verify(mockObserver).onChanged(org.mockito.ArgumentMatchers.argThat(result -> 
            result.status == NetworkResult.Status.SUCCESS && 
            result.data != null && 
            "test_access_token".equals(result.data.getAccessToken())
        ));
    }

    @Test
    public void testLogin_Failure() {
        when(mockAuthApi.login(anyString(), any())).thenReturn(mockCall);

        doAnswer(invocation -> {
            Callback<AuthResponse> callback = invocation.getArgument(0);
            ResponseBody errorBody = ResponseBody.create("Error", MediaType.parse("text/plain"));
            callback.onResponse(mockCall, Response.error(400, errorBody));
            return null;
        }).when(mockCall).enqueue(any());

        authRepository.login("1234567890", "password").observeForever(mockObserver);

        verify(mockObserver).onChanged(org.mockito.ArgumentMatchers.argThat(result -> 
            result.status == NetworkResult.Status.ERROR && result.message.contains("Login Failed")
        ));
    }
}
