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
        databaseMock.close(); // GIẢI PHÓNG STATIC MOCK SAU MỖI TEST
    }

    @Test
    void testGetAllActiveCourses_ReturnsCorrectList() throws SQLException {
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
                null // dòng 2 - toLocalDate -> tránh lỗi
        );
        AtomicInteger batDauIndex = new AtomicInteger(0);
        Mockito.when(mockRs.getDate("ngay_bat_dau")).thenAnswer(invocation -> ngayBatDauList.get(batDauIndex.getAndIncrement()));

        // Mock thông minh cho ngày kết thúc
        List<Date> ngayKetThucList = Arrays.asList(
                Date.valueOf("2024-06-01"), // dòng 1 - check null
                Date.valueOf("2024-06-01"), // dòng 1 - toLocalDate
                null, // dòng 2 - check null
                null // dòng 2 - toLocalDate
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
    void testGetAllCourses_ReturnsTrueList() throws SQLException {
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
                null // dòng 2 - toLocalDate -> tránh lỗi
        );
        AtomicInteger batDauIndex = new AtomicInteger(0);
        Mockito.when(mockRs.getDate("ngay_bat_dau")).thenAnswer(invocation -> ngayBatDauList.get(batDauIndex.getAndIncrement()));

        // Mock thông minh cho ngày kết thúc
        List<Date> ngayKetThucList = Arrays.asList(
                Date.valueOf("2024-06-01"), // dòng 1 - check null
                Date.valueOf("2024-06-01"), // dòng 1 - toLocalDate
                null, // dòng 2 - check null
                null // dòng 2 - toLocalDate
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
    void testGetCourseById_ReturnsTrue() throws SQLException {
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
    void testGetCourseById_NotFound() throws SQLException {
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

    @Test
    public void testGetNextImageNumber_ReturnsCorrectValue() throws Exception {
        Mockito.when(Database.getConn()).thenReturn(mockConn); // giả lập kết nối
        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockStmt);
        Mockito.when(mockStmt.executeQuery()).thenReturn(mockRs);
        Mockito.when(mockRs.next()).thenReturn(true);
        Mockito.when(mockRs.getInt(1)).thenReturn(5); // giả lập có 5 bản ghi

        int result = courseService.getNextImageNumber();
        assertEquals(6, result);
    }

    @Test
    public void testGetNextImageNumber_ThrowsSQLException() throws SQLException {
        // Giả lập lỗi kết nối hoặc truy vấn SQL
        Mockito.when(Database.getConn()).thenThrow(new SQLException("Lỗi giả lập"));

        SQLException thrown = assertThrows(SQLException.class, () -> {
            courseService.getNextImageNumber();
        });

        assertTrue(thrown.getMessage().contains("Lỗi khi lấy số thứ tự ảnh"));
    }

    @Test
    public void testSearchCourses_ReturnsCorrectList() throws SQLException {
        // Giả lập prepareStatement và executeQuery
        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockStmt);
        Mockito.when(mockStmt.executeQuery()).thenReturn(mockRs);

        // Đếm dòng hiện tại trong ResultSet
        AtomicInteger index = new AtomicInteger(0);

        // Mô phỏng next() trả về true cho 2 dòng, sau đó false
        Mockito.when(mockRs.next()).thenAnswer(inv -> {
            int i = index.get();
            if (i < 2) {
                index.incrementAndGet();
                return true;
            }
            return false;
        });

        // Dữ liệu mẫu cho 2 dòng
        int[] ids = {1, 2};
        String[] tenKH = {"Lập trình C++", "Thiết kế Web"};
        String[] moTa = {"Học C++ từ cơ bản", "Học HTML/CSS"};
        double[] gia = {150.0, 250.0};
        int[] slHV = {20, 35};
        String[] hinhAnh = {"c++.jpg", "web.jpg"};
        boolean[] active = {true, true};
        String[] tenGV = {null, "Lê Văn B"};
        LocalDate[] ngayBD = {LocalDate.of(2024, 1, 10), LocalDate.of(2024, 3, 15)};
        LocalDate[] ngayKT = {LocalDate.of(2024, 4, 10), LocalDate.of(2024, 6, 30)};

        // Mô phỏng các giá trị theo chỉ số dòng
        Mockito.when(mockRs.getInt("id")).thenAnswer(inv -> ids[index.get() - 1]);
        Mockito.when(mockRs.getString("ten_khoa_hoc")).thenAnswer(inv -> tenKH[index.get() - 1]);
        Mockito.when(mockRs.getString("mo_ta")).thenAnswer(inv -> moTa[index.get() - 1]);
        Mockito.when(mockRs.getDouble("gia")).thenAnswer(inv -> gia[index.get() - 1]);
        Mockito.when(mockRs.getInt("so_luong_hoc_vien_toi_da")).thenAnswer(inv -> slHV[index.get() - 1]);
        Mockito.when(mockRs.getString("hinh_anh")).thenAnswer(inv -> hinhAnh[index.get() - 1]);
        Mockito.when(mockRs.getBoolean("active")).thenAnswer(inv -> active[index.get() - 1]);
        Mockito.when(mockRs.getString("ten_giang_vien")).thenAnswer(inv -> tenGV[index.get() - 1]);
        Mockito.when(mockRs.getDate("ngay_bat_dau")).thenAnswer(inv
                -> ngayBD[index.get() - 1] == null ? null : Date.valueOf(ngayBD[index.get() - 1]));
        Mockito.when(mockRs.getDate("ngay_ket_thuc")).thenAnswer(inv
                -> ngayKT[index.get() - 1] == null ? null : Date.valueOf(ngayKT[index.get() - 1]));

        // Gọi hàm tìm kiếm
        String kw = "lập trình";
        List<KhoaHoc> result = courseService.searchCourses(kw);

        // Kiểm tra kết quả
        assertEquals(2, result.size());

        // Dòng 1
        KhoaHoc kh1 = result.get(0);
        assertEquals("Lập trình C++", kh1.getTenKhoaHoc());
        assertEquals("Chưa có giảng viên", kh1.getTenGiangVien());
        assertEquals(LocalDate.of(2024, 1, 10), kh1.getNgayBatDau());
        assertEquals(LocalDate.of(2024, 4, 10), kh1.getNgayKetThuc());

        // Dòng 2
        KhoaHoc kh2 = result.get(1);
        assertEquals("Thiết kế Web", kh2.getTenKhoaHoc());
        assertEquals("Lê Văn B", kh2.getTenGiangVien());
        assertEquals(LocalDate.of(2024, 3, 15), kh2.getNgayBatDau());
        assertEquals(LocalDate.of(2024, 6, 30), kh2.getNgayKetThuc());
    }

    @Test
    void testSearchCourses_SQLException() throws SQLException {
        SQLException fakeError = new SQLException("Fake error when searching");

        Mockito.when(mockConn.prepareStatement(Mockito.anyString()))
                .thenThrow(fakeError);

        SQLException ex = assertThrows(SQLException.class, () -> {
            courseService.searchCourses("java");
        });

        assertAll(
                () -> assertTrue(ex.getMessage().contains("Lỗi khi tìm kiếm khóa học")),
                () -> assertTrue(ex.getMessage().contains("Fake error")),
                () -> assertEquals(fakeError, ex.getCause())
        );
    }
    
    @Test
    public void testSearchCourses_DOS() throws SQLException {
        // Giả lập prepareStatement và executeQuery
        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockStmt);
        Mockito.when(mockStmt.executeQuery()).thenReturn(mockRs);

        // Đếm dòng hiện tại trong ResultSet
        AtomicInteger index = new AtomicInteger(0);

        // Mô phỏng next() trả về true cho 2 dòng, sau đó false
        Mockito.when(mockRs.next()).thenAnswer(inv -> {
            int i = index.get();
            if (i < 2) {
                index.incrementAndGet();
                return true;
            }
            return false;
        });

        // Dữ liệu mẫu
        int[] ids = {1, 2};
        String[] tenKH = {"Lập trình C++", "Thiết kế Web"};
        String[] moTa = {"Học C++ từ cơ bản", "Học HTML/CSS"};
        double[] gia = {150.0, 250.0};
        int[] slHV = {20, 35};
        String[] hinhAnh = {"c++.jpg", "web.jpg"};
        boolean[] active = {true, true};
        String[] tenGV = {null, "Lê Văn B"};
        LocalDate[] ngayBD = {LocalDate.of(2024, 1, 10), LocalDate.of(2024, 3, 15)};
        LocalDate[] ngayKT = {LocalDate.of(2024, 4, 10), LocalDate.of(2024, 6, 30)};

        // Mô phỏng các giá trị trong ResultSet
        Mockito.when(mockRs.getInt("id")).thenAnswer(inv -> ids[index.get() - 1]);
        Mockito.when(mockRs.getString("ten_khoa_hoc")).thenAnswer(inv -> tenKH[index.get() - 1]);
        Mockito.when(mockRs.getString("mo_ta")).thenAnswer(inv -> moTa[index.get() - 1]);
        Mockito.when(mockRs.getDouble("gia")).thenAnswer(inv -> gia[index.get() - 1]);
        Mockito.when(mockRs.getInt("so_luong_hoc_vien_toi_da")).thenAnswer(inv -> slHV[index.get() - 1]);
        Mockito.when(mockRs.getString("hinh_anh")).thenAnswer(inv -> hinhAnh[index.get() - 1]);
        Mockito.when(mockRs.getBoolean("active")).thenAnswer(inv -> active[index.get() - 1]);
        Mockito.when(mockRs.getString("ten_giang_vien")).thenAnswer(inv -> tenGV[index.get() - 1]);
        Mockito.when(mockRs.getDate("ngay_bat_dau")).thenAnswer(inv -> Date.valueOf(ngayBD[index.get() - 1]));
        Mockito.when(mockRs.getDate("ngay_ket_thuc")).thenAnswer(inv -> Date.valueOf(ngayKT[index.get() - 1]));

        // Tạo chuỗi cực dài (khoảng 100.000 ký tự)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            sb.append("a");
        }
        String keyword = sb.toString();

        // Đo thời gian thực thi
        long startTime = System.currentTimeMillis();
        List<KhoaHoc> result = courseService.searchCourses(keyword);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Kiểm tra: hệ thống không bị treo quá 2 giây
        assertTrue(duration < 2000, "Truy vấn mất quá nhiều thời gian (DoS risk)!");

        // Đảm bảo kết quả trả về đúng
        assertNotNull(result);
    }

    @Test
    void testGetEnrolledCourses_ReturnsCorrectList() throws Exception {
        Mockito.when(Database.getConn()).thenReturn(mockConn);
        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockStmt);
        int hocVienId = 1;

        // Thiết lập ResultSet trả về 1 khóa học
        Mockito.when(mockStmt.executeQuery()).thenReturn(mockRs);

        Mockito.when(mockRs.next()).thenReturn(true).thenReturn(false); // 1 row
        Mockito.when(mockRs.getInt("id")).thenReturn(101);
        Mockito.when(mockRs.getString("ten_khoa_hoc")).thenReturn("Lập trình Java");
        Mockito.when(mockRs.getString("mo_ta")).thenReturn("Mô tả khóa học");
        Mockito.when(mockRs.getDouble("gia")).thenReturn(1000000.0);
        Mockito.when(mockRs.getInt("so_luong_hoc_vien_toi_da")).thenReturn(20);
        Mockito.when(mockRs.getString("ten_giang_vien")).thenReturn("Nguyễn Văn A");
        Mockito.when(mockRs.getString("hinh_anh")).thenReturn("java.png");
        Mockito.when(mockRs.getBoolean("active")).thenReturn(true);
        Mockito.when(mockRs.getDate("ngay_bat_dau")).thenReturn(Date.valueOf("2025-05-01"));
        Mockito.when(mockRs.getDate("ngay_ket_thuc")).thenReturn(Date.valueOf("2025-06-01"));

        List<KhoaHoc> result = courseService.getEnrolledCourses(hocVienId);

        // Kiểm tra kết quả
        Assertions.assertEquals(1, result.size());
        KhoaHoc course = result.get(0);
        Assertions.assertEquals(101, course.getId());
        Assertions.assertEquals("Lập trình Java", course.getTenKhoaHoc());
        Assertions.assertEquals("Nguyễn Văn A", course.getTenGiangVien());
    }

    @Test
    void testGetEnrolledCourses_ThrowsSQLException() throws SQLException {
        Mockito.when(Database.getConn()).thenReturn(mockConn);
        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockStmt);
        int hocVienId = 1;

        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenThrow(new SQLException("Lỗi kết nối DB"));

        SQLException thrown = Assertions.assertThrows(
                SQLException.class,
                () -> courseService.getEnrolledCourses(hocVienId));
        Assertions.assertTrue(thrown.getMessage().contains("Lỗi khi lấy danh sách khóa học đã đăng ký"));
    }

    @Test
    void testIsCourseEnrolled_ReturnsTrue() throws SQLException {
        // Mock prepareStatement
        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockStmt);

        // Mock ResultSet trả về 1 dòng với giá trị > 0
        Mockito.when(mockStmt.executeQuery()).thenReturn(mockRs);
        Mockito.when(mockRs.next()).thenReturn(true);
        Mockito.when(mockRs.getInt(1)).thenReturn(1);

        boolean result = courseService.isCourseEnrolled(1, 2);
        assertTrue(result);
    }

    @Test
    void testIsCourseEnrolled_ThrowsSQLException() throws SQLException {
        databaseMock.when(Database::getConn).thenThrow(new SQLException("Kết nối thất bại"));

        SQLException ex = assertThrows(SQLException.class, () -> {
            courseService.isCourseEnrolled(1, 2);
        });

        assertTrue(ex.getMessage().contains("Lỗi khi kiểm tra trạng thái đăng ký"));
    }

    @Test
    void testGetCurrentEnrollmentCount_ReturnsCorrectCount() throws SQLException {
        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockStmt);
        Mockito.when(mockStmt.executeQuery()).thenReturn(mockRs);
        Mockito.when(mockRs.next()).thenReturn(true);
        Mockito.when(mockRs.getInt(1)).thenReturn(5);  // Giả lập có 5 học viên

        int result = courseService.getCurrentEnrollmentCount(1);
        assertEquals(5, result);
    }

    @Test
    void testGetCurrentEnrollmentCount_ThrowsSQLException() throws SQLException {
        databaseMock.when(Database::getConn).thenThrow(new SQLException("Kết nối thất bại"));

        SQLException ex = assertThrows(SQLException.class, () -> {
            courseService.getCurrentEnrollmentCount(1);
        });

        assertTrue(ex.getMessage().contains("Lỗi khi lấy số lượng học viên hiện tại"));
    }
    
    @Test
    void testEnrollCourse_Success() throws SQLException {
        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockStmt);
        Mockito.doNothing().when(mockStmt).setInt(Mockito.anyInt(), Mockito.anyInt());
        Mockito.doNothing().when(mockStmt).setTimestamp(Mockito.anyInt(), Mockito.any(Timestamp.class));
        Mockito.doNothing().when(mockStmt).setString(Mockito.anyInt(), Mockito.anyString());
        Mockito.when(mockStmt.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> courseService.enrollCourse(1, 100));
    }
    
    @Test
    void testEnrollCourse_ThrowsSQLException() throws SQLException {
        databaseMock.when(Database::getConn).thenThrow(new SQLException("Không thể kết nối CSDL"));

        SQLException ex = assertThrows(SQLException.class, () -> {
            courseService.enrollCourse(1, 100);
        });

        assertTrue(ex.getMessage().contains("Lỗi khi đăng ký khóa học"), "Thông báo lỗi không đúng");
    }
}