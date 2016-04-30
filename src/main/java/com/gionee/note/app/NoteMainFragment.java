package com.gionee.note.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.gionee.aminote.R;
import com.gionee.framework.log.Logger;
import com.gionee.note.app.NoteDelExecutor.NoteDelListener;
import com.gionee.note.app.NoteSelectionManager.SelectionListener;
import com.gionee.note.app.RecyclerViewAdapter.OnTouchListener;
import com.gionee.note.common.StatisticsModule;
import com.gionee.note.data.LocalSource;
import com.gionee.note.data.NoteSet;
import com.gionee.note.data.Path;
import com.gionee.note.widget.WidgetUtil;

public class NoteMainFragment extends Fragment implements LoadingListener, OnTouchListener, SelectionListener {
    private static final boolean DEBUG = false;
    private static final String TAG = "NoteMainFragment";
    private RecyclerViewAdapter mAdapter;
    private NoteDelExecutor mNoteDelExecutor;
    private NoteSelectionManager mNoteSelectionManager;
    private OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.note_main_activity_action_mode_title_layout_back:
                    NoteMainFragment.this.mNoteSelectionManager.leaveSelectionMode();
                    return;
                case R.id.note_main_activity_action_mode_title_layout_select:
                    if (NoteMainFragment.this.mNoteSelectionManager.inSelectAllMode()) {
                        NoteMainFragment.this.mNoteSelectionManager.deSelectAll();
                        return;
                    } else {
                        NoteMainFragment.this.mNoteSelectionManager.selectAll();
                        return;
                    }
                default:
                    return;
            }
        }
    };
    private RecyclerView mRecyclerView;
    private View mRooView;
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
    }

    private void initData() {
        NoteSet noteSet = ((NoteAppImpl) getActivity().getApplication()).getDataManager().getMediaSet(LocalSource.LOCAL_SET_PATH);
        NoteSelectionManager noteSelectionManager = new NoteSelectionManager();
        noteSelectionManager.setSelectionListener(this);
        this.mNoteSelectionManager = noteSelectionManager;
        this.mNoteSelectionManager.setSourceMediaSet(noteSet);
        this.mAdapter = new RecyclerViewAdapter(getActivity(), noteSet, this, noteSelectionManager);
        this.mAdapter.setOnTouchListener(this);
        this.mNoteDelExecutor = new NoteDelExecutor(getActivity());
    }

    private LayoutManager getLayoutManager() {
        return new StaggeredGridLayoutManager(getActivity().getResources().getInteger(R.integer.home_note_item_column), 1);
    }

    public void onLoadingStarted() {
        if (this.mAdapter.getItemCount() == 0) {
            this.mRecyclerView.setVisibility(4);
            this.mTipView.setVisibility(0);
            this.mTipTextView.setText(R.string.note_tip_loading);
            return;
        }
        this.mRecyclerView.setVisibility(0);
        this.mTipView.setVisibility(8);
    }

    public void onLoadingFinished(boolean loadingFailed) {
        if (this.mAdapter.getItemCount() == 0) {
            this.mRecyclerView.setVisibility(4);
            this.mTipView.setVisibility(0);
            this.mTipTextView.setText(R.string.note_tip_loadFinish);
            return;
        }
        this.mRecyclerView.setVisibility(0);
        this.mTipView.setVisibility(8);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = this.mRooView;
        if (rootView != null) {
            return rootView;
        }
        rootView = inflater.inflate(R.layout.note_main_fragment_layout, container, false);
        this.mRooView = rootView;
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.home_recycler_view);
        this.mRecyclerView = recyclerView;
        recyclerView.setLayoutManager(getLayoutManager());
        recyclerView.setAdapter(this.mAdapter);
        this.mTipView = rootView.findViewById(R.id.note_tip_view);
        this.mTipTextView = (TextView) rootView.findViewById(R.id.note_tip_text_view);
        return rootView;
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mAdapter.destroy();
        this.mNoteDelExecutor.destroy();
    }

    public void onDetach() {
        super.onDetach();
    }

    public void onResume() {
        super.onResume();
        this.mAdapter.resume();
        this.mNoteDelExecutor.resume();
    }

    public void onPause() {
        super.onPause();
        this.mAdapter.pause();
        this.mNoteDelExecutor.pause();
    }

    public void onSingleClickTouch(Path path) {
        if (this.mNoteSelectionManager.inSelectionMode()) {
            this.mNoteSelectionManager.toggle(path);
            return;
        }
        try {
            Intent intent = new Intent(getActivity(), NewNoteActivity.class);
            intent.putExtra(NewNoteActivity.NOTE_ITEM_PATH, path.toString());
            getActivity().startActivity(intent);
        } catch (Exception e) {
            Logger.printLog(TAG, "error:" + e);
        }
    }

    public void onLongClickTouch(Path path) {
        this.mNoteSelectionManager.toggle(path);
    }

    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case 1:
                startSelectionMode();
                this.mAdapter.notifyDataSetChanged();
                return;
            case 2:
                finishSelectionMode();
                this.mAdapter.notifyDataSetChanged();
                return;
            case 3:
                updateSelectionModeTitle();
                updateSelectionViewsState();
                this.mAdapter.notifyDataSetChanged();
                return;
            case 4:
                updateSelectionModeTitle();
                updateSelectionViewsState();
                this.mAdapter.notifyDataSetChanged();
                return;
            default:
                return;
        }
    }

    public void onSelectionChange(Path path, boolean selected) {
        updateSelectionModeTitle();
        updateSelectionViewsState();
        this.mAdapter.notifyDataSetChanged();
    }

    private void updateSelectionModeTitle() {
        setSelectionModeTitle(String.format(getActivity().getResources().getQuantityString(R.plurals.number_of_items_selected, this.mNoteSelectionManager.getSelectedCount()), new Object[]{Integer.valueOf(count)}));
    }

    private void startSelectionMode() {
        ((NoteMainActivity) getActivity()).startSelectionMode(this.mNoteSelectionManager, this.mOnClickListener);
    }

    public void onDel() {
        youjuStatistics(R.string.youju_batch_del);
        this.mNoteDelExecutor.startAction(this.mNoteSelectionManager, new NoteDelListener() {
            public void onDelPrepare() {
            }

            public int onDelInvalidId() {
                return 0;
            }

            public void onDelFinish(int success, int fail) {
                if (success > 0) {
                    WidgetUtil.updateAllWidgets();
                }
                NoteMainFragment.this.mNoteSelectionManager.leaveSelectionMode();
            }
        });
    }

    private void finishSelectionMode() {
        ((NoteMainActivity) getActivity()).finishSelectionMode();
    }

    private void setSelectionModeTitle(String title) {
        ((NoteMainActivity) getActivity()).setSelectionModeTitle(title);
    }

    private void updateSelectionViewsState() {
        ((NoteMainActivity) getActivity()).updateSelectionViewsState();
    }

    private void youjuStatistics(int stringId) {
        Logger.printLog("youju", "youju envent: " + getResources().getString(stringId));
        StatisticsModule.onEvent(getActivity(), getResources().getString(stringId));
    }
}
