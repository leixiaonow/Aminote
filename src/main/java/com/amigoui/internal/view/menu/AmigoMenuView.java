package com.amigoui.internal.view.menu;

import android.graphics.drawable.Drawable;

public interface AmigoMenuView {

    public interface ItemView {
        AmigoMenuItemImpl getItemData();

        void initialize(AmigoMenuItemImpl amigoMenuItemImpl, int i);

        boolean prefersCondensedTitle();

        void setCheckable(boolean z);

        void setChecked(boolean z);

        void setEnabled(boolean z);

        void setIcon(Drawable drawable);

        void setShortcut(boolean z, char c);

        void setTitle(CharSequence charSequence);

        boolean showsIcon();
    }

    int getWindowAnimations();

    void initialize(AmigoMenuBuilder amigoMenuBuilder);
}
