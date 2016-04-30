package com.gionee.note.app.attachment;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.gionee.aminote.R;
import com.gionee.note.common.Constants;
import com.gionee.note.common.NoteUtils;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SoundRecorder {
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final int MESSAGE_ERROR_TOAST = 2;
    private static final int MESSAGE_UPDATE_SOUND_TIME = 1;
    private Context mContext;
    private Dialog mDialog;
    private int mDurationInSec;
    private OnSoundRecorderCompleteListener mListener;
    private Handler mMainHander;
    private String mSoundPath;
    private MediaRecorder mSoundRecorder;
    private ImageView mStopButton;
    private TextView mTime;
    private Timer mTimer;
    private TimerTask mTimerTask;

    public interface OnSoundRecorderCompleteListener {
        void onRecorderComplete(String str, int i);
    }

    public SoundRecorder(Context context, OnSoundRecorderCompleteListener listener) {
        this.mContext = context;
        this.mListener = listener;
        initHandler();
    }

    public void launchRecording() {
        initDialog();
        showDialog();
        startRecorder();
        startTimer();
    }

    private void initDialog() {
        if (this.mDialog == null) {
            View content = LayoutInflater.from(this.mContext).inflate(R.layout.sound_recorder_layout, null, false);
            this.mTime = (TextView) content.findViewById(R.id.sound_recorder_time);
            this.mStopButton = (ImageView) content.findViewById(R.id.sound_recorder_stop);
            this.mStopButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    SoundRecorder.this.completeRecorder();
                }
            });
            Dialog dialog = new Dialog(this.mContext, R.style.DialogTheme);
            dialog.setContentView(content);
            dialog.setCancelable(false);
            Window window = dialog.getWindow();
            LayoutParams lp = window.getAttributes();
            lp.width = -1;
            lp.height = -2;
            window.setGravity(80);
            this.mDialog = dialog;
        }
    }

    private void initHandler() {
        this.mMainHander = new Handler(this.mContext.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        int elapse = msg.arg1;
                        SoundRecorder.this.mDurationInSec = elapse;
                        SoundRecorder.this.mTime.setText(NoteUtils.formatTime(elapse, " : "));
                        return;
                    case 2:
                        SoundRecorder.this.checkSoundRecordSuccess();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private void startRecorder() {
        String soundName = getSoundName();
        if (Constants.NOTE_MEDIA_SOUND_PATH.exists() || Constants.NOTE_MEDIA_SOUND_PATH.mkdirs()) {
            String path = Constants.NOTE_MEDIA_SOUND_PATH + "/" + soundName;
            this.mSoundPath = path;
            this.mSoundRecorder = new MediaRecorder();
            try {
                this.mSoundRecorder.setAudioSource(1);
                this.mSoundRecorder.setOutputFormat(3);
                this.mSoundRecorder.setAudioEncoder(1);
                this.mSoundRecorder.setOutputFile(path);
                this.mSoundRecorder.setOnErrorListener(new OnErrorListener() {
                    public void onError(MediaRecorder mr, int what, int extra) {
                        SoundRecorder.this.completeRecorder();
                    }
                });
                this.mSoundRecorder.prepare();
                this.mSoundRecorder.start();
            } catch (Exception e) {
                Toast.makeText(this.mContext, R.string.attachment_record_hint, 0).show();
                dismissDialog();
            }
        }
    }

    private void stopRecorder() {
        if (this.mSoundRecorder != null) {
            try {
                this.mSoundRecorder.stop();
                this.mSoundRecorder.release();
            } catch (Exception e) {
            }
            this.mSoundRecorder = null;
        }
    }

    private void startTimer() {
        this.mTimer = new Timer();
        this.mTimerTask = new TimerTask() {
            private int elapse = 0;

            public void run() {
                Message message = SoundRecorder.this.mMainHander.obtainMessage(1);
                message.arg1 = this.elapse;
                SoundRecorder.this.mMainHander.sendMessage(message);
                this.elapse++;
            }
        };
        this.mTimer.schedule(this.mTimerTask, new Date(), 1000);
        this.mMainHander.sendMessageDelayed(this.mMainHander.obtainMessage(2), 1500);
    }

    private void cancelTimer() {
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer = null;
        }
        if (this.mTimerTask != null) {
            this.mTimerTask.cancel();
            this.mTimerTask = null;
        }
    }

    private void completeRecorder() {
        stopRecorder();
        cancelTimer();
        dismissDialog();
        notifyListener();
    }

    private void checkSoundRecordSuccess() {
        if (!TextUtils.isEmpty(this.mSoundPath)) {
            File soundFile = new File(this.mSoundPath);
            if (soundFile.exists() && soundFile.length() <= 0 && soundFile.delete()) {
                cancelTimer();
                dismissDialog();
                Toast.makeText(this.mContext, R.string.attachment_record_permission_hint, 0).show();
            }
        }
    }

    private void notifyListener() {
        if (this.mListener != null) {
            this.mListener.onRecorderComplete(this.mSoundPath, this.mDurationInSec);
        }
        this.mSoundPath = null;
        this.mDurationInSec = 0;
    }

    private void showDialog() {
        this.mTime.setText(NoteUtils.formatTime(0, " : "));
        this.mDialog.show();
    }

    private void dismissDialog() {
        if (this.mDialog != null) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
    }

    public static synchronized String getSoundName() {
        String format;
        synchronized (SoundRecorder.class) {
            format = DATE_FORMATTER.format(new Date(System.currentTimeMillis()));
        }
        return format;
    }
}
