package com.gionee.framework.component;

import android.content.Context;

public interface ApplicationContextHolder {
    public static final Context APPLICATION_CONTEXT = BaseApplication.sApplication;
    public static final Context CONTEXT = APPLICATION_CONTEXT;
    public static final String PACKAGE_NAME = CONTEXT.getPackageName();
}
