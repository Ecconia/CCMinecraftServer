package de.ecconia.mcserver.network.tools.encryption;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;

public class SyncCryptUnit
{
	private final Cipher decryptCipher;
	private final Cipher encryptCipher;
	
	public SyncCryptUnit(SecretKey key)
	{
		try
		{
			decryptCipher = Cipher.getInstance("AES/CFB8/NoPadding");
			decryptCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(key.getEncoded()));
		}
		catch(InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e)
		{
			throw new CipherException("Could not create decryption cipher.", e);
		}
		
		try
		{
			encryptCipher = Cipher.getInstance("AES/CFB8/NoPadding");
			encryptCipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(key.getEncoded()));
		}
		catch(InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e)
		{
			throw new CipherException("Could not create encryption cipher.", e);
		}
	}
	
	public byte decryptByte(byte b)
	{
		return decryptBytes(new byte[] {b})[0];
	}
	
	public byte[] decryptBytes(byte[] bytes)
	{
		byte o[] = new byte[decryptCipher.getOutputSize(bytes.length)];
		
		try
		{
			decryptCipher.update(bytes, 0, bytes.length, o, 0);
			return o;
		}
		catch(ShortBufferException e)
		{
			throw new CipherException("Could not decrypt bytes.", e);
		}
	}
	
	public byte[] encryptBytes(byte[] bytes)
	{
		byte o[] = new byte[encryptCipher.getOutputSize(bytes.length)];
		
		try
		{
			encryptCipher.update(bytes, 0, bytes.length, o, 0);
			return o;
		}
		catch(ShortBufferException e)
		{
			throw new CipherException("Could not encrypt bytes.", e);
		}
	}
}
