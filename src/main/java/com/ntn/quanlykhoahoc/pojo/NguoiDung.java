package com.ntn.quanlykhoahoc.pojo;

public class NguoiDung {
    private int id; // Primary key
    private String ho;
    private String ten;
    private String email;
    private String matKhau;
    private boolean active;
    private String avatar;
    private int loaiNguoiDungId;

    // Hằng số cho avatar mặc định
    public static final String DEFAULT_AVATAR = "/com/ntn/images/avatars/default.jpg";

    // Default constructor
    public NguoiDung() {
        this.avatar = DEFAULT_AVATAR; // Khởi tạo avatar mặc định
    }

    // Constructor with ID (for existing users)
    public NguoiDung(int id, String ho, String ten, String email, String matKhau, int loaiNguoiDungId, boolean active, String avatar) {
        this.id = id;
        this.ho = ho;
        this.ten = ten;
        this.email = email;
        this.matKhau = matKhau;
        this.active = active;
        this.avatar = (avatar != null && !avatar.trim().isEmpty()) ? avatar : DEFAULT_AVATAR; // Đảm bảo avatar không null
        this.loaiNguoiDungId = loaiNguoiDungId;
    }

    // Constructor without ID (for new users)
    public NguoiDung(String ho, String ten, String email, String matKhau, int loaiNguoiDungId, boolean active, String avatar) {
        this.ho = ho;
        this.ten = ten;
        this.email = email;
        this.matKhau = matKhau;
        this.active = active;
        this.avatar = (avatar != null && !avatar.trim().isEmpty()) ? avatar : DEFAULT_AVATAR; // Đảm bảo avatar không null
        this.loaiNguoiDungId = loaiNguoiDungId;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getHo() {
        return ho;
    }

    public String getTen() {
        return ten;
    }

    public String getEmail() {
        return email;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public boolean isActive() {
        return active;
    }

    public String getAvatar() {
        return avatar;
    }

    public int getLoaiNguoiDungId() {
        return loaiNguoiDungId;
    }

    public String getFullName() {
        return (ho != null ? ho : "") + " " + (ten != null ? ten : "");
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setHo(String ho) {
        this.ho = ho;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAvatar(String avatar) {
        this.avatar = (avatar != null && !avatar.trim().isEmpty()) ? avatar : DEFAULT_AVATAR;
    }

    public void setLoaiNguoiDungId(int loaiNguoiDungId) {
        this.loaiNguoiDungId = loaiNguoiDungId;
    }

    @Override
    public String toString() {
        return "NguoiDung{" +
                "id=" + getId() +
                ", ho='" + ho + '\'' +
                ", ten='" + ten + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                ", avatar='" + avatar + '\'' +
                ", loaiNguoiDungId=" + loaiNguoiDungId +
                '}';
    }
}