package amigoui.view;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;

public interface AmigoMenuItem {
    public static final int SHOW_AS_ACTION_ALWAYS = 2;
    public static final int SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW = 8;
    public static final int SHOW_AS_ACTION_IF_ROOM = 1;
    public static final int SHOW_AS_ACTION_NEVER = 0;
    public static final int SHOW_AS_ACTION_WITH_TEXT = 4;

    public interface OnActionExpandListener {
        boolean onMenuItemActionCollapse(AmigoMenuItem amigoMenuItem);

        boolean onMenuItemActionExpand(AmigoMenuItem amigoMenuItem);
    }

    public interface OnMenuItemClickListener {
        boolean onMenuItemClick(AmigoMenuItem amigoMenuItem);
    }

    boolean collapseActionView();

    boolean expandActionView();

    AmigoActionProvider getActionProvider();

    View getActionView();

    char getAlphabeticShortcut();

    int getGroupId();

    Drawable getIcon();

    Intent getIntent();

    int getItemId();

    ContextMenuInfo getMenuInfo();

    char getNumericShortcut();

    int getOrder();

    AmigoSubMenu getSubMenu();

    CharSequence getTitle();

    CharSequence getTitleCondensed();

    boolean hasSubMenu();

    boolean isActionViewExpanded();

    boolean isCheckable();

    boolean isChecked();

    boolean isEnabled();

    boolean isVisible();

    AmigoMenuItem setActionProvider(AmigoActionProvider amigoActionProvider);

    AmigoMenuItem setActionView(int i);

    AmigoMenuItem setActionView(View view);

    AmigoMenuItem setAlphabeticShortcut(char c);

    AmigoMenuItem setCheckable(boolean z);

    AmigoMenuItem setChecked(boolean z);

    AmigoMenuItem setEnabled(boolean z);

    AmigoMenuItem setIcon(int i);

    AmigoMenuItem setIcon(Drawable drawable);

    AmigoMenuItem setIntent(Intent intent);

    AmigoMenuItem setNumericShortcut(char c);

    AmigoMenuItem setOnActionExpandListener(OnActionExpandListener onActionExpandListener);

    AmigoMenuItem setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener);

    AmigoMenuItem setShortcut(char c, char c2);

    void setShowAsAction(int i);

    AmigoMenuItem setShowAsActionFlags(int i);

    AmigoMenuItem setTitle(int i);

    AmigoMenuItem setTitle(CharSequence charSequence);

    AmigoMenuItem setTitleCondensed(CharSequence charSequence);

    AmigoMenuItem setVisible(boolean z);
}
