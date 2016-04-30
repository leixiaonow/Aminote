package com.gionee.note.data;

import com.gionee.note.common.IdentityCache;
import com.gionee.note.common.NoteUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class Path {
    private static final String TAG = "Path";
    private static Path sRoot = new Path(null, "ROOT");
    private IdentityCache<String, Path> mChildren;
    private WeakReference<NoteObject> mObject;
    private final Path mParent;
    private final String mSegment;

    private Path(Path parent, String segment) {
        this.mParent = parent;
        this.mSegment = segment;
    }

    public static Path fromString(String s) {
        Path current;
        synchronized (Path.class) {
            String[] segments = split(s);
            current = sRoot;
            for (String child : segments) {
                current = current.getChild(child);
            }
        }
        return current;
    }

    public static String[] split(String s) {
        int n = s.length();
        if (n == 0) {
            return new String[0];
        }
        if (s.charAt(0) != '/') {
            throw new RuntimeException("malformed path:" + s);
        }
        ArrayList<String> segments = new ArrayList();
        int i = 1;
        while (i < n) {
            int brace = 0;
            int j = i;
            while (j < n) {
                char c = s.charAt(j);
                if (c != '{') {
                    if (c != '}') {
                        if (brace == 0 && c == '/') {
                            break;
                        }
                    }
                    brace--;
                } else {
                    brace++;
                }
                j++;
            }
            if (brace != 0) {
                throw new RuntimeException("unbalanced brace in path:" + s);
            }
            segments.add(s.substring(i, j));
            i = j + 1;
        }
        String[] result = new String[segments.size()];
        segments.toArray(result);
        return result;
    }

    public static String[] splitSequence(String s) {
        int n = s.length();
        if (s.charAt(0) == '{' && s.charAt(n - 1) == '}') {
            ArrayList<String> segments = new ArrayList();
            int j;
            for (int i = 1; i < n - 1; i = j + 1) {
                int brace = 0;
                j = i;
                while (j < n - 1) {
                    char c = s.charAt(j);
                    if (c != '{') {
                        if (c != '}') {
                            if (brace == 0 && c == ',') {
                                break;
                            }
                        }
                        brace--;
                    } else {
                        brace++;
                    }
                    j++;
                }
                if (brace != 0) {
                    throw new RuntimeException("unbalanced brace in path:" + s);
                }
                segments.add(s.substring(i, j));
            }
            String[] result = new String[segments.size()];
            segments.toArray(result);
            return result;
        }
        throw new RuntimeException("bad sequence: " + s);
    }

    static void dumpAll(Path p, String prefix1, String prefix2) {
        synchronized (Path.class) {
            if (p.mChildren != null) {
                ArrayList<String> childrenKeys = p.mChildren.keys();
                int i = 0;
                int n = childrenKeys.size();
                Iterator i$ = childrenKeys.iterator();
                while (i$.hasNext()) {
                    Path child = (Path) p.mChildren.get((String) i$.next());
                    if (child == null) {
                        i++;
                    } else {
                        i++;
                        if (i < n) {
                            dumpAll(child, prefix2 + "+-- ", prefix2 + "|   ");
                        } else {
                            dumpAll(child, prefix2 + "+-- ", prefix2 + "    ");
                        }
                    }
                }
            }
        }
    }

    public Path getChild(String segment) {
        synchronized (Path.class) {
            Path p;
            if (this.mChildren == null) {
                this.mChildren = new IdentityCache();
            } else {
                p = (Path) this.mChildren.get(segment);
                if (p != null) {
                    return p;
                }
            }
            p = new Path(this, segment);
            this.mChildren.put(segment, p);
            return p;
        }
    }

    public Path getChild(int segment) {
        return getChild(String.valueOf(segment));
    }

    public Path getChild(long segment) {
        return getChild(String.valueOf(segment));
    }

    NoteObject getObject() {
        NoteObject noteObject;
        synchronized (Path.class) {
            noteObject = this.mObject == null ? null : (NoteObject) this.mObject.get();
        }
        return noteObject;
    }

    public void setObject(NoteObject object) {
        synchronized (Path.class) {
            boolean z = this.mObject == null || this.mObject.get() == null;
            NoteUtils.assertTrue(z);
            this.mObject = new WeakReference(object);
        }
    }

    public String toString() {
        StringBuilder sb;
        synchronized (Path.class) {
            sb = new StringBuilder();
            String[] segments = split();
            for (String append : segments) {
                sb.append("/");
                sb.append(append);
            }
        }
        return sb.toString();
    }

    public String[] split() {
        String[] segments;
        synchronized (Path.class) {
            Path p;
            int n = 0;
            for (p = this; p != sRoot; p = p.mParent) {
                n++;
            }
            segments = new String[n];
            p = this;
            int i = n - 1;
            while (p != sRoot) {
                int i2 = i - 1;
                segments[i] = p.mSegment;
                p = p.mParent;
                i = i2;
            }
        }
        return segments;
    }

    public String getPrefix() {
        if (this == sRoot) {
            return "";
        }
        return getPrefixPath().mSegment;
    }

    public Path getPrefixPath() {
        Path current;
        synchronized (Path.class) {
            if (this == sRoot) {
                throw new IllegalStateException();
            }
            while (current.mParent != sRoot) {
                current = current.mParent;
            }
        }
        return current;
    }
}
