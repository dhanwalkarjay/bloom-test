package com.bloom.customer.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bloom.customer.ui.checkout.AddressSelectActivity;
import com.bloom.customer.ui.checkout.AddresslessGiftingActivity;
import com.bloom.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DeliveryOptionsBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_delivery_options, container, false);
        
        View cardAddress = view.findViewById(R.id.cardAddressDelivery);
        View cardAddressless = view.findViewById(R.id.cardAddresslessDelivery);
        
        cardAddress.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddressSelectActivity.class));
            dismiss();
        });
        
        cardAddressless.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddresslessGiftingActivity.class));
            dismiss();
        });
        
        return view;
    }
}
