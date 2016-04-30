package amigoui.widget;

import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.View;
import android.widget.BaseAdapter;

import amigoui.app.AmigoActivity;

public class AmigoMultiChoiceAdapterHelper extends AmigoMultiChoiceAdapterHelperBase {
    private ActionMode actionMode;

    protected AmigoMultiChoiceAdapterHelper(BaseAdapter owner) {
        super(owner);
    }

    protected void startActionMode(View customView) {
        this.actionMode = ((AmigoActivity) getContext()).startActionMode((Callback) this.mOwner);
        if (customView != null) {
            if (this.actionMode != null) {
                this.actionMode.setCustomView(customView);
            }
        }
    }

    protected void finishActionMode() {
        if (this.actionMode != null) {
            this.actionMode.finish();
        }
    }

    protected void updateActionMode() {
        if (this.actionMode != null) {
            this.actionMode.invalidate();
        }
    }

    protected void setActionModeTitle(String title) {
        if (this.actionMode != null) {
            this.actionMode.setTitle(title);
        }
    }

    protected boolean isActionModeStarted() {
        return this.actionMode != null;
    }

    protected void clearActionMode() {
        this.actionMode = null;
    }
}
