package com.vaibhavdhunde.android.notes.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vaibhavdhunde.android.notes.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    private int[] mColors;

    public ColorAdapter(int[] colors) {
        mColors = colors;
    }

    public int[] getColors() {
        return mColors;
    }

    public class ColorViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.color_container)
        View mColorContainer;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View colorView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_color, parent, false);
        return new ColorViewHolder(colorView);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder colorViewHolder, int position) {
        int currentColor = getColors()[position];

        colorViewHolder.mColorContainer.setBackgroundColor(currentColor);
    }

    @Override
    public int getItemCount() {
        return mColors.length;
    }
}
