package com.bloom.agent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bloom.R;
import com.bloom.customer.data.model.Order;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AgentDeliveryAdapter extends RecyclerView.Adapter<AgentDeliveryAdapter.ViewHolder> {

    private List<Order> deliveries = new ArrayList<>();
    private OnDeliveryClickListener listener;

    public interface OnDeliveryClickListener {
        void onViewDetails(Order order);
    }

    public void setListener(OnDeliveryClickListener listener) {
        this.listener = listener;
    }

    public void setDeliveries(List<Order> newDeliveries) {
        this.deliveries = newDeliveries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_agent_delivery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = deliveries.get(position);
        holder.tvOrderId.setText("#" + order.getId().substring(0, Math.min(8, order.getId().length())));
        holder.tvStatus.setText(order.getStatus().replace("_", " ").toUpperCase());
        
        // Mocking customer details for now, as order model might not have them natively without join
        // We will assume Address and Profile data is fetched, or we just display placeholders until detail screen
        holder.tvCustomerName.setText("Customer for Order " + order.getId().substring(0, 4));
        holder.tvAddress.setText("Delivery Address will be shown in details");

        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetails(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deliveries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvCustomerName, tvAddress;
        MaterialButton btnViewDetails;

        ViewHolder(View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}
