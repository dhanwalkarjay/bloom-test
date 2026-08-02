package com.bloom.customer.ui.search;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.R;
import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.model.Product;
import com.bloom.customer.ui.auth.LoginActivity;
import com.bloom.customer.ui.cart.CartActivity;
import com.bloom.customer.ui.home.HomeActivity;
import com.bloom.customer.ui.lux.LuxActivity;
import com.bloom.customer.ui.orderhistory.OrdersActivity;
import com.bloom.customer.ui.product.ProductDetailActivity;
import com.bloom.databinding.ActivitySearchBinding;
import com.bloom.databinding.IncludeSearchProductCardBinding;
import com.google.gson.Gson;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private boolean showShops = true;
    private boolean showBouquets = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupProductCards();
        setupListeners();
    }

    private void setupProductCards() {
        bindProduct(
                binding.cardVelvetLove,
                R.drawable.search_bouquet_velvet_love,
                "BESTSELLER",
                "4.9",
                "Velvet Love",
                "$85.00",
                true
        );
        bindProduct(
                binding.cardPureGrace,
                R.drawable.search_bouquet_pure_grace,
                "PREMIUM",
                "4.7",
                "Pure Grace",
                "$120.00",
                false
        );
        bindProduct(
                binding.cardGoldenAnniversary,
                R.drawable.search_bouquet_golden_anniversary,
                "FRESH",
                "4.8",
                "Golden Anniversary",
                "$65.00",
                false
        );
        bindProduct(
                binding.cardMidnightBloom,
                R.drawable.search_bouquet_midnight_bloom,
                "LUXURY",
                "5.0",
                "Midnight Bloom",
                "$195.00",
                true
        );
    }

    private void bindProduct(
            IncludeSearchProductCardBinding card,
            int imageResId,
            String tag,
            String rating,
            String name,
            String price,
            boolean favorite
    ) {
        card.ivProduct.setImageResource(imageResId);
        card.tvTag.setText(tag);
        card.tvRating.setText(rating);
        card.tvProductName.setText(name);
        card.tvPrice.setText(price);
        card.btnFavorite.setImageResource(favorite ? R.drawable.ic_search_heart_filled : R.drawable.ic_search_heart);
        card.getRoot().setOnClickListener(v -> openProductDetail(name, tag, price, imageResId));
        card.btnFavorite.setOnClickListener(v -> {
            card.btnFavorite.setImageResource(R.drawable.ic_search_heart_filled);
            Toast.makeText(this, "Saved to favorites", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        binding.btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        binding.btnMenu.setOnClickListener(v -> finish());
        binding.tvSeeAll.setOnClickListener(v -> Toast.makeText(this, "Showing all shops", Toast.LENGTH_SHORT).show());
        binding.shopRosies.setOnClickListener(v -> Toast.makeText(this, "Rosie's Petals", Toast.LENGTH_SHORT).show());
        binding.shopBloomBar.setOnClickListener(v -> Toast.makeText(this, "The Bloom Bar", Toast.LENGTH_SHORT).show());

        binding.chipShops.setOnClickListener(v -> {
            showShops = !showShops;
            updateSections();
        });
        binding.chipBouquets.setOnClickListener(v -> {
            showBouquets = !showBouquets;
            updateSections();
        });
        binding.chipFilter.setOnClickListener(v -> Toast.makeText(this, "Filters coming soon", Toast.LENGTH_SHORT).show());
        binding.chipSort.setOnClickListener(v -> Toast.makeText(this, "Sort coming soon", Toast.LENGTH_SHORT).show());

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Toast.makeText(this, "Search updated", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        binding.navShop.setOnClickListener(v -> openHome());
        binding.navExplore.setOnClickListener(v -> binding.scrollContent.smoothScrollTo(0, 0));
        binding.navLux.setOnClickListener(v -> startActivity(new Intent(this, LuxActivity.class)));
        binding.navOrders.setOnClickListener(v -> openOrders());
        binding.navProfile.setOnClickListener(v -> openProtectedArea());
    }

    private void updateSections() {
        if (!showShops && !showBouquets) {
            showShops = true;
            showBouquets = true;
        }

        binding.shopsSection.setVisibility(showShops ? View.VISIBLE : View.GONE);
        binding.bouquetsSection.setVisibility(showBouquets ? View.VISIBLE : View.GONE);
        binding.chipShops.setBackgroundResource(showShops ? R.drawable.bg_search_chip_active : R.drawable.bg_search_chip_inactive);
        binding.chipBouquets.setBackgroundResource(showBouquets ? R.drawable.bg_search_chip_active : R.drawable.bg_search_chip_inactive);
    }

    private void openProductDetail(String name, String tag, String priceText, int imageResId) {
        Product product = new Product();
        product.setId("search-" + name.toLowerCase().replace(" ", "-"));
        product.setShopId("explore-marketplace");
        product.setName(name);
        product.setDescription(tag + " anniversary bouquet curated by Bloom.");
        product.setPrice(Double.parseDouble(priceText.replace("$", "")));
        product.setLux("LUXURY".equals(tag));
        product.setImageUrl(resourceUri(imageResId));

        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_json", new Gson().toJson(product));
        intent.putExtra("is_shop_open", true);
        startActivity(intent);
    }

    private String resourceUri(int resId) {
        return Uri.parse("android.resource://" + getPackageName() + "/" + resId).toString();
    }

    private void openHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void openProtectedArea() {
        if (SessionManager.getInstance(this).isLoggedIn()) {
            openHome();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void openOrders() {
        startActivity(new Intent(this, OrdersActivity.class));
    }
}
