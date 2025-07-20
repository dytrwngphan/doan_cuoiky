package com.example.doancuoiki_dotcuoi.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.model.Product;
import com.example.doancuoiki_dotcuoi.model.User;

import java.text.SimpleDateFormat;
import java.util.*;

public class MyProductAdapter extends RecyclerView.Adapter<MyProductAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> productList;
    private User currentUser;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onViewDetail(Product product);
        void onDelete(Product product, int position);
    }

    public MyProductAdapter(Context context, List<Product> productList, User user, OnProductActionListener listener) {
        this.context = context;
        this.productList = productList;
        this.currentUser = user;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product p = productList.get(position);
        // Ảnh sản phẩm
        if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) {
            Glide.with(context).load(p.getImageUrls().get(0)).placeholder(R.drawable.ic_image_placeholder).into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_image_placeholder);
        }
        // Tên sản phẩm
        holder.tvProductTitle.setText(p.getTitle());
        // Giá sản phẩm
        holder.tvProductPrice.setText(String.format("%,.0fđ", p.getPrice()));
        // Mô tả
        holder.tvProductDesc.setText(p.getDescription());
        // Danh mục
        holder.tvProductCategory.setText("Danh mục: " + p.getCategory());
        // Địa chỉ dạng text
        holder.tvProductLocation.setText(p.getAddressText() != null ? p.getAddressText() : "");
        // Trạng thái
        holder.tvProductStatus.setText(p.getStatus());
        // Thời gian đăng
        if (p.getCreatedAt() > 0) {
            String time = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date(p.getCreatedAt()));
            holder.tvTime.setText(time);
        } else {
            holder.tvTime.setText("");
        }

        // Avatar và tên user (của chủ sản phẩm là user hiện tại)
        if (currentUser != null && currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
            String img = currentUser.getProfileImage();
            Glide.with(context)
                    .load(img.startsWith("content://") ? Uri.parse(img) : img)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.imgUserAvatar);
        } else {
            holder.imgUserAvatar.setImageResource(R.drawable.ic_profile);
        }
        holder.tvUserName.setText(currentUser != null ? currentUser.getFullName() : "Bạn");

        // Xem chi tiết
        holder.btnViewDetail.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetail(p);
        });
        // Xoá sản phẩm
        holder.btnDeleteProduct.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(p, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, imgUserAvatar;
        TextView tvProductTitle, tvProductPrice, tvProductDesc, tvProductCategory, tvProductLocation, tvProductStatus, tvUserName, tvTime;
        Button btnViewDetail, btnDeleteProduct;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
            tvProductTitle = itemView.findViewById(R.id.tvProductTitle);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductDesc = itemView.findViewById(R.id.tvProductDesc);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);
            tvProductLocation = itemView.findViewById(R.id.tvProductLocation);
            tvProductStatus = itemView.findViewById(R.id.tvProductStatus);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
            btnDeleteProduct = itemView.findViewById(R.id.btnDeleteProduct);
        }
    }
}
