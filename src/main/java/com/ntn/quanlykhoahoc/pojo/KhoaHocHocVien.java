package com.ntn.quanlykhoahoc.pojo;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Lớp đại diện cho bản ghi đăng ký khóa học của học viên.
 */
public class KhoaHocHocVien {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty hocVienID = new SimpleIntegerProperty();
    private final IntegerProperty khoaHocID = new SimpleIntegerProperty();
    private final StringProperty ngayDangKy = new SimpleStringProperty();
    private final StringProperty trangThai = new SimpleStringProperty();

    public KhoaHocHocVien(int id, int hocVienID, int khoaHocID, String ngayDangKy, String trangThai) {
        if (id < 0 || hocVienID < 0 || khoaHocID < 0) {
            throw new IllegalArgumentException("ID phải không âm");
        }
        if (trangThai == null || !trangThai.matches("PENDING|ENROLLED|REJECTED")) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        }
        this.id.set(id);
        this.hocVienID.set(hocVienID);
        this.khoaHocID.set(khoaHocID);
        this.ngayDangKy.set(ngayDangKy);
        this.trangThai.set(trangThai);
    }

    // Getters và Property Getters
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    public int getHocVienID() { return hocVienID.get(); }
    public IntegerProperty hocVienIDProperty() { return hocVienID; }

    public int getKhoaHocID() { return khoaHocID.get(); }
    public IntegerProperty khoaHocIDProperty() { return khoaHocID; }

    public String getNgayDangKy() { return ngayDangKy.get(); }
    public StringProperty ngayDangKyProperty() { return ngayDangKy; }

    public String getTrangThai() { return trangThai.get(); }
    public StringProperty trangThaiProperty() { return trangThai; }
}