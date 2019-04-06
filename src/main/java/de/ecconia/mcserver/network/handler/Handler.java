package de.ecconia.mcserver.network.handler;

public interface Handler
{
	void handlePacket(byte[] bytes);
}
