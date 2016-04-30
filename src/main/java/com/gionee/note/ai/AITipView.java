package com.gionee.note.ai;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.gionee.aminote.R;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.common.Future;
import com.gionee.note.common.FutureListener;
import com.gionee.note.common.ThreadPool.Job;
import com.gionee.note.common.ThreadPool.JobContext;
import java.util.ArrayList;

public class AITipView extends ImageView implements OnClickListener {
    private static final int DELAY_MILLIS = 2000;
    private static final int MSG_REQUEST_CONTENT = 1;
    private static final int MSG_RESULT_KEY_WORDS = 2;
    private static final String TAG = "AITipView";
    private AITipCallback mAITipCallback;
    private boolean mDestroy;
    private Handler mHandler;
    private AnimationDrawable mHaveKeyWordDw;
    private KeyWordListener mKeyWordListener;
    private KeyWordWorker mKeyWordWorker;
    private ArrayList<String> mKeyWords;
    private Drawable mNotHaveKeyWordDw;

    public interface AITipCallback {
        String requestContent();

        void resultKeyWords(ArrayList<String> arrayList);
    }

    private static class KeyWordListener implements FutureListener<ArrayList<String>> {
        private Handler mHandler;
        private Object mLock;

        private KeyWordListener() {
            this.mLock = new Object();
        }

        public void setHandler(Handler handler) {
            synchronized (this.mLock) {
                this.mHandler = handler;
            }
        }

        public void onFutureDone(Future<ArrayList<String>> future) {
            ArrayList<String> keyWords = (ArrayList) future.get();
            synchronized (this.mLock) {
                if (this.mHandler != null) {
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(2, keyWords));
                }
            }
        }
    }

    private static class KeyWordWorker implements Job<ArrayList<String>> {
        private IntelligentAssistant mAI;
        private String mContent;
        private Object mLock;
        private View mView;

        private KeyWordWorker() {
            this.mLock = new Object();
        }

        public void setView(View view) {
            synchronized (this.mLock) {
                this.mView = view;
            }
        }

        public void setContent(String content) {
            synchronized (this.mLock) {
                this.mContent = content;
            }
        }

        public ArrayList<String> run(JobContext jc) {
            ArrayList<String> arrayList = null;
            try {
                View view;
                IntelligentAssistant ai = this.mAI;
                if (ai == null) {
                    synchronized (this.mLock) {
                        view = this.mView;
                    }
                    if (view != null) {
                        ai = new IntelligentAssistant(view.getContext());
                        this.mAI = ai;
                    }
                    return arrayList;
                }
                synchronized (this.mLock) {
                    view = this.mView;
                }
                if (view != null) {
                    String content;
                    synchronized (this.mLock) {
                        content = this.mContent;
                    }
                    if (!TextUtils.isEmpty(content)) {
                        arrayList = ai.getKeyWords(content);
                    }
                }
            } catch (Exception e) {
                Log.w(AITipView.TAG, "error", e);
            }
            return arrayList;
        }
    }

    public AITipView(Context context) {
        super(context);
        initAI();
    }

    public AITipView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AITipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAI();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void resume() {
        this.mHandler.sendEmptyMessageDelayed(1, 2000);
    }

    public void pause() {
        this.mHandler.removeMessages(1);
    }

    private void initAI() {
        initDrawable();
        updateTipDrawable();
        initHelper();
        setOnClickListener(this);
    }

    private void initHelper() {
        Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        AITipView.this.handlerRequestContent();
                        return;
                    case 2:
                        AITipView.this.handlerResultKeyWords((ArrayList) msg.obj);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mHandler = handler;
        this.mKeyWordWorker = new KeyWordWorker();
        this.mKeyWordWorker.setView(this);
        this.mKeyWordListener = new KeyWordListener();
        this.mKeyWordListener.setHandler(handler);
    }

    private void handlerRequestContent() {
        if (!this.mDestroy && this.mAITipCallback != null) {
            String content = this.mAITipCallback.requestContent();
            if (TextUtils.isEmpty(content)) {
                this.mKeyWords = null;
                updateTipDrawable();
            } else {
                this.mKeyWordWorker.setContent(new String(content));
                NoteAppImpl.getContext().getThreadPool().submit(this.mKeyWordWorker, this.mKeyWordListener);
            }
            this.mHandler.sendEmptyMessageDelayed(1, 2000);
        }
    }

    private void handlerResultKeyWords(ArrayList<String> keywords) {
        if (!this.mDestroy) {
            this.mKeyWords = keywords;
            updateTipDrawable();
        }
    }

    private void initDrawable() {
        this.mHaveKeyWordDw = (AnimationDrawable) ContextCompat.getDrawable(getContext(), R.drawable.ai_tip_drawable);
        this.mNotHaveKeyWordDw = ContextCompat.getDrawable(getContext(), R.drawable.ai_tip_dw_no);
    }

    public void setAICallback(AITipCallback callback) {
        this.mAITipCallback = callback;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mHaveKeyWordDw.isRunning()) {
            this.mHaveKeyWordDw.stop();
        }
        this.mKeyWordWorker.setView(null);
        this.mKeyWordListener.setHandler(null);
        this.mKeyWordWorker = null;
        this.mKeyWordListener = null;
        this.mHandler.removeMessages(1);
        this.mHandler = null;
        this.mDestroy = true;
        this.mAITipCallback = null;
    }

    public void onClick(View v) {
        AITipCallback callback = this.mAITipCallback;
        if (callback != null) {
            callback.resultKeyWords(this.mKeyWords);
        }
    }

    private void updateTipDrawable() {
        if (this.mKeyWords == null || this.mKeyWords.size() <= 0) {
            if (this.mHaveKeyWordDw.isRunning()) {
                this.mHaveKeyWordDw.stop();
            }
            setImageDrawable(this.mNotHaveKeyWordDw);
            return;
        }
        setImageDrawable(this.mHaveKeyWordDw);
        if (!this.mHaveKeyWordDw.isRunning()) {
            this.mHaveKeyWordDw.start();
        }
    }
}
