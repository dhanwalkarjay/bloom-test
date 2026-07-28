package com.bloom.customer.ui.orderhistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bloom.customer.data.model.Order;
import com.bloom.customer.data.model.OrderItem;
import com.bloom.databinding.ItemOrderHistoryBinding;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private final List<Order> orders = new ArrayList<>();
    private final List<String> expandedOrderIds = new ArrayList<>();
    private OnOrderHistoryClickListener listener;

    public interface OnOrderHistoryClickListener {
        void onReviewClick(Order order);
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
            binding.tvShopName.setText(order.getShop() != null ? order.getShop().getName() : "Unknown Shop");
            binding.tvStatus.setText(order.getStatus());
            binding.tvDate.setText(order.getCreatedAt().substring(0, 10)); // Simple date format
            binding.tvTotal.setText(String.format("₹%.2f", order.getTotalAmount()));

            // Itemized list logic
            binding.llItems.removeAllViews();
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    TextView tv = new TextView(itemView.getContext());
                    tv.setText(String.format("• %dx Product ID: %s (%s)", 
                        item.getQuantity(), item.getProductId(), item.getSize()));
                    tv.setTextColor(0xFF666666);
                    tv.setPadding(0, 4, 0, 4);
                    binding.llItems.addView(tv);
                }
            }

            boolean isExpanded = expandedOrderIds.contains(order.getId());
            binding.llItems.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            // Show Leave Review only if Delivered
            if ("Delivered".equalsIgnoreCase(order.getStatus())) {
                binding.btnLeaveReview.setVisibility(View.VISIBLE);
                binding.btnLeaveReview.setOnClickListener(v -> {
                    if (listener != null) listener.onReviewClick(order);
                });
            } else {
                binding.btnLeaveReview.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (isExpanded) {
                    expandedOrderIds.remove(order.getId());
                } else {
                    expandedOrderIds.add(order.getId());
                }
                notifyItemChanged(getAdapterPosition());
            });
        }
    }
}
