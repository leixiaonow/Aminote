package com.gionee.note.app.inputbackup;

import com.gionee.note.app.dataupgrade.DataUpgrade;
import com.gionee.note.common.NoteUtils;
import java.util.ArrayList;
import java.util.Iterator;

class OldNoteInfo {
    private String mContent;
    private long mId;
    private String mLabel;
    private String mLabelId;
    private String mNoteTitle;
    private ArrayList<SubInfo> mSubs;

    public OldNoteInfo(long id, String noteTitle, String content, String label) {
        this.mId = id;
        this.mNoteTitle = noteTitle;
        this.mContent = content;
        this.mLabel = label;
    }

    public long getId() {
        return this.mId;
    }

    public String getTitle() {
        return this.mNoteTitle;
    }

    public String getLabel() {
        return this.mLabel;
    }

    public String getLabelId() {
        return this.mLabelId;
    }

    public void setLabelId(String labelId) {
        this.mLabelId = labelId;
    }

    public ArrayList<SubInfo> getSubInfos() {
        return this.mSubs;
    }

    public String getContent() {
        return this.mContent;
    }

    public void resolveMedia() {
        ArrayList<SubInfo> subInfos = getSubInfos(this.mContent, DataUpgrade.PREFIX, DataUpgrade.SUFFIX);
        if (subInfos != null) {
            Iterator i$ = subInfos.iterator();
            while (i$.hasNext()) {
                SubInfo subData = (SubInfo) i$.next();
                if (subData.isMedia()) {
                    subData.resolveMedia();
                }
            }
            this.mSubs = subInfos;
        }
    }

    private ArrayList<SubInfo> getSubInfos(String content, String prefix, String suffix) {
        ArrayList<Integer> prefixs = NoteUtils.indexofs(content, prefix);
        if (prefixs == null) {
            return null;
        }
        ArrayList<Integer> suffixs = NoteUtils.indexofs(content, suffix);
        if (suffixs == null) {
            return null;
        }
        if (prefixs.size() != suffixs.size()) {
            return null;
        }
        int prefixLength = prefix.length();
        int suffixLength = suffix.length();
        ArrayList<SubInfo> subInfos = new ArrayList();
        int ps = 0;
        int size = prefixs.size();
        for (int i = 0; i < size; i++) {
            int start = ((Integer) prefixs.get(i)).intValue();
            int pe = start;
            if (pe > 0) {
                subInfos.add(new SubInfo(content.substring(ps, pe), false));
            }
            int end = ((Integer) suffixs.get(i)).intValue() + suffixLength;
            String str = content;
            subInfos.add(new SubInfo(str.substring(start + prefixLength, end - suffixLength), true));
            ps = end;
        }
        int length = content.length();
        if (ps >= length) {
            return subInfos;
        }
        subInfos.add(new SubInfo(content.substring(ps, length), false));
        return subInfos;
    }
}
