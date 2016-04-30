package amigoui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Insets;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.widget.PopupWindowCompat;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.MetaKeyKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.ViewTreeObserver.OnDrawListener;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.textservice.SpellCheckerSubtype;
import android.view.textservice.TextServicesManager;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.amigoui.internal.util.ReflectionUtils;

import java.util.Locale;

import amigoui.text.method.AmigoWordIterator;

public class AmigoEditText extends EditText {
    private static final boolean LOG_DBG = false;
    private static final String TAG = "AmigoEditText";
    private static final float[] sTmpPosition = new float[2];
    private final String LOG_TAG;
    protected boolean isEditToolbarReadMode;
    private boolean mAfterLongClicked;
    private int mArea;
    protected Context mContext;
    private int mCurOffset;
    private int mCurX;
    private int mCurY;
    private boolean mDeletable;
    private boolean mDiscardNextActionUp;
    protected boolean mDoubleTaped;
    private MotionEvent mDownMotionEvent;
    private int mDrawableSizeRight;
    private AmigoTextViewEditToolbar mEditToolbar;
    public int mEnd;
    private GnPositionListener mGnPositionListener;
    final int[] mGnTempCoords;
    private GnTextWatcher mGnTextWatcher;
    private boolean mImSwitcherEnabled;
    private boolean mIsFirstTap;
    private boolean mIsInTextSelectionMode;
    private boolean mIsSupportFloatingActionMode;
    private boolean mMagnifierAndTextSelectionEnabled;
    private boolean mMagnifierEnabled;
    private Callback mModeCallback;
    private boolean mOnScrollChanged;
    private OnPasswordDeletedListener mPasswordDeleteListener;
    private int mPreEnd;
    private int mPreStart;
    private float mPreviousTapPositionX;
    private float mPreviousTapPositionY;
    private long mPreviousTapUpTime;
    private boolean mQuickDelete;
    private Drawable mSelectHandleEnd;
    private Drawable mSelectHandleStart;
    private ActionMode mSelectionActionMode;
    private GnSelectionModifierCursorController mSelectionController;
    private boolean mSelectionControllerEnabled;
    private boolean mSelectionToolEnabled;
    boolean mShouldHandleDelete;
    private boolean mShowQuickDeleteDrawable;
    private final int mSquaredTouchSlopDistance;
    public int mStart;
    private OnTextDeletedListener mTextDeleteListener;
    private boolean mToolbarEnabled;
    private AmigoWordIterator mWordIterator;

    private interface GnCursorController extends OnTouchModeChangeListener {
        void hide();

        void onDetached();

        void show();
    }

    private interface GnEditTextPositionListener {
        void updatePosition(int i, int i2, boolean z, boolean z2);
    }

    private class GnPositionListener implements OnDrawListener {
        private final int MAXIMUM_NUMBER_OF_LISTENERS;
        private boolean[] mCanMove;
        private int mNumberOfListeners;
        private boolean mPositionHasChanged;
        private GnEditTextPositionListener[] mPositionListeners;
        private int mPositionX;
        private int mPositionY;
        private boolean mScrollHasChanged;

        private GnPositionListener() {
            this.MAXIMUM_NUMBER_OF_LISTENERS = 6;
            this.mPositionListeners = new GnEditTextPositionListener[6];
            this.mCanMove = new boolean[6];
            this.mPositionHasChanged = true;
        }

        public void addSubscriber(GnEditTextPositionListener positionListener, boolean canMove) {
            if (this.mNumberOfListeners == 0) {
                updatePosition();
                AmigoEditText.this.getViewTreeObserver().addOnDrawListener(this);
            }
            int emptySlotIndex = -1;
            int i = 0;
            while (i < 6) {
                GnEditTextPositionListener listener = this.mPositionListeners[i];
                if (listener != positionListener) {
                    if (emptySlotIndex < 0 && listener == null) {
                        emptySlotIndex = i;
                    }
                    i++;
                } else {
                    return;
                }
            }
            this.mPositionListeners[emptySlotIndex] = positionListener;
            this.mCanMove[emptySlotIndex] = canMove;
            this.mNumberOfListeners++;
        }

        public void removeSubscriber(GnEditTextPositionListener positionListener) {
            for (int i = 0; i < 6; i++) {
                if (this.mPositionListeners[i] == positionListener) {
                    this.mPositionListeners[i] = null;
                    this.mNumberOfListeners--;
                    break;
                }
            }
            if (this.mNumberOfListeners == 0) {
                AmigoEditText.this.getViewTreeObserver().removeOnDrawListener(this);
            }
        }

        public int getPositionX() {
            return this.mPositionX;
        }

        public int getPositionY() {
            return this.mPositionY;
        }

        public void onDraw() {
            CharSequence text = AmigoEditText.this.getText();
            int textLength = text.length();
            if (AmigoEditText.this.mStart < 0 || AmigoEditText.this.mStart > textLength) {
                AmigoEditText.this.mStart = 0;
            }
            if (AmigoEditText.this.mEnd > textLength) {
                AmigoEditText.this.mEnd = textLength;
                AmigoEditText.this.mSelectionController.hide();
            }
            Selection.setSelection((Spannable) text, AmigoEditText.this.mStart, AmigoEditText.this.mEnd);
            updatePosition();
            int i = 0;
            while (i < 6) {
                if (this.mPositionHasChanged || this.mScrollHasChanged || this.mCanMove[i]) {
                    GnEditTextPositionListener positionListener = this.mPositionListeners[i];
                    if (positionListener != null) {
                        positionListener.updatePosition(this.mPositionX, this.mPositionY, this.mPositionHasChanged, this.mScrollHasChanged);
                    }
                }
                i++;
            }
            this.mScrollHasChanged = false;
        }

        private void updatePosition() {
            boolean z;
            AmigoEditText.this.getLocationInWindow(AmigoEditText.this.mGnTempCoords);
            if (AmigoEditText.this.mGnTempCoords[0] == this.mPositionX && AmigoEditText.this.mGnTempCoords[1] == this.mPositionY) {
                z = false;
            } else {
                z = true;
            }
            this.mPositionHasChanged = z;
            this.mPositionX = AmigoEditText.this.mGnTempCoords[0];
            this.mPositionY = AmigoEditText.this.mGnTempCoords[1];
        }

        public void onScrollChanged() {
            this.mScrollHasChanged = true;
        }
    }

    private class GnTextWatcher implements TextWatcher {
        private GnTextWatcher() {
        }

        public void afterTextChanged(Editable arg0) {
            if (TextUtils.isEmpty(AmigoEditText.this.getText().toString())) {
                AmigoEditText.this.setCompoundDrawables(null, null, null, null);
                AmigoEditText.this.mDeletable = false;
            }
        }

        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }
    }

    public interface OnPasswordDeletedListener {
        boolean onPasswordDeleted();
    }

    public interface OnTextDeletedListener {
        boolean onTextDeleted();
    }

    private abstract class GnHandleView extends View implements GnEditTextPositionListener {
        private static final int HISTORY_SIZE = 5;
        private static final int TOUCH_UP_FILTER_DELAY_AFTER = 150;
        private static final int TOUCH_UP_FILTER_DELAY_BEFORE = 350;
        private final PopupWindow mContainer;
        protected Drawable mDrawable;
        protected Drawable mDrawableLtr;
        protected Drawable mDrawableRtl;
        protected int mHotspotX;
        protected int mHotspotY;
        private float mIdealVerticalOffset;
        private boolean mIsDragging;
        private int mLastParentX;
        private int mLastParentY;
        private int mNumberPreviousOffsets = 0;
        private boolean mPositionHasChanged = true;
        protected int mPositionX;
        protected int mPositionY;
        private int mPreviousOffset = -1;
        private int mPreviousOffsetIndex = 0;
        private final int[] mPreviousOffsets = new int[5];
        private final long[] mPreviousOffsetsTimes = new long[5];
        private float mTouchOffsetY;
        private float mTouchToWindowOffsetX;
        private float mTouchToWindowOffsetY;

        public abstract float computeHandlePositionY(int i);

        public abstract float computePointPositionY(float f, float f2, float f3);

        public abstract int getCurrentCursorOffset();

        protected abstract int getHotspotX(Drawable drawable, boolean z);

        protected abstract int getHotspotY(Drawable drawable, boolean z);

        public abstract boolean isHandleInParent();

        public abstract boolean isStartHandle();

        public abstract void updatePosition(float f, float f2);

        protected abstract void updateSelection(int i);

        public GnHandleView(Drawable drawableLtr, Drawable drawableRtl) {
            super(AmigoEditText.this.mContext);
            this.mContainer = new PopupWindow(AmigoEditText.this.mContext, null, android.R.attr.textSelectHandleWindowStyle);
//            this.mContainer.setWindowLayoutType(1002);
            PopupWindowCompat.setWindowLayoutType(mContainer,1002);//change add
            this.mContainer.setSplitTouchEnabled(true);
            this.mContainer.setClippingEnabled(false);
            this.mContainer.setContentView(this);
            this.mDrawableLtr = drawableLtr;
            this.mDrawableRtl = drawableRtl;
            updateDrawable();
            int handleHeight = this.mDrawable.getIntrinsicHeight();
            this.mTouchOffsetY = -0.3f * ((float) handleHeight);
            this.mIdealVerticalOffset = 0.7f * ((float) handleHeight);
        }

        protected void updateDrawable() {
            boolean isRtlCharAtOffset = AmigoEditText.this.getLayout().isRtlCharAt(getCurrentCursorOffset());
            this.mDrawable = isRtlCharAtOffset ? this.mDrawableRtl : this.mDrawableLtr;
            this.mHotspotX = getHotspotX(this.mDrawable, isRtlCharAtOffset);
            this.mHotspotY = getHotspotY(this.mDrawable, isRtlCharAtOffset);
        }

        private void startTouchUpFilter(int offset) {
            this.mNumberPreviousOffsets = 0;
            addPositionToTouchUpFilter(offset);
        }

        private void addPositionToTouchUpFilter(int offset) {
            this.mPreviousOffsetIndex = (this.mPreviousOffsetIndex + 1) % 5;
            this.mPreviousOffsets[this.mPreviousOffsetIndex] = offset;
            this.mPreviousOffsetsTimes[this.mPreviousOffsetIndex] = SystemClock.uptimeMillis();
            this.mNumberPreviousOffsets++;
        }

        private void filterOnTouchUp() {
            long now = SystemClock.uptimeMillis();
            int i = 0;
            int index = this.mPreviousOffsetIndex;
            int iMax = Math.min(this.mNumberPreviousOffsets, 5);
            while (i < iMax && now - this.mPreviousOffsetsTimes[index] < 150) {
                i++;
                index = ((this.mPreviousOffsetIndex - i) + 5) % 5;
            }
            if (i > 0 && i < iMax && now - this.mPreviousOffsetsTimes[index] > 350) {
                positionAtCursorOffset(this.mPreviousOffsets[index], false);
            }
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(this.mDrawable.getIntrinsicWidth() + 10, this.mDrawable.getIntrinsicHeight() + 10);
        }

        public void show() {
            if (!isShowing()) {
                AmigoEditText.this.getGnPositionListener().addSubscriber(this, true);
                this.mPreviousOffset = -1;
                positionAtCursorOffset(getCurrentCursorOffset(), false);
                AmigoEditText.this.mStart = AmigoEditText.this.getSelectionStart();
                AmigoEditText.this.mEnd = AmigoEditText.this.getSelectionEnd();
            }
        }

        protected void dismiss() {
            this.mIsDragging = false;
            this.mContainer.dismiss();
            onDetached();
        }

        public void hide() {
            dismiss();
            AmigoEditText.this.getGnPositionListener().removeSubscriber(this);
        }

        public boolean isShowing() {
            return this.mContainer.isShowing();
        }

        private boolean isVisible() {
            if (this.mIsDragging) {
                return true;
            }
            return isHandleInParent();
        }

        public boolean isDragging() {
            return this.mIsDragging;
        }

        protected void positionAtCursorOffset(int offset, boolean parentScrolled) {
            if (AmigoEditText.this.getLayout() != null) {
                if (offset != this.mPreviousOffset || parentScrolled) {
                    updateSelection(offset);
                    addPositionToTouchUpFilter(offset);
                    int line = AmigoEditText.this.getLayout().getLineForOffset(offset);
                    this.mPositionX = (int) ((AmigoEditText.this.getLayout().getPrimaryHorizontal(offset) - 0.5f) - ((float) this.mHotspotX));
                    this.mPositionY = (int) computeHandlePositionY(line);
                    this.mPositionX += AmigoEditText.this.viewportToContentHorizontalOffset();
                    this.mPositionY += AmigoEditText.this.viewportToContentVerticalOffset();
                    this.mPreviousOffset = offset;
                    this.mPositionHasChanged = true;
                }
                if (this.mIsDragging) {
                    AmigoEditText.this.hideEditToolbar();
                }
            }
        }

        public void updatePosition(int parentPositionX, int parentPositionY, boolean parentPositionChanged, boolean parentScrolled) {
            positionAtCursorOffset(getCurrentCursorOffset(), parentScrolled);
            if (parentPositionChanged || this.mPositionHasChanged) {
                if (this.mIsDragging) {
                    if (!(parentPositionX == this.mLastParentX && parentPositionY == this.mLastParentY)) {
                        this.mTouchToWindowOffsetX += (float) (parentPositionX - this.mLastParentX);
                        this.mTouchToWindowOffsetY += (float) (parentPositionY - this.mLastParentY);
                        this.mLastParentX = parentPositionX;
                        this.mLastParentY = parentPositionY;
                    }
                    onHandleMoved();
                }
                if (isVisible()) {
                    int positionX = parentPositionX + this.mPositionX;
                    int positionY = parentPositionY + this.mPositionY;
                    if (isShowing()) {
                        this.mContainer.update(positionX, positionY, -1, -1);
                    } else {
                        this.mContainer.showAtLocation(AmigoEditText.this, 0, positionX, positionY);
                    }
                } else if (isShowing()) {
                    dismiss();
                }
                this.mPositionHasChanged = false;
            }
        }

        protected void onDraw(Canvas c) {
            this.mDrawable.setBounds(0, 0, (getRight() - getLeft()) - 10, (getBottom() - getTop()) - 10);
            this.mDrawable.draw(c);
        }

        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getActionMasked()) {
                case 0:
                    startTouchUpFilter(getCurrentCursorOffset());
                    this.mTouchToWindowOffsetX = ev.getRawX() - ((float) this.mPositionX);
                    this.mTouchToWindowOffsetY = ev.getRawY() - ((float) this.mPositionY);
                    GnPositionListener positionListener = AmigoEditText.this.getGnPositionListener();
                    this.mLastParentX = positionListener.getPositionX();
                    this.mLastParentY = positionListener.getPositionY();
                    this.mIsDragging = true;
                    AmigoEditText.this.hideEditToolbar();
                    break;
                case 1:
                    filterOnTouchUp();
                    this.mIsDragging = false;
                    AmigoEditText.this.showEditToolbar();
                    AmigoEditText.this.mCurOffset = -1;
                    break;
                case 2:
                    float newVerticalOffset;
                    float rawX = ev.getRawX();
                    float rawY = ev.getRawY();
                    float previousVerticalOffset = this.mTouchToWindowOffsetY - ((float) this.mLastParentY);
                    float currentVerticalOffset = (rawY - ((float) this.mPositionY)) - ((float) this.mLastParentY);
                    if (previousVerticalOffset < this.mIdealVerticalOffset) {
                        newVerticalOffset = Math.max(Math.min(currentVerticalOffset, this.mIdealVerticalOffset), previousVerticalOffset);
                    } else {
                        newVerticalOffset = Math.min(Math.max(currentVerticalOffset, this.mIdealVerticalOffset), previousVerticalOffset);
                    }
                    this.mTouchToWindowOffsetY = ((float) this.mLastParentY) + newVerticalOffset;
                    updatePosition((rawX - this.mTouchToWindowOffsetX) + ((float) this.mHotspotX), computePointPositionY(rawY, this.mTouchToWindowOffsetY, this.mTouchOffsetY));
                    break;
                case 3:
                    this.mIsDragging = false;
                    AmigoEditText.this.showEditToolbar();
                    AmigoEditText.this.mCurOffset = -1;
                    break;
            }
            return true;
        }

        void onHandleMoved() {
        }

        public void onDetached() {
        }
    }

    private class GnSelectionModifierCursorController implements GnCursorController {
        private GnSelectionEndHandleView mEndHandle;
        private GnSelectionStartHandleView mStartHandle;

        GnSelectionModifierCursorController() {
        }

        public void show() {
            initDrawables();
            initHandles();
        }

        private void initDrawables() {
            if (AmigoEditText.this.mSelectHandleStart == null) {
                AmigoEditText.this.mSelectHandleStart = AmigoEditText.this.mContext.getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(AmigoEditText.this.mContext, "amigo_text_select_handle_top_left"));
            }
            if (AmigoEditText.this.mSelectHandleEnd == null) {
                AmigoEditText.this.mSelectHandleEnd = AmigoEditText.this.mContext.getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(AmigoEditText.this.mContext, "amigo_text_select_handle_top_right"));
            }
        }

        private void initHandles() {
            if (this.mStartHandle == null) {
                this.mStartHandle = new GnSelectionStartHandleView(AmigoEditText.this.mSelectHandleStart, AmigoEditText.this.mSelectHandleEnd);
            }
            if (this.mEndHandle == null) {
                this.mEndHandle = new GnSelectionEndHandleView(AmigoEditText.this.mSelectHandleEnd, AmigoEditText.this.mSelectHandleStart);
            }
            this.mStartHandle.show();
            this.mEndHandle.show();
        }

        public void hide() {
            if (this.mStartHandle != null) {
                this.mStartHandle.hide();
            }
            if (this.mEndHandle != null) {
                this.mEndHandle.hide();
            }
        }

        public void onTouchModeChanged(boolean isInTouchMode) {
            if (!isInTouchMode) {
                hide();
            }
        }

        public void onDetached() {
            AmigoEditText.this.getViewTreeObserver().removeOnTouchModeChangeListener(this);
            if (this.mStartHandle != null) {
                this.mStartHandle.onDetached();
            }
            if (this.mEndHandle != null) {
                this.mEndHandle.onDetached();
            }
        }

        public boolean isSelectionStartDragged() {
            return this.mStartHandle != null && this.mStartHandle.isDragging();
        }
    }

    private class GnSelectionEndHandleView extends GnHandleView {
        public GnSelectionEndHandleView(Drawable drawableLtr, Drawable drawableRtl) {
            super(drawableLtr, drawableRtl);
        }

        protected int getHotspotX(Drawable drawable, boolean isRtlRun) {
            if (isRtlRun) {
                return (drawable.getIntrinsicWidth() * 19) / 28;
            }
            return (drawable.getIntrinsicWidth() * 9) / 28;
        }

        protected int getHotspotY(Drawable drawable, boolean isRtlRun) {
            int textHeight = 0;
            if (AmigoEditText.this.getPaint() != null) {
                FontMetricsInt fm = AmigoEditText.this.getPaint().getFontMetricsInt();
                textHeight = fm.bottom - fm.top;
            }
            return ((drawable.getIntrinsicHeight() * 4) / 5) + textHeight;
        }

        public int getCurrentCursorOffset() {
            return AmigoEditText.this.getSelectionEnd();
        }

        public void updateSelection(int offset) {
            Selection.setSelection(AmigoEditText.this.getText(), AmigoEditText.this.getSelectionStart(), offset);
            updateDrawable();
        }

        public void updatePosition(float x, float y) {
            int offset = AmigoEditText.this.getOffsetForPosition(x, y);
            int line = AmigoEditText.this.getLineAtCoordinate(y);
            int end = AmigoEditText.this.getLayout().getLineEnd(line);
            int edittextRight = AmigoEditText.this.getRight();
            int edittextRightPadding = AmigoEditText.this.getPaddingRight();
            int letterWidth = getLetterWidth(line);
            if (offset == end - 1 && x > ((float) ((edittextRight - edittextRightPadding) - letterWidth))) {
                offset++;
            }
            int selectionStart = AmigoEditText.this.mStart;
            if (offset <= selectionStart) {
                offset = AmigoEditText.this.getOffsetForPosition(x, checkY(selectionStart, y));
                if (offset <= selectionStart) {
                    offset = Math.min(selectionStart + 1, AmigoEditText.this.getText().length());
                }
            }
            positionAtCursorOffset(offset, false);
            AmigoEditText.this.mEnd = offset;
        }

        private int getLetterWidth(int line) {
            int end = AmigoEditText.this.getLayout().getLineEnd(line);
            int lastLineEnd = AmigoEditText.this.getLayout().getLineEnd(line - 1);
            int width = AmigoEditText.this.getRight() - AmigoEditText.this.getLeft();
            if (end == lastLineEnd) {
                return 0;
            }
            return width / (end - lastLineEnd);
        }

        private float checkY(int selectionStart, float y) {
            int lineCount = AmigoEditText.this.getLayout().getLineCount();
            int selectionStartline = 0;
            for (int i = 0; i < lineCount; i++) {
                int start = AmigoEditText.this.getLayout().getLineStart(i);
                int end = AmigoEditText.this.getLayout().getLineEnd(i);
                if (selectionStart >= start && selectionStart <= end) {
                    selectionStartline = i;
                    break;
                }
            }
            int line = AmigoEditText.this.getLineAtCoordinate(y);
            while (line != selectionStartline) {
                y += (float) AmigoEditText.this.getLineHeight();
                line = AmigoEditText.this.getLineAtCoordinate(y);
            }
            return y;
        }

        public float computeHandlePositionY(int line) {
            return (float) (AmigoEditText.this.getLayout().getLineBottom(line) - 15);
        }

        public float computePointPositionY(float rawY, float touchToWindowOffsetY, float touchOffsetY) {
            return (rawY - touchToWindowOffsetY) + touchOffsetY;
        }

        public boolean isStartHandle() {
            return false;
        }

        public boolean isHandleInParent() {
            return AmigoEditText.this.isPositionVisible(this.mPositionX + this.mHotspotX, this.mPositionY);
        }
    }

    private class GnSelectionStartHandleView extends GnHandleView {
        public GnSelectionStartHandleView(Drawable drawableLtr, Drawable drawableRtl) {
            super(drawableLtr, drawableRtl);
        }

        protected int getHotspotX(Drawable drawable, boolean isRtlRun) {
            if (isRtlRun) {
                return (drawable.getIntrinsicWidth() * 9) / 28;
            }
            return (drawable.getIntrinsicWidth() * 19) / 28;
        }

        protected int getHotspotY(Drawable drawable, boolean isRtlRun) {
            int textHeight = 0;
            if (AmigoEditText.this.getPaint() != null) {
                FontMetricsInt fm = AmigoEditText.this.getPaint().getFontMetricsInt();
                textHeight = fm.bottom - fm.top;
            }
            return ((drawable.getIntrinsicHeight() * 4) / 5) + textHeight;
        }

        public int getCurrentCursorOffset() {
            return AmigoEditText.this.getSelectionStart();
        }

        public void updateSelection(int offset) {
            Selection.setSelection(AmigoEditText.this.getText(), offset, AmigoEditText.this.getSelectionEnd());
            updateDrawable();
        }

        public void updatePosition(float x, float y) {
            int offset = AmigoEditText.this.getOffsetForPosition(x, y);
            int selectionEnd = AmigoEditText.this.mEnd;
            if (offset >= selectionEnd) {
                offset = AmigoEditText.this.getOffsetForPosition(x, checkY(selectionEnd, y));
                if (offset >= selectionEnd) {
                    offset = Math.max(0, selectionEnd - 1);
                }
            }
            positionAtCursorOffset(offset, false);
            AmigoEditText.this.mEnd = selectionEnd;
            AmigoEditText.this.mStart = offset;
        }

        private float checkY(int selectionEnd, float y) {
            int lineCount = AmigoEditText.this.getLayout().getLineCount();
            int selectionEndline = 0;
            for (int i = 0; i < lineCount; i++) {
                int start = AmigoEditText.this.getLayout().getLineStart(i);
                int end = AmigoEditText.this.getLayout().getLineEnd(i);
                if (selectionEnd >= start && selectionEnd <= end) {
                    selectionEndline = i;
                    break;
                }
            }
            int line = AmigoEditText.this.getLineAtCoordinate(y);
            while (line != selectionEndline) {
                y -= (float) AmigoEditText.this.getLineHeight();
                line = AmigoEditText.this.getLineAtCoordinate(y);
            }
            return y;
        }

        public float computeHandlePositionY(int line) {
            return (float) ((AmigoEditText.this.getLayout().getLineBottom(line) - this.mHotspotY) - 10);
        }

        public float computePointPositionY(float rawY, float touchToWindowOffsetY, float touchOffsetY) {
            return ((rawY - touchToWindowOffsetY) + touchOffsetY) + ((float) this.mHotspotY);
        }

        public boolean isStartHandle() {
            return true;
        }

        public boolean isHandleInParent() {
            return AmigoEditText.this.isPositionVisible(this.mPositionX + this.mHotspotX, this.mPositionY + this.mHotspotY);
        }
    }

    public AmigoEditText(Context context) {
        this(context, null);
    }

    public AmigoEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 16842862);
    }

    public AmigoEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AmigoEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        this.mDoubleTaped = false;
        this.LOG_TAG = "GnEditText";
        this.mPreviousTapUpTime = 0;
        this.mDiscardNextActionUp = false;
        this.mToolbarEnabled = true;
        this.mSelectionToolEnabled = true;
        this.mIsFirstTap = true;
        this.mAfterLongClicked = false;
        this.mImSwitcherEnabled = true;
        this.mIsInTextSelectionMode = false;
        this.mGnTempCoords = new int[2];
        this.mCurOffset = -1;
        this.mMagnifierEnabled = true;
        this.isEditToolbarReadMode = false;
        this.mMagnifierAndTextSelectionEnabled = false;
        this.mDeletable = false;
        this.mQuickDelete = false;
        this.mShouldHandleDelete = false;
        this.mTextDeleteListener = null;
        this.mPasswordDeleteListener = null;
        this.mShowQuickDeleteDrawable = true;
        this.mGnTextWatcher = null;
        this.mIsSupportFloatingActionMode = false;
        this.mOnScrollChanged = false;
        this.mModeCallback = new Callback() {
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        };
        this.mContext = context;
        int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mSquaredTouchSlopDistance = touchSlop * touchSlop;
        this.mIsSupportFloatingActionMode = isSupportFloatingActionMode();
        if (!this.mIsSupportFloatingActionMode) {
            setCustomSelectionActionModeCallback(this.mModeCallback);
            setMagnifierAndTextSelectionEnabled(true);
            setFastDeletable(false);
            setMagnifierAndTextSelectionEnabled(true);
        }
        if (!(getText() == null && getText().equals(""))) {
            this.mDeletable = true;
        }
        changeColor(context, attrs, defStyleAttr, defStyleRes);
    }

    private void changeColor(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    }

//    private Drawable getSelectHandleCenter(TypedArray a) {
//        Editor editor = getEditor();
//        if (editor == null) {
//            return null;
//        }
//        Drawable selectHandleCenter = (Drawable) ReflectionUtils.getFieldValue(editor, "mSelectHandleCenter");
//        if (selectHandleCenter == null) {
//            selectHandleCenter = initSelectHandleCenter(a);
//        }
//        ReflectionUtils.setFieldValue(editor, "mSelectHandleCenter", selectHandleCenter);
//        return selectHandleCenter;
//    }

//    private Drawable initSelectHandleCenter(TypedArray a) {
//        return getContext().getResources().getDrawable(a.getResourceId(64, 0));
//    }

//    private Editor getEditor() {
//        return (Editor) ReflectionUtils.getFieldValue(this, "mEditor");
//    }

    public boolean isSupportFloatingActionMode() {
        int osVersion = getAndroidOSVersion();
        if (osVersion == 23 || osVersion > 23) {
            return true;
        }
        return false;
    }

    private int getAndroidOSVersion() {
        try {
            return Integer.valueOf(VERSION.SDK).intValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mIsSupportFloatingActionMode) {
            return super.onTouchEvent(event);
        }
        if (this.mQuickDelete && !isEmpty(getText().toString())) {
            int deltX = ((getRight() - getLeft()) - getPaddingRight()) - this.mDrawableSizeRight;
            if (deltX >= 0) {
                int cur_x = (int) event.getX();
                int cur_y = (int) event.getY();
                switch (event.getAction()) {
                    case 0:
                        if (cur_x > deltX && this.mDeletable) {
                            this.mShouldHandleDelete = true;
                            return true;
                        }
                    case 1:
                        if (cur_x > deltX && this.mDeletable && this.mShouldHandleDelete && (this.mTextDeleteListener == null || !this.mTextDeleteListener.onTextDeleted())) {
                            onFastDelete();
                            this.mShouldHandleDelete = false;
                            return true;
                        }
                    case 2:
                        if (cur_x >= deltX && cur_y >= 0 && cur_y > getHeight()) {
                            break;
                        }
                }
            }
            return false;
        }
        boolean handled = super.onTouchEvent(event);
        if (!isMagnifierAndTextSelectionEnabled() || !getDefaultEditable()) {
            return handled;
        }
        switch (event.getActionMasked()) {
            case 0:
                this.mPreStart = getSelectionStart();
                this.mPreEnd = getSelectionEnd();
                float x = event.getX();
                float y = event.getY();
                if (this.mDownMotionEvent != null) {
                    this.mDownMotionEvent.recycle();
                }
                this.mDownMotionEvent = MotionEvent.obtain(event);
                if (SystemClock.uptimeMillis() - this.mPreviousTapUpTime <= ((long) ViewConfiguration.getDoubleTapTimeout()) && isPositionOnText(x, y)) {
                    float deltaX = x - this.mPreviousTapPositionX;
                    float deltaY = y - this.mPreviousTapPositionY;
                    if ((deltaX * deltaX) + (deltaY * deltaY) < ((float) this.mSquaredTouchSlopDistance)) {
                        startTextSelectionMode();
                        if (isMagnifierAndTextSelectionEnabled()) {
                            this.mDoubleTaped = true;
                        }
                        this.mDiscardNextActionUp = true;
                    }
                }
                this.mPreviousTapPositionX = x;
                this.mPreviousTapPositionY = y;
                return handled;
            case 1:
                this.mCurOffset = -1;
                this.mPreviousTapUpTime = SystemClock.uptimeMillis();
                if (this.mDoubleTaped) {
                    if (this.mToolbarEnabled) {
                        if (this.mSelectionToolEnabled) {
                            startTextSelectionMode();
                        }
                        showEditToolbar();
                    }
                    this.mDoubleTaped = false;
                } else if (!isEditToolbarShowing() || this.mAfterLongClicked) {
                    int start = getSelectionStart();
                    boolean moved = this.mPreStart == this.mPreEnd && start == getSelectionEnd() && this.mPreStart != start;
                    if (!(getKeyListener() == null || !isInputMethodTarget() || isOutside(event) || this.mIsFirstTap || moved || !this.mToolbarEnabled)) {
                        showEditToolbar();
                    }
                } else if (this.mIsInTextSelectionMode) {
                    getEditToolbar().move();
                } else {
                    hideEditToolbar();
                }
                if (!this.mOnScrollChanged && this.mIsInTextSelectionMode) {
                    stopTextSelectionMode();
                }
                this.mOnScrollChanged = false;
                this.mAfterLongClicked = false;
                this.mIsFirstTap = false;
                return handled;
            case 2:
                return handled;
            default:
                return handled;
        }
    }

    private boolean isPositionOnText(float x, float y) {
        if (getLayout() == null) {
            return false;
        }
        int line = getLineAtCoordinate(y);
        x = convertToLocalHorizontalCoordinate(x);
        if (x < getLayout().getLineLeft(line) || x > getLayout().getLineRight(line)) {
            return false;
        }
        return true;
    }

    private int getLineAtCoordinate(float y) {
        return getLayout().getLineForVertical((int) (Math.min((float) ((getHeight() - getTotalPaddingBottom()) - 1), Math.max(0.0f, y - ((float) getTotalPaddingTop()))) + ((float) getScrollY())));
    }

    private float convertToLocalHorizontalCoordinate(float x) {
        return Math.min((float) ((getWidth() - getTotalPaddingRight()) - 1), Math.max(0.0f, x - ((float) getTotalPaddingLeft()))) + ((float) getScrollX());
    }

    private void showEditToolbar() {
        if (!isMagnifierAndTextSelectionEnabled() || !isToolbarEnabled()) {
            return;
        }
        if (isEditToolbarShowing()) {
            getEditToolbar().move();
        } else {
            getEditToolbar().show();
        }
    }

    public boolean isToolbarEnabled() {
        return this.mToolbarEnabled;
    }

    private boolean isEditToolbarShowing() {
        if (isMagnifierAndTextSelectionEnabled() && isToolbarEnabled() && this.mEditToolbar != null) {
            return this.mEditToolbar.isShowing();
        }
        return false;
    }

    public boolean isSelectionToolEnabled() {
        return this.mSelectionToolEnabled;
    }

    private void hideEditToolbar() {
        if (isMagnifierAndTextSelectionEnabled() && isToolbarEnabled() && this.mEditToolbar != null) {
            this.mEditToolbar.hide();
        }
    }

    private boolean isOutside(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        return x < 0.0f || x > ((float) getWidth()) || y < 0.0f || y > ((float) getHeight());
    }

    private synchronized AmigoTextViewEditToolbar getEditToolbar() {
        if (this.mEditToolbar == null) {
            this.mEditToolbar = new AmigoTextViewEditToolbar(this);
        }
        return this.mEditToolbar;
    }

    public boolean isImSwitcherEnabled() {
        return this.mImSwitcherEnabled;
    }

    protected boolean isMagnifierAndTextSelectionEnabled() {
        return this.mMagnifierAndTextSelectionEnabled;
    }

    public boolean startTextSelectionMode() {
        if (this.mIsInTextSelectionMode) {
            return false;
        }
        if (length() <= 0 || !requestFocus()) {
            return false;
        }
        if (hasSelection() || !selectCurrentWord()) {
            showGnSelectionModifierCursorController();
            this.mIsInTextSelectionMode = true;
        } else {
            showGnSelectionModifierCursorController();
            this.mIsInTextSelectionMode = true;
        }
        return true;
    }

    public int length() {
        return getText().length();
    }

    private boolean selectCurrentWord() {
        if (length() <= 0) {
            return false;
        }
        if (getTransformationMethod() instanceof PasswordTransformationMethod) {
            selectAll();
            return true;
        }
        int inputType = getInputType();
        int klass = inputType & 15;
        int variation = inputType & 4080;
        if (klass == 2 || klass == 3 || klass == 4 || variation == 16 || variation == 32 || variation == 208 || variation == 176) {
            selectAll();
            return true;
        }
        int selectionStart;
        int selectionEnd;
        int selStart = getSelectionStart();
        int selEnd = getSelectionEnd();
        int minOffset = Math.max(0, Math.min(selStart, selEnd));
        int maxOffset = Math.max(0, Math.max(selStart, selEnd));
        if (minOffset >= length()) {
            minOffset = length() - 1;
        }
        CharSequence text = getText();
        URLSpan[] urlSpans = ((Spanned) text).getSpans(minOffset, maxOffset, URLSpan.class);
        ImageSpan[] imageSpans = ((Spanned) text).getSpans(minOffset, maxOffset, ImageSpan.class);
        if (urlSpans.length >= 1) {
            URLSpan urlSpan = urlSpans[0];
            selectionStart = ((Spanned) text).getSpanStart(urlSpan);
            selectionEnd = ((Spanned) text).getSpanEnd(urlSpan);
        } else if (imageSpans.length >= 1) {
            ImageSpan imageSpan = imageSpans[0];
            selectionStart = ((Spanned) text).getSpanStart(imageSpan);
            selectionEnd = ((Spanned) text).getSpanEnd(imageSpan);
        } else {
            AmigoWordIterator wordIterator = getWordIterator(0);
            wordIterator.setCharSequence(text, minOffset, maxOffset);
            selectionStart = wordIterator.getBeginning(minOffset);
            selectionEnd = wordIterator.getEnd(maxOffset);
            if (selectionStart == -1 || selectionEnd == -1) {
                selectionStart = minOffset;
                selectionEnd = maxOffset;
            }
            if (selectionStart == selectionEnd) {
                int[] range = getCharRange(selectionStart);
                selectionStart = range[0];
                selectionEnd = range[1];
            }
        }
        Selection.setSelection((Spannable) text, selectionStart, selectionEnd);
        if (selectionEnd > selectionStart) {
            return true;
        }
        return false;
    }

    protected void showGnSelectionModifierCursorController() {
        getGnSelectionController().show();
        setCursorVisible(false);
    }

    protected void hideGnSelectionModifierCursorController() {
        if (this.mSelectionController != null) {
            this.mSelectionController.hide();
        }
        setCursorVisible(true);
    }

    private GnSelectionModifierCursorController getGnSelectionController() {
        if (this.mSelectionController == null) {
            this.mSelectionController = new GnSelectionModifierCursorController();
            getViewTreeObserver().addOnTouchModeChangeListener(this.mSelectionController);
        }
        return this.mSelectionController;
    }

    private GnPositionListener getGnPositionListener() {
        if (this.mGnPositionListener == null) {
            this.mGnPositionListener = new GnPositionListener();
        }
        return this.mGnPositionListener;
    }

    protected void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
        this.mOnScrollChanged = true;
        super.onScrollChanged(horiz, vert, oldHoriz, oldVert);
        if (this.mGnPositionListener != null) {
            this.mGnPositionListener.onScrollChanged();
        }
    }

    //change CankaoDaima
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean isPositionVisible(int positionX, int positionY) {
        synchronized (sTmpPosition) {
            float[] position = sTmpPosition;
            position[0] = (float) positionX;
            position[1] = (float) positionY;
            View view = this;
            while (view != null) {
                if (view != this) {
                    position[0] = position[0] - ((float) view.getScrollX());
                    position[1] = position[1] - ((float) view.getScrollY());
                }
                if (position[0] < 0.0f || position[1] < 0.0f || position[0] > ((float) view.getWidth()) || position[1] > ((float) view.getHeight())) {
                    return false;
                } else {
                    if (!view.getMatrix().isIdentity()) {
                        view.getMatrix().mapPoints(position);
                    }
                    position[0] = position[0] + ((float) view.getLeft());
                    position[1] = position[1] + ((float) view.getTop());
                    ViewParent parent = view.getParent();
                    if (parent instanceof View) {
                        view = (View) parent;
                    } else {
                        view = null;
                    }
                }
            }
            return true;
        }
    }


    protected int viewportToContentHorizontalOffset() {
        return getCompoundPaddingLeft() - getScrollX();
    }

    protected int viewportToContentVerticalOffset() {
        int offset = getExtendedPaddingTop() - getScrollY();
        if ((getGravity() & 112) != 48) {
            return offset + getVerticalOffset(false);
        }
        return offset;
    }

    private int getVerticalOffset(boolean forceNormal) {
        int gravity = getGravity() & 112;
        Layout l = getLayout();
        Layout hintLayout = getHintLayout();
        if (!(forceNormal || getText().length() != 0 || hintLayout == null)) {
            l = hintLayout;
        }
        if (gravity == 48) {
            return 0;
        }
        int boxht = getBoxHeight(l);
        int textht = l.getHeight();
        if (textht >= boxht) {
            return 0;
        }
        if (gravity == 80) {
            return boxht - textht;
        }
        return (boxht - textht) >> 1;
    }

    private Layout getHintLayout() {
        return (Layout) ReflectionUtils.getFieldValue(this, "mHintLayout");
    }

    private int getBoxHeight(Layout l) {
        Insets opticalInsets = getOpticalInsets();
        return ((getMeasuredHeight() - (getExtendedPaddingTop() + getExtendedPaddingBottom())) + opticalInsets.top) + opticalInsets.bottom;
    }

    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        if (isMagnifierAndTextSelectionEnabled() && hasSelection()) {
            setSelection(getSelectionEnd());
        }
    }

    public void stopTextSelectionMode() {
        if (this.mIsInTextSelectionMode) {
            Selection.setSelection(getText(), getSelectionEnd());
            hideGnSelectionModifierCursorController();
            this.mIsInTextSelectionMode = false;
        }
    }

    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (isMagnifierAndTextSelectionEnabled()) {
            if (getDefaultEditable()) {
                hideEditToolbar();
            }
            stopTextSelectionMode();
        }
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    public AmigoWordIterator getWordIterator(int i) {
        if (this.mWordIterator == null) {
            this.mWordIterator = new AmigoWordIterator(getTextServicesLocale());
        }
        return this.mWordIterator;
    }

    void onLocaleChanged() {
        this.mWordIterator = null;
    }

    public Locale getTextServicesLocale() {
        Locale locale = Locale.getDefault();
        TextServicesManager textServicesManager = (TextServicesManager) this.mContext.getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE);
        SpellCheckerSubtype subtype = null;
        if (subtype != null) {
            return new Locale(subtype.getLocale());
        }
        return locale;
    }

    private int[] getCharRange(int offset) {
        CharSequence text = getText();
        int textLength = length();
        if (offset + 1 < textLength && Character.isSurrogatePair(text.charAt(offset), text.charAt(offset + 1))) {
            return new int[]{offset, offset + 2};
        } else if (offset < textLength) {
            return new int[]{offset, offset + 1};
        } else {
            if (offset - 2 >= 0) {
                if (Character.isSurrogatePair(text.charAt(offset - 2), text.charAt(offset - 1))) {
                    return new int[]{offset - 2, offset};
                }
            }
            if (offset - 1 >= 0) {
                return new int[]{offset - 1, offset};
            }
            return new int[]{offset, offset};
        }
    }

    public boolean performLongClick() {
        if (this.mIsSupportFloatingActionMode) {
            return super.performLongClick();
        }
        showEditToolbar();
        boolean handled = false;
        if (isMagnifierAndTextSelectionEnabled()) {
            handled = showContextMenu();
            if (!(handled || this.mDownMotionEvent == null || !getDefaultEditable())) {
                positionCursor(this.mDownMotionEvent);
                this.mAfterLongClicked = true;
                handled = true;
            }
        }
        performHapticFeedback(0);
        return handled || super.performLongClick();
    }

    private void positionCursor(MotionEvent event) {
        if (getLayout() != null) {
            int line = AmigoTextViewHelper.getLineNumber(this, event.getY());
            int offset = AmigoTextViewHelper.getOffsetByLine(this, line, event.getX());
            if ((getText() == null || length() <= 0) && MetaKeyKeyListener.getMetaState(getText(), 65536) != 0) {
                Selection.setSelection(getText(), getSelectionStart(), offset);
            } else {
                Selection.setSelection(getText(), offset);
                stopTextSelectionMode();
            }
            if (isOutside(event)) {
                this.mCurX = Math.round(event.getX());
                this.mCurY = Math.round(event.getY());
                return;
            }
            Layout layout = getLayout();
            int left = Math.round(layout.getPrimaryHorizontal(offset));
            int top = layout.getLineTop(line);
            int bottom = layout.getLineBottom(line);
            if (event.getX() > (layout.getLineRight(line) + ((float) getTotalPaddingLeft())) - ((float) getScrollX())) {
                this.mCurX = Math.round(event.getX());
            } else if (offset != this.mCurOffset) {
                this.mCurX = (getTotalPaddingLeft() + left) - getScrollX();
                this.mCurOffset = offset;
            }
            this.mCurY = (Math.round(((float) (top + bottom)) / 2.0f) + getTotalPaddingTop()) - getScrollY();
        }
    }

    private void reset() {
        if (getDefaultEditable()) {
            hideEditToolbar();
        }
        if (this.mSelectionController != null) {
            this.mSelectionController.hide();
        }
        this.mCurX = 0;
        this.mCurY = 0;
        this.mCurOffset = -1;
        this.mIsFirstTap = true;
        if (this.mDownMotionEvent != null) {
            this.mDownMotionEvent.recycle();
        }
        this.mDownMotionEvent = null;
    }

    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (isMagnifierAndTextSelectionEnabled()) {
            reset();
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (isMagnifierAndTextSelectionEnabled()) {
            reset();
        }
    }

    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (isMagnifierAndTextSelectionEnabled()) {
            if (getDefaultEditable()) {
                hideEditToolbar();
            }
            if (event.getKeyCode() == 4) {
                stopTextSelectionMode();
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isMagnifierAndTextSelectionEnabled() && getDefaultEditable()) {
            hideEditToolbar();
        }
        return super.dispatchKeyEvent(event);
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (isMagnifierAndTextSelectionEnabled() && visibility != VISIBLE && getDefaultEditable()) {
            hideEditToolbar();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (isMagnifierAndTextSelectionEnabled() && getDefaultEditable()) {
            hideEditToolbar();
            if (hasSelection()) {
                setSelection(getSelectionEnd());
            }
        }
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection ic = super.onCreateInputConnection(outAttrs);
        if (isMagnifierAndTextSelectionEnabled() && onCheckIsTextEditor() && isEnabled()) {
            if (outAttrs.extras == null) {
                outAttrs.extras = new Bundle();
            }
            outAttrs.extras.putBoolean("IS_IME_STYLE_Gn", true);
        }
        return ic;
    }

    public void setMagnifierAndTextSelectionEnabled(boolean enabled) {
        if (isMagnifierAndTextSelectionEnabled() && !enabled) {
            stopTextSelectionMode();
        }
        this.mMagnifierAndTextSelectionEnabled = enabled;
    }

    public void setMagnifierEnabled(boolean magnifierEnabled) {
        this.mMagnifierEnabled = magnifierEnabled;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isMagnifierAndTextSelectionEnabled()) {
            moveEditToolbar();
            if (hasSelection() && isEditToolbarShowing()) {
                postInvalidateDelayed(500);
            }
        }
    }

    private void moveEditToolbar() {
        if (isMagnifierAndTextSelectionEnabled() && isToolbarEnabled()) {
            getEditToolbar().move();
        }
    }

    public void setToolbarEnabled(boolean toolbarEnabled) {
        this.mToolbarEnabled = toolbarEnabled;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (isMagnifierAndTextSelectionEnabled() && isEnabled()) {
            stopTextSelectionMode();
        }
        return super.onKeyUp(keyCode, event);
    }

    protected boolean getDefaultEditable() {
        return true;
    }

    public Editable getText() {
        return super.getText();
    }

    public void setFastDeletable(boolean quickDelete) {
        if (this.mQuickDelete != quickDelete) {
            this.mQuickDelete = quickDelete;
            if (this.mQuickDelete && this.mGnTextWatcher == null) {
                this.mGnTextWatcher = new GnTextWatcher();
                addTextChangedListener(this.mGnTextWatcher);
            }
        }
    }

    public boolean isFastDeletable() {
        return this.mQuickDelete;
    }

    public void setOnTextDeletedListener(OnTextDeletedListener textDeleteListener) {
        this.mTextDeleteListener = textDeleteListener;
    }

    public void setOnPasswordDeletedListener(OnPasswordDeletedListener passwordDeletedListener) {
        this.mPasswordDeleteListener = passwordDeletedListener;
    }

    private boolean isEmpty(String currentText) {
        if (currentText == null) {
            return false;
        }
        return TextUtils.isEmpty(currentText);
    }

    private void onFastDelete() {
        CharSequence mText = getText();
        ((Editable) mText).delete(0, mText.length());
        setText("");
    }

    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        if (!this.mShowQuickDeleteDrawable) {
            super.setCompoundDrawables(null, null, null, null);
        }
        super.setCompoundDrawables(left, top, right, bottom);
        if (right != null) {
            this.mDrawableSizeRight = right.getBounds().width();
        } else {
            this.mDrawableSizeRight = 0;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!this.mQuickDelete || keyCode != 67) {
            return super.onKeyDown(keyCode, event);
        }
        super.onKeyDown(keyCode, event);
        if (this.mPasswordDeleteListener != null) {
            this.mPasswordDeleteListener.onPasswordDeleted();
        }
        return true;
    }

    public void setQuickDeleteDrawableVisible(boolean show) {
        this.mShowQuickDeleteDrawable = show;
    }

    public boolean bringPointIntoView(int offset) {
        if (isStartHandleDraging()) {
            offset = getSelectionStart();
        }
        return super.bringPointIntoView(offset);
    }

    private boolean isStartHandleDraging() {
        if (this.mSelectionController == null) {
            return false;
        }
        return this.mSelectionController.isSelectionStartDragged();
    }

    public boolean onPreDraw() {
        return super.onPreDraw();
    }
}
