package com.gionee.note.app.dataupgrade;

import java.util.ArrayList;
import java.util.Iterator;

class OldNoteFolderData {
    private String mName;
    private int mNewId;
    private int mOldId;

    public OldNoteFolderData(int id, String name) {
        this.mOldId = id;
        this.mName = name;
    }

    public int getOldId() {
        return this.mOldId;
    }

    public int getNewId() {
        return this.mNewId;
    }

    public void setNewId(int id) {
        this.mNewId = id;
    }

    public String getName() {
        return this.mName;
    }

    public String toString() {
        return "folder oldId = " + this.mOldId + ",name = " + this.mName + ",newId = " + this.mNewId;
    }

    public static OldNoteFolderData getNoteFolderData(int folderId, ArrayList<OldNoteFolderData> oldNoteFolderDatas) {
        Iterator i$ = oldNoteFolderDatas.iterator();
        while (i$.hasNext()) {
            OldNoteFolderData noteFolderData = (OldNoteFolderData) i$.next();
            if (noteFolderData.getOldId() == folderId) {
                return noteFolderData;
            }
        }
        return null;
    }
}
