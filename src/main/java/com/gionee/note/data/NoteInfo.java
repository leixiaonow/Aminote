package com.gionee.note.data;

import java.util.ArrayList;

public class NoteInfo {
    public String mContent;
    public long mDateCreatedInMs;
    public long mDateModifiedInMs;
    public long mDateReminderInMs = 0;
    public volatile int mId = -1;
    public ArrayList<Integer> mLabel = new ArrayList();
    public String mTitle;
}
