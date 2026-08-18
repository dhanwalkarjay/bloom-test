package com.bloom.customer.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bloom.R;
import com.bloom.customer.data.model.Occasion;

import java.util.ArrayList;
import java.util.List;

public class OccasionAdapter extends RecyclerView.Adapter<OccasionAdapter.OccasionViewHolder> {

    private List<Occasion> occasions = new ArrayList<>();
    private final OnOccasionClickListener listener;

    public interface OnOccasionClickListener {
        void onDeleteClick(Occasion occasion);
        void onEditClick(Occasion occasion);
    }

    public OccasionAdapter(OnOccasionClickListener listener) {
        this.listener = listener;
    }

    public void setOccasions(List<Occasion> occasions) {
        this.occasions = occasions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OccasionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_occasion, parent, false);
        return new OccasionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OccasionViewHolder holder, int position) {
        Occasion occasion = occasions.get(position);
        holder.bind(occasion, listener);
    }

    @Override
    public int getItemCount() {
        return occasions.size();
    }

    static class OccasionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvOccasionTitle;
        private final TextView tvOccasionRecipient;
        private final TextView tvOccasionDate;
        private final ImageView btnDelete;
        private final ImageView btnEdit;

        public OccasionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOccasionTitle = itemView.findViewById(R.id.tvOccasionTitle);
            tvOccasionRecipient = itemView.findViewById(R.id.tvOccasionRecipient);
            tvOccasionDate = itemView.findViewById(R.id.tvOccasionDate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }

        public void bind(Occasion occasion, OnOccasionClickListener listener) {
            tvOccasionTitle.setText(occasion.getTitle());
            tvOccasionRecipient.setText("For " + occasion.getRecipientName() + " (" + occasion.getRecipientRelation() + ")");
            tvOccasionDate.setText(occasion.getTargetDate());
            
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(occasion);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(occasion);
                }
            });
        }
    }
}
