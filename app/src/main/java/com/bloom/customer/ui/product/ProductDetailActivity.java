package com.bloom.customer.ui.product;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import android.graphics.Color;

import com.bloom.R;
import com.bloom.customer.data.model.CartItem;
import com.bloom.customer.data.model.Product;
import com.bloom.customer.data.repository.CartRepository;
import com.bloom.databinding.ActivityProductDetailBinding;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

/**
 * Activity for displaying product details and customization.
 * Principle: Separation of Concerns - UI logic only.
 */
public class ProductDetailActivity extends AppCompatActivity {

    private ActivityProductDetailBinding binding;
    private CartRepository cartRepository;
    private Product product;
    private boolean isShopOpen = true;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(true);

        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int originalBottomBarHeight = binding.bottomActionBar.getLayoutParams().height;

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );

            binding.appBarLayout.setPadding(0, insets.top, 0, 0);

            binding.bottomActionBar.setPadding(
                    binding.bottomActionBar.getPaddingLeft(),
                    binding.bottomActionBar.getPaddingTop(),
                    binding.bottomActionBar.getPaddingRight(),
                    insets.bottom
            );
            
            if (originalBottomBarHeight > 0) {
                binding.bottomActionBar.getLayoutParams().height = originalBottomBarHeight + insets.bottom;
                binding.bottomActionBar.requestLayout();
            }

            return windowInsets;
        });

        ViewCompat.requestApplyInsets(binding.getRoot());

        // Parse product from intent
        String productJson = getIntent().getStringExtra("product_json");
        product = new Gson().fromJson(productJson, Product.class);
        isShopOpen = getIntent().getBooleanExtra("is_shop_open", true);

        cartRepository = new CartRepository(this);

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        binding.tvProductName.setText(product.getName());
        binding.tvPrice.setText("$" + product.getPrice());
        binding.tvDescription.setText(product.getDescription());

        Glide.with(this)
                .load(product.getImageUrl())
                .into(binding.ivProductImage);

        if (!isShopOpen) {
            binding.btnAddToCart.setEnabled(false);
            binding.btnAddToCart.setText(R.string.shop_closed);
            binding.btnAddToCart.setBackgroundColor(getColor(android.R.color.darker_gray));
        }
        
        updateQuantityText();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnMenu.setOnClickListener(v -> finish());

        binding.btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityText();
            }
        });

        binding.btnPlus.setOnClickListener(v -> {
            if (quantity < 10) {
                quantity++;
                updateQuantityText();
            }
        });

        binding.btnAddToCart.setOnClickListener(v -> {
            CartItem cartItem = new CartItem(product);
            cartItem.setQuantity(quantity);
            cartItem.setSize("Regular");
            cartItem.setCardMessage("");

            handleAddToCart(cartItem);
        });
    }

    private void updateQuantityText() {
        binding.tvQuantity.setText(String.valueOf(quantity));
    }

    private void handleAddToCart(CartItem item) {
        String currentShopId = cartRepository.getCartShopId();
        
        if (currentShopId != null && !currentShopId.equals(product.getShopId())) {
            showClearCartDialog(item);
        } else {
            cartRepository.addToCart(item);
            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showClearCartDialog(CartItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Clear Cart?")
                .setMessage("Your cart contains items from another shop. Clear cart to add this item?")
                .setPositiveButton("Clear & Add", (dialog, which) -> {
                    cartRepository.clearCart();
                    cartRepository.addToCart(item);
                    Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
