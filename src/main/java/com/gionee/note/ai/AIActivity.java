package com.gionee.note.ai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.GridLayout.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import com.gionee.aminote.R;
import com.gionee.note.ai.AISearchView.OnQueryTextListener;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.app.view.StandardActivity;
import com.gionee.note.app.view.StandardActivity.StandardAListener;
import com.gionee.note.common.StatisticsModule;
import com.gionee.note.common.ThreadPool.Job;
import com.gionee.note.common.ThreadPool.JobContext;
import java.util.ArrayList;
import java.util.List;
import uk.co.senab.photoview.IPhotoView;

public class AIActivity extends StandardActivity implements StandardAListener {
    private static final int COLUMN_COUNT = 2;
    public static final String KEY_AMI_Recommend = "key_ami_recommend";
    private static final String URI_PREFIX = "http://m.haosou.com/s?q=";
    private static final String URI_SUFFIX = "&src=home&srcg=zl_jinli_1";
    private GridLayout mAmiRecommendGridLy;
    private View mAmiRecommendPanel;
    private GridLayout mFavorRecommendGridLy;
    private View mFavorRecommendPanel;
    private boolean mIsDestroy;
    private boolean mIsOpenNetwork;
    private BroadcastReceiver mNetworkReceiver;
    private AISearchView mSearchView;

    private class NetworkReceiver extends BroadcastReceiver {
        private NetworkReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected()) {
                    AIActivity.this.mIsOpenNetwork = false;
                } else {
                    AIActivity.this.mIsOpenNetwork = true;
                }
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle((int) R.string.ai_activity_title);
        setStandardAListener(this);
        setNoteContentView(R.layout.ai_activity_content_ly);
        setNoteRootViewBackgroundColor(ContextCompat.getColor(this, R.color.abstract_note_activity_root_bg_color));
        initNetworkEnv();
        initView();
        initData();
    }

    private void initNetworkEnv() {
        NetworkInfo networkInfo = ((ConnectivityManager) NoteAppImpl.getContext().getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            this.mIsOpenNetwork = true;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        BroadcastReceiver networkReceiver = new NetworkReceiver();
        this.mNetworkReceiver = networkReceiver;
        registerReceiver(networkReceiver, filter);
    }

    private void initView() {
        this.mAmiRecommendPanel = findViewById(R.id.ami_recommend_panel);
        this.mFavorRecommendPanel = findViewById(R.id.favor_recommend_panel);
        this.mAmiRecommendGridLy = (GridLayout) findViewById(R.id.ami_recommend_grid_view);
        this.mFavorRecommendGridLy = (GridLayout) findViewById(R.id.favor_recommend_grid_view);
        this.mSearchView = (AISearchView) findViewById(R.id.recommend_search_panel);
        this.mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
            public void onQueryText(String newText) {
                if (AIActivity.this.mIsOpenNetwork) {
                    StatisticsModule.onEvent(AIActivity.this, (int) R.string.youju_call_ai_seach);
                    AIActivity.this.startWebPageActivity(newText, newText);
                    return;
                }
                AIActivity.this.showNotNetworkTip();
            }
        });
    }

    private OnClickListener createItemOnClickListener() {
        return new OnClickListener() {
            public void onClick(View v) {
                if (AIActivity.this.mIsOpenNetwork) {
                    HotWord searchObj = v.getTag();
                    if (searchObj instanceof HotWord) {
                        StatisticsModule.onEvent(AIActivity.this, (int) R.string.youju_call_ai_keyword_recommand);
                        HotWord hotWord = searchObj;
                        AIActivity.this.startWebPageActivity(hotWord.getTitle(), hotWord.getSearchWord());
                        return;
                    } else if (searchObj instanceof String) {
                        StatisticsModule.onEvent(AIActivity.this, (int) R.string.youju_click_ai_ami_recommand);
                        AIActivity.this.startWebPageActivity((String) searchObj, (String) searchObj);
                        return;
                    } else {
                        return;
                    }
                }
                AIActivity.this.showNotNetworkTip();
            }
        };
    }

    private void initData() {
        final OnClickListener itemOnClickListener = createItemOnClickListener();
        final LayoutInflater inflater = LayoutInflater.from(this);
        ArrayList<String> keyWords = getIntent().getExtras().getStringArrayList(KEY_AMI_Recommend);
        final int itemHeight = getResources().getDimensionPixelSize(R.dimen.recommend_item_height);
        if (keyWords != null && keyWords.size() > 0) {
            this.mAmiRecommendPanel.setVisibility(0);
            fullAmiRecommendPanel(itemOnClickListener, inflater, keyWords, itemHeight);
        }
        final boolean isOpenNetwork = this.mIsOpenNetwork;
        NoteAppImpl.getContext().getThreadPool().submit(new Job<Object>() {
            public Object run(JobContext jc) {
                final List<HotWord> hotWords = new HotWordUtils().getHotWorks(AIActivity.this, isOpenNetwork);
                if (hotWords != null) {
                    AIActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            if (!AIActivity.this.mIsDestroy) {
                                AIActivity.this.mFavorRecommendPanel.setVisibility(0);
                                AIActivity.this.fullFavorRecommendPanel(hotWords, inflater, itemOnClickListener, itemHeight);
                            }
                        }
                    });
                }
                return null;
            }
        });
    }

    private void fullFavorRecommendPanel(List<HotWord> hotWords, LayoutInflater inflater, OnClickListener itemOnClickListener, int itemHeight) {
        int length = hotWords.size();
        for (int i = 0; i < length; i++) {
            TextView textView = (TextView) inflater.inflate(R.layout.recommand_grid_view_item_ly, null);
            HotWord hotWord = (HotWord) hotWords.get(i);
            textView.setText(hotWord.getTitle());
            textView.setTag(hotWord);
            textView.setOnClickListener(itemOnClickListener);
            LayoutParams lp = new LayoutParams(GridLayout.spec(i / 2, 1, (float) IPhotoView.DEFAULT_MIN_SCALE), GridLayout.spec(i % 2, 1, (float) IPhotoView.DEFAULT_MIN_SCALE));
            lp.height = itemHeight;
            lp.setGravity(119);
            this.mFavorRecommendGridLy.addView(textView, lp);
        }
    }

    private void fullAmiRecommendPanel(OnClickListener itemOnClickListener, LayoutInflater inflater, ArrayList<String> keyWords, int itemHeight) {
        int length = keyWords.size();
        for (int i = 0; i < length; i++) {
            TextView textView = (TextView) inflater.inflate(R.layout.recommand_grid_view_item_ly, null);
            String keyWord = (String) keyWords.get(i);
            textView.setText(keyWord);
            textView.setTag(keyWord);
            textView.setOnClickListener(itemOnClickListener);
            LayoutParams lp = new LayoutParams(GridLayout.spec(i / 2, 1, (float) IPhotoView.DEFAULT_MIN_SCALE), GridLayout.spec(i % 2, 1, (float) IPhotoView.DEFAULT_MIN_SCALE));
            lp.height = itemHeight;
            lp.setGravity(119);
            this.mAmiRecommendGridLy.addView(textView, lp);
        }
    }

    private void showNotNetworkTip() {
        Toast.makeText(this, R.string.ai_activity_not_network_tip, 0).show();
    }

    private void startWebPageActivity(String title, String searchWord) {
        Intent intent = new Intent();
        intent.setClass(this, WebPageActivity.class);
        intent.putExtra(WebPageActivity.DATA_TITLE, title);
        intent.putExtra(WebPageActivity.DATA_URL, URI_PREFIX + searchWord + URI_SUFFIX);
        startActivity(intent);
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
        unregisterReceiver(this.mNetworkReceiver);
        this.mIsDestroy = true;
    }

    public void onClickHomeBack() {
        finish();
    }

    public void onClickRightView() {
    }
}
