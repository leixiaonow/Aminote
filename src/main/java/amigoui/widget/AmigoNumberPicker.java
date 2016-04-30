package amigoui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.gionee.aminote.R;

import java.util.Locale;

import amigoui.changecolors.ChameleonColorManager;
import amigoui.preference.AmigoPreference;
import amigoui.reflection.AmigoReflectionUtil;
import uk.co.senab.photoview.IPhotoView;

public class AmigoNumberPicker extends LinearLayout {
    private static final int BUTTON_ALPHA_OPAQUE = 1;
    private static final int BUTTON_ALPHA_TRANSPARENT = 0;
    private static final int CHANGE_CURRENT_BY_ONE_SCROLL_DURATION = 300;
    private static final long DEFAULT_LONG_PRESS_UPDATE_INTERVAL = 300;
    private static final char[] DIGIT_CHARACTERS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final String PROPERTY_BUTTON_ALPHA = "alpha";
    private static final String PROPERTY_SELECTOR_PAINT_ALPHA = "selectorPaintAlpha";
    private static final int SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800;
    private static final int SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT = 8;
    private static final int SELECTOR_MIDDLE_ITEM_INDEX = 2;
    private static final int SELECTOR_WHEEL_BRIGHT_ALPHA = 255;
    private static final int SELECTOR_WHEEL_DIM_ALPHA = 160;
    private static final int SELECTOR_WHEEL_STATE_LARGE = 2;
    private static final int SELECTOR_WHEEL_STATE_NONE = 0;
    private static final int SELECTOR_WHEEL_STATE_SMALL = 1;
    private static final int SHOW_INPUT_CONTROLS_DELAY_MILLIS = ViewConfiguration.getDoubleTapTimeout();
    private static final int SIZE_UNSPECIFIED = -1;
    private static final String TAG = "NumberPicker";
    private static final float TOP_AND_BOTTOM_FADING_EDGE_STRENGTH = 0.9f;
    public static final Formatter TWO_DIGIT_FORMATTER = new Formatter() {
        final Object[] mArgs = new Object[1];
        final StringBuilder mBuilder = new StringBuilder();
        final java.util.Formatter mFmt = new java.util.Formatter(this.mBuilder, Locale.US);

        public String format(int value) {
            this.mArgs[0] = Integer.valueOf(value);
            this.mBuilder.delete(0, this.mBuilder.length());
            this.mFmt.format("%02d", this.mArgs);
            return this.mFmt.toString();
        }
    };
    private static final int UNSCALED_DEFAULT_SELECTION_DIVIDER_HEIGHT = 2;
    private int bottomOfTopDivider;
    private final Scroller mAdjustScroller;
    private AdjustScrollerCommand mAdjustScrollerCommand;
    private boolean mAdjustScrollerOnUpEvent;
    private Align mAlign;
    private ChangeCurrentByOneFromLongPressCommand mChangeCurrentByOneFromLongPressCommand;
    private boolean mCheckBeginEditOnUpEvent;
    private final boolean mComputeMaxWidth;
    private int mCurrentScrollOffset;
    private final ImageButton mDecrementButton;
    private final Animator mDimSelectorWheelAnimator;
    private String[] mDisplayedValues;
    private final Scroller mFlingScroller;
    private final boolean mFlingable;
    private Formatter mFormatter;
    private final ImageButton mIncrementButton;
    private int mInitialScrollOffset;
    private final EditText mInputText;
    private float mLastDownEventY;
    private float mLastMotionEventY;
    private long mLastUpEventTimeMillis;
    private long mLongPressUpdateInterval;
    private final int mMaxHeight;
    private int mMaxValue;
    private int mMaxWidth;
    private int mMaximumFlingVelocity;
    private final int mMinHeight;
    private int mMinValue;
    private final int mMinWidth;
    private int mMinimumFlingVelocity;
    private OnScrollListener mOnScrollListener;
    private OnValueChangeListener mOnValueChangeListener;
    private int mPreviousScrollerY;
    private int mScrollState;
    private boolean mScrollWheelAndFadingEdgesInitialized;
    private final Drawable mSelectionBottomDivider;
    private final int mSelectionDividerHeight;
    private Drawable mSelectionSrc;
    private final Drawable mSelectionTopDivider;
    private int mSelectorElementHeight;
    private final SparseArray<String> mSelectorIndexToStringCache;
    private final int[] mSelectorIndices;
    private int mSelectorTextGapHeight;
    private final Paint mSelectorWheelPaint;
    private int mSelectorWheelState;
    private SetSelectionCommand mSetSelectionCommand;
    private final AnimatorSet mShowInputControlsAnimator;
    private final long mShowInputControlsAnimimationDuration;
    private final int mSolidColor;
    private final Rect mTempRect;
    private final int mTextSize;
    private int mTouchSlop;
    private int mValue;
    private VelocityTracker mVelocityTracker;
    private boolean mWrapSelectorWheel;
    private int topOfBottomDivider;
    private float xPosition;

    class AdjustScrollerCommand implements Runnable {
        AdjustScrollerCommand() {
        }

        public void run() {
            AmigoNumberPicker.this.mPreviousScrollerY = 0;
            if (AmigoNumberPicker.this.mInitialScrollOffset == AmigoNumberPicker.this.mCurrentScrollOffset) {
                AmigoNumberPicker.this.updateInputTextView();
                AmigoNumberPicker.this.showInputControls(AmigoNumberPicker.this.mShowInputControlsAnimimationDuration);
                return;
            }
            int deltaY = AmigoNumberPicker.this.mInitialScrollOffset - AmigoNumberPicker.this.mCurrentScrollOffset;
            if (Math.abs(deltaY) > AmigoNumberPicker.this.mSelectorElementHeight / 2) {
                deltaY += deltaY > 0 ? -AmigoNumberPicker.this.mSelectorElementHeight : AmigoNumberPicker.this.mSelectorElementHeight;
            }
            AmigoNumberPicker.this.mAdjustScroller.startScroll(0, 0, 0, deltaY, AmigoNumberPicker.SELECTOR_ADJUSTMENT_DURATION_MILLIS);
            AmigoNumberPicker.this.invalidate();
        }
    }

    class ChangeCurrentByOneFromLongPressCommand implements Runnable {
        private boolean mIncrement;

        ChangeCurrentByOneFromLongPressCommand() {
        }

        private void setIncrement(boolean increment) {
            this.mIncrement = increment;
        }

        public void run() {
            AmigoNumberPicker.this.changeCurrentByOne(this.mIncrement);
            AmigoNumberPicker.this.postDelayed(this, AmigoNumberPicker.this.mLongPressUpdateInterval);
        }
    }

    public static class CustomEditText extends EditText {
        public CustomEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public void onEditorAction(int actionCode) {
            super.onEditorAction(actionCode);
            if (actionCode == 6) {
                clearFocus();
            }
        }
    }

    public interface Formatter {
        String format(int i);
    }

    class InputTextFilter extends NumberKeyListener {
        InputTextFilter() {
        }

        public int getInputType() {
            return 1;
        }

        protected char[] getAcceptedChars() {
            return AmigoNumberPicker.DIGIT_CHARACTERS;
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            CharSequence filtered;
            String result;
            if (AmigoNumberPicker.this.mDisplayedValues == null) {
                filtered = super.filter(source, start, end, dest, dstart, dend);
                if (filtered == null) {
                    filtered = source.subSequence(start, end);
                }
                result = String.valueOf(dest.subSequence(0, dstart)) + filtered + dest.subSequence(dend, dest.length());
                if ("".equals(result)) {
                    return result;
                }
                return AmigoNumberPicker.this.getSelectedPos(result) > AmigoNumberPicker.this.mMaxValue ? "" : filtered;
            } else {
                filtered = String.valueOf(source.subSequence(start, end));
                if (TextUtils.isEmpty(filtered)) {
                    return "";
                }
                result = String.valueOf(dest.subSequence(0, dstart)) + filtered + dest.subSequence(dend, dest.length());
                String str = String.valueOf(result).toLowerCase();
                for (String val : AmigoNumberPicker.this.mDisplayedValues) {
                    if (val.toLowerCase().startsWith(str)) {
                        AmigoNumberPicker.this.postSetSelectionCommand(result.length(), val.length());
                        return val.subSequence(dstart, val.length());
                    }
                }
                return "";
            }
        }
    }

    public interface OnScrollListener {
        public static final int SCROLL_STATE_FLING = 2;
        public static final int SCROLL_STATE_IDLE = 0;
        public static final int SCROLL_STATE_TOUCH_SCROLL = 1;

        void onScrollStateChange(AmigoNumberPicker amigoNumberPicker, int i);
    }

    public interface OnValueChangeListener {
        void onValueChange(AmigoNumberPicker amigoNumberPicker, int i, int i2);
    }

    class SetSelectionCommand implements Runnable {
        private int mSelectionEnd;
        private int mSelectionStart;

        SetSelectionCommand() {
        }

        public void run() {
            AmigoNumberPicker.this.mInputText.setSelection(this.mSelectionStart, this.mSelectionEnd);
        }
    }

    public AmigoNumberPicker(Context context) {
        this(context, null);
    }

    public AmigoNumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, AmigoWidgetResource.getIdentifierByAttr(context, "amigonumberPickerStyle"));
    }

    public AmigoNumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mLongPressUpdateInterval = DEFAULT_LONG_PRESS_UPDATE_INTERVAL;
        this.mSelectorIndexToStringCache = new SparseArray();
        this.mSelectorIndices = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        this.mInitialScrollOffset = Integer.MIN_VALUE;
        this.mTempRect = new Rect();
        this.mScrollState = 0;
        this.mSelectionSrc = null;
        this.bottomOfTopDivider = 0;
        this.topOfBottomDivider = 0;
        this.mAlign = Align.CENTER;
        this.xPosition = 0.0f;
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.AmigoNumberPicker, defStyle, 0);
        this.mSolidColor = attributesArray.getColor(R.styleable.AmigoNumberPicker_amigosolidColor, 0);
        this.mFlingable = true;
        this.mSelectionTopDivider = attributesArray.getDrawable(R.styleable.AmigoNumberPicker_amigoselectiontopDivider);
        this.mSelectionBottomDivider = attributesArray.getDrawable(R.styleable.AmigoNumberPicker_amigoselectionbottomDivider);
        this.mSelectionDividerHeight = attributesArray.getDimensionPixelSize(R.styleable.AmigoNumberPicker_amigoselectionDividerHeight, (int) TypedValue.applyDimension(1, 2.0f, getResources().getDisplayMetrics()));
        this.mMinHeight = attributesArray.getDimensionPixelSize(R.styleable.AmigoNumberPicker_amigointernalMinHeight, -1);
        this.mMaxHeight = attributesArray.getDimensionPixelSize(R.styleable.AmigoNumberPicker_amigointernalMaxHeight, -1);
        if (this.mMinHeight == -1 || this.mMaxHeight == -1 || this.mMinHeight <= this.mMaxHeight) {
            this.mMinWidth = attributesArray.getDimensionPixelSize(R.styleable.AmigoNumberPicker_amigointernalMinWidth, -1);
            this.mMaxWidth = attributesArray.getDimensionPixelSize(R.styleable.AmigoNumberPicker_amigointernalMaxWidth, -1);
            if (this.mMinWidth == -1 || this.mMaxWidth == -1 || this.mMinWidth <= this.mMaxWidth) {
                this.mComputeMaxWidth = this.mMaxWidth == Integer.MAX_VALUE;
                this.mSelectionSrc = attributesArray.getDrawable(R.styleable.AmigoNumberPicker_amigoselectionSrc);
                int layoutResourceId = attributesArray.getResourceId(R.styleable.AmigoNumberPicker_amigointernalLayout, AmigoWidgetResource.getIdentifierByLayout(context, "amigo_number_picker"));
                attributesArray.recycle();
                this.mShowInputControlsAnimimationDuration = (long) getResources().getInteger(17694722);
                setWillNotDraw(false);
                setSelectorWheelState(0);
                ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(layoutResourceId, this, true);
                OnClickListener onClickListener = new OnClickListener() {
                    public void onClick(View v) {
                        InputMethodManager inputMethodManager = (InputMethodManager) AmigoNumberPicker.this.getContext().getSystemService("input_method");
                        if (inputMethodManager != null && inputMethodManager.isActive(AmigoNumberPicker.this.mInputText)) {
                            inputMethodManager.hideSoftInputFromWindow(AmigoNumberPicker.this.getWindowToken(), 0);
                        }
                        AmigoNumberPicker.this.mInputText.clearFocus();
                        if (v.getId() == AmigoWidgetResource.getIdentifierById(AmigoNumberPicker.this.getContext(), "amigo_increment")) {
                            AmigoNumberPicker.this.changeCurrentByOne(true);
                        } else {
                            AmigoNumberPicker.this.changeCurrentByOne(false);
                        }
                    }
                };
                OnLongClickListener onLongClickListener = new OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        AmigoNumberPicker.this.mInputText.clearFocus();
                        if (v.getId() == AmigoWidgetResource.getIdentifierById(AmigoNumberPicker.this.getContext(), "amigo_increment")) {
                            AmigoNumberPicker.this.postChangeCurrentByOneFromLongPress(true);
                        } else {
                            AmigoNumberPicker.this.postChangeCurrentByOneFromLongPress(false);
                        }
                        return true;
                    }
                };
                this.mIncrementButton = (ImageButton) findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_increment"));
                this.mIncrementButton.setOnClickListener(onClickListener);
                this.mIncrementButton.setOnLongClickListener(onLongClickListener);
                this.mDecrementButton = (ImageButton) findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_decrement"));
                this.mDecrementButton.setOnClickListener(onClickListener);
                this.mDecrementButton.setOnLongClickListener(onLongClickListener);
                this.mInputText = (EditText) findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_numberpicker_input"));
                this.mInputText.setOnFocusChangeListener(new OnFocusChangeListener() {
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            AmigoNumberPicker.this.mInputText.setSelection(0, 0);
                            AmigoNumberPicker.this.validateInputTextView(v);
                        }
                    }
                });
                this.mInputText.setFilters(new InputFilter[]{new InputTextFilter()});
                this.mInputText.setRawInputType(2);
                this.mInputText.setImeOptions(6);
                this.mInputText.setTextColor(getTextColorAccent(this.mInputText.getTextColors()));
                this.mTouchSlop = ViewConfiguration.getTapTimeout();
                ViewConfiguration configuration = ViewConfiguration.get(context);
                this.mTouchSlop = configuration.getScaledTouchSlop();
                this.mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
                this.mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity() / 8;
                this.mTextSize = (int) this.mInputText.getTextSize();
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setTextAlign(Align.CENTER);
                paint.setTextSize((float) this.mTextSize);
                paint.setTypeface(this.mInputText.getTypeface());
                paint.setColor(0xff888888);
                this.mSelectorWheelPaint = paint;
                int[] iArr = new int[2];
                this.mDimSelectorWheelAnimator = ObjectAnimator.ofInt(this, PROPERTY_SELECTOR_PAINT_ALPHA, new int[]{255, SELECTOR_WHEEL_DIM_ALPHA});
                float[] fArr = new float[2];
                final ObjectAnimator showIncrementButton = ObjectAnimator.ofFloat(this.mIncrementButton, PROPERTY_BUTTON_ALPHA, new float[]{0.0f, IPhotoView.DEFAULT_MIN_SCALE});
                fArr = new float[2];
                final ObjectAnimator showDecrementButton = ObjectAnimator.ofFloat(this.mDecrementButton, PROPERTY_BUTTON_ALPHA, new float[]{0.0f, IPhotoView.DEFAULT_MIN_SCALE});
                this.mShowInputControlsAnimator = new AnimatorSet();
                this.mShowInputControlsAnimator.playTogether(new Animator[]{this.mDimSelectorWheelAnimator, showIncrementButton, showDecrementButton});
                this.mShowInputControlsAnimator.addListener(new AnimatorListenerAdapter() {
                    private boolean mCanceled = false;

                    public void onAnimationEnd(Animator animation) {
                        if (!this.mCanceled) {
                            AmigoNumberPicker.this.setSelectorWheelState(1);
                        }
                        this.mCanceled = false;
                        showIncrementButton.setCurrentPlayTime(showIncrementButton.getDuration());
                        showDecrementButton.setCurrentPlayTime(showDecrementButton.getDuration());
                    }

                    public void onAnimationCancel(Animator animation) {
                        if (AmigoNumberPicker.this.mShowInputControlsAnimator.isRunning()) {
                            this.mCanceled = true;
                        }
                    }
                });
                this.mFlingScroller = new Scroller(getContext(), null, true);
                this.mAdjustScroller = new Scroller(getContext(), new DecelerateInterpolator(2.5f));
                updateInputTextView();
                updateIncrementAndDecrementButtonsVisibilityState();
                if (this.mFlingable) {
                    if (isInEditMode()) {
                        setSelectorWheelState(1);
                    } else {
                        setSelectorWheelState(2);
                        hideInputControls();
                    }
                }
                changeColor();
                return;
            }
            throw new IllegalArgumentException("minWidth > maxWidth");
        }
        throw new IllegalArgumentException("minHeight > maxHeight");
    }

    private ColorStateList getTextColorAccent(ColorStateList textColors) {
        int[][] newStates = new int[2][];//change
        newStates[0] = new int[]{-16842910};
        newStates[1] = new int[0];
        return new ColorStateList(newStates, new int[]{getDisableColor(textColors), -28672});
    }

    private int getDisableColor(ColorStateList textColors) {
        return AmigoReflectionUtil.getDisableColor(textColors);
    }

    private void changeColor() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            this.mSelectionTopDivider.setColorFilter(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1(), Mode.SRC_IN);
            this.mSelectionBottomDivider.setColorFilter(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1(), Mode.SRC_IN);
            this.mSelectorWheelPaint.setColor(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2());
            this.mInputText.setTextColor(ChameleonColorManager.getAccentColor_G1());
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int msrdWdth = getMeasuredWidth();
        int msrdHght = getMeasuredHeight();
        int inctBtnMsrdWdth = this.mIncrementButton.getMeasuredWidth();
        int incrBtnLeft = (msrdWdth - inctBtnMsrdWdth) / 2;
        this.mIncrementButton.layout(incrBtnLeft, 0, incrBtnLeft + inctBtnMsrdWdth, this.mIncrementButton.getMeasuredHeight() + 0);
        int inptTxtMsrdWdth = this.mInputText.getMeasuredWidth();
        int inptTxtMsrdHght = this.mInputText.getMeasuredHeight();
        int inptTxtLeft = (msrdWdth - inptTxtMsrdWdth) / 2;
        int inptTxtTop = (msrdHght - inptTxtMsrdHght) / 2;
        this.mInputText.layout(inptTxtLeft, inptTxtTop, inptTxtLeft + inptTxtMsrdWdth, inptTxtTop + inptTxtMsrdHght);
        int decrBtnMsrdWdth = this.mIncrementButton.getMeasuredWidth();
        int decrBtnLeft = (msrdWdth - decrBtnMsrdWdth) / 2;
        this.mDecrementButton.layout(decrBtnLeft, msrdHght - this.mDecrementButton.getMeasuredHeight(), decrBtnLeft + decrBtnMsrdWdth, msrdHght);
        if (!this.mScrollWheelAndFadingEdgesInitialized) {
            this.mScrollWheelAndFadingEdgesInitialized = true;
            initializeSelectorWheel();
            initializeFadingEdges();
        }
        if (this.mAlign == Align.CENTER) {
            this.xPosition = (float) (getWidth() / 2);
        }
        if (this.mAlign == Align.LEFT) {
            this.xPosition = (float) (this.mInputText.getLeft() + 6);
        }
        if (this.mAlign == Align.RIGHT) {
            this.xPosition = (float) (this.mInputText.getRight() - 6);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        this.mScrollWheelAndFadingEdgesInitialized = false;
        super.onConfigurationChanged(newConfig);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(makeMeasureSpec(widthMeasureSpec, this.mMaxWidth), makeMeasureSpec(heightMeasureSpec, this.mMaxHeight));
        setMeasuredDimension(resolveSizeAndStateRespectingMinSize(this.mMinWidth, getMeasuredWidth(), widthMeasureSpec), resolveSizeAndStateRespectingMinSize(this.mMinHeight, getMeasuredHeight(), heightMeasureSpec));
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled() || !this.mFlingable) {
            return false;
        }
        switch (event.getActionMasked()) {
            case 0:
                float y = event.getY();
                this.mLastDownEventY = y;
                this.mLastMotionEventY = y;
                removeAllCallbacks();
                this.mShowInputControlsAnimator.cancel();
                this.mDimSelectorWheelAnimator.cancel();
                this.mCheckBeginEditOnUpEvent = false;
                this.mAdjustScrollerOnUpEvent = true;
                if (this.mSelectorWheelState == 2) {
                    boolean scrollersFinished;
                    this.mSelectorWheelPaint.setAlpha(255);
                    if (this.mFlingScroller.isFinished() && this.mAdjustScroller.isFinished()) {
                        scrollersFinished = true;
                    } else {
                        scrollersFinished = false;
                    }
                    if (!scrollersFinished) {
                        this.mFlingScroller.forceFinished(true);
                        this.mAdjustScroller.forceFinished(true);
                        onScrollStateChange(0);
                    }
                    this.mCheckBeginEditOnUpEvent = scrollersFinished;
                    this.mAdjustScrollerOnUpEvent = true;
                    hideInputControls();
                    return true;
                } else if (isEventInVisibleViewHitRect(event, this.mIncrementButton) || isEventInVisibleViewHitRect(event, this.mDecrementButton)) {
                    return false;
                } else {
                    this.mAdjustScrollerOnUpEvent = false;
                    setSelectorWheelState(2);
                    hideInputControls();
                    return true;
                }
            case 2:
                if (((int) Math.abs(event.getY() - this.mLastDownEventY)) > this.mTouchSlop) {
                    this.mCheckBeginEditOnUpEvent = false;
                    onScrollStateChange(1);
                    setSelectorWheelState(2);
                    hideInputControls();
                    return true;
                }
                break;
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(ev);
        switch (ev.getActionMasked()) {
            case 1:
                if (this.mCheckBeginEditOnUpEvent) {
                    this.mCheckBeginEditOnUpEvent = false;
                    if (ev.getEventTime() - this.mLastUpEventTimeMillis < ((long) ViewConfiguration.getDoubleTapTimeout())) {
                        setSelectorWheelState(1);
                        showInputControls(this.mShowInputControlsAnimimationDuration);
                        this.mInputText.requestFocus();
                        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService("input_method");
                        if (inputMethodManager != null) {
                            inputMethodManager.showSoftInput(this.mInputText, 0);
                        }
                        this.mLastUpEventTimeMillis = ev.getEventTime();
                        return true;
                    }
                }
                VelocityTracker velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumFlingVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity();
                if (Math.abs(initialVelocity) > this.mMinimumFlingVelocity) {
                    fling(initialVelocity);
                    onScrollStateChange(2);
                } else if (!this.mAdjustScrollerOnUpEvent) {
                    postAdjustScrollerCommand(SHOW_INPUT_CONTROLS_DELAY_MILLIS);
                } else if (this.mFlingScroller.isFinished() && this.mAdjustScroller.isFinished()) {
                    postAdjustScrollerCommand(0);
                }
                this.mVelocityTracker.recycle();
                this.mVelocityTracker = null;
                this.mLastUpEventTimeMillis = ev.getEventTime();
                break;
            case 2:
                float currentMoveY = ev.getY();
                if ((this.mCheckBeginEditOnUpEvent || this.mScrollState != 1) && ((int) Math.abs(currentMoveY - this.mLastDownEventY)) > this.mTouchSlop) {
                    this.mCheckBeginEditOnUpEvent = false;
                    onScrollStateChange(1);
                }
                scrollBy(0, (int) (currentMoveY - this.mLastMotionEventY));
                invalidate();
                this.mLastMotionEventY = currentMoveY;
                break;
        }
        return true;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case 1:
            case 3:
                removeAllCallbacks();
                break;
            case 2:
                if (this.mSelectorWheelState == 2) {
                    removeAllCallbacks();
                    forceCompleteChangeCurrentByOneViaScroll();
                    break;
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == 23 || keyCode == 66) {
            removeAllCallbacks();
        }
        return super.dispatchKeyEvent(event);
    }

    public boolean dispatchTrackballEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == 3 || action == 1) {
            removeAllCallbacks();
        }
        return super.dispatchTrackballEvent(event);
    }

    public void computeScroll() {
        if (this.mSelectorWheelState != 0) {
            Scroller scroller = this.mFlingScroller;
            if (scroller.isFinished()) {
                scroller = this.mAdjustScroller;
                if (scroller.isFinished()) {
                    return;
                }
            }
            scroller.computeScrollOffset();
            int currentScrollerY = scroller.getCurrY();
            if (this.mPreviousScrollerY == 0) {
                this.mPreviousScrollerY = scroller.getStartY();
            }
            scrollBy(0, currentScrollerY - this.mPreviousScrollerY);
            this.mPreviousScrollerY = currentScrollerY;
            if (scroller.isFinished()) {
                onScrollerFinished(scroller);
            } else {
                invalidate();
            }
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mIncrementButton.setEnabled(enabled);
        this.mDecrementButton.setEnabled(enabled);
        this.mInputText.setEnabled(enabled);
    }

    public void scrollBy(int x, int y) {
        if (this.mSelectorWheelState != 0) {
            int[] selectorIndices = this.mSelectorIndices;
            if (!this.mWrapSelectorWheel && y > 0 && selectorIndices[2] <= this.mMinValue) {
                this.mCurrentScrollOffset = this.mInitialScrollOffset;
            } else if (this.mWrapSelectorWheel || y >= 0 || selectorIndices[2] < this.mMaxValue) {
                this.mCurrentScrollOffset += y;
                while (this.mCurrentScrollOffset - this.mInitialScrollOffset > this.mSelectorTextGapHeight) {
                    this.mCurrentScrollOffset -= this.mSelectorElementHeight;
                    decrementSelectorIndices(selectorIndices);
                    changeCurrent(selectorIndices[2]);
                    if (!this.mWrapSelectorWheel && selectorIndices[2] <= this.mMinValue) {
                        this.mCurrentScrollOffset = this.mInitialScrollOffset;
                    }
                }
                while (this.mCurrentScrollOffset - this.mInitialScrollOffset < (-this.mSelectorTextGapHeight)) {
                    this.mCurrentScrollOffset += this.mSelectorElementHeight;
                    incrementSelectorIndices(selectorIndices);
                    changeCurrent(selectorIndices[2]);
                    if (!this.mWrapSelectorWheel && selectorIndices[2] >= this.mMaxValue) {
                        this.mCurrentScrollOffset = this.mInitialScrollOffset;
                    }
                }
            } else {
                this.mCurrentScrollOffset = this.mInitialScrollOffset;
            }
        }
    }

    public int getSolidColor() {
        return this.mSolidColor;
    }

    public void setOnValueChangedListener(OnValueChangeListener onValueChangedListener) {
        this.mOnValueChangeListener = onValueChangedListener;
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mOnScrollListener = onScrollListener;
    }

    public void setFormatter(Formatter formatter) {
        if (formatter != this.mFormatter) {
            this.mFormatter = formatter;
            initializeSelectorWheelIndices();
            updateInputTextView();
        }
    }

    public void setValue(int value) {
        if (this.mValue != value) {
            if (value < this.mMinValue) {
                value = this.mWrapSelectorWheel ? this.mMaxValue : this.mMinValue;
            }
            if (value > this.mMaxValue) {
                value = this.mWrapSelectorWheel ? this.mMinValue : this.mMaxValue;
            }
            this.mValue = value;
            initializeSelectorWheelIndices();
            updateInputTextView();
            updateIncrementAndDecrementButtonsVisibilityState();
            invalidate();
        }
    }

    private void tryComputeMaxWidth() {
        if (this.mComputeMaxWidth) {
            int maxTextWidth = 0;
            int i;
            if (this.mDisplayedValues == null) {
                float maxDigitWidth = 0.0f;
                for (i = 0; i <= 9; i++) {
                    float digitWidth = this.mSelectorWheelPaint.measureText(String.valueOf(i));
                    if (digitWidth > maxDigitWidth) {
                        maxDigitWidth = digitWidth;
                    }
                }
                int numberOfDigits = 0;
                for (int current = this.mMaxValue; current > 0; current /= 10) {
                    numberOfDigits++;
                }
                maxTextWidth = (int) (((float) numberOfDigits) * maxDigitWidth);
            } else {
                for (String measureText : this.mDisplayedValues) {
                    float textWidth = this.mSelectorWheelPaint.measureText(measureText);
                    if (textWidth > ((float) maxTextWidth)) {
                        maxTextWidth = (int) textWidth;
                    }
                }
            }
            maxTextWidth += this.mInputText.getPaddingLeft() + this.mInputText.getPaddingRight();
            if (this.mMaxWidth != maxTextWidth) {
                if (maxTextWidth > this.mMinWidth) {
                    this.mMaxWidth = maxTextWidth;
                } else {
                    this.mMaxWidth = this.mMinWidth;
                }
                invalidate();
            }
        }
    }

    public boolean getWrapSelectorWheel() {
        return this.mWrapSelectorWheel;
    }

    public void setWrapSelectorWheel(boolean wrapSelectorWheel) {
        if (wrapSelectorWheel && this.mMaxValue - this.mMinValue < this.mSelectorIndices.length) {
            Log.d("AmigoNumberPicker", "Range less than selector items count");
            wrapSelectorWheel = false;
        }
        if (wrapSelectorWheel != this.mWrapSelectorWheel) {
            this.mWrapSelectorWheel = wrapSelectorWheel;
            updateIncrementAndDecrementButtonsVisibilityState();
        }
    }

    public void setOnLongPressUpdateInterval(long intervalMillis) {
        this.mLongPressUpdateInterval = intervalMillis;
    }

    public int getValue() {
        return this.mValue;
    }

    public int getMinValue() {
        return this.mMinValue;
    }

    public void setMinValue(int minValue) {
        if (this.mMinValue != minValue) {
            if (minValue < 0) {
                throw new IllegalArgumentException("minValue must be >= 0");
            }
            this.mMinValue = minValue;
            if (this.mMinValue > this.mValue) {
                this.mValue = this.mMinValue;
            }
            setWrapSelectorWheel(this.mMaxValue - this.mMinValue > this.mSelectorIndices.length);
            initializeSelectorWheelIndices();
            updateInputTextView();
            tryComputeMaxWidth();
        }
    }

    public int getMaxValue() {
        return this.mMaxValue;
    }

    public void setMaxValue(int maxValue) {
        if (this.mMaxValue != maxValue) {
            if (maxValue < 0) {
                throw new IllegalArgumentException("maxValue must be >= 0");
            }
            this.mMaxValue = maxValue;
            if (this.mMaxValue < this.mValue) {
                this.mValue = this.mMaxValue;
            }
            setWrapSelectorWheel(this.mMaxValue - this.mMinValue > this.mSelectorIndices.length);
            initializeSelectorWheelIndices();
            updateInputTextView();
            tryComputeMaxWidth();
        }
    }

    public String[] getDisplayedValues() {
        return this.mDisplayedValues;
    }

    public void setDisplayedValues(String[] displayedValues) {
        if (this.mDisplayedValues != displayedValues) {
            this.mDisplayedValues = displayedValues;
            if (this.mDisplayedValues != null) {
                this.mInputText.setRawInputType(524289);
            } else {
                this.mInputText.setRawInputType(2);
            }
            updateInputTextView();
            initializeSelectorWheelIndices();
            tryComputeMaxWidth();
        }
    }

    protected float getTopFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    protected float getBottomFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mFlingable && !isInEditMode()) {
            showInputControls(this.mShowInputControlsAnimimationDuration * 2);
        }
    }

    protected void onDetachedFromWindow() {
        removeAllCallbacks();
        super.onDetachedFromWindow();
    }

    protected void dispatchDraw(Canvas canvas) {
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.mShowInputControlsAnimator.isRunning() || this.mSelectorWheelState != 2) {
            long drawTime = getDrawingTime();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                if (getChildAt(i).isShown()) {
                    drawChild(canvas, getChildAt(i), drawTime);
                }
            }
        }
    }

    protected void onDraw(Canvas canvas) {
        if (this.mSelectorWheelState != 0) {
            drawDivider(canvas);
            drawMiddleWheel(canvas);
            drawAboveWheel(canvas);
            drawBelowWheel(canvas);
        }
    }

    private void drawDivider(Canvas canvas) {
        int saveCount = canvas.save();
        if (this.mSelectionTopDivider != null && this.mSelectionBottomDivider != null) {
            this.bottomOfTopDivider = (int) (((double) getHeight()) * 0.35d);
            this.mSelectionTopDivider.setBounds(0, this.bottomOfTopDivider - this.mSelectionDividerHeight, getRight(), this.bottomOfTopDivider);
            this.mSelectionTopDivider.draw(canvas);
            this.topOfBottomDivider = (int) (((double) getHeight()) * 0.65d);
            this.mSelectionBottomDivider.setBounds(0, this.topOfBottomDivider, getRight(), this.topOfBottomDivider + this.mSelectionDividerHeight);
            this.mSelectionBottomDivider.draw(canvas);
        } else if (this.mSelectionSrc != null) {
            int topOfTopDivider = ((getHeight() - this.mSelectorElementHeight) - this.mSelectionDividerHeight) / 2;
            this.mSelectionSrc.setBounds(0, topOfTopDivider, getWidth(), topOfTopDivider + this.mSelectorElementHeight);
            this.mSelectionSrc.draw(canvas);
        }
        canvas.restoreToCount(saveCount);
    }

    private void drawMiddleWheel(Canvas canvas) {
        int saveCount = canvas.save();
        float yOfSmallScroll = (float) this.mCurrentScrollOffset;
        canvas.clipRect(0, this.bottomOfTopDivider, getWidth(), this.topOfBottomDivider);
        Paint paint = initMiddleWheelPaint();
        int[] selectorIndices = this.mSelectorIndices;
        for (int i = 0; i < selectorIndices.length; i++) {
            String scrollSelectorValue = (String) this.mSelectorIndexToStringCache.get(selectorIndices[i]);
            if (i != 2 || this.mInputText.getVisibility() != 0) {
                canvas.drawText(scrollSelectorValue, this.xPosition, yOfSmallScroll, paint);
            }
            yOfSmallScroll = (float) (((double) yOfSmallScroll) + (((double) getHeight()) * 0.3d));
        }
        canvas.restoreToCount(saveCount);
    }

    private Paint initMiddleWheelPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(this.mAlign);
        paint.setTextSize((float) this.mTextSize);
        paint.setTypeface(this.mInputText.getTypeface());
        paint.setColor(this.mInputText.getTextColors().getColorForState(ENABLED_STATE_SET, -1));
        return paint;
    }

    public void setAlign(Align align) {
        this.mAlign = align;
        if (align == Align.CENTER) {
            this.mInputText.setGravity(17);
        } else if (align == Align.LEFT) {
            this.mInputText.setGravity(3);
        } else if (align == Align.RIGHT) {
            this.mInputText.setGravity(5);
        }
    }

    private void drawAboveWheel(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.clipRect(0, 0, getWidth(), this.bottomOfTopDivider);
        int[] selectorIndices = this.mSelectorIndices;
        float y = ((float) (0.875d * ((double) (((float) this.mCurrentScrollOffset) + (0.25f * ((float) getHeight())))))) / 1.5f;
        float desPosition = (float) (this.mInputText.getBaseline() + this.mInputText.getTop());
        float scale = IPhotoView.DEFAULT_MIN_SCALE;
        float textSize = IPhotoView.DEFAULT_MIN_SCALE;
        float x = this.xPosition;
        if (this.mAlign == Align.LEFT) {
            x = this.xPosition + 4.0f;
        } else if (this.mAlign == Align.RIGHT) {
            x = this.xPosition - 8.0f;
        }
        for (int i = 0; i < selectorIndices.length; i++) {
            String text = (String) this.mSelectorIndexToStringCache.get(selectorIndices[i]);
            if (!(i == 2 && this.mInputText.getVisibility() == 0)) {
                if (y <= desPosition) {
                    scale = y / desPosition;
                }
                if (scale < 0.4f) {
                    scale = 0.4f;
                }
                textSize = scale * ((float) this.mTextSize);
                this.mSelectorWheelPaint.setTextSize(textSize);
                this.mSelectorWheelPaint.setTextAlign(this.mAlign);
                canvas.drawText(text, x, y, this.mSelectorWheelPaint);
            }
            y += (((float) getHeight()) * 0.35f) / 2.0f;
            if (y >= ((float) this.bottomOfTopDivider)) {
                y += textSize * 0.3f;
            }
        }
        canvas.restoreToCount(saveCount);
    }

    private void drawBelowWheel(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.clipRect(0, this.topOfBottomDivider, getWidth(), getHeight());
        float transY = ((float) this.topOfBottomDivider) - (((((float) getHeight()) * 0.35f) / 2.0f) * IPhotoView.DEFAULT_MAX_SCALE);
        canvas.translate(0.0f, transY);
        int[] selectorIndices = this.mSelectorIndices;
        float y = ((float) (0.875d * ((double) (((float) this.mCurrentScrollOffset) + (0.25f * ((float) getHeight())))))) / 1.5f;
        float desPosition = (float) (this.mInputText.getBaseline() + this.mInputText.getTop());
        float scale = IPhotoView.DEFAULT_MIN_SCALE;
        float x = this.xPosition;
        if (this.mAlign == Align.LEFT) {
            x = this.xPosition + 4.0f;
        } else if (this.mAlign == Align.RIGHT) {
            x = this.xPosition - 8.0f;
        }
        for (int i = 0; i < selectorIndices.length; i++) {
            String text = (String) this.mSelectorIndexToStringCache.get(selectorIndices[i]);
            if (i != 2 || this.mInputText.getVisibility() != 0) {
                if (y >= ((float) this.topOfBottomDivider) - transY) {
                    scale = (((float) getHeight()) - y) / desPosition;
                }
                if (scale <= 0.4f) {
                    scale = 0.4f;
                }
                this.mSelectorWheelPaint.setTextSize(scale * ((float) this.mTextSize));
                this.mSelectorWheelPaint.setTextAlign(this.mAlign);
                canvas.drawText(text, x, y, this.mSelectorWheelPaint);
            }
            y += (((float) getHeight()) * 0.35f) / 2.0f;
        }
        canvas.restoreToCount(saveCount);
    }

    public void setSelectionSrc(Drawable drawable) {
        this.mSelectionSrc = drawable;
    }

    public void sendAccessibilityEvent(int eventType) {
    }

    private int makeMeasureSpec(int measureSpec, int maxSize) {
        if (maxSize == -1) {
            return measureSpec;
        }
        int size = MeasureSpec.getSize(measureSpec);
        int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case Integer.MIN_VALUE:
                return MeasureSpec.makeMeasureSpec(Math.min(size, maxSize), 1073741824);
            case 0:
                return MeasureSpec.makeMeasureSpec(maxSize, 1073741824);
            case 1073741824:
                return measureSpec;
            default:
                throw new IllegalArgumentException("Unknown measure mode: " + mode);
        }
    }

    private int resolveSizeAndStateRespectingMinSize(int minSize, int measuredSize, int measureSpec) {
        if (minSize != -1) {
            return resolveSizeAndState(Math.max(minSize, measuredSize), measureSpec, 0);
        }
        return measuredSize;
    }

    private void initializeSelectorWheelIndices() {
        this.mSelectorIndexToStringCache.clear();
        int[] selectorIdices = this.mSelectorIndices;
        int current = getValue();
        for (int i = 0; i < this.mSelectorIndices.length; i++) {
            int selectorIndex = current + (i - 2);
            if (this.mWrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex);
            }
            this.mSelectorIndices[i] = selectorIndex;
            ensureCachedScrollSelectorValue(this.mSelectorIndices[i]);
        }
    }

    private void changeCurrent(int current) {
        if (this.mValue != current) {
            if (this.mWrapSelectorWheel) {
                current = getWrappedSelectorIndex(current);
            }
            int previous = this.mValue;
            setValue(current);
            notifyChange(previous, current);
        }
    }

    private void changeCurrentByOne(boolean increment) {
        if (this.mFlingable) {
            this.mDimSelectorWheelAnimator.cancel();
            this.mInputText.setVisibility(4);
            this.mSelectorWheelPaint.setAlpha(255);
            this.mPreviousScrollerY = 0;
            forceCompleteChangeCurrentByOneViaScroll();
            if (increment) {
                this.mFlingScroller.startScroll(0, 0, 0, -this.mSelectorElementHeight, CHANGE_CURRENT_BY_ONE_SCROLL_DURATION);
            } else {
                this.mFlingScroller.startScroll(0, 0, 0, this.mSelectorElementHeight, CHANGE_CURRENT_BY_ONE_SCROLL_DURATION);
            }
            invalidate();
        } else if (increment) {
            changeCurrent(this.mValue + 1);
        } else {
            changeCurrent(this.mValue - 1);
        }
    }

    private void forceCompleteChangeCurrentByOneViaScroll() {
        Scroller scroller = this.mFlingScroller;
        if (!scroller.isFinished()) {
            int yBeforeAbort = scroller.getCurrY();
            scroller.abortAnimation();
            scrollBy(0, scroller.getCurrY() - yBeforeAbort);
        }
    }

    private void setSelectorPaintAlpha(int alpha) {
        this.mSelectorWheelPaint.setAlpha(alpha);
        invalidate();
    }

    private boolean isEventInVisibleViewHitRect(MotionEvent event, View view) {
        if (view.getVisibility() != 0) {
            return false;
        }
        view.getHitRect(this.mTempRect);
        return this.mTempRect.contains((int) event.getX(), (int) event.getY());
    }

    private void setSelectorWheelState(int selectorWheelState) {
        this.mSelectorWheelState = selectorWheelState;
        if (selectorWheelState == 2) {
            this.mSelectorWheelPaint.setAlpha(255);
        }
    }

    private void initializeSelectorWheel() {
        initializeSelectorWheelIndices();
        initTextGapHeight();
        this.mSelectorElementHeight = (int) (((double) getHeight()) * 0.3d);
        this.mInitialScrollOffset = (this.mInputText.getBaseline() + this.mInputText.getTop()) - (this.mSelectorElementHeight * 2);
        this.mCurrentScrollOffset = this.mInitialScrollOffset;
        updateInputTextView();
    }

    private void initTextGapHeight() {
        this.mSelectorTextGapHeight = (int) ((((float) (getHeight() - (this.mTextSize * 3))) / 2.0f) + 0.5f);
    }

    private void initializeFadingEdges() {
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(((getBottom() - getTop()) - this.mTextSize) / 2);
    }

    private void onScrollerFinished(Scroller scroller) {
        if (scroller != this.mFlingScroller) {
            updateInputTextView();
            showInputControls(this.mShowInputControlsAnimimationDuration);
        } else if (this.mSelectorWheelState == 2) {
            postAdjustScrollerCommand(0);
            onScrollStateChange(0);
        } else {
            updateInputTextView();
            fadeSelectorWheel(this.mShowInputControlsAnimimationDuration);
        }
    }

    private void onScrollStateChange(int scrollState) {
        if (this.mScrollState != scrollState) {
            this.mScrollState = scrollState;
            if (this.mOnScrollListener != null) {
                this.mOnScrollListener.onScrollStateChange(this, scrollState);
            }
        }
    }

    private void fling(int velocityY) {
        this.mPreviousScrollerY = 0;
        if (velocityY > 0) {
            this.mFlingScroller.fling(0, 0, 0, velocityY, 0, 0, 0, AmigoPreference.DEFAULT_ORDER);
        } else {
            this.mFlingScroller.fling(0, AmigoPreference.DEFAULT_ORDER, 0, velocityY, 0, 0, 0, AmigoPreference.DEFAULT_ORDER);
        }
        invalidate();
    }

    private void hideInputControls() {
        this.mShowInputControlsAnimator.cancel();
        this.mIncrementButton.setVisibility(4);
        this.mDecrementButton.setVisibility(4);
        this.mInputText.setVisibility(4);
    }

    private void showInputControls(long animationDuration) {
        updateIncrementAndDecrementButtonsVisibilityState();
        this.mInputText.setVisibility(0);
        this.mShowInputControlsAnimator.setDuration(animationDuration);
        this.mShowInputControlsAnimator.start();
    }

    private void fadeSelectorWheel(long animationDuration) {
        this.mInputText.setVisibility(0);
        this.mDimSelectorWheelAnimator.setDuration(animationDuration);
        this.mDimSelectorWheelAnimator.start();
    }

    private void updateIncrementAndDecrementButtonsVisibilityState() {
        if (this.mWrapSelectorWheel || this.mValue < this.mMaxValue) {
            this.mIncrementButton.setVisibility(0);
        } else {
            this.mIncrementButton.setVisibility(4);
        }
        if (this.mWrapSelectorWheel || this.mValue > this.mMinValue) {
            this.mDecrementButton.setVisibility(0);
        } else {
            this.mDecrementButton.setVisibility(4);
        }
    }

    private int getWrappedSelectorIndex(int selectorIndex) {
        if (selectorIndex > this.mMaxValue) {
            return (this.mMinValue + ((selectorIndex - this.mMaxValue) % (this.mMaxValue - this.mMinValue))) - 1;
        }
        if (selectorIndex < this.mMinValue) {
            return (this.mMaxValue - ((this.mMinValue - selectorIndex) % (this.mMaxValue - this.mMinValue))) + 1;
        }
        return selectorIndex;
    }

    private void incrementSelectorIndices(int[] selectorIndices) {
        for (int i = 0; i < selectorIndices.length - 1; i++) {
            selectorIndices[i] = selectorIndices[i + 1];
        }
        int nextScrollSelectorIndex = selectorIndices[selectorIndices.length - 2] + 1;
        if (this.mWrapSelectorWheel && nextScrollSelectorIndex > this.mMaxValue) {
            nextScrollSelectorIndex = this.mMinValue;
        }
        selectorIndices[selectorIndices.length - 1] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    private void decrementSelectorIndices(int[] selectorIndices) {
        for (int i = selectorIndices.length - 1; i > 0; i--) {
            selectorIndices[i] = selectorIndices[i - 1];
        }
        int nextScrollSelectorIndex = selectorIndices[1] - 1;
        if (this.mWrapSelectorWheel && nextScrollSelectorIndex < this.mMinValue) {
            nextScrollSelectorIndex = this.mMaxValue;
        }
        selectorIndices[0] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    private void ensureCachedScrollSelectorValue(int selectorIndex) {
        SparseArray<String> cache = this.mSelectorIndexToStringCache;
        if (((String) cache.get(selectorIndex)) == null) {
            String scrollSelectorValue;
            if (selectorIndex < this.mMinValue || selectorIndex > this.mMaxValue) {
                scrollSelectorValue = "";
            } else if (this.mDisplayedValues != null) {
                scrollSelectorValue = this.mDisplayedValues[selectorIndex - this.mMinValue];
            } else {
                scrollSelectorValue = formatNumber(selectorIndex);
            }
            cache.put(selectorIndex, scrollSelectorValue);
        }
    }

    private String formatNumber(int value) {
        return this.mFormatter != null ? this.mFormatter.format(value) : String.valueOf(value);
    }

    private void validateInputTextView(View v) {
        String str = String.valueOf(((TextView) v).getText());
        if (TextUtils.isEmpty(str)) {
            updateInputTextView();
        } else {
            changeCurrent(getSelectedPos(str.toString()));
        }
    }

    private void updateInputTextView() {
        if (this.mDisplayedValues == null) {
            this.mInputText.setText(formatNumber(this.mValue));
        } else {
            this.mInputText.setText(this.mDisplayedValues[this.mValue - this.mMinValue]);
        }
        this.mInputText.setSelection(this.mInputText.getText().length());
    }

    private void notifyChange(int previous, int current) {
        if (this.mOnValueChangeListener != null) {
            this.mOnValueChangeListener.onValueChange(this, previous, this.mValue);
        }
    }

    private void postChangeCurrentByOneFromLongPress(boolean increment) {
        this.mInputText.clearFocus();
        removeAllCallbacks();
        if (this.mChangeCurrentByOneFromLongPressCommand == null) {
            this.mChangeCurrentByOneFromLongPressCommand = new ChangeCurrentByOneFromLongPressCommand();
        }
        this.mChangeCurrentByOneFromLongPressCommand.setIncrement(increment);
        post(this.mChangeCurrentByOneFromLongPressCommand);
    }

    private void removeAllCallbacks() {
        if (this.mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(this.mChangeCurrentByOneFromLongPressCommand);
        }
        if (this.mAdjustScrollerCommand != null) {
            removeCallbacks(this.mAdjustScrollerCommand);
        }
        if (this.mSetSelectionCommand != null) {
            removeCallbacks(this.mSetSelectionCommand);
        }
    }

    private int getSelectedPos(String value) {
        if (this.mDisplayedValues == null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return this.mMinValue;
            }
        }
        for (int i = 0; i < this.mDisplayedValues.length; i++) {
            value = value.toLowerCase();
            if (this.mDisplayedValues[i].toLowerCase().startsWith(value)) {
                return this.mMinValue + i;
            }
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e2) {
            return this.mMinValue;
        }
    }

    private void postSetSelectionCommand(int selectionStart, int selectionEnd) {
        if (this.mSetSelectionCommand == null) {
            this.mSetSelectionCommand = new SetSelectionCommand();
        } else {
            removeCallbacks(this.mSetSelectionCommand);
        }
        this.mSetSelectionCommand.mSelectionStart = selectionStart;
        this.mSetSelectionCommand.mSelectionEnd = selectionEnd;
        post(this.mSetSelectionCommand);
    }

    private void postAdjustScrollerCommand(int delayMillis) {
        if (this.mAdjustScrollerCommand == null) {
            this.mAdjustScrollerCommand = new AdjustScrollerCommand();
        } else {
            removeCallbacks(this.mAdjustScrollerCommand);
        }
        postDelayed(this.mAdjustScrollerCommand, (long) delayMillis);
    }
}
