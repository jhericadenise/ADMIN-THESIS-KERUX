package com.kerux.admin_thesis_kerux.security;

import android.util.Base64;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Security {

    String AES = "AES";

    public String encrypt(String strToEncrypt) throws  Exception{
        SecretKeySpec key = generateKey("kerux");
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(strToEncrypt.getBytes());
        String encryptedValue = Base64.encodeToString(encVal, Base64.DEFAULT);

        return encryptedValue;
    }

    public SecretKeySpec generateKey(String strToEncrypt) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = strToEncrypt.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }

    public String decrypt(String decryptedStr) throws  Exception{
        SecretKeySpec key = generateKey("kerux");
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.decode(decryptedStr, Base64.DEFAULT);
        byte[] decodedVal = c.doFinal(decodedValue);
        String decryptedValue = new String(decodedVal);

        return decryptedValue;
    }
}
