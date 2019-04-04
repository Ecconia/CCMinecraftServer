package de.ecconia.mcserver;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import de.ecconia.mcserver.network.ClientConnection;
import de.ecconia.mcserver.network.helper.AsyncCryptTools;
import de.ecconia.mcserver.network.helper.PacketBuilder;

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
	
	public void playerJoinedGame(ClientConnection cc)
	{
		//Ping thread to keep the connection alive. Should ping every 5 seconds.
		new Thread(() -> {
			while(true)
			{
				try
				{
					Thread.sleep(5000);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace(System.out);
				}
				
				PacketBuilder builder = new PacketBuilder();
				builder.addCInt(0x21);
				builder.addLong(0); //ID of this ping.
				cc.sendPacket(builder.asBytes());
			}
		}, "Ping thread for ?").start();
		
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(0x25); //Join game packet
		builder.addInt(0); //Entity ID
		builder.addByte(1); //Creative
		builder.addInt(0); // Overworld
		builder.addByte(0); //Peaceful
		builder.addByte(0); //Max players (useless)
		builder.addString("default"); //Why.... (level type)
		builder.addBoolean(false); //Reduced debug info
		cc.sendPacket(builder.asBytes());
		
		builder = new PacketBuilder();
		builder.addCInt(0x32); //Player position and look
		builder.addDouble(0); //X
		builder.addDouble(64); //Y
		builder.addDouble(0); //Z
		builder.addFloat(0); //Yaw (Rotation)
		builder.addFloat(0); //Pitch (Neck)
		builder.addByte(0); //All absolute
		builder.addCInt(0); //Teleport ID
		cc.sendPacket(builder.asBytes());
		
		builder = new PacketBuilder();
		builder.addCInt(0x0E); //Chat Packet
		builder.addString("{\"text\":\"Welcome to this custom server, hope ya'll like what ya see!\",\"color\":\"yellow\"}"); //JSON chat message
		builder.addByte(0);
		cc.sendPacket(builder.asBytes());
	}
}
