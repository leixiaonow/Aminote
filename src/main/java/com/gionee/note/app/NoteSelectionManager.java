package com.gionee.note.app;

import amigoui.preference.AmigoPreference;
import com.gionee.note.data.NoteItem;
import com.gionee.note.data.NoteSet;
import com.gionee.note.data.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NoteSelectionManager {
    public static final int CANCEL_ALL_MODE = 4;
    private static final boolean DEBUG = false;
    public static final int ENTER_SELECTION_MODE = 1;
    public static final int LEAVE_SELECTION_MODE = 2;
    public static final int SELECT_ALL_MODE = 3;
    private static final String TAG = "NoteSelectionManager";
    private Set<Path> mClickedSet = new HashSet();
    private boolean mInSelectionMode;
    private boolean mInverseSelection;
    private SelectionListener mListener;
    private NoteSet mSourceNoteSet;
    private long mSourceVersion;
    private int mTotal = -1;

    public interface SelectionListener {
        void onSelectionChange(Path path, boolean z);

        void onSelectionModeChange(int i);
    }

    public void deSelectAll() {
        this.mInverseSelection = false;
        this.mClickedSet.clear();
        if (this.mListener != null) {
            this.mListener.onSelectionModeChange(4);
        }
    }

    public void enterSelectionMode() {
        if (!this.mInSelectionMode) {
            this.mInSelectionMode = true;
            if (this.mListener != null) {
                this.mListener.onSelectionModeChange(1);
            }
        }
    }

    public ArrayList<Path> getSelected() {
        return getSelected(AmigoPreference.DEFAULT_ORDER);
    }

    public ArrayList<Path> getSelected(int maxSelection) {
        ArrayList<Path> selected = new ArrayList();
        Iterator i$;
        Path id;
        if (this.mInverseSelection) {
            int total = getTotalCount();
            int index = 0;
            while (index < total) {
                int count = Math.min(total - index, 50);
                i$ = this.mSourceNoteSet.getNoteItem(index, count).iterator();
                while (i$.hasNext()) {
                    id = ((NoteItem) i$.next()).getPath();
                    if (!this.mClickedSet.contains(id)) {
                        selected.add(id);
                        if (selected.size() > maxSelection) {
                            return null;
                        }
                    }
                }
                index += count;
            }
            return selected;
        }
        for (Path id2 : this.mClickedSet) {
            selected.add(id2);
            if (selected.size() > maxSelection) {
                return null;
            }
        }
        return selected;
    }

    private int getTotalCount() {
        if (this.mSourceNoteSet == null) {
            return 0;
        }
        long sourceVersion = this.mSourceNoteSet.getDataVersion();
        if (this.mTotal < 0 || this.mSourceVersion != sourceVersion) {
            this.mSourceVersion = sourceVersion;
            this.mTotal = this.mSourceNoteSet.getNoteItemCount();
        }
        return this.mTotal;
    }

    public int getSelectedCount() {
        int count = this.mClickedSet.size();
        if (this.mInverseSelection) {
            return getTotalCount() - count;
        }
        return count;
    }

    public boolean inSelectAllMode() {
        return this.mInverseSelection && this.mClickedSet.size() == 0;
    }

    public boolean inSelectionMode() {
        return this.mInSelectionMode;
    }

    public void leaveSelectionMode() {
        if (this.mInSelectionMode) {
            this.mInSelectionMode = false;
            this.mInverseSelection = false;
            this.mClickedSet.clear();
            if (this.mListener != null) {
                this.mListener.onSelectionModeChange(2);
            }
        }
    }

    public void setSelectionListener(SelectionListener listener) {
        this.mListener = listener;
    }

    public void setSourceMediaSet(NoteSet set) {
        this.mSourceNoteSet = set;
        this.mSourceVersion = set.getDataVersion();
        this.mTotal = -1;
    }

    public void selectAll() {
        this.mInverseSelection = true;
        this.mClickedSet.clear();
        this.mTotal = -1;
        enterSelectionMode();
        if (this.mListener != null) {
            this.mListener.onSelectionModeChange(3);
        }
    }

    public void toggle(Path path) {
        if (this.mClickedSet.contains(path)) {
            this.mClickedSet.remove(path);
        } else {
            enterSelectionMode();
            this.mClickedSet.add(path);
        }
        if (getSelectedCount() == getTotalCount()) {
            selectAll();
        }
        if (this.mListener != null) {
            this.mListener.onSelectionChange(path, isItemSelected(path));
        }
    }

    public boolean isItemSelected(Path itemId) {
        return this.mInverseSelection ^ this.mClickedSet.contains(itemId);
    }
}
