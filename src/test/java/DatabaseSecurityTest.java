

import com.ntn.quanlykhoahoc.database.Database;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseSecurityTest {

    @Test
    void testGetUserIdByEmail_SQLInjectionAttempt() throws Exception {
        String maliciousInput = "' OR 1=1 --";
        int userId = Database.getUserIdByEmail(maliciousInput);

        // Nếu trả về -1, chứng tỏ đã an toàn
        assertEquals(-1, userId, "Có thể bị SQL Injection nếu không bảo vệ đúng!");
    }
}
