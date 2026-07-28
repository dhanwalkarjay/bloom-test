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

    public interface StatusUpdateListener {
        void onStatusUpdate(String orderId, String newStatus);
    }

    public RealtimeService() {
        this.client = new OkHttpClient();
    }

    public void startTracking(String orderId, StatusUpdateListener listener) {
        this.listener = listener;

        // Construct Supabase Realtime URL
        // Example: wss://<ref>.supabase.co/realtime/v1/websocket?apikey=<key>&vsn=1.0.0
        String url = Constants.SUPABASE_URL.replace("https://", "wss://") 
                   + "realtime/v1/websocket?apikey=" + Constants.SUPABASE_ANON_KEY + "&vsn=1.0.0";

        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket Connected");
                // Subscribe to changes for the specific order
                // This is a simplified JSON payload for Supabase Realtime
                String subMsg = "{\"topic\":\"realtime:public:orders:id=eq." + orderId + "\",\"event\":\"phx_join\",\"payload\":{},\"ref\":\"1\"}";
                webSocket.send(subMsg);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Message Received: " + text);
                // Parse the status from the realtime message and notify listener
                // In a real app, use Gson to parse the phoenix/supabase realtime envelope
                if (text.contains("Out for Delivery")) listener.onStatusUpdate(orderId, "Out for Delivery");
                else if (text.contains("Delivered")) listener.onStatusUpdate(orderId, "Delivered");
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket Closing: " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket Error: " + t.getMessage());
            }
        });
    }

    public void stopTracking() {
        if (webSocket != null) {
            webSocket.close(1000, "Tracking stopped");
        }
    }
}
