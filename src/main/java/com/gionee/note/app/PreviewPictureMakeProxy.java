package com.gionee.note.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.EditText;
import android.widget.Toast;
import com.gionee.aminote.R;
import com.gionee.framework.log.Logger;
import com.gionee.note.app.dialog.AmigoIndeterminateProgressDialog;
import com.gionee.note.app.effect.DrawableManager;
import com.gionee.note.app.view.NoteContentEditText;
import com.gionee.note.app.view.NoteTitleEditText;
import com.gionee.note.common.Constants;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.common.StorageUtils;
import com.gionee.note.common.ThreadPool.Job;
import com.gionee.note.common.ThreadPool.JobContext;
import com.gionee.note.photoview.PreviewActivity;
import java.io.File;
import uk.co.senab.photoview.IPhotoView;

public class PreviewPictureMakeProxy {
    private static final boolean DEBUG = false;
    private static final int EMPTY_TITLE_CONTENT_TOP = 6;
    private static final int MAX_SCREEN = 600;
    private static final int MSG_CREATE_BMP_FAIL = 7;
    private static final int MSG_DRAW_CONTENT = 3;
    private static final int MSG_FAIL = 1;
    private static final int MSG_NO_SPACE = 2;
    private static final int MSG_SUCCESS = 4;
    private static final String TAG = "PreviewPictureMakeProxy";
    private Activity mActivity;
    private EditText mContentView;
    private AmigoIndeterminateProgressDialog mDialog;
    private boolean mIsCancel;
    private Handler mMainHandler;
    private EditText mTitleView;

    private static class DrawContentProxy {
        private Canvas mCanvas;
        private int mContentTranslateY;
        private NoteContentEditText mContentView;
        private Bitmap mDstBmp;
        private float mScale;
        private boolean mShouldDrawTitleView;
        private File mTempShareFileDirectory;
        private int mTitleTranslateY;
        private NoteTitleEditText mTitleView;

        public DrawContentProxy(NoteTitleEditText titleView, NoteContentEditText contentView, Canvas canvas, File tempShareFileDirectory, Bitmap dstBmp, float scale, int titleTranslateY, int contentTranslateY, boolean shouldDrawTitleView) {
            this.mTitleView = titleView;
            this.mContentView = contentView;
            this.mCanvas = canvas;
            this.mTempShareFileDirectory = tempShareFileDirectory;
            this.mDstBmp = dstBmp;
            this.mScale = scale;
            this.mTitleTranslateY = titleTranslateY;
            this.mContentTranslateY = contentTranslateY;
            this.mShouldDrawTitleView = shouldDrawTitleView;
        }

        public void draw() {
            this.mCanvas.scale(this.mScale, this.mScale);
            if (this.mShouldDrawTitleView) {
                drawTitle();
            }
            drawContent();
        }

        private void drawTitle() {
            this.mCanvas.translate(0.0f, (float) this.mTitleTranslateY);
            this.mTitleView.draw(this.mCanvas);
            this.mCanvas.translate(0.0f, (float) (-this.mTitleTranslateY));
        }

        private void drawContent() {
            this.mCanvas.translate(0.0f, (float) this.mContentTranslateY);
            this.mContentView.setAmiTagEnable(true);
            this.mContentView.draw(this.mCanvas);
            this.mContentView.setAmiTagEnable(false);
            this.mCanvas.translate(0.0f, (float) (-this.mContentTranslateY));
        }

        public File getTempShareFileDirectory() {
            return this.mTempShareFileDirectory;
        }

        public Bitmap getDstBitmap() {
            return this.mDstBmp;
        }
    }

    private static class PreViewData {
        private Bitmap mBitmap;
        private String mFilePath;

        public PreViewData(Bitmap bitmap, String filePath) {
            this.mBitmap = bitmap;
            this.mFilePath = filePath;
        }

        public Bitmap getPreBitmap() {
            return this.mBitmap;
        }

        public String getFilePath() {
            return this.mFilePath;
        }
    }

    public PreviewPictureMakeProxy(Activity activity) {
        this.mActivity = activity;
        this.mMainHandler = new Handler(activity.getMainLooper()) {
            public void handleMessage(Message msg) {
                if (!PreviewPictureMakeProxy.this.mIsCancel) {
                    switch (msg.what) {
                        case 1:
                            Logger.printLog(PreviewPictureMakeProxy.TAG, "MSG_FAIL");
                            PreviewPictureMakeProxy.this.showCursor(PreviewPictureMakeProxy.this.mTitleView, PreviewPictureMakeProxy.this.mContentView);
                            PreviewPictureMakeProxy.this.mDialog.dismiss();
                            Toast.makeText(PreviewPictureMakeProxy.this.mActivity, R.string.create_picture_fail, 0).show();
                            return;
                        case 2:
                            Logger.printLog(PreviewPictureMakeProxy.TAG, "MSG_NO_SPACE");
                            PreviewPictureMakeProxy.this.showCursor(PreviewPictureMakeProxy.this.mTitleView, PreviewPictureMakeProxy.this.mContentView);
                            PreviewPictureMakeProxy.this.mDialog.dismiss();
                            Toast.makeText(PreviewPictureMakeProxy.this.mActivity, R.string.create_picture_fail_no_space, 0).show();
                            return;
                        case 3:
                            DrawContentProxy drawContentProxy = msg.obj;
                            drawContentProxy.draw();
                            PreviewPictureMakeProxy.this.saveShareBitmap(drawContentProxy.getTempShareFileDirectory(), drawContentProxy.getDstBitmap());
                            return;
                        case 4:
                            Logger.printLog(PreviewPictureMakeProxy.TAG, "MSG_SUCCESS");
                            PreviewPictureMakeProxy.this.showCursor(PreviewPictureMakeProxy.this.mTitleView, PreviewPictureMakeProxy.this.mContentView);
                            PreviewPictureMakeProxy.this.mDialog.dismiss();
                            PreViewData preViewData = msg.obj;
                            if (PreviewActivity.getsSharePreBitmap() != null) {
                                PreviewActivity.recycleSharePreBitmap();
                            }
                            PreviewActivity.setSharePreBitmap(preViewData.getPreBitmap());
                            if (!PreviewPictureMakeProxy.this.startPreviewActivity(preViewData.getFilePath())) {
                                PreviewActivity.recycleSharePreBitmap();
                                return;
                            }
                            return;
                        case 7:
                            Logger.printLog(PreviewPictureMakeProxy.TAG, "MSG_CREATE_BMP_FAIL");
                            PreviewPictureMakeProxy.this.showCursor(PreviewPictureMakeProxy.this.mTitleView, PreviewPictureMakeProxy.this.mContentView);
                            PreviewPictureMakeProxy.this.mDialog.dismiss();
                            Toast.makeText(PreviewPictureMakeProxy.this.mActivity, R.string.create_picture_create_bmp_fail, 0).show();
                            return;
                        default:
                            return;
                    }
                }
            }
        };
        this.mDialog = new AmigoIndeterminateProgressDialog(activity);
        this.mDialog.setMessage((int) R.string.create_picture);
    }

    private boolean startPreviewActivity(String filePath) {
        Intent intent = new Intent(this.mActivity, PreviewActivity.class);
        intent.putExtra("img_path", filePath);
        try {
            this.mActivity.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void showCursor(EditText titleView, EditText contentView) {
        if (titleView != null) {
            titleView.setCursorVisible(true);
        }
        if (contentView != null) {
            contentView.setCursorVisible(true);
        }
    }

    private void hideCursor(EditText titleView, EditText contentView) {
        if (titleView.isCursorVisible()) {
            this.mTitleView = titleView;
            titleView.setCursorVisible(false);
        }
        if (contentView.isCursorVisible()) {
            this.mContentView = contentView;
            contentView.setCursorVisible(false);
        }
    }

    public void cancel() {
        this.mIsCancel = true;
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }

    public void createPreviewPicture(final NoteTitleEditText titleEditText, final NoteContentEditText contentEditText, final int effect) {
        this.mDialog.show();
        hideCursor(titleEditText, contentEditText);
        ((NoteAppImpl) this.mActivity.getApplication()).getThreadPool().submit(new Job<Void>() {
            public Void run(JobContext jc) {
                int tw = titleEditText.getWidth();
                int th = titleEditText.getHeight();
                int cw = contentEditText.getWidth();
                int ch = contentEditText.getHeight();
                boolean isTitleEmpty = PreviewPictureMakeProxy.this.isTitleEmpty(titleEditText);
                MarginLayoutParams tlp = (MarginLayoutParams) titleEditText.getLayoutParams();
                int tMarginTop = tlp.topMargin;
                int tMarginBottom = tlp.bottomMargin;
                MarginLayoutParams clp = (MarginLayoutParams) contentEditText.getLayoutParams();
                int cMarginTop = clp.topMargin;
                int cMarginBottom = clp.bottomMargin;
                if (((ch == 0 ? 1 : 0) | (cw == 0 ? 1 : 0)) == 0) {
                    if (((th == 0 ? 1 : 0) | (tw == 0 ? 1 : 0)) == 0) {
                        int bitmapHeight;
                        NoteUtils.assertTrue(tw == cw);
                        int screenHeight = NoteUtils.sScreenHeight;
                        int bitmapWidth = cw;
                        if (isTitleEmpty) {
                            bitmapHeight = (ch + cMarginBottom) + ((int) (6.0f * NoteUtils.sDensity));
                        } else {
                            bitmapHeight = ((th + tMarginTop) + tMarginBottom) + ((ch + cMarginTop) + cMarginBottom);
                        }
                        float scale = IPhotoView.DEFAULT_MIN_SCALE;
                        int maxLimitHeight = screenHeight * PreviewPictureMakeProxy.MAX_SCREEN;
                        if (bitmapHeight > maxLimitHeight) {
                            scale = ((float) maxLimitHeight) / ((float) bitmapHeight);
                        }
                        if (scale < IPhotoView.DEFAULT_MIN_SCALE) {
                            bitmapWidth = (int) Math.ceil((double) (((float) bitmapWidth) * scale));
                            bitmapHeight = (int) Math.ceil((double) (((float) bitmapHeight) * scale));
                        }
                        File tempShareFileDirectory = StorageUtils.getAvailableFileDirectory(PreviewPictureMakeProxy.this.mActivity, (long) ((bitmapWidth * bitmapHeight) * 4), Constants.NOTE_MEDIA_IMAGE_TEMP_SHARE_PATH);
                        if (tempShareFileDirectory == null) {
                            PreviewPictureMakeProxy.this.mMainHandler.sendEmptyMessage(2);
                            return null;
                        }
                        Bitmap dstBmp = DrawableManager.getEffectBitmap(PreviewPictureMakeProxy.this.mActivity, effect, bitmapWidth, bitmapHeight);
                        if (dstBmp == null) {
                            PreviewPictureMakeProxy.this.mMainHandler.sendEmptyMessage(7);
                            return null;
                        }
                        int contentTranslateY;
                        Canvas canvas = new Canvas(dstBmp);
                        if (isTitleEmpty) {
                            contentTranslateY = (int) (6.0f * NoteUtils.sDensity);
                        } else {
                            contentTranslateY = ((th + tMarginTop) + tMarginBottom) + cMarginTop;
                        }
                        PreviewPictureMakeProxy.this.mMainHandler.sendMessage(PreviewPictureMakeProxy.this.mMainHandler.obtainMessage(3, new DrawContentProxy(titleEditText, contentEditText, canvas, tempShareFileDirectory, dstBmp, scale, tMarginTop, contentTranslateY, !isTitleEmpty)));
                        return null;
                    }
                }
                PreviewPictureMakeProxy.this.mMainHandler.sendEmptyMessage(1);
                return null;
            }
        });
    }

    private boolean isTitleEmpty(NoteTitleEditText titleEditText) {
        if (titleEditText == null) {
            return true;
        }
        Editable text = titleEditText.getText();
        if (text != null) {
            return TextUtils.isEmpty(text.toString());
        }
        return false;
    }

    private void saveShareBitmap(final File tempShareFileDirectory, final Bitmap dstBmp) {
        ((NoteAppImpl) this.mActivity.getApplication()).getThreadPool().submit(new Job<Void>() {
            public Void run(JobContext jc) {
                File saveFile;
                if (tempShareFileDirectory.exists()) {
                    PreviewPictureMakeProxy.this.clearOldTempFile(tempShareFileDirectory);
                    saveFile = new File(NoteUtils.getSaveImageFile(tempShareFileDirectory).getPath() + ".png");
                    if (NoteUtils.saveBitmap(dstBmp, saveFile, CompressFormat.PNG, 100)) {
                        PreviewPictureMakeProxy.this.mMainHandler.obtainMessage(4, new PreViewData(dstBmp, saveFile.getPath())).sendToTarget();
                    } else {
                        dstBmp.recycle();
                        PreviewPictureMakeProxy.this.mMainHandler.sendEmptyMessage(1);
                    }
                } else {
                    if (!tempShareFileDirectory.mkdirs()) {
                        dstBmp.recycle();
                        PreviewPictureMakeProxy.this.mMainHandler.sendEmptyMessage(1);
                    }
                    saveFile = new File(NoteUtils.getSaveImageFile(tempShareFileDirectory).getPath() + ".png");
                    if (NoteUtils.saveBitmap(dstBmp, saveFile, CompressFormat.PNG, 100)) {
                        dstBmp.recycle();
                        PreviewPictureMakeProxy.this.mMainHandler.sendEmptyMessage(1);
                    } else {
                        PreviewPictureMakeProxy.this.mMainHandler.obtainMessage(4, new PreViewData(dstBmp, saveFile.getPath())).sendToTarget();
                    }
                }
                return null;
            }
        });
    }

    private void clearOldTempFile(File dirFile) {
        if (dirFile.isDirectory()) {
            File[] files = dirFile.listFiles();
            if (files != null && files.length != 0) {
                long curTime = System.currentTimeMillis();
                for (File file : files) {
                    if (curTime - file.lastModified() > 600000 && !file.delete()) {
                        Logger.printLog(TAG, "clearOldTempFile del fail file = " + file);
                    }
                }
            }
        }
    }
}
