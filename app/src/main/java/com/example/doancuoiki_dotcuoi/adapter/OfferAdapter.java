package com.example.doancuoiki_dotcuoi.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.model.CartItem;
import com.example.doancuoiki_dotcuoi.model.Offer;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {
    private Context context;
    private List<Offer> offerList;
    private String currentUserId; // Người đang đăng nhập

    public OfferAdapter(Context context, List<Offer> offerList, String currentUserId) {
        this.context = context;
        this.offerList = offerList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_offer, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        Offer offer = offerList.get(position);
        // Show ảnh sản phẩm
        Glide.with(context)
                .load(offer.getProductImage())
                .placeholder(R.drawable.ic_image_placeholder)
                .into(holder.imgProduct);
        holder.tvProductTitle.setText(offer.getProductTitle());
        holder.tvOfferPrice.setText(String.format("Giá trả: %,.0fđ", offer.getOfferPrice()));
        holder.tvCreatedAt.setText(new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault()).format(new Date(offer.getCreatedAt())));
        holder.tvUserName.setText(offer.getBuyerName() != null ? "Người mua: " + offer.getBuyerName() : "");

        // Hiển thị status + ẩn hiện nút
        holder.btnAccept.setVisibility(View.GONE);
        holder.btnCounter.setVisibility(View.GONE);
        holder.btnReject.setVisibility(View.GONE);

        // Xác định user đang là seller hay buyer
        boolean isSeller = currentUserId.equals(offer.getSellerId());
        boolean isBuyer = currentUserId.equals(offer.getBuyerId());

        // Status: pending, countered, accepted, rejected
        if (isSeller) {
            // Seller xem
            switch (offer.getStatus()) {
                case "pending":
                    holder.tvStatus.setText("Chờ xác nhận");
                    holder.btnAccept.setVisibility(View.VISIBLE);
                    holder.btnCounter.setVisibility(View.VISIBLE);
                    holder.btnReject.setVisibility(View.VISIBLE);
                    break;
                case "countered":
                    holder.tvStatus.setText("Đã phản hồi giá, chờ buyer xác nhận");
                    break;
                case "accepted":
                    holder.tvStatus.setText("Đã chấp nhận");
                    break;
                case "rejected":
                    holder.tvStatus.setText("Đã từ chối");
                    break;
            }
        } else if (isBuyer) {
            // Buyer xem
            switch (offer.getStatus()) {
                case "pending":
                    holder.tvStatus.setText("Đang chờ seller xác nhận");
                    break;
                case "countered":
                    holder.tvStatus.setText("Seller phản hồi giá, chờ bạn xác nhận");
                    holder.btnAccept.setVisibility(View.VISIBLE);
                    holder.btnCounter.setVisibility(View.VISIBLE);
                    holder.btnReject.setVisibility(View.VISIBLE);
                    break;
                case "accepted":
                    holder.tvStatus.setText("Đã chấp nhận - chờ thanh toán");
                    break;
                case "rejected":
                    holder.tvStatus.setText("Đã từ chối");
                    break;
            }
        }

        // Xử lý các nút
        holder.btnAccept.setOnClickListener(v -> {
            updateOfferStatus(offer, "accepted", null);
        });

        holder.btnReject.setOnClickListener(v -> {
            updateOfferStatus(offer, "rejected", null);
        });

        holder.btnCounter.setOnClickListener(v -> {
            // Hiển thị dialog nhập giá mới
            final EditText input = new EditText(context);
            input.setHint("Nhập giá muốn trả (VNĐ)");
            new AlertDialog.Builder(context)
                    .setTitle(isSeller ? "Phản hồi giá cho buyer" : "Trả giá lại cho seller")
                    .setView(input)
                    .setPositiveButton("Gửi", (dialog, which) -> {
                        String priceStr = input.getText().toString().trim();
                        if (!TextUtils.isEmpty(priceStr)) {
                            double newPrice = Double.parseDouble(priceStr);
                            updateOfferStatus(offer, "countered", newPrice);
                        }
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return offerList.size();
    }

    static class OfferViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductTitle, tvOfferPrice, tvCreatedAt, tvUserName, tvStatus;
        Button btnAccept, btnCounter, btnReject;

        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductTitle = itemView.findViewById(R.id.tvProductTitle);
            tvOfferPrice = itemView.findViewById(R.id.tvOfferPrice);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnCounter = itemView.findViewById(R.id.btnCounter);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }

    // Cập nhật offer trên Firestore
    private void updateOfferStatus(Offer offer, String newStatus, Double newPrice) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("lastActionBy", currentUserId.equals(offer.getSellerId()) ? "seller" : "buyer");
        if (newPrice != null) updates.put("offerPrice", newPrice);

        db.collection("offers").document(offer.getOfferId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Đã cập nhật!", Toast.LENGTH_SHORT).show();
                    // Khi offer được chấp nhận bởi cả seller hoặc buyer đều tạo cart item
                    if ("accepted".equals(newStatus)) {
                        addToCartWhenOfferAccepted(offer);
                    }
                });
    }


    // Hàm này sẽ tạo CartItem khi offer được accepted
    private void addToCartWhenOfferAccepted(Offer offer) {
        String cartItemId = UUID.randomUUID().toString();
        CartItem cartItem = new CartItem();
        cartItem.setCartItemId(cartItemId);
        cartItem.setProductId(offer.getProductId());
        cartItem.setOfferId(offer.getOfferId());
        cartItem.setTitle(offer.getProductTitle());
        cartItem.setImageUrl(offer.getProductImage());
        cartItem.setPrice(offer.getOfferPrice());
        cartItem.setStatus("pending"); // Chưa thanh toán
        cartItem.setUserId(offer.getBuyerId());

        FirebaseFirestore.getInstance().collection("cart")
                .document(cartItemId)
                .set(cartItem);
    }

}
