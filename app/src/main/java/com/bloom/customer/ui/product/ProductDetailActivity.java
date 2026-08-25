package com.bloom.customer.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bloom.R;
import com.bloom.customer.data.model.Addon;
import com.bloom.customer.data.model.CartItem;
import com.bloom.customer.data.model.Product;
import java.util.List;
import java.util.ArrayList;
import com.bloom.customer.util.CurrencyFormatter;
import com.bloom.customer.util.NetworkResult;
import com.bloom.databinding.ActivityProductDetailBinding;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.snackbar.Snackbar;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Activity for displaying product details and customization.
 * Principle: Separation of Concerns - UI logic only.
 */
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.net.Uri;

public class ProductDetailActivity extends AppCompatActivity {

    private ActivityProductDetailBinding binding;
    private ProductDetailViewModel viewModel;
    private AddonAdapter addonAdapter;
    private Product product;
    private boolean isShopOpen = true;
    private int quantity = 1;
    private String selectedSize = "Regular";
    private double sizeMarkup = 0.0;
    private Uri selectedVideoUri;
    private ActivityResultLauncher<Intent> videoPickerLauncher;

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

        viewModel = new ViewModelProvider(this).get(ProductDetailViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );

            int paddingHorizontal = getResources().getDimensionPixelSize(R.dimen.spacing_l);
            int paddingVertical = getResources().getDimensionPixelSize(R.dimen.spacing_m);
            binding.topBar.setPadding(paddingHorizontal, insets.top + paddingVertical, paddingHorizontal, paddingVertical);

            binding.bottomActionBar.setPadding(
                    binding.bottomActionBar.getPaddingLeft(),
                    binding.bottomActionBar.getPaddingTop(),
                    binding.bottomActionBar.getPaddingRight(),
                    insets.bottom
            );
            
            binding.bottomActionBar.post(() -> {
                int bottomBarHeight = binding.bottomActionBar.getHeight();
                View scrollContent = binding.productScrollView.getChildAt(0);
                if (scrollContent != null) {
                    scrollContent.setPadding(
                        scrollContent.getPaddingLeft(),
                        scrollContent.getPaddingTop(),
                        scrollContent.getPaddingRight(),
                        bottomBarHeight + insets.bottom + paddingVertical
                    );
                }
            });
            
            return windowInsets;
        });

        ViewCompat.requestApplyInsets(binding.getRoot());

        // Parse product from intent
        String productJson = getIntent().getStringExtra("product_json");
        product = new Gson().fromJson(productJson, Product.class);
        isShopOpen = getIntent().getBooleanExtra("is_shop_open", true);

        videoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedVideoUri = result.getData().getData();
                    binding.tvVideoStatus.setText("Video Ready to Send");
                    binding.ivVideoIcon.setImageResource(android.R.drawable.ic_media_play);
                    binding.btnRemoveVideo.setVisibility(View.VISIBLE);
                }
            }
        );

        setupAddonsRecyclerView();
        setupUI();
        setupListeners();
        checkIfInCart();
        setupCartBadgeObserver();
        fetchAddons();
    }

    private void setupCartBadgeObserver() {
        viewModel.getCartItems().observe(this, items -> {
            int totalCount = 0;
            if (items != null) {
                for (CartItem item : items) {
                    totalCount += item.getQuantity();
                }
            }
            if (totalCount > 0) {
                binding.tvCartBadge.setVisibility(View.VISIBLE);
                binding.tvCartBadge.setText(String.valueOf(totalCount));
            } else {
                binding.tvCartBadge.setVisibility(View.GONE);
            }
        });
    }

    private void setupAddonsRecyclerView() {
        addonAdapter = new AddonAdapter();
        addonAdapter.setOnAddonSelectionChangedListener(() -> updateTotalPrice());
        binding.rvAddons.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        binding.rvAddons.setAdapter(addonAdapter);
    }

    private void fetchAddons() {
        List<Addon> dummyAddons = new ArrayList<>();
        
        Addon chocolates = new Addon();
        chocolates.setId("1");
        chocolates.setName("Luxury Chocolates");
        chocolates.setPrice(15.00);
        chocolates.setImageUrl("android.resource://" + getPackageName() + "/drawable/addon_chocolates");
        dummyAddons.add(chocolates);

        Addon bear = new Addon();
        bear.setId("2");
        bear.setName("Plush Bear");
        bear.setPrice(12.00);
        bear.setImageUrl("android.resource://" + getPackageName() + "/drawable/addon_bear");
        dummyAddons.add(bear);

        Addon vase = new Addon();
        vase.setId("3");
        vase.setName("Glass Vase");
        vase.setPrice(8.00);
        vase.setImageUrl("android.resource://" + getPackageName() + "/drawable/addon_vase");
        dummyAddons.add(vase);

        addonAdapter.setAddons(dummyAddons);
    }

    private void checkIfInCart() {
        viewModel.getCartItems().observe(this, items -> {
            boolean found = false;
            if (items != null) {
                for (CartItem item : items) {
                    if (item.getProduct().getId().equals(product.getId())) {
                        quantity = item.getQuantity();
                        updateQuantityText();
                        binding.btnAdd.setVisibility(View.GONE);
                        binding.llQuantity.setVisibility(View.VISIBLE);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                quantity = 1;
                updateQuantityText();
                binding.btnAdd.setVisibility(View.VISIBLE);
                binding.llQuantity.setVisibility(View.GONE);
            }
        });
    }

    private void setupUI() {
        binding.tvProductName.setText(product.getName());
        binding.tvPrice.setText(CurrencyFormatter.format(product.getPrice()));
        binding.tvDescription.setText(product.getDescription());

        // Setup Hero Carousel
        String[] imageUrls;
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            imageUrls = new String[]{product.getImageUrl()};
        } else {
            imageUrls = new String[]{"android.resource://" + getPackageName() + "/" + R.drawable.dummy_carousel_1};
        }
        ImagePagerAdapter adapter = new ImagePagerAdapter(imageUrls);
        binding.vpProductGallery.setAdapter(adapter);

        new TabLayoutMediator(binding.tabIndicator, binding.vpProductGallery,
                (tab, position) -> {
                    // No text, just dots
                }).attach();

        // Task 1: Enforce Dynamic Delivery Radius
        double distance = getIntent().getDoubleExtra("distance", 0);
        String shopJson = getIntent().getStringExtra("shop_json");
        double allowedRadiusKm = 5.0; // Default
        
        if (shopJson != null) {
            com.bloom.customer.data.model.Shop shop = new Gson().fromJson(shopJson, com.bloom.customer.data.model.Shop.class);
            if (shop != null && shop.getDeliveryRadiusKm() > 0) {
                allowedRadiusKm = shop.getDeliveryRadiusKm();
            }
        }

        if (distance > allowedRadiusKm * 1000) { // Convert KM to Meters
            binding.btnAdd.setEnabled(false);
            binding.btnAdd.setBackgroundColor(getColor(android.R.color.darker_gray));
            binding.btnCheckout.setEnabled(false);
            binding.btnCheckout.setBackgroundColor(getColor(android.R.color.darker_gray));
            binding.tvOutOfRadius.setVisibility(View.VISIBLE);
            binding.tvOutOfRadius.setText("Shop delivery radius is " + allowedRadiusKm + "km. You are at " + String.format("%.1f", distance/1000) + "km.");
        } else if (!isShopOpen) {
            binding.btnAdd.setEnabled(false);
            binding.btnAdd.setText(R.string.shop_closed);
            binding.btnAdd.setBackgroundColor(getColor(android.R.color.darker_gray));
            binding.btnCheckout.setEnabled(false);
            binding.btnCheckout.setBackgroundColor(getColor(android.R.color.darker_gray));
            binding.tvOutOfRadius.setVisibility(View.VISIBLE);
            binding.tvOutOfRadius.setText("This shop is currently closed.");
        }
        
        updateQuantityText();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnCartTop.setOnClickListener(v -> {
            startActivity(new Intent(this, com.bloom.customer.ui.cart.CartActivity.class));
        });

        binding.btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityText();
                updateCartQuantity();
            } else {
                com.bloom.customer.util.HapticUtil.performSuccess(this);
                viewModel.removeFromCart(product.getId());
                Snackbar snackbar = Snackbar.make(binding.getRoot(), "Removed from cart", Snackbar.LENGTH_SHORT);
                snackbar.setAnchorView(binding.bottomActionBar);
                snackbar.show();
            }
        });

        binding.btnPlus.setOnClickListener(v -> {
            if (quantity < 10) {
                quantity++;
                updateQuantityText();
                updateCartQuantity();
            }
        });

        if (binding.cgSizeSelector != null) {
            binding.cgSizeSelector.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) return;
                int checkedId = checkedIds.get(0);
                if (checkedId == binding.chipSizeDeluxe.getId()) {
                    selectedSize = "Deluxe";
                    sizeMarkup = product.getPrice() * 0.30;
                } else if (checkedId == binding.chipSizePremium.getId()) {
                    selectedSize = "Premium";
                    sizeMarkup = product.getPrice() * 0.60;
                } else {
                    selectedSize = "Regular";
                    sizeMarkup = 0.0;
                }
                updateTotalPrice();
                if (binding.llQuantity.getVisibility() == View.VISIBLE) {
                    updateCartQuantity();
                }
            });
        }

        if (binding.chipGroupMessage != null) {
            binding.chipGroupMessage.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) {
                    setCardMessage("");
                    return;
                }
                int checkedId = checkedIds.get(0);
                if (checkedId == binding.chipBirthday.getId()) setCardMessage("Happy Birthday!");
                else if (checkedId == binding.chipSympathy.getId()) setCardMessage("With deepest sympathy");
                else if (checkedId == binding.chipThinking.getId()) setCardMessage("Thinking of you");
                else if (checkedId == binding.chipCongrats.getId()) setCardMessage("Congratulations!");
            });
        }

        if (binding.llAttachVideo != null) {
            binding.llAttachVideo.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                videoPickerLauncher.launch(intent);
            });
            
            binding.btnRemoveVideo.setOnClickListener(v -> {
                selectedVideoUri = null;
                binding.tvVideoStatus.setText("Attach Video Message ✨");
                binding.ivVideoIcon.setImageResource(android.R.drawable.ic_menu_camera);
                binding.btnRemoveVideo.setVisibility(View.GONE);
            });
        }

        binding.btnAdd.setOnClickListener(v -> {
            com.bloom.customer.util.HapticUtil.performSuccess(this);
            CartItem cartItem = new CartItem(product);
            cartItem.setQuantity(1);
            cartItem.setSize(selectedSize);
            String message = binding.etCardMessage != null ? binding.etCardMessage.getText().toString() : "";
            cartItem.setCardMessage(message);
            if (selectedVideoUri != null) {
                cartItem.setMediaUrl(selectedVideoUri.toString());
            }
            cartItem.setAddons(addonAdapter.getSelectedAddons());

            handleAddToCart(cartItem);
            // After adding, checkIfInCart will trigger via observer and show quantity selector
        });

        binding.btnCheckout.setOnClickListener(v -> {
            com.bloom.customer.util.HapticUtil.performSuccess(this);
            Intent intent = new Intent(this, com.bloom.customer.ui.cart.CartActivity.class);
            startActivity(intent);
        });
    }

    private void updateCartQuantity() {
        CartItem cartItem = new CartItem(product);
        cartItem.setQuantity(quantity);
        cartItem.setSize(selectedSize);
        String message = binding.etCardMessage != null ? binding.etCardMessage.getText().toString() : "";
        cartItem.setCardMessage(message);
        if (selectedVideoUri != null) {
            cartItem.setMediaUrl(selectedVideoUri.toString());
        }
        cartItem.setAddons(addonAdapter.getSelectedAddons());
        viewModel.addToCart(cartItem);
    }

    private void setCardMessage(String message) {
        if (binding.etCardMessage != null) {
            binding.etCardMessage.setText(message);
        }
    }

    private void updateQuantityText() {
        binding.tvQuantity.setText(String.valueOf(quantity));
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double total = (product.getPrice() + sizeMarkup) * quantity;
        for (com.bloom.customer.data.model.Addon addon : addonAdapter.getSelectedAddons()) {
            total += addon.getPrice() * quantity; // Addons scale with bouquet quantity in this app's logic
        }
        binding.tvPrice.setText(com.bloom.customer.util.CurrencyFormatter.format(total));
        binding.btnAdd.setText(R.string.add_to_cart);
    }

    private void handleAddToCart(CartItem item) {
        String currentShopId = viewModel.getCartShopId();
        
        if (currentShopId != null && !currentShopId.equals(product.getShopId())) {
            showClearCartDialog(item);
        } else {
            viewModel.addToCart(item);
            Snackbar snackbar = Snackbar.make(binding.getRoot(), "Added to cart", Snackbar.LENGTH_SHORT);
            snackbar.setAnchorView(binding.bottomActionBar);
            snackbar.show();
        }
    }

    private void showClearCartDialog(CartItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Clear Cart?")
                .setMessage("Your cart contains items from another shop. Clear cart to add this item?")
                .setPositiveButton("Clear & Add", (dialog, which) -> {
                    viewModel.clearCart();
                    viewModel.addToCart(item);
                    Snackbar snackbar = Snackbar.make(binding.getRoot(), "Added to cart", Snackbar.LENGTH_SHORT);
                    snackbar.setAnchorView(binding.bottomActionBar);
                    snackbar.show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ViewHolder> {
        private final String[] imageUrls;

        public ImagePagerAdapter(String[] imageUrls) {
            this.imageUrls = imageUrls;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Glide.with(holder.imageView.getContext())
                    .load(imageUrls[position])
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imageUrls.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ViewHolder(@NonNull ImageView itemView) {
                super(itemView);
                this.imageView = itemView;
            }
        }
    }
}
