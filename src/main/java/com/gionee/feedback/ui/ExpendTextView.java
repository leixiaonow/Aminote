package com.gionee.feedback.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import com.gionee.feedback.utils.Log;

public class ExpendTextView extends TextView {
    private static final int DURATION = 500;
    private static final String EXPENDSTRING = "...";
    private static final String TAG = "ExpendTextView";
    private boolean isAnimation = false;
    private AnimationThread mAnimationThread = new AnimationThread();

    class AnimationThread extends Thread {
        private volatile int index;
        private boolean isLoop = true;
        private final Object object = new Object();
        private String[] stateTexts;

        AnimationThread() {
        }

        void setStateTexts(String... stateTexts) {
            this.stateTexts = stateTexts;
            this.index = stateTexts != null ? stateTexts.length - 1 : 0;
            synchronized (this.object) {
                this.object.notifyAll();
            }
        }

        void onDestory() {
            this.isLoop = false;
            synchronized (this.object) {
                this.object.notifyAll();
            }
        }

        public void run() {
            while (this.isLoop) {
                try {
                    if (this.stateTexts == null || this.stateTexts.length == 0 || this.stateTexts.length == 1) {
                        if (this.stateTexts != null && this.stateTexts.length == 1) {
                            ExpendTextView.this.post(new Runnable() {
                                public void run() {
                                    ExpendTextView.this.setText(AnimationThread.this.stateTexts[0]);
                                }
                            });
                        }
                        synchronized (this.object) {
                            this.object.wait(2147483647L);
                        }
                    }
                    final String text = this.stateTexts[this.index];
                    ExpendTextView.this.post(new Runnable() {
                        public void run() {
                            ExpendTextView.this.setText(text);
                        }
                    });
                    this.index--;
                    if (this.index < 0) {
                        this.index = this.stateTexts.length - 1;
                    }
                    synchronized (this.object) {
                        this.object.wait(500);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e2) {
                    e2.printStackTrace();
                } catch (NullPointerException e3) {
                    e3.printStackTrace();
                }
            }
        }
    }

    public ExpendTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mAnimationThread.start();
    }

    public void setAnimation(boolean isAnimation) {
        this.isAnimation = isAnimation;
    }

    public void setExpendText(String text) {
        Log.d(TAG, "setExpendText text = " + text);
        if (this.isAnimation) {
            LayoutParams params = getLayoutParams();
            params.width = (int) getPaint().measureText(text);
            setLayoutParams(params);
            if (TextUtils.isEmpty(text) || !text.endsWith(EXPENDSTRING)) {
                setGravity(17);
                this.mAnimationThread.setStateTexts(text);
                return;
            }
            int length = EXPENDSTRING.length();
            String[] stateTexts = new String[length];
            for (int i = 0; i < length; i++) {
                stateTexts[i] = (String) text.subSequence(0, text.length() - i);
            }
            setGravity(8388611);
            this.mAnimationThread.setStateTexts(stateTexts);
            return;
        }
        setText(text);
    }

    public void onDestory() {
        this.mAnimationThread.onDestory();
    }
}
