package com.bloom.customer.data.api;

import android.util.Log;

import com.bloom.customer.util.Constants;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Service to handle Supabase Realtime WebSocket connections.
 * Pattern: Observer Pattern - notifies listeners of status updates.
 */
public class RealtimeService {

    private static final String TAG = "RealtimeService";
    private WebSocket webSocket;
    private final OkHttpClient client;
    private StatusUpdateListener listener;
    private android.os.Handler heartbeatHandler;
    private Runnable heartbeatRunnable;

    public interface StatusUpdateListener {
        void onStatusUpdate(String orderId, String newStatus);
        void onConnectionError();
    }

    public RealtimeService() {
        this.client = new OkHttpClient.Builder()
            .pingInterval(20, java.util.concurrent.TimeUnit.SECONDS) // Basic WebSocket ping
            .build();
        this.heartbeatHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    }

    public void startTracking(String orderId, StatusUpdateListener listener) {
        this.listener = listener;

        String url = Constants.SUPABASE_URL.replace("https://", "wss://") 
                   + "realtime/v1/websocket?apikey=" + Constants.SUPABASE_ANON_KEY + "&vsn=1.0.0";

        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket Connected");
                
                // 1. Send phx_join to subscribe to the specific order
                String topic = "realtime:public:orders:id=eq." + orderId;
                String joinMsg = "{\"topic\":\"" + topic + "\",\"event\":\"phx_join\",\"payload\":{\"config\":{\"broadcast\":{\"ack\":false},\"presence\":{\"key\":\"\"},\"postgres_changes\":[{\"event\":\"UPDATE\",\"schema\":\"public\",\"table\":\"orders\",\"filter\":\"id=eq." + orderId + "\"}]}},\"ref\":\"1\"}";
                webSocket.send(joinMsg);

                // 2. Start Phoenix Heartbeat (required every 30s, we do 25s)
                startHeartbeat();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Message Received: " + text);
                try {
                    org.json.JSONObject json = new org.json.JSONObject(text);
                    String event = json.optString("event");
                    
                    if ("postgres_changes".equals(event)) {
                        org.json.JSONObject payload = json.optJSONObject("payload");
                        if (payload != null) {
                            org.json.JSONObject record = payload.optJSONObject("record");
                            if (record != null) {
                                String newStatus = record.optString("status");
                                if (newStatus != null && !newStatus.isEmpty()) {
                                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                        if (RealtimeService.this.listener != null) {
                                            RealtimeService.this.listener.onStatusUpdate(orderId, newStatus);
                                        }
                                    });
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "JSON Parse error: " + e.getMessage());
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket Closing: " + reason);
                stopHeartbeat();
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    if (listener != null) listener.onConnectionError();
                });
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket Error: " + t.getMessage());
                stopHeartbeat();
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    if (listener != null) listener.onConnectionError();
                });
            }
        });
    }

    private void startHeartbeat() {
        heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                if (webSocket != null) {
                    String heartbeatMsg = "{\"topic\":\"phoenix\",\"event\":\"heartbeat\",\"payload\":{},\"ref\":\"0\"}";
                    webSocket.send(heartbeatMsg);
                    heartbeatHandler.postDelayed(this, 25000);
                }
            }
        };
        heartbeatHandler.postDelayed(heartbeatRunnable, 25000);
    }

    private void stopHeartbeat() {
        if (heartbeatRunnable != null) {
            heartbeatHandler.removeCallbacks(heartbeatRunnable);
            heartbeatRunnable = null;
        }
    }

    public void stopTracking() {
        stopHeartbeat();
        if (webSocket != null) {
            webSocket.close(1000, "Tracking stopped");
            webSocket = null;
        }
    }

    public interface MerchantOrderListener {
        void onNewOrder();
    }

    public void startMerchantTracking(String shopId, MerchantOrderListener listener) {
        String url = Constants.SUPABASE_URL.replace("https://", "wss://") 
                   + "realtime/v1/websocket?apikey=" + Constants.SUPABASE_ANON_KEY + "&vsn=1.0.0";

        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "Merchant WebSocket Connected");
                String topic = "realtime:public:orders:shop_id=eq." + shopId;
                String joinMsg = "{\"topic\":\"" + topic + "\",\"event\":\"phx_join\",\"payload\":{\"config\":{\"broadcast\":{\"ack\":false},\"presence\":{\"key\":\"\"},\"postgres_changes\":[{\"event\":\"INSERT\",\"schema\":\"public\",\"table\":\"orders\",\"filter\":\"shop_id=eq." + shopId + "\"}]}},\"ref\":\"1\"}";
                webSocket.send(joinMsg);
                startHeartbeat();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Merchant Msg: " + text);
                try {
                    org.json.JSONObject json = new org.json.JSONObject(text);
                    if ("postgres_changes".equals(json.optString("event"))) {
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                            if (listener != null) listener.onNewOrder();
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "JSON Parse error: " + e.getMessage());
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                stopHeartbeat();
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                stopHeartbeat();
            }
        });
    }
}
