package com.example.doancuoiki_dotcuoi.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.adapter.ProductImageAdapter;
import com.example.doancuoiki_dotcuoi.adapter.DiscountAdapter;
import com.example.doancuoiki_dotcuoi.model.Product;
import com.example.doancuoiki_dotcuoi.model.Discount;
import com.example.doancuoiki_dotcuoi.activity.PickLocationActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import java.util.*;

import org.json.JSONObject;
import java.io.*;
import java.net.*;

public class EditProductFragment extends Fragment {
    private static final int REQUEST_PICK_IMAGE = 1;
    private static final int REQUEST_PICK_LOCATION = 2;

    private EditText edtTitle, edtDescription, edtPrice, edtDiscountCode, edtDiscountAmount;
    private Spinner spinnerCondition, spinnerCategory, spinnerStatus;
    private RecyclerView rvImages, rvDiscounts;
    private Button btnAddImage, btnAddDiscount, btnSave, btnCancel;
    private TextView tvLocation;

    private Product currentProduct;
    private List<String> imageUrls = new ArrayList<>();
    private List<Discount> discountList = new ArrayList<>();
    private int coverImageIndex = 0;
    private ProductImageAdapter imageAdapter;
    private DiscountAdapter discountAdapter;

    private double selectedLat = 0, selectedLng = 0;
    private String selectedAddress = "";

    private final String[] categories = {"Laptop", "Bàn phím", "Chuột", "PC", "Màn hình"};
    private final String[] conditions = {"Mới 100%", "Như mới", "Đã qua sử dụng", "Khác"};
    private final String[] statuses = {"Đang bán", "Ngừng bán"};

    private FirebaseFirestore db;

    public static EditProductFragment newInstance(String productJson) {
        EditProductFragment fragment = new EditProductFragment();
        Bundle args = new Bundle();
        args.putString("product_json", productJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_product, container, false);

        edtTitle = v.findViewById(R.id.edtTitle);
        edtDescription = v.findViewById(R.id.edtDescription);
        edtPrice = v.findViewById(R.id.edtPrice);
        spinnerCondition = v.findViewById(R.id.spinnerCondition);
        spinnerCategory = v.findViewById(R.id.spinnerCategory);
        spinnerStatus = v.findViewById(R.id.spinnerStatus);
        rvImages = v.findViewById(R.id.rvImages);
        rvDiscounts = v.findViewById(R.id.rvDiscounts);
        btnAddImage = v.findViewById(R.id.btnAddImage);
        btnAddDiscount = v.findViewById(R.id.btnAddDiscount);
        btnSave = v.findViewById(R.id.btnSave);
        btnCancel = v.findViewById(R.id.btnCancel);
        edtDiscountCode = v.findViewById(R.id.edtDiscountCode);
        edtDiscountAmount = v.findViewById(R.id.edtDiscountAmount);
        tvLocation = v.findViewById(R.id.tvLocation);

        db = FirebaseFirestore.getInstance();
        setupSpinners();

        // Load sản phẩm + lấy discount từ Firestore
        if (getArguments() != null && getArguments().containsKey("product_json")) {
            String productJson = getArguments().getString("product_json");
            currentProduct = new Gson().fromJson(productJson, Product.class);
            if (currentProduct != null && currentProduct.getImageUrls() != null)
                imageUrls = new ArrayList<>(currentProduct.getImageUrls());
            selectedLat = currentProduct.getLatitude();
            selectedLng = currentProduct.getLongitude();
            selectedAddress = currentProduct.getAddressText();
            tvLocation.setText(selectedAddress);
            if (currentProduct != null && currentProduct.getPostId() != null) {
                fetchDiscountsForProduct(currentProduct.getPostId());
            }
        }

        // Adapter ảnh sản phẩm (có xóa và chọn ảnh đại diện)
        imageAdapter = new ProductImageAdapter(getContext(), imageUrls);
        imageAdapter.setMainImagePos(coverImageIndex);
        imageAdapter.setOnImageRemoveListener(position -> {
            imageUrls.remove(position);
            imageAdapter.notifyItemRemoved(position);
            if (coverImageIndex >= imageUrls.size()) coverImageIndex = 0;
            imageAdapter.setMainImagePos(coverImageIndex);
        });
        rvImages.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        rvImages.setAdapter(imageAdapter);

        btnAddImage.setOnClickListener(v1 -> pickImages());

        // Adapter Discount
        discountAdapter = new DiscountAdapter(discountList, (discount, position) -> {
            // Xóa khỏi Firestore nếu có discountId
            if (discount.getDiscountId() != null && !discount.getDiscountId().isEmpty()) {
                db.collection("discounts").document(discount.getDiscountId()).delete();
            }
            discountList.remove(position);
            discountAdapter.notifyItemRemoved(position);
        });
        rvDiscounts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDiscounts.setAdapter(discountAdapter);

        btnAddDiscount.setOnClickListener(v1 -> {
            String code = edtDiscountCode.getText().toString().trim();
            String amountStr = edtDiscountAmount.getText().toString().trim();
            if (!code.isEmpty() && !amountStr.isEmpty()) {
                Discount d = new Discount();
                d.setCode(code);
                d.setAmount(Double.parseDouble(amountStr));
                d.setDiscountId(UUID.randomUUID().toString());
                d.setPostId(currentProduct != null ? currentProduct.getPostId() : "");
                discountList.add(d);
                discountAdapter.notifyDataSetChanged();
                edtDiscountCode.setText("");
                edtDiscountAmount.setText("");
            } else {
                Toast.makeText(getContext(), "Nhập đủ mã giảm và số tiền giảm!", Toast.LENGTH_SHORT).show();
            }
        });

        tvLocation.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), PickLocationActivity.class);
            startActivityForResult(intent, REQUEST_PICK_LOCATION);
        });

        btnSave.setOnClickListener(v1 -> saveProduct());
        btnCancel.setOnClickListener(v1 -> requireActivity().getSupportFragmentManager().popBackStack());

        if (currentProduct != null) setProductData(currentProduct);

        return v;
    }

    private void fetchDiscountsForProduct(String postId) {
        db.collection("discounts")
                .whereEqualTo("postId", postId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    discountList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        Discount d = doc.toObject(Discount.class);
                        discountList.add(d);
                    }
                    if (discountAdapter != null) discountAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Không thể tải mã giảm giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupSpinners() {
        spinnerCategory.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categories));
        spinnerCondition.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, conditions));
        spinnerStatus.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, statuses));
    }

    private void setProductData(Product product) {
        edtTitle.setText(product.getTitle());
        edtDescription.setText(product.getDescription());
        edtPrice.setText(String.valueOf(product.getPrice()));
        tvLocation.setText(product.getAddressText());
        setSpinnerSelection(spinnerCategory, categories, product.getCategory());
        setSpinnerSelection(spinnerCondition, conditions, product.getCondition());
        setSpinnerSelection(spinnerStatus, statuses, product.getStatus());
    }

    private void setSpinnerSelection(Spinner spinner, String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void pickImages() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), REQUEST_PICK_IMAGE);
    }

    private void saveProduct() {
        String title = edtTitle.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String condition = spinnerCondition.getSelectedItem().toString();
        String status = spinnerStatus.getSelectedItem().toString();
        String addressText = tvLocation.getText().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(priceStr)
                || imageUrls.isEmpty() || selectedLat == 0 || selectedLng == 0) {
            Toast.makeText(getContext(), "Nhập đủ thông tin, chọn vị trí và ít nhất 1 ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }
        double price = Double.parseDouble(priceStr);

        currentProduct.setTitle(title);
        currentProduct.setDescription(desc);
        currentProduct.setPrice(price);
        currentProduct.setLocation(addressText);
        currentProduct.setLatitude(selectedLat);
        currentProduct.setLongitude(selectedLng);
        currentProduct.setCategory(category);
        currentProduct.setCondition(condition);
        currentProduct.setImageUrls(new ArrayList<>(imageUrls));
        currentProduct.setStatus(status);
        currentProduct.setAddressText(addressText);

        db.collection("posts")
                .document(currentProduct.getPostId())
                .set(currentProduct)
                .addOnSuccessListener(unused -> {
                    for (Discount d : discountList) {
                        d.setPostId(currentProduct.getPostId());
                        db.collection("discounts").document(d.getDiscountId()).set(d);
                    }
                    Toast.makeText(getContext(), "Lưu chỉnh sửa thành công!", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lưu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_LOCATION && resultCode == Activity.RESULT_OK && data != null) {
            selectedLat = data.getDoubleExtra("lat", 0);
            selectedLng = data.getDoubleExtra("lng", 0);
            getAddressFromLatLng(selectedLat, selectedLng); // Chuyển lat/lng về addressText
        } else if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    uploadToCloudinary(imageUri);
                }
            } else if (data.getData() != null) {
                uploadToCloudinary(data.getData());
            }
        }
    }

    private void uploadToCloudinary(Uri imageUri) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dbcbtgaiy");
        config.put("api_key", "111442618142154");
        config.put("api_secret", "QcCk8g0RVjrNBpQjUek4JrIf2dE");
        try {
            MediaManager.get();
        } catch (Exception e) {
            MediaManager.init(getContext(), config);
        }
        MediaManager.get().upload(imageUri)
                .unsigned("android_unsigned")
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        imageUrls.add(url);
                        imageAdapter.notifyDataSetChanged();
                    }
                    @Override public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        Toast.makeText(getContext(), "Lỗi upload: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
                }).dispatch();
    }

    private void getAddressFromLatLng(double lat, double lng) {
        new Thread(() -> {
            try {
                String urlStr = "https://nominatim.openstreetmap.org/reverse?lat=" + lat + "&lon=" + lng + "&format=json";
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible)");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();
                JSONObject obj = new JSONObject(response.toString());
                String displayName = obj.optString("display_name");
                requireActivity().runOnUiThread(() -> {
                    selectedAddress = displayName;
                    tvLocation.setText(displayName);
                });
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    selectedAddress = "Lat: " + lat + ", Lng: " + lng;
                    tvLocation.setText(selectedAddress);
                });
            }
        }).start();
    }
}
