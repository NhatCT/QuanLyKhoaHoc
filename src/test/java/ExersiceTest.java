import com.ntn.quanlykhoahoc.pojo.BaiTap;
import com.ntn.quanlykhoahoc.pojo.HocVien_BaiTap;
import com.ntn.quanlykhoahoc.services.ExerciseServices;
import com.ntn.quanlykhoahoc.services.SubmitServices;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author ADMIN
 */
public class ExersiceTest {

    private static SubmitServices sm = new SubmitServices();
    private static ExerciseServices ex = new ExerciseServices();

    @Test
    public void testScore() throws SQLException {
        List<HocVien_BaiTap> hv_bt = new ArrayList<>();
        hv_bt = sm.getSubmits();
        for (HocVien_BaiTap ketQua : hv_bt) {
            assertTrue(ketQua.getDiem() >= 0 && ketQua.getDiem() <= 100, "Điểm phải nằm trong khoảng từ 0 đến 100");
        }

    }
// kiểm tra làm 1 lần duy 
    @Test
    public void testDoItOnce() throws SQLException {

        List<HocVien_BaiTap> hv_bt = new ArrayList<>();
        hv_bt = sm.getSubmitHocVienIDBaiTapID(24, 1);
        assertEquals(1, hv_bt.size());
    }
    // nop bai truoc deadline 
    @Test
    
    public void testTimeSubmit() throws SQLException{
    List<HocVien_BaiTap> hv_bt = new ArrayList<>();
        hv_bt = sm.getSubmitHocVienIDBaiTapID(24,1);
        HocVien_BaiTap hv_bt1 = hv_bt.get(0);
        Timestamp ngayNop = hv_bt1.getNgayNop();
        BaiTap bt = ex.getBaiTapByID(1);
        Timestamp deadline = bt.getDeadline();
         assertTrue(ngayNop.before(deadline) || ngayNop.equals(deadline),"Ngày nộp không hợp lệ, trễ deadline!");
    }

}
