package com.gionee.note.app;

import amigoui.widget.AmigoCheckBox;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.gionee.aminote.R;
import com.gionee.note.app.SlidingWindow.Listener;
import com.gionee.note.app.SlidingWindow.NoteEntry;
import com.gionee.note.app.attachment.LocalImageLoader;
import com.gionee.note.app.effect.EffectUtil;
import com.gionee.note.app.view.NoteCardBottomView;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.common.ThumbnailDecodeProcess.ThumbnailDecodeMode;
import com.gionee.note.data.NoteSet;
import com.gionee.note.data.Path;

public class RecyclerViewAdapter extends Adapter<NoteViewHolder> implements OnClickListener, OnLongClickListener {
    private static final int MESSAGE_CONTENT_CHANGE = 1;
    private int mCount = 0;
    private int[] mCurDate;
    private SlidingWindow mDataWindow;
    private EffectUtil mEffectUtil;
    private LocalImageLoader mImageLoad = new LocalImageLoader(NoteAppImpl.getContext());
    private LayoutInflater mLayoutInflater;
    private Handler mMainHandler;
    private NoteSelectionManager mNoteSelectionManager;
    private OnTouchListener mOnTouchListener;

    public interface OnTouchListener {
        void onLongClickTouch(Path path);

        void onSingleClickTouch(Path path);
    }

    public class MyDataModelListener implements Listener {
        public void onContentChanged() {
            RecyclerViewAdapter.this.mMainHandler.sendEmptyMessage(1);
        }

        public void onCountChanged(int count) {
            RecyclerViewAdapter.this.mCount = count;
            RecyclerViewAdapter.this.notifyDataSetChanged();
        }
    }

    public RecyclerViewAdapter(Activity activity, NoteSet set, LoadingListener loadingListener, NoteSelectionManager noteSelectionManager) {
        this.mImageLoad.setLoadingImage((int) R.drawable.note_card_default_image);
        this.mEffectUtil = new EffectUtil(System.currentTimeMillis());
        this.mNoteSelectionManager = noteSelectionManager;
        this.mLayoutInflater = LayoutInflater.from(activity);
        this.mDataWindow = new SlidingWindow(activity, set, loadingListener);
        this.mDataWindow.setListener(new MyDataModelListener());
        this.mMainHandler = new Handler(activity.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        RecyclerViewAdapter.this.notifyDataSetChanged();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public NoteViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View noteItem = this.mLayoutInflater.inflate(viewType == -1 ? R.layout.note_item_no_image : R.layout.note_item_have_image, viewGroup, false);
        NoteViewHolder holder = new NoteViewHolder(noteItem);
        View container = noteItem.findViewById(R.id.note_item_content_onclick_view);
        container.setTag(holder);
        container.setOnClickListener(this);
        container.setOnLongClickListener(this);
        holder.mTitle = (TextView) noteItem.findViewById(R.id.note_item_title);
        holder.mContent = (TextView) noteItem.findViewById(R.id.note_item_content);
        holder.mTime = (TextView) noteItem.findViewById(R.id.note_item_time);
        holder.mReminder = (ImageView) noteItem.findViewById(R.id.note_item_reminder);
        holder.mCheckBox = (AmigoCheckBox) noteItem.findViewById(R.id.note_item_checkbox);
        holder.mImage = viewType == -1 ? null : (ImageView) noteItem.findViewById(R.id.note_item_image);
        holder.mNoteCardBottomView = (NoteCardBottomView) noteItem.findViewById(R.id.note_item_card_bottom_view);
        return holder;
    }

    public void onBindViewHolder(NoteViewHolder noteViewHolder, int position) {
        NoteEntry noteEntry = this.mDataWindow.get(position);
        if (noteEntry != null && noteEntry.item != null) {
            ImageView imageView = noteViewHolder.mImage;
            if (imageView != null) {
                this.mImageLoad.loadImage(noteEntry, imageView, ThumbnailDecodeMode.CUT_WIDTH_AND_HEIGHT);
            }
            Path path = noteEntry.path;
            noteViewHolder.mPath = path;
            updateTitleState(noteViewHolder.mTitle, noteEntry.title);
            noteViewHolder.mContent.setText(noteEntry.content);
            noteViewHolder.mTime.setText(noteEntry.time);
            noteViewHolder.mNoteCardBottomView.setCardBg(this.mEffectUtil.getEffect(noteEntry.timeMillis));
            updateReminderState(noteViewHolder.mReminder, noteEntry.reminder);
            if (this.mNoteSelectionManager != null) {
                updateCheckBoxState(this.mNoteSelectionManager, noteViewHolder.mCheckBox, path);
            }
        }
    }

    private void updateTitleState(TextView titleView, String title) {
        titleView.setText(title);
        if (TextUtils.isEmpty(title)) {
            titleView.setVisibility(8);
        } else {
            titleView.setVisibility(0);
        }
    }

    private void updateReminderState(ImageView reminderView, long reminderTime) {
        if (reminderTime == 0) {
            reminderView.setVisibility(4);
        } else {
            reminderView.setVisibility(0);
        }
    }

    private void updateCheckBoxState(NoteSelectionManager noteSelectionManager, CheckBox checkBox, Path path) {
        if (noteSelectionManager.inSelectionMode()) {
            checkBox.setVisibility(0);
            if (noteSelectionManager.isItemSelected(path)) {
                checkBox.setChecked(true);
                return;
            } else {
                checkBox.setChecked(false);
                return;
            }
        }
        checkBox.setVisibility(4);
    }

    public int getItemCount() {
        return this.mCount;
    }

    public int getItemViewType(int position) {
        NoteEntry noteEntry = this.mDataWindow.get(position);
        if (noteEntry == null) {
            return -1;
        }
        return noteEntry.mediaType;
    }

    public void resume() {
        checkTimeChange();
        this.mDataWindow.resume();
    }

    private void checkTimeChange() {
        if (this.mCurDate == null) {
            this.mCurDate = NoteUtils.getToady();
            return;
        }
        int[] newCurDate = NoteUtils.getToady();
        if (!NoteUtils.isSomeDay(newCurDate, this.mCurDate)) {
            this.mCurDate = newCurDate;
            this.mEffectUtil = new EffectUtil(System.currentTimeMillis());
            notifyDataSetChanged();
        }
    }

    public void pause() {
        this.mDataWindow.pause();
    }

    public void destroy() {
        this.mDataWindow.destroy();
    }

    public boolean isEmpty() {
        return this.mCount == 0;
    }

    public void notifyVisibleRangeChanged(int visibleStart, int visibleEnd) {
        this.mDataWindow.setActiveWindow(visibleStart, visibleEnd);
    }

    public void onClick(View view) {
        if (this.mOnTouchListener != null) {
            this.mOnTouchListener.onSingleClickTouch(((NoteViewHolder) view.getTag()).mPath);
        }
    }

    public boolean onLongClick(View v) {
        if (this.mOnTouchListener != null) {
            this.mOnTouchListener.onLongClickTouch(((NoteViewHolder) v.getTag()).mPath);
        }
        return true;
    }

    public void setOnTouchListener(OnTouchListener listener) {
        this.mOnTouchListener = listener;
    }
}
