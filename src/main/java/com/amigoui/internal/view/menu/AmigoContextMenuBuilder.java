package com.amigoui.internal.view.menu;

import amigoui.widget.AmigoListView;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.View;

public class AmigoContextMenuBuilder extends AmigoMenuBuilder implements ContextMenu {
    private Context mContext;
    private Fragment mFragment;

    public AmigoContextMenuBuilder(Context context) {
        super(context);
        this.mContext = context;
    }

    public ContextMenu setHeaderIcon(Drawable icon) {
        return (ContextMenu) super.setHeaderIconInt(icon);
    }

    public ContextMenu setHeaderIcon(int iconRes) {
        return (ContextMenu) super.setHeaderIconInt(iconRes);
    }

    public ContextMenu setHeaderTitle(CharSequence title) {
        return (ContextMenu) super.setHeaderTitleInt(title);
    }

    public ContextMenu setHeaderTitle(int titleRes) {
        return (ContextMenu) super.setHeaderTitleInt(titleRes);
    }

    public ContextMenu setHeaderView(View view) {
        return (ContextMenu) super.setHeaderViewInt(view);
    }

    public AmigoMenuDialogHelper show(View originalView, IBinder token) {
        if (originalView == null || !(originalView instanceof AmigoListView) || getVisibleItems().size() <= 0) {
            return null;
        }
        AmigoMenuDialogHelper helper = new AmigoMenuDialogHelper(this);
        helper.setFragment(this.mFragment);
        helper.show(token);
        return helper;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void setFragment(Fragment fragment) {
        this.mFragment = fragment;
    }
}
