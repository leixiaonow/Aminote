package com.gionee.note.common;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class IdentityCache<K, V> {
    private ReferenceQueue<V> mQueue = new ReferenceQueue();
    private final HashMap<K, Entry<K, V>> mWeakMap = new HashMap();

    private static class Entry<K, V> extends WeakReference<V> {
        K mKey;

        public Entry(K key, V value, ReferenceQueue<V> queue) {
            super(value, queue);
            this.mKey = key;
        }
    }

    private void cleanUpWeakMap() {
        Entry<K, V> entry = (Entry) this.mQueue.poll();
        while (entry != null) {
            this.mWeakMap.remove(entry.mKey);
            entry = (Entry) this.mQueue.poll();
        }
    }

    public synchronized V put(K key, V value) {
        Entry<K, V> entry;
        cleanUpWeakMap();
        entry = (Entry) this.mWeakMap.put(key, new Entry(key, value, this.mQueue));
        return entry == null ? null : entry.get();
    }

    public synchronized V get(K key) {
        Entry<K, V> entry;
        cleanUpWeakMap();
        entry = (Entry) this.mWeakMap.get(key);
        return entry == null ? null : entry.get();
    }

    public synchronized ArrayList<K> keys() {
        return new ArrayList(this.mWeakMap.keySet());
    }
}
