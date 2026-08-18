package com.bloom.merchant;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bloom.customer.data.model.ShopInventoryItem;
import com.bloom.databinding.ItemMerchantInventoryBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MerchantInventoryAdapter extends RecyclerView.Adapter<MerchantInventoryAdapter.ViewHolder> {

    private List<ShopInventoryItem> items = new ArrayList<>();
    private OnItemToggledListener listener;

    public interface OnItemToggledListener {
        void onToggled(ShopInventoryItem item, boolean isAvailable);
    }

    public void setListener(OnItemToggledListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ShopInventoryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMerchantInventoryBinding binding = ItemMerchantInventoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMerchantInventoryBinding binding;

        public ViewHolder(ItemMerchantInventoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ShopInventoryItem item) {
            binding.tvName.setText(item.getName());
            
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
            binding.tvPrice.setText(format.format(item.getPricePerUnit()));
            binding.tvType.setText(item.getType());
            
            if (item.getColorHex() != null && !item.getColorHex().isEmpty()) {
                binding.tvEmoji.setText(item.getColorHex());
            } else {
                binding.tvEmoji.setText("📦");
            }
            
            // Avoid triggering listener when setting programmatically
            binding.switchAvailable.setOnCheckedChangeListener(null);
            binding.switchAvailable.setChecked(item.getStockQuantity() > 0);
            
            binding.switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setStockQuantity(isChecked ? 100 : 0);
                if (listener != null) {
                    listener.onToggled(item, isChecked);
                }
            });
        }
    }
}
