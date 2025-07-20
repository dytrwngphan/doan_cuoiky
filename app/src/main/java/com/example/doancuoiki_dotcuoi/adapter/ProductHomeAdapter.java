package com.example.doancuoiki_dotcuoi.adapter;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.model.Product;
import com.example.doancuoiki_dotcuoi.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProductHomeAdapter extends RecyclerView.Adapter<ProductHomeAdapter.ProductViewHolder> {

    private List<Product> list;
    private final Context context;
    private final OnProductClickListener listener;
    private final Map<String, User> userCache = new HashMap<>();

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductHomeAdapter(Context context, List<Product> list, OnProductClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void setList(List<Product> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_home, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product p = list.get(position);

        holder.tvTitle.setText(p.getTitle());
        holder.tvPrice.setText(String.format("%,.0fđ", p.getPrice()));
        holder.tvCategory.setText("Danh mục: " + p.getCategory());
        holder.tvDesc.setText(p.getDescription());
        holder.tvStatus.setText(p.getStatus());

        // Xử lý location
        String location = p.getLocation();
        if (location != null && location.startsWith("Lat:")) {
            double lat = p.getLatitude();
            double lng = p.getLongitude();
            getAddressFromLatLng(holder.tvLocation, lat, lng, context);
        } else {
            holder.tvLocation.setText(location != null ? location : "Địa chỉ không xác định");
        }

        // Hiển thị thời gian tạo post
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        if (p.getCreatedAt() > 0) {
            holder.tvTime.setText(sdf.format(new Date(p.getCreatedAt())));
        } else {
            holder.tvTime.setText("");
        }


        // Ảnh sản phẩm
        if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(p.getImageUrls().get(0))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .centerCrop()
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Tải thông tin user (avatar + tên) từ Firestore, cache theo ownerId
        String ownerId = p.getOwnerId();
        if (userCache.containsKey(ownerId)) {
            User u = userCache.get(ownerId);
            if (u != null) {
                holder.tvUserName.setText(u.getFullName());
                if (u.getProfileImage() != null && !u.getProfileImage().isEmpty()) {
                    Glide.with(context)
                            .load(u.getProfileImage())
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile)
                            .into(holder.imgUserAvatar);
                } else {
                    holder.imgUserAvatar.setImageResource(R.drawable.ic_profile);
                }
            }
        } else {
            // Đang tải
            holder.tvUserName.setText("Đang tải...");
            holder.imgUserAvatar.setImageResource(R.drawable.ic_profile);
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(ownerId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            User u = doc.toObject(User.class);
                            userCache.put(ownerId, u);
                            holder.tvUserName.setText(u.getFullName());
                            if (u.getProfileImage() != null && !u.getProfileImage().isEmpty()) {
                                Glide.with(context)
                                        .load(u.getProfileImage())
                                        .circleCrop()
                                        .placeholder(R.drawable.ic_profile)
                                        .into(holder.imgUserAvatar);
                            } else {
                                holder.imgUserAvatar.setImageResource(R.drawable.ic_profile);
                            }
                        }
                    });
        }

        holder.btnViewDetail.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(p);
        });
    }

    private void getAddressFromLatLng(TextView textView, double lat, double lng, Context context) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                String address;
                if (addresses != null && !addresses.isEmpty()) {
                    address = addresses.get(0).getAddressLine(0);
                } else {
                    address = "Lat: " + lat + ", Lng: " + lng;
                }
                String finalAddress = address;
                if (textView != null) {
                    textView.post(() -> {
                        if (textView != null) textView.setText(finalAddress);
                    });
                }
            } catch (Exception e) {
                if (textView != null) {
                    textView.post(() -> textView.setText("Lat: " + lat + ", Lng: " + lng));
                }
            }
        }).start();
    }


    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, imgUserAvatar;
        TextView tvTitle, tvPrice, tvCategory, tvLocation, tvDesc, tvStatus, tvUserName, tvTime;
        Button btnViewDetail;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
            tvTitle = itemView.findViewById(R.id.tvProductTitle);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvCategory = itemView.findViewById(R.id.tvProductCategory);
            tvLocation = itemView.findViewById(R.id.tvProductLocation); // ĐÚNG
            tvDesc = itemView.findViewById(R.id.tvProductDesc);
            tvStatus = itemView.findViewById(R.id.tvProductStatus);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    public void clearUserCache() {
        userCache.clear();
    }
}
