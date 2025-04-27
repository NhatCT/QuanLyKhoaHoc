/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.quanlykhoahoc.pojo;
import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author ADMIN
 */
public class HocVien_BaiTap {

    private int id;
    private int hocVienID;
    private int baiTapID;
    private int diem;
    private Timestamp ngayNop;

    public HocVien_BaiTap(int id, int hocVienID, int baiTapID, int diem, Timestamp ngayNop) {
        this.id = id;
        this.hocVienID = hocVienID;
        this.baiTapID = baiTapID;
        this.diem = diem;
        this.ngayNop = ngayNop;
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
     * @return the baiTapID
     */
    public int getBaiTapID() {
        return baiTapID;
    }

    /**
     * @param baiTapID the baiTapID to set
     */
    public void setBaiTapID(int baiTapID) {
        this.baiTapID = baiTapID;
    }

    /**
     * @return the diem
     */
    public int getDiem() {
        return diem;
    }

    /**
     * @param diem the diem to set
     */
    public void setDiem(int diem) {
        this.diem = diem;
    }

    /**
     * @return the ngayNop
     */
    public Timestamp getNgayNop() {
        return ngayNop;
    }

    /**
     * @param ngayNop the ngayNop to set
     */
    public void setNgayNop(Timestamp ngayNop) {
        this.ngayNop = ngayNop;
    }

}