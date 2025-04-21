/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.quanlykhoahoc.pojo;

/**
 *
 * @author ADMIN
 */
public class CauHoi {
    private int id;
    private String noiDung;
    private int baiTapID;

    public CauHoi(int id, String noiDung, int baiTapID) {
        this.id = id;
        this.noiDung = noiDung;
        this.baiTapID = baiTapID;
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
     * @return the noiDung
     */
    public String getNoiDung() {
        return noiDung;
    }

    /**
     * @param noiDung the noiDung to set
     */
    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
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
}
