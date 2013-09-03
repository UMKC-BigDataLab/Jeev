package edu.umkc.sce_med.fh504;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

//Source: http://stackoverflow.com/questions/992019/java-256-bit-aes-password-based-encryption

public class AESCrypto {

//	private final static String keyStr="C57K9MOxwfLMln5kA3NIC0Y01fP7tanS";
	private final static String keyStr="U2FsdGVkX1/YFgInQqR9q8Vk/qq9GcqV";

    private KeyGenerator keyGen;
    private static SecretKey key;

	// 8-byte Salt
	public AESCrypto(String k) {
		keyGen=null;
		key=null;
        try {
			keyGen = KeyGenerator.getInstance("AES");
			  key = new SecretKeySpec(keyStr.getBytes("UTF-8"), "AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      
	}

	private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

	public String encrypt(String plainText) throws UnsupportedEncodingException{
		
		InputStream in = new ByteArrayInputStream(plainText.getBytes("UTF-8"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        encrypt(in, out);
        
        return Base64.encodeToString(out.toByteArray(), Base64.NO_PADDING);
		
	}
	
	public String decrypt(String cipherText){
		
		 String plainText = null;
		try {
		byte [] cipherBytes= Base64.decode(cipherText, Base64.NO_PADDING);
		InputStream in = new ByteArrayInputStream(cipherBytes);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
        decrypt(in, out);

       
		
			plainText = new String(out.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return plainText;
	}
	
	
	
	/* now returns the IV that was used */
	static byte[] encrypt(InputStream is, OutputStream os) {
		try {
			AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
		    cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
		    CipherInputStream cis = new CipherInputStream(is, cipher);
		    
			doCopy(cis, os);
			return cipher.getIV();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	static void decrypt(InputStream is,OutputStream os) {
		
			Cipher cipher;
			try {
				cipher = Cipher.getInstance(ALGORITHM);
				IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
				cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
				CipherInputStream cis = new CipherInputStream(is, cipher);
				doCopy(cis, os);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidAlgorithmParameterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


	}

	private static void doCopy(InputStream is, OutputStream os)
			throws IOException {
		try {
			byte[] bytes = new byte[4096];
			int numBytes;
			while ((numBytes = is.read(bytes)) != -1) {
				os.write(bytes, 0, numBytes);
			}
		} finally {
			is.close();
			os.close();
		}
	}

	private static byte[] ivBytes = {(byte) 0xC8, (byte) 0x14A, (byte) 0xD5, (byte) 0x230,(byte) 0x6A7, (byte) 0x7DE, (byte) 0x6FF, (byte) 0xC3,(byte) 0xC9, (byte) 0xCA, (byte) 0xCB, (byte) 0x2ED,(byte) 0x57A, (byte) 0x7B8, (byte) 0x5C7, (byte) 0x2E4 };

}