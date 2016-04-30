package com.gionee.note.app;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.gionee.aminote.R;
import com.gionee.note.app.LabelManager.LabelHolder;
import java.util.ArrayList;

public class SearchLabelAdapt extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<LabelHolder> mLabels;

    public SearchLabelAdapt(Activity activity) {
        this.mInflater = LayoutInflater.from(activity);
    }

    public void setLabels(ArrayList<LabelHolder> lables) {
        this.mLabels = lables;
    }

    public int getCount() {
        return this.mLabels.size();
    }

    public String getItem(int i) {
        return ((LabelHolder) this.mLabels.get(i)).mContent;
    }

    public long getItemId(int i) {
        return (long) ((LabelHolder) this.mLabels.get(i)).mId;
    }

    public View getView(int i, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.search_label_item, null);
        }
        if (convertView instanceof TextView) {
            ((TextView) convertView).setText(getItem(i));
        }
        convertView.setTag(Integer.valueOf((int) getItemId(i)));
        return convertView;
    }
}
