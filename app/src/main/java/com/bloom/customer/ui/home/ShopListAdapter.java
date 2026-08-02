package com.bloom.customer.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bloom.customer.data.model.Shop;
import com.bloom.databinding.ItemShopBinding;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of shops.
 * Pattern: Adapter Pattern - bridges data and RecyclerView.
 * Principle: Single Responsibility - handles list item binding.
 */
public class ShopListAdapter extends RecyclerView.Adapter<ShopListAdapter.ShopViewHolder> {

    private final List<Shop> shops = new ArrayList<>();
    private OnShopClickListener listener;

    public interface OnShopClickListener {
        void onShopClick(Shop shop);
    }

    public void setOnShopClickListener(OnShopClickListener listener) {
        this.listener = listener;
    }

    public void setShops(List<Shop> newShops) {
        shops.clear();
        if (newShops != null) {
            shops.addAll(newShops);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShopBinding binding = ItemShopBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ShopViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        holder.bind(shops.get(position));
    }

    @Override
    public int getItemCount() {
        return shops.size();
    }

    class ShopViewHolder extends RecyclerView.ViewHolder {
        private final ItemShopBinding binding;

        ShopViewHolder(ItemShopBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Shop shop) {
            binding.tvShopName.setText(shop.getName());
            binding.tvRating.setText("★ " + shop.getRating());
            binding.tvDistance.setText(shop.getFormattedDistance());
            binding.tvPrepTime.setText(shop.getPrepTime());

            // Handle Top Rated badge
            binding.tvTopRatedBadge.setVisibility(shop.getRating() >= 4.7 ? View.VISIBLE : View.GONE);

            // Handle Open/Closed status
            if (shop.isOpen()) {
                binding.vStatusOverlay.setVisibility(View.GONE);
                binding.tvClosedIndicator.setVisibility(View.GONE);
            } else {
                binding.vStatusOverlay.setVisibility(View.VISIBLE);
                binding.tvClosedIndicator.setVisibility(View.VISIBLE);
                if (shop.getOpensAt() != null) {
                    binding.tvClosedIndicator.setText("OPENS AT " + shop.getOpensAt());
                } else {
                    binding.tvClosedIndicator.setText("CLOSED");
                }
            }

            Glide.with(binding.ivShopImage.getContext())
                    .load(shop.getImageUrl())
                    .centerCrop()
                    .into(binding.ivShopImage);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onShopClick(shop);
                }
            });
        }
    }
}
