package com.tanimworld.maya;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Gemini API secret encrypted at rest using an Android Keystore AES key. */
public final class SecretStore {
    private static final String ALIAS = "maya-tanim-aes-v1";
    private static SharedPreferences prefs(Context c) { return c.getSharedPreferences("maya_secure", Context.MODE_PRIVATE); }
    private static SecretKey getKey() throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore"); store.load(null);
        KeyStore.Entry entry = store.getEntry(ALIAS, null);
        if (entry instanceof KeyStore.SecretKeyEntry) return ((KeyStore.SecretKeyEntry)entry).getSecretKey();
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256).build());
        return generator.generateKey();
    }
    public static void setApiKey(Context c, String plaintext) throws Exception {
        if (plaintext == null || plaintext.trim().isEmpty()) { prefs(c).edit().remove("encrypted_api_key").apply(); return; }
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, getKey());
        byte[] encrypted = cipher.doFinal(plaintext.trim().getBytes(StandardCharsets.UTF_8));
        byte[] iv = cipher.getIV(); byte[] packed = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, packed, 0, iv.length);
        System.arraycopy(encrypted, 0, packed, iv.length, encrypted.length);
        prefs(c).edit().putString("encrypted_api_key", Base64.encodeToString(packed, Base64.NO_WRAP)).apply();
    }
    public static String getApiKey(Context c) {
        String encoded = prefs(c).getString("encrypted_api_key", ""); if (encoded.isEmpty()) return "";
        try {
            byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
            if (bytes.length < 29) return "";
            byte[] iv = new byte[12]; byte[] encrypted = new byte[bytes.length - 12];
            System.arraycopy(bytes, 0, iv, 0, 12); System.arraycopy(bytes, 12, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception ignored) { return ""; }
    }
}
