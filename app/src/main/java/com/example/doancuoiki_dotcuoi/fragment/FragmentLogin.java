package com.example.doancuoiki_dotcuoi.fragment;

import android.content.Intent;
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
import com.example.doancuoiki_dotcuoi.activity.Home;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class FragmentLogin extends Fragment {
    private TextInputEditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvSignup, tvForgot;
    private View btnGoogleLogin;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 123;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        edtEmail = view.findViewById(R.id.edtEmail);
        tvSignup = view.findViewById(R.id.tvSignup);
        edtPassword = view.findViewById(R.id.edtPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvForgot = view.findViewById(R.id.tvForgot);
        btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin);

        tvSignup.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_login_to_signin);
        });

        tvForgot.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_login_to_forget);
        });

        mAuth = FirebaseAuth.getInstance();
        btnLogin.setOnClickListener(v -> loginUser());

        // Google Sign In config
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());

        return view;
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString();

        if (!isValidEmail(email)) {
            edtEmail.setError("Email không hợp lệ!");
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            edtPassword.setError("Mật khẩu không được để trống!");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && !user.isEmailVerified()) {
                            Toast.makeText(getContext(), "Vui lòng xác thực email trước khi đăng nhập!", Toast.LENGTH_LONG).show();
                        } else if (user != null) {
                            fetchUserProfile(user.getUid());
                        }
                    } else {
                        Toast.makeText(getContext(), "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(Exception.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Google Sign-in thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(acct.getIdToken(), null))
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkOrCreateUserFirestore(user);
                        }
                    } else {
                        Toast.makeText(getContext(), "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Kiểm tra user có trong Firestore chưa, nếu chưa thì tạo mới với model mới nhất
    private void checkOrCreateUserFirestore(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        String email = firebaseUser.getEmail();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        String fullname = firebaseUser.getDisplayName();
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("userId", uid);
                        userMap.put("fullName", fullname != null ? fullname : "");
                        userMap.put("email", email != null ? email : "");
                        userMap.put("phone", "");
                        userMap.put("profileImage", firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "");
                        userMap.put("status", "Chưa kích hoạt");
                        userMap.put("address", "");
                        userMap.put("addressText", "");
                        userMap.put("lat", 0.0);
                        userMap.put("lng", 0.0);
                        userMap.put("createdAt", System.currentTimeMillis());
                        userMap.put("updatedAt", System.currentTimeMillis());
                        db.collection("users").document(uid).set(userMap)
                                .addOnSuccessListener(aVoid -> fetchUserProfile(uid))
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi tạo user mới: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        fetchUserProfile(uid);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi kiểm tra tài khoản Google: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchUserProfile(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        goToHome();
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy thông tin user!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi lấy dữ liệu user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void goToHome() {
        Intent intent = new Intent(getActivity(), Home.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
