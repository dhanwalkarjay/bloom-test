package com.bloom.customer.ui.cart;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bloom.customer.data.model.CartItem;
import com.bloom.databinding.ItemCartBinding;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying items in the cart.
 * Principle: Single Responsibility - handles cart item binding.
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> items = new ArrayList<>();
    private OnCartItemInteractionListener listener;

    public interface OnCartItemInteractionListener {
        void onRemove(int position);
        void onUpdateQuantity(int position, int newQuantity);
    }

    public void setListener(OnCartItemInteractionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CartItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding;

        CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CartItem item, int position) {
            binding.tvProductName.setText(item.getProduct().getName());
            binding.tvPrice.setText("$" + String.format("%.2f", item.getTotalPrice()));
            binding.tvQuantity.setText(String.valueOf(item.getQuantity()));
            
            String customization = item.getSize();
            // Wrap/Ribbon removed from CartItem as per latest prompt
            binding.tvCustomization.setText(customization);

            Glide.with(binding.ivProductImage.getContext())
                    .load(item.getProduct().getImageUrl())
                    .centerCrop()
                    .into(binding.ivProductImage);

            binding.btnRemove.setOnClickListener(v -> {
                if (listener != null) listener.onRemove(position);
            });

            binding.btnPlus.setOnClickListener(v -> {
                if (listener != null) listener.onUpdateQuantity(position, item.getQuantity() + 1);
            });

            binding.btnMinus.setOnClickListener(v -> {
                if (listener != null && item.getQuantity() > 1) {
                    listener.onUpdateQuantity(position, item.getQuantity() - 1);
                }
            });
        }
    }
}
