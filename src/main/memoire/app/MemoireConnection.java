package main.memoire.app;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.text.ParseException;

/*    
    sqlite3 "C:\Users\ZUHROH\Documents\NetBeansProjects\PBO2AppProjects\MemoireApp\src\resources\db\memoire_app.db"
    SELECT * FROM memoire_list;
    SELECT * FROM memoire_list WHERE DATE(daydate_created) = CURRENT_DATE;
*/
public class MemoireConnection {
    // SQLite database URL, relative to the project root
    private static final String DATABASE_URL = "jdbc:sqlite:" + getDatabasePath();
    private static Connection connection = null;

    // Private constructor to prevent instantiation
    private MemoireConnection() {}
    
    // Main method for testing the connection
    public static void main(String[] args) {
        try {
            Connection conn = MemoireConnection.getConnection();
            if (conn != null) {
                System.out.println("Database connected successfully!");
                closeConnection();
            } else {
                System.out.println("Failed to connect to the database.");
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
    }

   // Get the absolute path to the database file
    private static String getDatabasePath() {
        String userDir = System.getProperty("user.dir"); // Project root directory
        Path path = Paths.get(userDir, "src", "resources", "db", "memoire_app.db");
        System.out.println("Database path: " + path.toAbsolutePath());
        return path.toAbsolutePath().toString();
    }

    // Get a connection to the database
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC"); // Load SQLite JDBC driver
                connection = DriverManager.getConnection(DATABASE_URL);
            } catch (ClassNotFoundException | SQLException e) {
                throw new SQLException("Failed to connect to the database.", e);
            }
        }
        return connection;
    }

    // Close the database connection
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null; // Ensure connection is reset
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    // Get memos created today
    public static List<Memoire> getMemosCreatedToday() throws SQLException {
        List<Memoire> todayMemos = new ArrayList<>();
        String sql = "SELECT * FROM memoire_list WHERE DATE(daydate_created) = DATE('now')";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                todayMemos.add(mapResultSetToMemoire(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching today's memos: " + e.getMessage());
            throw e;
        }

        System.out.println("Fetched today's memos.");
        return todayMemos;
    }

    // Get all memoires
    public static List<Memoire> getAllMemos() throws SQLException {
        List<Memoire> memos = new ArrayList<>();
        String sql = "SELECT * FROM memoire_list";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                memos.add(mapResultSetToMemoire(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all memos: " + e.getMessage());
            throw e;
        }

        return memos;
    }

    // Utility method to map a ResultSet row to a Memoire object
    private static Memoire mapResultSetToMemoire(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String category = rs.getString("category");
        String contents = rs.getString("contents");

        // Parse daydate_created
        String dayDateCreatedString = rs.getString("daydate_created");
        java.sql.Date dayDateCreated = null;
        if (dayDateCreatedString != null) {
            try {
                java.util.Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(dayDateCreatedString);
                dayDateCreated = new java.sql.Date(parsedDate.getTime());
            } catch (ParseException e) {
                System.err.println("Error parsing dayDateCreated: " + e.getMessage());
            }
        }

        // Get lastEdited
        java.sql.Timestamp lastEdited = rs.getTimestamp("last_edited");

        // Return a new Memoire object
        return new Memoire(id, title, category, contents, dayDateCreated, lastEdited);
    }
}
