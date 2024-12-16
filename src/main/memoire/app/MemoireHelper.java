package main.memoire.app;

import java.sql.*;
import javax.swing.JOptionPane;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

public class MemoireHelper {
    private static final Logger LOGGER = Logger.getLogger(MemoireHelper.class.getName());

    // Method to save a note (insert or update)
    public static void saveMemoire(Memoire memoire) throws SQLException {
        String sql;
        if (memoire.getId() == 0) {
            // Check if the memo already exists by title
            if (memoExists(memoire.getTitle())) {
                // Ask user for confirmation to overwrite the existing memo
                int confirmation = JOptionPane.showConfirmDialog(null, 
                    "A memo with the same title already exists. Do you want to update it?", 
                    "Duplicate Memo", JOptionPane.YES_NO_OPTION);
                if (confirmation == JOptionPane.YES_OPTION) {
                    // Update the existing memo
                    sql = "UPDATE memoire_list SET title = ?, category = ?, contents = ?, last_edited = ? WHERE title = ?";
                    updateMemoire(memoire, sql);
                } else {
                    return;  // Do nothing if user opts not to update
                }
            } else {
                // Insert new memo
                sql = "INSERT INTO memoire_list (title, category, contents, last_edited) VALUES (?, ?, ?, ?)";
                insertMemoire(memoire, sql);
            }
        } else {
            // Update existing memo
            sql = "UPDATE memoire_list SET title = ?, category = ?, contents = ?, last_edited = ? WHERE id = ?";
            updateMemoire(memoire, sql);
        }
    }

    // Helper method to check if a memo with the same title exists
    private static boolean memoExists(String title) throws SQLException {
        String checkSQL = "SELECT COUNT(*) FROM memoire_list WHERE title = ?";
        try (Connection conn = MemoireConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSQL)) {
            pstmt.setString(1, title);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.getInt(1) > 0;
            }
        }
    }

    // Method for inserting new memos
    private static void insertMemoire(Memoire memoire, String sql) throws SQLException {
        try (Connection conn = MemoireConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, memoire.getTitle());
            pstmt.setString(2, memoire.getCategory());
            pstmt.setString(3, memoire.getContents());
            pstmt.setTimestamp(4, memoire.getLastEdited());
            pstmt.executeUpdate();
        }
    }

    // Method for updating existing memos
    private static void updateMemoire(Memoire memoire, String sql) throws SQLException {
        try (Connection conn = MemoireConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, memoire.getTitle());
            pstmt.setString(2, memoire.getCategory());
            pstmt.setString(3, memoire.getContents());
            pstmt.setTimestamp(4, memoire.getLastEdited());
            if (memoire.getId() == 0) {
                pstmt.setString(5, memoire.getTitle());  // Use title for update if no ID
            } else {
                pstmt.setInt(5, memoire.getId());  // Use ID for update
            }
            pstmt.executeUpdate();
        }
    }

    // Method to delete a memo by ID
    public static void deleteMemoire(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid memoire ID.");
        }

        String sql = "DELETE FROM memoire_list WHERE id = ?";

        try (Connection conn = MemoireConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No memoire found with the given ID.");
            }

            LOGGER.log(Level.INFO, "Memoire with ID {0} deleted successfully.", id);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting memoire", e);
            throw e;
        }
    }

    // Method to save a new category to the memoire_list table
    public static void saveCategory(String category, JComboBox<String> categoryComboBox) throws SQLException {
        // Validate that category is not empty or null
        if (category == null || category.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Category cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Define the SQL to check if the category already exists
        String checkCategorySql = "SELECT COUNT(*) FROM memoire_list WHERE category = ?";
        String insertCategorySql = "INSERT INTO memoire_list (category) VALUES (?)"; // Insert new category if not exists

        // Use try-with-resources to manage database resources
        try (Connection conn = MemoireConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkCategorySql)) {

            // Check if the category already exists
            checkStmt.setString(1, category);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Category already exists: " + category);
                    return;  // Avoid adding duplicate categories
                }
            }

            // Category does not exist, so proceed with inserting the category into the database
            try (PreparedStatement insertStmt = conn.prepareStatement(insertCategorySql)) {
                insertStmt.setString(1, category);
                int rowsAffected = insertStmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Category added successfully: " + category);

                    // Update the JComboBox to reflect the newly added category
                    // Use SwingUtilities.invokeLater to ensure UI update happens on the Event Dispatch Thread
                    SwingUtilities.invokeLater(() -> {
                        // Add the new category to the combo box if it was successfully inserted
                        categoryComboBox.addItem(category);
                        categoryComboBox.setSelectedItem(category); // Optionally, select the new category
                    });
                } else {
                    System.out.println("Category not added. No rows inserted.");
                }
            } catch (SQLException e) {
                // Log detailed error and rethrow for higher-level handling
                System.err.println("Error executing insert for category: " + category);
                JOptionPane.showMessageDialog(null, "Error adding category to database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                throw e;
            }
        } catch (SQLException e) {
            // Catch any exception from the connection or the SQL query execution
            System.err.println("Database error: " + e.getMessage());
            throw e;  // Rethrow to propagate the error
        }
    }
}
