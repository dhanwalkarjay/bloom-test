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
                if (isMapReady) {
                    updateMapMarkers(result.data);
                } else {
                    pendingOrderData = result.data;
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

        if (locationHelper.hasLocationPermission()) {
            locationHelper.getLastLocation(location -> {
                double liveLat = location != null ? location.getLatitude() : finalULat;
                double liveLng = location != null ? location.getLongitude() : finalULng;
                injectMapJs(finalSLat, finalSLng, liveLat, liveLng, finalRLat, finalRLng);
            });
        } else {
            injectMapJs(finalSLat, finalSLng, finalULat, finalULng, finalRLat, finalRLng);
        }
    }

    private void injectMapJs(double sLat, double sLng, double uLat, double uLng, double rLat, double rLng) {
        StringBuilder jsBuilder = new StringBuilder();
        jsBuilder.append("map.eachLayer(function(layer){ if(layer instanceof L.Marker || layer instanceof L.Polyline) map.removeLayer(layer); });");
        jsBuilder.append("var florist = L.marker([").append(sLat).append(", ").append(sLng).append("]).addTo(map);");
        jsBuilder.append("var user = L.marker([").append(uLat).append(", ").append(uLng).append("], {icon: L.divIcon({className: 'user-icon', html: '<div style=\"background:#4A7C59;width:12px;height:12px;border-radius:50%;border:2px solid white;\"></div>'})}).addTo(map);");
        
        if (rLat != 0 && rLng != 0) {
            jsBuilder.append("var rider = L.marker([").append(rLat).append(", ").append(rLng).append("], {icon: L.divIcon({className: 'rider-icon', html: '<div style=\"background:#A82D47;width:12px;height:12px;border-radius:50%;border:2px solid white;\"></div>'})}).addTo(map);");
            jsBuilder.append("var polyline = L.polyline([[").append(rLat).append(", ").append(rLng).append("], [").append(uLat).append(", ").append(uLng).append("]], {color: '#A82D47', dashArray: '10, 10'}).addTo(map);");
        } else {
            jsBuilder.append("var polyline = L.polyline([[").append(sLat).append(", ").append(sLng).append("], [").append(uLat).append(", ").append(uLng).append("]], {color: '#A82D47', dashArray: '10, 10'}).addTo(map);");
        }
        
        jsBuilder.append("map.fitBounds(polyline.getBounds(), {padding: [50, 50]});");
        
        final String finalJs = jsBuilder.toString();
        binding.mapWebView.post(() -> binding.mapWebView.evaluateJavascript(finalJs, null));
    }

    private void setupMap() {
        WebSettings webSettings = binding.mapWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        
        String html = "<html><head>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.7.1/dist/leaflet.css' />" +
                "<script src='https://unpkg.com/leaflet@1.7.1/dist/leaflet.js'></script>" +
                "<style>#map { height: 100%; width: 100%; margin: 0; padding: 0; }</style>" +
                "</head><body>" +
                "<div id='map'></div>" +
                "<script>" +
                "var map = L.map('map', {zoomControl: false}).setView([21.1458, 79.0882], 14);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);" +
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
        
        binding.rlStep1.setAlpha(step >= 1 ? active : inactive);
        binding.rlStep2.setAlpha(step >= 2 ? active : inactive);
        binding.rlStep3.setAlpha(step >= 3 ? active : inactive);
        binding.cvStep4.setAlpha(step >= 4 ? active : inactive);
        binding.rlStep5.setAlpha(step >= 5 ? active : inactive);
        
        binding.line1.setVisibility(step >= 2 ? View.VISIBLE : View.INVISIBLE);
        binding.line2.setVisibility(step >= 3 ? View.VISIBLE : View.INVISIBLE);
        binding.line3.setVisibility(step >= 4 ? View.VISIBLE : View.INVISIBLE);
        
        int activeColor = Color.parseColor("#A82D47");
        int inactiveColor = Color.parseColor("#CCCCCC");
        ColorStateList activeCSL = ColorStateList.valueOf(activeColor);
        ColorStateList inactiveCSL = ColorStateList.valueOf(inactiveColor);
        
        binding.ivStatus1.setBackgroundTintList(step >= 1 ? activeCSL : inactiveCSL);
        binding.ivStatus2.setBackgroundTintList(step >= 2 ? activeCSL : inactiveCSL);
        binding.ivStatus3.setBackgroundTintList(step >= 3 ? activeCSL : inactiveCSL);
        binding.ivStatus4.setBackgroundTintList(step >= 4 ? activeCSL : inactiveCSL);
        binding.ivStatus5.setBackgroundTintList(step >= 5 ? activeCSL : inactiveCSL);

        // Task 5: Show courier info and OTP only when Out for Delivery
        if (step >= 4) {
            binding.cvCourierInfo.setVisibility(View.VISIBLE);
            if (order != null) {
                binding.tvCourierName.setText(order.getRiderName() != null ? order.getRiderName() : "Assigned Rider");
                binding.tvOtp.setText(order.getDeliveryOtp() != null ? order.getDeliveryOtp() : "--- ---");
            }
        } else {
            binding.cvCourierInfo.setVisibility(View.GONE);
        }
    }
}
