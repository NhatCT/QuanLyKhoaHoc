package com.ntn.quanlykhoahoc.models;

public class KhoaHoc {
    private int id;
    private String tenKhoaHoc;
    private String moTa;
    private double gia;
    private String hinhAnh;
    private String tenGiangVien;

    public KhoaHoc(int id, String tenKhoaHoc, String moTa, double gia, String hinhAnh, String tenGiangVien) {
        this.id = id;
        this.tenKhoaHoc = tenKhoaHoc;
        this.moTa = moTa;
        this.gia = gia;
        this.hinhAnh = hinhAnh;
        this.tenGiangVien = tenGiangVien;
    }

    public int getId() { return id; }
    public String getTenKhoaHoc() { return tenKhoaHoc; }
    public String getMoTa() { return moTa; }
    public double getGia() { return gia; }
    public String getHinhAnh() { return hinhAnh; }
    public String getTenGiangVien() { return tenGiangVien; }
}
