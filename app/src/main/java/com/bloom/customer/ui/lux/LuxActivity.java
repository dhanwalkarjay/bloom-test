package com.bloom.customer.ui.lux;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.bloom.R;
import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.model.Product;
import com.bloom.customer.ui.auth.LoginActivity;
import com.bloom.customer.ui.home.HomeActivity;
import com.bloom.customer.ui.orderhistory.OrdersActivity;
import com.bloom.customer.ui.product.ProductDetailActivity;
import com.bloom.customer.ui.search.SearchActivity;
import com.bloom.databinding.ActivityLuxBinding;
import com.google.gson.Gson;

public class LuxActivity extends AppCompatActivity {

    private ActivityLuxBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLuxBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        binding.btnMenu.setOnClickListener(v -> finish());
        binding.ivProfile.setOnClickListener(v -> openProfile());

        binding.productNoir.setOnClickListener(v -> openProductDetail(
                "lux-noir-eclat",
                "Noir Éclat",
                "A sculptural masterpiece of rare black calla lilies.",
                285.00,
                R.drawable.lux_product_noir_eclat
        ));

        binding.productAura.setOnClickListener(v -> openProductDetail(
                "lux-aura-blush",
                "Aura Blush",
                "Ethereal layers of silk-petaled heirloom peonies.",
                340.00,
                R.drawable.lux_product_aura_blush
        ));

        binding.productMidnight.setOnClickListener(v -> openProductDetail(
                "lux-midnight-gilded",
                "Midnight Gilded",
                "Contrast of wild indigo thistles and gilded textures.",
                215.00,
                R.drawable.lux_product_midnight_gilded
        ));

        binding.navHome.setOnClickListener(v -> openHome());
        binding.navLux.setOnClickListener(v -> binding.luxScrollView.smoothScrollTo(0, 0));
        binding.navSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        binding.navOrders.setOnClickListener(v -> openOrders());
        binding.navProfile.setOnClickListener(v -> openProfile());
    }

    private void openProductDetail(String id, String name, String description, double price, int imageResId) {
        Product product = new Product();
        product.setId(id);
        product.setShopId("lux-atelier");
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setLux(true);
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

    private void openProtectedHomeSection() {
        if (SessionManager.getInstance(this).isLoggedIn()) {
            openHome();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void openProfile() {
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
