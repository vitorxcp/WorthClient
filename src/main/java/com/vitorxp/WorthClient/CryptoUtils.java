package com.vitorxp.WorthClient;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Base64;

public class CryptoUtils {
    private static byte[] obfuscatedKey = new byte[] {0x17, 0x00, 0x11, 0x10, 0x1B, 0x04, 0x13, 0x07,
            0x12, 0x0A, 0x15, 0x1F, 0x12, 0x11, 0x06, 0x0C};

    private static String getKey() {
        byte[] key = new byte[obfuscatedKey.length];
        for (int i = 0; i < obfuscatedKey.length; i++) {
            key[i] = (byte)(obfuscatedKey[i] ^ 0x55);
        }
        return new String(key);
    }

    private static final String KEY = getKey();

    public static String encrypt(String data) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(KEY.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(KEY.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
        return new String(original);
    }

    public static String getJarHash() throws Exception {
        InputStream is = WorthClient.class.getProtectionDomain().getCodeSource().getLocation().openStream();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int read;
        while ((read = is.read(buffer)) > 0) {
            md.update(buffer, 0, read);
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
