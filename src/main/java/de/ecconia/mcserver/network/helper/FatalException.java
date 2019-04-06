package de.ecconia.mcserver.network.helper;

@SuppressWarnings("serial")
public class FatalException extends RuntimeException
{
	public FatalException(String message)
	{
		super(message);
	}
	
	public FatalException(String message, Throwable e)
	{
		super(message, e);
	}
}
