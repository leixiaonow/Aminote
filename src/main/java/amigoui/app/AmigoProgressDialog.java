package amigoui.app;

import amigoui.changecolors.ChameleonColorManager;
import amigoui.widget.AmigoProgressBar;
import amigoui.widget.AmigoWidgetResource;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import java.text.NumberFormat;

public class AmigoProgressDialog extends AmigoAlertDialog {
    public static final int STYLE_HORIZONTAL = 1;
    public static final int STYLE_SPINNER = 0;
    private boolean mHasStarted;
    private int mIncrementBy;
    private int mIncrementSecondaryBy;
    private boolean mIndeterminate;
    private Drawable mIndeterminateDrawable;
    private int mMax;
    private CharSequence mMessage;
    private TextView mMessageView;
    private AmigoProgressBar mProgress;
    private Drawable mProgressDrawable;
    private TextView mProgressNumber;
    private String mProgressNumberFormat;
    private TextView mProgressPercent;
    private NumberFormat mProgressPercentFormat;
    private int mProgressStyle = 0;
    private int mProgressVal;
    private int mSecondaryProgressVal;
    private Handler mViewUpdateHandler;

    public AmigoProgressDialog(Context context) {
        super(context);
        initFormats();
    }

    public AmigoProgressDialog(Context context, int theme) {
        super(context, theme);
        initFormats();
    }

    private void initFormats() {
        this.mProgressNumberFormat = "%1d/%2d";
        this.mProgressPercentFormat = NumberFormat.getPercentInstance();
        this.mProgressPercentFormat.setMaximumFractionDigits(0);
    }

    public static AmigoProgressDialog show(Context context, CharSequence title, CharSequence message) {
        return show(context, title, message, false);
    }

    public static AmigoProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    public static AmigoProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    public static AmigoProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable, OnCancelListener cancelListener) {
        AmigoProgressDialog dialog = new AmigoProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIndeterminate(indeterminate);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
        return dialog;
    }

    protected void onCreate(Bundle savedInstanceState) {
        Context context = getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        if (this.mProgressStyle == 1) {
            this.mViewUpdateHandler = new Handler() {
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    int progress = AmigoProgressDialog.this.mProgress.getProgress();
                    int max = AmigoProgressDialog.this.mProgress.getMax();
                    if (AmigoProgressDialog.this.mProgressNumberFormat != null) {
                        String format = AmigoProgressDialog.this.mProgressNumberFormat;
                        AmigoProgressDialog.this.mProgressNumber.setText(String.format(format, new Object[]{Integer.valueOf(progress), Integer.valueOf(max)}));
                    } else {
                        AmigoProgressDialog.this.mProgressNumber.setText("");
                    }
                    if (AmigoProgressDialog.this.mProgressPercentFormat != null) {
                        SpannableString tmp = new SpannableString(AmigoProgressDialog.this.mProgressPercentFormat.format(((double) progress) / ((double) max)));
                        tmp.setSpan(new StyleSpan(1), 0, tmp.length(), 33);
                        AmigoProgressDialog.this.mProgressPercent.setText(tmp);
                        return;
                    }
                    AmigoProgressDialog.this.mProgressPercent.setText("");
                }
            };
            view = inflater.inflate(AmigoWidgetResource.getIdentifierByLayout(context, "amigo_alert_dialog_progress"), null);
            this.mProgress = (AmigoProgressBar) view.findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_progress"));
            this.mProgressNumber = (TextView) view.findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_progress_number"));
            this.mProgressPercent = (TextView) view.findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_progress_percent"));
            if (ChameleonColorManager.isNeedChangeColor()) {
                this.mProgressNumber.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
                this.mProgressPercent.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
            }
            setView(view);
        } else {
            view = inflater.inflate(AmigoWidgetResource.getIdentifierByLayout(context, "amigo_progress_dialog"), null);
            this.mProgress = (AmigoProgressBar) view.findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_progress"));
            this.mMessageView = (TextView) view.findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_message"));
            if (ChameleonColorManager.isNeedChangeColor()) {
                this.mMessageView.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
            }
            setView(view);
        }
        if (this.mMax > 0) {
            setMax(this.mMax);
        }
        if (this.mProgressVal > 0) {
            setProgress(this.mProgressVal);
        }
        if (this.mSecondaryProgressVal > 0) {
            setSecondaryProgress(this.mSecondaryProgressVal);
        }
        if (this.mIncrementBy > 0) {
            incrementProgressBy(this.mIncrementBy);
        }
        if (this.mIncrementSecondaryBy > 0) {
            incrementSecondaryProgressBy(this.mIncrementSecondaryBy);
        }
        if (this.mProgressDrawable != null) {
            setProgressDrawable(this.mProgressDrawable);
        }
        if (this.mIndeterminateDrawable != null) {
            setIndeterminateDrawable(this.mIndeterminateDrawable);
        }
        if (this.mMessage != null) {
            setMessage(this.mMessage);
        }
        setIndeterminate(this.mIndeterminate);
        onProgressChanged();
        super.onCreate(savedInstanceState);
    }

    public void onStart() {
        super.onStart();
        this.mHasStarted = true;
    }

    protected void onStop() {
        super.onStop();
        this.mHasStarted = false;
    }

    public void setProgress(int value) {
        if (this.mHasStarted) {
            this.mProgress.setProgress(value);
            onProgressChanged();
            return;
        }
        this.mProgressVal = value;
    }

    public void setSecondaryProgress(int secondaryProgress) {
        if (this.mProgress != null) {
            this.mProgress.setSecondaryProgress(secondaryProgress);
            onProgressChanged();
            return;
        }
        this.mSecondaryProgressVal = secondaryProgress;
    }

    public int getProgress() {
        if (this.mProgress != null) {
            return this.mProgress.getProgress();
        }
        return this.mProgressVal;
    }

    public int getSecondaryProgress() {
        if (this.mProgress != null) {
            return this.mProgress.getSecondaryProgress();
        }
        return this.mSecondaryProgressVal;
    }

    public int getMax() {
        if (this.mProgress != null) {
            return this.mProgress.getMax();
        }
        return this.mMax;
    }

    public void setMax(int max) {
        if (this.mProgress != null) {
            this.mProgress.setMax(max);
            onProgressChanged();
            return;
        }
        this.mMax = max;
    }

    public void incrementProgressBy(int diff) {
        if (this.mProgress != null) {
            this.mProgress.incrementProgressBy(diff);
            onProgressChanged();
            return;
        }
        this.mIncrementBy += diff;
    }

    public void incrementSecondaryProgressBy(int diff) {
        if (this.mProgress != null) {
            this.mProgress.incrementSecondaryProgressBy(diff);
            onProgressChanged();
            return;
        }
        this.mIncrementSecondaryBy += diff;
    }

    public void setProgressDrawable(Drawable d) {
        if (this.mProgress != null) {
            this.mProgress.setProgressDrawable(d);
        } else {
            this.mProgressDrawable = d;
        }
    }

    public void setIndeterminateDrawable(Drawable d) {
        if (this.mProgress != null) {
            this.mProgress.setIndeterminateDrawable(d);
        } else {
            this.mIndeterminateDrawable = d;
        }
    }

    public void setIndeterminate(boolean indeterminate) {
        if (this.mProgress != null) {
            this.mProgress.setIndeterminate(indeterminate);
        } else {
            this.mIndeterminate = indeterminate;
        }
    }

    public boolean isIndeterminate() {
        if (this.mProgress != null) {
            return this.mProgress.isIndeterminate();
        }
        return this.mIndeterminate;
    }

    public void setMessage(CharSequence message) {
        if (this.mProgress == null) {
            this.mMessage = message;
        } else if (this.mProgressStyle == 1) {
            super.setMessage(message);
        } else {
            this.mMessageView.setText(message);
        }
    }

    public void setProgressStyle(int style) {
        this.mProgressStyle = style;
    }

    public void setProgressNumberFormat(String format) {
        this.mProgressNumberFormat = format;
        onProgressChanged();
    }

    public void setProgressPercentFormat(NumberFormat format) {
        this.mProgressPercentFormat = format;
        onProgressChanged();
    }

    private void onProgressChanged() {
        if (this.mProgressStyle == 1 && this.mViewUpdateHandler != null && !this.mViewUpdateHandler.hasMessages(0)) {
            this.mViewUpdateHandler.sendEmptyMessage(0);
        }
    }
}
