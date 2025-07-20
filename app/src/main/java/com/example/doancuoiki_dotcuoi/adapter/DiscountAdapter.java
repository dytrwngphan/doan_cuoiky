package com.example.doancuoiki_dotcuoi.adapter;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.model.Discount;
import java.util.List;

public class DiscountAdapter extends RecyclerView.Adapter<DiscountAdapter.ViewHolder> {
    private final List<Discount> discounts;
    private final OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDelete(Discount discount, int position);
    }

    public DiscountAdapter(List<Discount> discounts, OnDeleteClickListener listener) {
        this.discounts = discounts;
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discount, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Discount d = discounts.get(pos);
        h.tvCode.setText("Mã: " + d.getCode());
        h.tvAmount.setText("Giảm: " + ((int)d.getAmount()) + "đ");
        h.btnDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null)
                onDeleteClickListener.onDelete(d, pos);
        });
    }

    @Override
    public int getItemCount() { return discounts.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvAmount;
        ImageButton btnDelete;
        public ViewHolder(@NonNull View v) {
            super(v);
            tvCode = v.findViewById(R.id.tvCode);
            tvAmount = v.findViewById(R.id.tvAmount);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
