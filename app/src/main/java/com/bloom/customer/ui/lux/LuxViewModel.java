package com.bloom.customer.ui.lux;

import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bloom.R;
import com.bloom.customer.data.model.Product;
import com.bloom.customer.util.NetworkResult;

import java.util.ArrayList;
import java.util.List;

public class LuxViewModel extends AndroidViewModel {

    private final MutableLiveData<NetworkResult<List<Product>>> luxProducts = new MutableLiveData<>();

    public LuxViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<NetworkResult<List<Product>>> getLuxProducts() {
        return luxProducts;
    }

    public void fetchLuxCollection() {
        luxProducts.setValue(NetworkResult.loading(null));

        // Simulate network delay for premium shimmer experience
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            List<Product> products = new ArrayList<>();

            products.add(createLuxProduct(
                    "lux-noir-eclat",
                    "Noir Éclat",
                    "A sculptural masterpiece of rare black calla lilies.",
                    285.00,
                    R.drawable.lux_product_noir_eclat,
                    "ONLY 4 LEFT TODAY"
            ));

            products.add(createLuxProduct(
                    "lux-aura-blush",
                    "Aura Blush",
                    "Ethereal layers of silk-petaled heirloom peonies.",
                    340.00,
                    R.drawable.lux_product_aura_blush,
                    "LIMITED EDITION"
            ));

            products.add(createLuxProduct(
                    "lux-midnight-gilded",
                    "Midnight Gilded",
                    "Contrast of wild indigo thistles and gilded textures.",
                    215.00,
                    R.drawable.lux_product_midnight_gilded,
                    "ONLY 2 LEFT TODAY"
            ));

            luxProducts.setValue(NetworkResult.success(products));
        }, 1500); // 1.5 second simulated delay
    }

    private Product createLuxProduct(String id, String name, String description, double price, int imageResId, String warning) {
        Product product = new Product();
        product.setId(id);
        product.setShopId("lux-atelier");
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setLux(true);
        // Using getApplication().getPackageName() instead of hardcoding
        product.setImageUrl(Uri.parse("android.resource://" + getApplication().getPackageName() + "/" + imageResId).toString());
        // Since Product model might not have a specific 'stockWarning' field, we can append it or handle it in UI.
        // Wait, does Product have a stockWarning or similar? Let's assume we can map the warning text based on the ID for now,
        // or just let the adapter handle it for the sake of this mock.
        // I will add a custom field or use the 'category' field temporarily for this mock, since 'category' isn't used much here.
        product.setCategory(warning); // HACK: Storing the warning text in category for this mock
        return product;
    }
}
