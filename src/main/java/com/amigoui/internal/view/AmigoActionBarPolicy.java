package com.amigoui.internal.view;

import com.gionee.aminote.R;
import amigoui.widget.AmigoWidgetResource;
import android.content.Context;
import android.content.res.TypedArray;

public class AmigoActionBarPolicy {
    private Context mContext;

    public static AmigoActionBarPolicy get(Context context) {
        return new AmigoActionBarPolicy(context);
    }

    private AmigoActionBarPolicy(Context context) {
        this.mContext = context;
    }

    public int getMaxActionButtons() {
        return 0;
    }

    public boolean showsOverflowMenuButton() {
        return false;
    }

    public int getEmbeddedMenuWidthLimit() {
        return 0;
    }

    public boolean hasEmbeddedTabs() {
        return this.mContext.getResources().getBoolean(AmigoWidgetResource.getIdentifierByBool(this.mContext, "amigo_action_bar_embed_tabs"));
    }

    public int getTabContainerHeight() {
        TypedArray a = this.mContext.obtainStyledAttributes(null, R.styleable.AmigoActionBar, AmigoWidgetResource.getIdentifierByAttr(this.mContext, "amigoactionBarStyle"), 0);
        int height = a.getLayoutDimension(R.styleable.AmigoActionBar_amigotabheight, 0);
        if (height == 0) {
            height = a.getLayoutDimension(R.styleable.AmigoActionBar_amigoheight, 0);
        }
        a.recycle();
        return height;
    }

    public boolean enableHomeButtonByDefault() {
        return false;
    }

    public int getStackedTabMaxWidth() {
        return this.mContext.getResources().getDimensionPixelSize(AmigoWidgetResource.getIdentifierByDimen(this.mContext, "amigo_action_bar_stacked_tab_max_width"));
    }
}
