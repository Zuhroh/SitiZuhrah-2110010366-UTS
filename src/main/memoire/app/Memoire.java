package main.memoire.app;


public class Memoire {
    private int id;
    private String title;
    private String category;
    private String contents;
    private java.sql.Date dayDateCreated; 
    private java.sql.Timestamp lastEdited;

    // Constructor
    public Memoire(int id, String title, String category, String contents, java.sql.Date dayDateCreated, java.sql.Timestamp lastEdited) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.contents = contents;
        this.dayDateCreated = dayDateCreated;
        this.lastEdited = lastEdited;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getContents() {
        return contents;
    }

    public java.sql.Date getDayDateCreated() {
        return dayDateCreated;
    }

    public java.sql.Timestamp getLastEdited() {
        return lastEdited;
    }
    
    // Setters for the fields
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setContents(String contents) { 
        this.contents = contents;
    }

    public void setDayDateCreated(java.sql.Date dayDateCreated) {
        this.dayDateCreated = dayDateCreated;
    }

    public void setLastEdited(java.sql.Timestamp lastEdited) {
        this.lastEdited = lastEdited;
    }
}
