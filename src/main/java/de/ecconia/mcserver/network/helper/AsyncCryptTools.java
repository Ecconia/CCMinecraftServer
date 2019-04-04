package de.ecconia.mcserver.network.helper;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AsyncCryptTools
{
	public static PublicKey bytesToPublicKey(byte[] pubkeyBytes)
	{
		try
		{
			return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubkeyBytes));
		}
		catch(InvalidKeySpecException | NoSuchAlgorithmException e)
		{
			//Do not capture this, terminate everything.
			throw new FatalException("ERROR: Exception while converting bytes to public key: " + e.getClass().getSimpleName() + " - " + e.getMessage());
		}
	}
	
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
			//Do not capture this, terminate everything.
			throw new FatalException("ERROR: Exception while creating server-hash: " + e.getClass().getSimpleName() + " - " + e.getMessage());
		}
	}
	
	public static byte[] encryptBytes(PublicKey serverPublicKey, byte[] encoded)
	{
		try
		{
			Cipher cipher = Cipher.getInstance(serverPublicKey.getAlgorithm().equals("RSA") ? "RSA/ECB/PKCS1Padding" : "AES/CFB8/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
			return cipher.doFinal(encoded);
		}
		catch(InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e)
		{
			//Do not capture this, terminate everything.
			throw new FatalException("ERROR: Exception while async encrypting data: " + e.getClass().getSimpleName() + " - " + e.getMessage());
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
			e.printStackTrace(System.out);
			throw new Error("Could not generate key pair.");
		}
	}
	
	public static SecretKey secredKeyFromBytes(byte[] bytes)
	{
		return new SecretKeySpec(bytes, "AES");
	}
	
	public static byte[] decrypt(Key key, byte[] bytes)
	{
		try
		{
			Cipher cipher = Cipher.getInstance(key.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(bytes);
		}
		catch(IllegalBlockSizeException e)
		{
			e.printStackTrace(System.out);
		}
		catch(BadPaddingException e)
		{
			e.printStackTrace(System.out);
		}
		catch(InvalidKeyException e)
		{
			e.printStackTrace(System.out);
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace(System.out);
		}
		catch(NoSuchPaddingException e)
		{
			e.printStackTrace(System.out);
		}
		
		System.out.println("Decryption failed.!");
		return null;
	}
}
