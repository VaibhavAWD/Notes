package com.vaibhavdhunde.android.notes.model;

public class Note {
    private int id;
    private String title;
    private String note;
    private int color;

    public Note(int id, String title, String note, int color) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public int getColor() {
        return color;
    }
}
