/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.vmct.testcases.CoursesTest;

import com.ntn.quanlykhoahoc.controllers.DashboardStudentController;
import com.ntn.quanlykhoahoc.pojo.KhoaHoc;
import com.ntn.quanlykhoahoc.services.CourseService;
import com.ntn.quanlykhoahoc.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DashboardStudentControllerTest {

    @Mock
    private CourseService courseService;

    @Mock
    private UserService userService;

    @InjectMocks
    private DashboardStudentController controller;

    private KhoaHoc khoaHoc;
    private List<KhoaHoc> enrolledCourses;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Khởi tạo dữ liệu kiểm tra
        enrolledCourses = new ArrayList<>();
        khoaHoc = new KhoaHoc();
        khoaHoc.setId(1);
        khoaHoc.setTenKhoaHoc("Java Cơ Bản");
        khoaHoc.setSoLuongHocVienToiDa(40);
        khoaHoc.setNgayBatDau(LocalDate.now().plusDays(10));

        // Sử dụng reflection để đặt userService thành mock
        Field userServiceField = DashboardStudentController.class.getDeclaredField("userService");
        userServiceField.setAccessible(true);
        userServiceField.set(controller, userService);

        // Sử dụng reflection để đặt courseService thành mock
        Field courseServiceField = DashboardStudentController.class.getDeclaredField("courseService");
        courseServiceField.setAccessible(true);
        courseServiceField.set(controller, courseService);
    }

    @Test
    public void testCanEnrollCourse_WhenCourseIsNotFull_ShouldReturnTrue() throws SQLException {
        int nguoiDungID = 8;
        int hocVienID = 1;

        // Thiết lập hành vi cho mock
        when(userService.getHocVienIDFromNguoiDung(nguoiDungID)).thenReturn(hocVienID);
        when(courseService.isCourseEnrolled(hocVienID, khoaHoc.getId())).thenReturn(false);
        when(courseService.getCurrentEnrollmentCount(khoaHoc.getId())).thenReturn(30);
        when(userService.hasOverlappingSchedule(hocVienID, khoaHoc.getId())).thenReturn(false);

        // Cập nhật ngày bắt đầu khóa học
        khoaHoc.setNgayBatDau(LocalDate.now().plusDays(5));

        // Gọi phương thức cần kiểm tra
        boolean result = controller.canEnrollCourse(nguoiDungID, khoaHoc, enrolledCourses);

        // Xác minh các tương tác
        verify(userService).getHocVienIDFromNguoiDung(nguoiDungID);
        verify(courseService).isCourseEnrolled(hocVienID, khoaHoc.getId());
        verify(courseService).getCurrentEnrollmentCount(khoaHoc.getId());
        verify(userService).hasOverlappingSchedule(hocVienID, khoaHoc.getId());

        // Kiểm tra kết quả
        assertTrue(result, "Khóa học nên được hiển thị vì chưa đầy học viên.");
    }
       @Test
    public void testCanEnrollCourse_WhenCourseIsFull() throws SQLException {
        // Giả lập các giá trị trả về từ các dịch vụ
        int nguoiDungID = 1;
        int hocVienID = 101;

        when(userService.getHocVienIDFromNguoiDung(nguoiDungID)).thenReturn(hocVienID);
        when(courseService.isCourseEnrolled(hocVienID, khoaHoc.getId())).thenReturn(false);
        when(courseService.getCurrentEnrollmentCount(khoaHoc.getId())).thenReturn(41); // Vượt quá giới hạn 40
        when(userService.hasOverlappingSchedule(hocVienID, khoaHoc.getId())).thenReturn(false);

        // Gỡ lỗi: Kiểm tra mock trước khi gọi phương thức
        System.out.println("Gỡ lỗi (Kiểm tra Full): getHocVienIDFromNguoiDung = " + userService.getHocVienIDFromNguoiDung(nguoiDungID));

        boolean result = controller.canEnrollCourse(nguoiDungID, khoaHoc, enrolledCourses);

        assertFalse(result, "Khóa học không nên được hiển thị vì đã đầy học viên.");
    }
}
