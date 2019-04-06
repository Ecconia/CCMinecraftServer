package de.ecconia.mcserver.network.tools.compression;

@SuppressWarnings("serial")
public class CompressionException extends RuntimeException
{
	public CompressionException(String message, Throwable t)
	{
		super(message, t);
	}
}
