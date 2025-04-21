/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.vmct.testcases.CoursesTest;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.KhoaHoc;
import com.ntn.quanlykhoahoc.services.CourseService;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;

@ExtendWith(MockitoExtension.class)
public class UseCoursesTest {

    @InjectMocks
    private CourseService courseService;

    @Mock
    private Connection mockConn;
    @Mock
    private PreparedStatement mockStmt;
    @Mock
    private ResultSet mockRs;

    private MockedStatic<Database> databaseMock;

    @BeforeEach
    void setUpGiangVien() throws SQLException {
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO nguoidung (id, ho, ten, email, mat_khau, loai_nguoi_dung_id) VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, 100); // giả lập giảng viên ID = 1
            stmt.setString(2, "Nguyen");
            stmt.setString(3, "Van A");
            stmt.setString(4, "nguyenvana@gmail.com");
            stmt.setString(5, "Abc@1234");
            stmt.setInt(6, 2);
            stmt.executeUpdate();
        }
    }

    @AfterEach
    void deleteGiangVien() throws SQLException {
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM `quanlykhoahoc`.`nguoidung` WHERE (`id` = '100');")) {
            stmt.executeUpdate();
        }
    }

    @Test
    void testAddCourse_validInput() throws SQLException {
        CourseService service = new CourseService();
        boolean result = service.addCourseWithImage(
                "Khóa học Spring Boot",
                100,
                "Dành cho sinh viên năm 3",
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 6, 1),
                1200.0,
                "springboot.png",
                true
        );
        assertTrue(result);
    }

    @Test
    void testAddCourse_invalidName() throws SQLException {
        CourseService service = new CourseService();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.addCourse(
                    "",
                    100,
                    "Dành cho sinh viên năm 3",
                    LocalDate.of(2025, 5, 1),
                    LocalDate.of(2025, 6, 1),
                    1200.0
            );
        });
        assertEquals("Tên khóa học không được để trống", exception.getMessage());
    }

    @Test
    void testAddCourse_invalidScript() throws SQLException {
        CourseService service = new CourseService();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.addCourse(
                    "Khóa học Spring Boot",
                    100,
                    "",
                    LocalDate.of(2025, 5, 1),
                    LocalDate.of(2025, 6, 1),
                    1200.0
            );
        });
        assertEquals("Mô tả không được để trống", exception.getMessage());
    }

    @Test
    void testAddCourse_invalidDate() throws SQLException {
        CourseService service = new CourseService();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.addCourse(
                    "Khóa học Spring Boot",
                    100,
                    "Dành cho sinh viên năm 3",
                    null,
                    LocalDate.of(2025, 6, 1),
                    1200.0
            );
        });
        assertEquals("Ngày bắt đầu và ngày kết thúc không được để trống", exception.getMessage());
    }

    @Test
    void testAddCourse_invalidEndDate() throws SQLException {
        CourseService service = new CourseService();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.addCourse(
                    "Khóa học Spring Boot",
                    100,
                    "Dành cho sinh viên năm 3",
                    LocalDate.of(2025, 6, 1),
                    LocalDate.of(2025, 5, 1),
                    1200.0
            );
        });
        assertEquals("Ngày kết thúc phải sau ngày bắt đầu", exception.getMessage());
    }

    @Test
    void testAddCourse_invalidFee() throws SQLException {
        CourseService service = new CourseService();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.addCourse(
                    "Khóa học Spring Boot",
                    100,
                    "Dành cho sinh viên năm 3",
                    LocalDate.of(2025, 5, 1),
                    LocalDate.of(2025, 6, 1),
                    -0.1
            );
        });
        assertEquals("Học phí không được âm", exception.getMessage());
    }

    @Test
    void testAddCourse_invalidTeacher() throws SQLException {
        CourseService service = new CourseService();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.addCourse(
                    "Khóa học Spring Boot",
                    101,
                    "Dành cho sinh viên năm 3",
                    LocalDate.of(2025, 5, 1),
                    LocalDate.of(2025, 6, 1),
                    1200.0
            );
        });
        assertEquals("Giảng viên không tồn tại hoặc không hợp lệ", exception.getMessage());
    }

    @Test
    void testAddCourseWithImage_SQLException() throws SQLException {
        CourseService mockCourseService = Mockito.mock(CourseService.class);

        Mockito.doThrow(new SQLException("Lỗi khi thêm khóa học"))
               .when(mockCourseService).addCourseWithImage(
                   Mockito.anyString(),
                   Mockito.anyInt(),
                   Mockito.anyString(),
                   Mockito.any(LocalDate.class),
                   Mockito.any(LocalDate.class),
                   Mockito.anyDouble(),
                   Mockito.anyString(),
                   Mockito.anyBoolean()
               );

        SQLException ex = assertThrows(SQLException.class, () -> {
            mockCourseService.addCourseWithImage(
                "Khóa học Java",
                100,
                "Nội dung khóa học",
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 6, 1),
                1200.0,
                "java.png",
                true
            );
        });

        assertTrue(ex.getMessage().contains("Lỗi khi thêm khóa học"), "Thông báo lỗi không đúng");
    }
}
