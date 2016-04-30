package com.gionee.note.app.attachment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import com.gionee.aminote.R;
import com.gionee.note.app.view.PicSelectorItemView;
import com.gionee.note.common.ThumbnailDecodeProcess.ThumbnailDecodeMode;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PicSelectorAdapter extends Adapter<PicSelectorAdapter.AttachPicViewHolder> implements OnClickListener {
    private static final int GO_TO_GALLERY = -1;
    private PicInfo[] mAttachPicInfos;
    private Activity mContext;
    private LayoutInflater mLayoutInflater;
    private LocalImageLoader mLocalImageLoader;
    private Drawable mPicSelectedDrawable;
    private Drawable mPicUnSelecteDrawable;
    private int mPreLoadPicWith;
    private int mPreloadPicHeight;
    private CopyOnWriteArrayList<String> mSelectedPicUris = new CopyOnWriteArrayList<>();
    private int mVisibleEnd;
    private int mVisibleStart;

    static class AttachPicViewHolder extends ViewHolder {
        ImageView pic;
        int postion;
        ImageView selectBox;

        public AttachPicViewHolder(View itemView) {
            super(itemView);
        }
    }

    public PicSelectorAdapter(Activity context, PicInfo[] attachInfos) {
        this.mContext = context;
        Resources resources = context.getResources();
        this.mAttachPicInfos = attachInfos;
        this.mLocalImageLoader = new LocalImageLoader(this.mContext);
        this.mLayoutInflater = LayoutInflater.from(this.mContext);
        this.mPreLoadPicWith = resources.getDimensionPixelOffset(R.dimen.attach_selector_pic_default_widht);
        this.mPreloadPicHeight = resources.getDimensionPixelOffset(R.dimen.attach_selector_pic_height);
        this.mPicSelectedDrawable = ResourcesCompat.getDrawable(resources, R.drawable.pic_selected, null);
        this.mPicUnSelecteDrawable = ResourcesCompat.getDrawable(resources, R.drawable.pic_unselected, null);
    }

    public AttachPicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = this.mLayoutInflater.inflate(R.layout.pic_selector_item, parent, false);
        PicSelectorItemView container = (PicSelectorItemView) item.findViewById(R.id.pic_selector_item_view);
        AttachPicViewHolder holder = new AttachPicViewHolder(item);
        container.setTag(holder);
        container.setOnClickListener(this);
        holder.pic = container.getImageView();
        holder.selectBox = container.getCheckBox();
        holder.selectBox.setOnClickListener(this);
        holder.selectBox.setTag(holder);
        return holder;
    }

    public void onBindViewHolder(AttachPicViewHolder holder, int position) {
        if (position == 0) {
            Drawable drawable = ResourcesCompat.getDrawable(this.mContext.getResources(), R.drawable.go_gallery, null);
            holder.pic.setImageDrawable(drawable);
            LayoutParams params = holder.pic.getLayoutParams();
            params.width = drawable.getIntrinsicWidth();
            holder.pic.setLayoutParams(params);
            holder.selectBox.setVisibility(8);
        } else {
            holder.selectBox.setVisibility(0);
            PicInfo info = this.mAttachPicInfos[position - 1];
            if (info != null) {
                String picUri = info.uri;
                if (!TextUtils.isEmpty(picUri)) {
                    this.mLocalImageLoader.loadImage(picUri, holder.pic, ThumbnailDecodeMode.HEIGHT_FIXED_WIDTH_SCALE);
                }
                if (info.isSelected) {
                    holder.selectBox.setImageDrawable(this.mPicSelectedDrawable);
                } else {
                    holder.selectBox.setImageDrawable(this.mPicUnSelecteDrawable);
                }
            } else {
                return;
            }
        }
        holder.postion = position - 1;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public int getItemCount() {
        return this.mAttachPicInfos.length + 1;
    }

    public void onClick(View v) {
        int position = ((AttachPicViewHolder) v.getTag()).postion;
        if (position == -1) {
            gotoPickPics();
            return;
        }
        PicInfo selectInfo = this.mAttachPicInfos[position];
        selectInfo.isSelected = !selectInfo.isSelected;
        if (!selectInfo.isSelected || this.mSelectedPicUris.contains(selectInfo.uri)) {
            this.mSelectedPicUris.remove(selectInfo.uri);
        } else {
            this.mSelectedPicUris.add(selectInfo.uri);
        }
        notifyDataSetChanged();
    }

    private void gotoPickPics() {
        Intent intent = new Intent("com.gionee.gallery.intent.action.GET_CONTENT");
        intent.setType("image/*");
        if (!isInstalledAPK(intent)) {
            Intent newIntent = new Intent("android.intent.action.GET_CONTENT");
            newIntent.setType("image/*");
            intent = Intent.createChooser(newIntent, null);
        }
        try {
            this.mContext.startActivityForResult(intent, 1);
        } catch (ActivityNotFoundException e) {
        }
    }

    public boolean isInstalledAPK(Intent intent) {
        List<ResolveInfo> resolveInfo = this.mContext.getPackageManager().queryIntentActivities(intent, 65536);
        if (resolveInfo == null || resolveInfo.size() <= 0) {
            return false;
        }
        return true;
    }

    public CopyOnWriteArrayList getSelectedPicUris() {
        return this.mSelectedPicUris;
    }

    public void clearSelectedPicUris() {
        this.mSelectedPicUris.clear();
    }

    public void preLoadPic(int preLoadDirection) {
        if (preLoadDirection == 0) {
            preLoadPre();
        } else if (preLoadDirection == 1) {
            preLoadNext();
        }
    }

    private void preLoadPre() {
        if (this.mVisibleStart > 0) {
            int preLoadStart = this.mVisibleStart;
            int i = 0;
            while (i < 5) {
                preLoadStart--;
                if (preLoadStart > 0) {
                    this.mLocalImageLoader.preLoadImage(this.mAttachPicInfos[preLoadStart].uri, this.mPreLoadPicWith, this.mPreloadPicHeight);
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    private void preLoadNext() {
        if (this.mVisibleEnd < this.mAttachPicInfos.length - 1) {
            int preLoadStart = this.mVisibleEnd;
            int i = 0;
            while (i < 5) {
                preLoadStart++;
                if (preLoadStart < this.mAttachPicInfos.length - 1) {
                    this.mLocalImageLoader.preLoadImage(this.mAttachPicInfos[preLoadStart].uri, this.mPreLoadPicWith, this.mPreloadPicHeight);
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    public void notifyVisibleRangeChanged(int visibleStart, int visibleEnd) {
        this.mVisibleStart = visibleStart;
        this.mVisibleEnd = visibleEnd;
    }
}
