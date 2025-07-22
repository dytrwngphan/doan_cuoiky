package com.example.doancuoiki_dotcuoi.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.adapter.CategoryAdapter;
import com.example.doancuoiki_dotcuoi.adapter.ProductHomeAdapter;
import com.example.doancuoiki_dotcuoi.model.Category;
import com.example.doancuoiki_dotcuoi.model.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvCategories, rvProducts;
    private CategoryAdapter categoryAdapter;
    private ProductHomeAdapter productHomeAdapter;
    private List<Category> categoryList;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();
    private TextView tvNoProducts;
    private ImageView ivSearch;

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvCategories = view.findViewById(R.id.rvCategories);
        rvProducts = view.findViewById(R.id.rvProducts);
        tvNoProducts = view.findViewById(R.id.tvNoProducts);
        ivSearch = view.findViewById(R.id.ivSearch);
        ivSearch.setOnClickListener(v -> openSearchFragment());


        db = FirebaseFirestore.getInstance();

        // Danh sách category cố định
        categoryList = new ArrayList<>();
        categoryList.add(new Category("Laptop", R.drawable.ic_laptop));
        categoryList.add(new Category("Bàn phím", R.drawable.ic_keyboard));
        categoryList.add(new Category("Chuột", R.drawable.ic_mouse));
        categoryList.add(new Category("PC", R.drawable.ic_pc));
        categoryList.add(new Category("Màn hình", R.drawable.ic_monitor));

        // Adapter cho category
        categoryAdapter = new CategoryAdapter(getContext(), categoryList, category -> filterProducts(category.getName()));
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Adapter cho product
        productHomeAdapter = new ProductHomeAdapter(getContext(), filteredProducts, product -> {
            showProductDetail(product);
        });

        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(productHomeAdapter);

        loadAllProducts();

        return view;
    }
    private void showProductDetail(Product product) {
        ProductDetailFragment fragment = ProductDetailFragment.newInstance(product);
        requireActivity().findViewById(R.id.detailContainer).setVisibility(View.VISIBLE);

        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .add(R.id.detailContainer, fragment, "ProductDetail")
                .addToBackStack("ProductDetail")
                .commit();
    }


    private void loadAllProducts() {
        db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allProducts.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) allProducts.add(p);
                    }
                    // Mặc định filter theo category đầu tiên
                    filterProducts(categoryList.get(0).getName());
                });
    }


    private void filterProducts(String categoryName) {
        filteredProducts.clear();
        for (Product p : allProducts) {
            // Lọc theo category và chỉ lấy những sản phẩm có status = "Đang bán"
            if (
                    p.getCategory() != null
                            && p.getCategory().equalsIgnoreCase(categoryName)
                            && p.getStatus() != null
                            && p.getStatus().equalsIgnoreCase("Đang bán")
            ) {
                filteredProducts.add(p);
            }
        }
        productHomeAdapter.setList(filteredProducts);

        // Hiển thị hoặc ẩn TextView nếu không có sản phẩm
        if (filteredProducts.isEmpty()) {
            tvNoProducts.setVisibility(View.VISIBLE);
        } else {
            tvNoProducts.setVisibility(View.GONE);
        }
    }

    private void openSearchFragment() {
        requireActivity().findViewById(R.id.detailContainer).setVisibility(View.VISIBLE);
        Fragment searchFragment = new SearchProductFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.detailContainer, searchFragment)
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void onResume() {
        super.onResume();
        // Xóa cache avatar mỗi lần vào lại trang home để luôn load mới user (avatar)
        if (productHomeAdapter != null) {
            productHomeAdapter.clearUserCache();
        }
        loadAllProducts(); // gọi lại hàm load sản phẩm như bạn đang có
    }

}
