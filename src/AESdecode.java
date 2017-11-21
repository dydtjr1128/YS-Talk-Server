import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


public class AESdecode {
	   private String ips;
	   private Key keySpec;
	   private static AESdecode INSTANCE;
	public static AESdecode getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AESdecode();
		}
		return INSTANCE;
	}

	private AESdecode() {
		String key = "1234567890123456";
        try {
            byte[] keyBytes = new byte[16];
            byte[] b = key.getBytes("UTF-8");
            System.arraycopy(b, 0, keyBytes, 0, keyBytes.length);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            this.ips = key.substring(0, 16);
            this.keySpec = keySpec;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
	}

	  public String Encrypt(String str) {
	        Cipher cipher;
	        try {
	            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	            cipher.init(Cipher.ENCRYPT_MODE, keySpec,
	                    new IvParameterSpec(ips.getBytes()));
	 
	            byte[] encrypted = cipher.doFinal(str.getBytes("UTF-8"));
	            String Str = new String(Base64.encodeBase64(encrypted));
	 
	            return Str;
	        } catch (NoSuchAlgorithmException | NoSuchPaddingException
	                | InvalidKeyException | InvalidAlgorithmParameterException
	                | IllegalBlockSizeException | BadPaddingException
	                | UnsupportedEncodingException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	 
	    public String Decrypt(String str) {
	        try {
	            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	            cipher.init(Cipher.DECRYPT_MODE, keySpec,
	                    new IvParameterSpec(ips.getBytes("UTF-8")));
	 
	            byte[] byteStr = Base64.decodeBase64(str.getBytes());
	            String Str = new String(cipher.doFinal(byteStr), "UTF-8");
	 
	            return Str;
	        } catch (NoSuchAlgorithmException | NoSuchPaddingException
	                | InvalidKeyException | InvalidAlgorithmParameterException
	                | IllegalBlockSizeException | BadPaddingException
	                | UnsupportedEncodingException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	/*
	 * public static String Decrypt(String text, String key) throws Exception {
	 * Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); byte[]
	 * keyBytes= new byte[16]; byte[] b= key.getBytes("UTF-8"); int len=
	 * b.length; if (len > keyBytes.length) len = keyBytes.length;
	 * System.arraycopy(b, 0, keyBytes, 0, len); SecretKeySpec keySpec = new
	 * SecretKeySpec(keyBytes, "AES"); IvParameterSpec ivSpec = new
	 * IvParameterSpec(keyBytes);
	 * cipher.init(Cipher.DECRYPT_MODE,keySpec,ivSpec);
	 * 
	 * BASE64Decoder decoder = new BASE64Decoder(); byte [] results =
	 * cipher.doFinal(decoder.decodeBuffer(text)); return new
	 * String(results,"UTF-8"); }
	 * 
	 * public static String Encrypt(String text, String key) throws Exception {
	 * Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); byte[]
	 * keyBytes= new byte[16]; byte[] b= key.getBytes("UTF-8"); int len=
	 * b.length; if (len > keyBytes.length) len = keyBytes.length;
	 * System.arraycopy(b, 0, keyBytes, 0, len); SecretKeySpec keySpec = new
	 * SecretKeySpec(keyBytes, "AES"); IvParameterSpec ivSpec = new
	 * IvParameterSpec(keyBytes);
	 * cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivSpec);
	 * 
	 * byte[] results = cipher.doFinal(text.getBytes("UTF-8")); BASE64Encoder
	 * encoder = new BASE64Encoder(); return encoder.encode(results); }
	 */
}
