package com.bloom.customer.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.graphics.Insets;
import android.content.res.Configuration;
import android.view.ViewGroup;

import com.bloom.R;
import com.bloom.customer.ui.shop.ShopDetailActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import android.net.Uri;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class ShopMapActivity extends AppCompatActivity {

    private WebView mapWebView;
    private JSONArray mockShops;
    private JSONArray allMockShops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Premium transparent status bar (Edge-to-Edge)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        
        setContentView(R.layout.activity_shop_map);

        mapWebView = findViewById(R.id.mapWebView);
        View cvBack = findViewById(R.id.cvBack);
        View btnRecenter = findViewById(R.id.btnRecenter);
        
        cvBack.setOnClickListener(v -> finish());
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            ViewGroup.MarginLayoutParams backParams = (ViewGroup.MarginLayoutParams) cvBack.getLayoutParams();
            backParams.topMargin = systemBars.top + (int) (16 * getResources().getDisplayMetrics().density);
            cvBack.setLayoutParams(backParams);
            
            ViewGroup.MarginLayoutParams recenterParams = (ViewGroup.MarginLayoutParams) btnRecenter.getLayoutParams();
            recenterParams.bottomMargin = systemBars.bottom + (int) (40 * getResources().getDisplayMetrics().density);
            btnRecenter.setLayoutParams(recenterParams);
            
            return insets;
        });

        btnRecenter.setOnClickListener(v -> {
            mapWebView.evaluateJavascript("setCenter(19.0760, 72.8777, 12);", null);
        });
        
        findViewById(R.id.btnZoomIn).setOnClickListener(v -> {
            mapWebView.evaluateJavascript("map.zoomIn();", null);
        });

        findViewById(R.id.btnZoomOut).setOnClickListener(v -> {
            mapWebView.evaluateJavascript("map.zoomOut();", null);
        });
        
        findViewById(R.id.cvSearchBar).setOnClickListener(v -> {
            Toast.makeText(this, "Opening Search...", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.chipNearby).setOnClickListener(v -> filterShops("Nearby"));
        findViewById(R.id.chipTopRated).setOnClickListener(v -> filterShops("Top Rated"));
        findViewById(R.id.chipDelivery).setOnClickListener(v -> filterShops("Delivery"));

        setupMap();
        generateMockShops();
    }

    private void setupMap() {
        WebSettings webSettings = mapWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        mapWebView.addJavascriptInterface(new WebAppInterface(), "AndroidBridge");
        
        mapWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Inject shops after page loads
                if (mockShops != null) {
                    mapWebView.evaluateJavascript("addShops('" + mockShops.toString().replace("'", "\\'") + "');", null);
                }
                
                // Inject Dark Mode if system is in dark mode
                int uiMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                if (uiMode == Configuration.UI_MODE_NIGHT_YES) {
                    mapWebView.evaluateJavascript("document.body.classList.add('dark-mode');", null);
                }
            }
        });
        
        mapWebView.setWebChromeClient(new WebChromeClient());
        mapWebView.loadUrl("file:///android_asset/shop_map.html");
    }

    private void generateMockShops() {
        try {
            mockShops = new JSONArray();
            
            JSONObject shop1 = new JSONObject();
            shop1.put("id", "shop_1");
            shop1.put("name", "Bloom & Wild");
            shop1.put("imageUrl", "https://images.unsplash.com/photo-1598889154784-0a6728ebcba5?auto=format&fit=crop&q=80&w=200");
            shop1.put("lat", 19.0820);
            shop1.put("lng", 72.8810);
            shop1.put("rating", 4.8);
            shop1.put("offerText", "20% OFF");
            shop1.put("address", "Lower Parel, Mumbai • 2.5 km away");
            mockShops.put(shop1);
            
            JSONObject shop2 = new JSONObject();
            shop2.put("id", "shop_2");
            shop2.put("name", "The Floral Studio");
            shop2.put("imageUrl", "https://images.unsplash.com/photo-1558350315-8aa00e8e4590?auto=format&fit=crop&q=80&w=200");
            shop2.put("lat", 19.0700);
            shop2.put("lng", 72.8700);
            shop2.put("rating", 4.2);
            shop2.put("offerText", "");
            shop2.put("address", "Bandra West, Mumbai • 4.1 km away");
            mockShops.put(shop2);

            JSONObject shop3 = new JSONObject();
            shop3.put("id", "shop_3");
            shop3.put("name", "Petals & Co.");
            shop3.put("imageUrl", "https://images.unsplash.com/photo-1563241598-a24ce245ef45?auto=format&fit=crop&q=80&w=200");
            shop3.put("lat", 19.0600);
            shop3.put("lng", 72.8900);
            shop3.put("rating", 4.9);
            shop3.put("offerText", "FREE DELIVERY");
            shop3.put("address", "Dadar, Mumbai • 1.2 km away");
            mockShops.put(shop3);
            
            allMockShops = new JSONArray(mockShops.toString());
            
        } catch (Exception e) {
            timber.log.Timber.e(e, "Error creating mock shops JSON");
        }
    }

    private void filterShops(String filterType) {
        try {
            if (allMockShops == null) return;
            
            JSONArray filtered = new JSONArray();
            for (int i = 0; i < allMockShops.length(); i++) {
                JSONObject shop = allMockShops.getJSONObject(i);
                
                if ("Top Rated".equals(filterType)) {
                    if (shop.getDouble("rating") >= 4.8) {
                        filtered.put(shop);
                    }
                } else if ("Delivery".equals(filterType)) {
                    String offerText = shop.optString("offerText", "");
                    if (offerText.toLowerCase().contains("delivery")) {
                        filtered.put(shop);
                    }
                } else {
                    filtered.put(shop);
                }
            }
            
            mockShops = filtered;
            mapWebView.evaluateJavascript("addShops('" + mockShops.toString().replace("'", "\\'") + "');", null);
            
        } catch (Exception e) {
            timber.log.Timber.e(e, "Error filtering mock shops");
        }
    }

    private class WebAppInterface {
        @JavascriptInterface
        public void onShopClicked(String shopId) {
            runOnUiThread(() -> showShopPreview(shopId));
        }
    }

    private void showShopPreview(String shopId) {
        try {
            JSONObject selectedShop = null;
            for (int i = 0; i < mockShops.length(); i++) {
                if (mockShops.getJSONObject(i).getString("id").equals(shopId)) {
                    selectedShop = mockShops.getJSONObject(i);
                    break;
                }
            }

            if (selectedShop == null) return;

            BottomSheetDialog dialog = new BottomSheetDialog(this, com.google.android.material.R.style.Theme_Design_Light_BottomSheetDialog);
            View view = getLayoutInflater().inflate(R.layout.bottom_sheet_shop_preview, null);
            dialog.setContentView(view);
            
            // Fix bottom sheet background transparency so MaterialCardView shows rounded corners
            View bottomSheet = (View) view.getParent();
            bottomSheet.setBackgroundResource(android.R.color.transparent);

            // Ensure it's fully expanded by default for cinematic effect
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);

            ImageView ivShop = view.findViewById(R.id.ivShopPreview);
            TextView tvName = view.findViewById(R.id.tvShopNamePreview);
            TextView tvRatingNum = view.findViewById(R.id.tvShopRatingNumber);
            TextView tvDescription = view.findViewById(R.id.tvShopDescriptionPreview);
            TextView tvMeta = view.findViewById(R.id.tvShopMetaPreview);
            TextView tvReviewCount = view.findViewById(R.id.tvShopReviewCount);

            tvName.setText(selectedShop.getString("name"));
            tvRatingNum.setText(String.valueOf(selectedShop.getDouble("rating")));
            
            // Generate some mock review counts based on rating for flavor
            int mockReviews = (int)(selectedShop.getDouble("rating") * 25 + Math.random() * 50);
            tvReviewCount.setText("(" + mockReviews + " reviews)");
            
            // Map the previous 'address' string to the meta string and description
            String address = selectedShop.getString("address");
            tvDescription.setText("London's premier boutique florist, crafting bespoke bouquets and floral arrangements. Mon-Sat 9am-6pm.");
            
            String offerText = selectedShop.optString("offerText", "");
            String metaText = offerText.isEmpty() ? address : offerText + " · " + address;
            tvMeta.setText(metaText);

            Glide.with(this)
                 .load(selectedShop.getString("imageUrl"))
                 .centerCrop()
                 .into(ivShop);

            String finalShopId = shopId;
            JSONObject finalSelectedShop = selectedShop;
            
            view.findViewById(R.id.btnVisitShop).setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(this, ShopDetailActivity.class);
                intent.putExtra("shop_json", finalSelectedShop.toString());
                startActivity(intent);
            });
            
            view.findViewById(R.id.btnDirections).setOnClickListener(v -> {
                dialog.dismiss();
                try {
                    // Mock user location start (South Mumbai)
                    double startLat = 18.9220; 
                    double startLng = 72.8347;
                    double shopLat = finalSelectedShop.getDouble("lat");
                    double shopLng = finalSelectedShop.getDouble("lng");
                    mapWebView.evaluateJavascript("drawRealtimeRoute(" + startLat + ", " + startLng + ", " + shopLat + ", " + shopLng + ");", null);
                    
                    // Live Movement Simulator
                    android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                    final double[] currentLat = {startLat};
                    final double[] currentLng = {startLng};
                    
                    // Calculate step sizes to reach destination in ~20 steps
                    double latStep = (shopLat - startLat) / 20.0;
                    double lngStep = (shopLng - startLng) / 20.0;
                    
                    Runnable runnable = new Runnable() {
                        int step = 0;
                        @Override
                        public void run() {
                            if (step < 20) {
                                currentLat[0] += latStep;
                                currentLng[0] += lngStep;
                                mapWebView.evaluateJavascript("updateUserLocation(" + currentLat[0] + ", " + currentLng[0] + ");", null);
                                step++;
                                handler.postDelayed(this, 1500); // update every 1.5s
                            }
                        }
                    };
                    handler.postDelayed(runnable, 1500);
                    
                } catch (Exception e) {
                    timber.log.Timber.e(e, "Error parsing shop JSON");
                }
            });

            view.findViewById(R.id.btnCall).setOnClickListener(v -> {
                dialog.dismiss();
                try {
                    Intent intent = new Intent(this, InAppCallActivity.class);
                    intent.putExtra("shop_name", finalSelectedShop.getString("name"));
                    intent.putExtra("shop_image", finalSelectedShop.getString("imageUrl"));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(ShopMapActivity.this, "Unable to start call", Toast.LENGTH_SHORT).show();
                }
            });

            dialog.show();

        } catch (Exception e) {
            timber.log.Timber.e(e, "Error finding nearest shop");
        }
    }
}
