package com.gionee.note.app;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import com.gionee.framework.component.BaseApplication;
import com.gionee.note.app.inputbackup.ImportBackUp;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.common.StatisticsModule;
import com.gionee.note.common.ThreadPool;
import com.gionee.note.data.DataManager;
import com.gionee.note.widget.WidgetUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import java.util.ArrayList;
import java.util.Iterator;

public class NoteAppImpl extends BaseApplication {
    private static NoteAppImpl mApp;
    private ArrayList<NoteDbInitCompleteNotify> mCurrentNotifys = new ArrayList();
    private DataManager mDataManager;
    private ImportBackUp mImportBackUp;
    private LabelManager mLabelManager;
    private Looper mSaveNoteDataLooper;
    private ThreadPool mThreadPool;

    public void onCreate() {
        super.onCreate();
        initContext();
        NoteUtils.initScreenSize(this);
        initThreadPool();
        initLabelManager();
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        StatisticsModule.init(this);
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        enableLog(0);
    }

    private void initContext() {
        mApp = this;
    }

    private void initLabelManager() {
        this.mLabelManager = new LabelManager(this);
        this.mLabelManager.init();
    }

    private void initThreadPool() {
        this.mThreadPool = new ThreadPool();
    }

    public ThreadPool getThreadPool() {
        return this.mThreadPool;
    }

    public synchronized DataManager getDataManager() {
        if (this.mDataManager == null) {
            this.mDataManager = new DataManager(this);
            this.mDataManager.initializeSourceMap();
        }
        return this.mDataManager;
    }

    public LabelManager getLabelManager() {
        return this.mLabelManager;
    }

    public static NoteAppImpl getContext() {
        return mApp;
    }

    public synchronized Looper getSaveNoteDataLooper() {
        if (this.mSaveNoteDataLooper == null) {
            HandlerThread handlerThread = new HandlerThread("save note data");
            handlerThread.start();
            this.mSaveNoteDataLooper = handlerThread.getLooper();
        }
        return this.mSaveNoteDataLooper;
    }

    public ImportBackUp getImportBackUp() {
        synchronized (this) {
            if (this.mImportBackUp == null) {
                this.mImportBackUp = new ImportBackUp();
            }
        }
        this.mImportBackUp.resetEnv();
        return this.mImportBackUp;
    }

    public void registerNoteDbInitCompleteNotify(NoteDbInitCompleteNotify notify) {
        if (!this.mCurrentNotifys.contains(notify)) {
            this.mCurrentNotifys.add(notify);
        }
    }

    public void unRegisterNoteDbInitCompleteNotify(NoteDbInitCompleteNotify notify) {
        this.mCurrentNotifys.remove(notify);
    }

    public void notifyDbInitComplete() {
        WidgetUtil.updateAllWidgets();
        Iterator it = this.mCurrentNotifys.iterator();
        while (it.hasNext()) {
            ((NoteDbInitCompleteNotify) it.next()).onNoteDbInitComplete();
        }
    }
}
