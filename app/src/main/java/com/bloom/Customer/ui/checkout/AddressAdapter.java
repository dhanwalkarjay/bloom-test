package com.bloom.customer.ui.checkout;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bloom.customer.data.model.Address;
import com.bloom.databinding.ItemAddressBinding;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private final List<Address> addresses = new ArrayList<>();
    private int selectedPosition = -1;
    private OnAddressSelectedListener listener;

    public interface OnAddressSelectedListener {
        void onAddressSelected(Address address);
    }

    public void setListener(OnAddressSelectedListener listener) {
        this.listener = listener;
    }

    public void setAddresses(List<Address> newAddresses) {
        addresses.clear();
        if (newAddresses != null) {
            addresses.addAll(newAddresses);
        }
        notifyDataSetChanged();
    }

    public Address getSelectedAddress() {
        if (selectedPosition != -1) return addresses.get(selectedPosition);
        return null;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAddressBinding binding = ItemAddressBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AddressViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        holder.bind(addresses.get(position), position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    class AddressViewHolder extends RecyclerView.ViewHolder {
        private final ItemAddressBinding binding;

        AddressViewHolder(ItemAddressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Address address, boolean isSelected) {
            binding.tvAddressLine.setText(address.getAddressLine());
            binding.tvCity.setText(address.getCity());
            binding.rbSelected.setChecked(isSelected);

            itemView.setOnClickListener(v -> {
                selectedPosition = getAdapterPosition();
                notifyDataSetChanged();
                if (listener != null) listener.onAddressSelected(address);
            });
        }
    }
}
