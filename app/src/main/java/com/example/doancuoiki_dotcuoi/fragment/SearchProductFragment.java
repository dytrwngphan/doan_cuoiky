package com.example.doancuoiki_dotcuoi.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.adapter.ProductHomeAdapter;
import com.example.doancuoiki_dotcuoi.model.Product;
import com.google.firebase.firestore.*;
import java.util.*;

public class SearchProductFragment extends Fragment {
    private EditText edtName;
    private Spinner spnMinPrice, spnMaxPrice, spnCondition, spnCategory, spnSort, spnLocation;
    private Button btnSearch;
    private RecyclerView rvProducts;
    private TextView tvNoProducts;
    private ImageButton btnBack;
    private ProductHomeAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private FirebaseFirestore db;

    private final String[] conditionOptions = {"loại", "Mới 100%", "Như mới", "Đã qua sử dụng", "Khác"};
    private final String[] categoryOptions = {"danh mục", "Laptop", "Bàn phím", "Chuột", "PC", "Màn hình"};
    private final String[] priceMinOptions = {"giá thấp nhất", "500000", "1000000", "2000000", "5000000", "10000000"};
    private final String[] priceMaxOptions = {"giá cao nhất", "1000000", "2000000", "5000000", "10000000", "20000000"};
    private final String[] sortOptions = {
            "Giá thấp → cao", "Giá cao → thấp", "Mới nhất → Cũ nhất", "Cũ nhất → Mới nhất"
    };
    // Danh sách 63 tỉnh/thành (bỏ tiền tố TP)
    private final String[] locationOptions = {
            "địa điểm",
            "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu", "Bắc Ninh", "Bến Tre", "Bình Định",
            "Bình Dương", "Bình Phước", "Bình Thuận", "Cà Mau", "Cần Thơ", "Cao Bằng", "Đà Nẵng", "Đắk Lắk",
            "Đắk Nông", "Điện Biên", "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Giang", "Hà Nam", "Hà Nội",
            "Hà Tĩnh", "Hải Dương", "Hải Phòng", "Hậu Giang", "Hòa Bình", "Hưng Yên", "Khánh Hòa", "Kiên Giang",
            "Kon Tum", "Lai Châu", "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Long An", "Nam Định", "Nghệ An", "Ninh Bình",
            "Ninh Thuận", "Phú Thọ", "Phú Yên", "Quảng Bình", "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", "Quảng Trị",
            "Sóc Trăng", "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên", "Thanh Hóa", "Thừa Thiên Huế", "Tiền Giang",
            "Hồ Chí Minh", "Trà Vinh", "Tuyên Quang", "Vĩnh Long", "Vĩnh Phúc", "Yên Bái"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_product, container, false);

        // Ánh xạ view
        edtName = v.findViewById(R.id.edtName);
        spnMinPrice = v.findViewById(R.id.spnMinPrice);
        spnMaxPrice = v.findViewById(R.id.spnMaxPrice);
        spnCondition = v.findViewById(R.id.spnCondition);
        spnCategory = v.findViewById(R.id.spnCategory);
        spnLocation = v.findViewById(R.id.spnLocation);
        spnSort = v.findViewById(R.id.spnSort);
        btnSearch = v.findViewById(R.id.btnSearch);
        rvProducts = v.findViewById(R.id.rvProducts);
        tvNoProducts = v.findViewById(R.id.tvNoProducts);
        btnBack = v.findViewById(R.id.btnBack);

        db = FirebaseFirestore.getInstance();

        spnCondition.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, conditionOptions));
        spnCategory.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categoryOptions));
        spnMinPrice.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, priceMinOptions));
        spnMaxPrice.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, priceMaxOptions));
        spnLocation.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, locationOptions));
        spnSort.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, sortOptions));

        adapter = new ProductHomeAdapter(getContext(), productList, this::openProductDetail);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(adapter);

        btnSearch.setOnClickListener(vw -> searchProducts());
        btnBack.setOnClickListener(vw -> requireActivity().getSupportFragmentManager().popBackStack());

        return v;
    }

    /** Hàm chuẩn hóa tên địa điểm, bỏ tiền tố, loại dấu tiếng Việt */
    public static String normalizeLocation(String input) {
        if (input == null) return "";
        String s = input.toLowerCase();
        s = s.replace("tp.", "")
                .replace("tp ", "")
                .replace("thành phố", "")
                .replace("tỉnh", "")
                .replace("thị xã", "")
                .replace("quận", "")
                .replace("huyện", "")
                .replace(":", "")
                .replace("-", "")
                .trim();
        // Bỏ dấu tiếng Việt (phổ biến nhất)
        s = s.replace("à", "a").replace("á", "a").replace("ả", "a").replace("ã", "a").replace("ạ", "a")
                .replace("â", "a").replace("ầ", "a").replace("ấ", "a").replace("ẩ", "a").replace("ẫ", "a").replace("ậ", "a")
                .replace("ă", "a").replace("ằ", "a").replace("ắ", "a").replace("ẳ", "a").replace("ẵ", "a").replace("ặ", "a")
                .replace("è", "e").replace("é", "e").replace("ẻ", "e").replace("ẽ", "e").replace("ẹ", "e")
                .replace("ê", "e").replace("ề", "e").replace("ế", "e").replace("ể", "e").replace("ễ", "e").replace("ệ", "e")
                .replace("ì", "i").replace("í", "i").replace("ỉ", "i").replace("ĩ", "i").replace("ị", "i")
                .replace("ò", "o").replace("ó", "o").replace("ỏ", "o").replace("õ", "o").replace("ọ", "o")
                .replace("ô", "o").replace("ồ", "o").replace("ố", "o").replace("ổ", "o").replace("ỗ", "o").replace("ộ", "o")
                .replace("ơ", "o").replace("ờ", "o").replace("ớ", "o").replace("ở", "o").replace("ỡ", "o").replace("ợ", "o")
                .replace("ù", "u").replace("ú", "u").replace("ủ", "u").replace("ũ", "u").replace("ụ", "u")
                .replace("ư", "u").replace("ừ", "u").replace("ứ", "u").replace("ử", "u").replace("ữ", "u").replace("ự", "u")
                .replace("ỳ", "y").replace("ý", "y").replace("ỷ", "y").replace("ỹ", "y").replace("ỵ", "y")
                .replace("đ", "d");
        s = s.replaceAll("\\s+", " ").trim();
        return s;
    }

    private boolean matchLocation(String selectedLocation, String addressText) {
        if (selectedLocation.equals("địa điểm")) return true;
        if (addressText == null) return false;

        String selected = normalizeLocation(selectedLocation);
        String address = normalizeLocation(addressText);

        return address.contains(selected);
    }

    private void searchProducts() {
        String name = edtName.getText().toString().trim().toLowerCase();
        String minPriceStr = spnMinPrice.getSelectedItem().toString();
        String maxPriceStr = spnMaxPrice.getSelectedItem().toString();
        String condition = spnCondition.getSelectedItem().toString();
        String category = spnCategory.getSelectedItem().toString();
        String location = spnLocation.getSelectedItem().toString();
        String sort = spnSort.getSelectedItem().toString();

        db.collection("posts").get().addOnCompleteListener(task -> {
            productList.clear();
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Product p = doc.toObject(Product.class);
                    // Tên
                    boolean matchName = TextUtils.isEmpty(name)
                            || (p.getTitle() != null && p.getTitle().toLowerCase().contains(name));
                    // Danh mục
                    boolean matchCategory = category.equals("danh mục") ||
                            (p.getCategory() != null && p.getCategory().equals(category));
                    // Condition
                    boolean matchCondition = condition.equals("loại") ||
                            (p.getCondition() != null && p.getCondition().equals(condition));
                    // Location
                    boolean matchLocation = matchLocation(location, p.getAddressText());
                    // Giá
                    double minPrice = 0, maxPrice = Double.MAX_VALUE;
                    try { if (!minPriceStr.equals("giá thấp nhất")) minPrice = Double.parseDouble(minPriceStr); } catch (Exception ignored) {}
                    try { if (!maxPriceStr.equals("giá cao nhất")) maxPrice = Double.parseDouble(maxPriceStr); } catch (Exception ignored) {}
                    boolean matchPrice = true;
                    if (p.getPrice() < minPrice || p.getPrice() > maxPrice) matchPrice = false;

                    // Tất cả đều đúng thì add
                    if (matchName && matchCategory && matchCondition && matchLocation && matchPrice) {
                        productList.add(p);
                    }
                }
                sortProducts(productList, sort);
            }
            adapter.notifyDataSetChanged();
            tvNoProducts.setVisibility(productList.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void sortProducts(List<Product> list, String sort) {
        switch (sort) {
            case "Giá thấp → cao":
                Collections.sort(list, Comparator.comparingDouble(Product::getPrice));
                break;
            case "Giá cao → thấp":
                Collections.sort(list, (a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                break;
            case "Mới nhất → Cũ nhất":
                Collections.sort(list, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                break;
            case "Cũ nhất → Mới nhất":
                Collections.sort(list, (a, b) -> Long.compare(a.getCreatedAt(), b.getCreatedAt()));
                break;


        }
    }

    private void openProductDetail(Product p) {
        Fragment detailFragment = ProductDetailFragment.newInstance(p);
        requireActivity().findViewById(R.id.detailContainer).setVisibility(View.VISIBLE);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.detailContainer, detailFragment)
                .addToBackStack(null)
                .commit();
    }
}
