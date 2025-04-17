//import com.ntn.quanlykhoahoc.controllers.Register;
//import org.junit.jupiter.api.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//class RegisterTest {
//
//    private Register registerController;
//
//    @BeforeEach
//    void setUp() {
//        registerController = new Register();
//    }
//
//    @Test
//    void testValidPassword() {
//        assertTrue(registerController.isValidPassword("Abc@1234"), "Mật khẩu hợp lệ bị từ chối!");
//        assertFalse(registerController.isValidPassword("123456"), "Chấp nhận mật khẩu quá đơn giản!");
//        assertFalse(registerController.isValidPassword("Abcdabcd"), "Thiếu số và ký tự đặc biệt!");
//        assertFalse(registerController.isValidPassword("Abc12345"), "Thiếu ký tự đặc biệt!");
//        assertFalse(registerController.isValidPassword("A@1"), "Quá ngắn!");
//        assertFalse(registerController.isValidPassword("A@1longpasswordlong"), "Quá dài!");
//    }
//
//    @Test
//    void testValidEmail() {
//        assertTrue(registerController.isValidEmail("test@example.com"), "Email hợp lệ bị từ chối!");
//        assertFalse(registerController.isValidEmail("test@example"), "Chấp nhận email không có đuôi!");
//        assertFalse(registerController.isValidEmail("test@.com"), "Chấp nhận email thiếu tên miền!");
//        assertFalse(registerController.isValidEmail("test.com"), "Chấp nhận email không có '@'!");
//    }
//
//    @Test
//    void testHashPassword() {
//        String password = "Abc@1234";
//        String hashed = registerController.hashPassword(password);
//        assertNotNull(hashed, "Băm mật khẩu thất bại!");
//        assertNotEquals(password, hashed, "Mật khẩu chưa được băm!");
//    }
//}
