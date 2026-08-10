package com.bloom.customer.ui.shop;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

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
    private boolean isShopOpen = true;

    public interface OnProductClickListener {
        void onProductClick(Product product, boolean isShopOpen);
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void setProducts(List<Product> newProducts, boolean isOpen) {
        this.isShopOpen = isOpen;
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
        private int quantity = 0;

        ProductViewHolder(ItemProductBinding binding) {
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

            // Reset state
            quantity = 0;
            collapseToPlus(false);

            // Card click → product detail
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product, isShopOpen);
                }
            });

            // + clicked → expand to stepper
            binding.ivPlus.setOnClickListener(v -> {
                quantity++;
                binding.tvQuantity.setText(String.valueOf(quantity));
                if (quantity == 1) {
                    expandToStepper();
                } else {
                    animateBump(binding.tvQuantity);
                }
            });

            // - clicked → decrement or collapse back
            binding.ivMinus.setOnClickListener(v -> {
                if (quantity > 1) {
                    quantity--;
                    binding.tvQuantity.setText(String.valueOf(quantity));
                    animateBump(binding.tvQuantity);
                } else {
                    quantity = 0;
                    collapseToPlus(true);
                }
            });
        }

        /** Animate the pill expanding from a single + button to the full - 1 + stepper */
        private void expandToStepper() {
            // First show all views (hidden) so they can be measured
            binding.ivMinus.setVisibility(View.VISIBLE);
            binding.vDivider1.setVisibility(View.VISIBLE);
            binding.tvQuantity.setVisibility(View.VISIBLE);
            binding.vDivider2.setVisibility(View.VISIBLE);

            // Start all elements at 0 alpha
            binding.ivMinus.setAlpha(0f);
            binding.vDivider1.setAlpha(0f);
            binding.tvQuantity.setAlpha(0f);
            binding.vDivider2.setAlpha(0f);

            // Scale the whole card to emphasize expansion
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.cardQuantityControl, "scaleX", 0.85f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.cardQuantityControl, "scaleY", 0.85f, 1f);
            scaleX.setInterpolator(new OvershootInterpolator(2.0f));
            scaleY.setInterpolator(new OvershootInterpolator(2.0f));
            scaleX.setDuration(350);
            scaleY.setDuration(350);

            AnimatorSet expandSet = new AnimatorSet();
            expandSet.playTogether(scaleX, scaleY);
            expandSet.start();

            // Fade in the new elements with a slight delay
            binding.ivMinus.animate().alpha(1f).setStartDelay(100).setDuration(200).start();
            binding.vDivider1.animate().alpha(1f).setStartDelay(120).setDuration(200).start();
            binding.tvQuantity.animate().alpha(1f).setStartDelay(140).setDuration(200)
                    .withEndAction(() -> animateBump(binding.tvQuantity)).start();
            binding.vDivider2.animate().alpha(1f).setStartDelay(160).setDuration(200).start();
        }

        /** Animate the stepper collapsing back to a single + button */
        private void collapseToPlus(boolean animate) {
            if (!animate) {
                binding.ivMinus.setVisibility(View.GONE);
                binding.vDivider1.setVisibility(View.GONE);
                binding.tvQuantity.setVisibility(View.GONE);
                binding.vDivider2.setVisibility(View.GONE);
                binding.ivPlus.setAlpha(1f);
                binding.cardQuantityControl.setScaleX(1f);
                binding.cardQuantityControl.setScaleY(1f);
                return;
            }

            // Fade out the expanded views first
            binding.ivMinus.animate().alpha(0f).setDuration(120).withEndAction(
                    () -> binding.ivMinus.setVisibility(View.GONE)).start();
            binding.vDivider1.animate().alpha(0f).setDuration(100).withEndAction(
                    () -> binding.vDivider1.setVisibility(View.GONE)).start();
            binding.tvQuantity.animate().alpha(0f).setDuration(100).withEndAction(
                    () -> binding.tvQuantity.setVisibility(View.GONE)).start();
            binding.vDivider2.animate().alpha(0f).setDuration(100).withEndAction(
                    () -> binding.vDivider2.setVisibility(View.GONE)).start();

            // Shrink-and-bounce the card back to pill
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.cardQuantityControl, "scaleX", 1f, 0.8f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.cardQuantityControl, "scaleY", 1f, 0.8f, 1f);
            scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleX.setDuration(280);
            scaleY.setDuration(280);

            AnimatorSet collapseSet = new AnimatorSet();
            collapseSet.playTogether(scaleX, scaleY);
            collapseSet.setStartDelay(100);
            collapseSet.start();
        }

        /** Small bounce animation on the quantity number when it changes */
        private void animateBump(View view) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.35f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.35f, 1f);
            scaleX.setDuration(220);
            scaleY.setDuration(220);
            scaleX.setInterpolator(new OvershootInterpolator(3f));
            scaleY.setInterpolator(new OvershootInterpolator(3f));

            AnimatorSet bump = new AnimatorSet();
            bump.playTogether(scaleX, scaleY);
            bump.start();
        }
    }
}
