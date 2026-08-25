package com.bloom.customer.ui.cart;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bloom.customer.data.model.CartItem;
import com.bloom.R;
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
            binding.tvPrice.setText("₹" + String.format("%.2f", item.getTotalPrice()));
            binding.tvQuantity.setText(String.valueOf(item.getQuantity()));
            binding.tvCustomization.setText(formatCustomization(item));

            Glide.with(binding.ivProductImage.getContext())
                    .load(item.getProduct().getImageUrl())
                    .placeholder(R.drawable.bg_cart_image_placeholder)
                    .error(R.drawable.bg_cart_image_placeholder)
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

        private String formatCustomization(CartItem item) {
            StringBuilder sb = new StringBuilder();
            String size = item.getSize();
            if (size == null || size.trim().isEmpty() || "Regular".equalsIgnoreCase(size.trim())) {
                sb.append("Standard arrangement");
            } else {
                sb.append(size.trim());
            }

            if (item.getAddons() != null && !item.getAddons().isEmpty()) {
                sb.append(" • + ");
                for (int i = 0; i < item.getAddons().size(); i++) {
                    sb.append(item.getAddons().get(i).getName());
                    if (i < item.getAddons().size() - 1) {
                        sb.append(", ");
                    }
                }
            }
            
            if (item.getCardMessage() != null && !item.getCardMessage().isEmpty()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("Msg: \"").append(item.getCardMessage()).append("\"");
            }
            return sb.toString();
        }
    }
}
