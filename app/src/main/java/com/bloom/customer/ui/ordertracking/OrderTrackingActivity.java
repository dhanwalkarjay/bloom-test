package com.bloom.customer.ui.ordertracking;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bloom.customer.data.model.Order;
import com.bloom.customer.data.repository.OrderRepository;
import com.bloom.customer.data.local.LocationHelper;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityOrderTrackingBinding;

public class OrderTrackingActivity extends AppCompatActivity {

    private ActivityOrderTrackingBinding binding;
    private OrderTrackingViewModel viewModel;
    private LocationHelper locationHelper;
    private String orderId;
    private boolean isMapReady = false;
    private Order pendingOrderData = null;
    private boolean isOtpRevealed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderId = getIntent().getStringExtra("order_id");
        viewModel = new ViewModelProvider(this).get(OrderTrackingViewModel.class);
        locationHelper = new LocationHelper(this);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCancelOrder.setOnClickListener(v -> cancelOrder());

        binding.btnHelp.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:18001234567"));
            startActivity(intent);
        });

        binding.btnCall.setOnClickListener(v -> {
            if (pendingOrderData != null && pendingOrderData.getRiderPhone() != null) {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
                intent.setData(android.net.Uri.parse("tel:" + pendingOrderData.getRiderPhone()));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Rider phone not available", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnChat.setOnClickListener(v -> {
            if (pendingOrderData != null && pendingOrderData.getRiderPhone() != null) {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse("sms:" + pendingOrderData.getRiderPhone()));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Rider phone not available", Toast.LENGTH_SHORT).show();
            }
        });

        setupMap();
        setupObservers();
        
        if (orderId != null) {
            viewModel.startTracking(orderId);
            fetchOrderData();
        }
    }

    private void fetchOrderData() {
        OrderRepository repository = new OrderRepository(this);
        repository.getOrderById(orderId).observe(this, result -> {
            if (result.status == NetworkResult.Status.SUCCESS && result.data != null) {
                pendingOrderData = result.data;
                if (isMapReady) {
                    updateMapMarkers(result.data);
                }
                updateTimeline(result.data.getStatus(), result.data);
            }
        });
    }

    private void cancelOrder() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel this order?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    viewModel.cancelOrder(orderId).observe(this, result -> {
                        if (result.status == NetworkResult.Status.SUCCESS) {
                            Toast.makeText(this, "Order cancelled", Toast.LENGTH_SHORT).show();
                            finish();
                        } else if (result.status == NetworkResult.Status.ERROR) {
                            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void updateMapMarkers(Order order) {
        if (order == null) return;
        
        double sLat = 21.1458, sLng = 79.0882; 
        double uLat = 21.1358, uLng = 79.0820; 
        
        if (order.getShop() != null && order.getShop().getLatitude() != 0) {
            sLat = order.getShop().getLatitude();
            sLng = order.getShop().getLongitude();
        }
        
        if (order.getAddress() != null && order.getAddress().getLatitude() != 0) {
            uLat = order.getAddress().getLatitude();
            uLng = order.getAddress().getLongitude();
        }

        final double finalSLat = sLat;
        final double finalSLng = sLng;
        final double finalULat = uLat;
        final double finalULng = uLng;
        final double finalRLat = order.getRiderLat();
        final double finalRLng = order.getRiderLng();
        final String shopName = (order.getShop() != null && order.getShop().getName() != null) 
                                ? order.getShop().getName().replace("'", "\\'") : "Florist";

        int state = 1;
        if (order.getStatus() != null) {
            String lowerStatus = order.getStatus().toLowerCase();
            if (lowerStatus.contains("deliver") || lowerStatus.contains("out")) {
                state = 2;
            }
        }

        final int finalState = state;
        
        // If testing state 2 but rider lat/lng is 0, provide dummy rider location near shop
        final double rLat = (state == 2 && finalRLat == 0) ? (sLat - 0.005) : finalRLat;
        final double rLng = (state == 2 && finalRLng == 0) ? (sLng - 0.005) : finalRLng;

        if (locationHelper.hasLocationPermission()) {
            locationHelper.getLastLocation(location -> {
                double liveLat = location != null ? location.getLatitude() : finalULat;
                double liveLng = location != null ? location.getLongitude() : finalULng;
                injectMapJs(finalState, finalSLat, finalSLng, liveLat, liveLng, rLat, rLng, shopName);
            });
        } else {
            injectMapJs(finalState, finalSLat, finalSLng, finalULat, finalULng, rLat, rLng, shopName);
        }
    }

    private void injectMapJs(int state, double sLat, double sLng, double uLat, double uLng, double rLat, double rLng, String shopName) {
        String js = String.format(java.util.Locale.US, "if(window.drawMapState) { window.drawMapState(%d, %f, %f, %f, %f, %f, %f, '%s'); }",
                state, sLat, sLng, uLat, uLng, rLat, rLng, shopName);
        binding.mapWebView.post(() -> binding.mapWebView.evaluateJavascript(js, null));
    }

    private void setupMap() {
        WebSettings webSettings = binding.mapWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        
        binding.mapWebView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                case android.view.MotionEvent.ACTION_MOVE:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        });
        
        String html = "<html><head>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.7.1/dist/leaflet.css' />" +
                "<script src='https://unpkg.com/leaflet@1.7.1/dist/leaflet.js'></script>" +
                "<style>#map { height: 100%; width: 100%; margin: 0; padding: 0; }</style>" +
                "</head><body>" +
                "<div id='map'></div>" +
                "<script>" +
                "var map = L.map('map', {zoomControl: false}).setView([21.1458, 79.0882], 14);" +
                "L.tileLayer('https://{s}.basemaps.cartocdn.com/light_nolabels/{z}/{x}/{y}{r}.png').addTo(map);" +
                "map.on('dragstart zoomstart', function() { if (window.recenterTimer) clearTimeout(window.recenterTimer); });" +
                "map.on('dragend zoomend', function() { " +
                "  if (window.recenterTimer) clearTimeout(window.recenterTimer);" +
                "  window.recenterTimer = setTimeout(function() {" +
                "    if (window.currentBounds) map.flyToBounds(window.currentBounds, {padding: [50, 50], duration: 0.5, easeLinearity: 0.1});" +
                "  }, 100);" +
                "});" +
                "function getBezierPoints(lat1, lng1, lat2, lng2) {" +
                "  var points = [];" +
                "  var latDiff = lat2 - lat1; var lngDiff = lng2 - lng1;" +
                "  var midLat = lat1 + latDiff/2 - lngDiff*0.2;" +
                "  var midLng = lng1 + lngDiff/2 + latDiff*0.2;" +
                "  for (var t = 0; t <= 1; t += 0.05) {" +
                "    var lat = (1-t)*(1-t)*lat1 + 2*(1-t)*t*midLat + t*t*lat2;" +
                "    var lng = (1-t)*(1-t)*lng1 + 2*(1-t)*t*midLng + t*t*lng2;" +
                "    points.push([lat, lng]);" +
                "  }" +
                "  return points;" +
                "}" +
                "function drawMapState(state, sLat, sLng, uLat, uLng, rLat, rLng, shopName) {" +
                "  map.eachLayer(function(l){ if(l instanceof L.Marker || l instanceof L.Polyline) map.removeLayer(l); });" +
                "  var shopIcon = L.divIcon({className: 'custom-icon', html: '<div style=\"display:flex; flex-direction:column; align-items:center;\"><div style=\"background:#333333;width:28px;height:28px;border-radius:14px;display:flex;justify-content:center;align-items:center;border:2px solid white;box-shadow:0 2px 4px rgba(0,0,0,0.2);\"><img src=\"data:image/svg+xml;utf8,<svg xmlns=\\'http://www.w3.org/2000/svg\\' viewBox=\\'0 0 24 24\\' fill=\\'%23FFFFFF\\'><path d=\\'M20 4H4v2h16V4zm1 10v4h-2v-4h-4v4h-2v-4h-4v4H7v-4H5v4H3v-4c0-1.1.9-2 2-2h14c1.1 0 2 .9 2 2zM5 8v2h14V8H5z\\'/></svg>\" width=\"16\" height=\"16\"/></div><div style=\"background:white;padding:2px 6px;border-radius:8px;font-family:sans-serif;font-size:10px;font-weight:bold;margin-top:4px;box-shadow:0 1px 3px rgba(0,0,0,0.2);\">'+shopName+'</div></div>', iconSize: [100, 50], iconAnchor: [50, 14]});" +
                "  var userIcon = L.divIcon({className: 'custom-icon', html: '<div style=\"background:#4A7C59;width:28px;height:28px;border-radius:14px;display:flex;justify-content:center;align-items:center;border:2px solid white;box-shadow:0 2px 4px rgba(0,0,0,0.2);\"><img src=\"data:image/svg+xml;utf8,<svg xmlns=\\'http://www.w3.org/2000/svg\\' viewBox=\\'0 0 24 24\\' fill=\\'%23FFFFFF\\'><path d=\\'M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z\\'/></svg>\" width=\"16\" height=\"16\"/></div>', iconSize: [28, 28], iconAnchor: [14, 14]});" +
                "  var riderIcon = L.divIcon({className: 'custom-icon', html: '<div style=\"background:#A82D47;width:32px;height:32px;border-radius:16px;display:flex;justify-content:center;align-items:center;border:2px solid white;box-shadow:0 4px 8px rgba(168,45,71,0.4);\"><img src=\"data:image/svg+xml;utf8,<svg xmlns=\\'http://www.w3.org/2000/svg\\' viewBox=\\'0 0 24 24\\' fill=\\'%23FFFFFF\\'><path d=\\'M15.5 5.5c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zM5 12c-2.8 0-5 2.2-5 5s2.2 5 5 5 5-2.2 5-5-2.2-5-5-5zm0 8.5c-1.9 0-3.5-1.6-3.5-3.5s1.6-3.5 3.5-3.5 3.5 1.6 3.5 3.5-1.6 3.5-3.5 3.5zm5.8-10l2.4-2.4.8.8c1.3 1.3 3 2.1 5.1 2.1V9c-1.5 0-2.7-.6-3.6-1.5l-1.9-1.9c-.5-.4-1-.6-1.6-.6s-1.1.2-1.4.6L7.8 8.4c-.4.4-.6.9-.6 1.4 0 .6.2 1.1.6 1.4L11 14v5h1.5v-5.5l-1.7-1.7V10.5z\\'/></svg>\" width=\"20\" height=\"20\"/></div>', iconSize: [32, 32], iconAnchor: [16, 16]});" +
                "  L.marker([sLat, sLng], {icon: shopIcon}).addTo(map);" +
                "  L.marker([uLat, uLng], {icon: userIcon}).addTo(map);" +
                "  var bounds = L.latLngBounds([sLat, sLng], [uLat, uLng]);" +
                "  if (state === 1) {" +
                "    window.currentBounds = bounds;" +
                "    L.polyline(getBezierPoints(sLat, sLng, uLat, uLng), {color: '#999999', weight: 2.5, dashArray: '6, 6'}).addTo(map);" +
                "    map.flyToBounds(bounds, {padding: [50, 50], duration: 1.5, easeLinearity: 0.1});" +
                "  } else {" +
                "    L.marker([rLat, rLng], {icon: riderIcon}).addTo(map);" +
                "    bounds.extend([rLat, rLng]);" +
                "    window.currentBounds = bounds;" +
                "    fetch('https://router.project-osrm.org/route/v1/driving/'+rLng+','+rLat+';'+uLng+','+uLat+'?geometries=geojson').then(r=>r.json()).then(data=>{" +
                "      if(data.routes && data.routes[0]) {" +
                "        var coords = data.routes[0].geometry.coordinates.map(c => [c[1], c[0]]);" +
                "        L.polyline(coords, {color: '#A82D47', weight: 4}).addTo(map);" +
                "      }" +
                "      map.flyToBounds(bounds, {padding: [50, 50], duration: 1.5, easeLinearity: 0.1});" +
                "    }).catch(e => {" +
                "      L.polyline([[rLat, rLng], [uLat, uLng]], {color: '#A82D47', weight: 4}).addTo(map);" +
                "      map.flyToBounds(bounds, {padding: [50, 50], duration: 1.5, easeLinearity: 0.1});" +
                "    });" +
                "  }" +
                "}" +
                "</script></body></html>";

        binding.mapWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                isMapReady = true;
                if (pendingOrderData != null) {
                    updateMapMarkers(pendingOrderData);
                }
            }
        });
        binding.mapWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    private void setupObservers() {
        viewModel.getOrderStatus().observe(this, status -> {
            if (status != null) {
                // Fetch full order data for map updates when status changes
                fetchOrderData();
            }
        });
    }

    private void updateTimeline(String status, Order order) {
        if (status == null) return;
        
        int step = 1;
        String lowerStatus = status.toLowerCase();
        if (lowerStatus.contains("deliver")) step = 5;
        else if (lowerStatus.contains("out")) step = 4;
        else if (lowerStatus.contains("prepar")) step = 3;
        else if (lowerStatus.contains("confirm")) step = 2;

        // Task 1: Cancel only if not prepared
        if (step < 3 && !"cancelled".equalsIgnoreCase(status)) {
            binding.btnCancelOrder.setVisibility(View.VISIBLE);
        } else {
            binding.btnCancelOrder.setVisibility(View.GONE);
        }

        float active = 1.0f;
        float inactive = 0.4f;
        
        animateStep(binding.rlStep1, step >= 1 ? active : inactive, 0);
        animateStep(binding.rlStep2, step >= 2 ? active : inactive, 100);
        animateStep(binding.rlStep3, step >= 3 ? active : inactive, 200);
        animateStep(binding.cvStep4, step >= 4 ? active : inactive, 300);
        animateStep(binding.rlStep5, step >= 5 ? active : inactive, 400);

        if (step == 1) startSonarRipple(binding.vRipple1, binding.ivStatus1);
        else if (step == 2) startSonarRipple(binding.vRipple2, binding.ivStatus2);
        else if (step == 3) startSonarRipple(binding.vRipple3, binding.ivStatus3);
        else if (step == 4) startSonarRipple(binding.vRipple4, binding.ivStatus4);
        else if (step == 5) startSonarRipple(binding.vRipple5, binding.ivStatus5);
        
        animateLine(binding.line1, step >= 2);
        animateLine(binding.line2, step >= 3);
        animateLine(binding.line3, step >= 4);
        
        animateIconColor(binding.ivStatus1, step >= 1);
        animateIconColor(binding.ivStatus2, step >= 2);
        animateIconColor(binding.ivStatus3, step >= 3);
        animateIconColor(binding.ivStatus4, step >= 4);
        animateIconColor(binding.ivStatus5, step >= 5);

        stopSonarRipple(binding.vRipple1, binding.ivStatus1);
        stopSonarRipple(binding.vRipple2, binding.ivStatus2);
        stopSonarRipple(binding.vRipple3, binding.ivStatus3);
        stopSonarRipple(binding.vRipple4, binding.ivStatus4);
        stopSonarRipple(binding.vRipple5, binding.ivStatus5);

        if (step == 1) startSonarRipple(binding.vRipple1, binding.ivStatus1);
        else if (step == 2) startSonarRipple(binding.vRipple2, binding.ivStatus2);
        else if (step == 3) startSonarRipple(binding.vRipple3, binding.ivStatus3);
        else if (step == 4) startSonarRipple(binding.vRipple4, binding.ivStatus4);
        else if (step == 5) startSonarRipple(binding.vRipple5, binding.ivStatus5);

        // Task 5: Show courier info and OTP only when Out for Delivery
        if (step >= 4) {
            if (binding.cvCourierInfo.getVisibility() != View.VISIBLE) {
                binding.cvCourierInfo.setVisibility(View.VISIBLE);
                binding.cvCourierInfo.setAlpha(0f);
                binding.cvCourierInfo.animate().alpha(1f).setDuration(500).start();
            }
            if (order != null) {
                binding.tvCourierName.setText(order.getRiderName() != null ? order.getRiderName() : "David R.");
                String otp = order.getDeliveryOtp();
                if (otp == null || otp.isEmpty()) otp = "419822"; 
                else otp = otp.replace(" ", ""); 
                if (otp.length() < 6) otp = "419822";
                animateOtpReveal(otp);
            }
        } else {
            binding.cvCourierInfo.setVisibility(View.GONE);
            isOtpRevealed = false;
            binding.tvOtp.setText("--- ---");
        }
    }

    private void animateStep(View stepView, float targetAlpha, long delay) {
        if (stepView.getAlpha() == targetAlpha) return;
        stepView.setTranslationY(40f);
        stepView.animate()
            .alpha(targetAlpha)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(delay)
            .setInterpolator(new android.view.animation.OvershootInterpolator())
            .start();
    }

    private void animateLine(View line, boolean shouldGrow) {
        int currentHeight = line.getLayoutParams().height;
        int targetHeight = shouldGrow ? (int)(50 * getResources().getDisplayMetrics().density) : 0;
        if (currentHeight == targetHeight) return;

        android.animation.ValueAnimator anim = android.animation.ValueAnimator.ofInt(currentHeight, targetHeight);
        anim.setDuration(shouldGrow ? 1200 : 300);
        anim.setInterpolator(new android.view.animation.DecelerateInterpolator());
        anim.addUpdateListener(animation -> {
            android.view.ViewGroup.LayoutParams params = line.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            line.setLayoutParams(params);
        });
        anim.start();
    }

    private void startSonarRipple(View rippleView, View iconView) {
        rippleView.setVisibility(View.VISIBLE);
        rippleView.setAlpha(1f);
        rippleView.setScaleX(1f);
        rippleView.setScaleY(1f);
        
        android.animation.PropertyValuesHolder scaleX = android.animation.PropertyValuesHolder.ofFloat("scaleX", 1f, 1.8f);
        android.animation.PropertyValuesHolder scaleY = android.animation.PropertyValuesHolder.ofFloat("scaleY", 1f, 1.8f);
        android.animation.PropertyValuesHolder alpha = android.animation.PropertyValuesHolder.ofFloat("alpha", 1f, 0f);
        
        android.animation.ObjectAnimator rippleAnim = android.animation.ObjectAnimator.ofPropertyValuesHolder(rippleView, scaleX, scaleY, alpha);
        rippleAnim.setDuration(1500);
        rippleAnim.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        rippleAnim.start();
        rippleView.setTag(rippleAnim);

        android.animation.ObjectAnimator iconAnim = android.animation.ObjectAnimator.ofPropertyValuesHolder(
                iconView,
                android.animation.PropertyValuesHolder.ofFloat("scaleX", 1.05f),
                android.animation.PropertyValuesHolder.ofFloat("scaleY", 1.05f)
        );
        iconAnim.setDuration(750);
        iconAnim.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        iconAnim.setRepeatMode(android.animation.ObjectAnimator.REVERSE);
        iconAnim.start();
        iconView.setTag(iconAnim);
    }
    
    private void stopSonarRipple(View rippleView, View iconView) {
        Object rAnim = rippleView.getTag();
        if (rAnim instanceof android.animation.ObjectAnimator) ((android.animation.ObjectAnimator) rAnim).cancel();
        rippleView.setVisibility(View.GONE);
        
        Object iAnim = iconView.getTag();
        if (iAnim instanceof android.animation.ObjectAnimator) ((android.animation.ObjectAnimator) iAnim).cancel();
        iconView.setScaleX(1f);
        iconView.setScaleY(1f);
    }

    private void animateIconColor(View view, boolean isActive) {
        int activeColor = Color.parseColor("#A82D47");
        int inactiveColor = Color.parseColor("#F8F8F8");
        int targetColor = isActive ? activeColor : inactiveColor;
        
        ColorStateList currentTint = view.getBackgroundTintList();
        if (currentTint != null && currentTint.getDefaultColor() == targetColor) return;
        
        int startColor = currentTint != null ? currentTint.getDefaultColor() : (isActive ? inactiveColor : activeColor);

        android.animation.ValueAnimator colorAnim = android.animation.ValueAnimator.ofObject(new android.animation.ArgbEvaluator(), startColor, targetColor);
        colorAnim.setDuration(500);
        colorAnim.addUpdateListener(animator -> view.setBackgroundTintList(ColorStateList.valueOf((int) animator.getAnimatedValue())));
        colorAnim.start();
    }

    private void animateOtpReveal(String targetOtp) {
        if (isOtpRevealed) {
            binding.tvOtp.setText(targetOtp.substring(0,3) + " " + targetOtp.substring(3));
            return;
        }
        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1500);
        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            if (fraction < 1f) {
                int random = 100000 + (int)(Math.random() * 900000);
                String randStr = String.valueOf(random);
                binding.tvOtp.setText(randStr.substring(0,3) + " " + randStr.substring(3));
            } else {
                binding.tvOtp.setText(targetOtp.substring(0,3) + " " + targetOtp.substring(3));
                isOtpRevealed = true;
            }
        });
        animator.start();
    }
}
