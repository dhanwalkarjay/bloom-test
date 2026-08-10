package com.bloom.customer.ui.notifications;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.model.Notification;
import com.bloom.customer.data.repository.NotificationRepository;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityNotificationBinding;

public class NotificationActivity extends AppCompatActivity {

    private ActivityNotificationBinding binding;
    private NotificationRepository repository;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new NotificationRepository(this);
        adapter = new NotificationAdapter();

        setupUI();
        fetchNotifications();
    }

    private void setupUI() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void fetchNotifications() {
        // Injecting Premium Demo Notifications
        binding.progressBar.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.GONE);

        java.util.List<Notification> demoNotifications = new java.util.ArrayList<>();

        Notification n1 = new Notification();
        n1.setId("1");
        n1.setTitle("Order Out for Delivery 🚚");
        n1.setMessage("Your premium roses order #10294 is out for delivery and will arrive shortly!");
        n1.setRead(false);
        demoNotifications.add(n1);

        Notification n2 = new Notification();
        n2.setId("2");
        n2.setTitle("Flash Sale: 20% Off 🌹");
        n2.setMessage("Get 20% off all Premium Roses today only. Tap to explore our luxury collection.");
        n2.setRead(true);
        demoNotifications.add(n2);

        Notification n3 = new Notification();
        n3.setId("3");
        n3.setTitle("Welcome to Bloom! ✨");
        n3.setMessage("We're thrilled to have you here. Discover our exclusive hand-picked floral arrangements.");
        n3.setRead(true);
        demoNotifications.add(n3);

        adapter.setNotifications(demoNotifications);
    }
}
