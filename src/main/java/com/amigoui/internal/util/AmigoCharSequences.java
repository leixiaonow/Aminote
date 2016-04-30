package com.amigoui.internal.util;

public class AmigoCharSequences {
    public static CharSequence forAsciiBytes(final byte[] bytes) {
        return new CharSequence() {
            public char charAt(int index) {
                return (char) bytes[index];
            }

            public int length() {
                return bytes.length;
            }

            public CharSequence subSequence(int start, int end) {
                return AmigoCharSequences.forAsciiBytes(bytes, start, end);
            }

            public String toString() {
                return new String(bytes);
            }
        };
    }

    public static CharSequence forAsciiBytes(final byte[] bytes, final int start, final int end) {
        validate(start, end, bytes.length);
        return new CharSequence() {
            public char charAt(int index) {
                return (char) bytes[start + index];
            }

            public int length() {
                return end - start;
            }

            public CharSequence subSequence(int newStart, int newEnd) {
                newStart -= start;
                newEnd -= start;
                AmigoCharSequences.validate(newStart, newEnd, length());
                return AmigoCharSequences.forAsciiBytes(bytes, newStart, newEnd);
            }

            public String toString() {
                return new String(bytes, start, length());
            }
        };
    }

    static void validate(int start, int end, int length) {
        if (start < 0) {
            throw new IndexOutOfBoundsException();
        } else if (end < 0) {
            throw new IndexOutOfBoundsException();
        } else if (end > length) {
            throw new IndexOutOfBoundsException();
        } else if (start > end) {
            throw new IndexOutOfBoundsException();
        }
    }

    public static boolean equals(CharSequence a, CharSequence b) {
        if (a.length() != b.length()) {
            return false;
        }
        int length = a.length();
        for (int i = 0; i < length; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static int compareToIgnoreCase(CharSequence me, CharSequence another) {
        int end;
        int myPos;
        int anotherPos;
        int myLen = me.length();
        int anotherLen = another.length();
        if (myLen < anotherLen) {
            end = myLen;
        } else {
            end = anotherLen;
        }
        int anotherPos2 = 0;
        int myPos2 = 0;
        while (myPos2 < end) {
            myPos = myPos2 + 1;
            anotherPos = anotherPos2 + 1;
            int result = Character.toLowerCase(me.charAt(myPos2)) - Character.toLowerCase(another.charAt(anotherPos2));
            if (result != 0) {
                return result;
            }
            anotherPos2 = anotherPos;
            myPos2 = myPos;
        }
        anotherPos = anotherPos2;
        myPos = myPos2;
        return myLen - anotherLen;
    }
}
