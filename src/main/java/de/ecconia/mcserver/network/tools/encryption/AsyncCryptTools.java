package de.ecconia.mcserver.network.tools.encryption;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AsyncCryptTools
{
	public static String generateHashFromBytes(String serverCode, byte[] firstBytes, byte[] secondBytes)
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(serverCode.getBytes("ISO_8859_1"));
			digest.update(firstBytes);
			digest.update(secondBytes);
			return new BigInteger(digest.digest()).toString(16);
		}
		catch(NoSuchAlgorithmException | UnsupportedEncodingException e)
		{
			throw new CipherException("Could not create server hash.", e);
		}
	}
	
	public static KeyPair generateKeyPair()
	{
		try
		{
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(1024);
			return generator.generateKeyPair();
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new CipherException("Could not generated key pair.", e);
		}
	}
	
	public static SecretKey secredKeyFromBytes(byte[] bytes)
	{
		return new SecretKeySpec(bytes, "AES");
	}
	
	public static byte[] decrypt(Key key, byte[] bytes)
	{
		Cipher cipher;
		try
		{
			cipher = Cipher.getInstance(key.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(bytes);
		}
		catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e)
		{
			throw new CipherException("Could not decrypt message.", e);
		}
	}
}
