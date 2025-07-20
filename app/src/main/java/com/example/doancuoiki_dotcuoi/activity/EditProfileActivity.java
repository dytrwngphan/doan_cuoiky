package com.example.doancuoiki_dotcuoi.activity;

import android.app.Activity;
import android.content.Intent;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.*;

public class EditProfileActivity extends AppCompatActivity {
    private ImageView ivAvatar;
    private EditText edtFullName, edtPhone, edtAddress;
    private Button btnSave;
    private Uri imageUri = null;
    private String uploadedAvatarUrl = null;
    private static final int PICK_IMAGE = 100;
    private static final int PICK_LOCATION = 2345;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private double selectedLat = 0;
    private double selectedLng = 0;
    private String selectedAddressText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivAvatar = findViewById(R.id.ivEditAvatar);
        edtFullName = findViewById(R.id.edtEditFullName);
        edtPhone = findViewById(R.id.edtEditPhone);
        edtAddress = findViewById(R.id.edtEditAddress);
        btnSave = findViewById(R.id.btnSaveProfile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initCloudinary();
        loadUser();

        ivAvatar.setOnClickListener(v -> pickImage());

        // Không cho nhập tay, chỉ chọn map
        edtAddress.setFocusable(false);
        edtAddress.setOnClickListener(v -> {
            Intent i = new Intent(EditProfileActivity.this, PickLocationActivity.class);
            startActivityForResult(i, PICK_LOCATION);
        });

        btnSave.setOnClickListener(v -> updateProfile());
    }

    private void initCloudinary() {
        try {
            MediaManager.get();
        } catch (Exception e) {
            HashMap<String, String> config = new HashMap<>();
            config.put("cloud_name", "dbcbtgaiy");
            config.put("api_key", "111442618142154");
            config.put("api_secret", "QcCk8g0RVjrNBpQjUek4JrIf2dE");
            MediaManager.init(this, config);
        }
    }

    private void loadUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User u = documentSnapshot.toObject(User.class);
                    if (u != null) {
                        edtFullName.setText(u.getFullName());
                        edtPhone.setText(u.getPhone());
                        uploadedAvatarUrl = u.getProfileImage();
                        if (uploadedAvatarUrl != null && !uploadedAvatarUrl.isEmpty())
                            Glide.with(this).load(uploadedAvatarUrl).circleCrop().into(ivAvatar);
                        else
                            ivAvatar.setImageResource(R.drawable.ic_profile);

                        // Lấy từ addressText, ưu tiên addressText trước
                        if (u.getAddressText() != null && !u.getAddressText().isEmpty()) {
                            edtAddress.setText(u.getAddressText());
                            selectedAddressText = u.getAddressText();
                        } else if (u.getAddress() != null && !u.getAddress().isEmpty()) {
                            edtAddress.setText(u.getAddress());
                            selectedAddressText = u.getAddress();
                        } else if (documentSnapshot.contains("lat") && documentSnapshot.contains("lng")) {
                            selectedLat = documentSnapshot.getDouble("lat");
                            selectedLng = documentSnapshot.getDouble("lng");
                            selectedAddressText = getAddressFromLatLng(selectedLat, selectedLng);
                            edtAddress.setText(selectedAddressText);
                        }
                        if (documentSnapshot.contains("lat")) selectedLat = documentSnapshot.getDouble("lat");
                        if (documentSnapshot.contains("lng")) selectedLng = documentSnapshot.getDouble("lng");
                    }
                });
    }

    // Chuyển lat/lng sang chuỗi địa chỉ
    private String getAddressFromLatLng(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(this);
            List<android.location.Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "";
    }

    private void pickImage() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).circleCrop().into(ivAvatar);
            uploadAvatarToCloudinary(imageUri);
        }
        if (requestCode == PICK_LOCATION && resultCode == RESULT_OK && data != null) {
            selectedLat = data.getDoubleExtra("lat", 0);
            selectedLng = data.getDoubleExtra("lng", 0);
            selectedAddressText = getAddressFromLatLng(selectedLat, selectedLng);
            edtAddress.setText(selectedAddressText);
        }
    }

    private void uploadAvatarToCloudinary(Uri imageUri) {
        Toast.makeText(this, "Đang upload ảnh...", Toast.LENGTH_SHORT).show();
        MediaManager.get().upload(imageUri)
                .unsigned("android_unsigned")
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onSuccess(String requestId, Map resultData) {
                        uploadedAvatarUrl = (String) resultData.get("secure_url");
                        Toast.makeText(EditProfileActivity.this, "Đã upload avatar!", Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        Toast.makeText(EditProfileActivity.this, "Lỗi upload: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
                }).dispatch();
    }

    private void updateProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        Map<String, Object> update = new HashMap<>();
        update.put("fullName", fullName);
        update.put("phone", phone);
        update.put("address", selectedAddressText);      // Địa chỉ hiển thị cũ
        update.put("addressText", selectedAddressText);   // Địa chỉ chuỗi theo model mới
        update.put("status", "Đã kích hoạt");
        update.put("updatedAt", System.currentTimeMillis());
        update.put("lat", selectedLat);
        update.put("lng", selectedLng);
        if (uploadedAvatarUrl != null) {
            update.put("profileImage", uploadedAvatarUrl);
        }

        db.collection("users").document(user.getUid()).update(update)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
