package com.bloom.customer.ui.home;

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

public class FeaturedProductAdapter extends RecyclerView.Adapter<FeaturedProductAdapter.ViewHolder> {

    private final List<Product> products = new ArrayList<>();
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onAddClick(Product product);
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        
        // Dynamic width: ~42% of screen to match Figma "peek" layout precisely
        int screenWidth = parent.getContext().getResources().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams params = binding.getRoot().getLayoutParams();
        params.width = (int) (screenWidth * 0.42);
        binding.getRoot().setLayoutParams(params);
        
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductBinding binding;

        ViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product) {
            binding.tvProductName.setText(product.getName());
            binding.tvPrice.setText("₹" + product.getPrice());
            binding.tvLuxBadge.setVisibility(product.isLux() ? View.VISIBLE : View.GONE);

            Glide.with(binding.ivProductImage.getContext())
                    .load(product.getImageUrl())
                    .centerCrop()
                    .into(binding.ivProductImage);
                    
            updateQuantityUI(product);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });

            binding.ivPlus.setOnClickListener(v -> {
                product.setCartQuantity(product.getCartQuantity() + 1);
                updateQuantityUI(product);
                if (listener != null) {
                    listener.onAddClick(product);
                }
            });
            
            binding.ivMinus.setOnClickListener(v -> {
                if (product.getCartQuantity() > 0) {
                    product.setCartQuantity(product.getCartQuantity() - 1);
                    updateQuantityUI(product);
                }
            });
        }
        
        private void updateQuantityUI(Product product) {
            if (product.getCartQuantity() > 0) {
                binding.ivMinus.setVisibility(View.VISIBLE);
                binding.vDivider1.setVisibility(View.VISIBLE);
                binding.tvQuantity.setVisibility(View.VISIBLE);
                binding.vDivider2.setVisibility(View.VISIBLE);
                binding.tvQuantity.setText(String.valueOf(product.getCartQuantity()));
            } else {
                binding.ivMinus.setVisibility(View.GONE);
                binding.vDivider1.setVisibility(View.GONE);
                binding.tvQuantity.setVisibility(View.GONE);
                binding.vDivider2.setVisibility(View.GONE);
            }
        }
    }
}
