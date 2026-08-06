package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central place for creating JDBC database connections.
 */
public final class DBConnection {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USERNAME = "Shreyash";
    private static final String PASSWORD = "Shreyash123";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("Oracle JDBC Driver not found.", exception);
        }
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
