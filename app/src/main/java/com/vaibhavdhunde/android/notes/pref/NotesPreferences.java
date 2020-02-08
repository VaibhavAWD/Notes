package com.vaibhavdhunde.android.notes.pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class NotesPreferences {
    private static final String PREF_NAME = "notes_pref";

    private static final String KEY_IS_VIEW_SET = "key_is_view_set";
    private static final String KEY_IS_VIEW_QUILT = "key_is_view_quilt";

    private SharedPreferences mPref;
    private Editor mEditor;

    public NotesPreferences(Context context) {
        mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mPref.edit();
        mEditor.apply();
    }

    public void setIsViewQuilt(boolean isViewQuilt) {
        mEditor.putBoolean(KEY_IS_VIEW_SET, true);
        mEditor.putBoolean(KEY_IS_VIEW_QUILT, isViewQuilt);
        mEditor.commit();
    }

    public boolean isViewQuilt() {
        if (!mPref.getBoolean(KEY_IS_VIEW_SET, false)) {
            return true;
        } else {
            return mPref.getBoolean(KEY_IS_VIEW_QUILT, false);
        }
    }
}
