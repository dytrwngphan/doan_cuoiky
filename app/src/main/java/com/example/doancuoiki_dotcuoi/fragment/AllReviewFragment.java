package com.example.doancuoiki_dotcuoi.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.adapter.ReviewAllAdapter;
import com.example.doancuoiki_dotcuoi.model.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class AllReviewFragment extends Fragment {
    private RecyclerView recyclerView;
    private ReviewAllAdapter adapter;
    private ImageButton btnBack;

    private FirebaseFirestore db;
    private String currentUserId;
    private List<Review> allReviews = new ArrayList<>();
    private Map<String, String> userNameCache = new HashMap<>();
    private Map<String, String> productTitleCache = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_reviews, container, false);

        recyclerView = v.findViewById(R.id.rvReviews);
        btnBack = v.findViewById(R.id.btnBack);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new ReviewAllAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());

        loadAllReviews();

        return v;
    }

    private void loadAllReviews() {
        // Lấy tất cả post của user
        db.collection("posts").whereEqualTo("ownerId", currentUserId).get()
                .addOnSuccessListener(postSnapshots -> {
                    List<String> postIds = new ArrayList<>();
                    for (DocumentSnapshot doc : postSnapshots) {
                        postIds.add(doc.getId());
                        // Nếu muốn hiển thị tên bài post thì lấy luôn ở đây
                        String title = doc.getString("title");
                        if (title != null)
                            productTitleCache.put(doc.getId(), title);
                    }
                    if (postIds.isEmpty()) {
                        adapter.setReviewList(new ArrayList<>());
                        return;
                    }
                    // Lấy tất cả review của các post này
                    getAllReviewsFromPostIds(postIds);
                });
    }

    private void getAllReviewsFromPostIds(List<String> postIds) {
        allReviews.clear();
        // Đếm số post đã lấy review xong
        final int[] completed = {0};
        for (String postId : postIds) {
            db.collection("posts").document(postId).collection("reviews")
                    .get()
                    .addOnSuccessListener(reviewSnapshots -> {
                        for (DocumentSnapshot doc : reviewSnapshots) {
                            Review review = doc.toObject(Review.class);
                            if (review != null) {
                                allReviews.add(review);
                                // Nếu muốn load tên user thì cache lại userId, lát lấy 1 lần sau
                            }
                        }
                        completed[0]++;
                        if (completed[0] == postIds.size()) {
                            // Xong hết các post, load tên user nếu muốn (option)
                            loadReviewerNamesThenDisplay();
                        }
                    });
        }
    }

    private void loadReviewerNamesThenDisplay() {
        // Nếu muốn lấy tên user của tất cả review
        Set<String> userIds = new HashSet<>();
        for (Review r : allReviews) {
            userIds.add(r.getUserId());
        }
        if (userIds.isEmpty()) {
            adapter.setReviewList(allReviews);
            return;
        }
        final int[] done = {0};
        for (String uid : userIds) {
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("fullName");
                            if (name != null) userNameCache.put(uid, name);
                        }
                        done[0]++;
                        if (done[0] == userIds.size()) {
                            adapter.setUserNameCache(userNameCache);
                            adapter.setProductTitleCache(productTitleCache);
                            adapter.setReviewList(allReviews);
                        }
                    });
        }
    }
}
