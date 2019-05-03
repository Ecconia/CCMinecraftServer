package de.ecconia.mcserver.network.helper;

import java.util.Arrays;

import de.ecconia.mcserver.Player;
import de.ecconia.mcserver.network.PacketSender;
import de.ecconia.mcserver.network.helper.packet.PacketBuilder;
import de.ecconia.mcserver.world.Chunk;
import de.ecconia.mcserver.world.SubChunk;

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
	
	public static void sendWorldBorderSetSize(PacketSender sender, double diameter)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(0x3B); //WorldBorder packet
		builder.addCInt(0);
		builder.addDouble(diameter);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendWorldBorderChangeSize(PacketSender sender, double currentDiameter, double newDiameter, long changeDuration)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(0x3B); //WorldBorder packet
		builder.addCInt(1);
		builder.addDouble(currentDiameter);
		builder.addDouble(newDiameter);
		builder.addCLong(changeDuration);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendWorldBorderSetCenter(PacketSender sender, double x, double z)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(0x3B); //WorldBorder packet
		builder.addCInt(2);
		builder.addDouble(x);
		builder.addDouble(z);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendWorldBorderInit(PacketSender sender, double x, double z, double currentDiameter, double newDiameter, long changeDuration, int portalTPBoundary, int warningTime, int warningBlocks)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(0x3B); //WorldBorder packet
		builder.addCInt(3);
		builder.addDouble(x);
		builder.addDouble(z);
		builder.addDouble(currentDiameter);
		builder.addDouble(newDiameter);
		builder.addCLong(changeDuration);
		builder.addCInt(portalTPBoundary);
		builder.addCInt(warningTime);
		builder.addCInt(warningBlocks);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendWorldBorderSetWarningTime(PacketSender sender, int warningTime)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(0x3B); //WorldBorder packet
		builder.addCInt(4);
		builder.addCInt(warningTime);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendWorldBorderSetWarningBlocks(PacketSender sender, int warningBlocks)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(0x3B); //WorldBorder packet
		builder.addCInt(5);
		builder.addCInt(warningBlocks);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendLoadChunk(Player sender, Chunk chunk)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(0x22); //WorldBorder packet
		
		builder.addInt(chunk.getX()); //X
		builder.addInt(chunk.getZ()); //Z
		
		builder.addBoolean(true); //Whole chunk - yes cause load chunk.
		
		SubChunk[] subchunks = chunk.getSubChunks();
		
		{
			int bit = 1;
			int subChunkMap = 0;
			for(int i = 0; i < subchunks.length; i++)
			{
				SubChunk subChunk = subchunks[i];
				subChunk.unDirty();
				if(!subChunk.isEmpty())
				{
					subChunkMap |= bit;
				}
				
				bit <<= 1;
			}
			builder.addCInt(subChunkMap);
		}
		
		{
			PacketBuilder chunkData = new PacketBuilder();
			for(SubChunk subChunk : subchunks)
			{
				if(subChunk.isEmpty())
				{
					continue;
				}
				
				chunkData.addByte(subChunk.getBpb());
				
				long[] longs = subChunk.getData();
				//Write longs:
				chunkData.addCInt(longs.length);
				for(int i = 0; i < longs.length; i++)
				{
					chunkData.addLong(longs[i]);
				}
				
				//Set light level stuff:
				//TODO: Store in chunk
				byte[] lightLevel = new byte[2048];
				Arrays.fill(lightLevel, (byte) 255);
				chunkData.addBytes(lightLevel);
				
				//Set sky light (we are in overworld:
				//TODO: Store in chunk
				byte[] skylightLevel = new byte[2048];
				Arrays.fill(skylightLevel, (byte) 255);
				chunkData.addBytes(skylightLevel);
				
				//Set biome map:
				//TODO: Store in chunk
				int[] biomeMap = new int[256];
				for(int i = 0; i < 256; i++)
				{
					chunkData.addInt(biomeMap[i]);
				}
			}
			
			byte[] chunkDataBytes = chunkData.asBytes();
			
			builder.addCInt(chunkDataBytes.length);
			builder.addBytes(chunkDataBytes);
		}
		
		builder.addCInt(0); //NBT amount
		
		sender.sendPacket(builder.asBytes());
	}
}
