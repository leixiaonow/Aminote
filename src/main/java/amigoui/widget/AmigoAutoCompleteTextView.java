package amigoui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Filter;
import android.widget.Filter.FilterListener;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.gionee.aminote.R;

import amigoui.changecolors.ChameleonColorManager;
import amigoui.reflection.AmigoRippleDrawable;

public class AmigoAutoCompleteTextView extends AmigoEditText implements FilterListener {
    static final boolean DEBUG = false;
    static final int EXPAND_MAX = 3;
    static final String TAG = "AutoCompleteTextView";
    private ListAdapter mAdapter;
    private boolean mBlockCompletion;
    private int mDropDownAnchorId;
    private boolean mDropDownDismissedOnCompletion;
    private Filter mFilter;
    private int mHintResource;
    private CharSequence mHintText;
    private TextView mHintView;
    private OnItemClickListener mItemClickListener;
    private OnItemSelectedListener mItemSelectedListener;
    private int mLastKeyCode;
    private Drawable mListSelector;
    private PopupDataSetObserver mObserver;
    private boolean mOpenBefore;
    private PassThroughClickListener mPassThroughClickListener;
    private ListPopupWindow mPopup;
    private boolean mPopupCanBeUpdated;
    private int mThreshold;
    private Validator mValidator;

    private class DropDownItemClickListener implements OnItemClickListener {
        private DropDownItemClickListener() {
        }

        public void onItemClick(AdapterView parent, View v, int position, long id) {
            AmigoAutoCompleteTextView.this.performCompletion(v, position, id);
        }
    }

    private class MyWatcher implements TextWatcher {
        private MyWatcher() {
        }

        public void afterTextChanged(Editable s) {
            AmigoAutoCompleteTextView.this.doAfterTextChanged();
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            AmigoAutoCompleteTextView.this.doBeforeTextChanged();
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    private class PassThroughClickListener implements OnClickListener {
        private OnClickListener mWrapped;

        private PassThroughClickListener() {
        }

        public void onClick(View v) {
            AmigoAutoCompleteTextView.this.onClickImpl();
            if (this.mWrapped != null) {
                this.mWrapped.onClick(v);
            }
        }
    }

    private class PopupDataSetObserver extends DataSetObserver {
        private PopupDataSetObserver() {
        }

        public void onChanged() {
            if (AmigoAutoCompleteTextView.this.mAdapter != null) {
                AmigoAutoCompleteTextView.this.post(new Runnable() {
                    public void run() {
                        ListAdapter adapter = AmigoAutoCompleteTextView.this.mAdapter;
                        if (adapter != null) {
                            AmigoAutoCompleteTextView.this.updateDropDownForFilter(adapter.getCount());
                        }
                    }
                });
            }
        }
    }

    public interface Validator {
        CharSequence fixText(CharSequence charSequence);

        boolean isValid(CharSequence charSequence);
    }

    public AmigoAutoCompleteTextView(Context context) {
        this(context, null);
    }

    public AmigoAutoCompleteTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.autoCompleteTextViewStyle);
    }

    public AmigoAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDropDownDismissedOnCompletion = true;
        this.mLastKeyCode = 0;
        this.mValidator = null;
        this.mPopupCanBeUpdated = true;
        this.mPopup = new ListPopupWindow(context, attrs, android.R.attr.autoCompleteTextViewStyle);
        this.mPopup.setSoftInputMode(16);
        this.mPopup.setPromptPosition(1);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoAutoCompleteTextView, defStyle, 0);
        this.mThreshold = a.getInt(R.styleable.AmigoAutoCompleteTextView_amigocompletionThreshold, 2);
        this.mListSelector = a.getDrawable(R.styleable.AmigoAutoCompleteTextView_amigodropDownSelector);
        this.mPopup.setListSelector(this.mListSelector);
        this.mPopup.setVerticalOffset((int) a.getDimension(R.styleable.AmigoAutoCompleteTextView_amigodropDownVerticalOffset, 0.0f));
        this.mPopup.setHorizontalOffset((int) a.getDimension(R.styleable.AmigoAutoCompleteTextView_amigodropDownHorizontalOffset, 0.0f));
        this.mDropDownAnchorId = a.getResourceId(R.styleable.AmigoAutoCompleteTextView_amigodropDownAnchor, -1);
        this.mPopup.setWidth(a.getLayoutDimension(R.styleable.AmigoAutoCompleteTextView_amigodropDownWidth, -2));
        this.mPopup.setHeight(a.getLayoutDimension(R.styleable.AmigoAutoCompleteTextView_amigodropDownHeight, -2));
        this.mHintResource = a.getResourceId(R.styleable.AmigoAutoCompleteTextView_amigocompletionHintView, R.layout.amigo_simple_dropdown_hint);
        this.mPopup.setOnItemClickListener(new DropDownItemClickListener());
        setCompletionHint(a.getText(R.styleable.AmigoAutoCompleteTextView_amigocompletionHint));
        int inputType = getInputType();
        if ((inputType & 15) == 1) {
            setRawInputType(inputType | 65536);
        }
        a.recycle();
        setFocusable(true);
        addTextChangedListener(new MyWatcher());
        this.mPassThroughClickListener = new PassThroughClickListener();
        super.setOnClickListener(this.mPassThroughClickListener);
        if (ChameleonColorManager.isNeedChangeColor()) {
            this.mPopup.setBackgroundDrawable(new ColorDrawable(ChameleonColorManager.getPopupBackgroudColor_B2()));
            changeListSelector(this.mListSelector);
        }
    }

    private void changeListSelector(Drawable selector) {
        if (selector != null && AmigoRippleDrawable.instanceofRippleDrawable(selector)) {
            AmigoRippleDrawable.setColor(selector, ColorStateList.valueOf(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3()));
        }
    }

    public void setOnClickListener(OnClickListener listener) {
        this.mPassThroughClickListener.mWrapped = listener;
    }

    private void onClickImpl() {
        if (isPopupShowing()) {
            ensureImeVisible(true);
        }
    }

    public void setCompletionHint(CharSequence hint) {
        this.mHintText = hint;
        if (hint == null) {
            this.mPopup.setPromptView(null);
            this.mHintView = null;
        } else if (this.mHintView == null) {
            TextView hintView = (TextView) LayoutInflater.from(getContext()).inflate(this.mHintResource, null).findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_text1"));
            hintView.setText(this.mHintText);
            this.mHintView = hintView;
            this.mPopup.setPromptView(hintView);
        } else {
            this.mHintView.setText(hint);
        }
    }

    public CharSequence getCompletionHint() {
        return this.mHintText;
    }

    public int getDropDownWidth() {
        return this.mPopup.getWidth();
    }

    public void setDropDownWidth(int width) {
        this.mPopup.setWidth(width);
    }

    public int getDropDownHeight() {
        return this.mPopup.getHeight();
    }

    public void setDropDownHeight(int height) {
        this.mPopup.setHeight(height);
    }

    public int getDropDownAnchor() {
        return this.mDropDownAnchorId;
    }

    public void setDropDownAnchor(int id) {
        this.mDropDownAnchorId = id;
        this.mPopup.setAnchorView(null);
    }

    public Drawable getDropDownBackground() {
        return this.mPopup.getBackground();
    }

    public void setDropDownBackgroundDrawable(Drawable d) {
        this.mPopup.setBackgroundDrawable(d);
    }

    public void setDropDownBackgroundResource(int id) {
        this.mPopup.setBackgroundDrawable(getResources().getDrawable(id));
    }

    public void setDropDownVerticalOffset(int offset) {
        this.mPopup.setVerticalOffset(offset);
    }

    public int getDropDownVerticalOffset() {
        return this.mPopup.getVerticalOffset();
    }

    public void setDropDownHorizontalOffset(int offset) {
        this.mPopup.setHorizontalOffset(offset);
    }

    public int getDropDownHorizontalOffset() {
        return this.mPopup.getHorizontalOffset();
    }

    public void setDropDownAnimationStyle(int animationStyle) {
        this.mPopup.setAnimationStyle(animationStyle);
    }

    //change
//    public boolean isDropDownAlwaysVisible(){
//        try {
//            return (Boolean)mPopup.getClass().getMethod("isDropDownAlwaysVisible").invoke("isDropDownAlwaysVisible");
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    public int getDropDownAnimationStyle() {
        return this.mPopup.getAnimationStyle();
    }

    //change
    public boolean isDropDownAlwaysVisible() {
        return this.mPopup.isDropDownAlwaysVisible();
    }

    //change
    public void setDropDownAlwaysVisible(boolean dropDownAlwaysVisible) {
        this.mPopup.setDropDownAlwaysVisible(dropDownAlwaysVisible);
//        try {
//            mPopup.getClass().getMethod("setDropDownAlwaysVisible",Boolean.TYPE).invoke("setDropDownAlwaysVisible",dropDownAlwaysVisible);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }

    }

    public boolean isDropDownDismissedOnCompletion() {
        return this.mDropDownDismissedOnCompletion;
    }

    public void setDropDownDismissedOnCompletion(boolean dropDownDismissedOnCompletion) {
        this.mDropDownDismissedOnCompletion = dropDownDismissedOnCompletion;
    }

    public int getThreshold() {
        return this.mThreshold;
    }

    public void setThreshold(int threshold) {
        if (threshold <= 0) {
            threshold = 1;
        }
        this.mThreshold = threshold;
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mItemClickListener = l;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener l) {
        this.mItemSelectedListener = l;
    }

    @Deprecated
    public OnItemClickListener getItemClickListener() {
        return this.mItemClickListener;
    }

    @Deprecated
    public OnItemSelectedListener getItemSelectedListener() {
        return this.mItemSelectedListener;
    }

    public OnItemClickListener getOnItemClickListener() {
        return this.mItemClickListener;
    }

    public OnItemSelectedListener getOnItemSelectedListener() {
        return this.mItemSelectedListener;
    }

    public void setOnDismissListener(final OnDismissListener dismissListener) {
        android.widget.PopupWindow.OnDismissListener wrappedListener = null;
        if (dismissListener != null) {
            wrappedListener = new android.widget.PopupWindow.OnDismissListener() {
                public void onDismiss() {
                    dismissListener.onDismiss();
                }
            };
        }
        this.mPopup.setOnDismissListener(wrappedListener);
    }

    public ListAdapter getAdapter() {
        return this.mAdapter;
    }

    public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
        if (this.mObserver == null) {
            this.mObserver = new PopupDataSetObserver();
        } else if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(this.mObserver);
        }
        this.mAdapter = adapter;
        if (this.mAdapter != null) {
            this.mFilter = ((Filterable) this.mAdapter).getFilter();
            adapter.registerDataSetObserver(this.mObserver);
        } else {
            this.mFilter = null;
        }
        this.mPopup.setAdapter(this.mAdapter);
    }

    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == 4 && isPopupShowing() && !this.isDropDownAlwaysVisible()) {
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
                    dismissDropDown();
                    return true;
                }
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mPopup.onKeyUp(keyCode, event)) {
            switch (keyCode) {
                case 23:
                case 61:
                case 66:
                    if (!event.hasNoModifiers()) {
                        return true;
                    }
                    performCompletion();
                    return true;
            }
        }
        if (!isPopupShowing() || keyCode != 61 || !event.hasNoModifiers()) {
            return super.onKeyUp(keyCode, event);
        }
        performCompletion();
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean z = true;
        if (!this.mPopup.onKeyDown(keyCode, event)) {
            if (!isPopupShowing()) {
                switch (keyCode) {
                    case 20:
                        if (event.hasNoModifiers()) {
                            performValidation();
                            break;
                        }
                        break;
                }
            }
            if (!(isPopupShowing() && keyCode == 61 && event.hasNoModifiers())) {
                this.mLastKeyCode = keyCode;
                z = super.onKeyDown(keyCode, event);
                this.mLastKeyCode = 0;
                if (z && isPopupShowing()) {
                    clearListSelection();
                }
            }
        }
        return z;
    }

    public boolean enoughToFilter() {
        return getText().length() >= this.mThreshold;
    }

    void doBeforeTextChanged() {
        if (!this.mBlockCompletion) {
            this.mOpenBefore = isPopupShowing();
        }
    }

    void doAfterTextChanged() {
        if (!this.mBlockCompletion) {
            if (this.mOpenBefore && !isPopupShowing()) {
                return;
            }
            if (!enoughToFilter()) {
                if (!this.isDropDownAlwaysVisible()) {
                    dismissDropDown();
                }
                if (this.mFilter != null) {
                    this.mFilter.filter(null);
                }
            } else if (this.mFilter != null) {
                this.mPopupCanBeUpdated = true;
                performFiltering(getText(), this.mLastKeyCode);
            }
        }
    }

    public boolean isPopupShowing() {
        return this.mPopup.isShowing();
    }

    protected CharSequence convertSelectionToString(Object selectedItem) {
        return this.mFilter.convertResultToString(selectedItem);
    }

    public void clearListSelection() {
        this.mPopup.clearListSelection();
    }

    public void setListSelection(int position) {
        this.mPopup.setSelection(position);
    }

    public int getListSelection() {
        return this.mPopup.getSelectedItemPosition();
    }

    protected void performFiltering(CharSequence text, int keyCode) {
        this.mFilter.filter(text, this);
    }

    public void performCompletion() {
        performCompletion(null, -1, -1);
    }

    public void onCommitCompletion(CompletionInfo completion) {
        if (isPopupShowing()) {
            this.mPopup.performItemClick(completion.getPosition());
        }
    }

    private void performCompletion(View selectedView, int position, long id) {
        if (isPopupShowing()) {
            Object selectedItem;
            if (position < 0) {
                selectedItem = this.mPopup.getSelectedItem();
            } else {
                selectedItem = this.mAdapter.getItem(position);
            }
            if (selectedItem == null) {
                Log.w(TAG, "performCompletion: no selected item");
                return;
            }
            this.mBlockCompletion = true;
            replaceText(convertSelectionToString(selectedItem));
            this.mBlockCompletion = false;
            if (this.mItemClickListener != null) {
                ListPopupWindow list = this.mPopup;
                if (selectedView == null || position < 0) {
                    selectedView = list.getSelectedView();
                    position = list.getSelectedItemPosition();
                    id = list.getSelectedItemId();
                }
                this.mItemClickListener.onItemClick(list.getListView(), selectedView, position, id);
            }
        }
        if (this.mDropDownDismissedOnCompletion && !this.isDropDownAlwaysVisible()) {
            dismissDropDown();
        }
    }

    public boolean isPerformingCompletion() {
        return this.mBlockCompletion;
    }

    public void setText(CharSequence text, boolean filter) {
        if (filter) {
            setText(text);
            return;
        }
        this.mBlockCompletion = true;
        setText(text);
        this.mBlockCompletion = false;
    }

    protected void replaceText(CharSequence text) {
        clearComposingText();
        setText(text);
        Editable spannable = getText();
        Selection.setSelection(spannable, spannable.length());
    }

    public void onFilterComplete(int count) {
        updateDropDownForFilter(count);
    }

    private void updateDropDownForFilter(int count) {
        if (getWindowVisibility() != GONE) {//8
            boolean dropDownAlwaysVisible = this.isDropDownAlwaysVisible();
            boolean enoughToFilter = enoughToFilter();
            if ((count > 0 || dropDownAlwaysVisible) && enoughToFilter) {
                if (hasFocus() && hasWindowFocus() && this.mPopupCanBeUpdated) {
                    showDropDown();
                }
            } else if (!dropDownAlwaysVisible && isPopupShowing()) {
                dismissDropDown();
                this.mPopupCanBeUpdated = true;
            }
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus && !this.isDropDownAlwaysVisible()) {
            dismissDropDown();
        }
    }

    protected void onDisplayHint(int hint) {
        super.onDisplayHint(hint);
        switch (hint) {
            case 4:
                if (!this.isDropDownAlwaysVisible()) {
                    dismissDropDown();
                    return;
                }
                return;
            default:
        }
    }

    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (!focused) {
            performValidation();
        }
        if (!focused && !this.isDropDownAlwaysVisible()) {
            dismissDropDown();
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        dismissDropDown();
        super.onDetachedFromWindow();
    }

    public void dismissDropDown() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.displayCompletions(this, null);
        }
        this.mPopup.dismiss();
        this.mPopupCanBeUpdated = false;
    }

    protected boolean setFrame(int l, int t, int r, int b) {
        boolean result = super.setFrame(l, t, r, b);
        if (isPopupShowing()) {
            showDropDown();
        }
        return result;
    }

    public void showDropDownAfterLayout() {
        this.mPopup.postShow();
    }

    public void ensureImeVisible(boolean visible) {
        this.mPopup.setInputMethodMode(visible ? 1 : 2);
        if (this.isDropDownAlwaysVisible() || (this.mFilter != null && enoughToFilter())) {
            showDropDown();
        }
    }

    public boolean isInputMethodNotNeeded() {
        return this.mPopup.getInputMethodMode() == 2;
    }

    public void showDropDown() {
        buildImeCompletions();
        if (this.mPopup.getAnchorView() == null) {
            if (this.mDropDownAnchorId != -1) {
                this.mPopup.setAnchorView(getRootView().findViewById(this.mDropDownAnchorId));
            } else {
                this.mPopup.setAnchorView(this);
            }
        }
        if (!isPopupShowing()) {
            this.mPopup.setInputMethodMode(1);
        }
        this.mPopup.show();
        this.mPopup.getListView().setOverScrollMode(0);
    }

    public void setForceIgnoreOutsideTouch(boolean forceIgnoreOutsideTouch) {
        this.mPopup.setForceIgnoreOutsideTouch(forceIgnoreOutsideTouch);
//        try {
//            mPopup.getClass().getMethod("setForceIgnoreOutsideTouch",Boolean.TYPE).invoke("setForceIgnoreOutsideTouch",forceIgnoreOutsideTouch);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }
    }

    private void buildImeCompletions() {
        ListAdapter adapter = this.mAdapter;
        if (adapter != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                int count = Math.min(adapter.getCount(), 20);
                CompletionInfo[] completions = new CompletionInfo[count];
                int realCount = 0;
                for (int i = 0; i < count; i++) {
                    if (adapter.isEnabled(i)) {
                        completions[realCount] = new CompletionInfo(adapter.getItemId(i), realCount, convertSelectionToString(adapter.getItem(i)));
                        realCount++;
                    }
                }
                if (realCount != count) {
                    CompletionInfo[] tmp = new CompletionInfo[realCount];
                    System.arraycopy(completions, 0, tmp, 0, realCount);
                    completions = tmp;
                }
                imm.displayCompletions(this, completions);
            }
        }
    }

    public void setValidator(Validator validator) {
        this.mValidator = validator;
    }

    public Validator getValidator() {
        return this.mValidator;
    }

    public void performValidation() {
        if (this.mValidator != null) {
            CharSequence text = getText();
            if (!TextUtils.isEmpty(text) && !this.mValidator.isValid(text)) {
                setText(this.mValidator.fixText(text));
            }
        }
    }

    protected Filter getFilter() {
        return this.mFilter;
    }
}
