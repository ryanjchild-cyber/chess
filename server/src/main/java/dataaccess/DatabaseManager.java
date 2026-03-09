package dataaccess;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
public class DatabaseManager {
    private static final Properties properties=new Properties();
    static {
        try (InputStream input=DatabaseManager.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input==null) {
                throw new RuntimeException("db.properties not found");
            }
            properties.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load db.properties",e);
        }
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        );
    }
    public static void createDatabase() throws SQLException {
        String url=properties.getProperty("db.url");
        String user=properties.getProperty("db.user");
        String password=properties.getProperty("db.password");
        int lastSlash=url.lastIndexOf("/");
        String serverUrl=url.substring(0,lastSlash);
        String dbName=url.substring(lastSlash+1);
        try (Connection connection=DriverManager.getConnection(serverUrl,user,password);
            var statement=connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS "+dbName);
        }
    }
}
