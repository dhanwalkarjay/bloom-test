package com.bloom.customer.ui.lux;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bloom.R;
import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.data.model.Product;
import com.bloom.customer.ui.auth.LoginActivity;
import com.bloom.customer.ui.product.ProductDetailActivity;
import com.bloom.databinding.FragmentLuxBinding;
import com.google.gson.Gson;

public class LuxFragment extends Fragment {

    private FragmentLuxBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLuxBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
    }

    private void setupListeners() {
        binding.btnMenu.setOnClickListener(v -> Toast.makeText(requireContext(), "Menu clicked", Toast.LENGTH_SHORT).show());
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

        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra("product_json", new Gson().toJson(product));
        intent.putExtra("is_shop_open", true);
        startActivity(intent);
    }

    private String resourceUri(int resId) {
        return Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + resId).toString();
    }

    private void openProfile() {
        if (!SessionManager.getInstance(requireContext()).isLoggedIn()) {
            startActivity(new Intent(requireContext(), LoginActivity.class));
        } else {
            Toast.makeText(requireContext(), "Already on Profile (handled by HomeActivity)", Toast.LENGTH_SHORT).show();
            // Since this is in HomeActivity now, clicking profile should navigate to ProfileFragment.
            // But usually this profile icon is just an extra entry point.
            // HomeActivity handles actual bottom nav.
            // I'll just leave it as is or show a toast if logged in.
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
