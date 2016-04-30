package amigoui.reflection;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.view.ViewGroup;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPresenter.Callback;

public class AmigoActionMenuPresenter {
    private Object mAmigoActionMenuPresenter = null;
    private Class<?> mClasz = null;

    public Object getmAmigoActionMenuPresenter() {
        return this.mAmigoActionMenuPresenter;
    }

    public AmigoActionMenuPresenter(Context context) {
        if (VERSION.SDK_INT < 21) {
            getClass(context, "com.android.internal.view.menu.ActionMenuPresenter");
        } else {
            getClass(context, "android.widget.ActionMenuPresenter");
        }
    }

    private void getClass(Context context, String className) {
        try {
            this.mClasz = Class.forName(className);
            this.mAmigoActionMenuPresenter = this.mClasz.getConstructor(new Class[]{Context.class}).newInstance(new Object[]{context});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        try {
            this.mClasz.getMethod("onConfigurationChanged", new Class[]{Configuration.class}).invoke(getmAmigoActionMenuPresenter(), new Object[]{newConfig});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean showOverflowMenu() {
        try {
            return ((Boolean) this.mClasz.getMethod("showOverflowMenu", new Class[0]).invoke(getmAmigoActionMenuPresenter(), new Object[0])).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hideOverflowMenu() {
        try {
            return ((Boolean) this.mClasz.getMethod("hideOverflowMenu", new Class[0]).invoke(getmAmigoActionMenuPresenter(), new Object[0])).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setCallback(Callback cb) {
        try {
            this.mClasz.getMethod("setCallback", new Class[]{Callback.class}).invoke(getmAmigoActionMenuPresenter(), new Object[]{cb});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setId(int menuId) {
        try {
            this.mClasz.getMethod("setId", new Class[]{Integer.TYPE}).invoke(getmAmigoActionMenuPresenter(), new Object[]{Integer.valueOf(menuId)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideSubMenus() {
        try {
            this.mClasz.getMethod("hideSubMenus", new Class[0]).invoke(getmAmigoActionMenuPresenter(), new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setExpandedActionViewsExclusive(boolean isExclusive) {
        try {
            this.mClasz.getMethod("setExpandedActionViewsExclusive", new Class[]{Boolean.TYPE}).invoke(getmAmigoActionMenuPresenter(), new Object[]{Boolean.valueOf(isExclusive)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ViewGroup getMenuView(ViewGroup root) {
        try {
            return (ViewGroup) this.mClasz.getMethod("getMenuView", new Class[]{ViewGroup.class}).invoke(getmAmigoActionMenuPresenter(), new Object[]{root});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setWidthLimit(int width, boolean strict) {
        try {
            this.mClasz.getMethod("setWidthLimit", new Class[]{Integer.TYPE, Boolean.TYPE}).invoke(getmAmigoActionMenuPresenter(), new Object[]{Integer.valueOf(width), Boolean.valueOf(strict)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setItemLimit(int itemCount) {
        try {
            this.mClasz.getMethod("setItemLimit", new Class[]{Integer.TYPE}).invoke(getmAmigoActionMenuPresenter(), new Object[]{Integer.valueOf(itemCount)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initForMenu(Context context, MenuBuilder menu) {
        try {
            this.mClasz.getMethod("initForMenu", new Class[]{Context.class, MenuBuilder.class}).invoke(getmAmigoActionMenuPresenter(), new Object[]{context, menu});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateMenuView(boolean cleared) {
        try {
            this.mClasz.getMethod("updateMenuView", new Class[]{Boolean.TYPE}).invoke(getmAmigoActionMenuPresenter(), new Object[]{Boolean.valueOf(cleared)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isOverflowMenuShowing() {
        try {
            return ((Boolean) this.mClasz.getMethod("isOverflowMenuShowing", new Class[0]).invoke(getmAmigoActionMenuPresenter(), new Object[0])).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isOverflowReserved() {
        try {
            return ((Boolean) this.mClasz.getMethod("isOverflowReserved", new Class[0]).invoke(getmAmigoActionMenuPresenter(), new Object[0])).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void dismissPopupMenus() {
        try {
            this.mClasz.getMethod("dismissPopupMenus", new Class[0]).invoke(getmAmigoActionMenuPresenter(), new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
