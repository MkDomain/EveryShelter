/*
 * Copyright (c) 2020 Alex Hornyai
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.ahornyai.imageshelter.utils;

import lombok.experimental.UtilityClass;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@UtilityClass
public class AESUtil {
    private static final byte[] INIT_VECTOR = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    public SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);

        return keyGenerator.generateKey();
    }

    public SecretKey getKeyFromString(String str) {
        byte[] decodedKey = Base64.getUrlDecoder().decode(str);

        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    public String getKeyAsString(SecretKey key) {
        return Base64.getUrlEncoder().encodeToString(key.getEncoded());
    }

    public Cipher getEncryptCipher(SecretKey key) throws Exception {
        return getCipher(key, Cipher.ENCRYPT_MODE);
    }

    public Cipher getDecryptCipher(SecretKey key) throws Exception {
        return getCipher(key, Cipher.DECRYPT_MODE);
    }

    private Cipher getCipher(SecretKey key, int mode) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(INIT_VECTOR);
        cipher.init(mode, keySpec, ivSpec);

        return cipher;
    }

}
