package amigoui.widget;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;

import java.util.HashSet;
import java.util.Set;

import amigoui.changecolors.ChameleonColorManager;

public abstract class AmigoMultiChoiceAdapterHelperBase implements OnItemLongClickListener, OnItemClickListener {
    private static final String BUNDLE_KEY = "mca_selection";
    protected static final String TAG = "AmigoMultiChoiceAdapterHelperBase";
    protected AdapterView<? super AmigoMultiChoiceBaseAdapter> mAdapterView;
    private Set<Long> mCheckedItems = new HashSet<>();
    private OnItemClickListener mItemClickListener;
    protected BaseAdapter mOwner;
    private AmigoButton mSelectAllBtn;
    private AmigoTextView mTitleView;

    protected abstract void clearActionMode();

    protected abstract void finishActionMode();

    protected abstract boolean isActionModeStarted();

    protected abstract void setActionModeTitle(String str);

    protected abstract void startActionMode(View view);

    protected abstract void updateActionMode();

    protected AmigoMultiChoiceAdapterHelperBase(BaseAdapter owner) {
        this.mOwner = owner;
    }

    public void restoreSelectionFromSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            long[] array = savedInstanceState.getLongArray(BUNDLE_KEY);
            this.mCheckedItems.clear();
            if (array != null) {
                for (long id : array) {
                    this.mCheckedItems.add(Long.valueOf(id));
                }
            }
        }
    }

    public void setAdapterView(AdapterView<? super BaseAdapter> adapterView) {
        this.mAdapterView = adapterView;
        checkActivity();
        adapterView.setOnItemLongClickListener(this);
        adapterView.setOnItemClickListener(this);
        adapterView.setAdapter(this.mOwner);
        if (!this.mCheckedItems.isEmpty()) {
            startActionMode(getActionModeCustomView());
            updateActionModeCustomView();
        }
    }

    private View getActionModeCustomView() {
        View view = LayoutInflater.from(getContext()).inflate(AmigoWidgetResource.getIdentifierByLayout(getContext(), "amigo_multichoice_select_action_mode_layout"), null);
        this.mTitleView = (AmigoTextView) view.findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_multichoice_selectedCount"));
        this.mSelectAllBtn = (AmigoButton) view.findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_multichoice_selectall"));
        if (ChameleonColorManager.isNeedChangeColor()) {
            this.mSelectAllBtn.setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
        }
        this.mSelectAllBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                AmigoMultiChoiceAdapterHelperBase.this.doClickSelectAllBtn();
            }
        });
        return view;
    }

    private void doClickSelectAllBtn() {
        int selCnt = getCheckedItemCount();
        int totalCnt = this.mOwner.getCount();
        int i;
        int correctedPosition;
        if (selCnt == getCheckableItemCount()) {
            for (i = 0; i < totalCnt; i++) {
                correctedPosition = correctPositionAccountingForHeader(this.mAdapterView, i);
                if (isChecked((long) correctedPosition)) {
                    this.mCheckedItems.remove(Long.valueOf((long) correctedPosition));
                }
            }
        } else {
            AmigoMultiChoiceAdapter adapter = (AmigoMultiChoiceAdapter) this.mOwner;
            for (i = 0; i < totalCnt; i++) {
                correctedPosition = correctPositionAccountingForHeader(this.mAdapterView, i);
                boolean wasSelected = isChecked((long) correctedPosition);
                boolean isCheckable = adapter.isItemCheckable(correctedPosition);
                if (!wasSelected && isCheckable) {
                    this.mCheckedItems.add(Long.valueOf((long) correctedPosition));
                }
            }
        }
        this.mOwner.notifyDataSetChanged();
        updateActionModeCustomView();
    }

    private void updateActionModeCustomView() {
        if (this.mTitleView != null && this.mSelectAllBtn != null) {
            int count = getCheckedItemCount();
            int checkableCnt = getCheckableItemCount();
            this.mTitleView.setText(getContext().getResources().getString(AmigoWidgetResource.getIdentifierByString(getContext(), "amigo_multichoice_select_text"), new Object[]{Integer.valueOf(count)}));
            if (count == checkableCnt) {
                this.mSelectAllBtn.setText(getContext().getResources().getString(AmigoWidgetResource.getIdentifierByString(getContext(), "amigo_multichoice_cancel_select_all")));
            } else {
                this.mSelectAllBtn.setText(getContext().getResources().getString(AmigoWidgetResource.getIdentifierByString(getContext(), "amigo_multichoice_select_all")));
            }
            updateActionMode();
        }
    }

    public void checkActivity() {
        if (getContext() instanceof ListActivity) {
            throw new RuntimeException("ListView cannot belong to an activity which subclasses ListActivity");
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void save(Bundle outState) {
        long[] array = new long[this.mCheckedItems.size()];
        int i = 0;
        for (Long id : this.mCheckedItems) {
            int i2 = i + 1;
            array[i] = id.longValue();
            i = i2;
        }
        outState.putLongArray(BUNDLE_KEY, array);
    }

    public void setItemChecked(long handle, boolean checked) {
        if (checked) {
            checkItem(handle);
        } else {
            uncheckItem(handle);
        }
    }

    public void checkItem(long handle) {
        if (!isChecked(handle)) {
            if (!isActionModeStarted()) {
                startActionMode(getActionModeCustomView());
            }
            this.mCheckedItems.add(Long.valueOf(handle));
            this.mOwner.notifyDataSetChanged();
            updateActionModeCustomView();
        }
    }

    public void uncheckItem(long handle) {
        if (isChecked(handle)) {
            this.mCheckedItems.remove(Long.valueOf(handle));
            this.mOwner.notifyDataSetChanged();
            updateActionModeCustomView();
        }
    }

    public Set<Long> getCheckedItems() {
        return new HashSet(this.mCheckedItems);
    }

    public int getCheckedItemCount() {
        return this.mCheckedItems.size();
    }

    public boolean isChecked(long handle) {
        return this.mCheckedItems.contains(Long.valueOf(handle));
    }

    public Context getContext() {
        return this.mAdapterView.getContext();
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        boolean z = false;
        //change
//        if (!this.mOwner.isItemCheckable(position)) {
//            return false;
//        }
        if (!((AmigoMultiChoiceBaseAdapter)this.mOwner).isItemCheckable(position)) {
            return false;
        }
        int correctedPosition = correctPositionAccountingForHeader(adapterView, position);
        long j = (long) correctedPosition;
        if (!isChecked((long) correctedPosition)) {
            z = true;
        }
        setItemChecked(j, z);
        return true;
    }

    private int correctPositionAccountingForHeader(AdapterView<?> adapterView, int position) {
        ListView listView = adapterView instanceof ListView ? (ListView) adapterView : null;
        if ((listView == null ? 0 : listView.getHeaderViewsCount()) > 0) {
            if (listView != null) {
                return position - listView.getHeaderViewsCount();
            }
        }
        return position;
    }

    public void onDestroyActionMode() {
        this.mCheckedItems.clear();
        clearActionMode();
        this.mOwner.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (isActionModeStarted()) {
            onItemLongClick(adapterView, view, position, id);
        } else if (this.mItemClickListener != null) {
            this.mItemClickListener.onItemClick(adapterView, view, position, id);
        }
    }

    public View getView(int position, View viewWithoutSelection) {
        AmigoCheckBox checkboxView = (AmigoCheckBox) ((ViewGroup) viewWithoutSelection).findViewById(android.R.id.checkbox);
        checkboxView.setChecked(isChecked((long) position));
        AmigoMultiChoiceAdapter adapter = (AmigoMultiChoiceAdapter) this.mOwner;
        if (isActionModeStarted() && adapter.isItemCheckable(position)) {
            checkboxView.setVisibility(View.VISIBLE);
        } else {
            checkboxView.setVisibility(View.GONE);
        }
        return viewWithoutSelection;
    }

    public void addMultichoiceView(View view) {
        View multichoiceView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(AmigoWidgetResource.getIdentifierByLayout(getContext(), "amigo_multichoice_checkbox"), (ViewGroup) view, false);
        LayoutParams rParams = new LayoutParams(-2, -2);
        rParams.addRule(11, -1);
        rParams.addRule(15, -1);
        ((ViewGroup) view).addView(multichoiceView, rParams);
    }

    public void enterMultiChoiceMode() {
        if (!isActionModeStarted()) {
            startActionMode(getActionModeCustomView());
        }
        this.mOwner.notifyDataSetChanged();
        updateActionModeCustomView();
    }

    private int getCheckableItemCount() {
        int cnt = 0;
        AmigoMultiChoiceAdapter adapter = (AmigoMultiChoiceAdapter) this.mOwner;
        int itemCnt = this.mOwner.getCount();
        for (int i = 0; i < itemCnt; i++) {
            if (adapter.isItemCheckable(correctPositionAccountingForHeader(this.mAdapterView, i))) {
                cnt++;
            }
        }
        return cnt;
    }
}
