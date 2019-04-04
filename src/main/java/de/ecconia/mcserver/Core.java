package de.ecconia.mcserver;

import java.util.ArrayList;
import java.util.List;

import de.ecconia.mcserver.network.ClientConnection;

public class Core
{
	private final List<ClientConnection> clientConnections = new ArrayList<>();
	
	public Core()
	{
	}
	
	public void addClient(ClientConnection clientConnection)
	{
		clientConnections.add(clientConnection);
	}
}
