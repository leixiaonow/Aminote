package com.gionee.note.app;

import amigoui.widget.AmigoCheckBox;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.gionee.note.app.view.NoteCardBottomView;
import com.gionee.note.data.Path;

public class NoteViewHolder extends ViewHolder {
    public AmigoCheckBox mCheckBox;
    public TextView mContent;
    public ImageView mImage = null;
    public NoteCardBottomView mNoteCardBottomView;
    public Path mPath;
    public ImageView mReminder;
    public TextView mTime;
    public TextView mTitle;

    public NoteViewHolder(View itemView) {
        super(itemView);
    }
}
