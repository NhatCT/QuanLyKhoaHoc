
import com.ntn.quanlykhoahoc.controllers.CourseController;
import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.BaiTap;
import com.ntn.quanlykhoahoc.services.ExerciseServices;
import java.util.List;

import com.ntn.quanlykhoahoc.pojo.BaiTap;
import com.ntn.quanlykhoahoc.pojo.HocVien_BaiTap;
import com.ntn.quanlykhoahoc.services.CourseService;
import com.ntn.quanlykhoahoc.services.ExerciseServices;
import com.ntn.quanlykhoahoc.services.SubmitServices;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class CertificateTest {

    private ExerciseServices exerciseServices = new ExerciseServices();
    private CourseService courseServices = new CourseService();
// điều kiện nhận tín chỉ
    @Test
    public void dieuKienNhanTinChi() throws SQLException {
        int idKhoaHoc = 3;
        double tatCaBT = (double) exerciseServices.getBaiTapsTheoKhoaHocID(idKhoaHoc).size();
        double baiTapDaLam = 0;
        int diemTrungBinh = 0;

        HocVien_BaiTap bt = null;

        List<BaiTap> ds = exerciseServices.getBaiTapsTheoKhoaHocID(idKhoaHoc);

        for (BaiTap bt2 : ds) {
            Connection conn = Database.getConn();
            String sql = "SELECT * FROM hocvien_baitap h_b "
                    + "JOIN baitap bt ON bt.id = h_b.baiTapID "
                    + "WHERE bt.khoaHocID = ? and h_b.hocVienID = ? and h_b.baiTapID = ?";
            PreparedStatement stm = conn.prepareStatement(sql);
            stm.setInt(1, idKhoaHoc);
            stm.setInt(2, 24);
            stm.setInt(3, bt2.getId());
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {
                HocVien_BaiTap c = new HocVien_BaiTap(
                        rs.getInt("id"),
                        rs.getInt("hocVienID"),
                        rs.getInt("baiTapID"),
                        rs.getInt("diem"),
                        rs.getTimestamp("deadline")
                );
                bt = c;
            }

            if (bt != null) {
                diemTrungBinh += bt.getDiem();
                baiTapDaLam++;
            }

        }
        diemTrungBinh = diemTrungBinh / exerciseServices.getBaiTapsTheoKhoaHocID(idKhoaHoc).size();

        double c = (baiTapDaLam / tatCaBT) * 100;
        int phanTramTienTrinh = (int) c;
        assertTrue(phanTramTienTrinh > 80 && diemTrungBinh > 70, "Phần trăm tiến trình phải lớn hơn 80");

    }

}
