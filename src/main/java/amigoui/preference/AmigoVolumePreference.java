package amigoui.preference;

import com.gionee.aminote.R;
import amigoui.preference.AmigoPreference.BaseSavedState;
import amigoui.preference.AmigoPreferenceManager.OnActivityStopListener;
import amigoui.widget.AmigoWidgetResource;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class AmigoVolumePreference extends AmigoSeekBarDialogPreference implements OnActivityStopListener, OnKeyListener {
    private static final String TAG = "VolumePreference";
    private SeekBarVolumizer mSeekBarVolumizer;
    private int mStreamType;

    public class SeekBarVolumizer implements OnSeekBarChangeListener, Runnable {
        private AudioManager mAudioManager;
        private Context mContext;
        private Handler mHandler;
        private int mLastProgress;
        private int mOriginalStreamVolume;
        private Ringtone mRingtone;
        private SeekBar mSeekBar;
        private int mStreamType;
        private int mVolumeBeforeMute;
        private ContentObserver mVolumeObserver;

        public SeekBarVolumizer(AmigoVolumePreference amigoVolumePreference, Context context, SeekBar seekBar, int streamType) {
            this(context, seekBar, streamType, null);
        }

        public SeekBarVolumizer(Context context, SeekBar seekBar, int streamType, Uri defaultUri) {
            this.mHandler = new Handler();
            this.mLastProgress = -1;
            this.mVolumeBeforeMute = -1;
            this.mVolumeObserver = new ContentObserver(this.mHandler) {
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    if (SeekBarVolumizer.this.mSeekBar != null && SeekBarVolumizer.this.mAudioManager != null) {
                        SeekBarVolumizer.this.mSeekBar.setProgress(SeekBarVolumizer.this.mAudioManager.getStreamVolume(SeekBarVolumizer.this.mStreamType));
                    }
                }
            };
            this.mContext = context;
            this.mAudioManager = (AudioManager) context.getSystemService("audio");
            this.mStreamType = streamType;
            this.mSeekBar = seekBar;
            initSeekBar(seekBar, defaultUri);
        }

        private void initSeekBar(SeekBar seekBar, Uri defaultUri) {
            seekBar.setMax(this.mAudioManager.getStreamMaxVolume(this.mStreamType));
            this.mOriginalStreamVolume = this.mAudioManager.getStreamVolume(this.mStreamType);
            seekBar.setProgress(this.mOriginalStreamVolume);
            seekBar.setOnSeekBarChangeListener(this);
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor(System.VOLUME_SETTINGS[this.mStreamType]), false, this.mVolumeObserver);
            if (defaultUri == null) {
                if (this.mStreamType == 2) {
                    defaultUri = System.DEFAULT_RINGTONE_URI;
                } else if (this.mStreamType == 5) {
                    defaultUri = System.DEFAULT_NOTIFICATION_URI;
                } else {
                    defaultUri = RingtoneManager.getActualDefaultRingtoneUri(this.mContext, 4);
                }
            }
            try {
                this.mRingtone = RingtoneManager.getRingtone(this.mContext, defaultUri);
            } catch (Exception e) {
                if (!(this.mStreamType == 2 || this.mStreamType == 5)) {
                    this.mRingtone = RingtoneManager.getRingtone(this.mContext, System.DEFAULT_ALARM_ALERT_URI);
                }
            }
            if (this.mRingtone != null) {
                this.mRingtone.setStreamType(this.mStreamType);
            }
        }

        public void stop() {
            stopSample();
            this.mContext.getContentResolver().unregisterContentObserver(this.mVolumeObserver);
            this.mSeekBar.setOnSeekBarChangeListener(null);
        }

        public void revertVolume() {
            this.mAudioManager.setStreamVolume(this.mStreamType, this.mOriginalStreamVolume, 0);
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
            if (fromTouch) {
                postSetVolume(progress);
            }
        }

        void postSetVolume(int progress) {
            this.mLastProgress = progress;
            this.mHandler.removeCallbacks(this);
            this.mHandler.post(this);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (!isSamplePlaying()) {
                startSample();
            }
        }

        public void run() {
            this.mAudioManager.setStreamVolume(this.mStreamType, this.mLastProgress, 0);
        }

        public boolean isSamplePlaying() {
            return this.mRingtone != null && this.mRingtone.isPlaying();
        }

        public void startSample() {
            AmigoVolumePreference.this.onSampleStarting(this);
            if (this.mRingtone != null) {
                this.mRingtone.play();
            }
        }

        public void stopSample() {
            if (this.mRingtone != null) {
                this.mRingtone.stop();
            }
        }

        public SeekBar getSeekBar() {
            return this.mSeekBar;
        }

        public void changeVolumeBy(int amount) {
            this.mSeekBar.incrementProgressBy(amount);
            if (!isSamplePlaying()) {
                startSample();
            }
            postSetVolume(this.mSeekBar.getProgress());
            this.mVolumeBeforeMute = -1;
        }

        public void muteVolume() {
            if (this.mVolumeBeforeMute != -1) {
                this.mSeekBar.setProgress(this.mVolumeBeforeMute);
                startSample();
                postSetVolume(this.mVolumeBeforeMute);
                this.mVolumeBeforeMute = -1;
                return;
            }
            this.mVolumeBeforeMute = this.mSeekBar.getProgress();
            this.mSeekBar.setProgress(0);
            stopSample();
            postSetVolume(0);
        }

        public void onSaveInstanceState(VolumeStore volumeStore) {
            if (this.mLastProgress >= 0) {
                volumeStore.volume = this.mLastProgress;
                volumeStore.originalVolume = this.mOriginalStreamVolume;
            }
        }

        public void onRestoreInstanceState(VolumeStore volumeStore) {
            if (volumeStore.volume != -1) {
                this.mOriginalStreamVolume = volumeStore.originalVolume;
                this.mLastProgress = volumeStore.volume;
                postSetVolume(this.mLastProgress);
            }
        }
    }

    public static class VolumeStore {
        public int originalVolume = -1;
        public int volume = -1;
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        VolumeStore mVolumeStore = new VolumeStore();

        public SavedState(Parcel source) {
            super(source);
            this.mVolumeStore.volume = source.readInt();
            this.mVolumeStore.originalVolume = source.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mVolumeStore.volume);
            dest.writeInt(this.mVolumeStore.originalVolume);
        }

        VolumeStore getVolumeStore() {
            return this.mVolumeStore;
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public AmigoVolumePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!NativePreferenceManager.getAnalyzeNativePreferenceXml() || attrs == null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoVolumePreference, 0, 0);
            this.mStreamType = a.getInt(R.styleable.AmigoVolumePreference_amigostreamType, 0);
            a.recycle();
            return;
        }
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            switch (attrs.getAttributeNameResource(i)) {
                case 16843273:
                    this.mStreamType = attrs.getAttributeIntValue(i, 0);
                    break;
                default:
                    break;
            }
        }
    }

    public void setStreamType(int streamType) {
        this.mStreamType = streamType;
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.mSeekBarVolumizer = new SeekBarVolumizer(this, getContext(), (SeekBar) view.findViewById(AmigoWidgetResource.getIdentifierById(view.getContext(), "amigo_seekbar")), this.mStreamType);
        getPreferenceManager().registerOnActivityStopListener(this);
        view.setOnKeyListener(this);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (this.mSeekBarVolumizer == null) {
            return true;
        }
        boolean isdown;
        if (event.getAction() == 0) {
            isdown = true;
        } else {
            isdown = false;
        }
        switch (keyCode) {
            case 24:
                if (!isdown) {
                    return true;
                }
                this.mSeekBarVolumizer.changeVolumeBy(1);
                return true;
            case 25:
                if (!isdown) {
                    return true;
                }
                this.mSeekBarVolumizer.changeVolumeBy(-1);
                return true;
            case 164:
                if (!isdown) {
                    return true;
                }
                this.mSeekBarVolumizer.muteVolume();
                return true;
            default:
                return false;
        }
    }

    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (!(positiveResult || this.mSeekBarVolumizer == null)) {
            this.mSeekBarVolumizer.revertVolume();
        }
        cleanup();
    }

    public void onActivityStop() {
        if (this.mSeekBarVolumizer != null) {
            this.mSeekBarVolumizer.stopSample();
        }
    }

    private void cleanup() {
        getPreferenceManager().unregisterOnActivityStopListener(this);
        if (this.mSeekBarVolumizer != null) {
            Dialog dialog = getDialog();
            if (dialog != null && dialog.isShowing()) {
                View view = dialog.getWindow().getDecorView().findViewById(AmigoWidgetResource.getIdentifierById(dialog.getContext(), "amigo_seekbar"));
                if (view != null) {
                    view.setOnKeyListener(null);
                }
                this.mSeekBarVolumizer.revertVolume();
            }
            this.mSeekBarVolumizer.stop();
            this.mSeekBarVolumizer = null;
        }
    }

    protected void onSampleStarting(SeekBarVolumizer volumizer) {
        if (this.mSeekBarVolumizer != null && volumizer != this.mSeekBarVolumizer) {
            this.mSeekBarVolumizer.stopSample();
        }
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        if (this.mSeekBarVolumizer != null) {
            this.mSeekBarVolumizer.onSaveInstanceState(myState.getVolumeStore());
        }
        return myState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        if (this.mSeekBarVolumizer != null) {
            this.mSeekBarVolumizer.onRestoreInstanceState(myState.getVolumeStore());
        }
    }
}
