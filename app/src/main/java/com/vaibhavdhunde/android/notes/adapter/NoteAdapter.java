package com.vaibhavdhunde.android.notes.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vaibhavdhunde.android.notes.EditorActivity;
import com.vaibhavdhunde.android.notes.R;
import com.vaibhavdhunde.android.notes.model.Note;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private ArrayList<Note> mNotes;

    public ArrayList<Note> getNotes() {
        return mNotes;
    }

    public void setNotes(ArrayList<Note> notes) {
        if (notes != null) {
            mNotes = notes;
            notifyDataSetChanged();
        }
    }

    @SuppressWarnings("WeakerAccess")
    public class NoteViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.note_container)
        LinearLayout mNoteContainer;

        @BindView(R.id.tv_title)
        TextView mDisplayTitle;

        @BindView(R.id.tv_note)
        TextView mDisplayNote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        View noteView = LayoutInflater.from(context)
                .inflate(R.layout.item_note, parent, false);

        NoteViewHolder noteViewHolder = new NoteViewHolder(noteView);
        applyTypeface(context, noteViewHolder);

        return noteViewHolder;
    }

    private void applyTypeface(Context context, NoteViewHolder noteViewHolder) {
        Typeface typefaceRobotoSlab = Typeface.createFromAsset(
                context.getAssets(),
                context.getString(R.string.typeface_roboto_slab)
        );
        noteViewHolder.mDisplayNote.setTypeface(typefaceRobotoSlab);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int position) {
        Note currentNote = getNotes().get(position);

        noteViewHolder.mDisplayTitle.setText(currentNote.getTitle());
        noteViewHolder.mDisplayNote.setText(currentNote.getNote());

        int noteColor = currentNote.getColor();
        noteViewHolder.mNoteContainer.setBackgroundColor(noteColor);
    }

    @Override
    public int getItemCount() {
        if (mNotes != null) {
            return mNotes.size();
        } else {
            return 0;
        }
    }
}
