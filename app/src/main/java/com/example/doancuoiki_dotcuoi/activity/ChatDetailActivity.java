package com.example.doancuoiki_dotcuoi.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.*;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.adapter.MessageAdapter;
import com.example.doancuoiki_dotcuoi.model.Message;
import com.example.doancuoiki_dotcuoi.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import java.util.*;

public class ChatDetailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;
    private RecyclerView rvMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();

    private String conversationId, receiverId, currentUserId;
    private String avatarLeftUrl = "", avatarRightUrl = "", receiverName = "";
    private EmojiEditText etMsg;
    private ImageButton btnSend, btnImage, btnEmoji, btnBack;
    private ImageView imgAvatar;
    private TextView tvUserName;
    private EmojiPopup emojiPopup;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ view
        rvMessages = findViewById(R.id.rvMessages);
        etMsg = findViewById(R.id.etMsg);
        btnSend = findViewById(R.id.btnSend);
        btnImage = findViewById(R.id.btnImage);
        btnEmoji = findViewById(R.id.btnEmoji);
        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUserName = findViewById(R.id.tvUserName);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Lấy intent truyền vào
        conversationId = getIntent().getStringExtra("conversationId");
        receiverId = getIntent().getStringExtra("receiverId");

        // Khởi tạo adapter
        messageAdapter = new MessageAdapter(messageList, currentUserId, avatarLeftUrl, avatarRightUrl);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(messageAdapter);

        // Emoji Popup
        emojiPopup = EmojiPopup.Builder.fromRootView(findViewById(android.R.id.content)).build(etMsg);
        btnEmoji.setOnClickListener(view -> emojiPopup.toggle());

        // Load info người nhận để hiển thị avatar, tên
        db.collection("users").document(receiverId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    avatarLeftUrl = user.getProfileImage();
                    receiverName = user.getFullName();
                    Glide.with(this).load(avatarLeftUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile)
                            .into(imgAvatar);
                    tvUserName.setText(receiverName);
                    messageAdapter.setAvatars(avatarLeftUrl, avatarRightUrl); // cập nhật avatar khi có dữ liệu
                }
            }
        });

        // Load info của mình để hiển thị avatar phải
        db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    avatarRightUrl = user.getProfileImage();
                    messageAdapter.setAvatars(avatarLeftUrl, avatarRightUrl);
                }
            }
        });

        // Lắng nghe message realtime
        db.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((snap, e) -> {
                    if (e != null) return;
                    messageList.clear();
                    for (DocumentSnapshot doc : snap) {
                        Message m = doc.toObject(Message.class);
                        if (m != null) {
                            m.setId(doc.getId());
                            messageList.add(m);
                        }
                    }
                    messageAdapter.notifyDataSetChanged();
                    rvMessages.scrollToPosition(messageList.size() - 1);
                });

        btnSend.setOnClickListener(view -> sendMessage());
        btnImage.setOnClickListener(view -> pickImage());
        btnBack.setOnClickListener(view -> finish());
    }

    private void sendMessage() {
        String text = etMsg.getText().toString().trim();
        if (text.isEmpty()) return;

        String msgId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        Message m = new Message();
        m.setId(msgId);
        m.setSenderId(currentUserId);
        m.setReceiverId(receiverId);
        m.setText(text);
        m.setType("text");
        m.setTimestamp(timestamp);

        db.collection("conversations").document(conversationId)
                .collection("messages").document(msgId).set(m);

        db.collection("conversations").document(conversationId)
                .update("lastMessage", text,
                        "lastTimestamp", timestamp,
                        "lastSenderId", currentUserId);

        etMsg.setText("");
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToCloudinary(imageUri);
        }
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        // Nếu đã init ở Application thì bỏ đoạn config này, chỉ gọi upload
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dbcbtgaiy");
        config.put("api_key", "111442618142154");
        config.put("api_secret", "QcCk8g0RVjrNBpQjUek4JrIf2dE");
        try {
            MediaManager.get();
        } catch (Exception e) {
            MediaManager.init(this, config);
        }
        MediaManager.get().upload(imageUri)
                .unsigned("android_unsigned")
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        sendImageMessage(url);
                    }
                    @Override public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        Toast.makeText(ChatDetailActivity.this, "Lỗi upload: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
                }).dispatch();
    }

    private void sendImageMessage(String imageUrl) {
        String msgId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        Message m = new Message();
        m.setId(msgId);
        m.setSenderId(currentUserId);
        m.setReceiverId(receiverId);
        m.setImageUrl(imageUrl);
        m.setType("image");
        m.setTimestamp(timestamp);

        db.collection("conversations").document(conversationId)
                .collection("messages").document(msgId).set(m);

        db.collection("conversations").document(conversationId)
                .update("lastMessage", "[Ảnh]",
                        "lastTimestamp", timestamp,
                        "lastSenderId", currentUserId);
    }
}
