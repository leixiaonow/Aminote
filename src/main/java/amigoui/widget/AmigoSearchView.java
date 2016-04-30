package amigoui.widget;

import com.gionee.aminote.R;
import amigoui.changecolors.ChameleonColorManager;
import amigoui.changecolors.ColorConfigConstants;
import android.app.PendingIntent;
import android.app.SearchableInfo;
import android.app.SearchableInfo.ActionKeyInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.CollapsibleActionView;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.WeakHashMap;

public class AmigoSearchView extends LinearLayout implements CollapsibleActionView {
    private static final boolean DBG = false;
    private static final String IME_OPTION_NO_MICROPHONE = "nm";
    private static final String LOG_TAG = "SearchView";
    private static ColorStateList mHintColor;
    private static ColorStateList mTextColor;
    private final int GN_SEARCHVIEW_ANIMTIME;
    private Bundle mAppSearchData;
    private boolean mClearingFocus;
    private ImageView mCloseButton;
    private int mCollapsedImeOptions;
    private View mDropDownAnchor;
    private boolean mExpandedInActionView;
    private LinearLayout mGnSearchBgLayout;
    private ImageView mGnSearchGoButton;
    private ImageView mGnSearchVoiceButton;
    private boolean mIconified;
    private boolean mIconifiedByDefault;
    private boolean mIsGnWidget3Style;
    private boolean mIsSearchSubmitMode;
    private boolean mIsSearchVoiceMode;
    private int mMaxWidth;
    private CharSequence mOldQueryText;
    private final OnClickListener mOnClickListener;
    private OnCloseListener mOnCloseListener;
    private final OnItemClickListener mOnItemClickListener;
    private final OnItemSelectedListener mOnItemSelectedListener;
    private OnQueryTextListener mOnQueryChangeListener;
    private OnFocusChangeListener mOnQueryTextFocusChangeListener;
    private OnClickListener mOnSearchClickListener;
    private OnSuggestionListener mOnSuggestionListener;
    private final WeakHashMap<String, ConstantState> mOutsideDrawablesCache;
    private CharSequence mQueryHint;
    private boolean mQueryRefinement;
    private SearchAutoComplete mQueryTextView;
    private int mQueryTextViewBackgroundColor;
    private Runnable mReleaseCursorRunnable;
    private AnimationListener mSearchAnimationListener;
    private View mSearchButton;
    private View mSearchEditFrame;
    private ImageView mSearchHintIcon;
    private View mSearchPlate;
    private Animation mSearchViewAnim;
    private LinearLayout mSearchViewLayout;
    private UnfoldAnimation mSearchViewUnfoldAnim;
    private Animation mSearchViewZoomAnim;
    private SearchableInfo mSearchable;
    private Runnable mShowImeRunnable;
    private boolean mSubmitButtonEnabled;
    private CursorAdapter mSuggestionsAdapter;
    OnKeyListener mTextKeyListener;
    private TextWatcher mTextWatcher;
    private Runnable mUpdateDrawableStateRunnable;
    private CharSequence mUserQuery;
    private final Intent mVoiceAppSearchIntent;
    private boolean mVoiceButtonEnabled;
    private Drawable mVoiceIcon;
    private final Intent mVoiceWebSearchIntent;

    public interface OnCloseListener {
        boolean onClose();
    }

    public interface OnQueryTextListener {
        boolean onQueryTextChange(String str);

        boolean onQueryTextSubmit(String str);
    }

    public interface OnSuggestionListener {
        boolean onSuggestionClick(int i);

        boolean onSuggestionSelect(int i);
    }

    private class UnfoldAnimation extends Animation {
        private int mDeltaWidth;
        private int mStartWidth;

        public UnfoldAnimation(int startWidth, int endWidth) {
            this.mStartWidth = startWidth;
            this.mDeltaWidth = endWidth - startWidth;
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            LayoutParams lp = (LayoutParams) AmigoSearchView.this.mGnSearchBgLayout.getLayoutParams();
            if (((double) interpolatedTime) >= 1.0d) {
                lp.width = -1;
            } else {
                lp.width = (int) (((float) this.mStartWidth) + (((float) this.mDeltaWidth) * interpolatedTime));
            }
            AmigoSearchView.this.mGnSearchBgLayout.setLayoutParams(lp);
        }

        public boolean willChangeBounds() {
            return true;
        }
    }

    public static class SearchAutoComplete extends AmigoAutoCompleteTextView {
        private AmigoSearchView mSearchView;
        private int mThreshold;

        public SearchAutoComplete(Context context) {
            this(context, null);
        }

        public SearchAutoComplete(Context context, AttributeSet attrs) {
            this(context, attrs, 16842859);
        }

        public SearchAutoComplete(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            this.mThreshold = getThreshold();
            if (AmigoSearchView.mTextColor != null) {
                setTextColor(AmigoSearchView.mTextColor);
            }
            if (AmigoSearchView.mHintColor != null) {
                setHintTextColor(AmigoSearchView.mHintColor);
            }
            changeColors();
        }

        private void changeColors() {
            if (ChameleonColorManager.isNeedChangeColor()) {
                setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
                setHintTextColor(ChameleonColorManager.getContentColorThirdlyOnAppbar_T3());
                setBackgroundColor(0);
            }
        }

        void setSearchView(AmigoSearchView searchView) {
            this.mSearchView = searchView;
        }

        public void setThreshold(int threshold) {
            super.setThreshold(threshold);
            this.mThreshold = threshold;
        }

        private boolean isEmpty() {
            return TextUtils.getTrimmedLength(getText()) == 0;
        }

        protected void replaceText(CharSequence text) {
        }

        public void performCompletion() {
        }

        public void onWindowFocusChanged(boolean hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);
            if (hasWindowFocus && this.mSearchView.hasFocus() && getVisibility() == VISIBLE) {
                ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(this, 0);
                if (AmigoSearchView.isLandscapeMode(getContext())) {
                    ensureImeVisible(true);
                }
            }
        }

        protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
            this.mSearchView.onTextFocusChanged();
        }

        public boolean enoughToFilter() {
            return this.mThreshold <= 0 || super.enoughToFilter();
        }

        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            if (keyCode == 4) {
                DispatcherState state;
                if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                    state = getKeyDispatcherState();
                    if (state == null) {
                        return true;
                    }
                    state.startTracking(event, this);
                    return true;
                } else if (event.getAction() == 1) {
                    state = getKeyDispatcherState();
                    if (state != null) {
                        state.handleUpEvent(event);
                    }
                    if (event.isTracking() && !event.isCanceled()) {
                        this.mSearchView.clearFocus();
                        this.mSearchView.setImeVisibility(false);
                        return true;
                    }
                }
            }
            return super.onKeyPreIme(keyCode, event);
        }
    }

    public AmigoSearchView(Context context) {
        this(context, null);
    }

    public AmigoSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mShowImeRunnable = new Runnable() {
            public void run() {
                InputMethodManager imm = (InputMethodManager) AmigoSearchView.this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInputUnchecked(0, null);
                }
            }
        };
        this.mUpdateDrawableStateRunnable = new Runnable() {
            public void run() {
            }
        };
        this.mReleaseCursorRunnable = new Runnable() {
            public void run() {
                if (AmigoSearchView.this.mSuggestionsAdapter != null && (AmigoSearchView.this.mSuggestionsAdapter instanceof AmigoSuggestionsAdapter)) {
                    ((AmigoSuggestionsAdapter) AmigoSearchView.this.mSuggestionsAdapter).close();
                }
            }
        };
        this.mOutsideDrawablesCache = new WeakHashMap();
        this.mOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                if (v == AmigoSearchView.this.mSearchButton) {
                    AmigoSearchView.this.onSearchClicked();
                } else if (v == AmigoSearchView.this.mCloseButton) {
                    AmigoSearchView.this.onCloseClicked();
                } else if (v == AmigoSearchView.this.mQueryTextView) {
                    AmigoSearchView.this.forceSuggestionQuery();
                }
            }
        };
        this.mTextKeyListener = new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (AmigoSearchView.this.mQueryTextView.isPopupShowing() && AmigoSearchView.this.mQueryTextView.getListSelection() != -1) {
                    return AmigoSearchView.this.onSuggestionsKey(v, keyCode, event);
                }
                if (!AmigoSearchView.this.mQueryTextView.isEmpty() && event.hasNoModifiers()) {
                    if (event.getAction() == 1 && keyCode == 66) {
                        v.cancelLongPress();
                        AmigoSearchView.this.onSubmitQuery();
                        return true;
                    } else if (AmigoSearchView.this.mSearchable != null && event.getAction() == 0) {
                        ActionKeyInfo actionKey = AmigoSearchView.this.mSearchable.findActionKey(keyCode);
                        if (!(actionKey == null || actionKey.getQueryActionMsg() == null)) {
                            AmigoSearchView.this.launchQuerySearch(keyCode, actionKey.getQueryActionMsg(), AmigoSearchView.this.mQueryTextView.getText().toString());
                            return true;
                        }
                    }
                }
                return false;
            }
        };
        this.mOnItemClickListener = new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                AmigoSearchView.this.onItemClicked(position, 0, null);
            }
        };
        this.mOnItemSelectedListener = new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                AmigoSearchView.this.onItemSelected(position);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };
        this.mTextWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int before, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int after) {
                AmigoSearchView.this.onTextChanged(s);
            }

            public void afterTextChanged(Editable s) {
            }
        };
        this.GN_SEARCHVIEW_ANIMTIME = 300;
        this.mIsGnWidget3Style = false;
        this.mIsSearchVoiceMode = false;
        this.mIsSearchSubmitMode = false;
        this.mSearchAnimationListener = new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (animation == AmigoSearchView.this.mSearchViewZoomAnim) {
                    AmigoSearchView.this.mSearchPlate.setVisibility(GONE);
                    AmigoSearchView.this.mGnSearchBgLayout.setVisibility(VISIBLE);
                    AmigoSearchView.this.setupSearchViewUnfoldAnim();
                    AmigoSearchView.this.mGnSearchBgLayout.startAnimation(AmigoSearchView.this.mSearchViewUnfoldAnim);
                } else if (animation == AmigoSearchView.this.mSearchViewUnfoldAnim) {
                    AmigoSearchView.this.mSearchPlate.setVisibility(VISIBLE);
                    AmigoSearchView.this.mGnSearchBgLayout.setVisibility(VISIBLE);
                    if (AmigoSearchView.this.mIsSearchSubmitMode && AmigoSearchView.this.mGnSearchGoButton != null) {
                        AmigoSearchView.this.mGnSearchGoButton.setVisibility(VISIBLE);
                    }
                    AmigoSearchView.this.mSearchPlate.startAnimation(AmigoSearchView.this.mSearchViewAnim);
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }
        };
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AmigoSearchView, R.attr.amigoSearchViewStyle, 0);
        mTextColor = typedArray.getColorStateList(R.styleable.AmigoSearchView_amigoTextColor);
        mHintColor = typedArray.getColorStateList(R.styleable.AmigoSearchView_amigoHintColor);
        this.mQueryTextViewBackgroundColor = typedArray.getColor(R.styleable.AmigoSearchView_amigoBackground, 0);
        this.mVoiceIcon = typedArray.getDrawable(R.styleable.AmigoSearchView_amigoVoiceIcon);
        int maxWidth = typedArray.getDimensionPixelSize(R.styleable.AmigoSearchView_amigoMaxWidth, -1);
        CharSequence queryHint = typedArray.getText(R.styleable.AmigoSearchView_amigoQueryHint);
        int imeOptions = typedArray.getInt(R.styleable.AmigoSearchView_amigoImeOptions, -1);
        int inputType = typedArray.getInt(R.styleable.AmigoSearchView_amigoInputType, -1);
        typedArray.recycle();
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(AmigoWidgetResource.getIdentifierByLayout(context, "amigo_search_view"), this, true);
        this.mSearchButton = findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_search_button"));
        this.mQueryTextView = (SearchAutoComplete) findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_search_src_text"));
        this.mQueryTextView.setSearchView(this);
        this.mSearchEditFrame = findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_search_edit_frame"));
        this.mSearchPlate = findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_search_plate"));
        this.mCloseButton = (ImageView) findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_search_close_btn"));
        this.mSearchHintIcon = (ImageView) findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_search_mag_icon"));
        this.mSearchButton.setOnClickListener(this.mOnClickListener);
        this.mCloseButton.setOnClickListener(this.mOnClickListener);
        this.mQueryTextView.setOnClickListener(this.mOnClickListener);
        this.mQueryTextView.addTextChangedListener(this.mTextWatcher);
        this.mQueryTextView.setOnItemClickListener(this.mOnItemClickListener);
        this.mQueryTextView.setOnItemSelectedListener(this.mOnItemSelectedListener);
        this.mQueryTextView.setOnKeyListener(this.mTextKeyListener);
        this.mQueryTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (AmigoSearchView.this.mOnQueryTextFocusChangeListener != null) {
                    AmigoSearchView.this.mOnQueryTextFocusChangeListener.onFocusChange(AmigoSearchView.this, hasFocus);
                }
            }
        });
        this.mQueryTextView.setDropDownWidth(-2);
        if (maxWidth != -1) {
            setMaxWidth(maxWidth);
        }
        if (!TextUtils.isEmpty(queryHint)) {
            setQueryHint(queryHint);
        }
        if (imeOptions != -1) {
            setImeOptions(imeOptions);
        }
        if (inputType != -1) {
            setInputType(inputType);
        }
        //change
//        TypedArray a = context.obtainStyledAttributes(attrs, android.R.styleable.View, 0, 0);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.View, 0, 0);
//        boolean focusable = a.getBoolean(19, true);
        boolean focusable = a.getBoolean(R.styleable.View[1],true);
        a.recycle();
        setFocusable(focusable);
        this.mVoiceWebSearchIntent = new Intent("android.speech.action.WEB_SEARCH");
        this.mVoiceWebSearchIntent.addFlags(ColorConfigConstants.DEFAULT_EDIT_TEXT_BACKGROUND_COLOR_B3);
        this.mVoiceWebSearchIntent.putExtra("android.speech.extra.LANGUAGE_MODEL", "web_search");
        this.mVoiceAppSearchIntent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
        this.mVoiceAppSearchIntent.addFlags(ColorConfigConstants.DEFAULT_EDIT_TEXT_BACKGROUND_COLOR_B3);
        this.mDropDownAnchor = findViewById(this.mQueryTextView.getDropDownAnchor());
        if (this.mDropDownAnchor != null) {
            this.mDropDownAnchor.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    AmigoSearchView.this.adjustDropDownSizeAndPosition();
                }
            });
        }
        updateViewsVisibility(this.mIconifiedByDefault);
        updateQueryHint();
        boolean z = isGioneeViewStyle() && isGioneeWidget3Support();
        this.mIsGnWidget3Style = z;
        if (this.mIsGnWidget3Style) {
            initSearchView();
        }
        changeColors();
    }

    private void changeColors() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            this.mCloseButton.getDrawable().setColorFilter(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1(), Mode.SRC_IN);
            this.mSearchHintIcon.getDrawable().setColorFilter(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1(), Mode.SRC_IN);
            changeSelectableItemBackground(this.mCloseButton);
            this.mGnSearchBgLayout.setBackgroundColor(ChameleonColorManager.getEditTextBackgroudColor_B3());
        }
    }

    private void changeSelectableItemBackground(View v) {
        Drawable background = v.getBackground();
    }

    public void setSearchableInfo(SearchableInfo searchable) {
        this.mSearchable = searchable;
        if (this.mSearchable != null) {
            updateSearchAutoComplete();
            updateQueryHint();
        }
        this.mVoiceButtonEnabled = hasVoiceSearch();
        if (this.mVoiceButtonEnabled) {
            this.mQueryTextView.setPrivateImeOptions(IME_OPTION_NO_MICROPHONE);
        }
        updateViewsVisibility(isIconified());
    }

    public void setAppSearchData(Bundle appSearchData) {
        this.mAppSearchData = appSearchData;
    }

    public void setImeOptions(int imeOptions) {
        this.mQueryTextView.setImeOptions(imeOptions);
    }

    public int getImeOptions() {
        return this.mQueryTextView.getImeOptions();
    }

    public void setInputType(int inputType) {
        this.mQueryTextView.setInputType(inputType);
    }

    public int getInputType() {
        return this.mQueryTextView.getInputType();
    }

    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if (this.mClearingFocus) {
            return false;
        }
        if (!isFocusable()) {
            return false;
        }
        if (isIconified()) {
            return super.requestFocus(direction, previouslyFocusedRect);
        }
        boolean result = this.mQueryTextView.requestFocus(direction, previouslyFocusedRect);
        if (!result) {
            return result;
        }
        updateViewsVisibility(false);
        return result;
    }

    public void clearFocus() {
        this.mClearingFocus = true;
        setImeVisibility(false);
        super.clearFocus();
        this.mQueryTextView.clearFocus();
        this.mClearingFocus = false;
    }

    public void setOnQueryTextListener(OnQueryTextListener listener) {
        this.mOnQueryChangeListener = listener;
    }

    public void setOnCloseListener(OnCloseListener listener) {
        this.mOnCloseListener = listener;
    }

    public void setOnQueryTextFocusChangeListener(OnFocusChangeListener listener) {
        this.mOnQueryTextFocusChangeListener = listener;
    }

    public void setOnSuggestionListener(OnSuggestionListener listener) {
        this.mOnSuggestionListener = listener;
    }

    public void setOnSearchClickListener(OnClickListener listener) {
        this.mOnSearchClickListener = listener;
    }

    public CharSequence getQuery() {
        return this.mQueryTextView.getText();
    }

    public void setQuery(CharSequence query, boolean submit) {
        this.mQueryTextView.setText(query);
        if (query != null) {
            this.mQueryTextView.setSelection(this.mQueryTextView.length());
            this.mUserQuery = query;
        }
        if (submit && !TextUtils.isEmpty(query)) {
            onSubmitQuery();
        }
    }

    public void setQueryHint(CharSequence hint) {
        this.mQueryHint = hint;
        updateQueryHint();
    }

    public CharSequence getQueryHint() {
        if (this.mQueryHint != null) {
            return this.mQueryHint;
        }
        if (this.mSearchable == null) {
            return null;
        }
        int hintId = this.mSearchable.getHintId();
        if (hintId != 0) {
            return getContext().getString(hintId);
        }
        return null;
    }

    public void setIconifiedByDefault(boolean iconified) {
        if (this.mIconifiedByDefault != iconified) {
            this.mIconifiedByDefault = iconified;
            updateViewsVisibility(iconified);
            updateQueryHint();
        }
    }

    public boolean isIconfiedByDefault() {
        return this.mIconifiedByDefault;
    }

    public void setIconified(boolean iconify) {
        if (iconify) {
            onCloseClicked();
        } else {
            onSearchClicked();
        }
    }

    public boolean isIconified() {
        return this.mIconified;
    }

    public void setSubmitButtonEnabled(boolean enabled) {
        this.mSubmitButtonEnabled = enabled;
        updateViewsVisibility(isIconified());
    }

    public boolean isSubmitButtonEnabled() {
        return this.mSubmitButtonEnabled;
    }

    public void setQueryRefinementEnabled(boolean enable) {
        this.mQueryRefinement = enable;
        if (this.mSuggestionsAdapter instanceof AmigoSuggestionsAdapter) {
            ((AmigoSuggestionsAdapter) this.mSuggestionsAdapter).setQueryRefinement(enable ? 2 : 1);
        }
    }

    public boolean isQueryRefinementEnabled() {
        return this.mQueryRefinement;
    }

    public void setSuggestionsAdapter(CursorAdapter adapter) {
        this.mSuggestionsAdapter = adapter;
        this.mQueryTextView.setAdapter(this.mSuggestionsAdapter);
    }

    public CursorAdapter getSuggestionsAdapter() {
        return this.mSuggestionsAdapter;
    }

    public void setMaxWidth(int maxpixels) {
        this.mMaxWidth = maxpixels;
        requestLayout();
    }

    public int getMaxWidth() {
        return this.mMaxWidth;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isIconified()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.AT_MOST:
                if (this.mMaxWidth <= 0) {
                    width = Math.min(getPreferredWidth(), width);
                    break;
                } else {
                    width = Math.min(this.mMaxWidth, width);
                    break;
                }
            case MeasureSpec.UNSPECIFIED:
                width = this.mMaxWidth > 0 ? this.mMaxWidth : getPreferredWidth();
                break;
            case MeasureSpec.EXACTLY:
                if (this.mMaxWidth > 0) {
                    width = Math.min(this.mMaxWidth, width);
                    break;
                }
                break;
        }
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), heightMeasureSpec);
    }

    private int getPreferredWidth() {
        return getContext().getResources().getDimensionPixelSize(AmigoWidgetResource.getIdentifierByDimen(this.mContext, "amigo_search_view_preferred_width"));
    }

    private void updateViewsVisibility(boolean collapsed) {
        int visCollapsed;
        boolean hasText;
        int i = GONE;
        boolean z = true;
        this.mIconified = collapsed;
        if (collapsed) {
            visCollapsed = VISIBLE;
        } else {
            visCollapsed = GONE;
        }
        if (TextUtils.isEmpty(this.mQueryTextView.getText())) {
            hasText = false;
        } else {
            hasText = true;
        }
        this.mSearchButton.setVisibility(visCollapsed);
        updateSubmitButton(hasText);
        View view = this.mSearchEditFrame;
        if (!collapsed) {
            i = VISIBLE;
        }
        view.setVisibility(i);
        updateCloseButton();
        if (hasText) {
            z = false;
        }
        updateVoiceButton(z);
        updateSubmitArea();
    }

    private boolean hasVoiceSearch() {
        if (this.mSearchable == null || !this.mSearchable.getVoiceSearchEnabled()) {
            return false;
        }
        Intent testIntent = null;
        if (this.mSearchable.getVoiceSearchLaunchWebSearch()) {
            testIntent = this.mVoiceWebSearchIntent;
        } else if (this.mSearchable.getVoiceSearchLaunchRecognizer()) {
            testIntent = this.mVoiceAppSearchIntent;
        }
        if (testIntent == null || getContext().getPackageManager().resolveActivity(testIntent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            return false;
        }
        return true;
    }

    private boolean isSubmitAreaEnabled() {
        return (this.mSubmitButtonEnabled || this.mVoiceButtonEnabled) && !isIconified();
    }

    private void updateSubmitButton(boolean hasText) {
        if (!this.mSubmitButtonEnabled || !isSubmitAreaEnabled() || !hasFocus()) {
            return;
        }
        if (!hasText && this.mVoiceButtonEnabled) {
        }
    }

    private void updateSubmitArea() {
    }

    private void updateCloseButton() {
        boolean hasText;
        boolean showClose;
        boolean z = true;
        if (TextUtils.isEmpty(this.mQueryTextView.getText())) {
            hasText = false;
        } else {
            hasText = true;
        }
        if (hasText || (this.mIconifiedByDefault && !this.mExpandedInActionView)) {
            showClose = true;
        } else {
            showClose = false;
        }
        this.mCloseButton.setVisibility(showClose ? VISIBLE : GONE);
        this.mCloseButton.getDrawable().setState(hasText ? ENABLED_STATE_SET : EMPTY_STATE_SET);
        if (this.mIsGnWidget3Style && this.mIsSearchVoiceMode) {
            if (showClose) {
                z = false;
            }
            updateSearchVoiceButton(z);
        }
    }

    private void postUpdateFocusedState() {
        post(this.mUpdateDrawableStateRunnable);
    }

    private void updateFocusedState() {
        this.mSearchPlate.getBackground().setState(this.mQueryTextView.hasFocus() ? FOCUSED_STATE_SET : EMPTY_STATE_SET);
        invalidate();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mSuggestionsAdapter != null && (this.mSuggestionsAdapter instanceof AmigoSuggestionsAdapter)) {
            ((AmigoSuggestionsAdapter) this.mSuggestionsAdapter).enable();
        }
        startBeginAnimation();
    }

    protected void onDetachedFromWindow() {
        removeCallbacks(this.mUpdateDrawableStateRunnable);
        post(this.mReleaseCursorRunnable);
        super.onDetachedFromWindow();
    }

    private void setImeVisibility(boolean visible) {
        if (visible) {
            post(this.mShowImeRunnable);
            return;
        }
        removeCallbacks(this.mShowImeRunnable);
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    void onQueryRefine(CharSequence queryText) {
        setQuery(queryText);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mSearchable == null) {
            return false;
        }
        ActionKeyInfo actionKey = this.mSearchable.findActionKey(keyCode);
        if (actionKey == null || actionKey.getQueryActionMsg() == null) {
            return super.onKeyDown(keyCode, event);
        }
        launchQuerySearch(keyCode, actionKey.getQueryActionMsg(), this.mQueryTextView.getText().toString());
        return true;
    }

    private boolean onSuggestionsKey(View v, int keyCode, KeyEvent event) {
        if (this.mSearchable == null || this.mSuggestionsAdapter == null || event.getAction() != 0 || !event.hasNoModifiers()) {
            return false;
        }
        if (keyCode == 66 || keyCode == 84 || keyCode == 61) {
            return onItemClicked(this.mQueryTextView.getListSelection(), 0, null);
        }
        if (keyCode == 21 || keyCode == 22) {
            int selPoint;
            if (keyCode == 21) {
                selPoint = 0;
            } else {
                selPoint = this.mQueryTextView.length();
            }
            this.mQueryTextView.setSelection(selPoint);
            this.mQueryTextView.setListSelection(0);
            this.mQueryTextView.clearListSelection();
            this.mQueryTextView.ensureImeVisible(true);
            return true;
        } else if (keyCode == 19 && this.mQueryTextView.getListSelection() == 0) {
            return false;
        } else {
            ActionKeyInfo actionKey = this.mSearchable.findActionKey(keyCode);
            if (actionKey == null) {
                return false;
            }
            if (actionKey.getSuggestActionMsg() == null && actionKey.getSuggestActionMsgColumn() == null) {
                return false;
            }
            int position = this.mQueryTextView.getListSelection();
            if (position == -1) {
                return false;
            }
            Cursor c = this.mSuggestionsAdapter.getCursor();
            if (!c.moveToPosition(position)) {
                return false;
            }
            String actionMsg = getActionKeyMessage(c, actionKey);
            if (actionMsg == null || actionMsg.length() <= 0) {
                return false;
            }
            return onItemClicked(position, keyCode, actionMsg);
        }
    }

    private static String getActionKeyMessage(Cursor c, ActionKeyInfo actionKey) {
        String result = null;
        String column = actionKey.getSuggestActionMsgColumn();
        if (column != null) {
            result = AmigoSuggestionsAdapter.getColumnString(c, column);
        }
        if (result == null) {
            return actionKey.getSuggestActionMsg();
        }
        return result;
    }

    private CharSequence getDecoratedHint(CharSequence hintText) {
        if (!this.mIconifiedByDefault) {
            return hintText;
        }
        SpannableStringBuilder ssb = new SpannableStringBuilder("   ");
        ssb.append(hintText);
        int textSize = (int) (((double) this.mQueryTextView.getTextSize()) * 1.25d);
        return ssb;
    }

    private void updateQueryHint() {
        if (this.mQueryHint != null) {
            this.mQueryTextView.setHint(getDecoratedHint(this.mQueryHint));
        } else if (this.mSearchable != null) {
            CharSequence hint = null;
            int hintId = this.mSearchable.getHintId();
            if (hintId != 0) {
                hint = getContext().getString(hintId);
            }
            if (hint != null) {
                this.mQueryTextView.setHint(getDecoratedHint(hint));
            }
        } else {
            this.mQueryTextView.setHint(getDecoratedHint(""));
        }
    }

    private void updateSearchAutoComplete() {
        int i = 1;
        this.mQueryTextView.setDropDownAnimationStyle(0);
        this.mQueryTextView.setThreshold(this.mSearchable.getSuggestThreshold());
        this.mQueryTextView.setImeOptions(this.mSearchable.getImeOptions());
        int inputType = this.mSearchable.getInputType();
        if ((inputType & 15) == 1) {
            inputType &= -65537;
            if (this.mSearchable.getSuggestAuthority() != null) {
                inputType = (inputType | 65536) | 524288;
            }
        }
        this.mQueryTextView.setInputType(inputType);
        if (this.mSuggestionsAdapter != null) {
            this.mSuggestionsAdapter.changeCursor(null);
        }
        if (this.mSearchable.getSuggestAuthority() != null) {
            this.mSuggestionsAdapter = new AmigoSuggestionsAdapter(getContext(), this, this.mSearchable, this.mOutsideDrawablesCache);
            this.mQueryTextView.setAdapter(this.mSuggestionsAdapter);
            AmigoSuggestionsAdapter amigoSuggestionsAdapter = (AmigoSuggestionsAdapter) this.mSuggestionsAdapter;
            if (this.mQueryRefinement) {
                i = 2;
            }
            amigoSuggestionsAdapter.setQueryRefinement(i);
        }
    }

    private void updateVoiceButton(boolean empty) {
        if (!this.mVoiceButtonEnabled || isIconified() || !empty) {
        }
    }

    private void onTextChanged(CharSequence newText) {
        boolean hasText;
        boolean z = true;
        CharSequence text = this.mQueryTextView.getText();
        this.mUserQuery = text;
        if (TextUtils.isEmpty(text)) {
            hasText = false;
        } else {
            hasText = true;
        }
        updateSubmitButton(hasText);
        if (hasText) {
            z = false;
        }
        updateVoiceButton(z);
        updateCloseButton();
        updateSubmitArea();
        if (!(this.mOnQueryChangeListener == null || TextUtils.equals(newText, this.mOldQueryText))) {
            this.mOnQueryChangeListener.onQueryTextChange(newText.toString());
        }
        this.mOldQueryText = newText.toString();
    }

    private void onSubmitQuery() {
        CharSequence query = this.mQueryTextView.getText();
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            if (this.mOnQueryChangeListener == null || !this.mOnQueryChangeListener.onQueryTextSubmit(query.toString())) {
                if (this.mSearchable != null) {
                    launchQuerySearch(0, null, query.toString());
                    setImeVisibility(false);
                }
                dismissSuggestions();
            }
        }
    }

    private void dismissSuggestions() {
        this.mQueryTextView.dismissDropDown();
    }

    private void onCloseClicked() {
        if (!TextUtils.isEmpty(this.mQueryTextView.getText())) {
            this.mQueryTextView.setText("");
            this.mQueryTextView.requestFocus();
            setImeVisibility(true);
        } else if (!this.mIconifiedByDefault) {
        } else {
            if (this.mOnCloseListener == null || !this.mOnCloseListener.onClose()) {
                clearFocus();
                updateViewsVisibility(true);
            }
        }
    }

    private void onSearchClicked() {
        updateViewsVisibility(false);
        this.mQueryTextView.requestFocus();
        setImeVisibility(true);
        if (this.mOnSearchClickListener != null) {
            this.mOnSearchClickListener.onClick(this);
        }
    }

    void onTextFocusChanged() {
        updateViewsVisibility(isIconified());
        postUpdateFocusedState();
        if (this.mQueryTextView.hasFocus()) {
            forceSuggestionQuery();
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        postUpdateFocusedState();
    }

    public void onActionViewCollapsed() {
        clearFocus();
        updateViewsVisibility(true);
        this.mQueryTextView.setImeOptions(this.mCollapsedImeOptions);
        this.mExpandedInActionView = false;
    }

    public void onActionViewExpanded() {
        if (!this.mExpandedInActionView) {
            this.mExpandedInActionView = true;
            this.mCollapsedImeOptions = this.mQueryTextView.getImeOptions();
            this.mQueryTextView.setImeOptions(this.mCollapsedImeOptions | 33554432);
            this.mQueryTextView.setText("");
            setIconified(false);
        }
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AmigoSearchView.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AmigoSearchView.class.getName());
    }

    private void adjustDropDownSizeAndPosition() {
        if (this.mDropDownAnchor.getWidth() > 1) {
            Resources res = getContext().getResources();
            int anchorPadding = this.mSearchPlate.getPaddingLeft();
            Rect dropDownPadding = new Rect();
            int iconOffset = this.mIconifiedByDefault ? res.getDimensionPixelSize(AmigoWidgetResource.getIdentifierByDimen(this.mContext, "amigo_dropdownitem_icon_width")) + res.getDimensionPixelSize(AmigoWidgetResource.getIdentifierByDimen(this.mContext, "amigo_dropdownitem_text_padding_left")) : 0;
            this.mQueryTextView.getDropDownBackground().getPadding(dropDownPadding);
            this.mQueryTextView.setDropDownHorizontalOffset(anchorPadding - (dropDownPadding.left + iconOffset));
            this.mQueryTextView.setDropDownWidth((((this.mDropDownAnchor.getWidth() + dropDownPadding.left) + dropDownPadding.right) + iconOffset) - anchorPadding);
        }
    }

    private boolean onItemClicked(int position, int actionKey, String actionMsg) {
        if (this.mOnSuggestionListener != null && this.mOnSuggestionListener.onSuggestionClick(position)) {
            return false;
        }
        launchSuggestion(position, 0, null);
        setImeVisibility(false);
        dismissSuggestions();
        return true;
    }

    private boolean onItemSelected(int position) {
        if (this.mOnSuggestionListener != null && this.mOnSuggestionListener.onSuggestionSelect(position)) {
            return false;
        }
        rewriteQueryFromSuggestion(position);
        return true;
    }

    private void rewriteQueryFromSuggestion(int position) {
        CharSequence oldQuery = this.mQueryTextView.getText();
        Cursor c = this.mSuggestionsAdapter.getCursor();
        if (c != null) {
            if (c.moveToPosition(position)) {
                CharSequence newQuery = this.mSuggestionsAdapter.convertToString(c);
                if (newQuery != null) {
                    setQuery(newQuery);
                    return;
                } else {
                    setQuery(oldQuery);
                    return;
                }
            }
            setQuery(oldQuery);
        }
    }

    private boolean launchSuggestion(int position, int actionKey, String actionMsg) {
        Cursor c = this.mSuggestionsAdapter.getCursor();
        if (c == null || !c.moveToPosition(position)) {
            return false;
        }
        launchIntent(createIntentFromSuggestion(c, actionKey, actionMsg));
        return true;
    }

    private void launchIntent(Intent intent) {
        if (intent != null) {
            try {
                getContext().startActivity(intent);
            } catch (RuntimeException ex) {
                Log.e(LOG_TAG, "Failed launch activity: " + intent, ex);
            }
        }
    }

    private void setQuery(CharSequence query) {
        this.mQueryTextView.setText(query, true);
        this.mQueryTextView.setSelection(TextUtils.isEmpty(query) ? 0 : query.length());
    }

    private void launchQuerySearch(int actionKey, String actionMsg, String query) {
        getContext().startActivity(createIntent("android.intent.action.SEARCH", null, null, query, actionKey, actionMsg));
    }

    private Intent createIntent(String action, Uri data, String extraData, String query, int actionKey, String actionMsg) {
        Intent intent = new Intent(action);
        intent.addFlags(ColorConfigConstants.DEFAULT_EDIT_TEXT_BACKGROUND_COLOR_B3);
        if (data != null) {
            intent.setData(data);
        }
        intent.putExtra("user_query", this.mUserQuery);
        if (query != null) {
            intent.putExtra("query", query);
        }
        if (extraData != null) {
            intent.putExtra("intent_extra_data_key", extraData);
        }
        if (this.mAppSearchData != null) {
            intent.putExtra("app_data", this.mAppSearchData);
        }
        if (actionKey != 0) {
            intent.putExtra("action_key", actionKey);
            intent.putExtra("action_msg", actionMsg);
        }
        intent.setComponent(this.mSearchable.getSearchActivity());
        return intent;
    }

    private Intent createVoiceWebSearchIntent(Intent baseIntent, SearchableInfo searchable) {
        Intent voiceIntent = new Intent(baseIntent);
        ComponentName searchActivity = searchable.getSearchActivity();
        voiceIntent.putExtra("calling_package", searchActivity == null ? null : searchActivity.flattenToShortString());
        return voiceIntent;
    }

    private Intent createVoiceAppSearchIntent(Intent baseIntent, SearchableInfo searchable) {
        ComponentName searchActivity = searchable.getSearchActivity();
        Intent queryIntent = new Intent("android.intent.action.SEARCH");
        queryIntent.setComponent(searchActivity);
//        PendingIntent pending = PendingIntent.getActivity(getContext(), 0, queryIntent, 0x40000000);
        PendingIntent pending = PendingIntent.getActivity(getContext(), 0, queryIntent, PendingIntent.FLAG_ONE_SHOT);
        Bundle queryExtras = new Bundle();
        if (this.mAppSearchData != null) {
            queryExtras.putParcelable("app_data", this.mAppSearchData);
        }
        Intent voiceIntent = new Intent(baseIntent);
        String languageModel = "free_form";
        String prompt = null;
        String language = null;
        int maxResults = 1;
        Resources resources = getResources();
        if (searchable.getVoiceLanguageModeId() != 0) {
            languageModel = resources.getString(searchable.getVoiceLanguageModeId());
        }
        if (searchable.getVoicePromptTextId() != 0) {
            prompt = resources.getString(searchable.getVoicePromptTextId());
        }
        if (searchable.getVoiceLanguageId() != 0) {
            language = resources.getString(searchable.getVoiceLanguageId());
        }
        if (searchable.getVoiceMaxResults() != 0) {
            maxResults = searchable.getVoiceMaxResults();
        }
        voiceIntent.putExtra("android.speech.extra.LANGUAGE_MODEL", languageModel);
        voiceIntent.putExtra("android.speech.extra.PROMPT", prompt);
        voiceIntent.putExtra("android.speech.extra.LANGUAGE", language);
        voiceIntent.putExtra("android.speech.extra.MAX_RESULTS", maxResults);
        voiceIntent.putExtra("calling_package", searchActivity == null ? null : searchActivity.flattenToShortString());
        voiceIntent.putExtra("android.speech.extra.RESULTS_PENDINGINTENT", pending);
        voiceIntent.putExtra("android.speech.extra.RESULTS_PENDINGINTENT_BUNDLE", queryExtras);
        return voiceIntent;
    }

    private Intent createIntentFromSuggestion(Cursor c, int actionKey, String actionMsg) {
        try {
            String action = AmigoSuggestionsAdapter.getColumnString(c, "suggest_intent_action");
            if (action == null) {
                action = this.mSearchable.getSuggestIntentAction();
            }
            if (action == null) {
                action = "android.intent.action.SEARCH";
            }
            String data = AmigoSuggestionsAdapter.getColumnString(c, "suggest_intent_data");
            if (data == null) {
                data = this.mSearchable.getSuggestIntentData();
            }
            if (data != null) {
                String id = AmigoSuggestionsAdapter.getColumnString(c, "suggest_intent_data_id");
                if (id != null) {
                    data = data + "/" + Uri.encode(id);
                }
            }
            return createIntent(action, data == null ? null : Uri.parse(data), AmigoSuggestionsAdapter.getColumnString(c, "suggest_intent_extra_data"), AmigoSuggestionsAdapter.getColumnString(c, "suggest_intent_query"), actionKey, actionMsg);
        } catch (RuntimeException e) {
            int rowNum;
            try {
                rowNum = c.getPosition();
            } catch (RuntimeException e2) {
                rowNum = -1;
            }
            Log.w(LOG_TAG, "Search suggestions cursor at row " + rowNum + " returned exception.", e);
            return null;
        }
    }

    private void forceSuggestionQuery() {
        this.mQueryTextView.doBeforeTextChanged();
        this.mQueryTextView.doAfterTextChanged();
    }

    static boolean isLandscapeMode(Context context) {
        return context.getResources().getConfiguration().orientation == 2;
    }

    private void initSearchView() {
        this.mSearchViewLayout = (LinearLayout) findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_search_bar"));
        this.mGnSearchBgLayout = (LinearLayout) findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_search_background"));
        this.mSearchViewZoomAnim = AnimationUtils.loadAnimation(getContext(), AmigoWidgetResource.getIdentifierByAnim(getContext(), "amigo_searchview_zoom"));
        this.mSearchViewAnim = AnimationUtils.loadAnimation(getContext(), AmigoWidgetResource.getIdentifierByAnim(getContext(), "amigo_searchview_text"));
        this.mGnSearchVoiceButton = (ImageView) findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_search_voice_btn"));
        this.mGnSearchGoButton = (ImageView) findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_search_go_btn"));
        if (this.mGnSearchBgLayout != null) {
            this.mGnSearchBgLayout.setBackgroundColor(this.mQueryTextViewBackgroundColor);
        }
        if (this.mGnSearchVoiceButton != null && this.mVoiceIcon != null) {
            this.mGnSearchVoiceButton.setImageDrawable(this.mVoiceIcon);
        }
    }

    public void setSubmitSearchMode(boolean isSubmitSearchMode) {
        setSubmitSearchMode(isSubmitSearchMode, null);
    }

    public void setSubmitSearchMode(boolean isSubmitSearchMode, OnClickListener searchSubmitClickListener) {
        this.mIsSearchSubmitMode = isSubmitSearchMode;
        if (this.mGnSearchGoButton != null) {
            this.mGnSearchGoButton.setVisibility(isSubmitSearchMode ? VISIBLE : GONE);
            if (isSubmitSearchMode) {
                this.mGnSearchBgLayout.setBackgroundResource(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_searchview_background_submit"));
                this.mGnSearchGoButton.setOnClickListener(searchSubmitClickListener);
            }
        }
    }

    public void setVoiceSearchMode(boolean isVoiceSearchMode, OnClickListener searchVoiceClickListener) {
        this.mIsSearchVoiceMode = isVoiceSearchMode;
        if (this.mGnSearchVoiceButton != null) {
            this.mGnSearchVoiceButton.setVisibility(isVoiceSearchMode ? VISIBLE : GONE);
            if (this.mIsSearchVoiceMode) {
                this.mGnSearchVoiceButton.setOnClickListener(searchVoiceClickListener);
                if (ChameleonColorManager.isNeedChangeColor()) {
                    this.mGnSearchVoiceButton.getDrawable().setColorFilter(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1(), Mode.SRC_IN);
                    changeSelectableItemBackground(this.mGnSearchVoiceButton);
                }
            }
        }
    }

    private void updateSearchVoiceButton(boolean isShow) {
        if (this.mGnSearchVoiceButton != null) {
            this.mGnSearchVoiceButton.setVisibility(isShow ? VISIBLE : GONE);
        }
    }

    private void startBeginAnimation() {
        this.mSearchViewLayout.startAnimation(this.mSearchViewZoomAnim);
    }

    public void startEndAnimation() {
    }

    private void setupSearchViewUnfoldAnim() {
        MarginLayoutParams lp = (MarginLayoutParams) this.mSearchEditFrame.getLayoutParams();
        this.mSearchViewUnfoldAnim = new UnfoldAnimation(this.mGnSearchBgLayout.getWidth(), this.mSearchEditFrame.getWidth());
        this.mSearchViewUnfoldAnim.setAnimationListener(this.mSearchAnimationListener);
        this.mSearchViewUnfoldAnim.setInterpolator(getContext(), android.R.anim.decelerate_interpolator);
        this.mSearchViewUnfoldAnim.setDuration(300);
    }

    private boolean isGioneeViewStyle() {
        return true;
    }

    private boolean isGioneeWidget3Support() {
        return true;
    }
}
