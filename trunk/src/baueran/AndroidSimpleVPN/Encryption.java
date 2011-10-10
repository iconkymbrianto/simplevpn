package baueran.AndroidSimpleVPN;

import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.net.util.Base64;

public class Encryption 
{
	/*
	 * Converts input into an md5-hashed String representation.
	 */
	
	public static String md5(String input) throws NoSuchAlgorithmException 
	{
		MessageDigest m = MessageDigest.getInstance("MD5");
		byte[] data = input.getBytes(); 
		m.update(data, 0, data.length);
		BigInteger i = new BigInteger(1, m.digest());
		return String.format("%1$032X", i);
	}

	 public static Key generateKey(String password) throws Exception
	 {
		 byte[] salt = new byte[] { 0x7d, 0x60, 0x43, 0x5f, 0x02, (byte) 0xe9, (byte) 0xe0, (byte) 0xae };
		 SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		 KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1024, 256);
		 SecretKey tmp = factory.generateSecret(spec);
		 SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
		 return secret;
	 }

	 // TODO: Static initialisation is probably not a good idea...
	 
	 private static byte[] iv = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		 						 0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00};

	 public static String encrypt(String message, String password) throws Exception 
	 {
		 Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		 cipher.init(Cipher.ENCRYPT_MODE, generateKey(password), new IvParameterSpec(iv));
		 
		 // TODO: If no static key is used, remove last parameter in the above
		 // init call and uncomment the next two lines:
		 // 
		 // AlgorithmParameters params = cipher.getParameters();
		 // iv = params.getParameterSpec(IvParameterSpec.class).getIV();

		 byte[] ciphertext = cipher.doFinal(message.getBytes("UTF-8"));
		 return new String(Base64.encodeBase64(ciphertext));
	 }
		 
	 public static String decrypt(String encryptedRaw, String password) throws Exception 
	 {
		 byte[] encrypted = Base64.decodeBase64(encryptedRaw);
		 Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		 cipher.init(Cipher.DECRYPT_MODE, generateKey(password), new IvParameterSpec(iv));
		 String plaintext = new String(cipher.doFinal(encrypted), "UTF-8");
		 return plaintext;
	 }
}
