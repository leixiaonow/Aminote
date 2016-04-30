package com.gionee.note.app.view;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;
import com.gionee.aminote.R;
import com.gionee.note.app.NoteAppImpl;

public class TextLengthFilter implements InputFilter {
    private int mMaxLength;

    public TextLengthFilter(int maxLength) {
        this.mMaxLength = maxLength;
    }

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        int keep = this.mMaxLength - (dest.length() - (dend - dstart));
        if (keep <= 0) {
            Toast.makeText(NoteAppImpl.getContext(), NoteAppImpl.getContext().getString(R.string.max_content_input_mum_limit), 0).show();
            return "";
        } else if (keep >= end - start) {
            return null;
        } else {
            keep += start;
            if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                keep--;
                if (keep == start) {
                    Toast.makeText(NoteAppImpl.getContext(), NoteAppImpl.getContext().getString(R.string.max_content_input_mum_limit), 0).show();
                    return "";
                }
            }
            Toast.makeText(NoteAppImpl.getContext(), NoteAppImpl.getContext().getString(R.string.max_content_input_mum_limit), 0).show();
            return source.subSequence(start, keep);
        }
    }
}
