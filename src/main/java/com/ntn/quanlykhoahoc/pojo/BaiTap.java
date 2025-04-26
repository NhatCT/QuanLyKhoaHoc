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
public class BaiTap {

    private int id;
    private int khoaHocID;
    private String tenBaiTap;
    private Timestamp deadline;

    public BaiTap(int id, int khoaHocID, String tenBaiTap, Timestamp deadline) {
        this.id = id;
        this.khoaHocID = khoaHocID;
        this.tenBaiTap = tenBaiTap;
        this.deadline = deadline;
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
     * @return the tenBaiTap
     */
    public String getTenBaiTap() {
        return tenBaiTap;
    }

    /**
     * @param tenBaiTap the tenBaiTap to set
     */
    public void setTenBaiTap(String tenBaiTap) {
        this.tenBaiTap = tenBaiTap;
    }

    /**
     * @return the deadline
     */
    public Timestamp getDeadline() {
        return deadline;
    }

    /**
     * @param deadline the deadline to set
     */
    public void setDeadline(Timestamp deadline) {
        this.deadline = deadline;
    }
}
