package com.bloom.customer.ui.shop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bloom.customer.data.model.Product;
import com.bloom.databinding.ItemProductBinding;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying products in a grid.
 * Principle: Single Responsibility - handles product item binding.
 */
public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.ProductViewHolder> {

    private final List<Product> products = new ArrayList<>();
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void setProducts(List<Product> newProducts) {
        products.clear();
        if (newProducts != null) {
            products.addAll(newProducts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductBinding binding;

        ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product) {
            binding.tvProductName.setText(product.getName());
            binding.tvPrice.setText("$" + product.getPrice());
            binding.tvLuxBadge.setVisibility(product.isLux() ? View.VISIBLE : View.GONE);

            Glide.with(binding.ivProductImage.getContext())
                    .load(product.getImageUrl())
                    .centerCrop()
                    .into(binding.ivProductImage);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}
