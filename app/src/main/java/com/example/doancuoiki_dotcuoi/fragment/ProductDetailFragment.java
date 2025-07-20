package com.example.doancuoiki_dotcuoi.fragment;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.activity.ChatDetailActivity;
import com.example.doancuoiki_dotcuoi.adapter.ProductImagePagerAdapter;
import com.example.doancuoiki_dotcuoi.adapter.ReviewAdapter;
import com.example.doancuoiki_dotcuoi.model.Product;
import com.example.doancuoiki_dotcuoi.model.Review;
import com.example.doancuoiki_dotcuoi.model.User;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.text.SimpleDateFormat;
import java.util.*;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.IOException;
import java.util.Locale;

public class ProductDetailFragment extends Fragment {

    private ImageView imgSellerAvatar;
    private TextView tvSellerName, tvProductTime, tvProductTitle, tvProductPrice, tvProductStatus, tvProductCondition, tvProductCategory, tvProductLocation, tvProductDesc, tvImageIndicator, tvRatingAvg;
    private ViewPager2 viewPagerImages;
    private ProductImagePagerAdapter imagePagerAdapter;
    private Button btnBuyProduct, btnAddToCart, btnChat, btnBargain;
    private RatingBar ratingBarAvg, ratingInput;
    private EditText edtReviewContent;
    private Button btnSubmitReview;
    private RecyclerView rvReviews;

    private FirebaseFirestore db;
    private Product currentProduct;
    private User sellerUser;

    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;

    public static ProductDetailFragment newInstance(Product product) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("product", product);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_product_detail, container, false);

        // Khởi tạo các view
        imgSellerAvatar = v.findViewById(R.id.imgSellerAvatar);
        tvSellerName = v.findViewById(R.id.tvSellerName);
        tvProductTime = v.findViewById(R.id.tvProductTime);
        tvProductTitle = v.findViewById(R.id.tvProductTitle);
        tvProductPrice = v.findViewById(R.id.tvProductPrice);
        tvProductStatus = v.findViewById(R.id.tvProductStatus);
        tvProductCondition = v.findViewById(R.id.tvProductCondition);
        tvProductCategory = v.findViewById(R.id.tvProductCategory);
        tvProductLocation = v.findViewById(R.id.tvProductLocation);
        tvProductDesc = v.findViewById(R.id.tvProductDesc);
        viewPagerImages = v.findViewById(R.id.viewPagerImages);
        tvImageIndicator = v.findViewById(R.id.tvImageIndicator);
        btnBuyProduct = v.findViewById(R.id.btnBuyProduct);
        btnAddToCart = v.findViewById(R.id.btnAddToCart);
        btnChat = v.findViewById(R.id.btnChat);
        btnBargain = v.findViewById(R.id.btnBargain);
        ratingBarAvg = v.findViewById(R.id.ratingBarAvg);
        tvRatingAvg = v.findViewById(R.id.tvRatingAvg);
        ratingInput = v.findViewById(R.id.ratingInput);
        edtReviewContent = v.findViewById(R.id.edtReviewContent);
        btnSubmitReview = v.findViewById(R.id.btnSubmitReview);
        rvReviews = v.findViewById(R.id.rvReviews);

        db = FirebaseFirestore.getInstance();

        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(getContext(), reviewList, review -> deleteReview(review.getReviewId()));
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setAdapter(reviewAdapter);

        if (getArguments() != null) {
            currentProduct = (Product) getArguments().getSerializable("product");
            if (currentProduct != null) {
                loadProductInfo(currentProduct);
                loadSellerInfo(currentProduct.getOwnerId());
                loadReviews();
            }
        }
        MaterialToolbar toolbar = v.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view1 -> {
            requireActivity().onBackPressed();
        });

        // Đổi indicator khi vuốt ảnh
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int pos) {
                super.onPageSelected(pos);
                if (currentProduct != null && currentProduct.getImageUrls() != null) {
                    updateImageIndicator(pos + 1, currentProduct.getImageUrls().size());
                }
            }
        });

        imgSellerAvatar.setOnClickListener(v1 -> openSellerProfile());
        tvSellerName.setOnClickListener(v1 -> openSellerProfile());

        btnSubmitReview.setOnClickListener(view -> submitReview());

        // Các nút chức năng
        btnBuyProduct.setOnClickListener(view -> showBuyDialog());
        btnAddToCart.setOnClickListener(view -> showAddToCartDialog());
        btnChat.setOnClickListener(view -> {
            if (sellerUser != null)
                openChatWithUser(sellerUser.getUserId());
        });
        btnBargain.setOnClickListener(view -> showBargainDialog());

        // Bấm vào vị trí để mở Google Maps
        tvProductLocation.setOnClickListener(view1 -> openLocationInMap());

        return v;
    }

    private void loadProductInfo(Product p) {
        tvProductTitle.setText(p.getTitle());
        tvProductPrice.setText(String.format("%,.0fđ", p.getPrice()));
        tvProductStatus.setText(p.getStatus());
        tvProductCondition.setText("Tình trạng: " + (p.getCondition() != null ? p.getCondition() : ""));
        tvProductCategory.setText("Danh mục: " + p.getCategory());
        tvProductDesc.setText(p.getDescription());

        // Thời gian đăng
        if (p.getCreatedAt() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            tvProductTime.setText(sdf.format(new Date(p.getCreatedAt())));
        } else {
            tvProductTime.setText("");
        }

        // Setup ViewPager2
        if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) {
            imagePagerAdapter = new ProductImagePagerAdapter(getContext(), p.getImageUrls());
            viewPagerImages.setAdapter(imagePagerAdapter);
            updateImageIndicator(1, p.getImageUrls().size());
        } else {
            imagePagerAdapter = new ProductImagePagerAdapter(getContext(), Collections.singletonList(""));
            viewPagerImages.setAdapter(imagePagerAdapter);
            updateImageIndicator(1, 1);
        }

        // Địa chỉ: nếu latitude/lng khác 0 thì reverse geocode ra địa chỉ!
        if (p.getLatitude() != 0 && p.getLongitude() != 0) {
            getAddressFromLatLng(p.getLatitude(), p.getLongitude());
        } else if (p.getLocation() != null && !p.getLocation().isEmpty()) {
            tvProductLocation.setText(p.getLocation());
        } else {
            tvProductLocation.setText("Không xác định");
        }
    }


    private void updateImageIndicator(int pos, int total) {
        tvImageIndicator.setText(pos + "/" + total);
    }

    private void loadSellerInfo(String ownerId) {
        db.collection("users").document(ownerId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        sellerUser = doc.toObject(User.class);
                        if (sellerUser != null) {
                            tvSellerName.setText(sellerUser.getFullName());
                            String img = sellerUser.getProfileImage();
                            if (img != null && !img.isEmpty()) {
                                if (img.startsWith("content://")) {
                                    Glide.with(this).load(Uri.parse(img)).circleCrop().placeholder(R.drawable.ic_profile).into(imgSellerAvatar);
                                } else {
                                    Glide.with(this).load(img).circleCrop().placeholder(R.drawable.ic_profile).into(imgSellerAvatar);
                                }
                            } else {
                                imgSellerAvatar.setImageResource(R.drawable.ic_profile);
                            }
                        }
                    }
                });
    }

    private void openSellerProfile() {
        if (sellerUser == null) return;

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (sellerUser.getUserId().equals(currentUid)) {
            Toast.makeText(getContext(), "Đây là sản phẩm của bạn!", Toast.LENGTH_SHORT).show();
        } else {
            // Mở profile người khác (PublicProfileFragment)
            Fragment fragment = PublicProfileFragment.newInstance(sellerUser.getUserId());
            requireActivity().findViewById(R.id.detailContainer).setVisibility(View.VISIBLE);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detailContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }




    private void loadReviews() {
        if (currentProduct == null) return;
        db.collection("posts")
                .document(currentProduct.getPostId())
                .collection("reviews")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.e("Review", "Error loading reviews: " + e.getMessage());
                        return;
                    }
                    if (snap != null) {
                        reviewList.clear();
                        double total = 0;
                        for (DocumentSnapshot doc : snap) {
                            Review r = doc.toObject(Review.class);
                            if (r != null) {
                                reviewList.add(r);
                                total += r.getRating();
                            }
                        }
                        reviewAdapter.notifyDataSetChanged();
                        double avg = reviewList.size() > 0 ? total / reviewList.size() : 0;
                        ratingBarAvg.setRating((float) avg);
                        tvRatingAvg.setText(String.format(Locale.getDefault(), "%.1f/5", avg));
                    }
                });
    }

    private void submitReview() {
        String reviewText = edtReviewContent.getText().toString().trim();
        int rating = (int) ratingInput.getRating();
        if (rating == 0) {
            Toast.makeText(getContext(), "Hãy chọn số sao!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(reviewText)) {
            Toast.makeText(getContext(), "Nhập nhận xét!", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String reviewId = UUID.randomUUID().toString();

        Review review = new Review();
        review.setReviewId(reviewId);
        review.setUserId(uid);
        review.setProductId(currentProduct.getPostId());
        review.setContent(reviewText);
        review.setRating(rating);
        review.setCreatedAt(Timestamp.now());

        db.collection("posts")
                .document(currentProduct.getPostId())
                .collection("reviews")
                .document(reviewId)
                .set(review)
                .addOnSuccessListener(unused -> {
                    edtReviewContent.setText("");
                    ratingInput.setRating(0);
                    Toast.makeText(getContext(), "Đã gửi đánh giá!", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteReview(String reviewId) {
        db.collection("posts")
                .document(currentProduct.getPostId())
                .collection("reviews")
                .document(reviewId)
                .delete()
                .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Đã xoá nhận xét", Toast.LENGTH_SHORT).show());
    }
    // Trong ProductDetailFragment (sửa openChatWithUser)
    private void openChatWithUser(String otherUserId) {
        if (otherUserId == null || otherUserId.isEmpty()) return;
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (otherUserId.equals(currentUserId)) {
            Toast.makeText(getContext(), "Bạn không thể nhắn tin cho chính mình!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Kiểm tra hoặc tạo conversation như cũ
        FirebaseFirestore.getInstance().collection("conversations")
                .whereArrayContains("participants", currentUserId)
                .get().addOnSuccessListener(querySnapshot -> {
                    String conversationId = null;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        List<String> parts = (List<String>) doc.get("participants");
                        if (parts != null && parts.contains(otherUserId)) {
                            conversationId = doc.getId();
                            break;
                        }
                    }
                    if (conversationId != null) {
                        openChatDetail(conversationId, otherUserId);
                    } else {
                        Map<String, Object> data = new HashMap<>();
                        List<String> participants = Arrays.asList(currentUserId, otherUserId);
                        data.put("participants", participants);
                        data.put("lastMessage", "");
                        data.put("lastTimestamp", System.currentTimeMillis());
                        data.put("lastSenderId", "");
                        FirebaseFirestore.getInstance().collection("conversations")
                                .add(data)
                                .addOnSuccessListener(documentReference -> {
                                    openChatDetail(documentReference.getId(), otherUserId);
                                });
                    }
                });
    }

    // Sửa lại openChatDetail để chuyển sang Activity
    private void openChatDetail(String conversationId, String receiverId) {
        Intent intent = new Intent(getContext(), ChatDetailActivity.class);
        intent.putExtra("conversationId", conversationId);
        intent.putExtra("receiverId", receiverId);
        startActivity(intent);
    }



    // ======= Các dialog chức năng ========

    private void showBuyDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Mua sản phẩm")
                .setMessage("Bạn muốn mua sản phẩm này?")
                .setPositiveButton("Mua", (dialog, which) -> {
                    // TODO: Xử lý logic mua hàng ở đây
                    Toast.makeText(getContext(), "Chức năng mua hàng chưa hoàn thiện!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showAddToCartDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Thêm vào giỏ hàng")
                .setMessage("Thêm sản phẩm này vào giỏ?")
                .setPositiveButton("Thêm", (dialog, which) -> {
                    // TODO: Xử lý thêm vào giỏ hàng ở đây
                    Toast.makeText(getContext(), "Đã thêm vào giỏ hàng (mô phỏng)!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showBargainDialog() {
        final EditText input = new EditText(getContext());
        input.setHint("Nhập giá muốn trả (VNĐ)");
        new AlertDialog.Builder(getContext())
                .setTitle("Trả giá")
                .setMessage("Bạn muốn thương lượng giá với người bán?")
                .setView(input)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    String offer = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(offer)) {
                        // TODO: Xử lý logic trả giá (gửi tới người bán...)
                        Toast.makeText(getContext(), "Đã gửi giá đề nghị: " + offer, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // ====== Địa chỉ từ lat/lon =======
    private void getAddressFromLatLng(double lat, double lng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String addrStr = addresses.get(0).getAddressLine(0);
                    requireActivity().runOnUiThread(() -> tvProductLocation.setText(addrStr));
                } else {
                    requireActivity().runOnUiThread(() ->
                            tvProductLocation.setText(String.format(Locale.getDefault(), "%.5f, %.5f", lat, lng)));
                }
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        tvProductLocation.setText(String.format(Locale.getDefault(), "%.5f, %.5f", lat, lng)));
            }
        }).start();
    }


    // ======= Mở vị trí bằng Google Maps ========
    private void openLocationInMap() {
        if (currentProduct != null) {
            double lat = currentProduct.getLatitude();
            double lng = currentProduct.getLongitude();
            if (lat != 0 && lng != 0) {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f", lat, lng, lat, lng);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        }
    }
}
