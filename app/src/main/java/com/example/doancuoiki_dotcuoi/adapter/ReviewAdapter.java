package com.example.doancuoiki_dotcuoi.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.model.Review;
import com.example.doancuoiki_dotcuoi.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private final Context context;
    private final List<Review> list;
    private final OnDeleteReviewListener onDeleteReviewListener;
    private final Map<String, User> userCache = new HashMap<>();

    public interface OnDeleteReviewListener {
        void onDelete(Review review);
    }

    public ReviewAdapter(Context context, List<Review> list, OnDeleteReviewListener listener) {
        this.context = context;
        this.list = list;
        this.onDeleteReviewListener = listener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder h, int pos) {
        Review r = list.get(pos);
        Log.d("ReviewAdapter", "onBindViewHolder: " + r.getContent());
        h.tvReviewContent.setText(r.getContent());
        h.ratingReview.setRating(r.getRating());
        if (r.getCreatedAt() != null) {
            h.tvReviewTime.setText(DateFormat.format("HH:mm dd/MM/yyyy", r.getCreatedAt().toDate()));
        } else h.tvReviewTime.setText("");

        // Load tên + avatar người đánh giá
        String uid = r.getUserId();
        if (userCache.containsKey(uid)) {
            setUserView(h, userCache.get(uid), r);
        } else {
            h.tvUserName.setText("Đang tải...");
            h.imgUserAvatar.setImageResource(R.drawable.ic_profile);
            FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener(doc -> {
                User u = doc.toObject(User.class);
                userCache.put(uid, u);
                setUserView(h, u, r);
            });
        }

        // Chỉ hiện nút xoá cho chính user
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (uid != null && currentUid.equals(uid)) {
            h.btnDeleteReview.setVisibility(View.VISIBLE);
            h.btnDeleteReview.setOnClickListener(v -> {
                if (onDeleteReviewListener != null) onDeleteReviewListener.onDelete(r);
            });
        } else {
            h.btnDeleteReview.setVisibility(View.GONE);
        }
    }

    private void setUserView(ReviewViewHolder h, User u, Review r) {
        if (u != null) {
            h.tvUserName.setText(u.getFullName());
            String img = u.getProfileImage();
            if (img != null && !img.isEmpty()) {
                if (img.startsWith("content://")) {
                    Glide.with(context).load(Uri.parse(img)).circleCrop().placeholder(R.drawable.ic_profile).into(h.imgUserAvatar);
                } else {
                    Glide.with(context).load(img).circleCrop().placeholder(R.drawable.ic_profile).into(h.imgUserAvatar);
                }
            } else {
                h.imgUserAvatar.setImageResource(R.drawable.ic_profile);
            }
        } else {
            h.tvUserName.setText("Người dùng");
            h.imgUserAvatar.setImageResource(R.drawable.ic_profile);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgUserAvatar;
        TextView tvUserName, tvReviewContent, tvReviewTime, btnDeleteReview;
        RatingBar ratingReview;

        public ReviewViewHolder(@NonNull View v) {
            super(v);
            imgUserAvatar = v.findViewById(R.id.imgUserAvatar);
            tvUserName = v.findViewById(R.id.tvUserName);
            tvReviewContent = v.findViewById(R.id.tvReviewContent);
            tvReviewTime = v.findViewById(R.id.tvReviewTime);
            ratingReview = v.findViewById(R.id.ratingReview);
            btnDeleteReview = v.findViewById(R.id.btnDeleteReview);
        }
    }
}
