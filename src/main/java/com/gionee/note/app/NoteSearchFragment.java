package com.gionee.note.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.gionee.aminote.R;
import com.gionee.framework.log.Logger;
import com.gionee.note.app.LabelManager.LabelDataChangeListener;
import com.gionee.note.app.RecyclerViewAdapter.OnTouchListener;
import com.gionee.note.app.view.NoteSearchView.OnQueryTextListener;
import com.gionee.note.data.KeyNoteSet;
import com.gionee.note.data.KeySource;
import com.gionee.note.data.Path;

public class NoteSearchFragment extends Fragment implements LoadingListener, OnItemClickListener, OnTouchListener {
    private static final boolean DEBUG = false;
    private static final int MESSAGE_UPDATE_LABEL = 1;
    private static final String TAG = "NoteSearchFragment";
    private SearchLabelAdapt mLabelAdapt;
    private LabelDataChangeListener mLabelDataChangeListener;
    private ListView mLabelList;
    private LabelManager mLabelManager;
    private Handler mMainHandler;
    private KeyNoteSet mNoteSet;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private View mRootView;
    private boolean mSearchContentIsEmpty = true;
    private View mSearchLabel;
    private TextView mTipTextView;
    private View mTipView;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        ((NoteMainActivity) getActivity()).beginNoteSearch(new OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                boolean isEmpty = TextUtils.isEmpty(newText);
                NoteSearchFragment.this.mSearchContentIsEmpty = isEmpty;
                if (!isEmpty) {
                    NoteSearchFragment.this.setSearchLabelVisibility(8);
                } else if (NoteSearchFragment.this.mLabelAdapt != null && NoteSearchFragment.this.mLabelAdapt.getCount() > 0) {
                    NoteSearchFragment.this.setSearchLabelVisibility(0);
                }
                NoteSearchFragment.this.mNoteSet.setKey(newText);
                return false;
            }
        });
    }

    private void setSearchLabelVisibility(int visibility) {
        if (this.mSearchLabel != null) {
            this.mSearchLabel.setVisibility(visibility);
        }
    }

    private void initData() {
        NoteAppImpl app = (NoteAppImpl) getActivity().getApplication();
        KeyNoteSet noteSet = (KeyNoteSet) app.getDataManager().getMediaSet(KeySource.KEY_SET_PATH);
        this.mNoteSet = noteSet;
        this.mRecyclerViewAdapter = new RecyclerViewAdapter(getActivity(), noteSet, this, null);
        this.mRecyclerViewAdapter.setOnTouchListener(this);
        this.mMainHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    NoteSearchFragment.this.mLabelAdapt.setLabels(NoteSearchFragment.this.mLabelManager.getLabelList());
                    NoteSearchFragment.this.mLabelAdapt.notifyDataSetChanged();
                }
            }
        };
        LabelManager labelManager = app.getLabelManager();
        this.mLabelDataChangeListener = new LabelDataChangeListener() {
            public void onDataChange() {
                NoteSearchFragment.this.mMainHandler.sendEmptyMessage(1);
            }
        };
        labelManager.addLabelDataChangeListener(this.mLabelDataChangeListener);
        this.mLabelManager = labelManager;
        this.mLabelAdapt = new SearchLabelAdapt(getActivity());
        this.mLabelAdapt.setLabels(labelManager.getLabelList());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = this.mRootView;
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.note_search_fragment_layout, container, false);
            this.mRootView = rootView;
            this.mRecyclerView = (RecyclerView) rootView.findViewById(R.id.search_recycler_view);
            this.mRecyclerView.setLayoutManager(getLayoutManager());
            this.mRecyclerView.setAdapter(this.mRecyclerViewAdapter);
            this.mTipView = rootView.findViewById(R.id.note_tip_view);
            this.mTipTextView = (TextView) rootView.findViewById(R.id.note_tip_text_view);
            this.mSearchLabel = rootView.findViewById(R.id.search_label);
            this.mLabelList = (ListView) rootView.findViewById(R.id.search_label_list);
            this.mLabelList.setAdapter(this.mLabelAdapt);
            this.mLabelList.setOnItemClickListener(this);
            if (this.mLabelAdapt.getCount() > 0) {
                setSearchLabelVisibility(0);
            } else {
                setSearchLabelVisibility(8);
            }
        }
        return rootView;
    }

    protected LayoutManager getLayoutManager() {
        return new StaggeredGridLayoutManager(getActivity().getResources().getInteger(R.integer.home_note_item_column), 1);
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mRecyclerViewAdapter.destroy();
        this.mLabelManager.removeLabelDataChangeListener(this.mLabelDataChangeListener);
        ((NoteMainActivity) getActivity()).endNoteSearch();
    }

    public void onDetach() {
        super.onDetach();
    }

    public void onResume() {
        super.onResume();
        this.mRecyclerViewAdapter.resume();
    }

    public void onPause() {
        super.onPause();
        this.mRecyclerViewAdapter.pause();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        try {
            int label = ((Integer) view.getTag()).intValue();
            Intent intent = new Intent(getActivity(), LabelNoteActivity.class);
            intent.putExtra(LabelNoteActivity.SELECT_LABEL_ID, label);
            intent.putExtra(LabelNoteActivity.SELECT_LABEL_NAME, ((TextView) view).getText());
            getActivity().startActivity(intent);
        } catch (Exception e) {
            Logger.printLog(TAG, "start LabelNoteActivity fail : " + e.toString());
        }
    }

    public void onLoadingStarted() {
        if (!this.mRecyclerViewAdapter.isEmpty() || this.mSearchContentIsEmpty) {
            this.mRecyclerView.setVisibility(0);
            this.mTipView.setVisibility(8);
            return;
        }
        this.mRecyclerView.setVisibility(4);
        this.mTipView.setVisibility(0);
        this.mTipTextView.setText(R.string.note_tip_search);
    }

    public void onLoadingFinished(boolean loadingFailed) {
        if (!this.mRecyclerViewAdapter.isEmpty() || this.mSearchContentIsEmpty) {
            this.mRecyclerView.setVisibility(0);
            this.mTipView.setVisibility(8);
            return;
        }
        this.mRecyclerView.setVisibility(4);
        this.mTipView.setVisibility(0);
        this.mTipTextView.setText(R.string.note_tip_searchFinish);
    }

    public void onSingleClickTouch(Path path) {
        try {
            Intent intent = new Intent(getActivity(), NewNoteActivity.class);
            intent.putExtra(NewNoteActivity.NOTE_ITEM_PATH, path.toString());
            getActivity().startActivity(intent);
        } catch (Exception e) {
            Logger.printLog(TAG, "start NewNoteActivity fail : " + e.toString());
        }
    }

    public void onLongClickTouch(Path path) {
    }
}
