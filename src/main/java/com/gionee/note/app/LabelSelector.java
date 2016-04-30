package com.gionee.note.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import com.gionee.aminote.R;
import com.gionee.framework.log.Logger;
import com.gionee.note.app.LabelManager.LabelHolder;
import java.util.ArrayList;
import java.util.Iterator;

public class LabelSelector implements OnClickListener {
    private static final String TAG = "LabelSelector";
    private Activity mActivity;
    private Dialog mDialog;
    private LayoutInflater mInflater;
    private LabelSelectorAdapter mLabelAdapter;
    private OnLabelChangedListener mLabelChangedListener;
    private ArrayList<LabelHolder> mLabels;
    private ArrayList<Integer> mSelectLabelIds = new ArrayList();
    private IYouJuCallback mYouJuCb;

    private class LabelSelectorAdapter extends BaseAdapter {
        private CheckListener mCheckListener = new CheckListener();

        private class CheckListener implements OnClickListener {
            private CheckListener() {
            }

            public void onClick(View v) {
                ViewHolder viewHolder = (ViewHolder) v.getTag();
                int id = viewHolder.labelId;
                if (viewHolder.checkBox.isChecked()) {
                    LabelSelector.this.mSelectLabelIds.remove(Integer.valueOf(id));
                } else {
                    LabelSelector.this.mSelectLabelIds.add(Integer.valueOf(id));
                }
                viewHolder.checkBox.toggle();
            }
        }

        public int getCount() {
            return LabelSelector.this.mLabels.size();
        }

        public String getItem(int i) {
            return ((LabelHolder) LabelSelector.this.mLabels.get(i)).mContent;
        }

        public long getItemId(int i) {
            return (long) ((LabelHolder) LabelSelector.this.mLabels.get(i)).mId;
        }

        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LabelSelector.this.mInflater.inflate(R.layout.label_selector_item, null);
                viewHolder = new ViewHolder();
                viewHolder.contentView = convertView;
                viewHolder.textView = (TextView) convertView.findViewById(R.id.label_select_list_item_text);
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.label_select_list_item_checkbox);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.textView.setText(getItem(i));
            viewHolder.contentView.setOnClickListener(this.mCheckListener);
            int labelId = (int) getItemId(i);
            viewHolder.labelId = labelId;
            viewHolder.checkBox.setChecked(LabelSelector.this.isSelected(labelId));
            return convertView;
        }
    }

    public interface OnLabelChangedListener {
        void onLabelChanged(ArrayList<Integer> arrayList);

        void onUpdate();
    }

    private static class ViewHolder {
        public CheckBox checkBox;
        public View contentView;
        public int labelId;
        public TextView textView;

        private ViewHolder() {
        }
    }

    public LabelSelector(Activity activity, OnLabelChangedListener listener) {
        this.mActivity = activity;
        this.mLabelChangedListener = listener;
        this.mInflater = LayoutInflater.from(activity);
    }

    public void setYouJuCb(IYouJuCallback cb) {
        this.mYouJuCb = cb;
    }

    public void updateLabelList(ArrayList<Integer> ids) {
        if (this.mLabels.size() == 0) {
            if (this.mDialog != null && this.mDialog.isShowing()) {
                this.mDialog.dismiss();
            }
        } else if (this.mDialog == null || !this.mDialog.isShowing()) {
            selectLabel(ids);
        } else {
            notifyDataSetChanged();
        }
    }

    public void setLabels(ArrayList<LabelHolder> labels) {
        this.mLabels = labels;
        if (labels.size() == 0) {
            this.mSelectLabelIds.clear();
            if (this.mDialog != null && this.mDialog.isShowing()) {
                this.mDialog.dismiss();
                return;
            }
            return;
        }
        removeInvalidLabel();
        notifyDataSetChanged();
    }

    public void selectLabel(ArrayList<Integer> ids) {
        this.mSelectLabelIds.clear();
        this.mSelectLabelIds.addAll(ids);
        if (this.mLabels.size() == 0) {
            this.mSelectLabelIds.clear();
            enterCustomLabel();
            return;
        }
        removeInvalidLabel();
        if (this.mDialog == null) {
            this.mDialog = createDialog();
        }
        if (!this.mDialog.isShowing()) {
            this.mDialog.show();
        }
        this.mLabelAdapter.notifyDataSetChanged();
    }

    private void notifyDataSetChanged() {
        if (this.mLabelAdapter != null) {
            this.mLabelAdapter.notifyDataSetChanged();
        }
    }

    private void removeInvalidLabel() {
        ArrayList<Integer> invalid = new ArrayList();
        ArrayList<LabelHolder> labels = this.mLabels;
        ArrayList<Integer> labelIds = this.mSelectLabelIds;
        Iterator i$ = labelIds.iterator();
        while (i$.hasNext()) {
            Integer id = (Integer) i$.next();
            if (!isValid(id.intValue(), labels)) {
                invalid.add(id);
            }
        }
        labelIds.removeAll(invalid);
    }

    private boolean isValid(int id, ArrayList<LabelHolder> labels) {
        Iterator i$ = labels.iterator();
        while (i$.hasNext()) {
            if (((LabelHolder) i$.next()).mId == id) {
                return true;
            }
        }
        return false;
    }

    private Dialog createDialog() {
        View content = LayoutInflater.from(this.mActivity).inflate(R.layout.label_selector_layout, null, false);
        ListView gridView = (ListView) content.findViewById(R.id.label_select_list);
        LabelSelectorAdapter labelAdapter = new LabelSelectorAdapter();
        this.mLabelAdapter = labelAdapter;
        gridView.setAdapter(labelAdapter);
        Dialog dialog = new Dialog(this.mActivity, R.style.DialogTheme);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(content);
        dialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                LabelSelector.this.mSelectLabelIds.clear();
                if (LabelSelector.this.mLabelChangedListener != null) {
                    LabelSelector.this.mLabelChangedListener.onUpdate();
                }
            }
        });
        Window window = dialog.getWindow();
        LayoutParams lp = window.getAttributes();
        lp.width = -1;
        lp.height = -2;
        window.setGravity(80);
        CheckBox checkBox = (CheckBox) content.findViewById(R.id.label_select_custom_label);
        Drawable tintIcon = DrawableCompat.wrap(ContextCompat.getDrawable(this.mActivity, R.drawable.label_selector_custom_label));
        DrawableCompat.setTintList(tintIcon, ContextCompat.getColorStateList(this.mActivity, R.color.action_bar_image_color));
        checkBox.setButtonDrawable(tintIcon);
        checkBox.setOnClickListener(this);
        content.findViewById(R.id.label_select_cancel).setOnClickListener(this);
        content.findViewById(R.id.label_select_sure).setOnClickListener(this);
        return dialog;
    }

    private void enterCustomLabel() {
        try {
            this.mActivity.startActivityForResult(new Intent(this.mActivity, LabelCustomActivity.class), 3);
        } catch (Exception e) {
            Logger.printLog(TAG, "enterCustomLabel fail : " + e.toString());
        }
    }

    private void dismissDialog() {
        if (this.mDialog != null) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.label_select_custom_label:
                enterCustomLabel();
                return;
            case R.id.label_select_cancel:
                dismissDialog();
                return;
            case R.id.label_select_sure:
                if (this.mLabelChangedListener != null) {
                    this.mLabelChangedListener.onLabelChanged(this.mSelectLabelIds);
                }
                Iterator i$ = this.mSelectLabelIds.iterator();
                while (i$.hasNext()) {
                    String labelName = getLabelContentById(((Integer) i$.next()).intValue());
                    if (labelName != null) {
                        youjuStatistics(R.string.youju_add_tag, labelName);
                    }
                }
                dismissDialog();
                return;
            default:
                return;
        }
    }

    private boolean isSelected(int labelId) {
        return this.mSelectLabelIds.contains(Integer.valueOf(labelId));
    }

    public String getLabelContentById(int id) {
        Iterator i$ = this.mLabels.iterator();
        while (i$.hasNext()) {
            LabelHolder holder = (LabelHolder) i$.next();
            if (holder.mId == id) {
                return holder.mContent;
            }
        }
        return null;
    }

    public boolean isLabelInvalid(ArrayList<Integer> ids) {
        if (ids == null || ids.size() == 0) {
            return true;
        }
        Iterator i$ = this.mLabels.iterator();
        while (i$.hasNext()) {
            if (ids.contains(Integer.valueOf(((LabelHolder) i$.next()).mId))) {
                return false;
            }
        }
        return true;
    }

    private void youjuStatistics(int stringId, String label) {
        if (this.mYouJuCb != null) {
            this.mYouJuCb.onLabelEvent(stringId, label);
        }
    }
}
