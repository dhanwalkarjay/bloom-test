package com.bloom.customer.ui.bespoke;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bloom.customer.data.model.ShopInventoryItem;
import com.bloom.databinding.ItemShopInventoryBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShopInventoryAdapter extends RecyclerView.Adapter<ShopInventoryAdapter.ViewHolder> {

    private List<ShopInventoryItem> items = new ArrayList<>();
    private final Map<String, Integer> selectedQuantities = new HashMap<>();
    private OnSelectionChangeListener listener;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(ShopInventoryItem item, int quantityChange);
    }

    public void setListener(OnSelectionChangeListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ShopInventoryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public Map<String, Integer> getSelectedQuantities() {
        return selectedQuantities;
    }

    public List<ShopInventoryItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShopInventoryBinding binding = ItemShopInventoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShopInventoryItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemShopInventoryBinding binding;

        public ViewHolder(ItemShopInventoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ShopInventoryItem item) {
            binding.tvName.setText(item.getName());
            
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
            binding.tvPrice.setText(format.format(item.getPricePerUnit()));

            if (item.getColorHex() != null && !item.getColorHex().isEmpty()) {
                int resId = binding.getRoot().getContext().getResources().getIdentifier(
                        item.getColorHex(), "drawable", binding.getRoot().getContext().getPackageName());
                if (resId != 0) {
                    binding.ivImage.setImageResource(resId);
                } else {
                    binding.ivImage.setImageResource(android.R.color.transparent);
                }
            } else {
                binding.ivImage.setImageResource(android.R.color.transparent);
            }

            int qty = selectedQuantities.getOrDefault(item.getId(), 0);
            binding.tvQty.setText(String.valueOf(qty));

            binding.btnAdd.setOnClickListener(v -> {
                int current = selectedQuantities.getOrDefault(item.getId(), 0);
                if (current < item.getStockQuantity()) {
                    selectedQuantities.put(item.getId(), current + 1);
                    binding.tvQty.setText(String.valueOf(current + 1));
                    if (listener != null) listener.onSelectionChanged(item, 1);
                }
            });

            binding.btnMinus.setOnClickListener(v -> {
                int current = selectedQuantities.getOrDefault(item.getId(), 0);
                if (current > 0) {
                    selectedQuantities.put(item.getId(), current - 1);
                    binding.tvQty.setText(String.valueOf(current - 1));
                    if (listener != null) listener.onSelectionChanged(item, -1);
                }
            });
        }
    }
}
