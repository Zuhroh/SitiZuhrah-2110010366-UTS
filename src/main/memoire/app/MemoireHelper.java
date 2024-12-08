package main.memoire.app;

import java.sql.*;

public class MemoireHelper {

    // Method to save a memo (insert or update)
    public static void saveMemoire(Memoire memoire) throws SQLException {
        String sql;
        if (memoire.getId() == 0) {
            // Insert new memo (SQLite will automatically handle 'id' and 'daydate_created')
            sql = "INSERT INTO memoire_list (title, category, contents, last_edited) VALUES (?, ?, ?, ?)";
        } else {
            // Update existing memo (set 'last_edited' manually)
            sql = "UPDATE memoire_list SET title = ?, category = ?, contents = ?, last_edited = ? WHERE id = ?";
        }

        try (Connection conn = MemoireConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, memoire.getTitle());
            pstmt.setString(2, memoire.getCategory());
            pstmt.setString(3, memoire.getContents());
            pstmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis())); // Set last_edited timestamp

            if (memoire.getId() != 0) {
                pstmt.setInt(5, memoire.getId());  // Set the memo ID if updating an existing note
            }

            int affectedRows = pstmt.executeUpdate();

            if (memoire.getId() == 0 && affectedRows > 0) {
                // Get the generated ID for new memos
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        memoire.setId(generatedKeys.getInt(1)); // Set the generated ID for the new memo
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving memoire: " + e.getMessage());
            throw e;
        }
    }

    // Method to delete a memo by ID
    public static void deleteMemoire(int id) throws SQLException {
        String sql = "DELETE FROM memoire_list WHERE id = ?";
        try (Connection conn = MemoireConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);  // Set the memo ID to delete
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting memoire: " + e.getMessage());
            throw e;
        }
    }
}
