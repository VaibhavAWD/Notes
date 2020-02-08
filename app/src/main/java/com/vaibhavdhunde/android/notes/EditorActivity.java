package com.vaibhavdhunde.android.notes;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.vaibhavdhunde.android.notes.adapter.ColorAdapter;
import com.vaibhavdhunde.android.notes.data.NoteContract.NoteEntry;
import com.vaibhavdhunde.android.notes.util.RecyclerTouchListener;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressLint("ClickableViewAccessibility")
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURRENT_NOTE_LOADER_ID = 1;

    private static final String KEY_HAS_NOTE_CHANGED = "key_has_note_changed";
    private static final String KEY_NOTE_COLOR = "key_note_color";

    @BindView(R.id.note_editor_container)
    FrameLayout mNoteEditorContainer;

    @BindView(R.id.input_title)
    EditText mInputTitle;

    @BindView(R.id.input_note)
    EditText mInputNote;

    private String mTitle;
    private String mNote;
    private int mColor = 0;

    private Uri mCurrentNoteUri;

    private boolean mHasNoteChanged = false;

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mHasNoteChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);
        setTitleColor(R.color.secondary_text);

        Intent notesIntent = getIntent();
        mCurrentNoteUri = notesIntent.getData();

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_HAS_NOTE_CHANGED)) {
            mHasNoteChanged = savedInstanceState.getBoolean(KEY_HAS_NOTE_CHANGED);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_NOTE_COLOR)) {
            mColor = savedInstanceState.getInt(KEY_NOTE_COLOR);

            if (mCurrentNoteUri == null) {
                changeNoteTheme();
            }
        }

        if (mCurrentNoteUri != null) {
            setTitle(R.string.label_edit_note);
            getLoaderManager().initLoader(CURRENT_NOTE_LOADER_ID, null, this);
        } else {
            setTitle(R.string.label_add_new_note);
            invalidateOptionsMenu();
        }

        applyTypeface();

        mInputTitle.setOnTouchListener(mOnTouchListener);
        mInputNote.setOnTouchListener(mOnTouchListener);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        final int id = menuItem.getItemId();
                        switch (id) {
                            case R.id.action_more:
                                mHasNoteChanged = true;
                                showColorPickerDialog();
                                break;
                            case R.id.action_send:
                                sendNote();
                                break;
                        }
                        return true;
                    }
                });
    }

    private void applyTypeface() {
        Typeface typefaceRobotoSlab = Typeface.createFromAsset(
                getAssets(),
                getString(R.string.typeface_roboto_slab)
        );
        mInputNote.setTypeface(typefaceRobotoSlab);
    }

    private void changeNoteTheme() {
        mNoteEditorContainer.setBackgroundColor(mColor);

        int[] noteColors = getResources().getIntArray(R.array.note_colors);
        int[] hintColors = getResources().getIntArray(R.array.note_hint_colors);

        int hintColor = 0;
        for (int i = 0; i < noteColors.length; i++) {
            if (mColor == noteColors[i]) {
                hintColor = hintColors[i];
                break;
            }
        }

        mInputTitle.setHintTextColor(hintColor);
        mInputNote.setHintTextColor(hintColor);
    }

    private void sendNote() {
        if (isNoteValidTitle()) {
            return;
        }

        if (isNotValidNote()) {
            return;
        }

        String title = "Title:\n" + mTitle;
        String note = "Note:\n\n" + mNote;
        String message = title + "\n\n" + note;

        Intent sendNoteIntent = new Intent(Intent.ACTION_SEND);
        sendNoteIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendNoteIntent.setType(getString(R.string.note_mime_type));
        if (sendNoteIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(sendNoteIntent, getString(R.string.intent_label_send_note)));
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentNoteUri == null) {
            MenuItem delete = menu.findItem(R.id.action_delete);
            delete.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                if (mHasNoteChanged) {
                    showSaveChangesDialog();
                } else {
                    finish();
                }
                return true;
            }
            case R.id.action_save:
                saveNote();
                return true;
            case R.id.action_delete: {
                deleteNote();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveNote() {
        if (isNoteValidTitle()) {
            return;
        }

        if (isNotValidNote()) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(NoteEntry.COLUMN_TITLE, mTitle);
        values.put(NoteEntry.COLUMN_NOTE, mNote);
        values.put(NoteEntry.COLUMN_COLOR, mColor);

        Uri newNoteUri = null;
        int numNotesUpdated = 0;
        if (mCurrentNoteUri != null) {
            numNotesUpdated = getContentResolver().update(
                    mCurrentNoteUri,
                    values,
                    null,
                    null
            );
        } else {
            newNoteUri = getContentResolver().insert(NoteEntry.CONTENT_URI, values);
        }

        if (newNoteUri != null || numNotesUpdated != 0) {
            Toast.makeText(this, R.string.success_save_note, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.error_save_note, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNoteValidTitle() {
        mTitle = mInputTitle.getText().toString().trim();
        if (TextUtils.isEmpty(mTitle)) {
            Toast.makeText(this, R.string.error_empty_title, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }

    private boolean isNotValidNote() {
        mNote = mInputNote.getText().toString().trim();
        if (TextUtils.isEmpty(mNote)) {
            Toast.makeText(this, R.string.error_empty_note, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }

    private void deleteNote() {
        int numNotesDeleted = getContentResolver().delete(
                mCurrentNoteUri,
                null,
                null
        );

        if (numNotesDeleted != 0) {
            Toast.makeText(this, R.string.success_del_note, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.error_del_note, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (mHasNoteChanged) {
            showSaveChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showSaveChangesDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.dialog_msg_save_changes);
        dialog.setNegativeButton(R.string.action_keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        dialog.setPositiveButton(R.string.action_discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        dialog.show();
    }

    private void showColorPickerDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        View colorView = LayoutInflater.from(mNoteEditorContainer.getContext())
                .inflate(R.layout.list_color_picker, mNoteEditorContainer, false);

        RecyclerView listColors = colorView.findViewById(R.id.list_colors);
        listColors.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                this,
                LinearLayout.HORIZONTAL,
                false
        );
        listColors.setLayoutManager(layoutManager);

        int[] colors = getResources().getIntArray(R.array.note_colors);
        final ColorAdapter colorAdapter = new ColorAdapter(colors);
        listColors.setAdapter(colorAdapter);

        listColors.addOnItemTouchListener(new RecyclerTouchListener(
                this, listColors, new RecyclerTouchListener.OnClickListener() {
            @Override
            public void OnClick(int position) {
                mColor = colorAdapter.getColors()[position];
                changeNoteTheme();
            }

            @Override
            public void OnLongClick(int position) {

            }
        }));

        dialog.setView(colorView);
        dialog.setTitle(R.string.dialog_title_pick_a_color);

        dialog.setNeutralButton(R.string.action_set_color, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        dialog.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_HAS_NOTE_CHANGED, mHasNoteChanged);

        if (mColor != 0) {
            outState.putInt(KEY_NOTE_COLOR, mColor);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case CURRENT_NOTE_LOADER_ID: {
                String[] projection = {
                        NoteEntry._ID,
                        NoteEntry.COLUMN_TITLE,
                        NoteEntry.COLUMN_NOTE,
                        NoteEntry.COLUMN_COLOR
                };

                return new CursorLoader(
                        this,
                        mCurrentNoteUri,
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
        loadNote(cursor);
    }

    private void loadNote(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                int columnIndexTitle = cursor.getColumnIndex(NoteEntry.COLUMN_TITLE);
                int columnIndexNote = cursor.getColumnIndex(NoteEntry.COLUMN_NOTE);
                int columnIndexColor = cursor.getColumnIndex(NoteEntry.COLUMN_COLOR);

                String title = cursor.getString(columnIndexTitle);
                mInputTitle.setText(title);

                String note = cursor.getString(columnIndexNote);
                mInputNote.setText(note);

                if (mColor == 0) {
                    mColor = cursor.getInt(columnIndexColor);
                }
                changeNoteTheme();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
