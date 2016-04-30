package com.amigoui.internal.view.menu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.amigoui.internal.view.menu.AmigoMenuBuilder.ItemInvoker;

public class AmigoExpandedMenuView extends ListView implements ItemInvoker, AmigoMenuView, OnItemClickListener {
    private int mAnimations;
    private AmigoMenuBuilder mMenu;

    public AmigoExpandedMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnItemClickListener(this);
    }

    public void initialize(AmigoMenuBuilder menu) {
        this.mMenu = menu;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setChildrenDrawingCacheEnabled(false);
    }

    public boolean invokeItem(AmigoMenuItemImpl item) {
        return this.mMenu.performItemAction(item, 0);
    }

    public void onItemClick(AdapterView parent, View v, int position, long id) {
        invokeItem((AmigoMenuItemImpl) getAdapter().getItem(position));
    }

    public int getWindowAnimations() {
        return this.mAnimations;
    }
}
