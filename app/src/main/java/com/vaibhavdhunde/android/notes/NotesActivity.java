package com.vaibhavdhunde.android.notes;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.vaibhavdhunde.android.notes.adapter.NoteAdapter;
import com.vaibhavdhunde.android.notes.data.NoteContract.NoteEntry;
import com.vaibhavdhunde.android.notes.model.Note;
import com.vaibhavdhunde.android.notes.pref.NotesPreferences;
import com.vaibhavdhunde.android.notes.util.RecyclerTouchListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NotesActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int NOTES_LOADER_ID = 1;

    private static final int LIST_SPAN_COUNT = 2;

    @BindView(R.id.list_notes)
    RecyclerView mListNotes;

    @BindView(R.id.empty_state_container)
    LinearLayout mEmptyStateContainer;

    @BindView(R.id.fab_add_new_note)
    FloatingActionButton mFabAddNewNote;

    private NoteAdapter mNoteAdapter;

    private int mNotesCount;

    private NotesPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        ButterKnife.bind(this);

        // TODO: On rotation action bar reappears if already scrolled off the screen

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPreferences = new NotesPreferences(this);
        if (mPreferences.isViewQuilt()) {
            showStaggeredList();
        } else {
            showLinearList();
        }

        setupListNotes();

        getLoaderManager().initLoader(NOTES_LOADER_ID, null, this);

        mListNotes.addOnItemTouchListener(new RecyclerTouchListener(
                this, mListNotes, new RecyclerTouchListener.OnClickListener() {
            @Override
            public void OnClick(int position) {
                Intent noteEditorIntent = new Intent(
                        NotesActivity.this, EditorActivity.class);

                Note currentNote = mNoteAdapter.getNotes().get(position);
                int currentNoteId = currentNote.getId();
                Uri currentNoteUri = ContentUris.withAppendedId(NoteEntry.CONTENT_URI, currentNoteId);
                noteEditorIntent.setData(currentNoteUri);

                startActivity(noteEditorIntent);
            }

            @Override
            public void OnLongClick(int position) {

            }
        }));

        mListNotes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && mFabAddNewNote.getVisibility() == View.VISIBLE) {
                    mFabAddNewNote.hide();
                } else if (dy < 0 && mFabAddNewNote.getVisibility() != View.VISIBLE) {
                    mFabAddNewNote.show();
                }
            }
        });
    }

    private void setupListNotes() {
        mListNotes.setHasFixedSize(true);
        mNoteAdapter = new NoteAdapter();
        mListNotes.setAdapter(mNoteAdapter);
    }

    @OnClick(R.id.fab_add_new_note)
    protected void addNewNote() {
        Intent noteEditorIntent = new Intent(NotesActivity.this, EditorActivity.class);
        startActivity(noteEditorIntent);
    }

    private void showNotes() {
        mListNotes.setVisibility(View.VISIBLE);
        mEmptyStateContainer.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        mEmptyStateContainer.setVisibility(View.VISIBLE);
        mListNotes.setVisibility(View.GONE);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem viewQuilt = menu.findItem(R.id.action_view_quilt);
        MenuItem viewStream = menu.findItem(R.id.action_view_stream);

        if (mNotesCount == 0) {
            MenuItem deleteAll = menu.findItem(R.id.action_delete_all);
            deleteAll.setVisible(false);
            viewQuilt.setVisible(false);
            viewStream.setVisible(false);
        }

        if (mPreferences.isViewQuilt()) {
            viewQuilt.setVisible(false);
        } else {
            viewStream.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_view_stream: {
                showLinearList();
                mPreferences.setIsViewQuilt(false);
                invalidateOptionsMenu();
                return true;
            }
            case R.id.action_view_quilt: {
                showStaggeredList();
                mPreferences.setIsViewQuilt(true);
                invalidateOptionsMenu();
                return true;
            }
            case R.id.action_delete_all: {
                showConfirmDeleteAllNotesDialog();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showLinearList() {
        LayoutManager layoutManager = new LinearLayoutManager(this);
        mListNotes.setLayoutManager(layoutManager);
    }

    private void showStaggeredList() {
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(LIST_SPAN_COUNT, LinearLayout.VERTICAL);
        mListNotes.setLayoutManager(layoutManager);
    }

    private void showConfirmDeleteAllNotesDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.dialog_msg_del_all_notes);
        dialog.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        dialog.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAllNotes();
            }
        });
        dialog.show();
    }

    private void deleteAllNotes() {
        int numNotesDeleted = getContentResolver().delete(
                NoteEntry.CONTENT_URI,
                null,
                null
        );

        if (numNotesDeleted != 0) {
            Toast.makeText(this, R.string.success_del_all_notes, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.error_del_all_notes, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case NOTES_LOADER_ID: {
                String[] projection = {
                        NoteEntry._ID,
                        NoteEntry.COLUMN_TITLE,
                        NoteEntry.COLUMN_NOTE,
                        NoteEntry.COLUMN_COLOR
                };

                return new CursorLoader(
                        this,
                        NoteEntry.CONTENT_URI,
                        projection,
                        null,
                        null,
                        null
                );
            }
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        loadNotes(cursor);
    }

    private void loadNotes(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                mNotesCount = cursor.getCount();

                ArrayList<Note> notes = new ArrayList<>();
                do {
                    int columnIndexId = cursor.getColumnIndex(NoteEntry._ID);
                    int columnIndexTitle = cursor.getColumnIndex(NoteEntry.COLUMN_TITLE);
                    int columnIndexNote = cursor.getColumnIndex(NoteEntry.COLUMN_NOTE);
                    int columnIndexColor = cursor.getColumnIndex(NoteEntry.COLUMN_COLOR);

                    int id = cursor.getInt(columnIndexId);
                    String title = cursor.getString(columnIndexTitle);
                    String note = cursor.getString(columnIndexNote);
                    int color = cursor.getInt(columnIndexColor);

                    notes.add(new Note(id, title, note, color));
                } while (cursor.moveToNext());

                mNoteAdapter.setNotes(notes);
                showNotes();
            }
        } else {
            mNotesCount = 0;
            showEmptyState();
        }

        invalidateOptionsMenu();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
