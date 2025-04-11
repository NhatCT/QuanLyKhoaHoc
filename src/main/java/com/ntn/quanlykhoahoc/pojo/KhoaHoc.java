package com.ntn.quanlykhoahoc.pojo;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Đại diện cho một thực thể khóa học với các thuộc tính liên kết JavaFX.
 */
public class KhoaHoc {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty tenKhoaHoc = new SimpleStringProperty(this, "tenKhoaHoc");
    private final IntegerProperty giangVienId = new SimpleIntegerProperty(this, "giangVienId"); // Thêm giangVienId
    private final StringProperty moTa = new SimpleStringProperty(this, "moTa");
    private final DoubleProperty gia = new SimpleDoubleProperty(this, "gia");
    private final StringProperty hinhAnh = new SimpleStringProperty(this, "hinhAnh");
    private final StringProperty tenGiangVien = new SimpleStringProperty(this, "tenGiangVien");
    private final BooleanProperty active = new SimpleBooleanProperty(this, "active");
    private final ObjectProperty<LocalDate> ngayBatDau = new SimpleObjectProperty<>(this, "ngayBatDau");
    private final ObjectProperty<LocalDate> ngayKetThuc = new SimpleObjectProperty<>(this, "ngayKetThuc");

    // Constructor mặc định
    public KhoaHoc() {
        // Không làm gì trong constructor mặc định
    }

    /**
     * Constructor đầy đủ với tất cả các thuộc tính.
     * @param id Mã khóa học
     * @param tenKhoaHoc Tên khóa học
     * @param moTa Mô tả khóa học
     * @param gia Giá khóa học
     * @param hinhAnh Đường dẫn hình ảnh
     * @param tenGiangVien Tên giảng viên
     * @param active Trạng thái hoạt động
     * @param ngayBatDau Ngày bắt đầu
     * @param ngayKetThuc Ngày kết thúc
     */
    public KhoaHoc(int id, String tenKhoaHoc, String moTa, double gia, 
                   String hinhAnh, String tenGiangVien, boolean active, 
                   LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        setId(id);
        setTenKhoaHoc(tenKhoaHoc);
        setMoTa(moTa);
        setGia(gia);
        setHinhAnh(hinhAnh);
        setTenGiangVien(tenGiangVien);
        setActive(active);
        setNgayBatDau(ngayBatDau);
        setNgayKetThuc(ngayKetThuc);
    }

    // Các phương thức getter
    public int getId() { return id.get(); }
    public String getTenKhoaHoc() { return tenKhoaHoc.get(); }
    public int getGiangVienId() { return giangVienId.get(); } // Thêm getter cho giangVienId
    public String getMoTa() { return moTa.get(); }
    public double getGia() { return gia.get(); }
    public String getHinhAnh() { return hinhAnh.get(); }
    public String getTenGiangVien() { return tenGiangVien.get(); }
    public boolean isActive() { return active.get(); }
    public LocalDate getNgayBatDau() { return ngayBatDau.get(); }
    public LocalDate getNgayKetThuc() { return ngayKetThuc.get(); }

    // Các phương thức setter với kiểm tra hợp lệ
    public void setId(int id) {
        if (id < 0) throw new IllegalArgumentException("Mã khóa học không thể âm");
        this.id.set(id);
    }

    public void setTenKhoaHoc(String tenKhoaHoc) {
        this.tenKhoaHoc.set(Objects.requireNonNullElse(tenKhoaHoc, ""));
    }

    public void setGiangVienId(int giangVienId) { // Thêm setter cho giangVienId
        if (giangVienId < 0) throw new IllegalArgumentException("ID giảng viên không thể âm");
        this.giangVienId.set(giangVienId);
    }

    public void setMoTa(String moTa) {
        this.moTa.set(Objects.requireNonNullElse(moTa, ""));
    }

    public void setGia(double gia) {
        if (gia < 0) throw new IllegalArgumentException("Giá không thể âm");
        this.gia.set(gia);
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh.set(Objects.requireNonNullElse(hinhAnh, ""));
    }

    public void setTenGiangVien(String tenGiangVien) {
        this.tenGiangVien.set(Objects.requireNonNullElse(tenGiangVien, "Chưa có giảng viên"));
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau.set(ngayBatDau);
        validateDates(); // Kiểm tra ngày hợp lệ
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc.set(ngayKetThuc);
        validateDates(); // Kiểm tra ngày hợp lệ
    }

    // Các phương thức property cho binding
    public IntegerProperty idProperty() { return id; }
    public StringProperty tenKhoaHocProperty() { return tenKhoaHoc; }
    public IntegerProperty giangVienIdProperty() { return giangVienId; } // Thêm property cho giangVienId
    public StringProperty moTaProperty() { return moTa; }
    public DoubleProperty giaProperty() { return gia; }
    public StringProperty hinhAnhProperty() { return hinhAnh; }
    public StringProperty tenGiangVienProperty() { return tenGiangVien; }
    public BooleanProperty activeProperty() { return active; }
    public ObjectProperty<LocalDate> ngayBatDauProperty() { return ngayBatDau; }
    public ObjectProperty<LocalDate> ngayKetThucProperty() { return ngayKetThuc; }

    /**
     * Kiểm tra tính hợp lệ của đối tượng trước khi lưu vào cơ sở dữ liệu.
     * @throws IllegalStateException nếu dữ liệu không hợp lệ
     */
    public void validate() throws IllegalStateException {
        if (getTenKhoaHoc() == null || getTenKhoaHoc().trim().isEmpty()) {
            throw new IllegalStateException("Tên khóa học không được để trống");
        }
        if (getMoTa() == null || getMoTa().trim().isEmpty()) {
            throw new IllegalStateException("Mô tả không được để trống");
        }
        if (getGia() < 0) {
            throw new IllegalStateException("Giá không được âm");
        }
        if (getNgayBatDau() == null || getNgayKetThuc() == null) {
            throw new IllegalStateException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }
        validateDates();
    }

    /**
     * Kiểm tra ngày bắt đầu và ngày kết thúc hợp lệ.
     * @throws IllegalStateException nếu ngày kết thúc trước ngày bắt đầu
     */
    private void validateDates() {
        LocalDate start = getNgayBatDau();
        LocalDate end = getNgayKetThuc();
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalStateException("Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    /**
     * Sao chép dữ liệu từ một đối tượng KhoaHoc khác.
     * @param other Đối tượng KhoaHoc để sao chép từ
     */
    public void copyFrom(KhoaHoc other) {
        if (other == null) return;
        setId(other.getId());
        setTenKhoaHoc(other.getTenKhoaHoc());
        setGiangVienId(other.getGiangVienId());
        setMoTa(other.getMoTa());
        setGia(other.getGia());
        setHinhAnh(other.getHinhAnh());
        setTenGiangVien(other.getTenGiangVien());
        setActive(other.isActive());
        setNgayBatDau(other.getNgayBatDau());
        setNgayKetThuc(other.getNgayKetThuc());
    }

    // So sánh đối tượng
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KhoaHoc)) return false;
        KhoaHoc khoaHoc = (KhoaHoc) o;
        return getId() == khoaHoc.getId() &&
               getGiangVienId() == khoaHoc.getGiangVienId() &&
               Double.compare(khoaHoc.getGia(), getGia()) == 0 &&
               isActive() == khoaHoc.isActive() &&
               Objects.equals(getTenKhoaHoc(), khoaHoc.getTenKhoaHoc()) &&
               Objects.equals(getMoTa(), khoaHoc.getMoTa()) &&
               Objects.equals(getHinhAnh(), khoaHoc.getHinhAnh()) &&
               Objects.equals(getTenGiangVien(), khoaHoc.getTenGiangVien()) &&
               Objects.equals(getNgayBatDau(), khoaHoc.getNgayBatDau()) &&
               Objects.equals(getNgayKetThuc(), khoaHoc.getNgayKetThuc());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTenKhoaHoc(), getGiangVienId(), getMoTa(), getGia(), 
                            getHinhAnh(), getTenGiangVien(), isActive(), 
                            getNgayBatDau(), getNgayKetThuc());
    }

    // Chuỗi đại diện cho đối tượng
    @Override
    public String toString() {
        return String.format("Khóa Học [ID: %d, Tên: %s, Giảng Viên: %s (ID: %d), Mô Tả: %s, Giá: %.2f, " +
                             "Hình Ảnh: %s, Hoạt Động: %b, Ngày Bắt Đầu: %s, Ngày Kết Thúc: %s]",
                getId(), getTenKhoaHoc(), getTenGiangVien(), getGiangVienId(), getMoTa(), getGia(),
                getHinhAnh(), isActive(), getNgayBatDau(), getNgayKetThuc());
    }
}