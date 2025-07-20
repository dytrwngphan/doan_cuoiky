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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class FragmentForget extends Fragment {

    private EditText edtEmailForgot;
    private Button btnSendReset;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forget, container, false);

        edtEmailForgot = view.findViewById(R.id.edtEmailForgot);
        btnSendReset = view.findViewById(R.id.btnSendReset);
        mAuth = FirebaseAuth.getInstance();

        btnSendReset.setOnClickListener(v -> sendResetEmail());

        View tvBackLogin = view.findViewById(R.id.tvGotoLogin);
        if (tvBackLogin != null) {
            tvBackLogin.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.popBackStack();
            });
        }

        return view;
    }

    private void sendResetEmail() {
        String email = edtEmailForgot.getText().toString().trim();
        if (!isValidEmail(email)) {
            edtEmailForgot.setError("Email không hợp lệ!");
            return;
        }
        btnSendReset.setEnabled(false);

        // Kiểm tra tồn tại trên Firestore trước khi gửi reset
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        edtEmailForgot.setError("Email này chưa đăng ký tài khoản!");
                        btnSendReset.setEnabled(true);
                    } else {
                        mAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(task -> {
                                    btnSendReset.setEnabled(true);
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư!", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getContext(), "Lỗi: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi kiểm tra email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSendReset.setEnabled(true);
                });
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
