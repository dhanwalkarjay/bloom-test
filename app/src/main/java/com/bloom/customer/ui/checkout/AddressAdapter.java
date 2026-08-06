package com.bloom.customer.ui.checkout;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bloom.R;
import com.bloom.customer.data.model.Address;
import com.bloom.databinding.ItemAddressBinding;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private final List<Address> addresses = new ArrayList<>();
    private int selectedPosition = -1;
    private OnAddressInteractionListener listener;

    public interface OnAddressInteractionListener {
        void onAddressSelected(Address address);
        void onAddressDelete(Address address);
    }

    public void setListener(OnAddressInteractionListener listener) {
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
            binding.tvAddressLabel.setText(address.getLabel());
            binding.tvAddressLine.setText(address.getAddressLine());
            
            String recipient = address.getRecipientName();
            if (address.getRecipientPhone() != null && !address.getRecipientPhone().isEmpty()) {
                recipient += ", " + address.getRecipientPhone();
            }
            binding.tvRecipient.setText(recipient);

            // Visual selection state
            if (isSelected) {
                binding.cvAddress.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.orders_primary));
                binding.cvAddress.setStrokeWidth(4);
            } else {
                binding.cvAddress.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.orders_outline_variant));
                binding.cvAddress.setStrokeWidth(2);
            }

            itemView.setOnClickListener(v -> {
                selectedPosition = getAdapterPosition();
                notifyDataSetChanged();
                if (listener != null) listener.onAddressSelected(address);
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onAddressDelete(address);
            });
        }
    }
}
