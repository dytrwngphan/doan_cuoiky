package com.example.doancuoiki_dotcuoi.adapter;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.model.Review;

import java.text.SimpleDateFormat;
import java.util.*;

public class ReviewAllAdapter extends RecyclerView.Adapter<ReviewAllAdapter.ReviewViewHolder> {

    private List<Review> reviewList = new ArrayList<>();
    private Map<String, String> userNameCache = new HashMap<>();
    private Map<String, String> productTitleCache = new HashMap<>();

    public void setReviewList(List<Review> list) {
        this.reviewList = list;
        notifyDataSetChanged();
    }

    // Optional: để set tên user và tên bài post nếu muốn truyền từ ngoài
    public void setUserNameCache(Map<String, String> cache) {
        this.userNameCache = cache;
    }

    public void setProductTitleCache(Map<String, String> cache) {
        this.productTitleCache = cache;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_all, parent, false);
        return new ReviewViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        // Lấy tên người đánh giá, tên bài post nếu có cache
        String userName = userNameCache.getOrDefault(review.getUserId(), review.getUserId());
        String productTitle = productTitleCache.getOrDefault(review.getProductId(), review.getProductId());

        holder.tvReviewerName.setText("Người đánh giá: " + userName);
        holder.tvPostTitle.setText("Tên bài post: " + productTitle);
        holder.ratingBar.setRating(review.getRating());
        holder.tvContent.setText("Đánh giá: " + review.getContent());

        if (review.getCreatedAt() != null) {
            java.util.Date date = review.getCreatedAt().toDate(); // SỬA LỖI ở đây!
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            holder.tvCreatedAt.setText("Ngày đánh giá: " + sdf.format(date));
        } else {
            holder.tvCreatedAt.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvReviewerName, tvPostTitle, tvContent, tvCreatedAt;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            tvPostTitle = itemView.findViewById(R.id.tvPostTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
