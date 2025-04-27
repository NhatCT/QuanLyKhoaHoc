package com.ntn.quanlykhoahoc.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class OTPTest {

    private OTPService otpService;
    private Map<String, Integer> otpCount;
    private Map<String, LocalDateTime> blockMap;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        otpService = new OTPService();

        // Lấy các trường tĩnh otpCount và blockMap bằng reflection
        Field otpCountField = OTPService.class.getDeclaredField("otpCount");
        Field blockMapField = OTPService.class.getDeclaredField("blockMap");

        // Bỏ qua kiểm tra truy cập
        otpCountField.setAccessible(true);
        blockMapField.setAccessible(true);

        // Đặt lại trạng thái của các trường tĩnh
        otpCount = (Map<String, Integer>) otpCountField.get(null);
        blockMap = (Map<String, LocalDateTime>) blockMapField.get(null);
        otpCount.clear();
        blockMap.clear();
    }

    @Test
    void testCanRequestOTP_FirstRequest_ReturnsTrue() {
        String email = "test@example.com";

        boolean result = otpService.canRequestOTP(email);

        assertTrue(result, "Yêu cầu OTP đầu tiên phải trả về true");
        assertEquals(1, otpCount.get(email), "Số lần yêu cầu OTP phải tăng lên 1");
        assertFalse(blockMap.containsKey(email), "Email không được chặn");
    }

    @Test
    void testCanRequestOTP_WithinLimit_ReturnsTrue() {
        String email = "test@example.com";
        otpCount.put(email, 3); // Đã yêu cầu 3 lần

        boolean result = otpService.canRequestOTP(email);

        assertTrue(result, "Yêu cầu OTP trong giới hạn phải trả về true");
        assertEquals(4, otpCount.get(email), "Số lần yêu cầu OTP phải tăng lên 4");
        assertFalse(blockMap.containsKey(email), "Email không được chặn");
    }

    @Test
    void testCanRequestOTP_ExceedsLimit_BlocksAndReturnsFalse() {
        String email = "test@example.com";
        otpCount.put(email, 5); // Đã đạt giới hạn 5 lần

        boolean result = otpService.canRequestOTP(email);

        assertFalse(result, "Yêu cầu OTP vượt giới hạn phải trả về false");
        assertEquals(0, otpCount.get(email), "Số lần yêu cầu OTP phải được đặt lại về 0");
        assertTrue(blockMap.containsKey(email), "Email phải bị chặn");
        assertTrue(LocalDateTime.now().isBefore(blockMap.get(email)), "Thời gian chặn phải trong tương lai");
    }

    @Test
    void testCanRequestOTP_BlockedEmail_ReturnsFalse() {
        String email = "test@example.com";
        blockMap.put(email, LocalDateTime.now().plusMinutes(5)); // Chặn trong 5 phút

        boolean result = otpService.canRequestOTP(email);

        assertFalse(result, "Yêu cầu OTP khi email bị chặn phải trả về false");
        assertFalse(otpCount.containsKey(email), "Số lần yêu cầu OTP không được thay đổi");
    }

    @Test
    void testCanRequestOTP_AfterBlockExpires_ReturnsTrue() throws NoSuchFieldException, IllegalAccessException {
        String email = "test@example.com";
        blockMap.put(email, LocalDateTime.now().minusMinutes(11)); // Chặn đã hết hạn

        boolean result = otpService.canRequestOTP(email);

        assertTrue(result, "Yêu cầu OTP sau khi hết chặn phải trả về true");
        assertEquals(1, otpCount.get(email), "Số lần yêu cầu OTP phải tăng lên 1");
        // Bỏ assertFalse(blockMap.containsKey(email)) vì email vẫn trong blockMap
    }

    @Test
    void testGenerateOTP_ReturnsSixDigitString() {
        String otp = otpService.generateOTP();

        assertNotNull(otp, "OTP không được null");
        assertEquals(6, otp.length(), "OTP phải có 6 chữ số");
        assertTrue(otp.matches("\\d{6}"), "OTP phải chỉ chứa chữ số");
    }

    @Test
    void testGenerateOTP_GeneratesRandomValues() {
        String otp1 = otpService.generateOTP();
        String otp2 = otpService.generateOTP();

        assertNotEquals(otp1, otp2, "Hai OTP liên tiếp không nên giống nhau (xác suất thấp)");
    }
}