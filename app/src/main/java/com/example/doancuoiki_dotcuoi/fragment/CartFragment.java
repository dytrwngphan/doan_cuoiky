package com.example.doancuoiki_dotcuoi.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.adapter.CartAdapter;
import com.example.doancuoiki_dotcuoi.model.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class CartFragment extends Fragment {

    private RecyclerView rvCart;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList = new ArrayList<>();
    private String currentUserId;
    private TextView tvNoCartItem, tvTotal;
    private Button btnCheckout;
    private CheckBox checkboxSelectAll;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cart, container, false);
        rvCart = v.findViewById(R.id.rvCart);
        tvNoCartItem = v.findViewById(R.id.tvNoCartItem);
        tvTotal = v.findViewById(R.id.tvTotal);
        btnCheckout = v.findViewById(R.id.btnCheckout);
        checkboxSelectAll = v.findViewById(R.id.checkboxSelectAll);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        cartAdapter = new CartAdapter(getContext(), cartItemList, new CartAdapter.CartListener() {
            @Override
            public void onItemCheckedChanged(CartItem item, boolean isChecked) {
                updateTotal();
                if (!isChecked) checkboxSelectAll.setChecked(false);
            }

            @Override
            public void onRemoveClicked(CartItem item) {
                removeCartItem(item);
            }
        });
        rvCart.setAdapter(cartAdapter);

        checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cartAdapter.selectAll(isChecked);
            updateTotal();
        });

        btnCheckout.setOnClickListener(view -> proceedCheckout());

        loadCartItems();

        return v;
    }

    private void loadCartItems() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cart")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("status", "pending") // chỉ lấy sản phẩm chưa thanh toán
                .addSnapshotListener((snapshots, error) -> {
                    cartItemList.clear();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots) {
                            CartItem item = doc.toObject(CartItem.class);
                            if (item != null) cartItemList.add(item);
                        }
                    }
                    cartAdapter.notifyDataSetChanged();
                    tvNoCartItem.setVisibility(cartItemList.isEmpty() ? View.VISIBLE : View.GONE);
                    updateTotal();
                });
    }

    private void removeCartItem(CartItem item) {
        FirebaseFirestore.getInstance()
                .collection("cart")
                .document(item.getCartItemId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Đã xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartAdapter.getSelectedItems()) {
            total += item.getPrice();
        }
        tvTotal.setText(String.format(Locale.getDefault(), "Tổng: %,.0fđ", total));
    }

    private void proceedCheckout() {
        List<CartItem> selected = cartAdapter.getSelectedItems();
        if (selected.isEmpty()) {
            Toast.makeText(getContext(), "Hãy chọn sản phẩm để thanh toán!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiện dialog xác nhận thanh toán
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận thanh toán")
                .setMessage("Bạn muốn thanh toán " + selected.size() + " sản phẩm?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    int[] doneCount = {0};
                    for (CartItem item : selected) {
                        // 1. Cập nhật status của CartItem
                        db.collection("cart")
                                .document(item.getCartItemId())
                                .update("status", "paid")
                                .addOnSuccessListener(aVoid -> {
                                    // 2. Khi CartItem update xong thì update status Product sang "Ngừng bán"
                                    db.collection("posts")
                                            .document(item.getProductId())
                                            .update("status", "Ngừng bán")
                                            .addOnSuccessListener(aVoid2 -> {
                                                // Hoàn tất 1 sản phẩm
                                                doneCount[0]++;
                                                if (doneCount[0] == selected.size()) {
                                                    Toast.makeText(getContext(), "Đã thanh toán thành công " + selected.size() + " sản phẩm!", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getContext(), "Lỗi khi cập nhật trạng thái sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Lỗi khi thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }


}
