package de.ecconia.mcserver.network.helper.reader;

public interface Reader
{
	byte readByte();
	
	byte[] readBytes(int amount);
}
