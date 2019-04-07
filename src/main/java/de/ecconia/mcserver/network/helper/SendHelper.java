package de.ecconia.mcserver.network.helper;

import de.ecconia.mcserver.network.PacketSender;
import de.ecconia.mcserver.network.helper.packet.PacketBuilder;

public class SendHelper
{
	//Chat constants:
	public static final int chatBox = 0;
	public static final int systemMessage = 1;
	public static final int hotbar = 2;
	
	public static void sendChat(PacketSender sender, String json, int destination)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(0x0E); //Chat Packet
		builder.addString(json);
		builder.addByte(destination);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendPing(PacketSender sender, long id)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(0x21);
		builder.addLong(id);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendJoinGame(PacketSender sender, int eid, int gm, int dim, int diff, int maxPlayers, String levelType, boolean reducedDebugInfo)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(0x25); //Join game packet
		builder.addInt(eid);
		builder.addByte(gm);
		builder.addInt(dim);
		builder.addByte(diff);
		builder.addByte(maxPlayers); //Useless
		builder.addString("default");
		builder.addBoolean(reducedDebugInfo);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendPositionAndLook(PacketSender sender, double x, double y, double z, float yaw, float pitch, byte mask, int teleportID)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(0x32); //Player position and look
		builder.addDouble(x); //X
		builder.addDouble(y); //Y
		builder.addDouble(z); //Z
		builder.addFloat(yaw); //Yaw (Rotation)
		builder.addFloat(pitch); //Pitch (Neck)
		builder.addByte(mask); //All absolute
		builder.addCInt(teleportID); //Teleport ID
		sender.sendPacket(builder.asBytes());
	}
}
