package com.example.doancuoiki_dotcuoi.model;

public class Message {
    private String id;
    private String senderId;
    private String receiverId;
    private String text;          // nội dung tin nhắn (có thể là text, emoji)
    private String imageUrl;      // url Cloudinary (nếu là tin nhắn ảnh)
    private long timestamp;
    private String type;          // "text" hoặc "image"

    public Message() {} // Firestore cần constructor rỗng

    public Message(String id, String senderId, String receiverId, String text, String imageUrl, long timestamp, String type) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.type = type;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
