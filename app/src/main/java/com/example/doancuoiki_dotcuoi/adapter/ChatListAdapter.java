package com.example.doancuoiki_dotcuoi.adapter;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.model.Conversation;

import java.text.SimpleDateFormat;
import java.util.*;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Conversation conversation, String otherUserId);
    }

    private List<Conversation> conversationList;
    private String currentUserId;
    private Map<String, String> avatarMap; // <userId, avatarUrl>
    private Map<String, String> nameMap;   // <userId, name>
    private OnItemClickListener listener;

    public ChatListAdapter(List<Conversation> conversationList, String currentUserId,
                           Map<String, String> avatarMap, Map<String, String> nameMap,
                           OnItemClickListener listener) {
        this.conversationList = conversationList;
        this.currentUserId = currentUserId;
        this.avatarMap = avatarMap;
        this.nameMap = nameMap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        Conversation c = conversationList.get(pos);
        String otherId = "";
        for (String uid : c.getParticipants()) {
            if (!uid.equals(currentUserId)) {
                otherId = uid;
                break; // tìm thấy là break luôn
            }
        }
        final String finalOtherId = otherId;

        holder.tvUserName.setText(nameMap.containsKey(otherId) ? nameMap.get(otherId) : "User");
        holder.tvLastMessage.setText(c.getLastMessage() != null ? c.getLastMessage() : "");
        holder.tvTime.setText(formatTime(c.getLastTimestamp()));
        if (avatarMap.containsKey(otherId) && avatarMap.get(otherId) != null && !avatarMap.get(otherId).isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarMap.get(otherId))
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_profile);
        }

        // Đánh dấu chưa đọc (nếu muốn dùng unreadCount)
        holder.imgUnread.setVisibility((c.getUnreadCount() > 0) ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(c, finalOtherId);
        });
    }

    @Override
    public int getItemCount() { return conversationList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, imgUnread;
        TextView tvUserName, tvLastMessage, tvTime;

        public ViewHolder(View v) {
            super(v);
            imgAvatar = v.findViewById(R.id.imgAvatar);
            imgUnread = v.findViewById(R.id.imgUnread);
            tvUserName = v.findViewById(R.id.tvUserName);
            tvLastMessage = v.findViewById(R.id.tvLastMessage);
            tvTime = v.findViewById(R.id.tvTime);
        }
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
