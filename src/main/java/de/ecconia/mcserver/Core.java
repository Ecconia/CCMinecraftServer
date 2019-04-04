package de.ecconia.mcserver;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import de.ecconia.mcserver.network.ClientConnection;
import de.ecconia.mcserver.network.helper.AsyncCryptTools;

public class Core
{
	private final List<ClientConnection> clientConnections = new ArrayList<>();
	
	private final KeyPair keyPair;
	
	public Core()
	{
		keyPair = AsyncCryptTools.generateKeyPair();
	}
	
	
	
	public KeyPair getKeyPair()
	{
		return keyPair;
	}
	
	public void addClient(ClientConnection clientConnection)
	{
		clientConnections.add(clientConnection);
	}
}
