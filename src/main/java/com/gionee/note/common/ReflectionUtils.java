package com.gionee.note.common;

import com.gionee.framework.log.Logger;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionUtils {
    private static final String TAG = "ReflectionUtils";

    public static Object getFeild(Object owner, String fieldName) {
        try {
            Field field = owner.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(owner);
        } catch (Exception e) {
            Logger.printLog(TAG, "getFeild failed fieldName=" + fieldName + " ,,,e ==" + e);
            return null;
        }
    }

    public static int getStaticIntFeild(Class<?> owner, String fieldName) {
        try {
            return owner.getDeclaredField(fieldName).getInt(null);
        } catch (Exception e) {
            Logger.printLog(TAG, "getIntFeild failed fieldName=" + fieldName + ",,,e = " + e);
            return -1;
        }
    }

    public static Field findField(Class<?> clazz, String name) {
        Class<?> searchType = clazz;
        while (!Object.class.equals(searchType) && searchType != null) {
            for (Field field : searchType.getDeclaredFields()) {
                if (name.equals(field.getName())) {
                    field.setAccessible(true);
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    public static void setField(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException ex) {
            Logger.printLog(TAG, "setField failed value= " + value + ",,,ex,,," + ex);
        }
    }

    public static Object getField(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException ex) {
            Logger.printLog(TAG, "getField failed :" + ex);
            return null;
        }
    }

    public static Method findMethod(Class<?> clazz, String name) {
        return findMethod(clazz, name, new Class[0]);
    }

    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        Class<?> searchType = clazz;
        while (searchType != null) {
            for (Method method : searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods()) {
                if (name.equals(method.getName()) && (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    public static Object invokeMethod(Method method, Object target) {
        return invokeMethod(method, target, new Object[0]);
    }

    public static Object invokeMethod(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (Exception ex) {
            Logger.printLog(TAG, "invokeMethod method= " + method + ",,,ex,,," + ex);
            return null;
        }
    }
}
