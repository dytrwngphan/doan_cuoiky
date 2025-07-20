package com.example.doancuoiki_dotcuoi.adapter;

import android.content.Context;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.model.Category;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private final Context context;
    private final List<Category> categoryList;
    private final OnCategoryClickListener listener;
    private int selectedPos = 0; // Mặc định chọn category đầu tiên

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context, List<Category> list, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int pos) {
        Category cat = categoryList.get(pos);
        holder.tvCategoryName.setText(cat.getName());
        holder.imgCategory.setImageResource(cat.getIconResId());
        holder.itemView.setBackgroundResource(pos == selectedPos ? R.drawable.bg_category_card_selected : R.drawable.bg_category_card);

        holder.itemView.setOnClickListener(v -> {
            int previousPos = selectedPos;
            selectedPos = holder.getAdapterPosition();
            notifyItemChanged(previousPos);
            notifyItemChanged(selectedPos);
            listener.onCategoryClick(cat);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCategory;
        TextView tvCategoryName;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
