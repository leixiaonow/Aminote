package com.gionee.feedback.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.gionee.feedback.db.ProviderFactory;
import com.gionee.feedback.logic.vo.DraftInfo;
import com.gionee.feedback.utils.BitmapUtils;
import com.gionee.feedback.utils.Log;
import com.gionee.res.Id;
import com.gionee.res.Layout;
import com.gionee.res.Text;
import com.gionee.res.Widget;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class DeleteAttachActivity extends Activity {
    public static final String SHOW_ITEM = "show_item";
    private static final String TAG = "TAGDeleteAttachActivity";
    private ActionBar mActionBar;
    private MyAdapter mAdapter;
    private Context mContext;
    private DraftInfo mDraftInfo;
    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        public void onPageScrolled(int i, float v, int i1) {
        }

        public void onPageSelected(int i) {
            DeleteAttachActivity.this.updateActionBar();
        }

        public void onPageScrollStateChanged(int i) {
        }
    };
    private List<String> mPaths;
    private ViewPager mViewPager;

    private class MyAdapter extends PagerAdapter {
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
        this.mContext = this;
        initView();
        initActionBar();
        init();
    }

    private void init() {
        this.mDraftInfo = (DraftInfo) ProviderFactory.draftProvider(this).queryHead();
        this.mPaths = this.mDraftInfo.getAttachTextArray();
        Log.d(TAG, "mPaths = " + this.mPaths);
        if (!(this.mPaths == null || this.mPaths.isEmpty())) {
            for (String path : this.mPaths) {
                addView(path);
            }
        }
        this.mViewPager.setCurrentItem(getIntent().getIntExtra(SHOW_ITEM, 0));
        updateActionBar();
    }

    private void initActionBar() {
        this.mActionBar = getActionBar();
        this.mActionBar.setDisplayHomeAsUpEnabled(true);
        this.mActionBar.setDisplayShowTitleEnabled(true);
        this.mActionBar.setDisplayShowHomeEnabled(false);
    }

    private void initView() {
        setContentView(Layout.gn_fb_layout_deleteattach.getIdentifier(this));
        this.mViewPager = (ViewPager) getView(Widget.gn_fb_id_deleteViewPager.getIdentifier(this));
        this.mViewPager.setOnPageChangeListener(this.mOnPageChangeListener);
        this.mAdapter = new MyAdapter();
        this.mViewPager.setAdapter(this.mAdapter);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.gionee.res.Menu.gn_fb_menu_attach_choice.getIdentifier(this.mContext), menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == Id.gn_fb_menu_attach_choice_delete.getIdentifier(this.mContext)) {
            deleteAttachImageDialog();
        } else if (id == 16908332) {
            super.onBackPressed();
        }
        return true;
    }

    private void deleteAttachImageDialog() {
        new Builder(this).setTitle(Text.gn_fb_string_dialog_delete_title.getIdentifier(this.mContext)).setMessage(Text.gn_fb_string_dialog_delete_message.getIdentifier(this.mContext)).setNegativeButton(17039360, null).setPositiveButton(17039370, handOkDelete()).create().show();
    }

    private void deleteAttachImage() {
        this.mPaths.remove(this.mViewPager.getCurrentItem());
        this.mDraftInfo.setAttachTextArray(this.mPaths);
        Log.d(TAG, "deleteAttachImage " + this.mPaths);
        ProviderFactory.draftProvider(this.mContext).update(this.mDraftInfo);
        removeView(getCurrentPage());
        if (this.mAdapter.getCount() == 0) {
            finish();
        }
    }

    private void addView(String path) {
        ImageView iv = new ImageView(this.mContext);
        iv.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (DeleteAttachActivity.this.mActionBar.isShowing()) {
                    DeleteAttachActivity.this.mActionBar.hide();
                } else {
                    DeleteAttachActivity.this.mActionBar.show();
                }
                DeleteAttachActivity.this.invalidateOptionsMenu();
            }
        });
        try {
            iv.setImageBitmap(BitmapUtils.decodeSampledBitmapFromUri(this.mContext, Uri.parse(path)));
            this.mViewPager.setCurrentItem(this.mAdapter.addView(iv), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e2) {
            Log.d(TAG, e2.getMessage());
        }
    }

    private void removeView(View defunctPage) {
        int pageIndex = this.mAdapter.removeView(this.mViewPager, defunctPage);
        if (pageIndex == this.mAdapter.getCount()) {
            pageIndex--;
        }
        this.mViewPager.setCurrentItem(pageIndex, true);
        updateActionBar();
    }

    private View getCurrentPage() {
        return this.mAdapter.getView(this.mViewPager.getCurrentItem());
    }

    private <T extends View> T getView(int id) {
        return findViewById(id);
    }

    private DialogInterface.OnClickListener handOkDelete() {
        return new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DeleteAttachActivity.this.deleteAttachImage();
            }
        };
    }

    private void updateActionBar() {
        this.mActionBar.setTitle((this.mViewPager.getCurrentItem() + 1) + "/" + this.mAdapter.getCount());
    }
}
