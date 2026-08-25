package com.bloom.customer.ui.lux;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bloom.R;
import com.bloom.customer.data.model.Product;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class LuxProductAdapter extends RecyclerView.Adapter<LuxProductAdapter.LuxViewHolder> {

    private List<Product> products = new ArrayList<>();
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    public void setListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LuxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lux_product, parent, false);
        return new LuxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LuxViewHolder holder, int position) {
        Product product = products.get(position);
        
        holder.tvName.setText(product.getName());
        holder.tvDescription.setText(product.getDescription());
        holder.tvPrice.setText("₹" + product.getPrice() + "0");
        
        // We used category field to store the stock warning mock
        holder.tvStockWarning.setText(product.getCategory());
        
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImageUrl())
                    .into(holder.ivProduct);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class LuxViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvDescription, tvPrice, tvStockWarning;

        public LuxViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStockWarning = itemView.findViewById(R.id.tvStockWarning);
        }
    }
}
