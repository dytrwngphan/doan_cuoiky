package com.example.doancuoiki_dotcuoi.model;

import java.util.List;

public class Conversation {
    private String id;                     // conversationId
    private List<String> participants;     // [userAId, userBId]
    private String lastMessage;            // nội dung cuối cùng
    private long lastTimestamp;            // thời gian gửi cuối cùng
    private String lastSenderId;           // ai gửi tin cuối cùng
    private int unreadCount;               // (tuỳ chọn) số tin chưa đọc với currentUser

    public Conversation() {}

    public Conversation(String id, List<String> participants, String lastMessage, long lastTimestamp, String lastSenderId, int unreadCount) {
        this.id = id;
        this.participants = participants;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
        this.lastSenderId = lastSenderId;
        this.unreadCount = unreadCount;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastTimestamp() { return lastTimestamp; }
    public void setLastTimestamp(long lastTimestamp) { this.lastTimestamp = lastTimestamp; }

    public String getLastSenderId() { return lastSenderId; }
    public void setLastSenderId(String lastSenderId) { this.lastSenderId = lastSenderId; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}
