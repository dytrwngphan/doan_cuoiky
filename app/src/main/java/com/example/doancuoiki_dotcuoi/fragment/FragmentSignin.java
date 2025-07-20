package com.example.doancuoiki_dotcuoi.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.doancuoiki_dotcuoi.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FragmentSignin extends Fragment {
    private TextInputEditText edtFullname, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnSignUp;
    private FirebaseAuth mAuth;
    private TextView tvGotoLogin;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signin, container, false);
        tvGotoLogin = view.findViewById(R.id.tvGotoLogin);
        edtFullname = view.findViewById(R.id.edtFullname);
        edtEmail = view.findViewById(R.id.edtEmailSignUp);
        edtPassword = view.findViewById(R.id.edtPasswordSignUp);
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPasswordSignUp);
        btnSignUp = view.findViewById(R.id.btnSignUp);

        mAuth = FirebaseAuth.getInstance();

        btnSignUp.setOnClickListener(v -> registerUser());
        tvGotoLogin.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_signin_to_login);
        });

        return view;
    }

    private void registerUser() {
        String fullName = edtFullname.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();

        // Validate inputs
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ!");
            return;
        }
        if (!isPasswordValid(password)) {
            edtPassword.setError("Mật khẩu phải tối thiểu 8 ký tự, gồm chữ hoa, thường, số và ký tự đặc biệt");
            return;
        }
        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu nhập lại không khớp");
            return;
        }

        // Register with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user, fullName, email);
                        }
                    } else {
                        Toast.makeText(getContext(), "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Kiểm tra mật khẩu đủ mạnh (8 ký tự, chữ hoa, thường, số, ký tự đặc biệt, không dấu)
    private boolean isPasswordValid(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$";
        // Không chứa ký tự tiếng Việt
        boolean hasVietnamese = password.matches(".*[À-ỹà-ỹ].*");
        return password.matches(regex) && !hasVietnamese;
    }

    // Lưu User lên Firestore với model mới
    private void saveUserToFirestore(FirebaseUser user, String fullName, String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", userId);
        userMap.put("fullName", fullName);
        userMap.put("email", email);
        userMap.put("phone", "");
        userMap.put("address", ""); // tỉnh/thành phố, cập nhật sau
        userMap.put("addressText", ""); // Địa chỉ cụ thể (nếu có)
        userMap.put("profileImage", "");
        userMap.put("status", "Chưa kích hoạt");
        userMap.put("createdAt", System.currentTimeMillis());
        userMap.put("updatedAt", System.currentTimeMillis());
        userMap.put("lat", 0.0);
        userMap.put("lng", 0.0);

        db.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> sendVerificationEmail(user))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Đăng ký thành công! Vui lòng kiểm tra email để xác nhận tài khoản.", Toast.LENGTH_LONG).show();
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                        navController.navigate(R.id.action_signin_to_login);
                    } else {
                        Toast.makeText(getContext(), "Lỗi gửi email xác nhận: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
