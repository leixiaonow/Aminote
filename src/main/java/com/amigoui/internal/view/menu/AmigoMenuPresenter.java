package com.amigoui.internal.view.menu;

import android.content.Context;
import android.os.Parcelable;
import android.view.ViewGroup;

public interface AmigoMenuPresenter {

    public interface Callback {
        void onCloseMenu(AmigoMenuBuilder amigoMenuBuilder, boolean z);

        boolean onOpenSubMenu(AmigoMenuBuilder amigoMenuBuilder);
    }

    boolean collapseItemActionView(AmigoMenuBuilder amigoMenuBuilder, AmigoMenuItemImpl amigoMenuItemImpl);

    boolean expandItemActionView(AmigoMenuBuilder amigoMenuBuilder, AmigoMenuItemImpl amigoMenuItemImpl);

    boolean flagActionItems();

    int getId();

    AmigoMenuView getMenuView(ViewGroup viewGroup);

    void initForMenu(Context context, AmigoMenuBuilder amigoMenuBuilder);

    void onCloseMenu(AmigoMenuBuilder amigoMenuBuilder, boolean z);

    void onRestoreInstanceState(Parcelable parcelable);

    Parcelable onSaveInstanceState();

    boolean onSubMenuSelected(AmigoSubMenuBuilder amigoSubMenuBuilder);

    void setCallback(Callback callback);

    void updateMenuView(boolean z);
}
