package amigoui.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

abstract class AmigoGenericInflater<T, P extends Parent> {
    private static final Class[] mConstructorSignature = new Class[]{Context.class, AttributeSet.class};
    private static final HashMap sConstructorMap = new HashMap();
    private final boolean DEBUG;
    private final Object[] mConstructorArgs;
    protected final Context mContext;
    private String mDefaultPackage;
    private Factory<T> mFactory;
    private boolean mFactorySet;

    public interface Factory<T> {
        T onCreateItem(String str, Context context, AttributeSet attributeSet);
    }

    public interface Parent<T> {
        void addItemFromInflater(T t);
    }

    private static class FactoryMerger<T> implements Factory<T> {
        private final Factory<T> mF1;
        private final Factory<T> mF2;

        FactoryMerger(Factory<T> f1, Factory<T> f2) {
            this.mF1 = f1;
            this.mF2 = f2;
        }

        public T onCreateItem(String name, Context context, AttributeSet attrs) {
            T v = this.mF1.onCreateItem(name, context, attrs);
            return v != null ? v : this.mF2.onCreateItem(name, context, attrs);
        }
    }

    public abstract AmigoGenericInflater cloneInContext(Context context);

    protected AmigoGenericInflater(Context context) {
        this.DEBUG = false;
        this.mConstructorArgs = new Object[2];
        this.mContext = context;
    }

    protected AmigoGenericInflater(AmigoGenericInflater<T, P> original, Context newContext) {
        this.DEBUG = false;
        this.mConstructorArgs = new Object[2];
        this.mContext = newContext;
        this.mFactory = original.mFactory;
    }

    public void setDefaultPackage(String defaultPackage) {
        this.mDefaultPackage = defaultPackage;
    }

    public String getDefaultPackage() {
        return this.mDefaultPackage;
    }

    public Context getContext() {
        return this.mContext;
    }

    public final Factory<T> getFactory() {
        return this.mFactory;
    }

    public void setFactory(Factory<T> factory) {
        if (this.mFactorySet) {
            throw new IllegalStateException("A factory has already been set on this inflater");
        } else if (factory == null) {
            throw new NullPointerException("Given factory can not be null");
        } else {
            this.mFactorySet = true;
            if (this.mFactory == null) {
                this.mFactory = factory;
            } else {
                this.mFactory = new FactoryMerger(factory, this.mFactory);
            }
        }
    }

    public T inflate(int resource, P root) {
        return inflate(resource, (Parent) root, root != null);
    }

    public T inflate(XmlPullParser parser, P root) {
        return inflate(parser, (Parent) root, root != null);
    }

    public T inflate(int resource, P root, boolean attachToRoot) {
        XmlPullParser parser = getContext().getResources().getXml(resource);
        try {
            T inflate = inflate(parser, (Parent) root, attachToRoot);
            return inflate;
        } finally {
            parser.close();
        }
    }

    public T inflate(XmlPullParser parser, P root, boolean attachToRoot) {
        T result;
        synchronized (this.mConstructorArgs) {
            AttributeSet attrs = Xml.asAttributeSet(parser);
            this.mConstructorArgs[0] = this.mContext;
            result = root;
            int type;
            do {
                try {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } catch (InflateException e) {
                    throw e;
                } catch (XmlPullParserException e2) {
                    InflateException ex = new InflateException(e2.getMessage());
                    ex.initCause(e2);
                    throw ex;
                } catch (IOException e3) {
                    ex = new InflateException(parser.getPositionDescription() + ": " + e3.getMessage());
                    ex.initCause(e3);
                    throw ex;
                }
            } while (type != 1);
            if (type != 2) {
                throw new InflateException(parser.getPositionDescription() + ": No start tag found!");
            }
            result = onMergeRoots(root, attachToRoot, (Parent) createItemFromTag(parser, parser.getName(), attrs));
            rInflate(parser, result, attrs);
        }
        return result;
    }

    public final T createItem(String name, String prefix, AttributeSet attrs) throws ClassNotFoundException, InflateException {
        InflateException ie;
        Constructor constructor = (Constructor) sConstructorMap.get(name);
        try {
            if (isNativePreferenceClass(name)) {
                NativePreferenceManager.setAnalyzeNativePreferenceXml(true);
                name = "Amigo" + name;
            } else if (name.startsWith("android.preference.")) {
                NativePreferenceManager.setAnalyzeNativePreferenceXml(true);
                name = "amigoui.preference.Amigo" + name.substring(name.lastIndexOf(46) + 1);
            } else {
                NativePreferenceManager.setAnalyzeNativePreferenceXml(false);
            }
            if (constructor == null) {
                String str;
                ClassLoader classLoader = this.mContext.getClassLoader();
                if (prefix != null) {
                    str = prefix + name;
                } else {
                    str = name;
                }
                constructor = classLoader.loadClass(str).getConstructor(mConstructorSignature);
                sConstructorMap.put(name, constructor);
            }
            Object[] args = this.mConstructorArgs;
            args[1] = attrs;
            return constructor.newInstance(args);
        } catch (NoSuchMethodException e) {
            StringBuilder append = new StringBuilder().append(attrs.getPositionDescription()).append(": Error inflating class ");
            if (prefix != null) {
                name = prefix + name;
            }
            ie = new InflateException(append.append(name).toString());
            ie.initCause(e);
            throw ie;
        } catch (ClassNotFoundException e2) {
            throw e2;
        } catch (Exception e3) {
            ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + constructor.getClass().getName());
            ie.initCause(e3);
            throw ie;
        }
    }

    protected T onCreateItem(String name, AttributeSet attrs) throws ClassNotFoundException {
        return createItem(name, this.mDefaultPackage, attrs);
    }

    private final T createItemFromTag(XmlPullParser parser, String name, AttributeSet attrs) {
        InflateException ie;
        T item = null;
        try {
            if (this.mFactory != null) {
                item = this.mFactory.onCreateItem(name, this.mContext, attrs);
            }
            if (item != null) {
                return item;
            }
            if (-1 == name.indexOf(46)) {
                return onCreateItem(name, attrs);
            }
            return createItem(name, null, attrs);
        } catch (InflateException e) {
            throw e;
        } catch (ClassNotFoundException e2) {
            ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name);
            ie.initCause(e2);
            throw ie;
        } catch (Exception e3) {
            ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name);
            ie.initCause(e3);
            throw ie;
        }
    }

    private void rInflate(XmlPullParser parser, T parent, AttributeSet attrs) throws XmlPullParserException, IOException {
        int depth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if ((type == 3 && parser.getDepth() <= depth) || type == 1) {
                return;
            }
            if (type == 2 && !onCreateCustomFromTag(parser, parent, attrs)) {
                T item = createItemFromTag(parser, parser.getName(), attrs);
                ((Parent) parent).addItemFromInflater(item);
                rInflate(parser, item, attrs);
            }
        }
    }

    protected boolean onCreateCustomFromTag(XmlPullParser parser, T t, AttributeSet attrs) throws XmlPullParserException {
        return false;
    }

    protected P onMergeRoots(P p, boolean attachToGivenRoot, P xmlRoot) {
        return xmlRoot;
    }

    private boolean isNativePreferenceClass(String name) {
        if (-1 != name.indexOf(46)) {
            return false;
        }
        if (name.equals("PreferenceScreen") || name.equals("PreferenceGroup") || name.equals("PreferenceCategory") || name.equals("Preference") || name.equals("PreferenceFragment") || name.equals("CheckBoxPreference") || name.equals("SwitchPreference") || name.equals("DialogPreference") || name.equals("EditTextPreference") || name.equals("ListPreference") || name.equals("MultiCheckPreference") || name.equals("MultiSelectListPreference") || name.equals("RingtonePreference") || name.equals("SeekBarDialogPreference") || name.equals("SeekBarPreference") || name.equals("VolumePreference")) {
            return true;
        }
        return false;
    }
}
