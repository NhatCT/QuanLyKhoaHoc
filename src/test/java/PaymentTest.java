import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.KhoaHocHocVien;
import com.ntn.quanlykhoahoc.pojo.ThanhToan;
import com.ntn.quanlykhoahoc.services.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PaymentTest {

    private PaymentService paymentService;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private Statement mockStatement;
    private LocalDateTime testDateTime;

    // Thiết lập các mock và dữ liệu giả lập trước mỗi kiểm thử
    @BeforeEach
    void setUp() throws SQLException {
        paymentService = new PaymentService();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockStatement = mock(Statement.class);
        testDateTime = LocalDateTime.now();

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSet.getInt(1)).thenReturn(1);
    }

    // Kiểm thử thêm bản ghi thanh toán mới
    @Test
    void testAddPayment() throws SQLException {
        try (MockedStatic<Database> mockedDatabase = Mockito.mockStatic(Database.class)) {
            mockedDatabase.when(Database::getConn).thenReturn(mockConnection);

            int result = paymentService.addPayment(1, 1, 100.0, testDateTime, "CREDIT_CARD");

            assertEquals(1, result);
            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).setInt(2, 1);
            verify(mockPreparedStatement).setDouble(3, 100.0);
            verify(mockPreparedStatement).setTimestamp(4, Timestamp.valueOf(testDateTime));
            verify(mockPreparedStatement).setString(5, "CREDIT_CARD");
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    // Kiểm thử thêm bản ghi vào khoahoc_hocvien thành công
    @Test
    void testAddToKhoaHocHocVien_Success() throws SQLException {
        PreparedStatement mockCheckStmt = mock(PreparedStatement.class);
        ResultSet mockCheckRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement("SELECT id FROM khoahoc_hocvien WHERE hocVienID = ? AND khoaHocID = ?"))
                .thenReturn(mockCheckStmt);
        when(mockCheckStmt.executeQuery()).thenReturn(mockCheckRs);
        when(mockCheckRs.next()).thenReturn(false);

        int result = paymentService.addToKhoaHocHocVien(mockConnection, 1, 1, testDateTime);

        assertEquals(1, result);
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).setInt(2, 1);
        verify(mockPreparedStatement).setTimestamp(3, Timestamp.valueOf(testDateTime));
        verify(mockPreparedStatement).setString(4, "PENDING");
        verify(mockPreparedStatement).executeUpdate();
    }

    // Kiểm thử thêm bản ghi vào khoahoc_hocvien khi đã tồn tại bản ghi trùng
    @Test
    void testAddToKhoaHocHocVien_DuplicateEntry() throws SQLException {
        PreparedStatement mockCheckStmt = mock(PreparedStatement.class);
        ResultSet mockCheckRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement("SELECT id FROM khoahoc_hocvien WHERE hocVienID = ? AND khoaHocID = ?"))
                .thenReturn(mockCheckStmt);
        when(mockCheckStmt.executeQuery()).thenReturn(mockCheckRs);
        when(mockCheckRs.next()).thenReturn(true);

        assertThrows(SQLException.class, () ->
                paymentService.addToKhoaHocHocVien(mockConnection, 1, 1, testDateTime)
        );
    }

    // Kiểm thử cập nhật bản ghi thanh toán thành công
    @Test
    void testUpdatePayment_Success() throws SQLException {
        try (MockedStatic<Database> mockedDatabase = Mockito.mockStatic(Database.class)) {
            mockedDatabase.when(Database::getConn).thenReturn(mockConnection);

            boolean result = paymentService.updatePayment(1, 1, 1, 200.0, testDateTime, "BANK_TRANSFER");

            assertTrue(result);
            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).setInt(2, 1);
            verify(mockPreparedStatement).setDouble(3, 200.0);
            verify(mockPreparedStatement).setTimestamp(4, Timestamp.valueOf(testDateTime));
            verify(mockPreparedStatement).setString(5, "BANK_TRANSFER");
            verify(mockPreparedStatement).setInt(6, 1);
        }
    }

    // Kiểm thử cập nhật bản ghi thanh toán với các tham số không hợp lệ
    @Test
    void testUpdatePayment_InvalidParameters() throws SQLException {
        boolean result = paymentService.updatePayment(0, 1, 1, 200.0, testDateTime, "BANK_TRANSFER");
        assertFalse(result);

        result = paymentService.updatePayment(1, 1, 1, 0, testDateTime, "BANK_TRANSFER");
        assertFalse(result);

        result = paymentService.updatePayment(1, 1, 1, 200.0, null, "BANK_TRANSFER");
        assertFalse(result);

        result = paymentService.updatePayment(1, 1, 1, 200.0, testDateTime, null);
        assertFalse(result);

        result = paymentService.updatePayment(1, 1, 1, 200.0, testDateTime, "");
        assertFalse(result);
    }

    // Kiểm thử xóa bản ghi thanh toán thành công
    @Test
    void testDeletePayment_Success() throws SQLException {
        try (MockedStatic<Database> mockedDatabase = Mockito.mockStatic(Database.class)) {
            mockedDatabase.when(Database::getConn).thenReturn(mockConnection);

            boolean result = paymentService.deletePayment("1");

            assertTrue(result);
            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    // Kiểm thử xóa bản ghi thanh toán với ID không hợp lệ
    @Test
    void testDeletePayment_InvalidId() throws SQLException {
        boolean result = paymentService.deletePayment("abc");
        assertFalse(result);
    }

    // Kiểm thử lấy danh sách tất cả bản ghi thanh toán
    @Test
    void testGetAllPayments() throws SQLException {
        try (MockedStatic<Database> mockedDatabase = Mockito.mockStatic(Database.class)) {
            mockedDatabase.when(Database::getConn).thenReturn(mockConnection);

            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            AtomicInteger rowIndex = new AtomicInteger(0);
            when(mockResultSet.next()).thenAnswer(invocation -> {
                int index = rowIndex.getAndIncrement();
                return index < 2;
            });

            when(mockResultSet.getInt("id")).thenAnswer(invocation -> rowIndex.get() == 1 ? 1 : 2);
            when(mockResultSet.getTimestamp("ngay_thanh_toan")).thenReturn(Timestamp.valueOf(testDateTime));
            when(mockResultSet.getDouble("so_tien")).thenAnswer(invocation -> rowIndex.get() == 1 ? 200000.0 : 455000.0);
            when(mockResultSet.getString("phuong_thuc")).thenAnswer(invocation -> rowIndex.get() == 1 ? "Tiền mặt" : "Chuyển khoản");
            when(mockResultSet.getString("hocVienID")).thenAnswer(invocation -> rowIndex.get() == 1 ? "1" : "2");
            when(mockResultSet.getString("khoaHocID")).thenAnswer(invocation -> rowIndex.get() == 1 ? "1" : "2");

            List<ThanhToan> payments = paymentService.getAllPayments();

            assertEquals(2, payments.size());
            assertEquals("1", payments.get(0).getThanhToanID());
            assertEquals("2", payments.get(1).getThanhToanID());
            assertEquals("200000.0", payments.get(0).getSoTien());
            assertEquals("455000.0", payments.get(1).getSoTien());
            assertEquals("Tiền mặt", payments.get(0).getPhuongThuc());
            assertEquals("Chuyển khoản", payments.get(1).getPhuongThuc());
        }
    }

    // Kiểm thử lấy bản ghi thanh toán theo ID khi tìm thấy
    @Test
    void testGetPaymentByTransactionId_Found() throws SQLException {
        try (MockedStatic<Database> mockedDatabase = Mockito.mockStatic(Database.class)) {
            mockedDatabase.when(Database::getConn).thenReturn(mockConnection);

            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("id")).thenReturn(1);
            when(mockResultSet.getTimestamp("ngay_thanh_toan")).thenReturn(Timestamp.valueOf(testDateTime));
            when(mockResultSet.getDouble("so_tien")).thenReturn(100.0);
            when(mockResultSet.getString("phuong_thuc")).thenReturn("CREDIT_CARD");
            when(mockResultSet.getString("hocVienID")).thenReturn("1");
            when(mockResultSet.getString("khoaHocID")).thenReturn("1");

            ThanhToan payment = paymentService.getPaymentByTransactionId(1);

            assertNotNull(payment);
            assertEquals("1", payment.getThanhToanID());
            assertEquals("100.0", payment.getSoTien());
            assertEquals("CREDIT_CARD", payment.getPhuongThuc());
            assertEquals("1", payment.getHocVienID());
            assertEquals("1", payment.getKhoaHocID());
        }
    }

    // Kiểm thử lấy bản ghi thanh toán theo ID khi không tìm thấy
    @Test
    void testGetPaymentByTransactionId_NotFound() throws SQLException {
        try (MockedStatic<Database> mockedDatabase = Mockito.mockStatic(Database.class)) {
            mockedDatabase.when(Database::getConn).thenReturn(mockConnection);

            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            ThanhToan payment = paymentService.getPaymentByTransactionId(1);

            assertNull(payment);
        }
    }

    // Kiểm thử xác thực học viên theo ID
    @Test
    void testIsValidHocVien() throws SQLException {
        try (MockedStatic<Database> mockedDatabase = Mockito.mockStatic(Database.class)) {
            mockedDatabase.when(Database::getConn).thenReturn(mockConnection);

            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true);
            boolean result = paymentService.isValidHocVien(1);
            assertTrue(result);

            when(mockResultSet.next()).thenReturn(false);
            result = paymentService.isValidHocVien(999);
            assertFalse(result);
        }
    }

    // Kiểm thử xác thực người dùng theo ID
    @Test
    void testIsValidNguoiDung() throws SQLException {
        try (MockedStatic<Database> mockedDatabase = Mockito.mockStatic(Database.class)) {
            mockedDatabase.when(Database::getConn).thenReturn(mockConnection);

            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true);
            boolean result = paymentService.isValidNguoiDung(1);
            assertTrue(result);

            when(mockResultSet.next()).thenReturn(false);
            result = paymentService.isValidNguoiDung(999);
            assertFalse(result);
        }
    }

    // Kiểm thử xác thực khóa học theo ID
    @Test
    void testIsValidCourse() throws SQLException {
        try (MockedStatic<Database> mockedDatabase = Mockito.mockStatic(Database.class)) {
            mockedDatabase.when(Database::getConn).thenReturn(mockConnection);

            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true);
            boolean result = paymentService.isValidCourse(1);
            assertTrue(result);

            when(mockResultSet.next()).thenReturn(false);
            result = paymentService.isValidCourse(999);
            assertFalse(result);
        }
    }

    // Kiểm thử cập nhật trạng thái khoahoc_hocvien thành công
    @Test
    void testUpdateKhoaHocHocVienStatus_Success() throws SQLException {
        try (MockedStatic<Database> mockedDatabase = Mockito.mockStatic(Database.class)) {
            mockedDatabase.when(Database::getConn).thenReturn(mockConnection);

            PreparedStatement mockSelectStmt = mock(PreparedStatement.class);
            ResultSet mockSelectRs = mock(ResultSet.class);
            when(mockConnection.prepareStatement("SELECT hocVienID, khoaHocID FROM khoahoc_hocvien WHERE id = ?"))
                    .thenReturn(mockSelectStmt);
            when(mockSelectStmt.executeQuery()).thenReturn(mockSelectRs);
            when(mockSelectRs.next()).thenReturn(true);
            when(mockSelectRs.getInt("hocVienID")).thenReturn(1);
            when(mockSelectRs.getInt("khoaHocID")).thenReturn(1);

            PreparedStatement mockUpdateStmt = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement("UPDATE khoahoc_hocvien SET trang_thai = ? WHERE id = ?"))
                    .thenReturn(mockUpdateStmt);
            when(mockUpdateStmt.executeUpdate()).thenReturn(1);

            boolean result = paymentService.updateKhoaHocHocVienStatus(1, "APPROVED");

            assertTrue(result);
            verify(mockSelectStmt).setInt(1, 1);
            verify(mockUpdateStmt).setString(1, "APPROVED");
            verify(mockUpdateStmt).setInt(2, 1);
            verify(mockConnection).setAutoCommit(false);
            verify(mockConnection).commit();
            verify(mockConnection).setAutoCommit(true);
        }
    }

    // Kiểm thử cập nhật trạng thái khoahoc_hocvien với tham số không hợp lệ
    @Test
    void testUpdateKhoaHocHocVienStatus_InvalidParameters() throws SQLException {
        boolean result = paymentService.updateKhoaHocHocVienStatus(0, "APPROVED");
        assertFalse(result);

        result = paymentService.updateKhoaHocHocVienStatus(1, null);
        assertFalse(result);

        result = paymentService.updateKhoaHocHocVienStatus(1, "INVALID_STATUS");
        assertFalse(result);
    }

    // Kiểm thử cập nhật trạng thái khoahoc_hocvien khi không tìm thấy bản ghi
    @Test
    void testUpdateKhoaHocHocVienStatus_RecordNotFound() throws SQLException {
        try (MockedStatic<Database> mockedDatabase = Mockito.mockStatic(Database.class)) {
            mockedDatabase.when(Database::getConn).thenReturn(mockConnection);

            PreparedStatement mockSelectStmt = mock(PreparedStatement.class);
            ResultSet mockSelectRs = mock(ResultSet.class);
            when(mockConnection.prepareStatement("SELECT hocVienID, khoaHocID FROM khoahoc_hocvien WHERE id = ?"))
                    .thenReturn(mockSelectStmt);
            when(mockSelectStmt.executeQuery()).thenReturn(mockSelectRs);
            when(mockSelectRs.next()).thenReturn(false);

            boolean result = paymentService.updateKhoaHocHocVienStatus(999, "APPROVED");

            assertFalse(result);
            verify(mockConnection).rollback();
        }
    }

    // Kiểm thử cập nhật trạng thái khoahoc_hocvien khi cập nhật thất bại
    @Test
    void testUpdateKhoaHocHocVienStatus_UpdateFailed() throws SQLException {
        try (MockedStatic<Database> mockedDatabase = Mockito.mockStatic(Database.class)) {
            mockedDatabase.when(Database::getConn).thenReturn(mockConnection);

            PreparedStatement mockSelectStmt = mock(PreparedStatement.class);
            ResultSet mockSelectRs = mock(ResultSet.class);
            when(mockConnection.prepareStatement("SELECT hocVienID, khoaHocID FROM khoahoc_hocvien WHERE id = ?"))
                    .thenReturn(mockSelectStmt);
            when(mockSelectStmt.executeQuery()).thenReturn(mockSelectRs);
            when(mockSelectRs.next()).thenReturn(true);
            when(mockSelectRs.getInt("hocVienID")).thenReturn(1);
            when(mockSelectRs.getInt("khoaHocID")).thenReturn(1);

            PreparedStatement mockUpdateStmt = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement("UPDATE khoahoc_hocvien SET trang_thai = ? WHERE id = ?"))
                    .thenReturn(mockUpdateStmt);
            when(mockUpdateStmt.executeUpdate()).thenReturn(0);

            boolean result = paymentService.updateKhoaHocHocVienStatus(1, "APPROVED");

            assertFalse(result);
            verify(mockConnection).rollback();
        }
    }

    // Kiểm thử cập nhật trạng thái khoahoc_hocvien khi từ chối thanh toán
    @Test
    void testUpdateKhoaHocHocVienStatus_RejectPayment() throws SQLException {
        try (MockedStatic<Database> mockedDatabase = Mockito.mockStatic(Database.class)) {
            mockedDatabase.when(Database::getConn).thenReturn(mockConnection);

            PreparedStatement mockSelectStmt = mock(PreparedStatement.class);
            ResultSet mockSelectRs = mock(ResultSet.class);
            when(mockConnection.prepareStatement("SELECT hocVienID, khoaHocID FROM khoahoc_hocvien WHERE id = ?"))
                    .thenReturn(mockSelectStmt);
            when(mockSelectStmt.executeQuery()).thenReturn(mockSelectRs);
            when(mockSelectRs.next()).thenReturn(true);
            when(mockSelectRs.getInt("hocVienID")).thenReturn(1);
            when(mockSelectRs.getInt("khoaHocID")).thenReturn(1);

            PreparedStatement mockUpdateStmt = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement("UPDATE khoahoc_hocvien SET trang_thai = ? WHERE id = ?"))
                    .thenReturn(mockUpdateStmt);
            when(mockUpdateStmt.executeUpdate()).thenReturn(1);

            PreparedStatement mockDeleteStmt = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement("DELETE FROM lichsu_thanhtoan WHERE hocVienID = ? AND khoaHocID = ?"))
                    .thenReturn(mockDeleteStmt);
            when(mockDeleteStmt.executeUpdate()).thenReturn(1);

            boolean result = paymentService.updateKhoaHocHocVienStatus(1, "REJECTED");

            assertTrue(result);
            verify(mockDeleteStmt).setInt(1, 1);
            verify(mockDeleteStmt).setInt(2, 1);
            verify(mockDeleteStmt).executeUpdate();
            verify(mockConnection).commit();
        }
    }

    // Kiểm thử lấy danh sách các bản ghi khoahoc_hocvien có trạng thái PENDING
    @Test
    void testGetPendingKhoaHocHocVien() throws SQLException {
        try (MockedStatic<Database> mockedDatabase = Mockito.mockStatic(Database.class)) {
            mockedDatabase.when(Database::getConn).thenReturn(mockConnection);

            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getInt("id")).thenReturn(1, 2);
            when(mockResultSet.getInt("hocVienID")).thenReturn(1, 2);
            when(mockResultSet.getInt("khoaHocID")).thenReturn(1, 2);
            when(mockResultSet.getTimestamp("ngay_dang_ky")).thenReturn(Timestamp.valueOf(testDateTime));
            when(mockResultSet.getString("trang_thai")).thenReturn("PENDING");

            List<KhoaHocHocVien> pendingRecords = paymentService.getPendingKhoaHocHocVien();

            assertEquals(2, pendingRecords.size());
            assertEquals(1, pendingRecords.get(0).getId());
            assertEquals(2, pendingRecords.get(1).getId());
            assertEquals("PENDING", pendingRecords.get(0).getTrangThai());
            assertEquals("PENDING", pendingRecords.get(1).getTrangThai());
        }
    }

    // Kiểm thử gửi thông báo cho quản trị viên
    @Test
    void testNotifyAdmin() throws SQLException {
        try (MockedStatic<Database> mockedDatabase = Mockito.mockStatic(Database.class)) {
            mockedDatabase.when(Database::getConn).thenReturn(mockConnection);

            PreparedStatement mockHocVienStmt = mock(PreparedStatement.class);
            ResultSet mockHocVienRs = mock(ResultSet.class);
            when(mockConnection.prepareStatement("SELECT n.ho, n.ten FROM hocvien h JOIN nguoidung n ON h.nguoiDungID = n.id WHERE h.id = ?"))
                    .thenReturn(mockHocVienStmt);
            when(mockHocVienStmt.executeQuery()).thenReturn(mockHocVienRs);
            when(mockHocVienRs.next()).thenReturn(true);
            when(mockHocVienRs.getString("ho")).thenReturn("Nguyen");
            when(mockHocVienRs.getString("ten")).thenReturn("Van A");

            PreparedStatement mockKhoaHocStmt = mock(PreparedStatement.class);
            ResultSet mockKhoaHocRs = mock(ResultSet.class);
            when(mockConnection.prepareStatement("SELECT ten_khoa_hoc FROM khoahoc WHERE id = ?"))
                    .thenReturn(mockKhoaHocStmt);
            when(mockKhoaHocStmt.executeQuery()).thenReturn(mockKhoaHocRs);
            when(mockKhoaHocRs.next()).thenReturn(true);
            when(mockKhoaHocRs.getString("ten_khoa_hoc")).thenReturn("Java Programming");

            PreparedStatement mockAdminStmt = mock(PreparedStatement.class);
            ResultSet mockAdminRs = mock(ResultSet.class);
            when(mockConnection.prepareStatement("SELECT id FROM nguoidung WHERE loai_nguoi_dung_id = 1 LIMIT 1"))
                    .thenReturn(mockAdminStmt);
            when(mockAdminStmt.executeQuery()).thenReturn(mockAdminRs);
            when(mockAdminRs.next()).thenReturn(true);
            when(mockAdminRs.getInt("id")).thenReturn(100);

            PreparedStatement mockNotifyStmt = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement("INSERT INTO thongbao (noi_dung, nguoi_nhan_id, ngay_gui, trang_thai) VALUES (?, ?, ?, ?)"))
                    .thenReturn(mockNotifyStmt);
            when(mockNotifyStmt.executeUpdate()).thenReturn(1);

            paymentService.notifyAdmin(1, 1, 123);

            verify(mockHocVienStmt).setInt(1, 1);
            verify(mockKhoaHocStmt).setInt(1, 1);
            verify(mockNotifyStmt).setInt(2, 100);
            verify(mockNotifyStmt).setString(4, "UNREAD");
        }
    }
}