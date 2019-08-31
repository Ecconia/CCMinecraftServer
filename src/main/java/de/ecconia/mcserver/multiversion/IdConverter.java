package de.ecconia.mcserver.multiversion;

import de.ecconia.mcserver.multiversion.packets.PacketsToClient;
import de.ecconia.mcserver.multiversion.packets.PacketsToServer;

public abstract class IdConverter
{
	public abstract int getVersion();
	
	public abstract Integer getID(PacketsToClient packet);
	
	public abstract PacketsToServer getStatusPacket(int id);
	
	public abstract PacketsToServer getLoginPacket(int id);
	
	public abstract PacketsToServer getPlayPacket(int id);
	
	public static String getName(PacketsToServer packet)
	{
		return getName(packet.name());
	}
	
	public static String getName(PacketsToClient packet)
	{
		return getName(packet.name());
	}
	
	private static String getName(String packet)
	{
		boolean toClient = packet.charAt(2) == 'C';
		String stage = getName(packet.charAt(0));
		String name = packet.substring(3);
		
		return stage + "->" + (toClient ? "Client" : "Server") + ":" + name;
	}
	
	private static String getName(char stage)
	{
		switch(stage)
		{
		case 'H':
			return "Handshake";
		case 'S':
			return "Status";
		case 'L':
			return "Login";
		case 'P':
			return "Play";
		default:
			return "Unknown";
		}
	}
}
