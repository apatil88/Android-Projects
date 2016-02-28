package com.amrutpatil.makeanote;

/**
 * Created by Amrut on 2/28/16.
 * Description: Class that represents archived notes for notes whose reminders have passed or if a note has been archived.
 */
public class Archive {
    private String title, description, dateTime, category, type;
    private int id;

    public Archive(String title, String description, String dateTime, String category, String type, int id) {
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.category = category;
        this.type = type;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
