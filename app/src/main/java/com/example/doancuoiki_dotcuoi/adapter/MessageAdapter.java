package com.example.doancuoiki_dotcuoi.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.doancuoiki_dotcuoi.R;
import com.example.doancuoiki_dotcuoi.model.Message;
import java.text.SimpleDateFormat;
import java.util.*;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> messageList;
    private String currentUserId;
    private String avatarLeftUrl;  // Avatar đối phương
    private String avatarRightUrl; // Avatar mình

    public MessageAdapter(List<Message> messageList, String currentUserId, String avatarLeftUrl, String avatarRightUrl) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.avatarLeftUrl = avatarLeftUrl;
        this.avatarRightUrl = avatarRightUrl;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getSenderId().equals(currentUserId) ? 1 : 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if (viewType == 1) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_right, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left, parent, false);
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        Message m = messageList.get(pos);

        if ("image".equals(m.getType()) && m.getImageUrl() != null && !m.getImageUrl().isEmpty()) {
            holder.tvMsg.setVisibility(View.GONE);
            holder.imgMsg.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(m.getImageUrl())
                    .placeholder(R.drawable.ic_image)
                    .into(holder.imgMsg);
        } else {
            holder.tvMsg.setVisibility(View.VISIBLE);
            holder.imgMsg.setVisibility(View.GONE);
            holder.tvMsg.setText(m.getText());
        }
        holder.tvTime.setText(formatTime(m.getTimestamp()));

        // Load avatar
        if (getItemViewType(pos) == 1 && avatarRightUrl != null && !avatarRightUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarRightUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.imgAvatar);
        } else if (getItemViewType(pos) == 0 && avatarLeftUrl != null && !avatarLeftUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarLeftUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_profile);
        }
    }

    @Override
    public int getItemCount() { return messageList.size(); }

    public void setAvatars(String left, String right) {
        this.avatarLeftUrl = left;
        this.avatarRightUrl = right;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvMsg, tvTime;
        public ImageView imgMsg, imgAvatar;
        public ViewHolder(View v) {
            super(v);
            tvMsg = v.findViewById(R.id.tvMsg);
            tvTime = v.findViewById(R.id.tvTime);
            imgMsg = v.findViewById(R.id.imgMsg);
            imgAvatar = v.findViewById(R.id.imgAvatar);
        }
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
