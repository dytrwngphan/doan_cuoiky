package com.example.doancuoiki_dotcuoi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.doancuoiki_dotcuoi.R;
import java.util.List;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder> {
    private Context context;
    private List<String> imageUrls;
    private int mainImagePos = 0;

    // Interface callback khi xóa ảnh
    public interface OnImageRemoveListener {
        void onRemove(int position);
    }
    private OnImageRemoveListener removeListener;
    public void setOnImageRemoveListener(OnImageRemoveListener listener) {
        this.removeListener = listener;
    }

    public ProductImageAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }
    public int getMainImagePos() {
        return mainImagePos;
    }
    public void setMainImagePos(int pos) {
        mainImagePos = pos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_image_selected, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int pos) {
        String url = imageUrls.get(pos);
        Glide.with(context).load(url).centerCrop().into(holder.imgSelected);

        // Viền vàng cho ảnh đại diện
        if (pos == mainImagePos) {
            holder.imgSelected.setBackgroundResource(R.drawable.bg_round_img_selected);
        } else {
            holder.imgSelected.setBackgroundResource(R.drawable.bg_round_img);
        }

        // Chọn ảnh làm đại diện
        holder.imgSelected.setOnClickListener(v -> setMainImagePos(pos));

        // Xóa ảnh
        holder.imgDelete.setOnClickListener(v -> {
            if (removeListener != null) removeListener.onRemove(pos);
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSelected, imgDelete;
        public ImageViewHolder(@NonNull View v) {
            super(v);
            imgSelected = v.findViewById(R.id.imgSelected);
            imgDelete = v.findViewById(R.id.imgDelete);
        }
    }
}
