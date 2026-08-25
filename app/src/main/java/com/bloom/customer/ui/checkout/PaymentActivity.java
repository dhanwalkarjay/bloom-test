package com.bloom.customer.ui.checkout;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bloom.R;
import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.model.CartItem;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import com.bloom.customer.data.model.Order;
import com.bloom.customer.data.model.OrderItem;
import com.bloom.customer.data.model.Shop;
import com.bloom.customer.data.model.Address;
import com.bloom.customer.ui.orderconfirmation.OrderConfirmationActivity;
import com.bloom.customer.util.CurrencyFormatter;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityPaymentBinding;

import java.util.ArrayList;
import java.util.List;

public class PaymentActivity extends AppCompatActivity {

    private ActivityPaymentBinding binding;
    private CheckoutViewModel viewModel;
    private double totalAmount;
    private double deliveryFee = 50.0;
    private String addressId;
    private String deliverySlot;
    private String selectedMethod = "CARD";
    private List<ResolveInfo> upiApps;
    private String selectedUpiPackage = null;
    private boolean isUpiAvailable = false;
    private ActivityResultLauncher<Intent> upiPaymentLauncher;
    private boolean isAddressless = false;
    private String recipientName;
    private String recipientPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);

        addressId = getIntent().getStringExtra("address_id");
        deliverySlot = getIntent().getStringExtra("delivery_slot");
        
        isAddressless = getIntent().getBooleanExtra("is_addressless", false);
        recipientName = getIntent().getStringExtra("recipient_name");
        recipientPhone = getIntent().getStringExtra("recipient_phone");

        setupToolbar();
        setupMethods();
        loadInstalledUpiApps();
        fetchDetailsAndCalculate();

        upiPaymentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK || result.getResultCode() == 11) {
                    if (result.getData() != null) {
                        String response = result.getData().getStringExtra("response");
                        if (isUpiPaymentSuccessful(response)) {
                            markOrderAsPaid();
                        } else {
                            handlePaymentFailure("Payment failed or was declined.");
                        }
                    } else {
                         handlePaymentFailure("Payment failed (No data returned).");
                    }
                } else {
                    handlePaymentFailure("Payment cancelled by user.");
                }
            }
        );

        binding.btnPayNow.setOnClickListener(v -> handlePlaceOrder());
        
        binding.switchAnonymous.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                com.bloom.customer.util.HapticUtil.performSuccess(this);
                showSnackbar("Identity Hidden");
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );

            binding.bottomBar.setPadding(
                    binding.bottomBar.getPaddingLeft(),
                    binding.bottomBar.getPaddingTop(),
                    binding.bottomBar.getPaddingRight(),
                    insets.bottom
            );

            binding.bottomBar.post(() -> {
                int bottomBarHeight = binding.bottomBar.getHeight();
                androidx.core.widget.NestedScrollView scrollView = (androidx.core.widget.NestedScrollView) binding.getRoot().getChildAt(1);
                if (scrollView != null) {
                    View scrollContent = scrollView.getChildAt(0);
                    if (scrollContent != null) {
                        scrollContent.setPadding(
                            scrollContent.getPaddingLeft(),
                            scrollContent.getPaddingTop(),
                            scrollContent.getPaddingRight(),
                            bottomBarHeight + insets.bottom
                        );
                    }
                }
            });
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(binding.getRoot());
    }

    private boolean isUpiPaymentSuccessful(String response) {
        if (response == null) return false;
        String[] params = response.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length >= 2 && keyValue[0].toLowerCase().equals("status")) {
                String status = keyValue[1].toLowerCase();
                return status.equals("success") || status.equals("submitted");
            }
        }
        return false;
    }

    private void handlePaymentFailure(String message) {
        showSnackbar(message);
        binding.btnPayNow.setEnabled(true);
        updatePayButtonText();
    }

    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG);
        snackbar.setAnchorView(binding.bottomBar);
        snackbar.show();
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupMethods() {
        binding.blackCardContainer.setOnClickListener(v -> selectMethod("CARD"));
        binding.cardUpi.setOnClickListener(v -> selectMethod("UPI"));
        binding.cardWallet.setOnClickListener(v -> selectMethod("WALLET"));
        binding.cardCod.setOnClickListener(v -> selectMethod("COD"));
        
        if (binding.ivEditCard != null) {
            binding.ivEditCard.setOnClickListener(v -> showSnackbar("Manage saved cards in Profile"));
        }
    }

    private void selectMethod(String method) {
        if (method.equals("UPI") && !isUpiAvailable) {
            showSnackbar("No UPI apps installed");
            return;
        }

        selectedMethod = method;
        
        binding.scrollUpiApps.setVisibility(method.equals("UPI") && isUpiAvailable ? View.VISIBLE : View.GONE);
        
        resetMethodUI(null, binding.rbCard);
        resetMethodUI(binding.cardUpi, binding.rbUpi);
        resetMethodUI(binding.cardWallet, binding.rbWallet);
        resetMethodUI(binding.cardCod, binding.rbCod);
        
        switch (method) {
            case "CARD":
                highlightMethod(null, binding.rbCard);
                break;
            case "UPI":
                highlightMethod(binding.cardUpi, binding.rbUpi);
                break;
            case "WALLET":
                highlightMethod(binding.cardWallet, binding.rbWallet);
                break;
            case "COD":
                highlightMethod(binding.cardCod, binding.rbCod);
                break;
        }
        updatePayButtonText();
    }

    private void resetMethodUI(com.google.android.material.card.MaterialCardView card, android.widget.RadioButton rb) {
        if (card != null) {
            card.setStrokeColor(ContextCompat.getColor(this, R.color.cart_outline_variant));
            card.setStrokeWidth(2);
        }
        rb.setChecked(false);
        rb.setButtonTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.cart_outline_variant)));
    }

    private void highlightMethod(com.google.android.material.card.MaterialCardView card, android.widget.RadioButton rb) {
        if (card != null) {
            card.setStrokeColor(ContextCompat.getColor(this, R.color.cart_primary));
            card.setStrokeWidth(4);
        }
        rb.setChecked(true);
        rb.setButtonTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.cart_primary)));
    }

    private void loadInstalledUpiApps() {
        Intent upiIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("upi://pay"));
        PackageManager pm = getPackageManager();
        upiApps = pm.queryIntentActivities(upiIntent, PackageManager.MATCH_DEFAULT_ONLY);

        if (upiApps == null || upiApps.isEmpty()) {
            isUpiAvailable = false;
            binding.cardUpi.setEnabled(false);
            binding.tvUpiTitle.setText("Online UPI (No apps found)");
            binding.tvUpiTitle.setTextColor(ContextCompat.getColor(this, R.color.cart_outline));
            binding.tvUpiDesc.setTextColor(ContextCompat.getColor(this, R.color.cart_outline));
            binding.cardUpiIcon.setCardBackgroundColor(ContextCompat.getColor(this, R.color.cart_surface_container));
            binding.rbUpi.setButtonTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.cart_outline)));
            return;
        }

        isUpiAvailable = true;
        selectedUpiPackage = upiApps.get(0).activityInfo.packageName; 

        for (ResolveInfo info : upiApps) {
            String packageName = info.activityInfo.packageName;
            CharSequence label = info.loadLabel(pm);
            Drawable icon = info.loadIcon(pm);

            LinearLayout appLayout = new LinearLayout(this);
            appLayout.setOrientation(LinearLayout.VERTICAL);
            appLayout.setGravity(Gravity.CENTER);
            
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics()),
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()), 0);
            appLayout.setLayoutParams(lp);

            ImageView iv = new ImageView(this);
            int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
            iv.setLayoutParams(new LinearLayout.LayoutParams(size, size));
            iv.setImageDrawable(icon);

            TextView tv = new TextView(this);
            tv.setText(label);
            tv.setTextSize(10f);
            tv.setGravity(Gravity.CENTER);
            tv.setSingleLine(true);
            tv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            
            appLayout.addView(iv);
            appLayout.addView(tv);

            appLayout.setOnClickListener(v -> {
                if (!selectedMethod.equals("UPI")) {
                    selectMethod("UPI");
                }
                selectedUpiPackage = packageName;
                updatePayButtonText();
                
                for (int i = 0; i < binding.llUpiAppsContainer.getChildCount(); i++) {
                    View child = binding.llUpiAppsContainer.getChildAt(i);
                    child.setAlpha(0.5f);
                }
                appLayout.setAlpha(1.0f);
            });

            if (packageName.equals(selectedUpiPackage)) {
                appLayout.setAlpha(1.0f);
            } else {
                appLayout.setAlpha(0.5f);
            }

            binding.llUpiAppsContainer.addView(appLayout);
        }
    }

    private void fetchDetailsAndCalculate() {
        String shopId = viewModel.getCartShopId();
        if (shopId == null) return;

        if (isAddressless) {
            deliveryFee = 100.0; // Flat fee for addressless
            updateSummary();
            return;
        }

        viewModel.getShopById(shopId).observe(this, shopResult -> {
            if (shopResult.status == NetworkResult.Status.SUCCESS && shopResult.data != null) {
                Shop shop = shopResult.data;
                viewModel.getAddressById(addressId).observe(this, addrResult -> {
                    if (addrResult.status == NetworkResult.Status.SUCCESS && addrResult.data != null) {
                        calculateDeliveryFee(shop, addrResult.data);
                    }
                });
            }
        });
    }

    private void calculateDeliveryFee(Shop shop, Address address) {
        if (shop.getLatitude() == 0 || shop.getLongitude() == 0 || 
            address.getLatitude() == 0 || address.getLongitude() == 0) {
            deliveryFee = 50.0; // Flat fallback fee if coordinates are missing
            updateSummary();
            return;
        }

        float[] results = new float[1];
        android.location.Location.distanceBetween(
                shop.getLatitude(), shop.getLongitude(),
                address.getLatitude(), address.getLongitude(),
                results
        );
        double distanceKm = results[0] / 1000.0;
        
        // Strict Dynamic Radius Enforcement
        double allowedRadius = shop.getDeliveryRadiusKm() > 0 ? shop.getDeliveryRadiusKm() : 5.0;
        
        if (distanceKm > allowedRadius) {
            binding.btnPayNow.setEnabled(false);
            binding.btnPayNow.setText(String.format("Shop radius is %.1fkm. You are at %.1fkm", allowedRadius, distanceKm));
            binding.btnPayNow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY));
        } else {
            binding.btnPayNow.setEnabled(true);
            updatePayButtonText();
            binding.btnPayNow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.cart_primary)));
        }

        if (deliverySlot != null && deliverySlot.contains("SAME-DAY")) {
            final int prepTime = shop.getPreparationMinutes() > 0 ? shop.getPreparationMinutes() : 60;
            binding.tvEstimatedEta.setVisibility(View.VISIBLE);
            binding.tvEstimatedEta.setText("Calculating real-time ETA...");
            
            // Task 8.3: OSRM Routing API for real traffic ETA
            new Thread(() -> {
                try {
                    String urlString = "https://router.project-osrm.org/route/v1/driving/" 
                            + shop.getLongitude() + "," + shop.getLatitude() + ";" 
                            + address.getLongitude() + "," + address.getLatitude();
                    java.net.URL url = new java.net.URL(urlString);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    
                    if (conn.getResponseCode() == 200) {
                        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) response.append(line);
                        in.close();
                        
                        org.json.JSONObject json = new org.json.JSONObject(response.toString());
                        org.json.JSONArray routes = json.getJSONArray("routes");
                        if (routes.length() > 0) {
                            double durationSec = routes.getJSONObject(0).getDouble("duration");
                            int travelTimeMins = (int) Math.ceil(durationSec / 60.0);
                            int totalEtaMins = prepTime + travelTimeMins;
                            
                            runOnUiThread(() -> {
                                if (isDestroyed() || isFinishing()) return;
                                binding.tvEstimatedEta.setText(String.format("Estimated ETA: ~%d mins (Live Traffic)", totalEtaMins));
                            });
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                // Fallback to heuristic
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    int travelTimeMins = (int) Math.ceil(distanceKm * 4);
                    int totalEtaMins = prepTime + travelTimeMins;
                    binding.tvEstimatedEta.setText(String.format("Estimated ETA: ~%d mins", totalEtaMins));
                });
            }).start();
        } else {
            binding.tvEstimatedEta.setVisibility(View.GONE);
        }

        deliveryFee = Math.max(20.0, distanceKm * 10.0);
        updateSummary();
    }

    private void updatePayButtonText() {
        if (!binding.btnPayNow.isEnabled()) return;

        String formattedTotal = CurrencyFormatter.format(totalAmount > 0 ? totalAmount : 0);
        switch (selectedMethod) {
            case "CARD":
                binding.btnPayNow.setText("Pay " + formattedTotal + " via Card");
                break;
            case "UPI":
                if (selectedUpiPackage != null) {
                    PackageManager pm = getPackageManager();
                    try {
                        CharSequence label = pm.getApplicationInfo(selectedUpiPackage, 0).loadLabel(pm);
                        binding.btnPayNow.setText("Pay " + formattedTotal + " via " + label);
                    } catch (PackageManager.NameNotFoundException e) {
                        binding.btnPayNow.setText("Pay " + formattedTotal + " via UPI");
                    }
                } else {
                    binding.btnPayNow.setText("Pay " + formattedTotal + " via UPI");
                }
                break;
            case "WALLET":
                binding.btnPayNow.setText("Pay " + formattedTotal + " via Wallet");
                break;
            case "BANK":
                binding.btnPayNow.setText("Pay " + formattedTotal + " via Bank");
                break;
            case "COD":
                if (totalAmount > 500) {
                    binding.btnPayNow.setText("Pay ₹100 Advance (Partial COD)");
                } else {
                    binding.btnPayNow.setText("Place Order (Cash on Delivery)");
                }
                break;
        }
    }

    private void updateSummary() {
        double subtotal = viewModel.getCartTotal();
        totalAmount = subtotal + deliveryFee + 10.0; // Including platform fee
        binding.tvTotalAmount.setText(CurrencyFormatter.format(totalAmount));
        
        // Update COD text if total > 500
        if (totalAmount > 500) {
            binding.tvCodTitle.setText("Partial COD (₹100 Advance)");
            binding.tvCodSubtitle.setText("Pay ₹100 now, rest on delivery");
            binding.tvCodTitle.setTextColor(android.graphics.Color.parseColor("#A82D47")); // Highlight
        } else {
            binding.tvCodTitle.setText("Cash on Delivery");
            binding.tvCodSubtitle.setText("Pay when flowers arrive");
            binding.tvCodTitle.setTextColor(ContextCompat.getColor(this, com.google.android.material.R.color.m3_sys_color_dynamic_light_on_surface));
        }

        updatePayButtonText();
        
        // Breakdown in accordion
        View breakdown = binding.getRoot().findViewById(R.id.llOrderDetails);
        if (breakdown != null) {
            TextView tvSub = breakdown.findViewById(R.id.tvSubtotal);
            TextView tvDel = breakdown.findViewById(R.id.tvDeliveryFee);
            if (tvSub != null) tvSub.setText(CurrencyFormatter.format(subtotal));
            if (tvDel != null) tvDel.setText(CurrencyFormatter.format(deliveryFee));
        }

        List<CartItem> items = viewModel.getCartItems().getValue();
        if (items != null && !items.isEmpty()) {
            String briefText = items.size() + "x '" + items.get(0).getProduct().getName() + "'";
            if (items.size() > 1) briefText += " + " + (items.size() - 1) + " items";
            binding.tvOrderSummaryBrief.setText(briefText);
        }

        binding.llOrderSummaryClickable.setOnClickListener(v -> {
            boolean isVisible = binding.llOrderDetails.getVisibility() == View.VISIBLE;
            binding.llOrderDetails.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            binding.ivSummaryArrow.animate().rotation(isVisible ? 0 : 180).start();
        });
    }

    private String getPaymentMethodName() {
        switch (selectedMethod) {
            case "UPI": return "Online UPI";
            case "WALLET": return "Digital Wallet";
            case "BANK": return "Bank Transfer";
            case "COD": return "Cash on Delivery";
            default: return "Credit/Debit Card";
        }
    }

    private void handlePlaceOrder() {
        binding.btnPayNow.setEnabled(false);
        binding.btnPayNow.setText("Processing Payment...");
        
        String shopId = viewModel.getCartShopId();
        viewModel.getProductsByShop(shopId).observe(this, result -> {
            if (result.status == com.bloom.customer.util.NetworkResult.Status.SUCCESS && result.data != null) {
                List<com.bloom.customer.data.model.Product> liveProducts = result.data;
                List<CartItem> cartItems = viewModel.getCartItems().getValue();
                
                if (cartItems != null) {
                    for (CartItem item : cartItems) {
                        for (com.bloom.customer.data.model.Product live : liveProducts) {
                            if (item.getProduct().getId().equals(live.getId())) {
                                if (live.getStockCount() < item.getQuantity()) {
                                    binding.btnPayNow.setEnabled(true);
                                    updatePayButtonText();
                                    new androidx.appcompat.app.AlertDialog.Builder(this)
                                        .setTitle("Item Sold Out")
                                        .setMessage("Sorry, '" + live.getName() + "' just sold out. Please remove it from your cart or reduce the quantity.")
                                        .setPositiveButton("OK", null)
                                        .show();
                                    return; // Abort
                                }
                            }
                        }
                    }
                }
                
                lockInventoryAndProceed();
            } else {
                binding.btnPayNow.setEnabled(true);
                updatePayButtonText();
                showSnackbar("Failed to verify inventory");
            }
        });
    }

    private void lockInventoryAndProceed() {
        binding.getRoot().postDelayed(() -> {
            if (isDestroyed() || isFinishing()) return;
            if ("COD".equals(selectedMethod) && totalAmount > 500) {
                binding.btnPayNow.setText("Processing Advance...");
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Partial COD Required")
                    .setMessage("For orders above ₹500, a non-refundable advance of ₹100 is required via UPI to prevent fake orders.")
                    .setPositiveButton("Pay ₹100 via UPI", (dialog, which) -> {
                        showSnackbar("Mock UPI Payment Successful!");
                        createPendingOrder();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        releaseInventoryLock();
                    })
                    .setCancelable(false)
                    .show();
            } else {
                createPendingOrder();
            }
        }, 1000);
    }

    private void releaseInventoryLock() {
        binding.getRoot().postDelayed(() -> {
            if (isDestroyed() || isFinishing()) return;
            binding.btnPayNow.setEnabled(true);
            updatePayButtonText();
            showSnackbar("Inventory lock released");
        }, 800);
    }

    private String pendingOrderId;

    private void createPendingOrder() {
        Order order = new Order();
        order.setUserId(SessionManager.getInstance(this).getUserId());
        order.setShopId(viewModel.getCartShopId());
        order.setAddressId(addressId);
        order.setTotalAmount(totalAmount);
        order.setDeliverySlot(deliverySlot);
        order.setStatus("placed");
        
        // Generate Delivery OTP
        String otp = String.format("%04d", new java.util.Random().nextInt(10000));
        order.setDeliveryOtp(otp);
        
        // 2-step checkout: always set to pending first to prevent ghost charges
        order.setPaymentStatus("pending");
        
        // Secret Admirer Mode
        boolean isAnonymous = binding.switchAnonymous.isChecked();
        order.setAnonymous(isAnonymous);
        
        // Address-less mode
        order.setAddressless(isAddressless);
        if (isAddressless) {
            order.setRecipientName(recipientName);
            order.setRecipientPhone(recipientPhone);
            order.setAddressId(null); // Just to be safe, nullify the dummy ID in the DB
        }
        
        order.setBouquetSubtotal(viewModel.getCartTotal());
        order.setDeliveryFee(deliveryFee);

        List<CartItem> cartItems = viewModel.getCartItems().getValue();
        List<OrderItem> orderItems = new ArrayList<>();

        if (cartItems != null) {
            for (CartItem ci : cartItems) {
                OrderItem oi = new OrderItem();
                oi.setProductId(ci.getProduct().getId());
                oi.setQuantity(ci.getQuantity());
                oi.setUnitPrice(ci.getProduct().getPrice());
                oi.setSize(ci.getSize());
                oi.setCardMessage(ci.getCardMessage());
                if (ci.getMediaUrl() != null) {
                    // MOCK UPLOAD: Instead of uploading to Supabase Storage, we simulate a returned URL
                    oi.setMediaUrl("https://mock-supabase-storage.com/gift-media/" + System.currentTimeMillis() + ".mp4");
                }
                orderItems.add(oi);
            }
        }
        order.setItems(orderItems);

        binding.progressBar.setVisibility(View.VISIBLE);

        viewModel.placeOrder(order).observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            if (result.status == com.bloom.customer.util.NetworkResult.Status.SUCCESS && result.data != null) {
                pendingOrderId = result.data.getId();
                proceedToActualPayment();
            } else {
                binding.btnPayNow.setEnabled(true);
                updatePayButtonText();
                showSnackbar("Failed to initialize order");
            }
        });
    }
    
    private void proceedToActualPayment() {
        if (selectedMethod.equals("COD")) {
            // COD is already pending, just finish
            finishOrderConfirmation();
            return;
        }
        
        if (selectedMethod.equals("UPI") && selectedUpiPackage != null) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("upi://pay?pa=bloom@okaxis&pn=Bloom%20Flowers&am=" + totalAmount + "&cu=INR"));
                intent.setPackage(selectedUpiPackage);
                upiPaymentLauncher.launch(intent);
                return;
            } catch (Exception e) {
                showSnackbar("Could not launch UPI app, completing order as pending");
                finishOrderConfirmation(); // fallback for testing
            }
        } else {
            // Mocking Razorpay/Card success
            showSnackbar("Payment successful via " + selectedMethod);
            markOrderAsPaid();
        }
    }
    
    private void markOrderAsPaid() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        viewModel.updateOrderPaymentStatus(pendingOrderId, "paid").observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            if (result.status == com.bloom.customer.util.NetworkResult.Status.SUCCESS) {
                finishOrderConfirmation();
            } else {
                showSnackbar("Order placed but payment status sync failed.");
                finishOrderConfirmation();
            }
        });
    }
    
    private void finishOrderConfirmation() {
        if (isAddressless && recipientPhone != null) {
            String senderName = binding.switchAnonymous.isChecked() ? "A Secret Admirer" : "A friend";
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("WhatsApp Notification Sent")
                .setMessage("Simulating Twilio WhatsApp API:\n\nTo: " + recipientPhone + "\nMessage: 'Hi! " + senderName + " has sent you a surprise gift via Bloom. Tap here to securely provide your delivery address.'")
                .setPositiveButton("OK", (dialog, which) -> proceedToConfirmationScreen())
                .setCancelable(false)
                .show();
        } else {
            proceedToConfirmationScreen();
        }
    }
    
    private void proceedToConfirmationScreen() {
        viewModel.clearCart();
        Intent intent = new Intent(this, OrderConfirmationActivity.class);
        intent.putExtra("order_id", pendingOrderId);
        intent.putExtra("shop_name", "Florist");
        intent.putExtra("payment_method", getPaymentMethodName());
        startActivity(intent);
        finishAffinity();
    }
}
