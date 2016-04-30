package com.gionee.feedback.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.gionee.feedback.logic.vo.AppData;
import com.gionee.feedback.logic.vo.FeedbackInfo;
import com.gionee.feedback.logic.vo.ReplyInfo;
import com.gionee.feedback.net.IAppData;
import com.gionee.feedback.utils.Log;
import com.gionee.feedback.utils.Utils;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DBManager implements IDBManager<FeedbackInfo>, ISubject {
    private static final String DESC = " desc";
    private static final String TAG = "DBManager";
    private static Context sAppContext;
    private DatabaseHelper mDBHelper;
    private List<WeakReference<DataChangeObserver>> mDataChangeObserver;
    private SQLiteDatabase mDatabase;
    private List<FeedbackInfo> mFeedbackInfos;
    private Object mLock;

    private static class DBManagerHolder {
        public static final DBManager INSTANCE = new DBManager();

        private DBManagerHolder() {
        }
    }

    private java.util.List<com.gionee.feedback.logic.vo.FeedbackInfo> getALLRecords() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x004f in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
*/
        /*
        r12 = this;
        r10 = new java.util.ArrayList;
        r10.<init>();
        r8 = 0;
        r0 = r12.mDatabase;	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r1 = "message";	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r2 = 0;	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r3 = 0;	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r4 = 0;	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r5 = 0;	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r6 = 0;	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r7 = "send_time desc";	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r8 = r0.query(r1, r2, r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r0 = "DBManager";	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r1.<init>();	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r2 = "getALLRecords ";	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r1 = r1.append(r8);	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r2 = ":";	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r2 = r8.getCount();	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r1 = r1.toString();	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        com.gionee.feedback.utils.Log.d(r0, r1);	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        r10 = r12.parseFeedbackInfos(r8);	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        if (r8 == 0) goto L_0x0044;
    L_0x0041:
        r8.close();
    L_0x0044:
        r11 = r10;
    L_0x0045:
        return r11;
    L_0x0046:
        r9 = move-exception;
        r9.printStackTrace();	 Catch:{ Exception -> 0x0046, all -> 0x0051 }
        if (r8 == 0) goto L_0x004f;
    L_0x004c:
        r8.close();
    L_0x004f:
        r11 = r10;
        goto L_0x0045;
    L_0x0051:
        r0 = move-exception;
        if (r8 == 0) goto L_0x0057;
    L_0x0054:
        r8.close();
    L_0x0057:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.gionee.feedback.db.DBManager.getALLRecords():java.util.List<com.gionee.feedback.logic.vo.FeedbackInfo>");
    }

    private com.gionee.feedback.logic.vo.FeedbackInfo getDBInfoByCID(long r12) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0026 in list [B:5:0x0023]
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
*/
        /*
        r11 = this;
        r10 = 0;
        r8 = 0;
        r0 = r11.mDatabase;	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r1 = "message";	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r2 = 0;	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r3 = "content_id = ?";	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r4 = 1;	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r5 = 0;	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r6 = java.lang.String.valueOf(r12);	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r4[r5] = r6;	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r5 = 0;	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r6 = 0;	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r7 = 0;	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r8 = r0.query(r1, r2, r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r8.moveToFirst();	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r0 = r11.getFeedbackInfo(r8);	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        if (r8 == 0) goto L_0x0026;
    L_0x0023:
        r8.close();
    L_0x0026:
        return r0;
    L_0x0027:
        r9 = move-exception;
        r0 = "DBManager";	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r1.<init>();	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r2 = "ERROR:";	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r2 = r9.getMessage();	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        r1 = r1.toString();	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        com.gionee.feedback.utils.Log.d(r0, r1);	 Catch:{ Exception -> 0x0027, all -> 0x004b }
        if (r8 == 0) goto L_0x0049;
    L_0x0046:
        r8.close();
    L_0x0049:
        r0 = r10;
        goto L_0x0026;
    L_0x004b:
        r0 = move-exception;
        if (r8 == 0) goto L_0x0051;
    L_0x004e:
        r8.close();
    L_0x0051:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.gionee.feedback.db.DBManager.getDBInfoByCID(long):com.gionee.feedback.logic.vo.FeedbackInfo");
    }

    private java.util.List<com.gionee.feedback.logic.vo.ReplyInfo> getReplyInfosByCID(long r14) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0033 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
*/
        /*
        r13 = this;
        r10 = new java.util.ArrayList;
        r10.<init>();
        r8 = 0;
        r0 = r13.mDatabase;	 Catch:{ Exception -> 0x002a, all -> 0x0035 }
        r1 = "reply";	 Catch:{ Exception -> 0x002a, all -> 0x0035 }
        r2 = 0;	 Catch:{ Exception -> 0x002a, all -> 0x0035 }
        r3 = "content_id = ?";	 Catch:{ Exception -> 0x002a, all -> 0x0035 }
        r4 = 1;	 Catch:{ Exception -> 0x002a, all -> 0x0035 }
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x002a, all -> 0x0035 }
        r5 = 0;	 Catch:{ Exception -> 0x002a, all -> 0x0035 }
        r6 = java.lang.String.valueOf(r14);	 Catch:{ Exception -> 0x002a, all -> 0x0035 }
        r4[r5] = r6;	 Catch:{ Exception -> 0x002a, all -> 0x0035 }
        r5 = 0;	 Catch:{ Exception -> 0x002a, all -> 0x0035 }
        r6 = 0;	 Catch:{ Exception -> 0x002a, all -> 0x0035 }
        r7 = "reply_time";	 Catch:{ Exception -> 0x002a, all -> 0x0035 }
        r8 = r0.query(r1, r2, r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x002a, all -> 0x0035 }
        r10 = r13.parseReplyInfos(r14, r8);	 Catch:{ Exception -> 0x002a, all -> 0x0035 }
        if (r8 == 0) goto L_0x0028;
    L_0x0025:
        r8.close();
    L_0x0028:
        r11 = r10;
    L_0x0029:
        return r11;
    L_0x002a:
        r9 = move-exception;
        r9.printStackTrace();	 Catch:{ Exception -> 0x002a, all -> 0x0035 }
        if (r8 == 0) goto L_0x0033;
    L_0x0030:
        r8.close();
    L_0x0033:
        r11 = r10;
        goto L_0x0029;
    L_0x0035:
        r0 = move-exception;
        if (r8 == 0) goto L_0x003b;
    L_0x0038:
        r8.close();
    L_0x003b:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.gionee.feedback.db.DBManager.getReplyInfosByCID(long):java.util.List<com.gionee.feedback.logic.vo.ReplyInfo>");
    }

    private boolean isExistInDB(java.lang.String r7, java.lang.String[] r8) {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:79)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
*/
        /*
        r6 = this;
        r3 = 1;
        r4 = 0;
        r1 = 0;
        r5 = r6.mDatabase;	 Catch:{ Exception -> 0x0024, all -> 0x002e }
        r1 = r5.rawQuery(r7, r8);	 Catch:{ Exception -> 0x0024, all -> 0x002e }
        if (r1 != 0) goto L_0x0011;
    L_0x000b:
        if (r1 == 0) goto L_0x0010;
    L_0x000d:
        r1.close();
    L_0x0010:
        return r4;
    L_0x0011:
        r1.moveToFirst();	 Catch:{ Exception -> 0x0024, all -> 0x002e }
        r5 = 0;	 Catch:{ Exception -> 0x0024, all -> 0x002e }
        r0 = r1.getInt(r5);	 Catch:{ Exception -> 0x0024, all -> 0x002e }
        if (r0 < r3) goto L_0x0022;
    L_0x001b:
        if (r1 == 0) goto L_0x0020;
    L_0x001d:
        r1.close();
    L_0x0020:
        r4 = r3;
        goto L_0x0010;
    L_0x0022:
        r3 = r4;
        goto L_0x001b;
    L_0x0024:
        r2 = move-exception;
        r2.printStackTrace();	 Catch:{ Exception -> 0x0024, all -> 0x002e }
        if (r1 == 0) goto L_0x0010;
    L_0x002a:
        r1.close();
        goto L_0x0010;
    L_0x002e:
        r3 = move-exception;
        if (r1 == 0) goto L_0x0034;
    L_0x0031:
        r1.close();
    L_0x0034:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.gionee.feedback.db.DBManager.isExistInDB(java.lang.String, java.lang.String[]):boolean");
    }

    public static synchronized DBManager getInstance(Context context) {
        DBManager dBManager;
        synchronized (DBManager.class) {
            sAppContext = context;
            dBManager = DBManagerHolder.INSTANCE;
        }
        return dBManager;
    }

    private DBManager() {
        this.mDBHelper = null;
        this.mDatabase = null;
        this.mDataChangeObserver = null;
        this.mLock = null;
        this.mDBHelper = new DatabaseHelper(sAppContext);
        this.mDatabase = this.mDBHelper.getWritableDatabase();
        this.mDataChangeObserver = new ArrayList();
        this.mFeedbackInfos = getALLRecords();
        this.mLock = new Object();
    }

    public List<WeakReference<DataChangeObserver>> getDataChangeObservers() {
        return this.mDataChangeObserver;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long insert(com.gionee.feedback.logic.vo.FeedbackInfo r7) {
        /*
        r6 = this;
        r2 = -1;
        r1 = r6.mDatabase;
        r1.beginTransaction();
        r2 = r6.insertFeedbackInfo(r7);	 Catch:{ Exception -> 0x001f }
        r4 = 0;
        r1 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));
        if (r1 <= 0) goto L_0x0019;
    L_0x0011:
        r6.notifyObservers();	 Catch:{ Exception -> 0x001f }
        r1 = r6.mDatabase;	 Catch:{ Exception -> 0x001f }
        r1.setTransactionSuccessful();	 Catch:{ Exception -> 0x001f }
    L_0x0019:
        r1 = r6.mDatabase;
        r1.endTransaction();
    L_0x001e:
        return r2;
    L_0x001f:
        r0 = move-exception;
        r0.printStackTrace();	 Catch:{ all -> 0x0029 }
        r1 = r6.mDatabase;
        r1.endTransaction();
        goto L_0x001e;
    L_0x0029:
        r1 = move-exception;
        r4 = r6.mDatabase;
        r4.endTransaction();
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.gionee.feedback.db.DBManager.insert(com.gionee.feedback.logic.vo.FeedbackInfo):long");
    }

    private long insertFeedbackInfo(FeedbackInfo feedbackInfo) {
        if (feedbackInfo == null) {
            return -1;
        }
        long index = this.mDatabase.insert("message", null, buildMessageContentValues(feedbackInfo));
        Log.d(TAG, "index = " + index);
        feedbackInfo.setID(index);
        if (index <= -1) {
            return index;
        }
        updateAddCacheInfos(feedbackInfo);
        return index;
    }

    private void updateAddCacheInfos(FeedbackInfo feedbackInfo) {
        try {
            FeedbackInfo info = (FeedbackInfo) Utils.deepCopy(feedbackInfo);
            int position = getPositionById(info.getID());
            synchronized (this.mLock) {
                if (position > -1) {
                    this.mFeedbackInfos.remove(position);
                    this.mFeedbackInfos.add(position, info);
                } else {
                    this.mFeedbackInfos.add(0, info);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "updateAddCacheInfos IOException " + e.getMessage());
        } catch (ClassNotFoundException e2) {
            Log.e(TAG, "updateAddCacheInfos ClassNotFoundException " + e2.getMessage());
        }
    }

    private void updateDeleteCacheInfo(long mFeedDbID) {
        int posiion = getPositionById(mFeedDbID);
        synchronized (this.mLock) {
            if (posiion > -1) {
                this.mFeedbackInfos.remove(posiion);
            }
        }
    }

    private ContentValues buildReplyContentValues(ReplyInfo replyInfo) {
        ContentValues replyValues = new ContentValues();
        replyValues.put("content_id", Long.valueOf(replyInfo.getContentID()));
        replyValues.put(ReplyImpl.IS_READ, String.valueOf(replyInfo.isReaded()));
        replyValues.put(ReplyImpl.REPLY_CONTENT, replyInfo.getReplyContent());
        replyValues.put(ReplyImpl.REPLY_ID, Long.valueOf(replyInfo.getReplyID()));
        replyValues.put(ReplyImpl.REPLY_PERSON, replyInfo.getReplyPerson());
        replyValues.put(ReplyImpl.REPLY_TIME, Long.valueOf(replyInfo.getReplyTime()));
        return replyValues;
    }

    private ContentValues buildMessageContentValues(FeedbackInfo info) {
        ContentValues messageValues = new ContentValues();
        messageValues.put("content_id", Long.valueOf(info.getContentID()));
        messageValues.put("content", info.getContent());
        messageValues.put(MessageImpl.USER_CONTACT, info.getUserContact());
        messageValues.put(MessageImpl.SEND_TIME, info.getSendTime());
        messageValues.put(MessageImpl.ATTACHS, info.getAttachTexts());
        return messageValues;
    }

    private boolean isReplyInfoExistInDB(ReplyInfo replyInfo) {
        return isExistInDB("select count(*) from reply where reply_id = ? and content_id = ?", new String[]{String.valueOf(replyInfo.getReplyID()), String.valueOf(replyInfo.getContentID())});
    }

    public void delete(FeedbackInfo... infos) {
        this.mDatabase.beginTransaction();
        try {
            for (FeedbackInfo feedbackInfo : infos) {
                if (feedbackInfo != null) {
                    long mId = feedbackInfo.getID();
                    Log.d(TAG, "mId = " + mId);
                    if (mId != -1) {
                        long index = (long) this.mDatabase.delete("message", "_id = ?", new String[]{String.valueOf(mId)});
                        if (feedbackInfo.getContentID() > -1) {
                            index = (long) this.mDatabase.delete("reply", "content_id = ?", new String[]{String.valueOf(feedbackInfo.getContentID())});
                        }
                        if (index > -1) {
                            updateDeleteCacheInfo(feedbackInfo.getID());
                        }
                    }
                }
            }
            notifyObservers();
            this.mDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.mDatabase.endTransaction();
        }
    }

    private int getPositionById(long feedDbID) {
        for (int i = 0; i < this.mFeedbackInfos.size(); i++) {
            FeedbackInfo feedbackInfo = (FeedbackInfo) this.mFeedbackInfos.get(i);
            if (feedbackInfo != null && feedDbID == feedbackInfo.getID()) {
                return i;
            }
        }
        return -1;
    }

    public synchronized void update(FeedbackInfo... infos) {
        this.mDatabase.beginTransaction();
        long index = -1;
        try {
            for (FeedbackInfo feedbackInfo : infos) {
                FeedbackInfo feedbackInfo2;
                if (feedbackInfo2 != null) {
                    if (feedbackInfo2.getID() > 0) {
                        index = updateFeedbackInfo(feedbackInfo2);
                        if (!(feedbackInfo2.getReplyInfos() == null || feedbackInfo2.getReplyInfos().isEmpty())) {
                            index = updateReplyInfos(feedbackInfo2);
                        }
                    } else {
                        FeedbackInfo feedInfo = getDBInfoByCID(feedbackInfo2.getContentID());
                        if (!(feedInfo == null || feedbackInfo2.getContentID() == feedInfo.getContentID())) {
                            index = updateFeedbackInfo(feedbackInfo2);
                        }
                        if (feedInfo != null) {
                            index = updateReplyInfos(feedbackInfo2);
                        }
                    }
                    Log.d(TAG, "index = " + index);
                    if (index > -1) {
                        if (feedbackInfo2.getContentID() > 0) {
                            feedbackInfo2 = getDBInfoByCID(feedbackInfo2.getContentID());
                        }
                        updateAddCacheInfos(feedbackInfo2);
                    }
                }
            }
            if (index > 0) {
                notifyObservers();
                this.mDatabase.setTransactionSuccessful();
            }
            this.mDatabase.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            this.mDatabase.endTransaction();
        } catch (Throwable th) {
            this.mDatabase.endTransaction();
        }
        return;
    }

    private long updateFeedbackInfo(FeedbackInfo feedbackInfo) {
        return (long) this.mDatabase.update("message", buildMessageContentValues(feedbackInfo), "_id = ?", new String[]{String.valueOf(feedbackInfo.getID())});
    }

    private long updateReplyInfos(FeedbackInfo feedbackInfo) {
        List<ReplyInfo> replyInfos = feedbackInfo.getReplyInfos();
        long index = -1;
        if (replyInfos == null || replyInfos.isEmpty()) {
            return -1;
        }
        for (ReplyInfo replyInfo : replyInfos) {
            ContentValues replyValues = buildReplyContentValues(replyInfo);
            if (isReplyInfoExistInDB(replyInfo)) {
                index = (long) this.mDatabase.update("reply", replyValues, "reply_id = ? and content_id = ?", new String[]{String.valueOf(replyInfo.getReplyID()), String.valueOf(replyInfo.getContentID())});
            } else {
                index = this.mDatabase.insert("reply", null, replyValues);
            }
        }
        return index;
    }

    private List<FeedbackInfo> parseFeedbackInfos(Cursor cursor) {
        List<FeedbackInfo> infos = new ArrayList();
        if (cursor != null && cursor.getCount() >= 1 && cursor.moveToFirst()) {
            do {
                infos.add(getFeedbackInfo(cursor));
            } while (cursor.moveToNext());
        }
        return infos;
    }

    private FeedbackInfo getFeedbackInfo(Cursor cursor) {
        FeedbackInfo feedbackInfo = new FeedbackInfo();
        feedbackInfo.setID((long) cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
        feedbackInfo.setContentID(cursor.getLong(cursor.getColumnIndexOrThrow("content_id")));
        feedbackInfo.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
        feedbackInfo.setSendTime(cursor.getString(cursor.getColumnIndexOrThrow(MessageImpl.SEND_TIME)));
        feedbackInfo.setAttachTexts(cursor.getString(cursor.getColumnIndexOrThrow(MessageImpl.ATTACHS)));
        feedbackInfo.setUserContact(cursor.getString(cursor.getColumnIndexOrThrow(MessageImpl.USER_CONTACT)));
        feedbackInfo.setReplyInfos(getReplyInfosByCID(cursor.getLong(cursor.getColumnIndexOrThrow("content_id"))));
        return feedbackInfo;
    }

    private List<ReplyInfo> parseReplyInfos(long contentID, Cursor cursor) {
        List<ReplyInfo> replies = new ArrayList();
        if (cursor != null && cursor.getCount() >= 1) {
            cursor.moveToFirst();
            do {
                ReplyInfo replyInfo = new ReplyInfo();
                replyInfo.setID(cursor.getInt(cursor.getColumnIndex("_id")));
                replyInfo.setContentID(contentID);
                replyInfo.setReplyID(cursor.getLong(cursor.getColumnIndex(ReplyImpl.REPLY_ID)));
                replyInfo.setReaded(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(ReplyImpl.IS_READ))));
                replyInfo.setReplyContent(cursor.getString(cursor.getColumnIndex(ReplyImpl.REPLY_CONTENT)));
                replyInfo.setReplyPerson(cursor.getString(cursor.getColumnIndex(ReplyImpl.REPLY_PERSON)));
                replyInfo.setReplyTime(cursor.getLong(cursor.getColumnIndex(ReplyImpl.REPLY_TIME)));
                replies.add(replyInfo);
            } while (cursor.moveToNext());
        }
        return replies;
    }

    public void registerDataObserver(DataChangeObserver observer) {
        this.mDataChangeObserver.add(new WeakReference(observer));
        if (observer != null) {
            try {
                observer.onDataChange((List) Utils.deepCopy(this.mFeedbackInfos));
            } catch (IOException e) {
                Log.e(TAG, "IOException : " + e.getMessage());
            } catch (ClassNotFoundException e2) {
                Log.e(TAG, "ClassNotFoundException : " + e2.getMessage());
            }
        }
    }

    public void unregisteredDataObserver(DataChangeObserver observer) {
        List<WeakReference<DataChangeObserver>> removeList = new ArrayList();
        for (WeakReference<DataChangeObserver> ob : this.mDataChangeObserver) {
            DataChangeObserver dataChangeObserver = (DataChangeObserver) ob.get();
            if (dataChangeObserver == null) {
                removeList.add(ob);
            } else if (dataChangeObserver.equals(observer)) {
                removeList.add(ob);
            }
        }
        this.mDataChangeObserver.removeAll(removeList);
    }

    private void notifyObservers() {
        try {
            List<FeedbackInfo> feedbackInfos = (List) Utils.deepCopy(this.mFeedbackInfos);
            Log.d(TAG, "notifyObservers feedbackInfos = " + feedbackInfos);
            for (WeakReference<DataChangeObserver> observer : this.mDataChangeObserver) {
                DataChangeObserver dataChangeObserver = (DataChangeObserver) observer.get();
                if (dataChangeObserver != null) {
                    dataChangeObserver.onDataChange(feedbackInfos);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException : " + e.getMessage());
        } catch (ClassNotFoundException e2) {
            Log.e(TAG, "ClassNotFoundException : " + e2.getMessage());
        }
    }

    public boolean hasNewReplies() {
        return isExistInDB("select count(*) from reply where is_read = ?", new String[]{String.valueOf(false)});
    }

    public void storageAppData(String appKey, String imei) {
        this.mDatabase.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(AppDataImpl.APP_KEY, appKey);
            values.put(AppDataImpl.IMEI, imei);
            this.mDatabase.insert("appdata", null, values);
            this.mDatabase.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            this.mDatabase.endTransaction();
        }
    }

    public boolean isStoragedAppData() {
        Cursor cursor = null;
        try {
            cursor = this.mDatabase.query("appdata", null, null, null, null, null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            } else if (cursor == null) {
                return true;
            } else {
                cursor.close();
                return true;
            }
        } catch (Exception e) {
            Log.d(TAG, "getAppKey " + e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public IAppData getAppData() {
        Cursor cursor = null;
        try {
            cursor = this.mDatabase.query("appdata", null, null, null, null, null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            cursor.moveToFirst();
            AppData appData = new AppData();
            appData.setAppKey(cursor.getString(cursor.getColumnIndex(AppDataImpl.APP_KEY)));
            appData.setImei(cursor.getString(cursor.getColumnIndex(AppDataImpl.IMEI)));
            if (cursor == null) {
                return appData;
            }
            cursor.close();
            return appData;
        } catch (Exception e) {
            Log.d(TAG, "getAppKey " + e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
