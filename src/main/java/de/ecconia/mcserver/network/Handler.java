package de.ecconia.mcserver.network;

public interface Handler
{
	void handlePacket(byte[] bytes);
}
