package com.gionee.note.feedback;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.gionee.aminote.R;
import com.gionee.feedback.utils.BitmapUtils;
import com.gionee.note.app.dialog.AmigoConfirmDialog;
import com.gionee.note.app.dialog.AmigoConfirmDialog.OnClickListener;
import com.gionee.note.app.view.StandardActivity;
import com.gionee.note.app.view.StandardActivity.StandardAListener;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;

public class AttachmentManagerActivity extends StandardActivity implements StandardAListener {
    private static final int MSG_DECODE_BITMAP_FINISH = 1;
    public static final String SHOW_ATTACH_ITEM_INDEX = "show_attach_item_index";
    public static final String SHOW_ATTACH_PATHS = "show_attach_paths";
    private static final String TAG = "AttachmentManager";
    private MyAdapter mAdapter;
    private boolean mIsLoadFinish;
    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        public void onPageScrolled(int i, float v, int i1) {
        }

        public void onPageSelected(int i) {
            AttachmentManagerActivity.this.updateTitle();
        }

        public void onPageScrollStateChanged(int i) {
        }
    };
    private ArrayList<String> mPaths;
    private ViewPager mViewPager;

    private static class MyAdapter extends PagerAdapter {
        private ArrayList<View> views;

        private MyAdapter() {
            this.views = new ArrayList();
        }

        public int getItemPosition(Object object) {
            int index = this.views.indexOf(object);
            if (index == -1) {
                return -2;
            }
            return index;
        }

        public Object instantiateItem(ViewGroup container, int position) {
            View v = (View) this.views.get(position);
            container.addView(v);
            return v;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) this.views.get(position));
        }

        public int getCount() {
            return this.views.size();
        }

        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public int addView(View v) {
            return addView(v, this.views.size());
        }

        public int addView(View v, int position) {
            this.views.add(position, v);
            notifyDataSetChanged();
            return position;
        }

        public int removeView(ViewPager pager, View v) {
            return removeView(pager, this.views.indexOf(v));
        }

        public int removeView(ViewPager pager, int position) {
            pager.setAdapter(null);
            this.views.remove(position);
            pager.setAdapter(this);
            return position;
        }

        public View getView(int position) {
            return (View) this.views.get(position);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    private void initData() {
        final ArrayList<String> paths = getIntent().getStringArrayListExtra(SHOW_ATTACH_PATHS);
        this.mPaths = paths;
        final int item = getIntent().getIntExtra(SHOW_ATTACH_ITEM_INDEX, 0);
        final Handler mainHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    Iterator i$ = msg.obj.iterator();
                    while (i$.hasNext()) {
                        AttachmentManagerActivity.this.addView((Bitmap) i$.next());
                    }
                    AttachmentManagerActivity.this.mIsLoadFinish = true;
                    AttachmentManagerActivity.this.mViewPager.setCurrentItem(item, false);
                    AttachmentManagerActivity.this.updateTitle();
                    AttachmentManagerActivity.this.setResult(AttachmentManagerActivity.this.mPaths);
                    if (AttachmentManagerActivity.this.mPaths.size() == 0) {
                        AttachmentManagerActivity.this.finish();
                    }
                }
            }
        };
        new Thread() {
            public void run() {
                ArrayList<String> attachPaths = new ArrayList();
                ArrayList<Bitmap> attachBitmaps = new ArrayList();
                Iterator i$ = paths.iterator();
                while (i$.hasNext()) {
                    String uri = (String) i$.next();
                    Bitmap bitmap = AttachmentManagerActivity.this.getBitmap(uri);
                    if (bitmap != null) {
                        attachPaths.add(uri);
                        attachBitmaps.add(bitmap);
                    }
                }
                AttachmentManagerActivity.this.mPaths = attachPaths;
                mainHandler.sendMessage(mainHandler.obtainMessage(1, attachBitmaps));
            }
        }.start();
        setResult(this.mPaths);
    }

    private void initView() {
        setTitleAndRightImageView(R.id.extender_standard_title_rigth_id);
        setStandardAListener(this);
        setNoteContentView(R.layout.fb_attach_manager_content_ly);
        setRootViewBackground();
        this.mViewPager = (ViewPager) findViewById(R.id.gn_fb_id_deleteViewPager);
        this.mViewPager.addOnPageChangeListener(this.mOnPageChangeListener);
        this.mAdapter = new MyAdapter();
        this.mViewPager.setAdapter(this.mAdapter);
    }

    private void setResult(ArrayList<String> paths) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(SHOW_ATTACH_PATHS, paths);
        setResult(-1, intent);
    }

    private void showConfirmDialog() {
        AmigoConfirmDialog confirmDialog = new AmigoConfirmDialog(this);
        confirmDialog.setMessage((int) R.string.fb_del_attach_dialog_message);
        confirmDialog.setTitle((int) R.string.fb_del_attach_dialog_title);
        confirmDialog.setOnClickListener(new OnClickListener() {
            public void onClick(int which) {
                if (which == -1) {
                    AttachmentManagerActivity.this.removeView();
                    if (AttachmentManagerActivity.this.mAdapter.getCount() == 0) {
                        AttachmentManagerActivity.this.finish();
                    }
                }
            }
        });
        confirmDialog.show();
    }

    private Bitmap getBitmap(String path) {
        try {
            return BitmapUtils.decodeSampledBitmapFromUri(this, Uri.parse(path));
        } catch (FileNotFoundException e) {
            Log.w(TAG, "error", e);
            return null;
        }
    }

    private void addView(Bitmap bitmap) {
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(bitmap);
        this.mAdapter.addView(iv);
    }

    private void setRootViewBackground() {
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.photo_background));
    }

    private void removeView() {
        this.mPaths.remove(this.mViewPager.getCurrentItem());
        int pageIndex = this.mAdapter.removeView(this.mViewPager, this.mAdapter.getView(this.mViewPager.getCurrentItem()));
        if (pageIndex == this.mAdapter.getCount()) {
            pageIndex--;
        }
        this.mViewPager.setCurrentItem(pageIndex, true);
        updateTitle();
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mIsLoadFinish) {
            return super.dispatchTouchEvent(ev);
        }
        return true;
    }

    private void updateTitle() {
        ((TextView) findViewById(R.id.freya_title_title)).setText((this.mViewPager.getCurrentItem() + 1) + "/" + this.mAdapter.getCount());
    }

    public void onClickHomeBack() {
        finish();
    }

    public void onClickRightView() {
        showConfirmDialog();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mViewPager.removeOnPageChangeListener(this.mOnPageChangeListener);
    }
}
