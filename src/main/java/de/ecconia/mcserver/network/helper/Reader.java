package de.ecconia.mcserver.network.helper;

public interface Reader
{
	byte readByte();
	
	byte[] readBytes(int amount);
}
