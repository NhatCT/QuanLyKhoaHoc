package com.ntn.quanlykhoahoc.services;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.NguoiDung;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private EmailService mockEmailService;

    @InjectMocks
    private UserService userService;

    private MockedStatic<Database> mockedDatabase;

    @BeforeEach
    void setUp() {
        // Mock kết nối database
        mockedDatabase = mockStatic(Database.class);
        mockedDatabase.when(Database::getConn).thenReturn(mockConnection);
    }

    @AfterEach
    void tearDown() {
        // Đóng mock database sau mỗi test
        mockedDatabase.close();
    }

    @Nested
    class ScheduleTests {

        @Test
        void testKhongTrungLich() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            assertFalse(userService.hasOverlappingSchedule(1, 2));

            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).setInt(2, 2);
        }

        @Test
        void testCoTrungLich() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);

            assertTrue(userService.hasOverlappingSchedule(1, 2));
        }
    }

    @Nested
    class UserRegistrationTests {

        @Test
        void testDangKyUserThanhCong() throws SQLException {
            // Mock cho prepareStatement
            PreparedStatement validationStmt = mock(PreparedStatement.class);
            PreparedStatement emailCheckStmt = mock(PreparedStatement.class);
            PreparedStatement registerStmt = mock(PreparedStatement.class);

            ResultSet validationRs = mock(ResultSet.class);
            ResultSet emailCheckRs = mock(ResultSet.class);

            // Khi SQL query chứa "SELECT id FROM loainguoidung" -> validationStmt
            when(mockConnection.prepareStatement(contains("SELECT id FROM loainguoidung"))).thenReturn(validationStmt);
            // Khi SQL query chứa "SELECT email FROM nguoidung" -> emailCheckStmt
            when(mockConnection.prepareStatement(contains("SELECT email FROM nguoidung"))).thenReturn(emailCheckStmt);
            // Khi SQL query chứa "INSERT INTO" -> registerStmt
            when(mockConnection.prepareStatement(contains("INSERT INTO"))).thenReturn(registerStmt);

            // Setup cho validation và email check
            when(validationStmt.executeQuery()).thenReturn(validationRs);
            when(emailCheckStmt.executeQuery()).thenReturn(emailCheckRs);

            // Đảm bảo loại người dùng 2 là hợp lệ
            when(validationRs.next()).thenReturn(true);
            // Đảm bảo email chưa tồn tại
            when(emailCheckRs.next()).thenReturn(false);

            // Thành công khi insert
            when(registerStmt.executeUpdate()).thenReturn(1);

            boolean result = userService.registerUser("Nguyễn", "Văn A", "valid@example.com", "password", 2, "/custom.jpg", true);

            assertTrue(result);

            // Verify các phương thức được gọi
            verify(validationStmt).setInt(1, 2);
            verify(emailCheckStmt).setString(1, "valid@example.com");
            verify(registerStmt).executeUpdate();
        }

        @Test
        void testDangKyUserVoiAvatarMacDinh() throws SQLException {
            // Mock cho prepareStatement
            PreparedStatement validationStmt = mock(PreparedStatement.class);
            PreparedStatement emailCheckStmt = mock(PreparedStatement.class);
            PreparedStatement registerStmt = mock(PreparedStatement.class);

            ResultSet validationRs = mock(ResultSet.class);
            ResultSet emailCheckRs = mock(ResultSet.class);

            // Thiết lập các PreparedStatement cho các truy vấn SQL
            when(mockConnection.prepareStatement(contains("SELECT id FROM loainguoidung"))).thenReturn(validationStmt);
            when(mockConnection.prepareStatement(contains("SELECT email FROM nguoidung"))).thenReturn(emailCheckStmt);
            when(mockConnection.prepareStatement(contains("INSERT INTO"))).thenReturn(registerStmt);

            // Thiết lập cho validation và email check
            when(validationStmt.executeQuery()).thenReturn(validationRs);
            when(emailCheckStmt.executeQuery()).thenReturn(emailCheckRs);

            // Đảm bảo loại người dùng 3 là hợp lệ
            when(validationRs.next()).thenReturn(true);
            // Đảm bảo email chưa tồn tại
            when(emailCheckRs.next()).thenReturn(false);

            // Thành công khi insert
            when(registerStmt.executeUpdate()).thenReturn(1);

            boolean result = userService.registerUser("Nguyễn", "Văn A", "valid@example.com", "password", 3, null, true);

            assertTrue(result);
            verify(registerStmt).setString(7, "/com/ntn/images/avatars/default.jpg");
        }

        @Nested
        class UserUpdateTests {

            private NguoiDung existingUser;

            @BeforeEach
            void setUp() {
                existingUser = new NguoiDung();
                existingUser.setEmail("old@example.com");
            }

            @Test
            void testCapNhatUserThanhCong() throws SQLException {
                NguoiDung updatedUser = new NguoiDung();
                updatedUser.setHo("Trần");
                updatedUser.setTen("Văn B");
                updatedUser.setEmail("new@example.com");
                updatedUser.setMatKhau("newpassword");
                updatedUser.setActive(true);
                updatedUser.setLoaiNguoiDungId(2);
                updatedUser.setAvatar("/new-avatar.jpg");

                // Mock cho prepareStatement
                PreparedStatement validationStmt = mock(PreparedStatement.class);
                PreparedStatement updateStmt = mock(PreparedStatement.class);

                ResultSet validationRs = mock(ResultSet.class);

                // Setup các PreparedStatement cho các SQL query khác nhau
                when(mockConnection.prepareStatement(contains("SELECT id FROM loainguoidung"))).thenReturn(validationStmt);
                when(mockConnection.prepareStatement(contains("UPDATE nguoidung"))).thenReturn(updateStmt);

                // Setup cho validation
                when(validationStmt.executeQuery()).thenReturn(validationRs);

                // Đảm bảo loại người dùng 2 là hợp lệ
                when(validationRs.next()).thenReturn(true);

                // Thành công khi update
                when(updateStmt.executeUpdate()).thenReturn(1);

                boolean result = userService.updateUser(existingUser, updatedUser);

                assertTrue(result);

                // Verify các phương thức được gọi
                verify(validationStmt).setInt(1, 2);
                verify(updateStmt).executeUpdate();
            }

            @Nested
            class UserManagementTests {

                @Test
                void testLayTatCaUser() throws SQLException {
                    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
                    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
                    when(mockResultSet.next()).thenReturn(true).thenReturn(false);
                    when(mockResultSet.getInt("id")).thenReturn(1);
                    when(mockResultSet.getString("ho")).thenReturn("Nguyễn");
                    when(mockResultSet.getString("ten")).thenReturn("Văn A");
                    when(mockResultSet.getString("email")).thenReturn("test@example.com");
                    when(mockResultSet.getBoolean("active")).thenReturn(true);
                    when(mockResultSet.getInt("loai_nguoi_dung_id")).thenReturn(2);
                    when(mockResultSet.getString("avatar")).thenReturn("/avatar.jpg");

                    List<NguoiDung> users = userService.getAllUsers();
                    assertEquals(1, users.size());
                    assertEquals("Nguyễn", users.get(0).getHo());
                    assertEquals("Văn A", users.get(0).getTen());
                }

                @Test
                void testXoaUser() throws SQLException {
                    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
                    when(mockPreparedStatement.executeUpdate()).thenReturn(1);
                    assertTrue(userService.deleteUser("test@example.com"));
                    verify(mockPreparedStatement).setString(1, "test@example.com");
                }
            }

            @Nested
            class ExceptionHandlingTests {

                @Test
                void testLoiSQLKhiDangKy() throws SQLException {
                    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Lỗi DB"));
                    assertThrows(SQLException.class, () -> userService.registerUser("Nguyễn", "Văn A", "test@test.com", "pass", 2, null, true));
                }
            }
        }
    }
}
