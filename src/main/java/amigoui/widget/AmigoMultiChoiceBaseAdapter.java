package amigoui.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import java.util.Set;

public abstract class AmigoMultiChoiceBaseAdapter extends BaseAdapter implements Callback, AmigoMultiChoiceAdapter {
    private AmigoMultiChoiceAdapterHelper helper = new AmigoMultiChoiceAdapterHelper(this);

    protected abstract View getViewImpl(int i, View view, ViewGroup viewGroup);

    public AmigoMultiChoiceBaseAdapter(Bundle savedInstanceState) {
        this.helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public void setAdapterView(AdapterView<? super BaseAdapter> adapterView) {
        this.helper.setAdapterView(adapterView);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.helper.setOnItemClickListener(listener);
    }

    public void save(Bundle outState) {
        this.helper.save(outState);
    }

    public void setItemChecked(long position, boolean checked) {
        this.helper.setItemChecked(position, checked);
    }

    public Set<Long> getCheckedItems() {
        return this.helper.getCheckedItems();
    }

    public int getCheckedItemCount() {
        return this.helper.getCheckedItemCount();
    }

    public boolean isChecked(long position) {
        return this.helper.isChecked(position);
    }

    protected void finishActionMode() {
        this.helper.finishActionMode();
    }

    protected Context getContext() {
        return this.helper.getContext();
    }

    public void onDestroyActionMode(ActionMode mode) {
        this.helper.onDestroyActionMode();
    }

    public boolean isItemCheckable(int position) {
        return true;
    }

    public final View getView(int position, View convertView, ViewGroup parent) {
        View viewWithoutSelection = getViewImpl(position, convertView, parent);
        if (convertView == null) {
            this.helper.addMultichoiceView(viewWithoutSelection);
        }
        return this.helper.getView(position, viewWithoutSelection);
    }

    public void enterMultiChoiceMode() {
        this.helper.enterMultiChoiceMode();
    }
}
