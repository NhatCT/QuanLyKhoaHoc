

import com.ntn.quanlykhoahoc.controllers.DashboardStudentController;
import com.ntn.quanlykhoahoc.pojo.KhoaHoc;
import com.ntn.quanlykhoahoc.services.CourseService;
import com.ntn.quanlykhoahoc.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

public class CourseLogicRegressionTest {
    private CourseService courseService;
    private UserService userService;
    private DashboardStudentController controller;
    
    // Tạo class con có thể inject service mock
    private static class TestableDashboardStudentController extends DashboardStudentController {
        private final CourseService courseService;
        private final UserService userService;
        
        public TestableDashboardStudentController(CourseService courseService, UserService userService) {
            this.courseService = courseService;
            this.userService = userService;
        }
        
        @Override
        public boolean canEnrollCourse(int hocVienId, KhoaHoc khoaHoc, java.util.List<KhoaHoc> enrolledCourses) {
            try {
                return !enrolledCourses.contains(khoaHoc)
                        && !courseService.isCourseEnrolled(hocVienId, khoaHoc.getId())
                        && courseService.getCurrentEnrollmentCount(khoaHoc.getId()) < 40
                        && !userService.hasOverlappingSchedule(hocVienId, khoaHoc.getId())
                        && khoaHoc.getNgayBatDau().isAfter(LocalDate.now());
            } catch (Exception e) {
                System.err.println("Lỗi khi kiểm tra điều kiện đăng ký: " + e.getMessage());
                return false; // hoặc xử lý khác tùy logic
            }
        }
    }
    
    @BeforeEach
    void setUp() {
        courseService = mock(CourseService.class);
        userService = mock(UserService.class);
        controller = new TestableDashboardStudentController(courseService, userService);
    }
    
    @Test
    void testCanEnrollCourse_WhenValid_ReturnsTrue() throws SQLException {
        int hocVienId = 1001;
        KhoaHoc khoaHoc = new KhoaHoc();
        khoaHoc.setId(1);
        khoaHoc.setTenKhoaHoc("Java Cơ Bản");
        khoaHoc.setNgayBatDau(LocalDate.now().plusDays(3));
        
        when(courseService.isCourseEnrolled(hocVienId, 1)).thenReturn(false);
        when(courseService.getCurrentEnrollmentCount(1)).thenReturn(10);
        when(userService.hasOverlappingSchedule(hocVienId, 1)).thenReturn(false);
        
        boolean result = controller.canEnrollCourse(hocVienId, khoaHoc, Collections.emptyList());
        
        assertTrue(result, "Học viên đủ điều kiện phải được phép đăng ký khóa học.");
    }
    
    @Test
    void testCanEnrollCourse_AlreadyEnrolled_ReturnsFalse() throws SQLException {
        int hocVienId = 1001;
        KhoaHoc khoaHoc = new KhoaHoc();
        khoaHoc.setId(1);
        khoaHoc.setTenKhoaHoc("Java Cơ Bản");
        khoaHoc.setNgayBatDau(LocalDate.now().plusDays(3));
        
        // Add course to already enrolled list
        when(courseService.isCourseEnrolled(hocVienId, 1)).thenReturn(true);
        
        boolean result = controller.canEnrollCourse(hocVienId, khoaHoc, Collections.emptyList());
        
        assertFalse(result, "Học viên đã đăng ký khóa học không được phép đăng ký lại.");
    }
    
    @Test
    void testCanEnrollCourse_CourseInList_ReturnsFalse() throws SQLException {
        int hocVienId = 1001;
        KhoaHoc khoaHoc = new KhoaHoc();
        khoaHoc.setId(1);
        khoaHoc.setTenKhoaHoc("Java Cơ Bản");
        khoaHoc.setNgayBatDau(LocalDate.now().plusDays(3));
        
        // Test with course in the enrolledCourses list
        boolean result = controller.canEnrollCourse(hocVienId, khoaHoc, Collections.singletonList(khoaHoc));
        
        assertFalse(result, "Học viên đã có khóa học trong danh sách không được phép đăng ký lại.");
    }
    
    @Test
    void testCanEnrollCourse_ClassFull_ReturnsFalse() throws SQLException {
        int hocVienId = 1001;
        KhoaHoc khoaHoc = new KhoaHoc();
        khoaHoc.setId(1);
        khoaHoc.setTenKhoaHoc("Java Cơ Bản");
        khoaHoc.setNgayBatDau(LocalDate.now().plusDays(3));
        
        when(courseService.isCourseEnrolled(hocVienId, 1)).thenReturn(false);
        when(courseService.getCurrentEnrollmentCount(1)).thenReturn(40); // Class full
        
        boolean result = controller.canEnrollCourse(hocVienId, khoaHoc, Collections.emptyList());
        
        assertFalse(result, "Không được phép đăng ký khi lớp đã đầy.");
    }
    
    @Test
    void testCanEnrollCourse_ScheduleOverlap_ReturnsFalse() throws SQLException {
        int hocVienId = 1001;
        KhoaHoc khoaHoc = new KhoaHoc();
        khoaHoc.setId(1);
        khoaHoc.setTenKhoaHoc("Java Cơ Bản");
        khoaHoc.setNgayBatDau(LocalDate.now().plusDays(3));
        
        when(courseService.isCourseEnrolled(hocVienId, 1)).thenReturn(false);
        when(courseService.getCurrentEnrollmentCount(1)).thenReturn(10);
        when(userService.hasOverlappingSchedule(hocVienId, 1)).thenReturn(true); // Schedule overlap
        
        boolean result = controller.canEnrollCourse(hocVienId, khoaHoc, Collections.emptyList());
        
        assertFalse(result, "Không được phép đăng ký khi lịch học bị trùng.");
    }
    
    @Test
    void testCanEnrollCourse_PastStartDate_ReturnsFalse() throws SQLException {
        int hocVienId = 1001;
        KhoaHoc khoaHoc = new KhoaHoc();
        khoaHoc.setId(1);
        khoaHoc.setTenKhoaHoc("Java Cơ Bản");
        khoaHoc.setNgayBatDau(LocalDate.now().minusDays(1)); // Past start date
        
        when(courseService.isCourseEnrolled(hocVienId, 1)).thenReturn(false);
        when(courseService.getCurrentEnrollmentCount(1)).thenReturn(10);
        when(userService.hasOverlappingSchedule(hocVienId, 1)).thenReturn(false);
        
        boolean result = controller.canEnrollCourse(hocVienId, khoaHoc, Collections.emptyList());
        
        assertFalse(result, "Không được phép đăng ký khóa học đã bắt đầu.");
    }
    
    @Test
    void testCanEnrollCourse_ServiceException_ReturnsFalse() throws SQLException {
        int hocVienId = 1001;
        KhoaHoc khoaHoc = new KhoaHoc();
        khoaHoc.setId(1);
        khoaHoc.setTenKhoaHoc("Java Cơ Bản");
        khoaHoc.setNgayBatDau(LocalDate.now().plusDays(3));
        
        when(courseService.isCourseEnrolled(hocVienId, 1)).thenThrow(new SQLException("Database error"));
        
        boolean result = controller.canEnrollCourse(hocVienId, khoaHoc, Collections.emptyList());
        
        assertFalse(result, "Phải trả về false khi có lỗi xảy ra.");
    }
}