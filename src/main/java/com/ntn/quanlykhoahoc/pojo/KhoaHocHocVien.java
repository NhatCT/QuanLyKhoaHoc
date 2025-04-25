package com.ntn.quanlykhoahoc.pojo;

import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class KhoaHocHocVien {
    private final int id;
    private final int hocVienID;
    private final int khoaHocID;
    private final SimpleStringProperty ngayDangKy;
    private final SimpleStringProperty trangThai;

    public KhoaHocHocVien(int id, int hocVienID, int khoaHocID, String ngayDangKy, String trangThai) {
        this.id = id;
        this.hocVienID = hocVienID;
        this.khoaHocID = khoaHocID;
        this.ngayDangKy = new SimpleStringProperty();
        this.trangThai = new SimpleStringProperty(trangThai);

        if (ngayDangKy != null && !ngayDangKy.isEmpty()) {
            ngayDangKy = ngayDangKy.replaceAll("\\.0$", "");
            try {
                // Hỗ trợ định dạng có microsecond (7 chữ số) hoặc không có
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSSS]");
                LocalDateTime parsedDate = LocalDateTime.parse(ngayDangKy, formatter);
                // Lưu dưới dạng yyyy-MM-dd HH:mm:ss
                this.ngayDangKy.set(parsedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Định dạng ngày đăng ký không hợp lệ: " + ngayDangKy, e);
            }
        } else {
            this.ngayDangKy.set("");
        }
    }

    public int getId() {
        return id;
    }

    public int getHocVienID() {
        return hocVienID;
    }

    public int getKhoaHocID() {
        return khoaHocID;
    }

    public String getNgayDangKy() {
        return ngayDangKy.get();
    }

    public SimpleStringProperty ngayDangKyProperty() {
        return ngayDangKy;
    }

    public LocalDateTime getNgay_dang_ky() {
        String ngayDangKyStr = ngayDangKy.get();
        if (ngayDangKyStr == null || ngayDangKyStr.isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(ngayDangKyStr, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("Không thể phân tích ngày đăng ký: " + ngayDangKyStr, e);
        }
    }

    public String getTrangThai() {
        return trangThai.get();
    }

    public SimpleStringProperty trangThaiProperty() {
        return trangThai;
    }
}