
import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.KhoaHoc;
import com.ntn.quanlykhoahoc.services.CourseService;
import com.ntn.quanlykhoahoc.controllers.DashboardStudentController;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PerformanceTest {

    private final CourseService courseService = new CourseService();
    private final DashboardStudentController controller = new DashboardStudentController();

    @Test
    void testLoadCourses_PerformanceUnder2Seconds() throws Exception {
        long start = System.currentTimeMillis();

        List<KhoaHoc> allCourses = courseService.getAllActiveCourses();
        int userId = Database.getUserIdByEmail("hv1@example.com");
        List<KhoaHoc> enrolled = courseService.getEnrolledCourses(userId);

        List<KhoaHoc> filtered = allCourses.stream()
                .filter(k -> controller.canEnrollCourse(userId, k, enrolled))
                .toList();

        long end = System.currentTimeMillis();
        long duration = end - start;

        System.out.println("Thời gian xử lý: " + duration + "ms");
        assertTrue(duration < 2000, "Quá trình tải khóa học mất quá nhiều thời gian!");
    }
}
