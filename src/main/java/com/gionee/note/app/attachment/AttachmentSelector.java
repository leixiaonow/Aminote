package com.gionee.note.app.attachment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore.Images.Media;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.gionee.aminote.R;
import com.gionee.framework.log.Logger;
import com.gionee.note.app.NewNoteActivity;
import com.gionee.note.app.attachment.SoundRecorder.OnSoundRecorderCompleteListener;
import com.gionee.note.app.view.AttachPicRecycleView;
import com.gionee.note.common.Constants;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.common.StorageUtils;
import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

public class AttachmentSelector {
    public static final int IMAGW_MIN_SIZE = 10485760;
    private static final String TAG = "AttachmentSelector";
    private Activity mActivity;
    private OnSelectPicToAddListener mAddPicListener;
    private Dialog mPicSelectDialog;
    private SoundRecorder mSoundRecorder;
    private OnTakePhotoListener mTakePhotoListener;

    public interface OnSelectPicToAddListener {
        void onAdd(CopyOnWriteArrayList<String> copyOnWriteArrayList);
    }

    public interface OnTakePhotoListener {
        void onStartTakePhoto(Uri uri);
    }

    public AttachmentSelector(Activity activity, OnSoundRecorderCompleteListener listener, OnTakePhotoListener takePhotoListener, OnSelectPicToAddListener addPicListener) {
        this.mActivity = activity;
        this.mSoundRecorder = new SoundRecorder(activity, listener);
        this.mTakePhotoListener = takePhotoListener;
        this.mAddPicListener = addPicListener;
    }

    public void gotoRecordSound() {
        this.mSoundRecorder.launchRecording();
    }

    public void gotoSelectImage() {
        if (!isPicSelectDialogShowing()) {
            NewNoteActivity activity = this.mActivity;
            Handler bgHandle = activity.getBackgroundHandler();
            final Handler mainHandler = activity.getMainHandler();
            bgHandle.post(new Runnable() {
                public void run() {
                    final PicInfo[] picUri = AttachmentSelector.this.getPicUris();
                    mainHandler.post(new Runnable() {
                        public void run() {
                            if (picUri != null && picUri.length != 0) {
                                AttachmentSelector.this.createSelectPicDialog(picUri);
                            }
                        }
                    });
                }
            });
        }
    }

    private void createSelectPicDialog(PicInfo[] picUris) {
        LinearLayout content = (LinearLayout) LayoutInflater.from(this.mActivity).inflate(R.layout.attach_picture_selector_layout, null, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.mActivity.getApplicationContext());
        layoutManager.setOrientation(0);
        AttachPicRecycleView adapterView = (AttachPicRecycleView) content.findViewById(R.id.pic_select_list);
        adapterView.setLayoutManager(layoutManager);
        final PicSelectorAdapter adapter = new PicSelectorAdapter(this.mActivity, picUris);
        adapterView.setAdapter(adapter);
        this.mPicSelectDialog = new Dialog(this.mActivity, R.style.DialogTheme);
        this.mPicSelectDialog.setCanceledOnTouchOutside(true);
        this.mPicSelectDialog.setContentView(content);
        handleAddPic(content, adapter, this.mPicSelectDialog);
        handleCancelPic(content, this.mPicSelectDialog, adapter);
        this.mPicSelectDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                adapter.clearSelectedPicUris();
                AttachmentSelector.this.mPicSelectDialog = null;
            }
        });
        Window window = this.mPicSelectDialog.getWindow();
        LayoutParams lp = window.getAttributes();
        lp.width = -1;
        lp.height = -2;
        window.setGravity(80);
        this.mPicSelectDialog.show();
    }

    private PicInfo[] getPicUris() {
        PicInfo[] picInfoArr = null;
        Cursor cursor = null;
        try {
            cursor = this.mActivity.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, null, null, null, "date_added DESC");
            if (cursor != null && cursor.getCount() != 0) {
                picInfoArr = new PicInfo[cursor.getCount()];
                int count = 0;
                cursor.moveToFirst();
                do {
                    int count2 = count;
                    String column = "_data";
                    String filePath = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                    PicInfo info = new PicInfo();
                    info.uri = Uri.fromFile(new File(filePath)).toString();
                    count = count2 + 1;
                    picInfoArr[count2] = info;
                } while (cursor.moveToNext());
                if (cursor != null) {
                    cursor.close();
                }
            } else if (cursor != null) {
                cursor.close();
            }
            return picInfoArr;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void dismissPicSelectDialog() {
        if (this.mPicSelectDialog != null && this.mPicSelectDialog.isShowing()) {
            this.mPicSelectDialog.dismiss();
            this.mPicSelectDialog = null;
        }
    }

    private boolean isPicSelectDialogShowing() {
        return this.mPicSelectDialog != null && this.mPicSelectDialog.isShowing();
    }

    private void handleAddPic(View content, final PicSelectorAdapter adapter, final Dialog dialog) {
        ((TextView) content.findViewById(R.id.pic_select_add)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                CopyOnWriteArrayList selectPic = adapter.getSelectedPicUris();
                if (selectPic.size() > 0) {
                    AttachmentSelector.this.mAddPicListener.onAdd(selectPic);
                }
                AttachmentSelector.this.mPicSelectDialog = null;
            }
        });
    }

    private void handleCancelPic(View content, final Dialog dialog, final PicSelectorAdapter adapter) {
        ((TextView) content.findViewById(R.id.pic_select_cancel)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                adapter.clearSelectedPicUris();
                AttachmentSelector.this.mPicSelectDialog = null;
            }
        });
    }

    public void gotoTakePhotos() {
        try {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            File fileDirectory = StorageUtils.getAvailableFileDirectory(this.mActivity, 10485760, Constants.NOTE_MEDIA_PHOTO_PATH);
            if (fileDirectory == null) {
                fileDirectory = Constants.NOTE_MEDIA_PHOTO_PATH;
            }
            if (fileDirectory.exists() || fileDirectory.mkdirs()) {
                Uri uri = Uri.fromFile(NoteUtils.getSaveImageFile(fileDirectory));
                intent.putExtra("output", uri);
                this.mActivity.startActivityForResult(intent, 2);
                if (this.mTakePhotoListener != null) {
                    this.mTakePhotoListener.onStartTakePhoto(uri);
                }
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this.mActivity, R.string.attachment_enter_camera_fail, 0).show();
            Logger.printLog(TAG, "gotoTakePhotos fail : " + e.toString());
        }
    }
}
