package de.ecconia.mcserver.network.helper;

@SuppressWarnings("serial")
public class CipherException extends RuntimeException
{
	public CipherException(String message, Throwable t)
	{
		super(message, t);
	}
}
