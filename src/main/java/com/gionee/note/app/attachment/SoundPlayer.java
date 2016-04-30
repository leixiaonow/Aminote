package com.gionee.note.app.attachment;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.gionee.aminote.R;
import com.gionee.note.common.NoteUtils;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SoundPlayer {
    private static final int MESSAGE_UPDATE_SOUND_TIME = 1;
    private ImageView mButton;
    private Context mContext;
    private Dialog mDialog;
    private int mDurationInMs;
    private Handler mMainHandler;
    private MediaPlayer mSoundPlayer;
    private TextView mTime;
    private Timer mTimer;
    private TimerTask mTimerTask;

    public SoundPlayer(Context context) {
        this.mContext = context;
        initDialog();
        initHandler();
    }

    public void launchPlayer(String soundPath, int durationInSec) {
        showDialog();
        startPlayer(soundPath, durationInSec);
    }

    private void initDialog() {
        if (this.mDialog == null) {
            View content = LayoutInflater.from(this.mContext).inflate(R.layout.sound_player_layout, null, false);
            this.mTime = (TextView) content.findViewById(R.id.sound_player_time);
            this.mButton = (ImageView) content.findViewById(R.id.sound_player_button);
            this.mButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (SoundPlayer.this.mSoundPlayer.isPlaying()) {
                        SoundPlayer.this.pausePlayer();
                    } else {
                        SoundPlayer.this.reStartPlayer();
                    }
                }
            });
            Dialog dialog = new Dialog(this.mContext, R.style.DialogTheme) {
                public void onDetachedFromWindow() {
                    super.onDetachedFromWindow();
                    SoundPlayer.this.completePlayer();
                }
            };
            dialog.setContentView(content);
            dialog.setCanceledOnTouchOutside(false);
            Window window = dialog.getWindow();
            LayoutParams lp = window.getAttributes();
            lp.width = -1;
            lp.height = -2;
            window.setGravity(80);
            this.mDialog = dialog;
        }
    }

    private void initHandler() {
        this.mMainHandler = new Handler(this.mContext.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if (SoundPlayer.this.mSoundPlayer != null) {
                            int time = (SoundPlayer.this.mDurationInMs - SoundPlayer.this.mSoundPlayer.getCurrentPosition()) / 1000;
                            SoundPlayer.this.mTime.setText(NoteUtils.formatTime(NoteUtils.clamp(time, 0, time), " : "));
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private void startPlayer(String soundPath, final int durationInSec) {
        this.mSoundPlayer = new MediaPlayer();
        try {
            this.mSoundPlayer.setDataSource(soundPath);
            this.mSoundPlayer.prepareAsync();
        } catch (IOException e) {
        }
        this.mSoundPlayer.setOnErrorListener(new OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                SoundPlayer.this.completePlayer();
                return true;
            }
        });
        this.mSoundPlayer.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                SoundPlayer.this.completePlayer();
            }
        });
        this.mSoundPlayer.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                SoundPlayer.this.mDurationInMs = mp.getDuration();
                if (durationInSec != SoundPlayer.this.mDurationInMs / 1000) {
                    SoundPlayer.this.mDurationInMs = durationInSec * 1000;
                }
                mp.start();
                SoundPlayer.this.startTimer();
            }
        });
    }

    private void reStartPlayer() {
        this.mButton.setImageResource(R.drawable.media_pause);
        this.mSoundPlayer.start();
        startTimer();
    }

    private void pausePlayer() {
        this.mButton.setImageResource(R.drawable.media_play);
        this.mSoundPlayer.pause();
        cancelTimer();
    }

    private void stopPlayer() {
        if (this.mSoundPlayer != null) {
            this.mSoundPlayer.stop();
            this.mSoundPlayer.release();
            this.mSoundPlayer = null;
        }
    }

    private void startTimer() {
        this.mTimer = new Timer();
        this.mTimerTask = new TimerTask() {
            public void run() {
                SoundPlayer.this.mMainHandler.sendEmptyMessage(1);
            }
        };
        this.mTimer.schedule(this.mTimerTask, new Date(), 1000);
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

    private void completePlayer() {
        stopPlayer();
        cancelTimer();
        dismissDialog();
    }

    private void showDialog() {
        this.mDialog.show();
    }

    private void dismissDialog() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
    }
}
