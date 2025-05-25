// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CryptUtilTest {

    private static final int[] KEY_LENGTHS = {128, 192, 256};

    private final String[] MESSAGES = {
            "",
            "secret message",
            System.getProperties().toString()
    };

    @Test
    void testTextEncryption() throws GeneralSecurityException {
        for (int keyLength : KEY_LENGTHS) {
            System.out.format("Testing encryption with key length %d bits%n", keyLength);

            byte[] key = CryptUtil.generateKey(keyLength);
            System.out.format("key = %s%n", HexFormat.of().formatHex(key));

            for (String message : MESSAGES) {
                System.out.format("message length = %d%n", message.length());

                String encrypted = CryptUtil.encrypt(key, message);
                String decrypted = CryptUtil.decrypt(key, encrypted);

                System.out.format("cipher  length = %d%n", encrypted.length());

                assertEquals(message, decrypted);
            }
        }
    }

}
