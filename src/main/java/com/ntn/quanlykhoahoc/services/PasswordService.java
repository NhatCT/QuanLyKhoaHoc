package com.ntn.quanlykhoahoc.services;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordService {

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    public String validatePassword(String password) {
        if (password.length() < 8 || password.length() > 16) {
            return "Mật khẩu phải từ 8 đến 16 ký tự.";
        }
        if (password.contains(" ")) {
            return "Mật khẩu không được chứa khoảng trắng.";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Mật khẩu phải chứa ít nhất một chữ cái thường.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Mật khẩu phải chứa ít nhất một chữ cái hoa.";
        }
        if (!password.matches(".*\\d.*")) {
            return "Mật khẩu phải chứa ít nhất một chữ số.";
        }
        if (!password.matches(".*[^A-Za-z\\d].*")) {
            return "Mật khẩu phải chứa ít nhất một ký tự đặc biệt.";
        }
        return null; // Hợp lệ
    }
}
