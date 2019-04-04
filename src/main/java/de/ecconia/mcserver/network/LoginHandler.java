package de.ecconia.mcserver.network;

import de.ecconia.mcserver.Core;

public class LoginHandler implements Handler
{
	private final Core core;
	private final ClientConnection cc;
	
	public LoginHandler(Core core, ClientConnection cc)
	{
		this.cc = cc;
		this.core = core;
	}
	
	@Override
	public void handlePacket(byte[] bytes)
	{
	}
}
