package main.memoire.app;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Paths;
import java.nio.file.Path;

public class MemoireConnection {
    /*    
    sqlite3 "C:\Users\ZUHROH\Documents\NetBeansProjects\PBO2AppProjects\MemoireApp\src\resources\db\memoire_app.db"
    SELECT * FROM memoire_list;
    SELECT * FROM memoire_list WHERE DATE(daydate_created) = CURRENT_DATE;

    */
    // Main method to test the database connection
    public static void main(String[] args) {
        try {
            Connection conn = MemoireConnection.getConnection();
            if (conn != null) {
                System.out.println("Database connected successfully!");
                closeConnection(); // Close connection after the test
            } else {
                System.out.println("Failed to connect to the database.");
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
    }
    
    // Relative path to the database file from the project root
    private static final String DATABASE_URL = "jdbc:sqlite:" + getDatabasePath();
    private static Connection connection = null;

    // Private constructor to prevent instantiation
    private MemoireConnection() {
    
    }

    // Method to retrieve the relative path of the database
    private static String getDatabasePath() {
        // Get the current working directory (project root)
        String userDir = System.getProperty("user.dir");
        
        // Construct the path relative to the project root (assuming db is located in src/resources/db)
        Path path = Paths.get(userDir, "src", "resources", "db", "memoire_app.db");
        
        System.out.println("Database path: " + path.toAbsolutePath());
        // Return the absolute path for SQLite connection
        return path.toAbsolutePath().toString();
    }

    // Open connection - Do not close automatically here
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DATABASE_URL);
            } catch (ClassNotFoundException | SQLException e) {
                throw new SQLException("Failed to connect to the database.", e);
            }
        }
        return connection;
    }

    // Don't automatically close the connection here
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                
            }
        }
    }
    
    // Method to get memoire created today
    public static ArrayList<Memoire> getMemosCreatedToday() throws SQLException {
        ArrayList<Memoire> todayMemos = new ArrayList<>();

        // Get today's date in SQL Date format
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

        // Corrected SQL query: use the correct table name "memoire_list"
        String sql = "SELECT * FROM memoire_list WHERE DATE(daydate_created) = DATE('now', '+8 hours')";

        try (Connection conn = MemoireConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, today);  // Set the current date as parameter

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Retrieve daydate_created and last_edited from ResultSet
                    java.sql.Date daydateCreated = rs.getDate("daydate_created");  
                    java.sql.Timestamp lastEdited = rs.getTimestamp("last_edited"); 

                    // Create Memoire object with the retrieved fields
                    Memoire memoire = new Memoire(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("category"),
                        rs.getString("contents"),
                        daydateCreated,
                        lastEdited
                    );
                    todayMemos.add(memoire);  // Add memo to the list
                }
            }
        } catch (SQLException e) {
            throw e;
        }
        System.out.println("Querying for today's memos: SELECT * FROM memoire_list WHERE DATE(daydate_created) = CURRENT_DATE");
        return todayMemos;  // Return the list of today's memos
    }
    
    // Method to get all memoires created
    public static List<Memoire> getAllMemos() throws SQLException {
        List<Memoire> memos = new ArrayList<>();
        String sql = "SELECT * FROM memoire_list";
        try (Connection conn = MemoireConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Retrieve daydateMade and lastEdited from ResultSet
                java.sql.Date daydateMade = rs.getDate("daydate_created");  
                java.sql.Timestamp lastEdited = rs.getTimestamp("last_edited"); 

                // Create Memoire object with the retrieved fields
                Memoire memo = new Memoire(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("category"),
                    rs.getString("contents"),
                    daydateMade,
                    lastEdited
                );
                memos.add(memo);  // Add the memo to the list
            }
        } catch (SQLException e) {
            throw e;  // Rethrow the SQLException if it occurs
        }
        return memos;  // Return the list of all memos
    }
}
