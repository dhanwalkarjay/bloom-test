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

    public void setAddons(List<Addon> newAddons) {
        addons.clear();
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

            binding.cbSelected.setChecked(selectedAddons.contains(addon));

            itemView.setOnClickListener(v -> {
                if (selectedAddons.contains(addon)) {
                    selectedAddons.remove(addon);
                } else {
                    selectedAddons.add(addon);
                }
                notifyItemChanged(getAdapterPosition());
            });
        }
    }
}
