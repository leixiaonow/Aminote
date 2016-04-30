package com.gionee.note.app.dataupgrade;

import java.util.ArrayList;
import java.util.Iterator;

class SubData {
    private String mContent;
    private boolean mIsMedia;
    private String mMediaFilePath;
    private int mTime;

    public SubData(String content, boolean isMedia) {
        this.mContent = content;
        this.mIsMedia = isMedia;
    }

    public void resolveMedia(ArrayList<String> mediaFileNames) {
        if (this.mIsMedia) {
            String[] cs = this.mContent.split(DataUpgrade.SPLIT);
            String mediaName = cs[0];
            calculateTime(cs);
            String realFilePath = converMediaFilePath(mediaName, mediaFileNames);
            if (realFilePath == null) {
                realFilePath = mediaName;
            }
            this.mMediaFilePath = realFilePath;
        }
    }

    public void resolveMedia() {
        if (this.mIsMedia) {
            String[] cs = this.mContent.split(DataUpgrade.SPLIT);
            String mediaName = cs[0];
            calculateTime(cs);
            this.mMediaFilePath = mediaName;
        }
    }

    private void calculateTime(String[] cs) {
        this.mTime = (Integer.valueOf(cs[1]).intValue() * 60) + Integer.valueOf(cs[2]).intValue();
    }

    public boolean isMedia() {
        return this.mIsMedia;
    }

    public String getMediaFilePath() {
        return this.mMediaFilePath;
    }

    public int getTime() {
        return this.mTime;
    }

    public String getContent() {
        return this.mContent;
    }

    public String toString() {
        return "mContent = " + this.mContent + ",mIsMedia = " + this.mIsMedia + ",mMediaFilePath = " + this.mMediaFilePath;
    }

    private String converMediaFilePath(String name, ArrayList<String> mediaFileNames) {
        String filePath = null;
        Iterator i$ = mediaFileNames.iterator();
        while (i$.hasNext()) {
            String path = (String) i$.next();
            if (path.contains(name)) {
                filePath = path;
                break;
            }
        }
        if (filePath != null) {
            return filePath.substring(1, filePath.length());
        }
        return null;
    }
}
