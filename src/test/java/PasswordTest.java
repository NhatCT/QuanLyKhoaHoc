package com.ntn.quanlykhoahoc.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    // Tests cho hashPassword
    @Test
    void testHashPassword_ValidPassword_ReturnsHashedPassword() {
        String password = "Test@123";
        String hashedPassword = passwordService.hashPassword(password);

        assertNotNull(hashedPassword, "Mật khẩu mã hóa không được null");
        assertTrue(hashedPassword.startsWith("$2a$10$"), "Mật khẩu mã hóa phải sử dụng BCrypt");
        assertNotEquals(password, hashedPassword, "Mật khẩu mã hóa phải khác mật khẩu gốc");
    }

    // Tests cho verifyPassword
    @Test
    void testVerifyPassword_ValidPasswordAndHash_ReturnsTrue() {
        String password = "Test@123";
        String hashedPassword = passwordService.hashPassword(password);

        boolean result = passwordService.verifyPassword(password, hashedPassword);

        assertTrue(result, "Mật khẩu hợp lệ phải được xác minh đúng");
    }

    @Test
    void testVerifyPassword_InvalidPassword_ReturnsFalse() {
        String password = "Test@123";
        String wrongPassword = "Wrong@123";
        String hashedPassword = passwordService.hashPassword(password);

        boolean result = passwordService.verifyPassword(wrongPassword, hashedPassword);

        assertFalse(result, "Mật khẩu không hợp lệ phải được xác minh sai");
    }

    // Tests cho validatePassword
    @Test
    void testValidatePassword_ValidPassword_ReturnsNull() {
        String validPassword = "Test@123";

        String result = passwordService.validatePassword(validPassword);

        assertNull(result, "Mật khẩu hợp lệ phải trả về null");
    }

    @ParameterizedTest
    @CsvSource({
            "Test@12, Mật khẩu phải từ 8 đến 16 ký tự.",
            "Test@123456789012, Mật khẩu phải từ 8 đến 16 ký tự.",
            "Test @123, Mật khẩu không được chứa khoảng trắng.",
            "TEST@123, Mật khẩu phải chứa ít nhất một chữ cái thường.",
            "test@123, Mật khẩu phải chứa ít nhất một chữ cái hoa.",
            "Test@test, Mật khẩu phải chứa ít nhất một chữ số.",
            "Test1234, Mật khẩu phải chứa ít nhất một ký tự đặc biệt."
    })
    void testValidatePassword_InvalidPassword_ReturnsErrorMessage(String password, String expectedError) {
        String result = passwordService.validatePassword(password);

        assertEquals(expectedError, result, "Thông báo lỗi phải khớp với trường hợp mật khẩu không hợp lệ");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", // Mật khẩu rỗng
            "      " // Mật khẩu chỉ chứa khoảng trắng
    })
    void testValidatePassword_EmptyOrBlankPassword_ReturnsLengthError(String password) {
        String result = passwordService.validatePassword(password);

        assertEquals("Mật khẩu phải từ 8 đến 16 ký tự.", result, "Mật khẩu rỗng hoặc chỉ chứa khoảng trắng phải báo lỗi độ dài");
    }
}