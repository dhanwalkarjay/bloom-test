package com.bloom.customer.ui.orderhistory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.FragmentOrderListBinding;

public class OrderListFragment extends Fragment {

    private static final String ARG_IS_PAST = "arg_is_past";

    private FragmentOrderListBinding binding;
    private OrderHistoryViewModel viewModel;
    private OrderHistoryAdapter adapter;
    private boolean isPast;

    public static OrderListFragment newInstance(boolean isPast) {
        OrderListFragment fragment = new OrderListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_PAST, isPast);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isPast = getArguments().getBoolean(ARG_IS_PAST, false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Use the activity's ViewModel so it's shared across tabs
        viewModel = new ViewModelProvider(requireActivity()).get(OrderHistoryViewModel.class);
        
        setupRecyclerView();
        
        binding.swipeRefresh.setOnRefreshListener(this::fetchOrders);
        fetchOrders();
    }

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter();
        binding.rvOrderHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOrderHistory.setAdapter(adapter);

        adapter.setListener(new OrderHistoryAdapter.OnOrderHistoryClickListener() {
            @Override
            public void onReviewClick(com.bloom.customer.data.model.Order order) {
                Intent intent = new Intent(requireContext(), RateOrderActivity.class);
                intent.putExtra("order_id", order.getId());
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    intent.putExtra("product_name", order.getItems().get(0).getProduct().getTitle());
                }
                startActivity(intent);
            }

            @Override
            public void onOrderClick(com.bloom.customer.data.model.Order order) {
                Intent intent = new Intent(requireContext(), com.bloom.customer.ui.ordertracking.OrderTrackingActivity.class);
                intent.putExtra("order_id", order.getId());
                startActivity(intent);
            }

            @Override
            public void onDownloadInvoiceClick(com.bloom.customer.data.model.Order order) {
                try {
                    android.net.Uri pdfUri = com.bloom.customer.util.InvoiceGenerator.generateAndSaveInvoice(requireContext(), order);
                    // Phase 4: Corporate Assistant One-Tap PDF sharing
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/pdf");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Invoice for Order #" + order.getId());
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Please find attached the invoice for my recent Bloom purchase.");
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    
                    startActivity(Intent.createChooser(shareIntent, "Share Invoice"));
                } catch (Exception e) {
                    timber.log.Timber.e(e, "Error parsing saved order history state");
                    android.widget.Toast.makeText(requireContext(), "Failed to generate invoice", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchOrders() {
        String userId = SessionManager.getInstance(requireContext()).getUserId();
        if (userId == null) {
            binding.swipeRefresh.setRefreshing(false);
            binding.rvOrderHistory.setVisibility(View.GONE);
            binding.tvEmptyTitle.setText("Log in to view orders");
            binding.tvEmptySubtitle.setText("Sign in to track your luxury floral deliveries.");
            binding.btnRetry.setText("Join Now");
            binding.btnRetry.setVisibility(View.VISIBLE);
            binding.btnRetry.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), com.bloom.customer.ui.auth.LoginActivity.class));
            });
            binding.emptyState.setVisibility(View.VISIBLE);
            return;
        } else {
            binding.btnRetry.setText("Retry");
            binding.btnRetry.setOnClickListener(v -> fetchOrders());
        }

        binding.shimmerLoading.setVisibility(View.VISIBLE);
        binding.shimmerLoading.startShimmer();
        binding.rvOrderHistory.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.GONE);

        viewModel.getOrderHistory(userId).observe(getViewLifecycleOwner(), result -> {
            binding.swipeRefresh.setRefreshing(false);
            binding.shimmerLoading.stopShimmer();
            binding.shimmerLoading.setVisibility(View.GONE);
            
            if (result.status == NetworkResult.Status.SUCCESS) {
                if (result.data != null && !result.data.isEmpty()) {
                    java.util.List<com.bloom.customer.data.model.Order> filtered = new java.util.ArrayList<>();
                    for (com.bloom.customer.data.model.Order o : result.data) {
                        String status = o.getStatus() != null ? o.getStatus() : "placed";
                        boolean isDelivered = "Delivered".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status);
                        
                        if (isPast) {
                            if (isDelivered) filtered.add(o);
                        } else {
                            if (!isDelivered) filtered.add(o);
                        }
                    }
                    
                    if (isPast) {
                        // Inject dummy order for testing PDF
                        com.bloom.customer.data.model.Order dummyOrder = new com.bloom.customer.data.model.Order();
                        dummyOrder.setId("test-849204");
                        dummyOrder.setStatus("Delivered");
                        dummyOrder.setTotalAmount(245.50);
                        dummyOrder.setBouquetSubtotal(200.00);
                        dummyOrder.setDeliveryFee(25.00);
                        dummyOrder.setTaxAmount(20.50);
                        dummyOrder.setCreatedAt("2023-11-05T10:00:00Z");
                        
                        com.bloom.customer.data.model.OrderItem dummyItem = new com.bloom.customer.data.model.OrderItem();
                        dummyItem.setQuantity(2);
                        dummyItem.setUnitPrice(100.0);
                        com.bloom.customer.data.model.OrderItem.ProductInfo dummyProduct = new com.bloom.customer.data.model.OrderItem.ProductInfo();
                        dummyProduct.setTitle("Premium Peony Arrangement");
                        dummyItem.setProduct(dummyProduct);
                        java.util.List<com.bloom.customer.data.model.OrderItem> dummyItems = new java.util.ArrayList<>();
                        dummyItems.add(dummyItem);
                        dummyOrder.setItems(dummyItems);
                        
                        com.bloom.customer.data.model.Address dummyAddress = new com.bloom.customer.data.model.Address();
                        dummyAddress.setRecipientName("Executive Assistant");
                        dummyAddress.setFullAddress("400 Corporate Blvd\nFloor 12, Marketing Dept\nSan Francisco, CA 94107");
                        dummyOrder.setAddress(dummyAddress);
                        
                        filtered.add(0, dummyOrder);
                    }
                    
                    if (filtered.isEmpty()) {
                        binding.rvOrderHistory.setVisibility(View.GONE);
                        binding.tvEmptyTitle.setText("Your floral journey starts here");
                        binding.tvEmptySubtitle.setText("Discover our curated collection of luxury bouquets.");
                        binding.btnRetry.setText("Shop Now");
                        binding.btnRetry.setVisibility(View.VISIBLE);
                        binding.btnRetry.setOnClickListener(v -> {
                            // Optionally switch to Home tab
                        });
                        binding.emptyState.setVisibility(View.VISIBLE);
                    } else {
                        adapter.setOrders(filtered);
                        binding.rvOrderHistory.setVisibility(View.VISIBLE);
                        binding.emptyState.setVisibility(View.GONE);
                    }
                } else {
                    binding.tvEmptyTitle.setText("Your floral journey starts here");
                    binding.tvEmptySubtitle.setText("Discover our curated collection of luxury bouquets.");
                    binding.btnRetry.setText("Shop Now");
                    binding.btnRetry.setVisibility(View.VISIBLE);
                    binding.btnRetry.setOnClickListener(v -> {});
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
            } else {
                binding.tvEmptyTitle.setText("Couldn't load orders");
                binding.tvEmptySubtitle.setText(result.message != null ? result.message : "Pull down to retry.");
                binding.btnRetry.setVisibility(View.VISIBLE);
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
