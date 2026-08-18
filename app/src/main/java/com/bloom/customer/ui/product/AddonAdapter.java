package com.bloom.customer.ui.product;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bloom.customer.data.model.Addon;
import com.bloom.databinding.ItemAddonBinding;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class AddonAdapter extends RecyclerView.Adapter<AddonAdapter.ViewHolder> {

    private final List<Addon> addons = new ArrayList<>();
    private final List<Addon> selectedAddons = new ArrayList<>();

    public interface OnAddonSelectionChangedListener {
        void onSelectionChanged();
    }

    private OnAddonSelectionChangedListener listener;

    public void setOnAddonSelectionChangedListener(OnAddonSelectionChangedListener listener) {
        this.listener = listener;
    }

    public void setAddons(List<Addon> newAddons) {
        addons.clear();
        selectedAddons.clear(); // Clear selections when data changes to avoid stale data
        if (newAddons != null) addons.addAll(newAddons);
        notifyDataSetChanged();
    }

    public List<Addon> getSelectedAddons() {
        return selectedAddons;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAddonBinding binding = ItemAddonBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(addons.get(position));
    }

    @Override
    public int getItemCount() {
        return addons.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAddonBinding binding;

        ViewHolder(ItemAddonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Addon addon) {
            binding.tvAddonName.setText(addon.getName());
            binding.tvAddonPrice.setText("₹" + String.format("%.2f", addon.getPrice()));
            
            Glide.with(binding.ivAddonImage.getContext())
                    .load(addon.getImageUrl())
                    .centerCrop()
                    .into(binding.ivAddonImage);

            boolean isSelected = selectedAddons.contains(addon);
            
            if (isSelected) {
                binding.cvAddon.setStrokeColor(binding.cvAddon.getContext().getColor(com.bloom.R.color.bloom_primary));
                binding.cvAddon.setStrokeWidth(4);
                binding.cvAddon.setCardBackgroundColor(binding.cvAddon.getContext().getColor(com.bloom.R.color.cart_surface_container_low));
                binding.ivCheckIndicator.setImageResource(android.R.drawable.checkbox_on_background);
                binding.ivCheckIndicator.setImageTintList(android.content.res.ColorStateList.valueOf(binding.cvAddon.getContext().getColor(com.bloom.R.color.bloom_primary)));
            } else {
                binding.cvAddon.setStrokeColor(binding.cvAddon.getContext().getColor(com.bloom.R.color.cart_outline_variant));
                binding.cvAddon.setStrokeWidth(2);
                binding.cvAddon.setCardBackgroundColor(binding.cvAddon.getContext().getColor(com.bloom.R.color.white));
                binding.ivCheckIndicator.setImageResource(com.bloom.R.drawable.ic_add);
                binding.ivCheckIndicator.setImageTintList(android.content.res.ColorStateList.valueOf(binding.cvAddon.getContext().getColor(com.bloom.R.color.cart_on_surface)));
            }

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return; // Guard against invalid position

                if (selectedAddons.contains(addon)) {
                    selectedAddons.remove(addon);
                } else {
                    selectedAddons.add(addon);
                }
                notifyItemChanged(position);
                if (listener != null) listener.onSelectionChanged();
            });
        }
    }
}
