package com.gionee.note.common;

class ContainerHelpers {
    static final long[] EMPTY_LONGS = new long[0];
    static final Object[] EMPTY_OBJECTS = new Object[0];

    ContainerHelpers() {
    }

    static int binarySearch(int[] array, int size, int value) {
        int lo = 0;
        int hi = size - 1;
        while (lo <= hi) {
            int i = (lo + hi) >>> 1;
            int midVal = array[i];
            if (midVal < value) {
                lo = i + 1;
            } else if (midVal <= value) {
                return i;
            } else {
                hi = i - 1;
            }
        }
        return lo ^ -1;
    }

    static int binarySearch(long[] array, int size, long value) {
        int lo = 0;
        int hi = size - 1;
        while (lo <= hi) {
            int i = (lo + hi) >>> 1;
            long midVal = array[i];
            if (midVal < value) {
                lo = i + 1;
            } else if (midVal <= value) {
                return i;
            } else {
                hi = i - 1;
            }
        }
        return lo ^ -1;
    }

    static int idealLongArraySize(int need) {
        return idealByteArraySize(need * 8) / 8;
    }

    static int idealByteArraySize(int need) {
        for (int i = 4; i < 32; i++) {
            if (need <= (1 << i) - 12) {
                return (1 << i) - 12;
            }
        }
        return need;
    }
}
