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
public class GetCoursesTest {
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
    void setUp() throws SQLException {
        databaseMock = Mockito.mockStatic(Database.class);
        databaseMock.when(Database::getConn).thenReturn(mockConn);
    }

    @AfterEach
    void tearDown() {
        databaseMock.close(); // 👈 GIẢI PHÓNG STATIC MOCK SAU MỖI TEST
    }

    @Test
    void testGetAllActiveCourses_phuDDCS() throws SQLException {
        // Mock prepareStatement và executeQuery
        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockStmt);
        Mockito.when(mockStmt.executeQuery()).thenReturn(mockRs);
        Mockito.when(mockRs.next()).thenReturn(true, true, false); // 2 dòng dữ liệu

        // Mock dữ liệu chung
        Mockito.when(mockRs.getInt("id")).thenReturn(1, 2);
        Mockito.when(mockRs.getString("ten_khoa_hoc")).thenReturn("Java 101", "Python 101");
        Mockito.when(mockRs.getString("mo_ta")).thenReturn("Khóa học Java", "Khóa học Python");
        Mockito.when(mockRs.getDouble("gia")).thenReturn(100.0, 200.0);
        Mockito.when(mockRs.getString("hinh_anh")).thenReturn("java.jpg", "python.jpg");
        Mockito.when(mockRs.getString("ten_giang_vien")).thenReturn(null, "Nguyễn Văn A");
        Mockito.when(mockRs.getBoolean("active")).thenReturn(true, true);

        // Mock thông minh cho ngày bắt đầu
        List<Date> ngayBatDauList = Arrays.asList(
            Date.valueOf("2024-01-01"), // dòng 1 - check null
            Date.valueOf("2024-01-01"), // dòng 1 - toLocalDate
            null, // dòng 2 - check null
            null  // dòng 2 - toLocalDate -> tránh lỗi
        );
        AtomicInteger batDauIndex = new AtomicInteger(0);
        Mockito.when(mockRs.getDate("ngay_bat_dau")).thenAnswer(invocation -> ngayBatDauList.get(batDauIndex.getAndIncrement()));

        // Mock thông minh cho ngày kết thúc
        List<Date> ngayKetThucList = Arrays.asList(
            Date.valueOf("2024-06-01"), // dòng 1 - check null
            Date.valueOf("2024-06-01"), // dòng 1 - toLocalDate
            null, // dòng 2 - check null
            null  // dòng 2 - toLocalDate
        );
        AtomicInteger ketThucIndex = new AtomicInteger(0);
        Mockito.when(mockRs.getDate("ngay_ket_thuc")).thenAnswer(invocation -> ngayKetThucList.get(ketThucIndex.getAndIncrement()));

        // Gọi service
        List<KhoaHoc> result = courseService.getAllActiveCourses();

        // Kiểm tra kết quả
        assertEquals(2, result.size());

        // Dòng 1
        assertEquals("Chưa có giảng viên", result.get(0).getTenGiangVien());
        assertEquals(LocalDate.of(2024, 1, 1), result.get(0).getNgayBatDau());
        assertEquals(LocalDate.of(2024, 6, 1), result.get(0).getNgayKetThuc());

        // Dòng 2
        assertEquals("Nguyễn Văn A", result.get(1).getTenGiangVien());
        assertNull(result.get(1).getNgayBatDau());
        assertNull(result.get(1).getNgayKetThuc());
    }
    
    @Test
    void testGetAllActiveCourses_SQLExceptionMessage() throws SQLException {
        SQLException cause = new SQLException("Mock error");
        Mockito.when(mockConn.prepareStatement(Mockito.anyString()))
               .thenThrow(cause);

        SQLException ex = assertThrows(SQLException.class, courseService::getAllActiveCourses);
        assertAll(
            () -> assertTrue(ex.getMessage().contains("Lỗi khi lấy danh sách khóa học đang hoạt động")),
            () -> assertTrue(ex.getMessage().contains("Mock error")),
            () -> assertEquals(cause, ex.getCause())
        );
    }

    @Test
    void testGetAllCourses_phuDDCS() throws SQLException {
        // Mock prepareStatement và executeQuery
        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockStmt);
        Mockito.when(mockStmt.executeQuery()).thenReturn(mockRs);
        Mockito.when(mockRs.next()).thenReturn(true, true, false); // 2 dòng dữ liệu

        // Mock dữ liệu các cột chung
        Mockito.when(mockRs.getInt("id")).thenReturn(1, 2);
        Mockito.when(mockRs.getString("ten_khoa_hoc")).thenReturn("Java 101", "Python 101");
        Mockito.when(mockRs.getInt("giangVienID")).thenReturn(0, 5);
        Mockito.when(mockRs.getString("mo_ta")).thenReturn("Khóa học Java", "Khóa học Python");
        Mockito.when(mockRs.getDouble("gia")).thenReturn(100.0, 200.0);
        Mockito.when(mockRs.getInt("so_luong_hoc_vien_toi_da")).thenReturn(30, 25);
        Mockito.when(mockRs.getString("hinh_anh")).thenReturn("java.jpg", "python.jpg");
        Mockito.when(mockRs.getString("ten_giang_vien")).thenReturn(null, "Nguyễn Văn A");
        Mockito.when(mockRs.getBoolean("active")).thenReturn(true, false);

        // Mock thông minh cho ngày bắt đầu
        List<Date> ngayBatDauList = Arrays.asList(
            Date.valueOf("2024-01-01"), // dòng 1 - check null
            Date.valueOf("2024-01-01"), // dòng 1 - toLocalDate
            null, // dòng 2 - check null
            null  // dòng 2 - toLocalDate -> tránh lỗi
        );
        AtomicInteger batDauIndex = new AtomicInteger(0);
        Mockito.when(mockRs.getDate("ngay_bat_dau")).thenAnswer(invocation -> ngayBatDauList.get(batDauIndex.getAndIncrement()));

        // Mock thông minh cho ngày kết thúc
        List<Date> ngayKetThucList = Arrays.asList(
            Date.valueOf("2024-06-01"), // dòng 1 - check null
            Date.valueOf("2024-06-01"), // dòng 1 - toLocalDate
            null, // dòng 2 - check null
            null  // dòng 2 - toLocalDate
        );
        AtomicInteger ketThucIndex = new AtomicInteger(0);
        Mockito.when(mockRs.getDate("ngay_ket_thuc")).thenAnswer(invocation -> ngayKetThucList.get(ketThucIndex.getAndIncrement()));

        // Gọi service
        List<KhoaHoc> result = courseService.getAllCourses();

        // Kiểm tra kết quả
        assertEquals(2, result.size());

        // Dòng 1
        KhoaHoc kh1 = result.get(0);
        assertEquals("Chưa có giảng viên", kh1.getTenGiangVien());
        assertEquals(LocalDate.of(2024, 1, 1), kh1.getNgayBatDau());
        assertEquals(LocalDate.of(2024, 6, 1), kh1.getNgayKetThuc());
        assertTrue(kh1.isActive());
        // Dòng 2
        KhoaHoc kh2 = result.get(1);
        assertEquals("Nguyễn Văn A", kh2.getTenGiangVien());
        assertNull(kh2.getNgayBatDau());
        assertNull(kh2.getNgayKetThuc());
        assertFalse(kh2.isActive());
    }
  
    @Test
    void testGetAllCourses_SQLExceptionMessage() throws SQLException {
        SQLException fakeCause = new SQLException("Fake inner error");
        Mockito.when(mockConn.prepareStatement(Mockito.anyString()))
               .thenThrow(fakeCause);

        SQLException ex = assertThrows(SQLException.class, courseService::getAllCourses);
        assertAll(
            () -> assertTrue(ex.getMessage().contains("Lỗi khi lấy danh sách tất cả khóa học")),
            () -> assertTrue(ex.getMessage().contains("Fake inner error")),
            () -> assertEquals(fakeCause, ex.getCause())    
        );
    }
    
    @Test
    void testGetCourseById_phuDDCS() throws SQLException {
        int courseId = 1;

        // Mock prepareStatement và setInt
        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockStmt);

        // Không cần mock setInt nếu không kiểm tra verify, nhưng có thể thêm:
        Mockito.doNothing().when(mockStmt).setInt(Mockito.eq(1), Mockito.eq(courseId));

        // Mock executeQuery
        Mockito.when(mockStmt.executeQuery()).thenReturn(mockRs);
        Mockito.when(mockRs.next()).thenReturn(true); // Có 1 dòng kết quả

        // Mock dữ liệu
        Mockito.when(mockRs.getInt("id")).thenReturn(1);
        Mockito.when(mockRs.getString("ten_khoa_hoc")).thenReturn("Java 101");
        Mockito.when(mockRs.getInt("giangVienID")).thenReturn(0);
        Mockito.when(mockRs.getString("mo_ta")).thenReturn("Khóa học Java");
        Mockito.when(mockRs.getDouble("gia")).thenReturn(100.0);
        Mockito.when(mockRs.getInt("so_luong_hoc_vien_toi_da")).thenReturn(30);
        Mockito.when(mockRs.getString("hinh_anh")).thenReturn("java.jpg");
        Mockito.when(mockRs.getString("ten_giang_vien")).thenReturn("Nguyễn Văn A");
        Mockito.when(mockRs.getBoolean("active")).thenReturn(true);

        // Mock ngày bắt đầu/kết thúc
        Mockito.when(mockRs.getDate("ngay_bat_dau")).thenReturn(Date.valueOf("2024-01-01"));
        Mockito.when(mockRs.getDate("ngay_ket_thuc")).thenReturn(Date.valueOf("2024-06-01"));

        // Gọi hàm service
        KhoaHoc result = courseService.getCourseById(courseId);

        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals("Nguyễn Văn A", result.getTenGiangVien());
        assertEquals(LocalDate.of(2024, 1, 1), result.getNgayBatDau());
        assertEquals(LocalDate.of(2024, 6, 1), result.getNgayKetThuc());
    }
    
    @Test
    void testGetCourseById_phuDDCS_nhanhNull() throws SQLException {
        int courseId = 1;

        // Mock prepareStatement và setInt
        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockStmt);

        // Không cần mock setInt nếu không kiểm tra verify, nhưng có thể thêm:
        Mockito.doNothing().when(mockStmt).setInt(Mockito.eq(1), Mockito.eq(courseId));

        // Mock executeQuery
        Mockito.when(mockStmt.executeQuery()).thenReturn(mockRs);
        Mockito.when(mockRs.next()).thenReturn(true); // Có 1 dòng kết quả

        // Mock dữ liệu
        Mockito.when(mockRs.getInt("id")).thenReturn(1);
        Mockito.when(mockRs.getString("ten_khoa_hoc")).thenReturn("Java 101");
        Mockito.when(mockRs.getInt("giangVienID")).thenReturn(0);
        Mockito.when(mockRs.getString("mo_ta")).thenReturn("Khóa học Java");
        Mockito.when(mockRs.getDouble("gia")).thenReturn(100.0);
        Mockito.when(mockRs.getInt("so_luong_hoc_vien_toi_da")).thenReturn(30);
        Mockito.when(mockRs.getString("hinh_anh")).thenReturn("java.jpg");
        Mockito.when(mockRs.getString("ten_giang_vien")).thenReturn(null);
        Mockito.when(mockRs.getBoolean("active")).thenReturn(true);

        // Mock ngày bắt đầu/kết thúc
        Mockito.when(mockRs.getDate("ngay_bat_dau")).thenReturn(null);
        Mockito.when(mockRs.getDate("ngay_ket_thuc")).thenReturn(null);

        // Gọi hàm service
        KhoaHoc result = courseService.getCourseById(courseId);

        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals("Chưa có giảng viên", result.getTenGiangVien());
        assertNull(result.getNgayBatDau());
        assertNull(result.getNgayKetThuc());
    }
    
    @Test
    void testGetCourseById_notFound() throws SQLException {
        int courseId = 999;
        
        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockStmt);
        Mockito.doNothing().when(mockStmt).setInt(Mockito.eq(1), Mockito.eq(courseId));
        Mockito.when(mockStmt.executeQuery()).thenReturn(mockRs);
        Mockito.when(mockRs.next()).thenReturn(false);
        
        KhoaHoc result = courseService.getCourseById(courseId);
        assertNull(result);
    }
    
    @Test
    void testGetCourseById_SQLExceptionMessage() throws SQLException {
        int courseId = 123;
        SQLException cause = new SQLException("DB error");
        Mockito.when(mockConn.prepareStatement(Mockito.anyString()))
               .thenThrow(cause);

        SQLException ex = assertThrows(SQLException.class, () -> courseService.getCourseById(courseId));
        assertAll(
            () -> assertTrue(ex.getMessage().contains("Lỗi khi lấy thông tin khóa học ID " + courseId)),
            () -> assertTrue(ex.getMessage().contains("DB error")),
            () -> assertEquals(cause, ex.getCause())
        );
    }
}
