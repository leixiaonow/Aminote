package com.gionee.note.app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.gionee.aminote.R;
import com.gionee.note.app.LabelManager.LabelDataChangeListener;
import com.gionee.note.app.LabelManager.LabelHolder;
import com.gionee.note.app.dialog.AmigoConfirmDialog;
import com.gionee.note.app.view.NoteLabelAddView;
import com.gionee.note.app.view.NoteLabelAddView.OnAddLabelListener;
import com.gionee.note.common.StatisticsModule;
import java.util.ArrayList;
import java.util.Iterator;

public class LabelCustomActivity extends AbstractNoteActivity implements OnClickListener {
    private static final boolean DEBUG = false;
    private static final int MESSAGE_ADD_LABEL = 0;
    private static final int MESSAGE_REMOVE_LABEL = 1;
    private static final int MESSAGE_UPDATE_LABEL = 2;
    private static final String TAG = "LabelCustomActivity";
    private LabelCustomAdapter mAdapter;
    private LayoutInflater mInflater;
    private LabelDataChangeListener mLabelDataChangeListener;
    private LabelManager mLabelManager;
    private ArrayList<LabelHolder> mLabels;
    private ListView mListView;
    private Handler mMainHandler;
    private NoteLabelAddView mNoteLabelAddView;
    private Handler mWorkHandler;
    private HandlerThread mWorkThread;

    private class LabelCustomAdapter extends BaseAdapter {
        private LabelCustomAdapter() {
        }

        public int getCount() {
            return LabelCustomActivity.this.mLabels.size();
        }

        public String getItem(int i) {
            return ((LabelHolder) LabelCustomActivity.this.mLabels.get(i)).mContent;
        }

        public long getItemId(int i) {
            return (long) ((LabelHolder) LabelCustomActivity.this.mLabels.get(i)).mId;
        }

        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LabelCustomActivity.this.mInflater.inflate(R.layout.label_custom_item, null, false);
                holder.textView = (TextView) convertView.findViewById(R.id.label_custom_list_item_text);
                holder.deleteButton = (ImageView) convertView.findViewById(R.id.label_custom_list_item_delete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textView.setText(getItem(i));
            holder.deleteButton.setTag(Integer.valueOf((int) getItemId(i)));
            holder.deleteButton.setOnClickListener(LabelCustomActivity.this);
            return convertView;
        }
    }

    static class ViewHolder {
        ImageView deleteButton;
        TextView textView;

        ViewHolder() {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNoteTitleView(R.layout.label_custom_title_layout);
        setNoteContentView(R.layout.label_custom_content_layout);
        findViewById(R.id.action_bar_label_custom_back).setOnClickListener(this);
        initHandler();
        initData();
        initView();
        tintImageViewDrawable(R.id.label_custom_edit_button, R.drawable.label_custom_add_sure, R.color.label_custom_edit_sure_button);
        setResult();
    }

    protected void onResume() {
        super.onResume();
        StatisticsModule.onResume(this);
    }

    protected void onPause() {
        super.onPause();
        StatisticsModule.onPause(this);
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mLabelManager.removeLabelDataChangeListener(this.mLabelDataChangeListener);
        this.mWorkHandler.removeCallbacksAndMessages(null);
        this.mWorkHandler = null;
        this.mWorkThread.quit();
        this.mWorkThread = null;
        this.mMainHandler.removeCallbacksAndMessages(null);
        this.mMainHandler = null;
    }

    private void initData() {
        LabelManager labelManager = ((NoteAppImpl) getApplication()).getLabelManager();
        this.mLabelDataChangeListener = new LabelDataChangeListener() {
            public void onDataChange() {
                if (LabelCustomActivity.this.mMainHandler != null) {
                    LabelCustomActivity.this.mMainHandler.sendEmptyMessage(2);
                }
            }
        };
        labelManager.addLabelDataChangeListener(this.mLabelDataChangeListener);
        this.mLabelManager = labelManager;
        this.mLabels = labelManager.getLabelList();
        this.mInflater = LayoutInflater.from(getApplicationContext());
        this.mAdapter = new LabelCustomAdapter();
    }

    private void initHandler() {
        HandlerThread workThread = new HandlerThread("label_custom_work_thread");
        workThread.start();
        this.mWorkThread = workThread;
        this.mWorkHandler = new Handler(workThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        LabelCustomActivity.this.mLabelManager.addLabel(msg.obj);
                        LabelCustomActivity.this.youjuStatistics(R.string.youju_add_cumtom_tag);
                        return;
                    case 1:
                        LabelCustomActivity.this.mLabelManager.removeLabelById(((Integer) msg.obj).intValue());
                        LabelCustomActivity.this.youjuStatistics(R.string.youju_del_tag);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mMainHandler = new Handler(getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 2:
                        LabelCustomActivity.this.mLabels = LabelCustomActivity.this.mLabelManager.getLabelList();
                        LabelCustomActivity.this.mAdapter.notifyDataSetChanged();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private void initView() {
        this.mListView = (ListView) findViewById(R.id.label_custom_list);
        this.mListView.setAdapter(this.mAdapter);
        this.mNoteLabelAddView = (NoteLabelAddView) findViewById(R.id.note_label_add_view);
        this.mNoteLabelAddView.setOnAddLabelListener(new OnAddLabelListener() {
            public void onAddLabel(String newLabelName) {
                LabelCustomActivity.this.saveCustomLabel(newLabelName);
            }
        });
    }

    private void tintImageViewDrawable(int imageViewId, int iconId, int colorsId) {
        Drawable tintIcon = DrawableCompat.wrap(ContextCompat.getDrawable(this, iconId));
        DrawableCompat.setTintList(tintIcon, ContextCompat.getColorStateList(this, colorsId));
        ((ImageView) findViewById(imageViewId)).setImageDrawable(tintIcon);
    }

    private void setResult() {
        setResult(-1, null);
    }

    private void saveCustomLabel(String labelName) {
        if (containLabel(labelName)) {
            Toast.makeText(this, R.string.label_custom_exist, 0).show();
        } else if (this.mWorkHandler != null) {
            this.mWorkHandler.sendMessage(this.mWorkHandler.obtainMessage(0, labelName));
        }
    }

    private boolean containLabel(String content) {
        Iterator i$ = this.mLabels.iterator();
        while (i$.hasNext()) {
            if (TextUtils.equals(((LabelHolder) i$.next()).mContent, content)) {
                return true;
            }
        }
        return false;
    }

    private void deleteLabel(View view) {
        final int id = ((Integer) view.getTag()).intValue();
        AmigoConfirmDialog dialog = new AmigoConfirmDialog(this);
        dialog.setTitle((int) R.string.button_delete);
        dialog.setMessage((int) R.string.label_custom_delete_attention);
        dialog.setOnClickListener(new AmigoConfirmDialog.OnClickListener() {
            public void onClick(int which) {
                if (which == -1 && LabelCustomActivity.this.mWorkHandler != null) {
                    LabelCustomActivity.this.mWorkHandler.sendMessage(LabelCustomActivity.this.mWorkHandler.obtainMessage(1, Integer.valueOf(id)));
                }
            }
        });
        dialog.show();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.label_custom_list_item_delete:
                deleteLabel(view);
                return;
            case R.id.action_bar_label_custom_back:
                this.mNoteLabelAddView.clearFocus();
                finish();
                return;
            default:
                return;
        }
    }

    private void youjuStatistics(int stringId) {
        StatisticsModule.onEvent((Context) this, getResources().getString(stringId));
    }
}
