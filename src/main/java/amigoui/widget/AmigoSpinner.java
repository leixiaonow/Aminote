package amigoui.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.gionee.aminote.R;

import amigoui.app.AmigoAlertDialog;
import amigoui.changecolors.ChameleonColorManager;

public class AmigoSpinner extends Spinner
{
    public static final int MODE_DIALOG = 0;
    public static final int MODE_DROPDOWN = 1;
    private static final int MODE_THEME = -1;
    private int mDropDownWidth;
    private ListAdapter mListAdapter;
    private int mMode;
    AdapterView.OnItemClickListener mOnItemClickListener;
    private DropdownPopup mPopup;
    private CharSequence mPromptText;

    public AmigoSpinner(final Context context) {
        this(context, null);
    }

    public AmigoSpinner(final Context context, final int n) {
        this(context, null, 16842881, n);
    }

    public AmigoSpinner(final Context context, final AttributeSet set) {
        this(context, set, 16842881);
    }

    public AmigoSpinner(final Context context, final AttributeSet set, final int n) {
        this(context, set, n, 0, -1);
    }

    public AmigoSpinner(final Context context, final AttributeSet set, final int n, final int n2) {
        this(context, set, n, 0, n2);
    }

    public AmigoSpinner(final Context context, final AttributeSet set, final int n, final int n2, final int mMode) {
        super(context, set, n);
        final TypedArray obtainStyledAttributes = context.obtainStyledAttributes(set, R.styleable.AmigoSpinner, n, 0);
        this.mMode = mMode;
        if (this.mMode == -1) {
            this.mMode = obtainStyledAttributes.getInt(R.styleable.AmigoSpinner_amigospinnerMode, 1);
        }
        if (this.isModeDropDown()) {
            final DropdownPopup mPopup = new DropdownPopup(context, set, n, 0);
//            change
//            this.mDropDownWidth = obtainStyledAttributes.getLayoutDimension(4, -2);
            this.mDropDownWidth = obtainStyledAttributes.getLayoutDimension(R.styleable.AmigoSpinner_amigoprompt, -2);
            this.mPopup = mPopup;
        }
        final CharSequence[] textArray = obtainStyledAttributes.getTextArray(R.styleable.AmigoSpinner_amigoentries);
        if (textArray != null) {
            final int identifierByLayout = AmigoWidgetResource.getIdentifierByLayout(context, "amigo_simple_spinner_item");
            final int identifierByLayout2 = AmigoWidgetResource.getIdentifierByLayout(context, "amigo_simple_spinner_dropdown_item");
            final ArrayAdapter adapter = new ArrayAdapter<>(context, identifierByLayout, textArray);
            adapter.setDropDownViewResource(identifierByLayout2);
            this.setAdapter((SpinnerAdapter)adapter);
        }
        if (ChameleonColorManager.isNeedChangeColor()) {
            this.changeStateDrawable();
            if (this.isModeDropDown()) {
                this.setPopupBackgroundDrawable((Drawable)new ColorDrawable(ChameleonColorManager.getBackgroudColor_B1()));
            }
        }
        this.setPrompt(obtainStyledAttributes.getString(R.styleable.AmigoSpinner_amigoprompt));
        obtainStyledAttributes.recycle();
    }

    static /* synthetic */ boolean access$300(final AmigoSpinner amigoSpinner) {
        return amigoSpinner.isVisibleToUser();
    }

    private void changeStateDrawable() {
    }

    private boolean isModeDropDown() {
        return this.mMode != 0;
    }

    private boolean stateIsSelected(final int[] array) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == 16842919) {
                return true;
            }
        }
        return false;
    }

    public CharSequence getPrompt() {
        return this.mPromptText;
    }

    protected void onDetachedFromWindow() {
        if (this.isModeDropDown() && this.mPopup != null && this.mPopup.isShowing()) {
            this.mPopup.dismiss();
        }
        super.onDetachedFromWindow();
    }

    public boolean performClick() {
        if (this.mMode == 0) {
            new AmigoAlertDialog.Builder(this.getContext()).setTitle(this.getPrompt()).setSingleChoiceItems(this.mListAdapter, this.getSelectedItemPosition(), (DialogInterface.OnClickListener)this).show();
        }
        else if (!this.mPopup.isShowing()) {
            this.mPopup.show(this.getTextDirection(), this.getTextAlignment());
        }
        return true;
    }

    public void setAdapter(final SpinnerAdapter adapter) {
        super.setAdapter(adapter);
        this.mListAdapter = (ListAdapter)new DropDownAdapter(adapter);
        if (this.mPopup != null && this.isModeDropDown()) {
            this.mPopup.setAdapter(this.mListAdapter);
        }
    }

    public void setDropDownHorizontalOffset(final int horizontalOffset) {
        if (this.isModeDropDown()) {
            this.mPopup.setHorizontalOffset(horizontalOffset);
        }
    }

    public void setDropDownVerticalOffset(final int verticalOffset) {
        if (this.isModeDropDown()) {
            this.mPopup.setVerticalOffset(verticalOffset);
        }
    }

    public void setDropDownWidth(final int mDropDownWidth) {
        this.mDropDownWidth = mDropDownWidth;
    }

    public void setOnItemClickListener(final AdapterView.OnItemClickListener adapterView$OnItemClickListener) {
        super.setOnItemClickListener(adapterView$OnItemClickListener);
        this.mOnItemClickListener = adapterView$OnItemClickListener;
    }

    public void setPopupBackgroundDrawable(final Drawable drawable) {
        if (this.isModeDropDown() && this.mPopup != null) {
            this.mPopup.setBackgroundDrawable((Drawable)new ColorDrawable(ChameleonColorManager.getPopupBackgroudColor_B2()));
        }
    }

    public void setPrompt(final CharSequence charSequence) {
        if (this.isModeDropDown() && this.mPopup != null) {
            this.mPopup.setPromptText(charSequence);
        }
        this.mPromptText = charSequence;
    }

    private static class DropDownAdapter implements ListAdapter, SpinnerAdapter
    {
        private SpinnerAdapter mAdapter;
        private ListAdapter mListAdapter;

        public DropDownAdapter(final SpinnerAdapter mAdapter) {
            this.mAdapter = mAdapter;
            if (mAdapter instanceof ListAdapter) {
                this.mListAdapter = (ListAdapter)mAdapter;
            }
        }

        public boolean areAllItemsEnabled() {
            final ListAdapter mListAdapter = this.mListAdapter;
            return mListAdapter == null || mListAdapter.areAllItemsEnabled();
        }

        public int getCount() {
            if (this.mAdapter == null) {
                return 0;
            }
            return this.mAdapter.getCount();
        }

        public View getDropDownView(final int n, final View view, final ViewGroup viewGroup) {
            if (this.mAdapter == null) {
                return null;
            }
            return this.mAdapter.getDropDownView(n, view, viewGroup);
        }

        public Object getItem(final int n) {
            if (this.mAdapter == null) {
                return null;
            }
            return this.mAdapter.getItem(n);
        }

        public long getItemId(final int n) {
            if (this.mAdapter == null) {
                return -1L;
            }
            return this.mAdapter.getItemId(n);
        }

        public int getItemViewType(final int n) {
            return 0;
        }

        public View getView(final int n, final View view, final ViewGroup viewGroup) {
            return this.getDropDownView(n, view, viewGroup);
        }

        public int getViewTypeCount() {
            return 1;
        }

        public boolean hasStableIds() {
            return this.mAdapter != null && this.mAdapter.hasStableIds();
        }

        public boolean isEmpty() {
            return this.getCount() == 0;
        }

        public boolean isEnabled(final int n) {
            final ListAdapter mListAdapter = this.mListAdapter;
            return mListAdapter == null || mListAdapter.isEnabled(n);
        }

        public void registerDataSetObserver(final DataSetObserver dataSetObserver) {
            if (this.mAdapter != null) {
                this.mAdapter.registerDataSetObserver(dataSetObserver);
            }
        }

        public void unregisterDataSetObserver(final DataSetObserver dataSetObserver) {
            if (this.mAdapter != null) {
                this.mAdapter.unregisterDataSetObserver(dataSetObserver);
            }
        }
    }

    private class DropdownPopup extends ListPopupWindow
    {
        private static final int MAX_ITEMS_MEASURED = 15;
        private ListAdapter mAdapter;
        private CharSequence mHintText;
        private Rect mTempRect;

        public DropdownPopup(final Context context, final AttributeSet set, final int n, final int n2) {
            super(context, set, n, n2);
            this.mTempRect = new Rect();
            this.setAnchorView((View)AmigoSpinner.this);
            this.setModal(true);
            this.setPromptPosition(0);
            this.setOnItemClickListener((AdapterView.OnItemClickListener)new AdapterView.OnItemClickListener() {
                public void onItemClick(final AdapterView adapterView, final View view, final int selection, final long n) {
                    AmigoSpinner.this.setSelection(selection);
                    if (AmigoSpinner.this.mOnItemClickListener != null) {
                        AmigoSpinner.this.performItemClick(view, selection, DropdownPopup.this.mAdapter.getItemId(selection));
                    }
                    DropdownPopup.this.dismiss();
                }
            });
        }

        private void changeDropDownListSelector(final Drawable drawable) {
        }

        void computeContentWidth() {
            final Drawable background = this.getBackground();
            int right;
            if (background != null) {
                background.getPadding(this.mTempRect);
                if (AmigoSpinner.this.isLayoutRtl()) {
                    right = this.mTempRect.right;
                }
                else {
                    right = -this.mTempRect.left;
                }
            }
            else {
                final Rect mTempRect = this.mTempRect;
                this.mTempRect.right = 0;
                mTempRect.left = 0;
                right = 0;
            }
            final int paddingLeft = AmigoSpinner.this.getPaddingLeft();
            final int paddingRight = AmigoSpinner.this.getPaddingRight();
            final int width = AmigoSpinner.this.getWidth();
            if (AmigoSpinner.this.mDropDownWidth == -2) {
                int measureContentWidth = this.measureContentWidth((SpinnerAdapter)this.mAdapter, this.getBackground());
                final int n = AmigoSpinner.this.mContext.getResources().getDisplayMetrics().widthPixels - this.mTempRect.left - this.mTempRect.right;
                if (measureContentWidth > n) {
                    measureContentWidth = n;
                }
                this.setContentWidth(Math.max(measureContentWidth, width - paddingLeft - paddingRight));
            }
            else if (AmigoSpinner.this.mDropDownWidth == -1) {
                this.setContentWidth(width - paddingLeft - paddingRight);
            }
            else {
                this.setContentWidth(AmigoSpinner.this.mDropDownWidth);
            }
            int horizontalOffset;
            if (AmigoSpinner.this.isLayoutRtl()) {
                horizontalOffset = right + (width - paddingRight - this.getWidth());
            }
            else {
                horizontalOffset = right + paddingLeft;
            }
            this.setHorizontalOffset(horizontalOffset);
        }

        public void dismiss() {
            super.dismiss();
        }

        public CharSequence getHintText() {
            return this.mHintText;
        }

        int measureContentWidth(final SpinnerAdapter spinnerAdapter, final Drawable drawable) {
            int max;
            if (spinnerAdapter == null) {
                max = 0;
            }
            else {
                max = 0;
                View view = null;
                int n = 0;
                final int measureSpec = View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                final int measureSpec2 = View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                final int max2 = Math.max(0, this.getSelectedItemPosition());
                for (int min = Math.min(spinnerAdapter.getCount(), max2 + 15), i = Math.max(0, max2 - (15 - (min - max2))); i < min; ++i) {
                    final int itemViewType = spinnerAdapter.getItemViewType(i);
                    if (itemViewType != n) {
                        n = itemViewType;
                        view = null;
                    }
                    view = spinnerAdapter.getView(i, view, (ViewGroup)AmigoSpinner.this);
                    if (view.getLayoutParams() == null) {
                        view.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
                    }
                    view.measure(measureSpec, measureSpec2);
                    max = Math.max(max, view.getMeasuredWidth());
                }
                if (drawable != null) {
                    drawable.getPadding(this.mTempRect);
                    return max + (this.mTempRect.left + this.mTempRect.right);
                }
            }
            return max;
        }

        public void setAdapter(final ListAdapter listAdapter) {
            super.setAdapter(listAdapter);
            this.mAdapter = listAdapter;
        }

        public void setPromptText(final CharSequence mHintText) {
            this.mHintText = mHintText;
        }

        public void show(final int textDirection, final int textAlignment) {
            final boolean showing = this.isShowing();
            this.computeContentWidth();
            this.setInputMethodMode(2);
            super.show();
            final ListView listView = this.getListView();
            listView.setChoiceMode(1);
            listView.setTextDirection(textDirection);
            listView.setTextAlignment(textAlignment);
            this.setSelection(AmigoSpinner.this.getSelectedItemPosition());
            this.changeDropDownListSelector(listView.getSelector());
            if (!showing) {
                final ViewTreeObserver viewTreeObserver = AmigoSpinner.this.getViewTreeObserver();
                if (viewTreeObserver != null) {
                    final ViewTreeObserver.OnGlobalLayoutListener viewTreeObserver$OnGlobalLayoutListener = (ViewTreeObserver.OnGlobalLayoutListener)new ViewTreeObserver.OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            if (!AmigoSpinner.access$300(AmigoSpinner.this)) {
                                DropdownPopup.this.dismiss();
                                return;
                            }
                            DropdownPopup.this.computeContentWidth();
                            DropdownPopup.this.show();
                        }
                    };
                    viewTreeObserver.addOnGlobalLayoutListener((ViewTreeObserver.OnGlobalLayoutListener)viewTreeObserver$OnGlobalLayoutListener);
                    this.setOnDismissListener((PopupWindow.OnDismissListener)new PopupWindow.OnDismissListener() {
                        public void onDismiss() {
                            final ViewTreeObserver viewTreeObserver = AmigoSpinner.this.getViewTreeObserver();
                            if (viewTreeObserver != null) {
                                viewTreeObserver.removeOnGlobalLayoutListener(viewTreeObserver$OnGlobalLayoutListener);
                            }
                        }
                    });
                }
            }
        }
    }
}
