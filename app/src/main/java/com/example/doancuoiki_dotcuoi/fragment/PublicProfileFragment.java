package com.example.doancuoiki_dotcuoi.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.activity.ChatDetailActivity;
import com.example.doancuoiki_dotcuoi.adapter.ProductHomeAdapter;
import com.example.doancuoiki_dotcuoi.model.Product;
import com.example.doancuoiki_dotcuoi.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class PublicProfileFragment extends Fragment {

    private ImageView imgAvatar;
    private TextView tvFullName, tvEmail, tvAddress;
    private Button btnChat;
    private RecyclerView rvProducts;
    private TextView tvNoProduct;

    private ProductHomeAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private FirebaseFirestore db;

    private User userData;
    private String userId; // id người xem

    // Truyền vào userId của người cần xem
    public static PublicProfileFragment newInstance(String userId) {
        PublicProfileFragment fragment = new PublicProfileFragment();
        Bundle args = new Bundle();
        args.putString("user_id", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_public_profile, container, false);

        imgAvatar = v.findViewById(R.id.imgAvatar);
        tvFullName = v.findViewById(R.id.tvFullName);
        tvEmail = v.findViewById(R.id.tvEmail);
        tvAddress = v.findViewById(R.id.tvAddress);
        btnChat = v.findViewById(R.id.btnChat);
        rvProducts = v.findViewById(R.id.rvProducts);
        tvNoProduct = v.findViewById(R.id.tvNoProduct);

        db = FirebaseFirestore.getInstance();

        // Adapter sản phẩm
        productAdapter = new ProductHomeAdapter(getContext(), productList, this::openProductDetail);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(productAdapter);

        btnChat.setOnClickListener(view -> openChatWithUser(userData != null ? userData.getUserId() : null));

        // Lấy userId cần xem profile
        if (getArguments() != null && getArguments().containsKey("user_id")) {
            userId = getArguments().getString("user_id");
            loadUserInfo(userId);
            loadUserProducts(userId);
        }

        return v;
    }

    private void loadUserInfo(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        userData = doc.toObject(User.class);
                        if (userData != null) {
                            tvFullName.setText(userData.getFullName());
                            tvEmail.setText(userData.getEmail());
                            tvAddress.setText(userData.getAddressText());
                            String img = userData.getProfileImage();
                            if (img != null && !img.isEmpty()) {
                                Glide.with(this).load(img).circleCrop().placeholder(R.drawable.ic_profile).into(imgAvatar);
                            } else {
                                imgAvatar.setImageResource(R.drawable.ic_profile);
                            }
                        }
                    }
                });
    }
    private void loadUserProducts(String userId) {
        db.collection("posts")
                .whereEqualTo("ownerId", userId)
                .whereEqualTo("status", "Đang bán")
                .get()
                .addOnCompleteListener(task -> {
                    productList.clear();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Product p = doc.toObject(Product.class);
                            productList.add(p);
                        }
                    }
                    productAdapter.setList(productList);
                    tvNoProduct.setVisibility(productList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }
    // Trong PublicProfileFragment
    private void openChatWithUser(String otherUserId) {
        if (otherUserId == null || otherUserId.isEmpty()) return;
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (otherUserId.equals(currentUserId)) {
            Toast.makeText(getContext(), "Bạn không thể nhắn tin cho chính mình!", Toast.LENGTH_SHORT).show();
            return;
        }
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


    // Mở chat với user
    // Xem chi tiết sản phẩm
    private void openProductDetail(Product p) {
        Fragment detailFragment = ProductDetailFragment.newInstance(p);
        requireActivity().findViewById(R.id.detailContainer).setVisibility(View.VISIBLE);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.detailContainer, detailFragment)
                .addToBackStack(null)
                .commit();
    }
}
