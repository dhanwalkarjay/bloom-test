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
            binding.tvStatus.setText(order.getStatus());
            binding.tvDate.setText(order.getCreatedAt().substring(0, 10));
            binding.tvTotal.setText(String.format("₹%.2f", order.getTotalAmount()));
            
            // Show first product name as main title
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                String firstItemName = order.getItems().get(0).getProduct() != null ? 
                    order.getItems().get(0).getProduct().getTitle() : "Bouquet";
                binding.tvShopName.setText(firstItemName);
                binding.tvShopName.setVisibility(View.VISIBLE);
            } else {
                binding.tvShopName.setText("Order #" + order.getId().substring(0, 5));
                binding.tvShopName.setVisibility(View.VISIBLE);
            }

            // Itemized list logic
            binding.llItems.removeAllViews();
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    TextView tv = new TextView(itemView.getContext());
                    String productName = item.getProduct() != null ? item.getProduct().getTitle() : "Product";
                    tv.setText(String.format("• %dx %s (%s)", 
                        item.getQuantity(), productName, item.getSize()));
                    tv.setTextColor(0xFF24181A);
                    tv.setTextSize(13);
                    tv.setPadding(0, 4, 0, 4);
                    binding.llItems.addView(tv);
                }
            }

            // Always show items for better visibility as requested
            binding.llItems.setVisibility(View.VISIBLE);

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
                // Click could open order details if needed
            });
        }
    }
}
