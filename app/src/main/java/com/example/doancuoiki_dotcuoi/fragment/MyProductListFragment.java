package com.example.doancuoiki_dotcuoi.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.adapter.MyProductAdapter;
import com.example.doancuoiki_dotcuoi.model.Product;
import com.example.doancuoiki_dotcuoi.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;
import com.google.gson.Gson;

public class MyProductListFragment extends Fragment {
    private ImageButton btnBack;
    private TextView tvNoProduct;
    private RecyclerView rvMyProducts;
    private MyProductAdapter adapter;
    private List<Product> myProductList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_product_list, container, false);

        btnBack = v.findViewById(R.id.btnBack);
        tvNoProduct = v.findViewById(R.id.tvNoProduct);
        rvMyProducts = v.findViewById(R.id.rvMyProducts);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Load user info trước khi tạo adapter để truyền avatar, tên user
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    currentUser = documentSnapshot.toObject(User.class);
                    adapter = new MyProductAdapter(getContext(), myProductList, currentUser, new MyProductAdapter.OnProductActionListener() {
                        @Override
                        public void onViewDetail(Product product) {
                            openProductDetail(product);
                        }

                        @Override
                        public void onDelete(Product product, int position) {
                            deleteProduct(product, position);
                        }
                    });
                    rvMyProducts.setLayoutManager(new LinearLayoutManager(getContext()));
                    rvMyProducts.setAdapter(adapter);

                    loadMyProducts();
                });

        btnBack.setOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());

        return v;
    }

    private void loadMyProducts() {
        myProductList.clear();
        db.collection("posts")
                .whereEqualTo("ownerId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product p = doc.toObject(Product.class);
                        // Đảm bảo postId (Firestore id) được gán cho model nếu bị thiếu
                        if (p.getPostId() == null || p.getPostId().isEmpty()) {
                            p.setPostId(doc.getId());
                        }
                        myProductList.add(p);
                    }
                    adapter.notifyDataSetChanged();
                    tvNoProduct.setVisibility(myProductList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    tvNoProduct.setVisibility(View.VISIBLE);
                    tvNoProduct.setText("Không thể tải sản phẩm: " + e.getMessage());
                });
    }

    private void openProductDetail(Product p) {
        String productJson = new Gson().toJson(p); // Chuyển Product thành String JSON
        Fragment detailFragment = EditProductFragment.newInstance(productJson); // truyền JSON vào
        requireActivity().findViewById(R.id.detailContainer).setVisibility(View.VISIBLE);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.detailContainer, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    private void deleteProduct(Product p, int position) {
        db.collection("posts").document(p.getPostId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    myProductList.remove(position);
                    adapter.notifyItemRemoved(position);
                    tvNoProduct.setVisibility(myProductList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    // Thông báo lỗi nếu cần
                });
    }
}
