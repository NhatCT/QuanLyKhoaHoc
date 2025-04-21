/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.quanlykhoahoc.pojo;

/**
 *
 * @author ADMIN
 */
public class DapAn {

    private int id;
    private String noiDung;
    private boolean dapAnDung;
    private int cauHoiID;

    public DapAn(int id, String noiDung, boolean dapAnDung, int cauHoiID) {
        this.id = id;
        this.noiDung = noiDung;
        this.dapAnDung = dapAnDung;
        this.cauHoiID = cauHoiID;
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
     * @return the dapAnDung
     */
    public boolean isDapAnDung() {
        return dapAnDung;
    }

    /**
     * @param dapAnDung the dapAnDung to set
     */
    public void setDapAnDung(boolean dapAnDung) {
        this.dapAnDung = dapAnDung;
    }

    /**
     * @return the cauHoiID
     */
    public int getCauHoiID() {
        return cauHoiID;
    }

    /**
     * @param cauHoiID the cauHoiID to set
     */
    public void setCauHoiID(int cauHoiID) {
        this.cauHoiID = cauHoiID;
    }
}
