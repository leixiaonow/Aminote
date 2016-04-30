package com.gionee.appupgrade.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class GNDecodeUtils {
    private static final String AES = "AES";
    private static final String AES_CBC_PKCS5Padding = "AES/CBC/PKCS5Padding";
    private static final String CHARSET = "UTF-8";
    private static final String HEX = "0123456789ABCDEF";
    private static final String SEED = "GIONEE2012061900";
    private static final String VIPARA = "0102030405060708";

    public static String encrypt(String seed, String cleartext) throws Exception {
        return toHex(encrypt(getRawKey(seed.getBytes()), cleartext.getBytes(CHARSET)));
    }

    public static String decrypt(String seed, String encrypted) throws Exception {
        return new String(decrypt(getRawKey(seed.getBytes()), toByte(encrypted)), CHARSET);
    }

    public static byte[] getRawKey(byte[] seed) throws Exception {
        return seed;
    }

    public static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        IvParameterSpec zeroIv = new IvParameterSpec(VIPARA.getBytes());
        SecretKeySpec skeySpec = new SecretKeySpec(raw, AES);
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5Padding);
        cipher.init(1, skeySpec, zeroIv);
        return cipher.doFinal(clear);
    }

    public static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        IvParameterSpec zeroIv = new IvParameterSpec(VIPARA.getBytes());
        SecretKeySpec skeySpec = new SecretKeySpec(raw, AES);
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5Padding);
        cipher.init(2, skeySpec, zeroIv);
        return cipher.doFinal(encrypted);
    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(hexString.substring(i * 2, (i * 2) + 2), 16).byteValue();
        }
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null) {
            return "";
        }
        StringBuffer result = new StringBuffer(buf.length * 2);
        for (byte appendHex : buf) {
            appendHex(result, appendHex);
        }
        return result.toString();
    }

    public static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 15)).append(HEX.charAt(b & 15));
    }

    public static String get(String str) {
        if (str == null) {
            str = "";
        }
        try {
            return encrypt(SEED, str);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String decrypt(String encrypted) {
        try {
            return decrypt(SEED, encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void main(String[] args) {
        try {
            String encrypted = get("123655474174521");
            System.out.println("encrypted: " + encrypted);
            System.out.println("decrypt: " + decrypt(encrypted));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
