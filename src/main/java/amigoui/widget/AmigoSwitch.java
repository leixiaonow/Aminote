package amigoui.widget;

import com.gionee.aminote.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CompoundButton;
import java.util.Locale;
import uk.co.senab.photoview.IPhotoView;

public class AmigoSwitch extends CompoundButton {
    private static final int[] CHECKED_STATE_SET = new int[]{16842912};
    private static final int MONOSPACE = 3;
    private static final int SANS = 1;
    private static final int SERIF = 2;
    private static final int TOUCH_MODE_DOWN = 1;
    private static final int TOUCH_MODE_DRAGGING = 2;
    private static final int TOUCH_MODE_IDLE = 0;
    private final int ANIM_THUMB_STEP;
    private final int DOT_OFFSET_X;
    private final int DOT_OFFSET_Y;
    private final int SWITCH_TEXT_MAX_WIDTH;
    private final int SWITCH_TEXT_PADDING_BOTTOM;
    private final int SWITCH_TEXT_PADDING_LEFT;
    private final int SWITCH_TEXT_PADDING_RIGHT;
    private final int SWITCH_TEXT_PADDING_TOP;
    private final int SWITCH_TEXT_SIZE;
    private int mDotHeight;
    private int mDotWidth;
    private boolean mIsAnimation;
    private boolean mIsFirstTime;
    private boolean mIsGioneeSwitchStyle;
    private boolean mIsGioneeWidget3;
    private int mMinFlingVelocity;
    private Layout mOffLayout;
    private Layout mOnLayout;
    private int mSwitchBottom;
    private int mSwitchHeight;
    private int mSwitchLeft;
    private int mSwitchMinWidth;
    private int mSwitchPadding;
    private int mSwitchRight;
    private int mSwitchTop;
    private int mSwitchWidth;
    private final Rect mTempRect;
    private ColorStateList mTextColors;
    private CharSequence mTextOff;
    private CharSequence mTextOn;
    private TextPaint mTextPaint;
    private Drawable mThumbDotOffDrawable;
    private Drawable mThumbDotOnDrawable;
    private Drawable mThumbDrawable;
    private int mThumbHeight;
    private Point mThumbPoint;
    private float mThumbPosition;
    private int mThumbTextPadding;
    private int mThumbWidth;
    private int mTouchMode;
    private int mTouchSlop;
    private float mTouchX;
    private float mTouchY;
    private Drawable mTrackDrawable;
    private Drawable mTrackOffDrawable;
    private Drawable mTrackOnDrawable;
    private int mTrackSignHeight;
    private int mTrackSignLeft;
    private Drawable mTrackSignOffDrawable;
    private Drawable mTrackSignOnDrawable;
    private int mTrackSignRight;
    private int mTrackSignTop;
    private int mTrackSignWidth;
    private VelocityTracker mVelocityTracker;

    class Point {
        private int x;
        private int y;

        Point() {
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public void setX(int x) {
            this.x = x;
        }
    }

    static /* synthetic */ float access$016(AmigoSwitch x0, float x1) {
        float f = x0.mThumbPosition + x1;
        x0.mThumbPosition = f;
        return f;
    }

    static /* synthetic */ float access$024(AmigoSwitch x0, float x1) {
        float f = x0.mThumbPosition - x1;
        x0.mThumbPosition = f;
        return f;
    }

    public AmigoSwitch(Context context) {
        this(context, null);
    }

    public AmigoSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.amigoswitchStyle);
    }

    public AmigoSwitch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mVelocityTracker = VelocityTracker.obtain();
        this.mIsGioneeSwitchStyle = false;
        this.SWITCH_TEXT_PADDING_LEFT = 10;
        this.SWITCH_TEXT_PADDING_RIGHT = 10;
        this.SWITCH_TEXT_PADDING_TOP = 3;
        this.SWITCH_TEXT_PADDING_BOTTOM = 3;
        this.SWITCH_TEXT_MAX_WIDTH = 23;
        this.SWITCH_TEXT_SIZE = 24;
        this.mIsGioneeWidget3 = false;
        this.mIsAnimation = false;
        this.mIsFirstTime = true;
        this.mThumbPoint = new Point();
        this.DOT_OFFSET_X = 12;
        this.DOT_OFFSET_Y = 0;
        this.ANIM_THUMB_STEP = 5;
        this.mTempRect = new Rect();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoSwitch, defStyle, 0);
        this.mTextPaint = new TextPaint(1);
        Resources res = getResources();
        this.mTextPaint.density = res.getDisplayMetrics().density;
        this.mTextPaint.setTextSize(24.0f);
        this.mThumbDrawable = a.getDrawable(R.styleable.AmigoSwitch_amigothumb);
        this.mTrackDrawable = a.getDrawable(R.styleable.AmigoSwitch_amigotrack);
        this.mTextOn = a.getString(R.styleable.AmigoSwitch_amigotextOn);
        this.mTextOff = a.getString(R.styleable.AmigoSwitch_amigotextOff);
        this.mThumbTextPadding = a.getDimensionPixelSize(R.styleable.AmigoSwitch_amigothumbTextPadding, 0);
        this.mSwitchMinWidth = a.getDimensionPixelSize(R.styleable.AmigoSwitch_amigoswitchMinWidth, 0);
        this.mSwitchPadding = a.getDimensionPixelSize(R.styleable.AmigoSwitch_amigoswitchPadding, 0);
        a.recycle();
        this.mIsGioneeSwitchStyle = isGioneeSwitchStyle();
        this.mIsGioneeWidget3 = isGioneeWidget3Support();
        if (this.mIsGioneeSwitchStyle && this.mIsGioneeWidget3) {
            this.mTrackOnDrawable = getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(getContext(), "amigo_switch_track_on"));
            this.mTrackOffDrawable = getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(getContext(), "amigo_switch_track_off"));
            this.mThumbDotOnDrawable = getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(getContext(), "amigo_switch_thumb_dot_on"));
            this.mThumbDotOffDrawable = getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(getContext(), "amigo_switch_thumb_dot_off"));
        }
        ViewConfiguration config = ViewConfiguration.get(context);
        this.mTouchSlop = config.getScaledTouchSlop();
        this.mMinFlingVelocity = config.getScaledMinimumFlingVelocity();
        refreshDrawableState();
        setChecked(isChecked());
    }

    private void setSwitchTypefaceByIndex(int typefaceIndex, int styleIndex) {
        Typeface tf = null;
        switch (typefaceIndex) {
            case 1:
                tf = Typeface.SANS_SERIF;
                break;
            case 2:
                tf = Typeface.SERIF;
                break;
            case 3:
                tf = Typeface.MONOSPACE;
                break;
        }
        setSwitchTypeface(tf, styleIndex);
    }

    public void setSwitchTypeface(Typeface tf, int style) {
        boolean z = false;
        if (style > 0) {
            int typefaceStyle;
            float f;
            if (tf == null) {
                tf = Typeface.defaultFromStyle(style);
            } else {
                tf = Typeface.create(tf, style);
            }
            setSwitchTypeface(tf);
            if (tf != null) {
                typefaceStyle = tf.getStyle();
            } else {
                typefaceStyle = 0;
            }
            int need = style & (typefaceStyle ^ -1);
            TextPaint textPaint = this.mTextPaint;
            if ((need & 1) != 0) {
                z = true;
            }
            textPaint.setFakeBoldText(z);
            textPaint = this.mTextPaint;
            if ((need & 2) != 0) {
                f = -0.25f;
            } else {
                f = 0.0f;
            }
            textPaint.setTextSkewX(f);
            return;
        }
        this.mTextPaint.setFakeBoldText(false);
        this.mTextPaint.setTextSkewX(0.0f);
        setSwitchTypeface(tf);
    }

    public void setSwitchTypeface(Typeface tf) {
        if (this.mTextPaint.getTypeface() != tf) {
            this.mTextPaint.setTypeface(tf);
            requestLayout();
            invalidate();
        }
    }

    public void setSwitchPadding(int pixels) {
        this.mSwitchPadding = pixels;
        requestLayout();
    }

    public int getSwitchPadding() {
        return this.mSwitchPadding;
    }

    public void setSwitchMinWidth(int pixels) {
        this.mSwitchMinWidth = pixels;
        requestLayout();
    }

    public int getSwitchMinWidth() {
        return this.mSwitchMinWidth;
    }

    public void setThumbTextPadding(int pixels) {
        this.mThumbTextPadding = pixels;
        requestLayout();
    }

    public int getThumbTextPadding() {
        return this.mThumbTextPadding;
    }

    public void setTrackDrawable(Drawable track) {
        this.mTrackDrawable = track;
        requestLayout();
    }

    public void setTrackResource(int resId) {
        setTrackDrawable(getContext().getResources().getDrawable(resId));
    }

    public Drawable getTrackDrawable() {
        return this.mTrackDrawable;
    }

    public void setThumbDrawable(Drawable thumb) {
        this.mThumbDrawable = thumb;
        requestLayout();
    }

    public void setThumbResource(int resId) {
        setThumbDrawable(getContext().getResources().getDrawable(resId));
    }

    public Drawable getThumbDrawable() {
        return this.mThumbDrawable;
    }

    public CharSequence getTextOn() {
        return this.mTextOn;
    }

    public void setTextOn(CharSequence textOn) {
        this.mTextOn = textOn;
        requestLayout();
    }

    public CharSequence getTextOff() {
        return this.mTextOff;
    }

    public void setTextOff(CharSequence textOff) {
        this.mTextOff = textOff;
        requestLayout();
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxTextWidth;
        if (this.mOnLayout == null) {
            this.mOnLayout = makeLayout(this.mTextOn);
        }
        if (this.mOffLayout == null) {
            this.mOffLayout = makeLayout(this.mTextOff);
        }
        this.mTrackDrawable.getPadding(this.mTempRect);
        if (!this.mIsGioneeSwitchStyle) {
            maxTextWidth = Math.max(this.mOnLayout.getWidth(), this.mOffLayout.getWidth());
        } else if (this.mIsGioneeWidget3) {
            maxTextWidth = Math.max(this.mOnLayout.getWidth(), this.mOffLayout.getWidth());
        } else if (isChineseLanguage()) {
            maxTextWidth = Math.max(this.mOnLayout.getWidth(), this.mOffLayout.getWidth());
        } else {
            maxTextWidth = 23;
        }
        int switchWidth = Math.max(this.mSwitchMinWidth, (((maxTextWidth * 2) + (this.mThumbTextPadding * 4)) + this.mTempRect.left) + this.mTempRect.right);
        int switchHeight = this.mTrackDrawable.getIntrinsicHeight();
        if (this.mIsGioneeSwitchStyle && this.mIsGioneeWidget3) {
            this.mThumbWidth = this.mThumbDrawable.getIntrinsicWidth();
            this.mDotWidth = this.mThumbDotOnDrawable.getIntrinsicWidth();
            this.mDotHeight = this.mThumbDotOnDrawable.getIntrinsicHeight();
        } else {
            this.mThumbWidth = (this.mThumbTextPadding * 2) + maxTextWidth;
        }
        this.mSwitchWidth = switchWidth;
        this.mSwitchHeight = switchHeight;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredHeight = getMeasuredHeight();
        if (this.mIsGioneeSwitchStyle && this.mIsGioneeWidget3) {
            this.mSwitchHeight = this.mTrackOffDrawable.getIntrinsicHeight();
            this.mSwitchWidth = this.mTrackOnDrawable.getIntrinsicWidth();
        }
        if (measuredHeight < switchHeight) {
            setMeasuredDimension(getMeasuredWidthAndState(), switchHeight);
        }
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);
        Layout layout = isChecked() ? this.mOnLayout : this.mOffLayout;
        if (layout != null && !TextUtils.isEmpty(layout.getText())) {
            event.getText().add(layout.getText());
        }
    }

    private Layout makeLayout(CharSequence text) {
        CharSequence transformed = text;
        return new StaticLayout(transformed, this.mTextPaint, (int) Math.ceil((double) Layout.getDesiredWidth(transformed, this.mTextPaint)), Alignment.ALIGN_NORMAL, IPhotoView.DEFAULT_MIN_SCALE, 0.0f, true);
    }

    private boolean hitThumb(float x, float y) {
        this.mThumbDrawable.getPadding(this.mTempRect);
        int thumbLeft = (this.mSwitchLeft + ((int) (this.mThumbPosition + 0.5f))) - this.mTouchSlop;
        return x > ((float) thumbLeft) && x < ((float) ((((this.mThumbWidth + thumbLeft) + this.mTempRect.left) + this.mTempRect.right) + this.mTouchSlop)) && y > ((float) (this.mSwitchTop - this.mTouchSlop)) && y < ((float) (this.mSwitchBottom + this.mTouchSlop));
    }

    public boolean onTouchEvent(MotionEvent ev) {
        this.mVelocityTracker.addMovement(ev);
        int action = ev.getActionMasked();
        if (!isEnabled()) {
            return true;
        }
        float x;
        float y;
        switch (action) {
            case 0:
                x = ev.getX();
                y = ev.getY();
                if (isEnabled() && hitThumb(x, y)) {
                    this.mTouchMode = 1;
                    this.mTouchX = x;
                    this.mTouchY = y;
                    break;
                }
            case 1:
            case 3:
                if (this.mTouchMode != 2) {
                    this.mTouchMode = 0;
                    this.mVelocityTracker.clear();
                    break;
                }
                stopDrag(ev);
                return true;
            case 2:
                switch (this.mTouchMode) {
                    case 0:
                        break;
                    case 1:
                        x = ev.getX();
                        y = ev.getY();
                        if (Math.abs(x - this.mTouchX) > ((float) this.mTouchSlop) || Math.abs(y - this.mTouchY) > ((float) this.mTouchSlop)) {
                            this.mTouchMode = 2;
                            getParent().requestDisallowInterceptTouchEvent(true);
                            this.mTouchX = x;
                            this.mTouchY = y;
                            return true;
                        }
                    case 2:
                        x = ev.getX();
                        float newPos = Math.max(0.0f, Math.min(this.mThumbPosition + (x - this.mTouchX), (float) getThumbScrollRange()));
                        if (newPos == this.mThumbPosition) {
                            return true;
                        }
                        this.mThumbPosition = newPos;
                        this.mTouchX = x;
                        invalidate();
                        return true;
                    default:
                        break;
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void cancelSuperTouch(MotionEvent ev) {
        MotionEvent cancel = MotionEvent.obtain(ev);
        cancel.setAction(3);
        super.onTouchEvent(cancel);
        cancel.recycle();
    }

    private void stopDrag(MotionEvent ev) {
        boolean commitChange;
        this.mTouchMode = 0;
        if (ev.getAction() == 1 && isEnabled()) {
            commitChange = true;
        } else {
            commitChange = false;
        }
        cancelSuperTouch(ev);
        if (commitChange) {
            this.mVelocityTracker.computeCurrentVelocity(1000);
            float xvel = this.mVelocityTracker.getXVelocity();
            boolean newState = Math.abs(xvel) > ((float) this.mMinFlingVelocity) ? isLayoutRtl() ? xvel < 0.0f : xvel > 0.0f : getTargetCheckedState();
            animateThumbToCheckedState(newState);
            return;
        }
        animateThumbToCheckedState(isChecked());
    }

    private void animateThumbToCheckedState(boolean newCheckedState) {
        setChecked(newCheckedState);
    }

    private boolean getTargetCheckedState() {
        if (isLayoutRtl()) {
            if (this.mThumbPosition <= ((float) (getThumbScrollRange() / 2))) {
                return true;
            }
            return false;
        } else if (this.mThumbPosition < ((float) (getThumbScrollRange() / 2))) {
            return false;
        } else {
            return true;
        }
    }

    private void setThumbPosition(boolean checked) {
        float f = 0.0f;
        if (isLayoutRtl()) {
            if (!checked) {
                f = (float) getThumbScrollRange();
            }
            this.mThumbPosition = f;
            return;
        }
        if (checked) {
            f = (float) getThumbScrollRange();
        }
        this.mThumbPosition = f;
    }

    public void setChecked(boolean checked) {
        super.setChecked(checked);
        if (this.mIsGioneeSwitchStyle && this.mIsGioneeWidget3) {
            animToPosition();
            return;
        }
        setThumbPosition(isChecked());
        invalidate();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int switchLeft;
        int switchRight;
        int switchTop;
        int switchBottom;
        super.onLayout(changed, left, top, right, bottom);
        if (!this.mIsGioneeSwitchStyle || !this.mIsGioneeWidget3) {
            setThumbPosition(isChecked());
        } else if (this.mIsFirstTime) {
            this.mIsFirstTime = false;
            setThumbPosition(isChecked());
        }
        if (isLayoutRtl()) {
            switchLeft = getPaddingLeft();
            switchRight = switchLeft + this.mSwitchWidth;
        } else {
            switchRight = getWidth() - getPaddingRight();
            switchLeft = switchRight - this.mSwitchWidth;
        }
        switch (getGravity() & 112) {
            case 16:
                switchTop = (((getPaddingTop() + getHeight()) - getPaddingBottom()) / 2) - (this.mSwitchHeight / 2);
                switchBottom = switchTop + this.mSwitchHeight;
                break;
            case 80:
                switchBottom = getHeight() - getPaddingBottom();
                switchTop = switchBottom - this.mSwitchHeight;
                break;
            default:
                switchTop = getPaddingTop();
                switchBottom = switchTop + this.mSwitchHeight;
                break;
        }
        this.mSwitchLeft = switchLeft;
        this.mSwitchTop = switchTop;
        this.mSwitchBottom = switchBottom;
        this.mSwitchRight = switchRight;
    }

    protected void onDraw(Canvas canvas) {
        int alpha;
        super.onDraw(canvas);
        int switchLeft = this.mSwitchLeft;
        int switchTop = this.mSwitchTop;
        int switchRight = this.mSwitchRight;
        int switchBottom = this.mSwitchBottom;
        if (this.mIsGioneeSwitchStyle && this.mIsGioneeWidget3) {
            int thumbCenterPos = (int) this.mThumbPosition;
            int rang = getThumbScrollRange();
            if (thumbCenterPos < getThumbScrollRange() / 4) {
                alpha = 0;
            } else if (thumbCenterPos > getThumbScrollRange() - (getThumbScrollRange() / 4)) {
                alpha = 255;
            } else {
                alpha = (thumbCenterPos * 255) / rang;
            }
            if (isLaterAPI16()) {
                this.mTrackOnDrawable.setAlpha(alpha);
                this.mTrackOnDrawable.setBounds(switchLeft, switchTop, switchRight, switchBottom);
                this.mTrackOnDrawable.draw(canvas);
                this.mTrackOffDrawable.setAlpha(255 - alpha);
                this.mTrackOffDrawable.setBounds(switchLeft, switchTop, switchRight, switchBottom);
                this.mTrackOffDrawable.draw(canvas);
            } else if (thumbCenterPos > getThumbScrollRange() - (getThumbScrollRange() / 4)) {
                this.mTrackOnDrawable.setBounds(switchLeft, switchTop, switchRight, switchBottom);
                this.mTrackOnDrawable.draw(canvas);
                this.mTrackOffDrawable.setBounds(switchLeft, switchTop, switchRight, switchBottom);
                this.mTrackOffDrawable.draw(canvas);
            }
        } else {
            this.mTrackDrawable.setBounds(switchLeft, switchTop, switchRight, switchBottom);
            this.mTrackDrawable.draw(canvas);
        }
        canvas.save();
        this.mTrackDrawable.getPadding(this.mTempRect);
        int switchInnerLeft = switchLeft + this.mTempRect.left;
        int switchInnerTop = switchTop + this.mTempRect.top;
        int switchInnerRight = switchRight - this.mTempRect.right;
        int switchInnerBottom = switchBottom - this.mTempRect.bottom;
        canvas.clipRect(switchInnerLeft, switchTop, switchInnerRight, switchBottom);
        if (this.mIsGioneeSwitchStyle && !this.mIsGioneeWidget3) {
            if (isEnabled()) {
                Drawable onSwitchDrawable = getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(getContext(), "amigo_switch_select"));
                onSwitchDrawable.setBounds(switchInnerLeft, switchTop, switchInnerRight, switchBottom);
                onSwitchDrawable.setAlpha(calculateAlpha(((int) (this.mThumbPosition + 0.5f)) - this.mTempRect.left, (switchInnerRight - switchInnerLeft) - ((this.mThumbWidth + this.mTempRect.right) + this.mTempRect.left)));
                onSwitchDrawable.draw(canvas);
            }
            this.mTextPaint.setColor(-1);
            if (isChineseLanguage()) {
                canvas.save();
                Layout onSwitchTextLayout = this.mOnLayout;
                canvas.translate((float) (switchInnerLeft + ((((switchInnerRight - switchInnerLeft) / 2) - onSwitchTextLayout.getWidth()) / 2)), (float) ((((switchInnerTop + switchInnerBottom) / 2) - (onSwitchTextLayout.getHeight() / 2)) - 3));
                onSwitchTextLayout.draw(canvas);
                canvas.restore();
                canvas.save();
                Layout offSwitchTextLayout = this.mOffLayout;
                canvas.translate((float) ((switchInnerRight - offSwitchTextLayout.getWidth()) - ((((switchInnerRight - switchInnerLeft) / 2) - offSwitchTextLayout.getWidth()) / 2)), (float) ((((switchInnerTop + switchInnerBottom) / 2) - (offSwitchTextLayout.getHeight() / 2)) - 3));
                offSwitchTextLayout.draw(canvas);
                canvas.restore();
            } else {
                int switchWidth = switchInnerRight - switchInnerLeft;
                int switchHeight = switchBottom - switchTop;
                Drawable switchOnDrawable = getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(getContext(), "amigo_switch_on"));
                switchOnDrawable.setBounds(((switchWidth / 2) - switchOnDrawable.getIntrinsicWidth()) / 2, (switchHeight - switchOnDrawable.getIntrinsicHeight()) / 2, ((switchWidth / 2) + switchOnDrawable.getIntrinsicWidth()) / 2, (switchOnDrawable.getIntrinsicHeight() + switchHeight) / 2);
                switchOnDrawable.draw(canvas);
                Drawable switchOffDrawable = getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(getContext(), "amigo_switch_off"));
                switchOffDrawable.setBounds(switchWidth - (((switchWidth / 2) + switchOffDrawable.getIntrinsicWidth()) / 2), (switchHeight - switchOffDrawable.getIntrinsicHeight()) / 2, switchWidth - (((switchWidth / 2) - switchOffDrawable.getIntrinsicWidth()) / 2), (switchOffDrawable.getIntrinsicHeight() + switchHeight) / 2);
                switchOffDrawable.draw(canvas);
            }
        }
        this.mThumbDrawable.getPadding(this.mTempRect);
        int thumbPos = (int) (this.mThumbPosition + 0.5f);
        int thumbLeft = (switchInnerLeft - this.mTempRect.left) + thumbPos;
        int thumbRight = ((switchInnerLeft + thumbPos) + this.mThumbWidth) + this.mTempRect.right;
        if (this.mIsGioneeSwitchStyle) {
            if (thumbLeft < switchLeft) {
                thumbLeft = switchLeft;
            }
            if (thumbRight > switchRight) {
                thumbRight = switchRight;
            }
        }
        this.mThumbDrawable.setBounds(thumbLeft, switchTop, thumbRight, switchBottom);
        this.mThumbDrawable.draw(canvas);
        if (this.mTextColors != null) {
            this.mTextPaint.setColor(this.mTextColors.getColorForState(getDrawableState(), this.mTextColors.getDefaultColor()));
        }
        this.mTextPaint.drawableState = getDrawableState();
        if (!this.mIsGioneeSwitchStyle) {
            Layout switchText = getTargetCheckedState() ? this.mOnLayout : this.mOffLayout;
            if (switchText != null) {
                canvas.translate((float) (((thumbLeft + thumbRight) / 2) - (switchText.getWidth() / 2)), (float) (((switchInnerTop + switchInnerBottom) / 2) - (switchText.getHeight() / 2)));
                switchText.draw(canvas);
            }
        }
        if (this.mIsGioneeSwitchStyle && this.mIsGioneeWidget3) {
            int radius = ((this.mThumbWidth - this.mDotWidth) / 2) - 12;
            int range = getThumbScrollRange();
            getPoint(thumbLeft, range, radius, this.mThumbPoint);
            int dotLeft = (this.mThumbPoint.getX() + thumbLeft) + 12;
            int dotTop = ((((this.mThumbWidth / 2) + switchTop) - this.mThumbPoint.getY()) - (this.mDotHeight / 2)) + 0;
            int dotRight = dotLeft + this.mDotWidth;
            int dotBottom = dotTop + this.mDotHeight;
            alpha = calculateAlpha(thumbLeft, range);
            this.mThumbDotOffDrawable.setAlpha(255 - alpha);
            this.mThumbDotOffDrawable.setBounds(dotLeft, dotTop, dotRight, dotBottom);
            this.mThumbDotOffDrawable.draw(canvas);
            this.mThumbDotOnDrawable.setAlpha(alpha);
            this.mThumbDotOnDrawable.setBounds(dotLeft, dotTop, dotRight, dotBottom);
            this.mThumbDotOnDrawable.draw(canvas);
        }
        canvas.restore();
    }

    private int getAndroidSDKVersion() {
        return Integer.valueOf(VERSION.SDK).intValue();
    }

    private boolean isLaterAPI16() {
        return getAndroidSDKVersion() > 16;
    }

    public int getCompoundPaddingLeft() {
        if (!isLayoutRtl()) {
            return super.getCompoundPaddingLeft();
        }
        int padding = super.getCompoundPaddingLeft() + this.mSwitchWidth;
        if (TextUtils.isEmpty(getText())) {
            return padding;
        }
        return padding + this.mSwitchPadding;
    }

    private int calculateAlpha(int curLength, int allLength) {
        int alpha = (curLength * 255) / allLength;
        if (alpha <= 0) {
            return 0;
        }
        if (alpha >= 255) {
            return 255;
        }
        return alpha;
    }

    private boolean isGioneeSwitchStyle() {
        return true;
    }

    private boolean isChineseLanguage() {
        String laungue = Locale.getDefault().getLanguage();
        if (laungue == null || !laungue.equals("zh")) {
            return false;
        }
        return true;
    }

    public int getCompoundPaddingRight() {
        if (isLayoutRtl()) {
            return super.getCompoundPaddingRight();
        }
        int padding = super.getCompoundPaddingRight() + this.mSwitchWidth;
        if (TextUtils.isEmpty(getText())) {
            return padding;
        }
        return padding + this.mSwitchPadding;
    }

    private int getThumbScrollRange() {
        if (this.mTrackDrawable == null) {
            return 0;
        }
        this.mTrackDrawable.getPadding(this.mTempRect);
        return ((this.mSwitchWidth - this.mThumbWidth) - this.mTempRect.left) - this.mTempRect.right;
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        int[] myDrawableState = getDrawableState();
        if (this.mThumbDrawable != null) {
            this.mThumbDrawable.setState(myDrawableState);
        }
        if (this.mTrackDrawable != null) {
            this.mTrackDrawable.setState(myDrawableState);
        }
        if (this.mIsGioneeSwitchStyle && this.mIsGioneeWidget3) {
            if (this.mThumbDrawable != null) {
                this.mThumbDrawable.setState(myDrawableState);
            }
            if (this.mTrackOffDrawable != null) {
                this.mTrackOffDrawable.setState(myDrawableState);
            }
            if (this.mTrackOnDrawable != null) {
                this.mTrackOnDrawable.setState(myDrawableState);
            }
            if (this.mThumbDotOffDrawable != null) {
                this.mThumbDotOffDrawable.setState(myDrawableState);
            }
            if (this.mThumbDotOnDrawable != null) {
                this.mThumbDotOnDrawable.setState(myDrawableState);
            }
        }
        invalidate();
    }

    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mThumbDrawable || who == this.mTrackDrawable;
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        this.mThumbDrawable.jumpToCurrentState();
        this.mTrackDrawable.jumpToCurrentState();
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AmigoSwitch.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AmigoSwitch.class.getName());
        CharSequence switchText = isChecked() ? this.mTextOn : this.mTextOff;
        if (!TextUtils.isEmpty(switchText)) {
            CharSequence oldText = info.getText();
            if (TextUtils.isEmpty(oldText)) {
                info.setText(switchText);
                return;
            }
            StringBuilder newText = new StringBuilder();
            newText.append(oldText).append(' ').append(switchText);
            info.setText(newText);
        }
    }

    private boolean isGioneeWidget3Support() {
        return true;
    }

    private void getPoint(int pos, int len, int radius, Point point) {
        double degree = 3.141592653589793d - ((((double) pos) / ((double) len)) * 3.141592653589793d);
        point.setX((int) (((double) radius) + (((double) radius) * Math.cos(degree))));
        point.setY((int) (((double) radius) * Math.sin(degree)));
    }

    private void animToPosition() {
        if (!this.mIsAnimation) {
            postAnimation();
        }
    }

    private void postAnimation() {
        postDelayed(new Runnable() {
            public void run() {
                if (AmigoSwitch.this.isChecked()) {
                    AmigoSwitch.access$016(AmigoSwitch.this, 5.0f);
                    if (AmigoSwitch.this.mThumbPosition >= ((float) AmigoSwitch.this.getThumbScrollRange())) {
                        AmigoSwitch.this.setThumbPosition(AmigoSwitch.this.isChecked());
                        AmigoSwitch.this.invalidate();
                        AmigoSwitch.this.mIsAnimation = false;
                        return;
                    }
                }
                AmigoSwitch.access$024(AmigoSwitch.this, 5.0f);
                if (AmigoSwitch.this.mThumbPosition <= ((float) AmigoSwitch.this.mSwitchLeft)) {
                    AmigoSwitch.this.setThumbPosition(AmigoSwitch.this.isChecked());
                    AmigoSwitch.this.invalidate();
                    AmigoSwitch.this.mIsAnimation = false;
                    return;
                }
                AmigoSwitch.this.mIsAnimation = true;
                AmigoSwitch.this.invalidate();
                AmigoSwitch.this.postAnimation();
            }
        }, 15);
    }
}
