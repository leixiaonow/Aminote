package com.gionee.note.photoview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.gionee.aminote.R;
import com.gionee.framework.log.Logger;
import com.gionee.note.app.AbstractNoteActivity;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.app.view.SharePreView;
import com.gionee.note.common.DecodeUtils;
import com.gionee.note.common.FileUtils;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.common.StorageUtils;
import com.gionee.note.common.ThreadPool.Job;
import com.gionee.note.common.ThreadPool.JobContext;
import java.io.File;

public class PreviewActivity extends AbstractNoteActivity implements OnClickListener {
    private static final boolean DEBUG = false;
    private static final String TAG = "PreviewActivity";
    private static Bitmap sSharePreBitmap;
    private String mImgPath;
    private Uri mUri;

    public static void setSharePreBitmap(Bitmap sharePreBitmap) {
        NoteUtils.assertTrue(sSharePreBitmap == null);
        sSharePreBitmap = sharePreBitmap;
    }

    public static Bitmap getsSharePreBitmap() {
        return sSharePreBitmap;
    }

    public static void recycleSharePreBitmap() {
        if (sSharePreBitmap != null) {
            sSharePreBitmap.recycle();
            sSharePreBitmap = null;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentView();
        initListener();
        this.mImgPath = getIntent().getStringExtra("img_path");
        this.mUri = Uri.parse(Uri.decode(Uri.fromFile(new File(this.mImgPath)).toString()));
        Bitmap bitmap = getsSharePreBitmap();
        if (bitmap != null) {
            ((SharePreView) findViewById(R.id.share_preview)).setBitmap(bitmap);
            return;
        }
        findViewById(R.id.preview_load_bar).setVisibility(0);
        asynLoadSharePreviewBitmap();
    }

    private void asynLoadSharePreviewBitmap() {
        NoteAppImpl.getContext().getThreadPool().submit(new Job<Object>() {
            public Object run(JobContext jc) {
                final Bitmap preViewBitmap = DecodeUtils.decodeBitmap(NoteAppImpl.getContext(), PreviewActivity.this.mUri);
                PreviewActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        if (!PreviewActivity.this.validActivityEnv()) {
                            return;
                        }
                        if (preViewBitmap == null) {
                            PreviewActivity.this.finish();
                            return;
                        }
                        PreviewActivity.this.findViewById(R.id.preview_load_bar).setVisibility(8);
                        PreviewActivity.setSharePreBitmap(preViewBitmap);
                        ((SharePreView) PreviewActivity.this.findViewById(R.id.share_preview)).setBitmap(preViewBitmap);
                    }
                });
                return null;
            }
        });
    }

    private boolean validActivityEnv() {
        return (isFinishing() || isDestroyed()) ? false : true;
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.preview_activity_title_layout_back:
                onBack();
                return;
            case R.id.preview_activity_title_layout_share:
                shareNoteImage();
                return;
            case R.id.preview_activity_title_layout_save:
                saveImage();
                return;
            default:
                return;
        }
    }

    private void onBack() {
        finish();
    }

    private void shareNoteImage() {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("image/*");
        intent.putExtra("android.intent.extra.STREAM", this.mUri);
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.note_action_share_string)));
    }

    private void saveImage() {
        File originFile = new File(this.mImgPath);
        if (originFile.exists()) {
            File targetFile = StorageUtils.getAvailableFileDirectory(this, originFile.length(), new File(Environment.getExternalStorageDirectory(), "/amigo/AmiNote"));
            if (targetFile != null) {
                boolean allowSave;
                if (targetFile.exists()) {
                    allowSave = true;
                } else {
                    allowSave = targetFile.mkdirs();
                    if (!allowSave) {
                        Logger.printLog(TAG, "create AmiNote dir fail");
                    }
                }
                if (allowSave) {
                    String finallyPath = NoteUtils.getSaveImageFile(targetFile).getPath() + ".png";
                    if (FileUtils.copyFile(this.mImgPath, finallyPath)) {
                        notifyMediaScanFile(new File(finallyPath));
                        showCopyState(finallyPath);
                        return;
                    }
                }
            }
            Toast.makeText(this, R.string.file_save_fail_toast_text, 0).show();
            return;
        }
        Toast.makeText(this, R.string.file_save_fail_origin_no_exist, 0).show();
    }

    private void notifyMediaScanFile(File file) {
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        intent.setData(uri);
        sendBroadcast(intent);
    }

    private void showCopyState(String fPath) {
        Toast.makeText(this, getResources().getString(R.string.file_save_toast_text) + NoteUtils.customName(this, fPath), 0).show();
    }

    private void initContentView() {
        setNoteTitleView(R.layout.shared_image_preview_title);
        setNoteContentView(R.layout.preview_layout);
        setNoteRootViewBackgroundColor(ContextCompat.getColor(this, R.color.preview_activity_bg_color));
    }

    private void initListener() {
        findViewById(R.id.preview_activity_title_layout_back).setOnClickListener(this);
        findViewById(R.id.preview_activity_title_layout_share).setOnClickListener(this);
        findViewById(R.id.preview_activity_title_layout_save).setOnClickListener(this);
    }
}
