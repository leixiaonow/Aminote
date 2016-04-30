package com.gionee.note.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import com.gionee.note.photoview.PreviewActivity;
import uk.co.senab.photoview.IPhotoView;

public class SharePreView extends View {
    private Bitmap mBitmap;
    private int mBitmapH;
    private int mBitmapW;
    private Rect mDst;
    private final GestureDetector mGestureDetector;
    private boolean mIsFlinged;
    private boolean mIsInit;
    private boolean mIsScrolling;
    private RectF mOurSrcRectF;
    private Paint mPaint;
    private float mScale;
    private int mScrollLimit;
    private ScrollerHelper mScroller;
    private Rect mSrc;
    private RectF mSrcRectF;
    private int mY;

    public SharePreView(Context context) {
        this(context, null);
    }

    public SharePreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        setLayerType(1, null);
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
        if (getWidth() != 0 && getHeight() != 0) {
            init();
            invalidate();
        }
    }

    private void init() {
        this.mPaint = new Paint(6);
        this.mIsInit = true;
        calculate();
    }

    private void calculate() {
        int vw = getWidth();
        int vh = getHeight();
        int dw = this.mBitmap.getWidth();
        int dh = this.mBitmap.getHeight();
        this.mBitmapW = dw;
        this.mBitmapH = dh;
        float scale = ((float) vw) / ((float) dw);
        this.mScale = IPhotoView.DEFAULT_MIN_SCALE / scale;
        int contentLength = Math.round(((float) dh) * scale);
        int limit = contentLength - vh;
        if (limit <= 0) {
            limit = 0;
        }
        this.mScrollLimit = limit;
        if (contentLength > vh) {
            this.mDst.set(0, 0, vw, vh);
            return;
        }
        int top = (vh - contentLength) / 2;
        this.mDst.set(0, top, vw, top + contentLength);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (checkEnvIsOk()) {
            this.mGestureDetector.onTouchEvent(event);
            switch (event.getAction()) {
                case 0:
                    this.mScroller.forceFinished();
                    break;
                case 1:
                    if (this.mIsScrolling && !this.mIsFlinged) {
                        this.mScroller.springBack(this.mY, 0, getScrollLimit());
                    }
                    this.mIsScrolling = false;
                    this.mIsFlinged = false;
                    invalidate();
                    break;
                case 3:
                    if (this.mIsScrolling && !this.mIsFlinged) {
                        this.mScroller.springBack(this.mY, 0, getScrollLimit());
                    }
                    this.mIsScrolling = false;
                    this.mIsFlinged = false;
                    invalidate();
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    public SharePreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mIsScrolling = false;
        this.mIsFlinged = false;
        this.mSrc = new Rect();
        this.mOurSrcRectF = new RectF();
        this.mSrcRectF = new RectF();
        this.mDst = new Rect();
        this.mScroller = new ScrollerHelper(context);
        this.mGestureDetector = new GestureDetector(context, new OnGestureListener() {
            public boolean onDown(MotionEvent e) {
                return false;
            }

            public void onShowPress(MotionEvent e) {
            }

            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                float distance = distanceY;
                SharePreView.this.mIsScrolling = true;
                SharePreView.this.overScrollBy(Math.round(distance), 0, SharePreView.this.getScrollLimit(), 0);
                return true;
            }

            public void onLongPress(MotionEvent e) {
            }

            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                int scrollLimit = SharePreView.this.getScrollLimit();
                if (scrollLimit == 0) {
                    return false;
                }
                float velocity = velocityY;
                SharePreView.this.mIsFlinged = true;
                SharePreView.this.mScroller.fling(SharePreView.this.mY, (int) (-velocity), 0, scrollLimit, SharePreView.this.getHeight() / 2);
                SharePreView.this.invalidate();
                return true;
            }
        });
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (checkEnvIsOk()) {
            canvas.clipRect(0, 0, getWidth(), getHeight());
            computeY();
            this.mSrcRectF.set(0.0f, (float) this.mY, (float) getWidth(), (float) (this.mY + getHeight()));
            RectF outRectF = this.mOurSrcRectF;
            mapRect(outRectF, this.mSrcRectF, 0.0f, 0.0f, 0.0f, 0.0f, this.mScale, this.mScale);
            int right = this.mBitmapW;
            int top = (int) outRectF.top;
            int bottom = (int) outRectF.bottom;
            if (bottom > this.mBitmapH) {
                bottom = this.mBitmapH;
            }
            this.mSrc.set(0, top, right, bottom);
            canvas.drawBitmap(this.mBitmap, this.mSrc, this.mDst, this.mPaint);
        }
    }

    private boolean checkEnvIsOk() {
        if (!this.mIsInit || this.mBitmap == null || this.mBitmap.isRecycled()) {
            return false;
        }
        return true;
    }

    private void overScrollBy(int deltaY, int min, int max, int overScrollY) {
        int newScrollY = this.mY + deltaY;
        int top = min - overScrollY;
        int bottom = max + overScrollY;
        boolean clampedY = false;
        if (newScrollY > bottom) {
            newScrollY = bottom;
            clampedY = true;
        } else if (newScrollY < top) {
            newScrollY = top;
            clampedY = true;
        }
        onOverScrolled(newScrollY, clampedY);
    }

    private void onOverScrolled(int newScrollY, boolean clampedY) {
        this.mY = newScrollY;
        if (!this.mScroller.isFinished() && clampedY) {
            this.mScroller.springBack(this.mY, 0, getScrollLimit());
        }
        invalidate();
    }

    private void computeY() {
        if (this.mScroller.advanceAnimation()) {
            overScrollBy(this.mScroller.getPosition() - this.mY, 0, getScrollLimit(), 0);
        }
    }

    private int getScrollLimit() {
        return this.mScrollLimit;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!this.mIsInit && this.mBitmap != null) {
            init();
            invalidate();
        }
    }

    public static void mapRect(RectF output, RectF src, float x0, float y0, float x, float y, float scaleX, float scaleY) {
        output.set(((src.left - x0) * scaleX) + x, ((src.top - y0) * scaleY) + y, ((src.right - x0) * scaleX) + x, ((src.bottom - y0) * scaleY) + y);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mBitmap = null;
        PreviewActivity.recycleSharePreBitmap();
    }
}
