package com.vaibhavdhunde.android.notes.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.vaibhavdhunde.android.notes.data.NoteContract.NoteEntry;

public class NoteDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notes.db";

    private static final int DATABASE_VERSION = 2;

    public NoteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        createTableNotes(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        addColumnColorToTableNotes(database);
    }

    private void addColumnColorToTableNotes(SQLiteDatabase database) {
        String SQL_ALTER_TABLE_NOTES = "ALTER TABLE " + NoteEntry.TABLE_NAME +
                " ADD COLUMN " + NoteEntry.COLUMN_COLOR + " INTEGER DEFAULT 0;";
        database.execSQL(SQL_ALTER_TABLE_NOTES);
    }

    private void createTableNotes(SQLiteDatabase database) {
        String SQL_CREATE_TABLE_NOTES = "CREATE TABLE IF NOT EXISTS " + NoteEntry.TABLE_NAME + " (" +
                NoteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NoteEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                NoteEntry.COLUMN_NOTE + " TEXT NOT NULL, " +
                NoteEntry.COLUMN_COLOR + " INTEGER DEFAULT 0);";
        database.execSQL(SQL_CREATE_TABLE_NOTES);
    }
}
