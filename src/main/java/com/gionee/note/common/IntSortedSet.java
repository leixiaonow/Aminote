package com.gionee.note.common;

public class IntSortedSet {
    static final /* synthetic */ boolean $assertionsDisabled = (!IntSortedSet.class.desiredAssertionStatus());
    private int endPos;
    private int[] items;

    public IntSortedSet() {
        this(10);
    }

    public IntSortedSet(int capacity) {
        this.items = null;
        this.endPos = 0;
        this.items = new int[capacity];
    }

    public void add(int item) {
        this.items = insert(this.items, this.endPos, findInsertIndex(item), item);
        if ($assertionsDisabled || this.endPos <= this.items.length) {
            this.endPos++;
            return;
        }
        throw new AssertionError();
    }

    public int findInsertIndex(int item) {
        return ContainerHelpers.binarySearch(this.items, this.endPos, item) ^ -1;
    }

    public boolean findAndInsert(int item) {
        int index = indexOf(item);
        if (index >= 0) {
            return true;
        }
        insertAtIndex(item, index ^ -1);
        return false;
    }

    public int indexOf(int item) {
        return ContainerHelpers.binarySearch(this.items, this.endPos, item);
    }

    public boolean contains(int item) {
        return ContainerHelpers.binarySearch(this.items, this.endPos, item) >= 0;
    }

    public boolean delete(int item) {
        int index = ContainerHelpers.binarySearch(this.items, this.endPos, item);
        if (index < 0) {
            return false;
        }
        System.arraycopy(this.items, index + 1, this.items, index, this.endPos - (index + 1));
        this.endPos--;
        return true;
    }

    public String toString() {
        if (this.endPos == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder(this.endPos * 6);
        sb.append('[');
        sb.append(this.items[0]);
        for (int i = 1; i < this.endPos; i++) {
            sb.append(", ");
            sb.append(this.items[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    private void insertAtIndex(int item, int index) {
        this.items = insert(this.items, this.endPos, index, item);
        this.endPos++;
    }

    private static int growSize(int currentSize) {
        return currentSize <= 4 ? 8 : currentSize * 2;
    }

    private static int[] insert(int[] array, int currentSize, int index, int element) {
        if (!$assertionsDisabled && currentSize > array.length) {
            throw new AssertionError();
        } else if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        } else {
            int[] newArray = new int[growSize(currentSize)];
            System.arraycopy(array, 0, newArray, 0, index);
            newArray[index] = element;
            System.arraycopy(array, index, newArray, index + 1, array.length - index);
            return newArray;
        }
    }
}
