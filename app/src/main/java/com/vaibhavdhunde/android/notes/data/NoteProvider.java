package com.vaibhavdhunde.android.notes.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.vaibhavdhunde.android.notes.data.NoteContract.NoteEntry;

@SuppressWarnings("ConstantConditions")
public class NoteProvider extends ContentProvider {

    private static final String LOG_TAG = NoteProvider.class.getSimpleName();

    private static final int NOTES = 100;
    private static final int NOTE_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private NoteDbHelper mDbHelper;

    static {
        sUriMatcher.addURI(
                NoteContract.CONTENT_AUTHORITY,
                NoteContract.PATH_NOTES,
                NOTES
        );
        sUriMatcher.addURI(
                NoteContract.CONTENT_AUTHORITY,
                NoteContract.PATH_NOTES + "/#",
                NOTE_ID
        );
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new NoteDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES: {
                cursor = database.query(
                        NoteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case NOTE_ID: {
                selection = NoteEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(
                        NoteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new IllegalArgumentException("Cannot resolve URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return insertNote(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for this URI: " + uri);
        }
    }

    private Uri insertNote(Uri uri, ContentValues contentValues) {
        String title = contentValues.getAsString(NoteEntry.COLUMN_TITLE);
        if (title == null) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        String note = contentValues.getAsString(NoteEntry.COLUMN_NOTE);
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be empty");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long newNoteId = database.insert(NoteEntry.TABLE_NAME, null, contentValues);

        if (newNoteId == -1) {
            Log.d(LOG_TAG, "Failed to insert note.");
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, newNoteId);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTE_ID:
                selection = NoteEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateNote(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for this URI: " + uri);
        }
    }

    private int updateNote(Uri uri, ContentValues contentValues, String selection,
                           String[] selectionArgs) {
        String title = contentValues.getAsString(NoteEntry.COLUMN_TITLE);
        if (title == null) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        String note = contentValues.getAsString(NoteEntry.COLUMN_NOTE);
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be empty");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int numNotesUpdated = database.update(
                NoteEntry.TABLE_NAME,
                contentValues,
                selection,
                selectionArgs
        );

        if (numNotesUpdated == 0) {
            Log.d(LOG_TAG, "Failed to update note.");
            return 0;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return numNotesUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return deleteNote(uri, selection, selectionArgs);
            case NOTE_ID:
                selection = NoteEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deleteNote(uri, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for this URI: " + uri);
        }
    }

    private int deleteNote(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int numNotesDeleted = database.delete(NoteEntry.TABLE_NAME, selection, selectionArgs);

        if (numNotesDeleted == 0) {
            Log.d(LOG_TAG, "Failed to delete note(s).");
            return 0;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return numNotesDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return NoteEntry.CONTENT_LIST_TYPE;
            case NOTE_ID:
                return NoteEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri + " with match: " + match);
        }
    }
}
