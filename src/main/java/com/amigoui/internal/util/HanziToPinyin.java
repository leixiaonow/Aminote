package com.amigoui.internal.util;

import android.text.TextUtils;
import android.util.Log;
import com.gionee.appupgrade.common.NewVersion.VersionType;
import com.gionee.note.data.LocalNoteItem;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class HanziToPinyin {
    private static final Collator COLLATOR = Collator.getInstance(Locale.CHINA);
    private static final boolean DEBUG = false;
    private static final String FIRST_PINYIN_UNIHAN = "阿";
    private static final char FIRST_UNIHAN = '㐀';
    public static final byte[][] GN_PINYINS = new byte[][]{new byte[]{(byte) 83, (byte) 72, (byte) 69, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 79, (byte) 85, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 65, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 69, (byte) 82, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 104, (byte) 97, (byte) 110, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 101, (byte) 110, (byte) 103, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 105, (byte) 97, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 97, (byte) 105, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 105, (byte) 110, (byte) 103, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 97, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 69, (byte) 110, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 105, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 97, (byte) 110, (byte) 103, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 111, (byte) 117, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 97, (byte) 105, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 101, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 104, (byte) 97, (byte) 110, (byte) 103, (byte) 0}, new byte[]{(byte) 89, (byte) 105, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 101, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 104, (byte) 117, (byte) 111, (byte) 0, (byte) 0}};
    public static final char[] GN_UNIHANS = new char[]{'沈', '阚', '俞', '钭', '於', '翟', '瞿', '儿', '单', '曾', '贾', '呆', '丁', '大', '嗯', '益', '行', '头', '戴', '客', '长', '宜', '呵', '说'};
    private static final String LAST_PINYIN_UNIHAN = "蓙";
    private static final int LOW_TO_BIG_GAP = -32;
    private static final byte[][] PINYINS = new byte[][]{new byte[]{(byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 65, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 69, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 69, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 79, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 79, (byte) 85, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 85, (byte) 65, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 85, (byte) 65, (byte) 73, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 85, (byte) 65, (byte) 78, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 85, (byte) 65, (byte) 78, (byte) 71}, new byte[]{(byte) 67, (byte) 72, (byte) 85, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 85, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 85, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 73, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 73, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 69, (byte) 82, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 85, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 85, (byte) 65, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 85, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 71, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 77, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 85, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 85, (byte) 65, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 85, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 72, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 79, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 85, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 85, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 85, (byte) 65, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 85, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 75, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 86, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 85, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 73, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 73, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 78, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 73, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 85, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 79, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 85, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 65, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 69, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 69, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 79, (byte) 85, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 85, (byte) 65, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 85, (byte) 65, (byte) 73, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 85, (byte) 65, (byte) 78, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 85, (byte) 65, (byte) 78, (byte) 71}, new byte[]{(byte) 83, (byte) 72, (byte) 85, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 85, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 85, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 79, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 85, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 85, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 65, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 69, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 69, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 79, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 79, (byte) 85, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 85, (byte) 65, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 85, (byte) 65, (byte) 73, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 85, (byte) 65, (byte) 78, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 85, (byte) 65, (byte) 78, (byte) 71}, new byte[]{(byte) 90, (byte) 72, (byte) 85, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 85, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 85, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}};
    public static final String SPLIT_STRING = "`!``!!!`!!`";
    private static final String TAG = "HanziToPinyin";
    private static final char[] UNIHANS = new char[]{'呵', '哎', '安', '肮', '凹', '八', '挀', '扳', '邦', '包', '卑', '奔', '伻', '屄', '边', '标', '憋', '邠', '槟', '癶', '峬', '嚓', '婇', '飡', '仓', '操', '冊', '嵾', '噌', '叉', '钗', '辿', '伥', '抄', '车', '抻', '柽', '吃', '充', '抽', '出', '欻', '揣', '川', '疮', '吹', '杶', '逴', '疵', '匆', '凑', '粗', '汆', '崔', '邨', '搓', '咑', '大', '疸', '当', '刀', '淂', '得', '扥', '灯', '氐', '嗲', '甸', '刁', '爹', '仃', '丟', '东', '唗', '嘟', '偳', '堆', '鐓', '多', '婀', '诶', '奀', '鞥', '而', '发', '帆', '方', '飞', '分', '丰', '覅', '仏', '紑', '伕', '旮', '该', '甘', '冈', '皋', '戈', '給', '根', '庚', '工', '勾', '估', '瓜', '罫', '关', '光', '归', '衮', '呙', '哈', '咳', '顸', '苀', '蒿', '诃', '黒', '拫', '亨', '噷', '吽', '齁', '匢', '花', '怀', '犿', '巟', '灰', '昏', '吙', '丌', '加', '戋', '江', '艽', '阶', '巾', '劤', '冂', '勼', '匊', '娟', '噘', '军', '咔', '开', '刊', '闶', '尻', '匼', '剋', '肯', '阬', '空', '抠', '刳', '夸', '蒯', '宽', '匡', '亏', '坤', '扩', '垃', '来', '兰', '啷', '捞', '仂', '勒', '塄', '刕', '倆', '奁', '良', '撩', '列', '拎', '〇', '溜', '龙', '瞜', '噜', '娈', '畧', '抡', '罗', '呣', '妈', '霾', '嫚', '邙', '猫', '麼', '沒', '门', '甿', '咪', '眠', '喵', '咩', '民', '名', '谬', '摸', '哞', '毪', '拏', '孻', '囡', '囊', '孬', '讷', '馁', '恁', '能', '妮', '拈', '嬢', '鸟', '捏', '您', '宁', '妞', '农', '羺', '奴', '奻', '虐', '挪', '喔', '讴', '趴', '拍', '眅', '乓', '抛', '呸', '喷', '匉', '丕', '偏', '剽', '氕', '姘', '乒', '钋', '剖', '仆', '七', '掐', '千', '呛', '悄', '癿', '侵', '靑', '邛', '丘', '曲', '弮', '缺', '夋', '呥', '穣', '娆', '惹', '人', '扔', '日', '茸', '厹', '如', '堧', '桵', '闰', '若', '仨', '毢', '三', '桒', '掻', '色', '森', '僧', '杀', '筛', '山', '伤', '弰', '奢', '申', '升', '尸', '収', '书', '刷', '摔', '闩', '双', '谁', '吮', '妁', '厶', '忪', '捜', '苏', '狻', '夊', '孙', '唆', '他', '苔', '坍', '铴', '夲', '忑', '熥', '剔', '天', '佻', '帖', '厅', '囲', '偷', '鋀', '湍', '推', '吞', '托', '挖', '歪', '弯', '尪', '危', '塭', '翁', '挝', '兀', '夕', '虾', '仚', '乡', '灱', '些', '心', '星', '凶', '休', '旴', '轩', '疶', '勋', '丫', '恹', '央', '幺', '耶', '一', '欭', '应', '哟', '佣', '优', '扜', '鸢', '曰', '晕', '匝', '災', '糌', '牂', '傮', '则', '贼', '怎', '増', '吒', '捚', '沾', '张', '钊', '蜇', '贞', '争', '之', '中', '州', '朱', '抓', '跩', '专', '妆', '隹', '宒', '卓', '孜', '宗', '邹', '租', '钻', '厜', '尊', '昨'};
    private static final HashMap<Character, Character> dialerKeyMap = new HashMap();
    private static HanziToPinyin sInstance;
    private final boolean mHasChinaCollator;

    public static class Token {
        public static final int LATIN = 1;
        public static final int PINYIN = 2;
        public static final String SEPARATOR = " ";
        public static final int UNKNOWN = 3;
        public String source;
        public String target;
        public int type;

        public Token(int type, String source, String target) {
            this.type = type;
            this.source = source;
            this.target = target;
        }
    }

    private class DialerSearchToken extends Token {
        static final int FIRSTCASE = 0;
        static final int LOWERCASE = 2;
        static final int UPPERCASE = 1;

        private DialerSearchToken() {
        }
    }

    static {
        dialerKeyMap.put(Character.valueOf('0'), Character.valueOf('0'));
        dialerKeyMap.put(Character.valueOf('1'), Character.valueOf('1'));
        dialerKeyMap.put(Character.valueOf('2'), Character.valueOf('2'));
        dialerKeyMap.put(Character.valueOf('a'), Character.valueOf('2'));
        dialerKeyMap.put(Character.valueOf('b'), Character.valueOf('2'));
        dialerKeyMap.put(Character.valueOf('c'), Character.valueOf('2'));
        dialerKeyMap.put(Character.valueOf('A'), Character.valueOf('2'));
        dialerKeyMap.put(Character.valueOf('B'), Character.valueOf('2'));
        dialerKeyMap.put(Character.valueOf('C'), Character.valueOf('2'));
        dialerKeyMap.put(Character.valueOf('3'), Character.valueOf('3'));
        dialerKeyMap.put(Character.valueOf('d'), Character.valueOf('3'));
        dialerKeyMap.put(Character.valueOf('e'), Character.valueOf('3'));
        dialerKeyMap.put(Character.valueOf('f'), Character.valueOf('3'));
        dialerKeyMap.put(Character.valueOf('D'), Character.valueOf('3'));
        dialerKeyMap.put(Character.valueOf('E'), Character.valueOf('3'));
        dialerKeyMap.put(Character.valueOf('F'), Character.valueOf('3'));
        dialerKeyMap.put(Character.valueOf('4'), Character.valueOf('4'));
        dialerKeyMap.put(Character.valueOf('g'), Character.valueOf('4'));
        dialerKeyMap.put(Character.valueOf('h'), Character.valueOf('4'));
        dialerKeyMap.put(Character.valueOf('i'), Character.valueOf('4'));
        dialerKeyMap.put(Character.valueOf('G'), Character.valueOf('4'));
        dialerKeyMap.put(Character.valueOf('H'), Character.valueOf('4'));
        dialerKeyMap.put(Character.valueOf('I'), Character.valueOf('4'));
        dialerKeyMap.put(Character.valueOf('5'), Character.valueOf('5'));
        dialerKeyMap.put(Character.valueOf('j'), Character.valueOf('5'));
        dialerKeyMap.put(Character.valueOf('k'), Character.valueOf('5'));
        dialerKeyMap.put(Character.valueOf('l'), Character.valueOf('5'));
        dialerKeyMap.put(Character.valueOf('J'), Character.valueOf('5'));
        dialerKeyMap.put(Character.valueOf('K'), Character.valueOf('5'));
        dialerKeyMap.put(Character.valueOf('L'), Character.valueOf('5'));
        dialerKeyMap.put(Character.valueOf('6'), Character.valueOf('6'));
        dialerKeyMap.put(Character.valueOf('m'), Character.valueOf('6'));
        dialerKeyMap.put(Character.valueOf('n'), Character.valueOf('6'));
        dialerKeyMap.put(Character.valueOf('o'), Character.valueOf('6'));
        dialerKeyMap.put(Character.valueOf('M'), Character.valueOf('6'));
        dialerKeyMap.put(Character.valueOf('N'), Character.valueOf('6'));
        dialerKeyMap.put(Character.valueOf('O'), Character.valueOf('6'));
        dialerKeyMap.put(Character.valueOf('7'), Character.valueOf('7'));
        dialerKeyMap.put(Character.valueOf('p'), Character.valueOf('7'));
        dialerKeyMap.put(Character.valueOf('q'), Character.valueOf('7'));
        dialerKeyMap.put(Character.valueOf('r'), Character.valueOf('7'));
        dialerKeyMap.put(Character.valueOf('s'), Character.valueOf('7'));
        dialerKeyMap.put(Character.valueOf('P'), Character.valueOf('7'));
        dialerKeyMap.put(Character.valueOf('Q'), Character.valueOf('7'));
        dialerKeyMap.put(Character.valueOf('R'), Character.valueOf('7'));
        dialerKeyMap.put(Character.valueOf('S'), Character.valueOf('7'));
        dialerKeyMap.put(Character.valueOf('8'), Character.valueOf('8'));
        dialerKeyMap.put(Character.valueOf('t'), Character.valueOf('8'));
        dialerKeyMap.put(Character.valueOf('u'), Character.valueOf('8'));
        dialerKeyMap.put(Character.valueOf('v'), Character.valueOf('8'));
        dialerKeyMap.put(Character.valueOf('T'), Character.valueOf('8'));
        dialerKeyMap.put(Character.valueOf('U'), Character.valueOf('8'));
        dialerKeyMap.put(Character.valueOf('V'), Character.valueOf('8'));
        dialerKeyMap.put(Character.valueOf('9'), Character.valueOf('9'));
        dialerKeyMap.put(Character.valueOf('w'), Character.valueOf('9'));
        dialerKeyMap.put(Character.valueOf('x'), Character.valueOf('9'));
        dialerKeyMap.put(Character.valueOf('y'), Character.valueOf('9'));
        dialerKeyMap.put(Character.valueOf('z'), Character.valueOf('9'));
        dialerKeyMap.put(Character.valueOf('W'), Character.valueOf('9'));
        dialerKeyMap.put(Character.valueOf('X'), Character.valueOf('9'));
        dialerKeyMap.put(Character.valueOf('Y'), Character.valueOf('9'));
        dialerKeyMap.put(Character.valueOf('Z'), Character.valueOf('9'));
        dialerKeyMap.put(Character.valueOf('#'), Character.valueOf('#'));
        dialerKeyMap.put(Character.valueOf('*'), Character.valueOf('*'));
        dialerKeyMap.put(Character.valueOf('+'), Character.valueOf('+'));
        dialerKeyMap.put(Character.valueOf(','), Character.valueOf(','));
        dialerKeyMap.put(Character.valueOf(';'), Character.valueOf(';'));
        dialerKeyMap.put(Character.valueOf('`'), Character.valueOf('`'));
        dialerKeyMap.put(Character.valueOf('!'), Character.valueOf('!'));
    }

    protected HanziToPinyin(boolean hasChinaCollator) {
        this.mHasChinaCollator = hasChinaCollator;
    }

    public static HanziToPinyin getInstance() {
        HanziToPinyin hanziToPinyin;
        synchronized (HanziToPinyin.class) {
            if (sInstance != null) {
                hanziToPinyin = sInstance;
            } else {
                Locale[] locale = Collator.getAvailableLocales();
                for (Locale equals : locale) {
                    if (equals.equals(Locale.CHINA)) {
                        sInstance = new HanziToPinyin(true);
                        hanziToPinyin = sInstance;
                        break;
                    }
                }
                Log.w(TAG, "There is no Chinese collator, HanziToPinyin is disabled");
                sInstance = new HanziToPinyin(false);
                hanziToPinyin = sInstance;
            }
        }
        return hanziToPinyin;
    }

    private static boolean doSelfValidation() {
        char lastChar = UNIHANS[0];
        String lastString = Character.toString(lastChar);
        for (char c : UNIHANS) {
            if (lastChar != c) {
                String curString = Character.toString(c);
                if (COLLATOR.compare(lastString, curString) >= 0) {
                    Log.e(TAG, "Internal error in Unihan table. The last string \"" + lastString + "\" is greater than current string \"" + curString + "\".");
                    return false;
                }
                lastString = curString;
            }
        }
        return true;
    }

    private Token getToken(char character, boolean flag) {
        Token token = new Token();
        String letter = Character.toString(character);
        token.source = letter;
        int offset = -1;
        if (character < 'Ā') {
            token.type = 1;
            token.target = letter;
        } else if (character < FIRST_UNIHAN) {
            token.type = 3;
            token.target = letter;
        } else {
            int cmp = COLLATOR.compare(letter, FIRST_PINYIN_UNIHAN);
            if (cmp < 0) {
                token.type = 3;
                token.target = letter;
            } else {
                if (cmp == 0) {
                    token.type = 2;
                    offset = 0;
                } else {
                    cmp = COLLATOR.compare(letter, LAST_PINYIN_UNIHAN);
                    if (cmp > 0) {
                        token.type = 3;
                        token.target = letter;
                    } else if (cmp == 0) {
                        token.type = 2;
                        offset = UNIHANS.length - 1;
                    }
                }
                token.type = 2;
                if (offset < 0) {
                    int begin = 0;
                    int end = UNIHANS.length - 1;
                    while (begin <= end) {
                        offset = (int) ((((long) begin) + ((long) end)) / 2);
                        cmp = COLLATOR.compare(letter, Character.toString(UNIHANS[offset]));
                        if (cmp == 0) {
                            break;
                        } else if (cmp > 0) {
                            begin = offset + 1;
                        } else {
                            end = offset - 1;
                        }
                    }
                }
                if (cmp < 0) {
                    offset--;
                }
                StringBuilder pinyin = new StringBuilder();
                int j = 0;
                while (j < PINYINS[offset].length && PINYINS[offset][j] != (byte) 0) {
                    pinyin.append((char) PINYINS[offset][j]);
                    j++;
                }
                if (flag && pinyin.length() == 1) {
                    pinyin.append('0');
                }
                token.target = pinyin.toString();
            }
        }
        return token;
    }

    public ArrayList<Token> get(String input) {
        ArrayList<Token> tokens = new ArrayList();
        if (this.mHasChinaCollator && !TextUtils.isEmpty(input)) {
            int inputLength = input.length();
            StringBuilder sb = new StringBuilder();
            int tokenType = 1;
            for (int i = 0; i < inputLength; i++) {
                char character = input.charAt(i);
                if (character == ' ') {
                    if (sb.length() > 0) {
                        addToken(sb, tokens, tokenType);
                    }
                } else if (character < 'Ā') {
                    if (tokenType != 1 && sb.length() > 0) {
                        addToken(sb, tokens, tokenType);
                    }
                    tokenType = 1;
                    sb.append(character);
                } else if (character < FIRST_UNIHAN) {
                    if (tokenType != 3 && sb.length() > 0) {
                        addToken(sb, tokens, tokenType);
                    }
                    tokenType = 3;
                    sb.append(character);
                } else {
                    Token t = getToken(character, false);
                    if (t.type == 2) {
                        if (sb.length() > 0) {
                            addToken(sb, tokens, tokenType);
                        }
                        tokens.add(t);
                        tokenType = 2;
                    } else {
                        if (tokenType != t.type && sb.length() > 0) {
                            addToken(sb, tokens, tokenType);
                        }
                        tokenType = t.type;
                        sb.append(character);
                    }
                }
            }
            if (sb.length() > 0) {
                addToken(sb, tokens, tokenType);
            }
        }
        return tokens;
    }

    private void addToken(StringBuilder sb, ArrayList<Token> tokens, int tokenType) {
        String str = sb.toString();
        tokens.add(new Token(tokenType, str, str));
        sb.setLength(0);
    }

    public String getTokensForDialerSearch(String input, StringBuilder offsets) {
        if (offsets == null || input == null || TextUtils.isEmpty(input)) {
            return null;
        }
        StringBuilder subStrSet = new StringBuilder();
        ArrayList<Token> tokens = new ArrayList();
        ArrayList<String> shortSubStrOffset = new ArrayList();
        int inputLength = input.length();
        StringBuilder subString = new StringBuilder();
        StringBuilder subStrOffset = new StringBuilder();
        int tokenType = 1;
        int caseTypePre = 0;
        int mPos = 0;
        for (int i = 0; i < inputLength; i++) {
            char character = input.charAt(i);
            if (character == '-' || character == ',') {
                mPos++;
            } else if (character == ' ') {
                if (subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                addSubString(tokens, shortSubStrOffset, subStrSet, offsets);
                mPos++;
                caseTypePre = 0;
            } else if (character < 'Ā') {
                if (tokenType != 1 && subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                int caseTypeCurr = (character < 'A' || character > 'Z') ? 2 : 1;
                if (caseTypePre == 2 && caseTypeCurr == 1) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                caseTypePre = caseTypeCurr;
                tokenType = 1;
                Character c = Character.valueOf(Character.toUpperCase(character));
                if (c != null) {
                    subString.append(c);
                    subStrOffset.append((char) mPos);
                }
                mPos++;
            } else if (character < '㐀') {
                mPos++;
            } else {
                Token t = getToken(character, false);
                int tokenSize = t.target.length();
                if (t.type == 2) {
                    if (subString.length() > 0) {
                        addToken(subString, tokens, tokenType);
                        addOffsets(subStrOffset, shortSubStrOffset);
                    }
                    tokens.add(t);
                    for (int j = 0; j < tokenSize; j++) {
                        subStrOffset.append((char) mPos);
                    }
                    addOffsets(subStrOffset, shortSubStrOffset);
                    tokenType = 2;
                    caseTypePre = 0;
                    mPos++;
                } else {
                    mPos++;
                }
            }
            if (mPos > 127) {
                break;
            }
        }
        if (subString.length() > 0) {
            addToken(subString, tokens, tokenType);
            addOffsets(subStrOffset, shortSubStrOffset);
        }
        addSubString(tokens, shortSubStrOffset, subStrSet, offsets);
        return subStrSet.toString();
    }

    public String getFristCharsForDialerSearch(String input) {
        if (input == null || TextUtils.isEmpty(input)) {
            return null;
        }
        int i;
        StringBuilder returnStr = new StringBuilder();
        ArrayList<Token> tokens = new ArrayList();
        ArrayList<String> shortSubStrOffset = new ArrayList();
        int inputLength = input.length();
        StringBuilder subString = new StringBuilder();
        StringBuilder subStrOffset = new StringBuilder();
        int tokenType = 1;
        int caseTypePre = 0;
        int mPos = 0;
        for (i = 0; i < inputLength; i++) {
            char character = input.charAt(i);
            if (character == '-' || character == ',') {
                mPos++;
            } else if (character == ' ') {
                if (subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                mPos++;
                caseTypePre = 0;
            } else if (character < 'Ā') {
                if (tokenType != 1 && subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                int caseTypeCurr = (character < 'A' || character > 'Z') ? 2 : 1;
                if (caseTypePre == 2 && caseTypeCurr == 1) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                caseTypePre = caseTypeCurr;
                tokenType = 1;
                Character c = (Character) dialerKeyMap.get(Character.valueOf(character));
                if (c != null) {
                    subString.append(c);
                    subStrOffset.append((char) mPos);
                }
                mPos++;
            } else if (character < '㐀') {
                mPos++;
            } else {
                Token t = getToken(character, true);
                int tokenSize = t.target.length();
                if (t.type == 2) {
                    if (subString.length() > 0) {
                        addToken(subString, tokens, tokenType);
                        addOffsets(subStrOffset, shortSubStrOffset);
                    }
                    tokens.add(t);
                    for (int j = 0; j < tokenSize; j++) {
                        subStrOffset.append((char) mPos);
                    }
                    addOffsets(subStrOffset, shortSubStrOffset);
                    tokenType = 2;
                    caseTypePre = 0;
                    mPos++;
                } else {
                    mPos++;
                }
            }
            if (mPos > 127) {
                break;
            }
        }
        if (subString.length() > 0) {
            addToken(subString, tokens, tokenType);
        }
        Iterator i$ = tokens.iterator();
        while (i$.hasNext()) {
            Token mtoken = (Token) i$.next();
            if (!(mtoken == null || mtoken.target == null || mtoken.target.length() <= 0)) {
                if (mtoken.type == 2) {
                    returnStr.append(mtoken.target.substring(0, 1));
                } else {
                    returnStr.append(mtoken.target);
                }
            }
        }
        returnStr.append(SPLIT_STRING);
        i$ = tokens.iterator();
        while (i$.hasNext()) {
            mtoken = (Token) i$.next();
            if (!(mtoken == null || mtoken.target == null)) {
                if (mtoken.type == 2) {
                    returnStr.append(mtoken.target).append(LocalNoteItem.LABEL_SEPARATOR);
                } else {
                    String targetStr = mtoken.target;
                    StringBuilder tempStr = new StringBuilder();
                    for (i = 0; i < targetStr.length(); i++) {
                        tempStr.append(targetStr.charAt(i)).append(LocalNoteItem.LABEL_SEPARATOR);
                    }
                    if (tempStr.length() > 0) {
                        tempStr.deleteCharAt(tempStr.length() - 1);
                    }
                    returnStr.append(tempStr).append(LocalNoteItem.LABEL_SEPARATOR);
                }
            }
        }
        for (i = 0; i < returnStr.length(); i++) {
            returnStr.replace(i, i + 1, "" + dialerKeyMap.get(Character.valueOf(returnStr.charAt(i))));
        }
        return returnStr.deleteCharAt(returnStr.length() - 1).toString().replaceAll(VersionType.NORMAL_VERSION, "");
    }

    private void addOffsets(StringBuilder sb, ArrayList<String> shortSubStrOffset) {
        shortSubStrOffset.add(sb.toString());
        sb.setLength(0);
    }

    private void addSubString(ArrayList<Token> tokens, ArrayList<String> shortSubStrOffset, StringBuilder subStrSet, StringBuilder offsets) {
        if (tokens != null && !tokens.isEmpty()) {
            int size = tokens.size();
            int len = 0;
            StringBuilder mShortSubStr = new StringBuilder();
            StringBuilder mShortSubStrOffsets = new StringBuilder();
            StringBuilder mShortSubStrSet = new StringBuilder();
            StringBuilder mShortSubStrOffsetsSet = new StringBuilder();
            for (int i = size - 1; i >= 0; i--) {
                String mTempStr = ((Token) tokens.get(i)).target;
                if (i == 0 && mTempStr.length() == 1 && ((Token) tokens.get(0)).type == 1) {
                    mTempStr = mTempStr + VersionType.NORMAL_VERSION;
                }
                len += mTempStr.length();
                String mTempOffset = (String) shortSubStrOffset.get(i);
                if (mShortSubStr.length() > 0) {
                    mShortSubStr.deleteCharAt(0);
                    mShortSubStrOffsets.deleteCharAt(0);
                }
                mShortSubStr.insert(0, mTempStr);
                mShortSubStr.insert(0, (char) len);
                mShortSubStrOffsets.insert(0, mTempOffset);
                mShortSubStrOffsets.insert(0, (char) len);
                mShortSubStrSet.insert(0, mShortSubStr);
                mShortSubStrOffsetsSet.insert(0, mShortSubStrOffsets);
            }
            subStrSet.append(mShortSubStrSet);
            offsets.append(mShortSubStrOffsetsSet);
            tokens.clear();
            shortSubStrOffset.clear();
        }
    }

    public String HanziToPinyinString(String input) {
        if (input == null || TextUtils.isEmpty(input)) {
            return null;
        }
        int i;
        StringBuilder returnStr = new StringBuilder();
        ArrayList<Token> tokens = new ArrayList();
        ArrayList<String> shortSubStrOffset = new ArrayList();
        int inputLength = input.length();
        StringBuilder subString = new StringBuilder();
        StringBuilder subStrOffset = new StringBuilder();
        int tokenType = 1;
        int caseTypePre = 0;
        int mPos = 0;
        for (i = 0; i < inputLength; i++) {
            char character = input.charAt(i);
            if (character == '-' || character == ',') {
                mPos++;
            } else if (character == ' ') {
                if (subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                mPos++;
                caseTypePre = 0;
            } else if (character < 'Ā') {
                if (tokenType != 1 && subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                int caseTypeCurr = (character < 'A' || character > 'Z') ? 2 : 1;
                if (caseTypePre == 2 && caseTypeCurr == 1) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                caseTypePre = caseTypeCurr;
                tokenType = 1;
                Character c = Character.valueOf(Character.toUpperCase(character));
                if (c != null) {
                    subString.append(c);
                    subStrOffset.append((char) mPos);
                }
                mPos++;
            } else if (character < '㐀') {
                mPos++;
            } else {
                Token t = getToken(character, true);
                int tokenSize = t.target.length();
                if (t.type == 2) {
                    if (subString.length() > 0) {
                        addToken(subString, tokens, tokenType);
                        addOffsets(subStrOffset, shortSubStrOffset);
                    }
                    tokens.add(t);
                    for (int j = 0; j < tokenSize; j++) {
                        subStrOffset.append((char) mPos);
                    }
                    addOffsets(subStrOffset, shortSubStrOffset);
                    tokenType = 2;
                    caseTypePre = 0;
                    mPos++;
                } else {
                    mPos++;
                }
            }
            if (mPos > 127) {
                break;
            }
        }
        if (subString.length() > 0) {
            addToken(subString, tokens, tokenType);
        }
        Iterator i$ = tokens.iterator();
        while (i$.hasNext()) {
            Token mtoken = (Token) i$.next();
            if (!(mtoken == null || mtoken.target == null)) {
                if (mtoken.type == 2) {
                    String lowerCaseString = mtoken.target.toLowerCase();
                    char firstChar = lowerCaseString.charAt(0);
                    if (firstChar < 'a' || firstChar > 'z') {
                        returnStr.append(mtoken.target).append(LocalNoteItem.LABEL_SEPARATOR);
                    } else {
                        char[] transChars = lowerCaseString.toCharArray();
                        transChars[0] = (char) (transChars[0] + LOW_TO_BIG_GAP);
                        returnStr.append(new String(transChars)).append(LocalNoteItem.LABEL_SEPARATOR);
                    }
                } else {
                    String targetStr = mtoken.target;
                    StringBuilder tempStr = new StringBuilder();
                    for (i = 0; i < targetStr.length(); i++) {
                        tempStr.append(targetStr.charAt(i)).append(LocalNoteItem.LABEL_SEPARATOR);
                    }
                    if (tempStr.length() > 0) {
                        tempStr.deleteCharAt(tempStr.length() - 1);
                    }
                    returnStr.append(tempStr).append(LocalNoteItem.LABEL_SEPARATOR);
                }
            }
        }
        return returnStr.length() == 0 ? returnStr.toString() : returnStr.deleteCharAt(returnStr.length() - 1).toString().replaceAll(VersionType.NORMAL_VERSION, "");
    }
}
