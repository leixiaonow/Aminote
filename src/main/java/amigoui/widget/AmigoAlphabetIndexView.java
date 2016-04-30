package amigoui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;

import com.gionee.aminote.R;

import amigoui.changecolors.ChameleonColorManager;

public final class AmigoAlphabetIndexView extends AbsListIndexer {
    private static final int INVALID_INDEX = -1;
    private static final String LETTER_DISPLAY_AREA_REPRESENTER = "M";
    private final int ALPHABET_LEN;
    private int MAX_TOUCHBLE_WIDTH;
    private String[] mAlphabet;
    private int mCurrentLetterColor;
    private int mCurrentShowingBgcolor;
    private int mDisableLetterColor;
    private int mDrawCircleMaxTop;
    private int mDrawCircleMinTop;
    private int mEnableLetterColor;
    private boolean mIsTouching;
    LetterHolder[] mLetterHolders;
    private int mLetterTextSize;
    private ListView mList;
    private int mListOffset;
    private Paint mPaint;
    private int mPreTouchingLetterIndex;
    private SectionIndexer mSectionIndexer;
    private String[] mSectionStrings;
    private int mShowingLetterColor;
    private int mShowingLetterIndex;
    private int mTouchingAlphbetIndex;
    private int mTouchingLeftOffset;
    private int mTouchingLetterHeight;
    private int mTouchingLetterTextSize;
    private int mTouchingLetterWidth;

    class LetterHolder {
        Rect mDrawRect;
        boolean mIsEnable;
        String mLetter;
        Rect mOrigRect;

        public LetterHolder(AmigoAlphabetIndexView amigoAlphabetIndexView, int left, int top, int right, int bottom, String letter) {
            this(new Rect(left, top, right, bottom), letter);
        }

        public LetterHolder(Rect origRect, String letter) {
            this.mIsEnable = false;
            this.mDrawRect = new Rect(origRect);
            Rect rect = this.mDrawRect;
            rect.left -= AmigoAlphabetIndexView.this.mTouchingLeftOffset;
            rect = this.mDrawRect;
            rect.right -= AmigoAlphabetIndexView.this.mTouchingLeftOffset;
            this.mOrigRect = new Rect(origRect);
            this.mLetter = letter;
        }

        public int getTextTop() {
            return (this.mDrawRect.top / 2) + (this.mDrawRect.bottom / 2);
        }

        public int getNavigationTextLeft() {
            return this.mOrigRect.left + (this.mOrigRect.width() / 2);
        }

        public int getNavigationCircleLeft() {
            return this.mDrawRect.left + (this.mDrawRect.width() / 2);
        }

        public void setEnable(boolean isEnable) {
            this.mIsEnable = isEnable;
        }

        public boolean isEnable() {
            return this.mIsEnable;
        }
    }

    public AmigoAlphabetIndexView(Context context) {
        this(context, null, 0);
    }

    public AmigoAlphabetIndexView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AmigoAlphabetIndexView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.MAX_TOUCHBLE_WIDTH = 50;
        this.mShowingLetterIndex = -1;
        this.mTouchingAlphbetIndex = -1;
        this.mPreTouchingLetterIndex = -1;
        this.mIsTouching = false;
        this.mSectionStrings = new String[]{null};
//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoAlphabetIndexView, R.attr.AmigoAlphabetIndexViewStyle, 0);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoAlphabetIndexView, R.attr.AmigoAlphabetIndexViewStyle, R.style.AmigoAlphabetIndexViewStyle);
        Resources res = getResources();
        this.MAX_TOUCHBLE_WIDTH = res.getDimensionPixelOffset(R.dimen.alphabetindex_max_touchble_width);
        this.mLetterTextSize = a.getDimensionPixelSize(R.styleable.AmigoAlphabetIndexView_amigoSectionFontSize, toRawTextSize(14.0f));
        this.mTouchingLetterTextSize = a.getDimensionPixelSize(R.styleable.AmigoAlphabetIndexView_amigoTouchingLetterFontSize, toRawTextSize(20.0f));
        this.mTouchingLeftOffset = a.getDimensionPixelOffset(R.styleable.AmigoAlphabetIndexView_amigoTouchingLeftOffset, 30);
        this.mEnableLetterColor = a.getColor(R.styleable.AmigoAlphabetIndexView_amigoEnableSectionColor, res.getColor(R.color.amigo_content_color_secondary_on_backgroud_c2));
        this.mDisableLetterColor = a.getColor(R.styleable.AmigoAlphabetIndexView_amigoDisableSectionColor, res.getColor(R.color.amigo_content_color_thirdly_on_backgroud_c3));
        this.mShowingLetterColor = a.getColor(R.styleable.AmigoAlphabetIndexView_amigoShowingLetterColor, res.getColor(R.color.amigo_accent_color_g1));
        this.mCurrentLetterColor = -1;
        this.mCurrentShowingBgcolor = this.mShowingLetterColor;
        a.recycle();
        this.mAlphabet = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", LETTER_DISPLAY_AREA_REPRESENTER, "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
        this.ALPHABET_LEN = this.mAlphabet.length;
        init(context);
        ChangeViewColorWithChameleon();
    }

    protected void init(Context context) {
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setTextAlign(Align.CENTER);
        Rect bounds = new Rect();
        this.mPaint.setTextSize((float) this.mTouchingLetterTextSize);
        this.mPaint.getTextBounds(LETTER_DISPLAY_AREA_REPRESENTER, 0, 1, bounds);
        this.mTouchingLetterWidth = bounds.width();
        this.mTouchingLetterHeight = bounds.height();
    }

    public void layout(int l, int t, int r, int b) {
        super.layout(l, t, r, b);
        if (getHeight() > 0) {
            initLetterHolders(getHeight());
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mLetterHolders != null) {
            int i = 0;
            while (i < this.mLetterHolders.length) {
                int paintColor;
                float f;
                LetterHolder hodler = this.mLetterHolders[i];
                int textTop = hodler.getTextTop();
                int textLeft = hodler.getNavigationTextLeft();
                int circleLeft = hodler.getNavigationCircleLeft();
                if (this.mShowingLetterIndex == i) {
                    paintColor = this.mShowingLetterColor;
                } else if (hodler.isEnable()) {
                    paintColor = this.mEnableLetterColor;
                } else {
                    paintColor = this.mDisableLetterColor;
                }
                this.mPaint.setTextSize((float) this.mLetterTextSize);
                boolean isNavigation = this.mTouchingAlphbetIndex == i && this.mIsTouching;
                if (isNavigation) {
                    this.mPaint.setTextSize((float) this.mTouchingLetterTextSize);
                    this.mPaint.setColor(this.mCurrentShowingBgcolor);
                    int minDimension = Math.min(this.mTouchingLetterHeight, this.mTouchingLetterWidth);
                    if (textTop < this.mDrawCircleMinTop) {
                        textTop = this.mDrawCircleMinTop;
                    } else if (textTop > this.mDrawCircleMaxTop) {
                        textTop = this.mDrawCircleMaxTop;
                    }
                    canvas.drawCircle((float) circleLeft, (float) (textTop - (minDimension / 2)), (float) minDimension, this.mPaint);
                    paintColor = this.mCurrentLetterColor;
                }
                this.mPaint.setColor(paintColor);
                String str = hodler.mLetter;
                if (isNavigation) {
                    f = (float) circleLeft;
                } else {
                    f = (float) textLeft;
                }
                canvas.drawText(str, f, (float) textTop, this.mPaint);
                i++;
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        this.mTouchingAlphbetIndex = getTouchingIndex((int) event.getY());
        if (this.mIsTouching || getWidth() - x <= this.MAX_TOUCHBLE_WIDTH) {
            switch (action) {
                case 0:
                    cancelFling();
                    this.mIsTouching = true;
                    break;
                case 2:
                    this.mIsTouching = true;
                    break;
                default:
                    this.mIsTouching = false;
                    countShowingLetterIndex();
                    break;
            }
        } else if (this.mIsTouching) {
            this.mIsTouching = false;
        }
        if (this.mIsTouching && !(this.mPreTouchingLetterIndex == this.mTouchingAlphbetIndex && this.mShowingLetterIndex == this.mTouchingAlphbetIndex)) {
            this.mPreTouchingLetterIndex = this.mTouchingAlphbetIndex;
            moveListToSection(toSectionIndex(this.mTouchingAlphbetIndex));
        }
        invalidate();
        return this.mIsTouching;
    }

    private boolean countShowingLetterIndex() {
        if (this.mSectionIndexer == null || this.mList == null) {
            return false;
        }
        int position = this.mList.getFirstVisiblePosition() - this.mListOffset;
        if (position < 0) {
            position = 0;
        }
        int shallShowing = toAlphbetIndex(this.mSectionIndexer.getSectionForPosition(position));
        if (this.mShowingLetterIndex == shallShowing) {
            return false;
        }
        this.mShowingLetterIndex = shallShowing;
        return true;
    }

    public void invalidateShowingLetterIndex() {
        if (countShowingLetterIndex()) {
            invalidate();
        }
    }

    protected int getTouchingIndex(int touchingY) {
        if (touchingY < this.mLetterHolders[0].mOrigRect.top) {
            return 0;
        }
        if (touchingY > this.mLetterHolders[this.ALPHABET_LEN - 1].mOrigRect.bottom) {
            return this.ALPHABET_LEN - 1;
        }
        int start = 0;
        int end = this.ALPHABET_LEN - 1;
        int mid = (0 + end) >> 1;
        Rect rect = new Rect(this.mLetterHolders[mid].mOrigRect);
        while (!rect.contains(rect.left, touchingY) && end > start) {
            if (touchingY < rect.top) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
            mid = (int) ((((long) start) + ((long) end)) / 2);
            rect = this.mLetterHolders[mid].mOrigRect;
        }
        return mid;
    }

    private void cancelFling() {
        if (this.mList != null) {
            MotionEvent cancelFling = MotionEvent.obtain(0, 0, 3, 0.0f, 0.0f, 0);
            this.mList.onTouchEvent(cancelFling);
            cancelFling.recycle();
        }
    }

    public void setList(ListView listView) {
        setList(listView, null);
    }

    public void setList(ListView listView, OnScrollListener scrollListener) {
        if (listView != null) {
            this.mList = listView;
            this.mList.setFastScrollEnabled(false);
            this.mList.setVerticalScrollBarEnabled(false);
            if (scrollListener != null) {
                this.mList.setOnScrollListener(scrollListener);
            }
            initSections(this.mList);
            countShowingLetterIndex();
            invalidate();
            return;
        }
        throw new IllegalArgumentException("Can not set a null list!");
    }

    public void updateIndexer(SectionIndexer indexer) {
        this.mSectionIndexer = indexer;
        initSections();
        countShowingLetterIndex();
        invalidate();
    }

    private void initSections() {
        if (this.mSectionIndexer != null) {
            Object[] sections = this.mSectionIndexer.getSections();
            if (sections == null || sections.length <= 0) {
                this.mSectionStrings = new String[]{null};
            } else {
                this.mSectionStrings = new String[sections.length];
                for (int i = 0; i < this.mSectionStrings.length; i++) {
                    this.mSectionStrings[i] = sections[i].toString();
                    boolean setNull = true;
                    for (String equalsIgnoreCase : this.mAlphabet) {
                        if (equalsIgnoreCase.equalsIgnoreCase(this.mSectionStrings[i])) {
                            setNull = false;
                            break;
                        }
                    }
                    if (setNull) {
                        this.mSectionStrings[i] = null;
                    }
                }
            }
            pickDisableSection();
        }
    }

    private void initSections(ListView listView) {
        Adapter adapter = listView.getAdapter();
        this.mSectionIndexer = null;
        if (adapter instanceof HeaderViewListAdapter) {
            this.mListOffset = ((HeaderViewListAdapter) adapter).getHeadersCount();
            adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }
        if (adapter instanceof SectionIndexer) {
            this.mSectionIndexer = (SectionIndexer) adapter;
            Object[] sections = this.mSectionIndexer.getSections();
            if (sections == null || sections.length <= 0) {
                this.mSectionStrings = new String[]{null};
            } else {
                this.mSectionStrings = new String[sections.length];
                for (int i = 0; i < this.mSectionStrings.length; i++) {
                    this.mSectionStrings[i] = sections[i].toString();
                    boolean setNull = true;
                    for (String equalsIgnoreCase : this.mAlphabet) {
                        if (equalsIgnoreCase.equalsIgnoreCase(this.mSectionStrings[i])) {
                            setNull = false;
                            break;
                        }
                    }
                    if (setNull) {
                        this.mSectionStrings[i] = null;
                    }
                }
            }
        }
        pickDisableSection();
    }

    private int toSectionIndex(int alphbetIndex) {
        Log.d("maxw", "toSectionIndex, alphbetIndex=" + alphbetIndex);
        if (alphbetIndex >= 0 && alphbetIndex < this.mAlphabet.length) {
            String letter = this.mAlphabet[alphbetIndex];
            Log.d("maxw", "mSectionStrings.length=" + this.mSectionStrings.length);
            for (int i = 0; i < this.mSectionStrings.length; i++) {
                if (this.mSectionStrings[i] != null) {
                    if (letter.equalsIgnoreCase("#")) {
                        return this.mSectionStrings.length - 1;
                    }
                    if (letter.compareToIgnoreCase(this.mSectionStrings[i]) <= 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private int toAlphbetIndex(int sectionIndex) {
        if (sectionIndex >= 0 && sectionIndex < this.mSectionStrings.length) {
            String section = this.mSectionStrings[sectionIndex];
            for (int i = 0; i < this.mAlphabet.length; i++) {
                if (this.mAlphabet[i].equalsIgnoreCase(section)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void moveListToSection(int sectionIndex) {
        if (this.mList != null && this.mSectionIndexer != null && -1 != sectionIndex) {
            this.mList.setSelectionFromTop(this.mListOffset + this.mSectionIndexer.getPositionForSection(sectionIndex), 0);
        }
    }

    private void ChangeViewColorWithChameleon() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            this.mEnableLetterColor = ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2();
            this.mDisableLetterColor = ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3();
            this.mShowingLetterColor = ChameleonColorManager.getAccentColor_G1();
            this.mCurrentShowingBgcolor = ChameleonColorManager.getAccentColor_G1();
        }
    }

    private void initLetterHolders(int viewHeight) {
        int alphbetLen = this.mAlphabet.length;
        if (alphbetLen > 0) {
            int sectionTopOffset = getPaddingTop();
            int realHeight = (viewHeight - sectionTopOffset) - getPaddingBottom();
            int sectionHeight = realHeight / alphbetLen;
            sectionTopOffset += (realHeight % alphbetLen) / 2;
            int leftOffset = (getWidth() - sectionHeight) - getPaddingRight();
            this.mLetterHolders = new LetterHolder[alphbetLen];
            int right = leftOffset + sectionHeight;
            int top = sectionTopOffset;
            int bottom = top + sectionHeight;
            for (int i = 0; i < alphbetLen; i++) {
                this.mLetterHolders[i] = new LetterHolder(this, leftOffset, top, right, bottom, this.mAlphabet[i]);
                top += sectionHeight;
                bottom += sectionHeight;
            }
            pickDisableSection();
            if (alphbetLen > 2) {
                this.mDrawCircleMinTop = this.mLetterHolders[2].getTextTop();
                this.mDrawCircleMaxTop = this.mLetterHolders[alphbetLen - 2].getTextTop();
            }
        }
    }

    private boolean pickDisableSection() {
        if (this.mLetterHolders == null) {
            return false;
        }
        for (int i = 0; i < this.mLetterHolders.length; i++) {
            this.mLetterHolders[i].setEnable(false);
            if (this.mSectionStrings == null || this.mSectionStrings.length <= 0) {
                this.mLetterHolders[i].setEnable(false);
            } else {
                for (String equalsIgnoreCase : this.mSectionStrings) {
                    if (this.mLetterHolders[i].mLetter.equalsIgnoreCase(equalsIgnoreCase)) {
                        this.mLetterHolders[i].setEnable(true);
                        break;
                    }
                }
            }
        }
        return true;
    }

    public boolean isBusying() {
        return this.mIsTouching;
    }

    public String[] getAlphabet() {
        return this.mAlphabet;
    }

    public void setDisableLetterColor(int color) {
        this.mDisableLetterColor = color;
    }

    public void setEnableLetterColor(int color) {
        this.mEnableLetterColor = color;
    }

    public void setShowingLetterColor(int color) {
        this.mShowingLetterColor = color;
        this.mCurrentShowingBgcolor = this.mShowingLetterColor;
    }
}
