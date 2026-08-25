package com.bloom.customer.ui.checkout;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bloom.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {
    
    private List<String> timeSlots = new ArrayList<>();
    private int selectedPosition = 0;
    private OnSlotSelectedListener listener;

    public interface OnSlotSelectedListener {
        void onSlotSelected(String slot);
    }

    public TimeSlotAdapter(OnSlotSelectedListener listener) {
        this.listener = listener;
    }

    public void setSlots(List<String> slots) {
        this.timeSlots = slots;
        this.selectedPosition = 0; // Default to first slot
        notifyDataSetChanged();
        if (!slots.isEmpty() && listener != null) {
            listener.onSlotSelected(slots.get(0));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String slot = timeSlots.get(position);
        holder.tvSlotTime.setText(slot);

        boolean isSelected = position == selectedPosition;
        
        if (isSelected) {
            holder.cardSlot.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.home_primary));
            holder.cardSlot.setStrokeWidth(0);
            holder.tvSlotTime.setTextColor(Color.WHITE);
            holder.ivSlotIcon.setVisibility(View.VISIBLE);
            holder.ivSlotIcon.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            
            if (slot.contains("ASAP")) {
                holder.ivSlotIcon.setImageResource(R.drawable.ic_product_lightning);
            } else {
                holder.ivSlotIcon.setImageResource(R.drawable.ic_product_calendar);
            }
        } else {
            holder.cardSlot.setCardBackgroundColor(com.google.android.material.color.MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorSurface, Color.WHITE));
            holder.cardSlot.setStrokeWidth((int)(1 * holder.itemView.getContext().getResources().getDisplayMetrics().density));
            holder.cardSlot.setStrokeColor(com.google.android.material.color.MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOutline, Color.LTGRAY));
            holder.tvSlotTime.setTextColor(com.google.android.material.color.MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOnSurface, Color.BLACK));
            holder.ivSlotIcon.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onSlotSelected(slot);
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardSlot;
        TextView tvSlotTime;
        ImageView ivSlotIcon;

        ViewHolder(View view) {
            super(view);
            cardSlot = view.findViewById(R.id.cardSlot);
            tvSlotTime = view.findViewById(R.id.tvSlotTime);
            ivSlotIcon = view.findViewById(R.id.ivSlotIcon);
        }
    }
}
