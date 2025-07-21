package com.example.doancuoiki_dotcuoi.adapter;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.model.CartItem;
import java.util.*;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    public interface CartListener {
        void onItemCheckedChanged(CartItem item, boolean isChecked);
        void onRemoveClicked(CartItem item);
    }

    private Context context;
    private List<CartItem> itemList;
    private Set<String> selectedItemIds = new HashSet<>();
    private CartListener listener;

    public CartAdapter(Context context, List<CartItem> itemList, CartListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    public List<CartItem> getSelectedItems() {
        List<CartItem> result = new ArrayList<>();
        for (CartItem item : itemList) {
            if (selectedItemIds.contains(item.getCartItemId())) {
                result.add(item);
            }
        }
        return result;
    }

    public void selectAll(boolean select) {
        selectedItemIds.clear();
        if (select) {
            for (CartItem item : itemList) {
                selectedItemIds.add(item.getCartItemId());
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int pos) {
        CartItem item = itemList.get(pos);
        holder.tvProductTitle.setText(item.getTitle());
        holder.tvOfferPrice.setText(String.format(Locale.getDefault(), "Giá: %,.0fđ", item.getPrice()));

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_image_placeholder)
                .centerCrop()
                .into(holder.imgProduct);

        holder.checkboxSelect.setOnCheckedChangeListener(null);
        holder.checkboxSelect.setChecked(selectedItemIds.contains(item.getCartItemId()));
        holder.checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedItemIds.add(item.getCartItemId());
            else selectedItemIds.remove(item.getCartItemId());
            if (listener != null) listener.onItemCheckedChanged(item, isChecked);
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemoveClicked(item);
        });
    }

    @Override
    public int getItemCount() { return itemList.size(); }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductTitle, tvOfferPrice;
        CheckBox checkboxSelect;
        ImageButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductTitle = itemView.findViewById(R.id.tvProductTitle);
            tvOfferPrice = itemView.findViewById(R.id.tvOfferPrice);
            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
