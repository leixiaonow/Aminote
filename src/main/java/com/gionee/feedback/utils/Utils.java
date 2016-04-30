package com.gionee.feedback.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.Base64;
import com.gionee.res.Text;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.sql.Date;
import java.text.SimpleDateFormat;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class Utils {
    private static final String ALGORITHM_DES = "DES/ECB/PKCS5Padding";
    private static final String DES_KEY = "gioneerc4-KEY#S9qmsJ*TpEIv}+ChY2v-N5KyXuR^Ln5.>1#k[Qw@9[3A1v/LY`AWB)|Dp/&kM_@]AoOwF6AI%HqV>(;h{33Y2fXiOQt8yMUjB:";
    private static final String TAG = "Utils";

    public static String getSystemTime(Context context) {
        return getFormatTime(System.currentTimeMillis(), context);
    }

    public static String getFormatTime(long time, Context context) {
        return new SimpleDateFormat(context.getString(Text.gn_fb_string_format.getIdentifier(context))).format(new Date(time));
    }

    public static <T> T deepCopy(T src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        new ObjectOutputStream(byteOut).writeObject(src);
        return new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray())).readObject();
    }

    public static String encode(String data) {
        try {
            return encode(DES_KEY, data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @TargetApi(8)
    private static String encode(String key, byte[] data) {
        try {
            Key secretKey = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(key.getBytes("UTF-8")));
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            cipher.init(1, secretKey);
            return Base64.encodeToString(cipher.doFinal(data), 0);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
