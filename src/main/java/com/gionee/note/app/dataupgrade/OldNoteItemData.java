package com.gionee.note.app.dataupgrade;

import com.gionee.note.common.NoteUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

class OldNoteItemData {
    private long mAlarmTime;
    private String mContent;
    private long mCreateTime;
    private int mId;
    private String mJsonContent;
    private String mLabelId;
    private String mNoteTile;
    private String mParentFile;
    private ArrayList<SubData> mSubs;

    public OldNoteItemData(int id, String content, String cDate, String cTime, long aTime, String parentFile, String noteTile) {
        this.mId = id;
        this.mContent = content;
        this.mAlarmTime = aTime;
        this.mParentFile = parentFile;
        this.mNoteTile = noteTile;
        String[] ymd = cDate.split("-");
        String[] hms = cTime.split(DataUpgrade.SPLIT);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(ymd[0]), Integer.parseInt(ymd[1]) - 1, Integer.parseInt(ymd[2]), Integer.parseInt(hms[0]), Integer.parseInt(hms[1]), Integer.parseInt(hms[2]));
        this.mCreateTime = calendar.getTimeInMillis();
    }

    public int getId() {
        return this.mId;
    }

    public long getAlarmTime() {
        return this.mAlarmTime;
    }

    public long getCreateTime() {
        return this.mCreateTime;
    }

    public String getContent() {
        return this.mContent;
    }

    public void setLabel(String labelId) {
        this.mLabelId = labelId;
    }

    public String getLabelId() {
        return this.mLabelId;
    }

    public int getFolderId() {
        if (this.mParentFile == null || DataUpgrade.NO.equals(this.mParentFile)) {
            return -1;
        }
        return Integer.parseInt(this.mParentFile);
    }

    public void setJsonContent(String jsonContent) {
        this.mJsonContent = jsonContent;
    }

    public String getJsonContent() {
        return this.mJsonContent;
    }

    public String getNoteTile() {
        return this.mNoteTile != null ? this.mNoteTile : "";
    }

    public void resolveMedia(ArrayList<String> mediaFileNames) {
        ArrayList<SubData> subDatas = getSubDatas(this.mContent, DataUpgrade.PREFIX, DataUpgrade.SUFFIX);
        if (subDatas != null) {
            Iterator i$ = subDatas.iterator();
            while (i$.hasNext()) {
                SubData subData = (SubData) i$.next();
                if (subData.isMedia()) {
                    subData.resolveMedia(mediaFileNames);
                }
            }
            this.mSubs = subDatas;
        }
    }

    public void resolveMedia() {
        ArrayList<SubData> subDatas = getSubDatas(this.mContent, DataUpgrade.PREFIX, DataUpgrade.SUFFIX);
        if (subDatas != null) {
            Iterator i$ = subDatas.iterator();
            while (i$.hasNext()) {
                SubData subData = (SubData) i$.next();
                if (subData.isMedia()) {
                    subData.resolveMedia();
                }
            }
            this.mSubs = subDatas;
        }
    }

    public ArrayList<SubData> getSubs() {
        return this.mSubs;
    }

    public String toString() {
        return "item mId = " + this.mId + ",mContent = " + this.mContent + ",mCreateTime = " + this.mCreateTime + ",mAlarmTime = " + this.mAlarmTime + ",mParentFile = " + this.mParentFile + ",mNoteTile = " + this.mNoteTile + ",mLabelId = " + this.mLabelId;
    }

    private ArrayList<SubData> getSubDatas(String content, String prefix, String suffix) {
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
        ArrayList<SubData> subDatas = new ArrayList();
        int ps = 0;
        int size = prefixs.size();
        for (int i = 0; i < size; i++) {
            int start = ((Integer) prefixs.get(i)).intValue();
            int pe = start;
            if (pe > 0) {
                subDatas.add(new SubData(content.substring(ps, pe), false));
            }
            int end = ((Integer) suffixs.get(i)).intValue() + suffixLength;
            String str = content;
            subDatas.add(new SubData(str.substring(start + prefixLength, end - suffixLength), true));
            ps = end;
        }
        int length = content.length();
        if (ps >= length) {
            return subDatas;
        }
        subDatas.add(new SubData(content.substring(ps, length), false));
        return subDatas;
    }

    public static OldNoteItemData getNoteItemData(int noteId, ArrayList<OldNoteItemData> oldNoteItemDatas) {
        Iterator i$ = oldNoteItemDatas.iterator();
        while (i$.hasNext()) {
            OldNoteItemData noteItemData = (OldNoteItemData) i$.next();
            if (noteItemData.getId() == noteId) {
                return noteItemData;
            }
        }
        return null;
    }
}
