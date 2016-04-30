package com.amigoui.internal.view.menu;

import amigoui.app.AmigoActivity;
import amigoui.app.AmigoAlertDialog;
import amigoui.app.AmigoAlertDialog.Builder;
import amigoui.widget.AmigoWidgetResource;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.amigoui.internal.view.menu.AmigoMenuPresenter.Callback;

public class AmigoMenuDialogHelper implements OnKeyListener, OnClickListener, OnDismissListener, Callback {
    private AmigoAlertDialog mDialog;
    private Fragment mFragment;
    private AmigoMenuBuilder mMenu;
    AmigoListMenuPresenter mPresenter;
    private Callback mPresenterCallback;

    public AmigoMenuDialogHelper(AmigoMenuBuilder menu) {
        this.mMenu = menu;
    }

    public void show(IBinder windowToken) {
        AmigoMenuBuilder menu = this.mMenu;
        Builder builder = new Builder(menu.getContext());
        this.mPresenter = new AmigoListMenuPresenter(builder.getContext(), AmigoWidgetResource.getIdentifierByLayout(builder.getContext(), "amigo_list_menu_item_layout"));
        this.mPresenter.setCallback(this);
        this.mMenu.addMenuPresenter(this.mPresenter);
        builder.setAdapter(this.mPresenter.getAdapter(), this);
        View headerView = menu.getHeaderView();
        if (headerView != null) {
            builder.setCustomTitle(headerView);
        } else {
            builder.setIcon(menu.getHeaderIcon()).setTitle(menu.getHeaderTitle());
        }
        builder.setOnKeyListener(this);
        this.mDialog = builder.create();
        this.mDialog.setOnDismissListener(this);
        LayoutParams lp = this.mDialog.getWindow().getAttributes();
        lp.type = 1003;
        if (windowToken != null) {
            lp.token = windowToken;
        }
        lp.flags |= 131072;
        this.mDialog.show();
    }

    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == 82 || keyCode == 4) {
            Window win;
            View decor;
            DispatcherState ds;
            if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                win = this.mDialog.getWindow();
                if (win != null) {
                    decor = win.getDecorView();
                    if (decor != null) {
                        ds = decor.getKeyDispatcherState();
                        if (ds != null) {
                            ds.startTracking(event, this);
                            return true;
                        }
                    }
                }
            } else if (event.getAction() == 1 && !event.isCanceled()) {
                win = this.mDialog.getWindow();
                if (win != null) {
                    decor = win.getDecorView();
                    if (decor != null) {
                        ds = decor.getKeyDispatcherState();
                        if (ds != null && ds.isTracking(event)) {
                            this.mMenu.close(true);
                            dialog.dismiss();
                            return true;
                        }
                    }
                }
            }
        }
        return this.mMenu.performShortcut(keyCode, event, 0);
    }

    public void setPresenterCallback(Callback cb) {
        this.mPresenterCallback = cb;
    }

    public void dismiss() {
        boolean mIsActivityFinish = false;
        Context context = this.mMenu.getContext();
        if (context instanceof AmigoActivity) {
            AmigoActivity activity = (AmigoActivity) context;
            if (activity != null) {
                mIsActivityFinish = activity.isFinishing() || activity.isDestroyed();
            }
        }
        if (this.mDialog != null && !mIsActivityFinish) {
            this.mDialog.dismiss();
        }
    }

    public void onDismiss(DialogInterface dialog) {
        this.mPresenter.onCloseMenu(this.mMenu, true);
    }

    public void onCloseMenu(AmigoMenuBuilder menu, boolean allMenusAreClosing) {
        if (allMenusAreClosing || menu == this.mMenu) {
            dismiss();
        }
        if (this.mPresenterCallback != null) {
            this.mPresenterCallback.onCloseMenu(menu, allMenusAreClosing);
        }
        ((AmigoActivity) this.mMenu.getContext()).onContextMenuClosed(this.mMenu);
    }

    public boolean onOpenSubMenu(AmigoMenuBuilder subMenu) {
        if (this.mPresenterCallback != null) {
            return this.mPresenterCallback.onOpenSubMenu(subMenu);
        }
        return false;
    }

    public void onClick(DialogInterface dialog, int which) {
        Context context = this.mMenu.getContext();
        AmigoMenuItemImpl menuItem = (AmigoMenuItemImpl) this.mPresenter.getAdapter().getItem(which);
        Intent intent = menuItem.getIntent();
        if (intent != null) {
            context.startActivity(intent);
            return;
        }
        OnMenuItemClickListener clickListener = menuItem.getMenuItemClickListener();
        if (clickListener != null && clickListener.onMenuItemClick(menuItem)) {
            return;
        }
        if (this.mFragment != null) {
            this.mFragment.onContextItemSelected(menuItem);
        } else {
            ((AmigoActivity) context).onContextItemSelected(menuItem);
        }
    }

    public void setFragment(Fragment fragment) {
        this.mFragment = fragment;
    }
}
