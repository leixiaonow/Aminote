package com.amigoui.internal.view.menu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import com.amigoui.internal.view.menu.AmigoMenuBuilder.Callback;
import com.gionee.note.app.dataupgrade.DataUpgrade;

public class AmigoSubMenuBuilder extends AmigoMenuBuilder implements SubMenu {
    private AmigoMenuItemImpl mItem;
    private AmigoMenuBuilder mParentMenu;

    public AmigoSubMenuBuilder(Context context, AmigoMenuBuilder parentMenu, AmigoMenuItemImpl item) {
        super(context);
        this.mParentMenu = parentMenu;
        this.mItem = item;
    }

    public void setQwertyMode(boolean isQwerty) {
        this.mParentMenu.setQwertyMode(isQwerty);
    }

    public boolean isQwertyMode() {
        return this.mParentMenu.isQwertyMode();
    }

    public void setShortcutsVisible(boolean shortcutsVisible) {
        this.mParentMenu.setShortcutsVisible(shortcutsVisible);
    }

    public boolean isShortcutsVisible() {
        return this.mParentMenu.isShortcutsVisible();
    }

    public Menu getParentMenu() {
        return this.mParentMenu;
    }

    public MenuItem getItem() {
        return this.mItem;
    }

    public void setCallback(Callback callback) {
        this.mParentMenu.setCallback(callback);
    }

    public AmigoMenuBuilder getRootMenu() {
        return this.mParentMenu;
    }

    boolean dispatchMenuItemSelected(AmigoMenuBuilder AmigoMenu, MenuItem item) {
        return super.dispatchMenuItemSelected(AmigoMenu, item) || this.mParentMenu.dispatchMenuItemSelected(AmigoMenu, item);
    }

    public SubMenu setIcon(Drawable icon) {
        this.mItem.setIcon(icon);
        return this;
    }

    public SubMenu setIcon(int iconRes) {
        this.mItem.setIcon(iconRes);
        return this;
    }

    public SubMenu setHeaderIcon(Drawable icon) {
        return (SubMenu) super.setHeaderIconInt(icon);
    }

    public SubMenu setHeaderIcon(int iconRes) {
        return (SubMenu) super.setHeaderIconInt(iconRes);
    }

    public SubMenu setHeaderTitle(CharSequence title) {
        return (SubMenu) super.setHeaderTitleInt(title);
    }

    public SubMenu setHeaderTitle(int titleRes) {
        return (SubMenu) super.setHeaderTitleInt(titleRes);
    }

    public SubMenu setHeaderView(View view) {
        return (SubMenu) super.setHeaderViewInt(view);
    }

    public boolean expandItemActionView(AmigoMenuItemImpl item) {
        return this.mParentMenu.expandItemActionView(item);
    }

    public boolean collapseItemActionView(AmigoMenuItemImpl item) {
        return this.mParentMenu.collapseItemActionView(item);
    }

    public String getActionViewStatesKey() {
        int itemId = this.mItem != null ? this.mItem.getItemId() : 0;
        if (itemId == 0) {
            return null;
        }
        return super.getActionViewStatesKey() + DataUpgrade.SPLIT + itemId;
    }
}
