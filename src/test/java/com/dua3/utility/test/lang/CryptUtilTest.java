package com.dua3.utility.test.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.GeneralSecurityException;

import org.junit.jupiter.api.Test;

import com.dua3.utility.lang.CryptUtil;
import com.dua3.utility.text.TextUtil;

public class CryptUtilTest {
    
	private static final int[] KEY_LENGTHS = { 128, 192, 256 };
 
	private String MESSAGES[] = {
			"",
			"secret message",
			System.getProperties().toString()
	};
	
    @Test
    public void testTextEncryption() throws GeneralSecurityException {
    	for (int keyLength: KEY_LENGTHS) {
    		System.out.format("Testing encryption with keylength %d bits%n", keyLength);
    	
    		byte[] key = CryptUtil.generateKey(keyLength);
    		System.out.format("key = %s%n", TextUtil.toHexString(key));
    		
    		for (String message: MESSAGES) {
        		System.out.format("message length = %d%n", message.length());

        		String encrypted = CryptUtil.encrypt(message, key);
            	String decrypted = CryptUtil.decrypt(encrypted, key);
            	
            	System.out.format("cipher  length = %d%n", encrypted.length());

            	assertEquals(message, decrypted);
    		}
    	}
    }

}
