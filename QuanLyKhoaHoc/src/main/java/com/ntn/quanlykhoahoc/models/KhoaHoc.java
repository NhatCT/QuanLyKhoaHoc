package com.ntn.quanlykhoahoc.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class KhoaHoc {
    private int id;
    private StringProperty tenKhoaHoc;
    private StringProperty giangVien;
    private StringProperty gia;
    private String hinhAnh;

    public KhoaHoc(int id, String tenKhoaHoc, String giangVien, String gia, String hinhAnh) {
        this.id = id;
        this.tenKhoaHoc = new SimpleStringProperty(tenKhoaHoc);
        this.giangVien = new SimpleStringProperty(giangVien);
        this.gia = new SimpleStringProperty(gia);
        this.hinhAnh = hinhAnh;
    }

    public int getId() {
        return id;
    }

    public String getTenKhoaHoc() {
        return tenKhoaHoc.get();
    }

    public StringProperty tenKhoaHocProperty() {
        return tenKhoaHoc;
    }

    public String getGiangVien() {
        return giangVien.get();
    }

    public StringProperty giangVienProperty() {
        return giangVien;
    }

    public String getGia() {
        return gia.get();
    }

    public StringProperty giaProperty() {
        return gia;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public StringProperty hinhAnhProperty() {
        return new SimpleStringProperty(hinhAnh);
    }
}
