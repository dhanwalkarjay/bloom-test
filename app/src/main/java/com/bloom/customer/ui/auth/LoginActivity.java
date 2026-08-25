package com.bloom.customer.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bloom.databinding.ActivityLoginBinding;
import com.bloom.customer.ui.home.HomeActivity;
import com.bloom.customer.util.NetworkResult;
import com.bloom.customer.util.InputValidator;

import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.IntentSenderRequest;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;

import com.truecaller.android.sdk.oAuth.TcSdk;
import com.truecaller.android.sdk.oAuth.TcSdkOptions;
import com.truecaller.android.sdk.oAuth.TcOAuthCallback;
import com.truecaller.android.sdk.oAuth.TcOAuthData;
import com.truecaller.android.sdk.oAuth.TcOAuthError;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Activity for user login using Google One Tap and Phone Hint API.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;
    private SignInClient oneTapClient;
    
    private final ActivityResultLauncher<IntentSenderRequest> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                    String idToken = credential.getGoogleIdToken();
                    if (idToken != null) {
                        loginWithSupabase(idToken);
                    }
                } catch (ApiException e) {
                    setLoading(false);
                    binding.tvError.setText("Google Sign-In failed.");
                    binding.tvError.setVisibility(View.VISIBLE);
                }
            });

    private final ActivityResultLauncher<IntentSenderRequest> phoneHintLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                try {
                    String phoneNumber = Identity.getSignInClient(this).getPhoneNumberFromIntent(result.getData());
                    if (phoneNumber != null && phoneNumber.length() > 5 && !phoneNumber.equals("1")) {
                        updatePhoneNumber(phoneNumber);
                    } else {
                        showManualPhoneInput();
                    }
                } catch (ApiException e) {
                    // User canceled or no number found. Show manual input.
                    showManualPhoneInput();
                }
            });

    private String currentCodeVerifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false); 
        androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(true);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(AuthViewModel.class);

        // Configure Google Sign-In as a backup
        oneTapClient = Identity.getSignInClient(this);
        
        // Hide buttons initially — flow is automatic
        binding.btnGoogleLogin.setVisibility(View.GONE);
        binding.btnTruecallerLogin.setVisibility(View.GONE);
        binding.btnLogin.setVisibility(View.GONE);
        
        // Set up bottom links
        binding.tvMerchantLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, com.bloom.merchant.auth.MerchantLoginActivity.class));
        });

        binding.tvAgentLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, com.bloom.agent.auth.AgentLoginActivity.class));
        });

        // 1. Initialize Truecaller SDK
        TcSdkOptions tcSdkOptions = new TcSdkOptions.Builder(this, new TcOAuthCallback() {
            @Override
            public void onSuccess(TcOAuthData tcOAuthData) {
                // Truecaller verified! Send auth code to backend
                verifyTruecallerWithBackend(tcOAuthData.getAuthorizationCode(), currentCodeVerifier);
            }

            @Override
            public void onFailure(TcOAuthError tcOAuthError) {
                // User denied or Truecaller failed — fall back to OTP
                Log.d("LoginActivity", "Truecaller failed: " + tcOAuthError.getErrorCode());
                showManualPhoneInput();
            }

            @Override
            public void onVerificationRequired(TcOAuthError tcOAuthError) {
                // Additional verification needed — fall back to OTP
                showManualPhoneInput();
            }
        }).build();

        TcSdk.init(tcSdkOptions);

        // 2. AUTO-TRIGGER: Check if Truecaller is available and trigger immediately
        try {
            if (TcSdk.getInstance().isOAuthFlowUsable()) {
                // Truecaller is installed! Auto-trigger the consent screen
                Log.d("LoginActivity", "Truecaller detected — auto-triggering verification");
                setLoading(true);
                binding.tvTitle.setText("Verifying with Truecaller...");
                
                currentCodeVerifier = com.truecaller.android.sdk.oAuth.CodeVerifierUtil.Companion.generateRandomCodeVerifier();
                String codeChallenge = com.truecaller.android.sdk.oAuth.CodeVerifierUtil.Companion.getCodeChallenge(currentCodeVerifier);
                
                TcSdk.getInstance().setOAuthState(java.util.UUID.randomUUID().toString());
                TcSdk.getInstance().setOAuthScopes(new String[]{"profile", "phone"});
                TcSdk.getInstance().setCodeChallenge(codeChallenge);
                TcSdk.getInstance().getAuthorizationCode(LoginActivity.this);
            } else {
                // Truecaller not installed — go straight to OTP
                Log.d("LoginActivity", "Truecaller not available — showing OTP input");
                showManualPhoneInput();
            }
        } catch (Exception e) {
            // Any SDK error — go straight to OTP
            Log.e("LoginActivity", "Truecaller SDK error", e);
            showManualPhoneInput();
        }
    }

    private void verifyTruecallerWithBackend(String authorizationCode, String codeVerifier) {
        setLoading(true);
        // Call the backend endpoint
        viewModel.verifyTruecaller(authorizationCode, codeVerifier).observe(this, result -> {
            if (result == null || result.status == NetworkResult.Status.LOADING) {
                return; // Ignore loading state
            }
            
            setLoading(false);
            if (result.status == NetworkResult.Status.SUCCESS) {
                Intent intent = new Intent(LoginActivity.this, com.bloom.customer.ui.home.HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (result.status == NetworkResult.Status.ERROR) {
                Toast.makeText(this, "Backend verification failed", Toast.LENGTH_SHORT).show();
                showManualPhoneInput();
            }
        });
    }

    private void initiateGoogleSignIn() {
        setLoading(true);
        binding.tvError.setVisibility(View.GONE);
        
        String clientId = getString(com.bloom.R.string.default_web_client_id);
        BeginSignInRequest signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(clientId)
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .setAutoSelectEnabled(false)
                .build();

        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, result -> {
                    IntentSenderRequest request = new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                    googleSignInLauncher.launch(request);
                })
                .addOnFailureListener(this, e -> {
                    setLoading(false);
                    binding.tvError.setText("Failed to start Google Sign-In.");
                    binding.tvError.setVisibility(View.VISIBLE);
                    Log.e("LoginActivity", "Google Sign-In Error", e);
                });
    }

    private void loginWithSupabase(String idToken) {
        viewModel.loginWithGoogle(idToken).observe(this, result -> {
            switch (result.status) {
                case SUCCESS:
                    // Successfully logged into Supabase. Now capture Phone Number.
                    requestPhoneNumber();
                    break;
                case ERROR:
                    setLoading(false);
                    binding.tvError.setText(result.message);
                    binding.tvError.setVisibility(View.VISIBLE);
                    break;
                case LOADING:
                    break;
            }
        });
    }

    private void requestPhoneNumber() {
        GetPhoneNumberHintIntentRequest request = GetPhoneNumberHintIntentRequest.builder().build();
        oneTapClient.getPhoneNumberHintIntent(request)
                .addOnSuccessListener(result -> {
                    try {
                        IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(result.getIntentSender()).build();
                        phoneHintLauncher.launch(intentSenderRequest);
                    } catch (Exception e) {
                        goToHome();
                    }
                })
                .addOnFailureListener(e -> {
                    showManualPhoneInput(); // Phone hint not available, show manual input
                });
    }

    private void showManualPhoneInput() {
        setLoading(false);
        binding.btnGoogleLogin.setVisibility(View.GONE);
        binding.btnTruecallerLogin.setVisibility(View.GONE);
        binding.llPhoneInput.setVisibility(View.VISIBLE);
        binding.btnLogin.setVisibility(View.VISIBLE);
        binding.btnLogin.setText("Get OTP");
        binding.tvTitle.setText("Verify your number");
        
        binding.btnLogin.setOnClickListener(v -> attemptSendOtp());
    }

    private void attemptSendOtp() {
        String phone = binding.etPhone.getText().toString().trim();
        if (!InputValidator.isValidPhone(phone)) {
            binding.tilPhone.setError("Invalid phone number");
            return;
        }
        binding.tilPhone.setError(null);
        setLoading(true);
        
        String formattedPhone = "+91" + phone.replace("+91", "");
        viewModel.sendBackendOtp(formattedPhone).observe(this, result -> {
            setLoading(false);
            if (result.status == NetworkResult.Status.SUCCESS) {
                // OTP sent successfully, show OTP input
                binding.llPhoneInput.setVisibility(View.GONE);
                binding.llOtpInput.setVisibility(View.VISIBLE);
                binding.btnLogin.setText("Verify OTP");
                binding.btnLogin.setOnClickListener(v -> attemptVerifyOtp(formattedPhone));
                
                // DEV MODE: Show OTP in toast and auto-fill (remove in production)
                if (result.data != null) {
                    Toast.makeText(this, "DEV OTP: " + result.data, Toast.LENGTH_LONG).show();
                    binding.etOtp.setText(result.data);
                }
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.tvError.setText(result.message);
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updatePhoneNumber(String phoneNumber) {
        viewModel.updateUserPhone(phoneNumber).observe(this, result -> {
            if (result.status != NetworkResult.Status.LOADING) {
                goToHome();
            }
        });
    }

    private void attemptVerifyOtp(String formattedPhone) {
        String otp = binding.etOtp.getText().toString().trim();
        if (otp.length() != 6) {
            binding.tilOtp.setError("Enter a valid 6-digit OTP");
            return;
        }
        binding.tilOtp.setError(null);
        setLoading(true);

        viewModel.verifyBackendOtp(formattedPhone, otp).observe(this, result -> {
            setLoading(false);
            if (result.status == NetworkResult.Status.SUCCESS) {
                goToHome();
            } else if (result.status == NetworkResult.Status.ERROR) {
                binding.tvOtpError.setText(result.message);
                binding.tvOtpError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void goToHome() {
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnGoogleLogin.setEnabled(!isLoading);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TcSdk.SHARE_PROFILE_REQUEST_CODE) {
            try {
                TcSdk.getInstance().onActivityResultObtained(this, requestCode, resultCode, data);
            } catch (Exception e) {
                showManualPhoneInput();
            }
        }
    }
}
