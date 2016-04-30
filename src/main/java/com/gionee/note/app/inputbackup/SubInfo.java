package com.gionee.note.app.inputbackup;

import com.gionee.note.app.dataupgrade.DataUpgrade;

public class SubInfo {
    private String mContent;
    private boolean mIsMedia;
    private String mMediaFilePath;
    private int mTime;

    public SubInfo(String content, boolean isMedia) {
        this.mContent = content;
        this.mIsMedia = isMedia;
    }

    public String getContent() {
        return this.mContent;
    }

    public int getTime() {
        return this.mTime;
    }

    public boolean isMedia() {
        return this.mIsMedia;
    }

    public String getMediaFilePath() {
        return this.mMediaFilePath;
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
}
