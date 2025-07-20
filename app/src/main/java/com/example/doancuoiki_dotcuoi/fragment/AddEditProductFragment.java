package com.example.doancuoiki_dotcuoi.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.activity.result.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.adapter.ProductImageAdapter;
import com.example.doancuoiki_dotcuoi.model.Product;
import com.example.doancuoiki_dotcuoi.activity.PickLocationActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import org.json.JSONObject;

public class AddEditProductFragment extends Fragment {
    private EditText edtTitle, edtDescription, edtPrice;
    private Spinner spinnerCondition, spinnerCategory, spinnerStatus;
    private RecyclerView rvImages;
    private Button btnAddImage, btnSave, btnCancel;
    private ProductImageAdapter imageAdapter;
    private TextView tvLocation;

    private final List<String> imageUrls = new ArrayList<>();
    private final List<Uri> selectedUris = new ArrayList<>();
    private final String[] categories = {"Laptop", "Bàn phím", "Chuột", "PC", "Màn hình"};
    private final String[] conditions = {"Mới 100%", "Như mới", "Đã qua sử dụng", "Khác"};
    private final String[] statusOptions = {"Đang bán", "Ngừng bán"};

    // Vị trí
    private double selectedLat = 0, selectedLng = 0;
    private String selectedAddress = "";

    private FirebaseFirestore db;

    // Launcher for pick location
    private final ActivityResultLauncher<Intent> pickLocationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedLat = result.getData().getDoubleExtra("lat", 0);
                    selectedLng = result.getData().getDoubleExtra("lng", 0);
                    // Gọi API để chuyển về địa chỉ text
                    getAddressFromLatLng(selectedLat, selectedLng);
                }
            }
    );

    private void initCloudinaryIfNeeded() {
        try { MediaManager.get();
        } catch (Exception e) {
            HashMap<String, String> config = new HashMap<>();
            config.put("cloud_name", "dbcbtgaiy");
            config.put("api_key", "111442618142154");
            config.put("api_secret", "QcCk8g0RVjrNBpQjUek4JrIf2dE");
            MediaManager.init(getContext(), config);
        }
    }

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                            selectedUris.add(imageUri);
                            uploadToCloudinary(imageUri);
                        }
                    } else if (result.getData().getData() != null) {
                        Uri imageUri = result.getData().getData();
                        selectedUris.add(imageUri);
                        uploadToCloudinary(imageUri);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_edit_product, container, false);

        // Ánh xạ view sản phẩm
        rvImages = v.findViewById(R.id.rvImages);
        btnAddImage = v.findViewById(R.id.btnAddImage);
        edtTitle = v.findViewById(R.id.edtTitle);
        edtDescription = v.findViewById(R.id.edtDescription);
        edtPrice = v.findViewById(R.id.edtPrice);
        tvLocation = v.findViewById(R.id.tvLocation);
        spinnerCondition = v.findViewById(R.id.spinnerCondition);
        spinnerCategory = v.findViewById(R.id.spinnerCategory);
        spinnerStatus = v.findViewById(R.id.spinnerStatus);
        btnSave = v.findViewById(R.id.btnSave);
        btnCancel = v.findViewById(R.id.btnCancle);

        db = FirebaseFirestore.getInstance();
        initCloudinaryIfNeeded();

        spinnerCategory.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categories));
        spinnerCondition.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, conditions));
        spinnerStatus.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, statusOptions));
        spinnerStatus.setSelection(0);

        imageAdapter = new ProductImageAdapter(getContext(), imageUrls);
        // Thiết lập listener xóa ảnh
        imageAdapter.setOnImageRemoveListener(position -> {
            imageUrls.remove(position);
            imageAdapter.notifyItemRemoved(position);
            if (imageAdapter.getItemCount() > 0 && imageAdapter.getMainImagePos() >= imageAdapter.getItemCount()) {
                imageAdapter.setMainImagePos(0);
            }

        });
        rvImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvImages.setAdapter(imageAdapter);

        tvLocation.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), PickLocationActivity.class);
            pickLocationLauncher.launch(intent);
        });

        btnAddImage.setOnClickListener(v1 -> pickImages());
        btnSave.setOnClickListener(v1 -> saveProduct());
        btnCancel.setOnClickListener(v1 -> requireActivity().onBackPressed());

        return v;
    }

    private void pickImages() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
        scanDownloadFolder();
    }

    private void uploadToCloudinary(Uri imageUri) {
        MediaManager.get().upload(imageUri)
                .unsigned("android_unsigned")
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        imageUrls.add(url);
                        imageAdapter.notifyDataSetChanged();
                        if (imageUrls.size() == 1) imageAdapter.setMainImagePos(0);
                    }
                    @Override public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        Toast.makeText(getContext(), "Lỗi upload: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
                }).dispatch();
    }

    private void saveProduct() {
        String title = edtTitle.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String condition = spinnerCondition.getSelectedItem().toString();
        String location = selectedAddress; // text thực tế, không còn là Lat/Lng
        String status = spinnerStatus.getSelectedItem().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(priceStr)
                || imageUrls.isEmpty() || selectedLat == 0 || selectedLng == 0) {
            Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin, chọn vị trí trên bản đồ và ít nhất 1 ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }
        double price = Double.parseDouble(priceStr);

        Product p = new Product();
        p.setTitle(title);
        p.setDescription(desc);
        p.setPrice(price);
        p.setLocation(location);
        p.setLatitude(selectedLat);
        p.setLongitude(selectedLng);
        p.setAddressText(location);
        p.setCategory(category);
        p.setCondition(condition);
        p.setImageUrls(new ArrayList<>(imageUrls));
        p.setStatus(status);
        p.setOwnerId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        p.setCreatedAt(System.currentTimeMillis());
        p.setUpdatedAt(System.currentTimeMillis());

        String docId = db.collection("posts").document().getId();
        p.setPostId(docId);

        db.collection("posts").document(docId).set(p)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                    resetForm();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void scanDownloadFolder() {
        File downloadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(downloadDir));
        requireActivity().sendBroadcast(scanIntent);
    }

    private void resetForm() {
        edtTitle.setText("");
        edtDescription.setText("");
        edtPrice.setText("");
        tvLocation.setText("");
        selectedAddress = "";
        selectedLat = 0;
        selectedLng = 0;
        spinnerCategory.setSelection(0);
        spinnerCondition.setSelection(0);
        spinnerStatus.setSelection(0);
        imageUrls.clear();
        selectedUris.clear();
        if (imageAdapter != null) imageAdapter.notifyDataSetChanged();
    }

    // Hàm gọi Nominatim (OpenStreetMap) để lấy addressText từ lat/lng
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
