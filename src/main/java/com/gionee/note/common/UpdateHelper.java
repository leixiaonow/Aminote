package com.gionee.note.common;

import android.text.TextUtils;
import com.gionee.note.data.LocalNoteItem;
import java.util.ArrayList;

public class UpdateHelper {
    private boolean mUpdated = false;

    public int update(int original, int update) {
        if (original == update) {
            return original;
        }
        this.mUpdated = true;
        return update;
    }

    public long update(long original, long update) {
        if (original == update) {
            return original;
        }
        this.mUpdated = true;
        return update;
    }

    public String update(ArrayList<Integer> original, String update) {
        if (TextUtils.isEmpty(update)) {
            if (original != null && original.size() > 0) {
                this.mUpdated = true;
            }
            return null;
        } else if (original == null || original.size() == 0) {
            this.mUpdated = true;
            return update;
        } else {
            StringBuilder builder = new StringBuilder();
            int length = original.size();
            for (int i = 0; i < length; i++) {
                builder.append(original.get(i));
                if (i != length - 1) {
                    builder.append(LocalNoteItem.LABEL_SEPARATOR);
                }
            }
            if (TextUtils.equals(builder.toString(), update)) {
                return update;
            }
            this.mUpdated = true;
            return update;
        }
    }

    public ArrayList<Integer> update(ArrayList<Integer> original, ArrayList<Integer> update) {
        int i;
        StringBuilder originBuilder = new StringBuilder();
        int length = original.size();
        for (i = 0; i < length; i++) {
            originBuilder.append(original.get(i));
            if (i != length - 1) {
                originBuilder.append(LocalNoteItem.LABEL_SEPARATOR);
            }
        }
        StringBuilder updateBuilder = new StringBuilder();
        length = update.size();
        for (i = 0; i < length; i++) {
            updateBuilder.append(update.get(i));
            if (i != length - 1) {
                updateBuilder.append(LocalNoteItem.LABEL_SEPARATOR);
            }
        }
        if (!TextUtils.equals(originBuilder.toString(), updateBuilder.toString())) {
            this.mUpdated = true;
            original.clear();
            original.addAll(update);
        }
        return original;
    }

    public <T> T update(T original, T update) {
        if (NoteUtils.equals(original, update)) {
            return original;
        }
        this.mUpdated = true;
        return update;
    }

    public boolean isUpdated() {
        return this.mUpdated;
    }
}
