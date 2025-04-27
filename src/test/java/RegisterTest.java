import com.ntn.quanlykhoahoc.services.PasswordService;
import com.ntn.quanlykhoahoc.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterTest {

    private PasswordService passwordService;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
        emailService = new EmailService();
    }

    @Test
    void testValidPassword() {
        assertNull(passwordService.validatePassword("Abc@1234"), "Mật khẩu hợp lệ bị từ chối!");
        assertNotNull(passwordService.validatePassword("123456"), "Chấp nhận mật khẩu quá đơn giản!");
        assertNotNull(passwordService.validatePassword("Abcdabcd"), "Thiếu số và ký tự đặc biệt!");
        assertNotNull(passwordService.validatePassword("Abc12345"), "Thiếu ký tự đặc biệt!");
        assertNotNull(passwordService.validatePassword("A@1"), "Quá ngắn!");
        assertNotNull(passwordService.validatePassword("A@1longpasswordlong"), "Quá dài!");
    }

    @Test
    void testValidEmail() {
        assertTrue(emailService.isValidEmail("test@example.com"), "Email hợp lệ bị từ chối!");
        assertFalse(emailService.isValidEmail("test@example"), "Chấp nhận email không có đuôi!");
        assertFalse(emailService.isValidEmail("test@.com"), "Chấp nhận email thiếu tên miền!");
        assertFalse(emailService.isValidEmail("test.com"), "Chấp nhận email không có '@'!");
    }

    @Test
    void testHashPassword() {
        String password = "Abc@1234";
        String hashed = passwordService.hashPassword(password);
        assertNotNull(hashed, "Băm mật khẩu thất bại!");
        assertNotEquals(password, hashed, "Mật khẩu chưa được băm!");
    }
}
