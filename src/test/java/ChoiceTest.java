import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.DapAn;
import com.ntn.quanlykhoahoc.services.ChoiceServices;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChoiceTest {
    @InjectMocks
    private ChoiceServices choiceServices;
    
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
    void testGetDapAnTheoCauHoiID_Success() throws SQLException {
        // Mock ResultSet: 4 bản ghi đáp án
        when(mockRs.next()).thenReturn(true, true, true, true, false);
        
        // Đáp án 1
        when(mockRs.getInt("id")).thenReturn(1, 2, 3, 4);
        when(mockRs.getString("noiDung")).thenReturn(
                "Đáp án A", 
                "Đáp án B", 
                "Đáp án C", 
                "Đáp án D"
        );
        when(mockRs.getBoolean("dapAnDung")).thenReturn(false, true, false, false);
        when(mockRs.getInt("cauHoiID")).thenReturn(5, 5, 5, 5);
        
        // Gọi phương thức
        int cauHoiId = 5;
        List<DapAn> dapAnList = choiceServices.getDapAnTheoCauHoiID(cauHoiId);
        
        // Kiểm tra kết quả
        assertEquals(4, dapAnList.size(), "Phải trả về đúng 4 đáp án");
        
        // Kiểm tra thông tin của từng đáp án
        assertEquals("Đáp án A", dapAnList.get(0).getNoiDung());
        assertEquals(false, dapAnList.get(0).isDapAnDung());
        
        assertEquals("Đáp án B", dapAnList.get(1).getNoiDung());
        assertEquals(true, dapAnList.get(1).isDapAnDung());
        
        assertEquals("Đáp án C", dapAnList.get(2).getNoiDung());
        assertEquals(false, dapAnList.get(2).isDapAnDung());
        
        assertEquals("Đáp án D", dapAnList.get(3).getNoiDung());
        assertEquals(false, dapAnList.get(3).isDapAnDung());
        
        // Xác minh PreparedStatement được thiết lập đúng
        verify(mockStmt).setInt(1, cauHoiId);
    }
    

    @Test
    void testGetDapAnTheoCauHoiID_EmptyList() throws SQLException {
        // Mock ResultSet: không có bản ghi nào
        when(mockRs.next()).thenReturn(false);
        
        // Gọi phương thức
        int cauHoiId = 99; // ID câu hỏi không có đáp án
        List<DapAn> dapAnList = choiceServices.getDapAnTheoCauHoiID(cauHoiId);
        
        // Kiểm tra kết quả
        assertTrue(dapAnList.isEmpty(), "Danh sách đáp án phải rỗng");
        
        // Xác minh PreparedStatement được thiết lập đúng
        verify(mockStmt).setInt(1, cauHoiId);
    }
    
    
    @Test
    void testGetDapAnTheoCauHoiID_SQLException() throws SQLException {
        // Mock SQLException khi executeQuery
        when(mockStmt.executeQuery()).thenThrow(new SQLException("Database error"));
        
        // Gọi phương thức và xác minh ngoại lệ
        int cauHoiId = 5;
        try {
            choiceServices.getDapAnTheoCauHoiID(cauHoiId);
        } catch (SQLException e) {
            assertEquals("Database error", e.getMessage());
        }
        
        // Xác minh PreparedStatement được thiết lập đúng
        verify(mockStmt).setInt(1, cauHoiId);
    }
}