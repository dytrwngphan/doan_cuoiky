package com.example.doancuoiki_dotcuoi.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.activity.ChatDetailActivity;
import com.example.doancuoiki_dotcuoi.adapter.ChatListAdapter;
import com.example.doancuoiki_dotcuoi.model.Conversation;
import com.example.doancuoiki_dotcuoi.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class ChatListFragment extends Fragment {

    private RecyclerView rvChats;
    private ChatListAdapter chatListAdapter;
    private List<Conversation> conversationList = new ArrayList<>();
    private Map<String, String> avatarMap = new HashMap<>();
    private Map<String, String> nameMap = new HashMap<>();

    private String currentUserId;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_list, container, false);

        rvChats = v.findViewById(R.id.rvChats);
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        chatListAdapter = new ChatListAdapter(conversationList, currentUserId, avatarMap, nameMap,
                (conversation, otherUserId) -> openChatDetail(conversation, otherUserId));
        rvChats.setAdapter(chatListAdapter);

        loadConversations();

        return v;
    }

    private void loadConversations() {
        db.collection("conversations")
                .whereArrayContains("participants", currentUserId)
                .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    conversationList.clear();
                    avatarMap.clear();
                    nameMap.clear();
                    if (snapshots != null) {
                        List<String> userIdsToFetch = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Conversation c = doc.toObject(Conversation.class);
                            c.setId(doc.getId());
                            conversationList.add(c);
                            // Chuẩn bị lấy avatar/name của người còn lại
                            for (String uid : c.getParticipants()) {
                                if (!uid.equals(currentUserId)) userIdsToFetch.add(uid);
                            }
                        }
                        fetchUserInfos(userIdsToFetch);
                        chatListAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void fetchUserInfos(List<String> userIds) {
        for (String uid : userIds) {
            if (avatarMap.containsKey(uid) && nameMap.containsKey(uid)) continue; // Đã lấy rồi
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        avatarMap.put(uid, user.getProfileImage());
                        nameMap.put(uid, user.getFullName());
                        chatListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private void openChatDetail(Conversation conversation, String otherUserId) {
        // Mở ChatDetailFragment, truyền conversationId và otherUserId
        Intent intent = new Intent(getContext(), ChatDetailActivity.class);
        intent.putExtra("conversationId", conversation.getId());
        intent.putExtra("receiverId", otherUserId);
        startActivity(intent);

    }
}
