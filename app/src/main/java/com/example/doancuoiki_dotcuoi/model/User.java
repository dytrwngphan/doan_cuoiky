package com.example.doancuoiki_dotcuoi.model;

public class User {
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String profileImage;
    private String status;
    private long createdAt;
    private long updatedAt;
    private double lat; // Kinh độ
    private double lng; // Vĩ độ
    private String addressText; // Địa chỉ chuỗi (hiển thị & tìm kiếm)

    public User() {}

    public User(String userId, String fullName, String email, String phone, String address, String profileImage,
                String status, long createdAt, long updatedAt, double lat, double lng, String addressText) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.profileImage = profileImage;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lat = lat;
        this.lng = lng;
        this.addressText = addressText;
    }

    // Getter và Setter đầy đủ
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public String getAddressText() { return addressText; }
    public void setAddressText(String addressText) { this.addressText = addressText; }
}
