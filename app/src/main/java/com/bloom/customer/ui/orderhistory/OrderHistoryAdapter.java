package com.bloom.customer.ui.orderhistory;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bloom.customer.data.model.Order;
import com.bloom.customer.data.model.OrderItem;
import com.bloom.customer.util.CurrencyFormatter;
import com.bloom.databinding.ItemOrderHistoryBinding;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private final List<Order> orders = new ArrayList<>();
    private OnOrderHistoryClickListener listener;

    public interface OnOrderHistoryClickListener {
        void onReviewClick(Order order);
        void onOrderClick(Order order);
        void onDownloadInvoiceClick(Order order);
    }

    public void setListener(OnOrderHistoryClickListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<Order> newOrders) {
        orders.clear();
        if (newOrders != null) {
            orders.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderHistoryBinding binding = ItemOrderHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderHistoryBinding binding;

        OrderViewHolder(ItemOrderHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Order order) {
            String status = order.getStatus() != null ? order.getStatus() : "placed";
            binding.tvStatus.setText(status.toUpperCase());
            updateStatusStyle(status);

            binding.tvDate.setText("Ordered on " + (order.getCreatedAt() != null ? order.getCreatedAt().substring(0, 10) : ""));
            binding.tvTotal.setText(CurrencyFormatter.format(order.getTotalAmount()));
            
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                OrderItem firstItem = order.getItems().get(0);
                binding.tvShopName.setText(firstItem.getProduct() != null ? firstItem.getProduct().getTitle() : "Bouquet");
                
                if (firstItem.getProduct() != null && firstItem.getProduct().getImages() != null) {
                    Glide.with(itemView.getContext())
                        .load(firstItem.getProduct().getImages())
                        .into(binding.ivProduct);
                }
            } else {
                binding.tvShopName.setText("Order #" + (order.getId() != null ? order.getId().substring(0, 5) : "---"));
            }

            // Show Leave Review and Download Invoice if Delivered
            if ("Delivered".equalsIgnoreCase(status)) {
                binding.btnLeaveReview.setVisibility(View.VISIBLE);
                binding.btnDownloadInvoice.setVisibility(View.VISIBLE);
                binding.btnTrackOrder.setVisibility(View.GONE);
                
                binding.btnLeaveReview.setOnClickListener(v -> {
                    if (listener != null) listener.onReviewClick(order);
                });
                binding.btnDownloadInvoice.setOnClickListener(v -> {
                    if (listener != null) listener.onDownloadInvoiceClick(order);
                });
            } else {
                binding.btnLeaveReview.setVisibility(View.GONE);
                binding.btnDownloadInvoice.setVisibility(View.GONE);
                binding.btnTrackOrder.setVisibility(View.VISIBLE);
                binding.btnTrackOrder.setOnClickListener(v -> {
                    if (listener != null) listener.onOrderClick(order);
                });
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onOrderClick(order);
            });
        }

        private void updateStatusStyle(String status) {
            String lower = status.toLowerCase();
            int bgColor = Color.parseColor("#FFF9C4"); // Default yellow
            int textColor = Color.parseColor("#FBC02D");

            if (lower.contains("deliver")) {
                bgColor = Color.parseColor("#E8F5E9");
                textColor = Color.parseColor("#2E7D32");
            } else if (lower.contains("cancel")) {
                bgColor = Color.parseColor("#FFEBEE");
                textColor = Color.parseColor("#C62828");
            } else if (lower.contains("out")) {
                bgColor = Color.parseColor("#E3F2FD");
                textColor = Color.parseColor("#1565C0");
            }
            binding.cvStatus.setCardBackgroundColor(bgColor);
            binding.tvStatus.setTextColor(textColor);
        }
    }
}
