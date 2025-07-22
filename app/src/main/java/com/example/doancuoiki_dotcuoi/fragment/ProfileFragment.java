package com.example.doancuoiki_dotcuoi.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.activity.EditProfileActivity;
import com.example.doancuoiki_dotcuoi.activity.auth_activity;
import com.example.doancuoiki_dotcuoi.model.Product;
import com.example.doancuoiki_dotcuoi.model.User;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.List;

public class ProfileFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvFullName, tvEmail, tvCreatedAt, tvReviews, tvOffers, tvTransactions;
    private TextView tvListProduct;
    private TextView tvEditProfile, tvLogout, tvDeleteAccount;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivAvatar = view.findViewById(R.id.ivAvatar);
        TextView tvOffers = view.findViewById(R.id.tvOffers);
        tvFullName = view.findViewById(R.id.tvFullName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvCreatedAt = view.findViewById(R.id.tvCreatedAt);
        tvListProduct = view.findViewById(R.id.tvListProduct);
        TextView tvReviews = view.findViewById(R.id.tvReviews);

        tvEditProfile = view.findViewById(R.id.tvEditProfile);
        tvLogout = view.findViewById(R.id.tvLogout);
        tvDeleteAccount = view.findViewById(R.id.tvDeleteAccount);
        tvTransactions = view.findViewById(R.id.tvTransactions);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        TextView tvTransactions = view.findViewById(R.id.tvTransactions);
        tvTransactions.setOnClickListener(v -> {
            Fragment fragment = new TransactionHistoryFragment();
            requireActivity().findViewById(R.id.detailContainer).setVisibility(View.VISIBLE);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detailContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });


        // Chỉnh sửa tài khoản
        tvEditProfile.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(i);
        });
        tvReviews.setOnClickListener(v -> {
            Fragment fragment = new AllReviewFragment();
            requireActivity().findViewById(R.id.detailContainer).setVisibility(View.VISIBLE);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detailContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        tvOffers.setOnClickListener(v -> {
            Fragment fragment = new OfferListFragment(); // Giả sử bạn đã có OfferListFragment
            requireActivity().findViewById(R.id.detailContainer).setVisibility(View.VISIBLE);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detailContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        tvListProduct.setOnClickListener(v -> {
            Fragment fragment = new MyProductListFragment();
            requireActivity().findViewById(R.id.detailContainer).setVisibility(View.VISIBLE);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detailContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Đăng xuất
        tvLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn chắc chắn muốn đăng xuất?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        mAuth.signOut();
                        startActivity(new Intent(getActivity(), auth_activity.class));
                        requireActivity().finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // Xóa tài khoản (và toàn bộ sản phẩm của user)
        tvDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUser();
    }

    private void loadUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        db.collection("users").document(user.getUid())
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User u = documentSnapshot.toObject(User.class);
                        if (u == null) return;
                        tvFullName.setText(u.getFullName());
                        tvEmail.setText(u.getEmail());
                        // Hiển thị ngày tạo
                        if (u.getCreatedAt() > 0) {
                            tvCreatedAt.setText("Ngày tạo: " +
                                    android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", new java.util.Date(u.getCreatedAt())));
                        } else {
                            tvCreatedAt.setText("");
                        }

                        String img = u.getProfileImage();
                        if (img != null && !img.isEmpty()) {
                            Glide.with(this).load(img.startsWith("content://") ? Uri.parse(img) : img).circleCrop().into(ivAvatar);
                        } else {
                            ivAvatar.setImageResource(R.drawable.ic_profile);
                        }
                    }
                });
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa tài khoản")
                .setMessage("Bạn chắc chắn muốn xóa tài khoản? Tất cả dữ liệu và sản phẩm sẽ bị xóa vĩnh viễn.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteAccount())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();

        // Xóa tất cả sản phẩm của user trước
        db.collection("posts").whereEqualTo("ownerId", uid).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    // Xóa user Firestore
                    db.collection("users").document(uid).delete();
                    // Xóa trên Firebase Auth
                    user.delete().addOnCompleteListener(task -> {
                        Toast.makeText(getContext(), "Đã xóa tài khoản và sản phẩm!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), auth_activity.class));
                        requireActivity().finish();
                    });
                });
    }
}
