import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.KhoaHocHocVien;
import com.ntn.quanlykhoahoc.services.EnrollmentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollmentTest {
    @InjectMocks
    private EnrollmentService enrollmentService;
    
    @Mock
    private Connection mockConn;
    @Mock
    private PreparedStatement mockStmt;
    @Mock
    private ResultSet mockRs;
    
    private MockedStatic<Database> databaseMock;
    
    @BeforeEach
    void setUp() throws Exception {
        // Mock phương thức tĩnh Database.getConn()
        databaseMock = Mockito.mockStatic(Database.class);
        databaseMock.when(Database::getConn).thenReturn(mockConn);
        
        // Chỉ mock prepareStatement, không mock executeQuery ở đây
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
    }
    
    @AfterEach
    void tearDown() {
        // Giải phóng mock tĩnh
        databaseMock.close();
    }
    
    /**
     * TC1: Kiểm tra lấy danh sách đăng ký khóa học đang chờ duyệt
     */
    @Test
    void testGetPendingKhoaHocHocVien_Success() throws SQLException {
        // Thiết lập mock cho executeQuery trong bài kiểm thử này
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        
        // Mock ResultSet: 3 bản ghi đăng ký đang chờ duyệt
        when(mockRs.next()).thenReturn(true, true, true, false);
        when(mockRs.getInt("id")).thenReturn(1, 2, 3);
        when(mockRs.getInt("hoc_vien_id")).thenReturn(101, 102, 103);
        when(mockRs.getInt("khoa_hoc_id")).thenReturn(201, 202, 203);
        when(mockRs.getString("ngay_dang_ky")).thenReturn(
            "2025-04-15 00:00:00", 
            "2025-04-16 00:00:00", 
            "2025-04-17 00:00:00"
        );
        when(mockRs.getString("trang_thai")).thenReturn("PENDING", "PENDING", "PENDING");

        // Gọi phương thức
        List<KhoaHocHocVien> pendingEnrollments = enrollmentService.getPendingKhoaHocHocVien();

        // Kiểm tra kết quả
        assertEquals(3, pendingEnrollments.size(), "Phải trả về đúng 3 đăng ký đang chờ duyệt");

        // Kiểm tra thông tin đăng ký đầu tiên
        KhoaHocHocVien firstEnrollment = pendingEnrollments.get(0);
        assertEquals(1, firstEnrollment.getId());
        assertEquals(101, firstEnrollment.getHocVienID());
        assertEquals(201, firstEnrollment.getKhoaHocID());
        assertEquals("2025-04-15 00:00:00", firstEnrollment.getNgayDangKy());
        assertEquals("PENDING", firstEnrollment.getTrangThai());

        // Kiểm tra thông tin đăng ký thứ hai
        KhoaHocHocVien secondEnrollment = pendingEnrollments.get(1);
        assertEquals(2, secondEnrollment.getId());
        assertEquals(102, secondEnrollment.getHocVienID());
        assertEquals(202, secondEnrollment.getKhoaHocID());
        assertEquals("2025-04-16 00:00:00", secondEnrollment.getNgayDangKy());
        assertEquals("PENDING", secondEnrollment.getTrangThai());

        // Kiểm tra thông tin đăng ký thứ ba
        KhoaHocHocVien thirdEnrollment = pendingEnrollments.get(2);
        assertEquals(3, thirdEnrollment.getId());
        assertEquals(103, thirdEnrollment.getHocVienID());
        assertEquals(203, thirdEnrollment.getKhoaHocID());
        assertEquals("2025-04-17 00:00:00", thirdEnrollment.getNgayDangKy());
        assertEquals("PENDING", thirdEnrollment.getTrangThai());

        // Xác minh SQL query được thực thi
        verify(mockStmt).executeQuery();
    }
    
    /**
     * TC2: Kiểm tra lấy danh sách đăng ký khóa học đang chờ duyệt khi không có đăng ký nào
     */
    @Test
    void testGetPendingKhoaHocHocVien_EmptyList() throws SQLException {
        // Thiết lập mock cho executeQuery trong bài kiểm thử này
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        
        // Mock ResultSet: không có bản ghi nào
        when(mockRs.next()).thenReturn(false);
        
        // Gọi phương thức
        List<KhoaHocHocVien> pendingEnrollments = enrollmentService.getPendingKhoaHocHocVien();
        
        // Kiểm tra kết quả
        assertTrue(pendingEnrollments.isEmpty(), "Danh sách đăng ký phải rỗng");
        
        // Xác minh SQL query được thực thi
        verify(mockStmt).executeQuery();
    }
    
    /**
     * TC3: Kiểm tra cập nhật trạng thái đăng ký khóa học thành công
     */
    @Test
    void testUpdateKhoaHocHocVienStatus_Success() throws SQLException {
        // Mock executeUpdate để trả về 1 hàng bị ảnh hưởng
        when(mockStmt.executeUpdate()).thenReturn(1);
        
        // Gọi phương thức
        int enrollmentId = 1;
        String newStatus = "APPROVED";
        enrollmentService.updateKhoaHocHocVienStatus(enrollmentId, newStatus);
        
        // Xác minh PreparedStatement được thiết lập đúng
        verify(mockStmt).setString(1, newStatus);
        verify(mockStmt).setInt(2, enrollmentId);
        verify(mockStmt).executeUpdate();
    }
    
    /**
     * TC4: Kiểm tra cập nhật trạng thái đăng ký khóa học thất bại - không tìm thấy ID
     */
    @Test
    void testUpdateKhoaHocHocVienStatus_NotFound() throws SQLException {
        // Mock executeUpdate để trả về 0 hàng bị ảnh hưởng (không tìm thấy bản ghi)
        when(mockStmt.executeUpdate()).thenReturn(0);
        
        // Gọi phương thức và xác minh ngoại lệ
        int nonExistentId = 999;
        String newStatus = "APPROVED";
        
        SQLException exception = assertThrows(SQLException.class, () -> {
            enrollmentService.updateKhoaHocHocVienStatus(nonExistentId, newStatus);
        });
        
        // Kiểm tra thông báo lỗi
        assertEquals("Không tìm thấy bản ghi đăng ký với ID: " + nonExistentId, exception.getMessage());
        
        // Xác minh PreparedStatement được thiết lập đúng
        verify(mockStmt).setString(1, newStatus);
        verify(mockStmt).setInt(2, nonExistentId);
        verify(mockStmt).executeUpdate();
    }
    
    /**
     * TC5: Kiểm tra xử lý ngoại lệ khi cập nhật trạng thái đăng ký thất bại
     */
    @Test
    void testUpdateKhoaHocHocVienStatus_SQLException() throws SQLException {
        // Mock SQLException khi executeUpdate
        when(mockStmt.executeUpdate()).thenThrow(new SQLException("Database error"));
        
        // Gọi phương thức và xác minh ngoại lệ
        int enrollmentId = 1;
        String newStatus = "APPROVED";
        
        SQLException exception = assertThrows(SQLException.class, () -> {
            enrollmentService.updateKhoaHocHocVienStatus(enrollmentId, newStatus);
        });
        
        // Kiểm tra thông báo lỗi
        assertEquals("Database error", exception.getMessage());
        
        // Xác minh PreparedStatement được thiết lập đúng
        verify(mockStmt).setString(1, newStatus);
        verify(mockStmt).setInt(2, enrollmentId);
    }
}