/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.quanlykhoahoc.pojo;

import java.sql.Date;

/**
 *
 * @author ADMIN
 */
public class KhoaHoc_HocVien {
    private int id;
    private int hocVienID;
    private int khoaHocID;
    private Date ngay_dang_ky;
    private String trang_thai;

    public KhoaHoc_HocVien(int id, int hocVienID, int khoaHocID, Date ngay_dang_ky, String trang_thai) {
        this.id = id;
        this.hocVienID = hocVienID;
        this.khoaHocID = khoaHocID;
        this.ngay_dang_ky = ngay_dang_ky;
        this.trang_thai = trang_thai;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the hocVienID
     */
    public int getHocVienID() {
        return hocVienID;
    }

    /**
     * @param hocVienID the hocVienID to set
     */
    public void setHocVienID(int hocVienID) {
        this.hocVienID = hocVienID;
    }

    /**
     * @return the khoaHocID
     */
    public int getKhoaHocID() {
        return khoaHocID;
    }

    /**
     * @param khoaHocID the khoaHocID to set
     */
    public void setKhoaHocID(int khoaHocID) {
        this.khoaHocID = khoaHocID;
    }

    /**
     * @return the ngay_dang_ky
     */
    public Date getNgay_dang_ky() {
        return ngay_dang_ky;
    }

    /**
     * @param ngay_dang_ky the ngay_dang_ky to set
     */
    public void setNgay_dang_ky(Date ngay_dang_ky) {
        this.ngay_dang_ky = ngay_dang_ky;
    }

    /**
     * @return the trang_thai
     */
    public String getTrang_thai() {
        return trang_thai;
    }

    /**
     * @param trang_thai the trang_thai to set
     */
    public void setTrang_thai(String trang_thai) {
        this.trang_thai = trang_thai;
    }
    
}