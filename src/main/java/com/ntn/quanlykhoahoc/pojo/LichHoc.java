package com.ntn.quanlykhoahoc.pojo;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class LichHoc {
    private final IntegerProperty id;
    private final IntegerProperty khoaHocId;
    private final StringProperty tenKhoaHoc;
    private final ObjectProperty<LocalDate> ngayHoc;
    private final ObjectProperty<LocalTime> gioBatDau;
    private final ObjectProperty<LocalTime> gioKetThuc;
    private final IntegerProperty giangVienId;
    private final StringProperty giangVien;
    private final StringProperty lienKet;
    private final StringProperty status;

    public LichHoc(int id, int khoaHocId, String tenKhoaHoc, LocalDate ngayHoc, LocalTime gioBatDau,
                   LocalTime gioKetThuc, int giangVienId, String giangVien, String lienKet) {
        this.id = new SimpleIntegerProperty(id);
        this.khoaHocId = new SimpleIntegerProperty(khoaHocId);
        this.tenKhoaHoc = new SimpleStringProperty(tenKhoaHoc);
        this.ngayHoc = new SimpleObjectProperty<>(ngayHoc);
        this.gioBatDau = new SimpleObjectProperty<>(gioBatDau);
        this.gioKetThuc = new SimpleObjectProperty<>(gioKetThuc);
        this.giangVienId = new SimpleIntegerProperty(giangVienId);
        this.giangVien = new SimpleStringProperty(giangVien);
        this.lienKet = new SimpleStringProperty(lienKet);
        this.status = new SimpleStringProperty();

        calculateStatus();
        validateTimes();
    }

    // Getters
    public int getId() { return id.get(); }
    public int getKhoaHocId() { return khoaHocId.get(); }
    public String getTenKhoaHoc() { return tenKhoaHoc.get(); }
    public LocalDate getNgayHoc() { return ngayHoc.get(); }
    public LocalTime getGioBatDau() { return gioBatDau.get(); }
    public LocalTime getGioKetThuc() { return gioKetThuc.get(); }
    public int getGiangVienId() { return giangVienId.get(); }
    public String getGiangVien() { return giangVien.get(); }
    public String getLienKet() { return lienKet.get(); }
    public String getStatus() { return status.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setKhoaHocId(int khoaHocId) { this.khoaHocId.set(khoaHocId); }
    public void setTenKhoaHoc(String tenKhoaHoc) { this.tenKhoaHoc.set(tenKhoaHoc); }
    public void setNgayHoc(LocalDate ngayHoc) { 
        this.ngayHoc.set(ngayHoc); 
        calculateStatus();
    }
    public void setGioBatDau(LocalTime gioBatDau) { 
        this.gioBatDau.set(gioBatDau); 
        validateTimes();
        calculateStatus();
    }
    public void setGioKetThuc(LocalTime gioKetThuc) { 
        this.gioKetThuc.set(gioKetThuc); 
        validateTimes();
        calculateStatus();
    }
    public void setGiangVienId(int giangVienId) { this.giangVienId.set(giangVienId); }
    public void setGiangVien(String giangVien) { this.giangVien.set(giangVien); }
    public void setLienKet(String lienKet) { this.lienKet.set(lienKet); }

    // JavaFX Property getters
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty khoaHocIdProperty() { return khoaHocId; }
    public StringProperty tenKhoaHocProperty() { return tenKhoaHoc; }
    public ObjectProperty<LocalDate> ngayHocProperty() { return ngayHoc; }
    public ObjectProperty<LocalTime> gioBatDauProperty() { return gioBatDau; }
    public ObjectProperty<LocalTime> gioKetThucProperty() { return gioKetThuc; }
    public IntegerProperty giangVienIdProperty() { return giangVienId; }
    public StringProperty giangVienProperty() { return giangVien; }
    public StringProperty lienKetProperty() { return lienKet; }
    public StringProperty statusProperty() { return status; }

    private void validateTimes() {
        if (gioBatDau.get() != null && gioKetThuc.get() != null) {
            if (gioBatDau.get().isAfter(gioKetThuc.get())) {
                throw new IllegalArgumentException("Giờ bắt đầu phải trước giờ kết thúc.");
            }
        }
    }

    public void calculateStatus() {
        if (ngayHoc.get() == null || gioBatDau.get() == null || gioKetThuc.get() == null) {
            status.set("Chưa xác định");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sessionStart = LocalDateTime.of(ngayHoc.get(), gioBatDau.get());
        LocalDateTime sessionEnd = LocalDateTime.of(ngayHoc.get(), gioKetThuc.get());

        if (now.isBefore(sessionStart)) {
            status.set("Sắp tới");
        } else if (now.isAfter(sessionEnd)) {
            status.set("Đã kết thúc");
        } else {
            status.set("Đang diễn ra");
        }
    }

    @Override
    public String toString() {
        return "LichHoc{" +
                "id=" + id.get() +
                ", khoaHocId=" + khoaHocId.get() +
                ", tenKhoaHoc='" + tenKhoaHoc.get() + '\'' +
                ", ngayHoc=" + ngayHoc.get() +
                ", gioBatDau=" + gioBatDau.get() +
                ", gioKetThuc=" + gioKetThuc.get() +
                ", giangVienId=" + giangVienId.get() +
                ", giangVien='" + giangVien.get() + '\'' +
                ", lienKet='" + lienKet.get() + '\'' +
                ", status='" + status.get() + '\'' +
                '}';
    }
}