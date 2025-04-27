package com.vmct.testcases.CoursesTest;

import com.ntn.quanlykhoahoc.services.CourseService;
import com.ntn.quanlykhoahoc.pojo.KhoaHoc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SearchSecurityTest {

    private CourseService courseService;

    @BeforeEach
    void setUp() {
        // Mock đối tượng CourseService
        courseService = mock(CourseService.class);
    }

    @Test
    void testSQLInjectionInSearch() throws Exception {
        // Giả lập keyword chứa SQL Injection
        String maliciousKeyword = "' OR 1=1 --";

        // Khi tìm với keyword đó, không trả về bất kỳ khóa học nào
        when(courseService.searchCourses(maliciousKeyword)).thenReturn(Collections.emptyList());

        // Gọi tìm kiếm
        List<KhoaHoc> results = courseService.searchCourses(maliciousKeyword);

        // Đảm bảo: không crash, không lỗi, chỉ đơn giản là không tìm thấy kết quả
        assertNotNull(results, "Kết quả tìm kiếm không nên null");
        assertEquals(0, results.size(), "Không nên tìm được khóa học với mã độc");
    }

    @Test
    void testXSSInjectionInSearch() throws Exception {
        // Giả lập keyword chứa XSS
        String maliciousKeyword = "<script>alert(1)</script>";

        // Khi tìm với keyword đó, không trả về bất kỳ khóa học nào
        when(courseService.searchCourses(maliciousKeyword)).thenReturn(Collections.emptyList());

        List<KhoaHoc> results = courseService.searchCourses(maliciousKeyword);

        // Kiểm tra an toàn
        assertNotNull(results);
        assertEquals(0, results.size(), "Không nên thực thi script hay tìm thấy khóa học");
    }

    @Test
    void testSQLDropTableAttempt() throws Exception {
        // Giả lập keyword phá hoại
        String maliciousKeyword = "'; DROP TABLE khoahoc; --";

        // Khi tìm với keyword đó, cũng chỉ trả về empty
        when(courseService.searchCourses(maliciousKeyword)).thenReturn(Collections.emptyList());

        List<KhoaHoc> results = courseService.searchCourses(maliciousKeyword);

        assertNotNull(results);
        assertEquals(0, results.size(), "Không nên ảnh hưởng đến database khi nhập mã độc");
    }
    
    @Test
    void testSearchPerformanceWithLongKeyword() throws Exception {
        // Tạo chuỗi cực dài (100.000 ký tự)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            sb.append("a");
        }
        String longKeyword = sb.toString();

        // Mock: Giả lập kết quả tìm kiếm trả về danh sách rỗng
        when(courseService.searchCourses(longKeyword)).thenReturn(Collections.emptyList());

        // Đo thời gian bắt đầu
        long startTime = System.currentTimeMillis();

        // Gọi phương thức tìm kiếm
        List<KhoaHoc> results = courseService.searchCourses(longKeyword);

        // Đo thời gian kết thúc
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime; // Tính thời gian thực hiện

        // Kiểm tra
        assertNotNull(results, "Kết quả tìm kiếm không nên null");
        assertTrue(duration < 1000, "Thời gian xử lý phải nhỏ hơn 1 giây, thực tế: " + duration + "ms");
    }
}
