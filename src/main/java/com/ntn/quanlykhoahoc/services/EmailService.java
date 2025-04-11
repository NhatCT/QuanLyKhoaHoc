package com.ntn.quanlykhoahoc.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailService {

    // --- Kiểm tra định dạng email ---
    public boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) return false;

        String domain = email.substring(email.lastIndexOf("@") + 1);
        String topLevelDomain = domain.substring(domain.lastIndexOf(".") + 1);
        return topLevelDomain.matches(".*[A-Za-z].*");
    }

    // --- Gửi email xác thực ---
    public boolean sendEmail(String toEmail, String otp) {
        if (!isValidEmail(toEmail)) {
            System.out.println("Email không hợp lệ: " + toEmail);
            return false;
        }

        // Thông tin tài khoản Gmail gửi mail
        final String fromEmail = "nhatlovely2017@gmail.com"; // <- Thay bằng email thật
        final String password = "zmmd wfhj ccmz igcm";     // <- Thay bằng mật khẩu ứng dụng

        // Cấu hình SMTP server
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // TLS
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Tạo session với thông tin xác thực
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            // Tạo nội dung email
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Mã OTP xác thực");
            message.setText("Xin chào!\n\nMã OTP của bạn là: " + otp + "\n\nTrân trọng!");

            // Gửi mail
            Transport.send(message);
            System.out.println("✅ Gửi email thành công đến " + toEmail);
            return true;

        } catch (MessagingException e) {
            System.err.println("❌ Gửi email thất bại: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
