package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.InputStream;

public class DBConnUtil {
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Load properties from resources folder
            Properties props = new Properties();
            InputStream input = DBConnUtil.class.getClassLoader().getResourceAsStream("db.properties");

            if (input == null) {
                throw new RuntimeException("❌ db.properties file not found in classpath!");
            }

            props.load(input);

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.username");
            String pass = props.getProperty("db.password");

            conn = DriverManager.getConnection(url, user, pass);
            System.out.println("✅ Connected to database successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Database connection failed!", e);
        }

        return conn;
    }
}