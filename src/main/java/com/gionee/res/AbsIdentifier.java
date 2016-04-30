package com.gionee.res;

import amigoui.changecolors.ColorConfigConstants;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import java.lang.reflect.Field;

public abstract class AbsIdentifier implements Identifier {
    protected String mName;

    protected abstract String getType();

    protected AbsIdentifier(String name) {
        this.mName = name;
    }

    public int getIdentifier(Context context) throws ResourceNotFoundException {
        if (context == null) {
            throw new NullPointerException("context is null");
        }
        Resources res = context.getResources();
        String packageName = context.getPackageName();
        String type = getType();
        int id = res.getIdentifier(this.mName, type, packageName);
        if (id != 0) {
            return id;
        }
        throw new ResourceNotFoundException(type, this.mName);
    }

    public Object getResourceId(Context context) {
        try {
            for (Class<?> childClass : Class.forName(context.getPackageName() + ".R").getClasses()) {
                if (childClass.getSimpleName().equals(getType())) {
                    for (Field field : childClass.getFields()) {
                        String fieldName = field.getName();
                        if (fieldName.equals(this.mName)) {
                            System.out.println(fieldName);
                            return field.get(null);
                        }
                    }
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getIdentifier(Activity activity, String name) {
        if (activity == null) {
            throw new NullPointerException("context is null");
        }
        Resources res = activity.getResources();
        String packageName = activity.getPackageName();
        String type = ColorConfigConstants.ID;
        int id = res.getIdentifier(name, type, packageName);
        if (id != 0) {
            return id;
        }
        throw new ResourceNotFoundException(type, name);
    }
}
