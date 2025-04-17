//import com.ntn.quanlykhoahoc.controllers.Login;
//import org.junit.jupiter.api.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class LoginTest {
//    private Login loginController;
//
//    @BeforeEach
//    void setUp() {
//        loginController = new Login();
//    }
//
//    @Test
//    void testAuthenticate_Success() {
//        boolean result = loginController.authenticate("2251052082nhat@ou.edu.vn", "Nhat#1908", "Học viên");
//        assertTrue(result, "Đăng nhập thất bại!");
//    }
//
//    @Test
//    void testAuthenticate_Failure_WrongPassword() {
//        boolean result = loginController.authenticate("2251052082nhat@ou.edu.vn", "Nhat#1907", "Học viên");
//        assertFalse(result, "Đăng nhập mật khẩu sai!");
//    }
//
//    @Test
//    void testAuthenticate_Failure_UserNotFound() {
//        boolean result = loginController.authenticate("notexist@ou.edu.vn", "Nhat#1908", "Học viên");
//        assertFalse(result, "Đăng nhập user không tồn tại!");
//    }
//
//    @Test
//    void testAuthenticate_InvalidRole() {
//        boolean result = loginController.authenticate("2251052082nhat@ou.edu.vn", "Nhat#1908", "Admin");
//        assertFalse(result, "Đăng nhập  role sai!");
//    }
//}
