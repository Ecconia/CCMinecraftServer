package de.ecconia.mcserver.network.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class StreamWrapper implements Reader
{
	private final InputStream is;
	
	public StreamWrapper(InputStream is)
	{
		this.is = is;
	}
	
	@Override
	public byte readByte()
	{
		try
		{
			int i = is.read();
			
			if(i == -1)
			{
				throw new DisconnectException();
			}
			
			return (byte) i;
		}
		catch(IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
	
	@Override
	public byte[] readBytes(int amount)
	{
		try
		{
			byte[] bytes = new byte[amount];
			
			int pointer = 0;
			int remaining = amount;
			
			while(remaining > 0)
			{
				int amountRead = is.read(bytes, pointer, remaining);
				if(amountRead == -1)
				{
					throw new DisconnectException();
				}
				
				remaining -= amountRead;
				pointer += amountRead;
			}
			
			return bytes;
		}
		catch(IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
