package com.ntn.quanlykhoahoc.pojo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ThanhToan {
    private final StringProperty thanhToanID = new SimpleStringProperty();
    private final StringProperty ngayThanhToan = new SimpleStringProperty();
    private final StringProperty soTien = new SimpleStringProperty();
    private final StringProperty phuongThuc = new SimpleStringProperty();
    private final StringProperty hocVienID = new SimpleStringProperty();
    private final StringProperty khoaHocID = new SimpleStringProperty();

    public ThanhToan() {}

    public ThanhToan(String thanhToanID, String ngayThanhToan, String soTien, String phuongThuc,
                    String hocVienID, String khoaHocID) {
        this.thanhToanID.set(thanhToanID);
        this.ngayThanhToan.set(ngayThanhToan);
        this.soTien.set(soTien);
        this.phuongThuc.set(phuongThuc);
        this.hocVienID.set(hocVienID);
        this.khoaHocID.set(khoaHocID);
    }

    // Getters and Property methods
    public String getThanhToanID() { return thanhToanID.get(); }
    public StringProperty thanhToanIDProperty() { return thanhToanID; }
    public void setThanhToanID(String thanhToanID) { this.thanhToanID.set(thanhToanID); }

    public String getNgayThanhToan() { return ngayThanhToan.get(); }
    public StringProperty ngayThanhToanProperty() { return ngayThanhToan; }
    public void setNgayThanhToan(String ngayThanhToan) { this.ngayThanhToan.set(ngayThanhToan); }

    public String getSoTien() { return soTien.get(); }
    public StringProperty soTienProperty() { return soTien; }
    public void setSoTien(String soTien) { this.soTien.set(soTien); }

    public String getPhuongThuc() { return phuongThuc.get(); }
    public StringProperty phuongThucProperty() { return phuongThuc; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc.set(phuongThuc); }

    public String getHocVienID() { return hocVienID.get(); }
    public StringProperty hocVienIDProperty() { return hocVienID; }
    public void setHocVienID(String hocVienID) { this.hocVienID.set(hocVienID); }

    public String getKhoaHocID() { return khoaHocID.get(); }
    public StringProperty khoaHocIDProperty() { return khoaHocID; }
    public void setKhoaHocID(String khoaHocID) { this.khoaHocID.set(khoaHocID); }
}