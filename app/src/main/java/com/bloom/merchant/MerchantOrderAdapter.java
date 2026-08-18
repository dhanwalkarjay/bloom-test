package com.bloom.merchant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bloom.R;
import com.bloom.customer.data.model.Order;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class MerchantOrderAdapter extends RecyclerView.Adapter<MerchantOrderAdapter.OrderViewHolder> {

    private List<Order> orders = new ArrayList<>();
    
    public interface OnOrderActionListener {
        void onAccept(Order order);
        void onDecline(Order order);
    }
    
    private OnOrderActionListener listener;

    public void setOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }
    
    public void setListener(OnOrderActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_merchant_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.tvOrderId.setText("Order #" + order.getId().substring(0, 8).toUpperCase());
        holder.chipStatus.setText(order.getStatus().toUpperCase());
        
        // Mocking items for now
        holder.tvOrderItems.setText("1x Bespoke Creation\n1x Handwritten Note");
        
        if (order.getStatus().equalsIgnoreCase("placed")) {
            holder.btnAccept.setText("Accept Order");
            holder.btnDecline.setVisibility(View.VISIBLE);
        } else if (order.getStatus().equalsIgnoreCase("preparing")) {
            holder.btnAccept.setText("Mark Out for Delivery");
            holder.btnDecline.setVisibility(View.GONE);
        } else if (order.getStatus().equalsIgnoreCase("out_for_delivery")) {
            holder.btnAccept.setText("Mark Delivered");
            holder.btnDecline.setVisibility(View.GONE);
        } else {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnDecline.setVisibility(View.GONE);
        }
        
        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(order);
        });
        
        holder.btnDecline.setOnClickListener(v -> {
            if (listener != null) listener.onDecline(order);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderTime, tvOrderItems;
        Chip chipStatus;
        MaterialButton btnAccept, btnDecline;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }
    }
}
