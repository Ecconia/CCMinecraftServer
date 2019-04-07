package de.ecconia.mcserver;

import java.util.UUID;

import de.ecconia.mcserver.network.ClientConnection;

public class Player
{
	//The core in charge:
	private final Core core;
	//The network adapter, manages the connection:
	private final ClientConnection connection;
	
	//Info, where the player wanted to connect to:
	private final int targetVersion;
	private final String targetDomain;
	private final int targetPort;
	
	//Player infos:
	private final UUID uuid;
	private final String username;
	
	public Player(Core core, ClientConnection cc, int targetVersion, String targetDomain, int targetPort, UUID uuid, String username)
	{
		this.core = core;
		this.connection = cc;
		this.targetVersion = targetVersion;
		this.targetDomain = targetDomain;
		this.targetPort = targetPort;
		this.uuid = uuid;
		this.username = username;
	}
	
	public void sendPacket(byte[] data)
	{
		connection.sendPacket(data);
	}
	
	public boolean isConnected()
	{
		return connection.isConnected();
	}

	public ClientConnection getConnection()
	{
		return connection;
	}

	public String getUsername()
	{
		return username;
	}

	public UUID getUUID()
	{
		return uuid;
	}
}
