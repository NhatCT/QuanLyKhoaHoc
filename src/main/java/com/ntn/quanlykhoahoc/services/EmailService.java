package com.ntn.quanlykhoahoc.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String FROM_EMAIL = "nhatlovely2017@gmail.com";
    private static final String APP_PASSWORD = "zmmd wfhj ccmz igcm";

    public boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) return false;

        String domain = email.substring(email.lastIndexOf("@") + 1);
        String topLevelDomain = domain.substring(domain.lastIndexOf(".") + 1);
        return topLevelDomain.matches(".*[A-Za-z].*");
    }

    public boolean sendOtpEmail(String toEmail, String otp) {
        String subject = "Mã OTP xác thực";
        String content = "Xin chào!\n\nMã OTP của bạn là: " + otp + "\n\nTrân trọng!";
        return sendEmail(toEmail, subject, content);
    }

    public boolean sendEmail(String toEmail, String subject, String content) {
        if (!isValidEmail(toEmail)) {
            System.out.println("Email không hợp lệ: " + toEmail);
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
            System.out.println("✅ Gửi email thành công đến " + toEmail);
            return true;

        } catch (MessagingException e) {
            System.err.println("❌ Gửi email thất bại: " + e.getMessage());
            return false;
        }
    }
}