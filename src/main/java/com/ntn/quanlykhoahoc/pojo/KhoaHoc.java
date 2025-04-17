package com.ntn.quanlykhoahoc.pojo;

import javafx.beans.property.*;

import java.time.LocalDate;

public class KhoaHoc {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty tenKhoaHoc = new SimpleStringProperty();
    private final StringProperty moTa = new SimpleStringProperty();
    private final DoubleProperty gia = new SimpleDoubleProperty();
    private final IntegerProperty soLuongHocVienToiDa = new SimpleIntegerProperty();
    private final StringProperty tenGiangVien = new SimpleStringProperty();
    private final StringProperty hinhAnh = new SimpleStringProperty();
    private final BooleanProperty active = new SimpleBooleanProperty();
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private int giangVienId;

    // Constructor cho sinh viên (không cần giangVienId)
    public KhoaHoc(int id, String tenKhoaHoc, String moTa, double gia, int soLuongHocVienToiDa, String tenGiangVien,
                   String hinhAnh, boolean active, LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        this.id.set(id);
        this.tenKhoaHoc.set(tenKhoaHoc);
        this.moTa.set(moTa);
        this.gia.set(gia);
        this.soLuongHocVienToiDa.set(soLuongHocVienToiDa);
        this.tenGiangVien.set(tenGiangVien);
        this.hinhAnh.set(hinhAnh);
        this.active.set(active);
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    // Constructor mặc định
    public KhoaHoc() {
        this(0, "", "", 0.0, 40, "", "", false, null, null);
    }

    // Getters và Setters
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getTenKhoaHoc() {
        return tenKhoaHoc.get();
    }

    public void setTenKhoaHoc(String tenKhoaHoc) {
        this.tenKhoaHoc.set(tenKhoaHoc);
    }

    public String getMoTa() {
        return moTa.get();
    }

    public void setMoTa(String moTa) {
        this.moTa.set(moTa);
    }

    public double getGia() {
        return gia.get();
    }

    public void setGia(double gia) {
        this.gia.set(gia);
    }

    public int getSoLuongHocVienToiDa() {
        return soLuongHocVienToiDa.get();
    }

    public void setSoLuongHocVienToiDa(int soLuongHocVienToiDa) {
        this.soLuongHocVienToiDa.set(soLuongHocVienToiDa);
    }

    public String getTenGiangVien() {
        return tenGiangVien.get();
    }

    public void setTenGiangVien(String tenGiangVien) {
        this.tenGiangVien.set(tenGiangVien);
    }

    public String getHinhAnh() {
        return hinhAnh.get();
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh.set(hinhAnh);
    }

    public boolean isActive() {
        return active.get();
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public int getGiangVienId() {
        return giangVienId;
    }

    public void setGiangVienId(int giangVienId) {
        this.giangVienId = giangVienId;
    }

    // Property getters cho JavaFX
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty tenKhoaHocProperty() {
        return tenKhoaHoc;
    }

    public StringProperty moTaProperty() {
        return moTa;
    }

    public DoubleProperty giaProperty() {
        return gia;
    }

    public IntegerProperty soLuongHocVienToiDaProperty() {
        return soLuongHocVienToiDa;
    }

    public StringProperty tenGiangVienProperty() {
        return tenGiangVien;
    }

    public StringProperty hinhAnhProperty() {
        return hinhAnh;
    }

    public BooleanProperty activeProperty() {
        return active;
    }
}