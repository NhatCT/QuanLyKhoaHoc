
import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.LichHoc;
import com.ntn.quanlykhoahoc.services.TimetableService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TimetableTest {

    @InjectMocks
    private TimetableService timetableService;

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

        // Mock prepareStatement và executeQuery
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
    }

    @AfterEach
    void tearDown() {
        // Giải phóng mock tĩnh
        databaseMock.close();
    }

    @Test
    void testGetTimetableForStudent_Success() throws Exception {
        // Mock ResultSet: 2 bản ghi
        when(mockRs.next()).thenReturn(true, true, false);
        when(mockRs.getInt("id")).thenReturn(1, 2);
        when(mockRs.getInt("khoaHocId")).thenReturn(101, 102);
        when(mockRs.getString("ten_khoa_hoc")).thenReturn("Java cơ bản", "Python nâng cao");
        when(mockRs.getDate("ngay_hoc")).thenReturn(
                java.sql.Date.valueOf(LocalDate.of(2025, 4, 23)),
                java.sql.Date.valueOf(LocalDate.of(2025, 4, 24))
        );
        when(mockRs.getTime("gio_bat_dau")).thenReturn(
                java.sql.Time.valueOf(LocalTime.of(9, 0)),
                java.sql.Time.valueOf(LocalTime.of(14, 0))
        );
        when(mockRs.getTime("gio_ket_thuc")).thenReturn(
                java.sql.Time.valueOf(LocalTime.of(11, 0)),
                java.sql.Time.valueOf(LocalTime.of(16, 0))
        );
        when(mockRs.getInt("giangVienId")).thenReturn(201, 202);
        when(mockRs.getString("giang_vien")).thenReturn("Nguyen Van A", "Tran Thi B");
        when(mockRs.getString("lien_ket")).thenReturn("http://zoom.us/123", "http://zoom.us/456");

        // Gọi phương thức
        List<LichHoc> timetable = timetableService.getTimetableForStudent("student@example.com");

        // Kiểm tra kết quả
        assertEquals(2, timetable.size());
        assertEquals("Java Basics", timetable.get(0).getTenKhoaHoc());
        assertEquals("Advanced Python", timetable.get(1).getTenKhoaHoc());

        // Xác minh PreparedStatement được thiết lập đúng
        verify(mockStmt).setString(1, "student@example.com");
    }
// Nguyen
    @Test
    public void checkTeacherScheduleConflict() throws SQLException {
        Connection connection = Database.getConn();

        String query = "SELECT giangVienId, ngay_hoc, gio_bat_dau, COUNT(DISTINCT khoaHocId) AS classCount "
                + "FROM khoa_hoc "
                + "GROUP BY giangVienId, ngay_hoc, gio_bat_dau "
                + "HAVING COUNT(DISTINCT khoaHocId) > 1"; // Kiểm tra nếu có giáo viên dạy > 1 lớp cùng ngày và giờ

        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet result = statement.executeQuery();

       

            while (result.next()) {
                int teacherId = result.getInt("giangVienId");
                String classDate = result.getString("ngay_hoc");
                String startTime = result.getString("gio_bat_dau");
                int classCount = result.getInt("classCount");

                // Kiểm tra xem có giáo viên nào dạy nhiều lớp cùng ngày và giờ không
                assertTrue(classCount > 1,
                        "Giáo viên không bị trùng lịch" );
            }
        }
    }

