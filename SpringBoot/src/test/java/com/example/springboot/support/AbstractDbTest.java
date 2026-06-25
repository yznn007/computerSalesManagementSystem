package com.example.springboot.support;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/computer_sales_db_test?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai",
        "spring.datasource.username=root",
        "spring.datasource.password=root"
})
public abstract class AbstractDbTest {

    @BeforeAll
    static void initTestDatabase() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/computer_sales_db_test"
                + "?createDatabaseIfNotExist=true"
                + "&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
        try (Connection conn = DriverManager.getConnection(url, "root", "root")) {
            dropAllTables(conn);
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("test-init.sql"));
            replaceSeedPasswords(conn);
            createProcedures(conn);
        }
    }

    private static void dropAllTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS=0");
            stmt.execute("DROP TABLE IF EXISTS Order_Detail");
            stmt.execute("DROP TABLE IF EXISTS Sales_Order");
            stmt.execute("DROP TABLE IF EXISTS Desktop_Composition");
            stmt.execute("DROP TABLE IF EXISTS Spare_Part_Detail");
            stmt.execute("DROP TABLE IF EXISTS Laptop_Detail");
            stmt.execute("DROP TABLE IF EXISTS Desktop_Detail");
            stmt.execute("DROP TABLE IF EXISTS Product");
            stmt.execute("DROP TABLE IF EXISTS Staff");
            stmt.execute("DROP TABLE IF EXISTS Customer");
            stmt.execute("SET FOREIGN_KEY_CHECKS=1");
        }
    }

    private static void replaceSeedPasswords(Connection conn) throws SQLException {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        conn.setAutoCommit(false);
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT customer_id, password_hash FROM Customer WHERE password_hash LIKE '__SEED_%'")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String seed = rs.getString("password_hash");
                String plain = seed.substring("__SEED_".length(), seed.length() - "__".length());
                try (PreparedStatement up = conn.prepareStatement(
                        "UPDATE Customer SET password_hash=? WHERE customer_id=?")) {
                    up.setString(1, encoder.encode(plain));
                    up.setInt(2, rs.getInt("customer_id"));
                    up.executeUpdate();
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT staff_id, password_hash FROM Staff WHERE password_hash LIKE '__SEED_%'")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String seed = rs.getString("password_hash");
                String plain = seed.substring("__SEED_".length(), seed.length() - "__".length());
                try (PreparedStatement up = conn.prepareStatement(
                        "UPDATE Staff SET password_hash=? WHERE staff_id=?")) {
                    up.setString(1, encoder.encode(plain));
                    up.setInt(2, rs.getInt("staff_id"));
                    up.executeUpdate();
                }
            }
        }
        conn.commit();
    }

    private static void createProcedures(Connection conn) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (Reader r = new InputStreamReader(
                new ClassPathResource("test-procedures.sql").getInputStream(), StandardCharsets.UTF_8)) {
            char[] buf = new char[4096];
            int n;
            while ((n = r.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
        }
        for (String block : sb.toString().split("//")) {
            String trimmed = block.trim();
            if (!trimmed.isEmpty()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }
}