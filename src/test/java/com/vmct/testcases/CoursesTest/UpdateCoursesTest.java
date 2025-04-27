/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.vmct.testcases.CoursesTest;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.services.CourseService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)

public class UpdateCoursesTest {
    
    @InjectMocks
    private CourseService courseService;

    @Mock
    private Connection mockConn;
    @Mock
    private PreparedStatement mockStmt;
    @Mock
    private ResultSet mockRs;

    private MockedStatic<Database> databaseMock;

    @Test
    void testUpdateCourse_validInput() throws SQLException {
        CourseService service = new CourseService();
        boolean result = service.updateCourse(
            1,
            "Khóa học Java",
            3,
            "Mô tả",
            LocalDate.of(2025, 5, 1),
            LocalDate.of(2025, 6, 1),
            1000.0,
            "image.jpg",
            true
        );
        assertTrue(result);
    }
    
    @Test
    void testUpdateCourse_invalidId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.updateCourse(
                0,
                "Khóa học Java",
                1,
                "Mô tả",
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 6, 1),
                1000.0,
                "image.jpg",
                true
            );
        });
        assertEquals("ID khóa học không hợp lệ", exception.getMessage());
    }
    
    @Test
    void testUpdateCourse_invalidName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.updateCourse(
                1,
                "   ",
                1,
                "Mô tả",
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 6, 1),
                1000.0,
                "image.jpg",
                true
            );
        });
        assertEquals("Tên khóa học không được để trống", exception.getMessage());
    }
    
    @Test
    void testUpdateCourse_invalidScript() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.updateCourse(
                1,
                "Khóa học Java",
                1,
                "",
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 6, 1),
                1000.0,
                "image.jpg",
                true
            );
        });
        assertEquals("Mô tả không được để trống", exception.getMessage());
    }
    
    @Test
    void testUpdateCourse_invalidDate() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.updateCourse(
                1,
                "Khóa học Java",
                1,
                "Mô tả",
                LocalDate.of(2025, 5, 1),
                null,
                1000.0,
                "image.jpg",
                true
            );
        });
        assertEquals("Ngày bắt đầu và ngày kết thúc không được để trống", exception.getMessage());
    }
    
    @Test
    void testUpdateCourse_invalidEndDate() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.updateCourse(
                1,
                "Khóa học Java",
                1,
                "Mô tả",
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 5, 1),
                1000.0,
                "image.jpg",
                true
            );
        });
        assertEquals("Ngày kết thúc phải sau ngày bắt đầu", exception.getMessage());
    }
    
    @Test
    void testUpdateCourse_invalidFee() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.updateCourse(
                1,
                "Khóa học Java",
                1,
                "Mô tả",
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 6, 1),
                -0.1,
                "image.jpg",
                true
            );
        });
        assertEquals("Học phí không được âm", exception.getMessage());
    }
    
    @Test
    void testUpdateCourse_invalidTeacher() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.updateCourse(
                1,
                "Khóa học Java",
                0,
                "Mô tả",
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 6, 1),
                1000.0,
                "image.jpg",
                true
            );
        });
        assertEquals("Giảng viên không tồn tại hoặc không hợp lệ", exception.getMessage());
    }
    
   @Test
    void testUpdateCourse_SQLException() throws SQLException {
        // Giả lập Database.getConn() trả về mockConn
        databaseMock = Mockito.mockStatic(Database.class);
        databaseMock.when(Database::getConn).thenReturn(mockConn);

        // Giả lập mockConn.prepareStatement(...) ném SQLException
        Mockito.when(mockConn.prepareStatement(Mockito.anyString()))
               .thenThrow(new SQLException("Lỗi khi cập nhật khóa học"));

        // Gọi thực tế hàm courseService.updateCourse và kiểm tra exception
        SQLException ex = assertThrows(SQLException.class, () -> {
            courseService.updateCourse(
                1,
                "Java nâng cao",
                3,
                "Mô tả khóa học",
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 6, 1),
                1500.0,
                "java.png",
                true
            );
        });

        assertTrue(ex.getMessage().contains("Lỗi khi cập nhật khóa học"), "Thông báo lỗi không đúng");

        // Đóng static mock sau khi xong
        databaseMock.close();
    }
}
