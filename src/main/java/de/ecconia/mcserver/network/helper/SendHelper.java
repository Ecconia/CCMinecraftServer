package de.ecconia.mcserver.network.helper;

import java.util.Arrays;
import java.util.UUID;

import de.ecconia.java.json.JSONObject;
import de.ecconia.mcserver.multiversion.IdConverter;
import de.ecconia.mcserver.multiversion.packets.PacketsToClient;
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
	
	public static void sendChat(PacketSender sender, IdConverter idc, String json, int destination)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCChatMessage)); //Chat Packet
		builder.addString(json);
		builder.addByte(destination);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendChat(PacketSender sender, IdConverter idc, String text)
	{
		JSONObject json = new JSONObject();
		json.put("text", text);
		json.put("color", "white");
		
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCChatMessage)); //Chat Packet
		builder.addString(json.printJSON());
		builder.addByte(chatBox);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendPing(PacketSender sender, IdConverter idc, long id)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCKeepAlive));
		builder.addLong(id);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendJoinGame(PacketSender sender, IdConverter idc, int eid, int gm, int dim, int diff, int maxPlayers, String levelType, boolean reducedDebugInfo)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCJoinGame)); //Join game packet
		builder.addInt(eid);
		builder.addByte(gm);
		builder.addInt(dim);
		if(idc.getVersion() == 404) //TODO: Replace with correct stable version.
		{
			builder.addByte(diff);
		}
		builder.addByte(maxPlayers); //Useless
		builder.addString("default");
		if(idc.getVersion() == 498) //TODO: Replace with correct stable version.
		{
			builder.addCInt(32); //Render distance (2 to 32) <- LAME.
		}
		builder.addBoolean(reducedDebugInfo);
		sender.sendPacket(builder.asBytes());
		
		//TODO: Set difficulty, if 1.14.4 requires that.
	}
	
	public static void sendPositionAndLook(PacketSender sender, IdConverter idc, double x, double y, double z, float yaw, float pitch, byte mask, int teleportID)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCPlayerPositionAndLook)); //Player position and look
		builder.addDouble(x); //X
		builder.addDouble(y); //Y
		builder.addDouble(z); //Z
		builder.addFloat(yaw); //Yaw (Rotation)
		builder.addFloat(pitch); //Pitch (Neck)
		builder.addByte(mask); //All absolute
		builder.addCInt(teleportID); //Teleport ID
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendWorldBorderSetSize(PacketSender sender, IdConverter idc, double diameter)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCWorldBorder)); //WorldBorder packet
		builder.addCInt(0);
		builder.addDouble(diameter);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendWorldBorderChangeSize(PacketSender sender, IdConverter idc, double currentDiameter, double newDiameter, long changeDuration)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCWorldBorder)); //WorldBorder packet
		builder.addCInt(1);
		builder.addDouble(currentDiameter);
		builder.addDouble(newDiameter);
		builder.addCLong(changeDuration);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendWorldBorderSetCenter(PacketSender sender, IdConverter idc, double x, double z)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCWorldBorder)); //WorldBorder packet
		builder.addCInt(2);
		builder.addDouble(x);
		builder.addDouble(z);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendWorldBorderInit(PacketSender sender, IdConverter idc, double x, double z, double currentDiameter, double newDiameter, long changeDuration, int portalTPBoundary, int warningTime, int warningBlocks)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCWorldBorder)); //WorldBorder packet
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
	
	public static void sendWorldBorderSetWarningTime(PacketSender sender, IdConverter idc, int warningTime)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCWorldBorder)); //WorldBorder packet
		builder.addCInt(4);
		builder.addCInt(warningTime);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendWorldBorderSetWarningBlocks(PacketSender sender, IdConverter idc, int warningBlocks)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCWorldBorder)); //WorldBorder packet
		builder.addCInt(5);
		builder.addCInt(warningBlocks);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendLoadChunk(PacketSender sender, IdConverter idc, Chunk chunk)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCChunkData));
		
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
		
		if(idc.getVersion() == 498)
		{
			//NBT hight value.
			byte[] nbtHight = new byte[] {
				(byte) 10, //Compound
				(byte) 0, //Length of name of root compound
				(byte) 0, //Length of name of root compound
				/*
				(byte)0x0c, //Long array
				0x00, 0x0f, //Length of "MOTION_BLOCKING"
				//"MOTION_BLOCKING"
				(byte) 0x4d,
				(byte) 0x4f,
				(byte) 0x54,
				(byte) 0x49,
				(byte) 0x4f,
				(byte) 0x4e,
				(byte) 0x5f,
				(byte) 0x42,
				(byte) 0x4c,
				(byte) 0x4f,
				(byte) 0x43,
				(byte) 0x4b,
				(byte) 0x49,
				(byte) 0x4e,
				(byte) 0x47,
				0, 0, 0, 0x24, //Array length
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				
				(byte)0x0c, //Long array
				0x00, 0x0d, //Length of "WORLD_SURFACE"
				//"MOTION_BLOCKING":
				0x57, 0x4f, 0x52, 0x4c, 0x44, 0x5f, 0x53, 0x55, 0x52, 0x46, 0x41, 0x43, 0x45, 
				0, 0, 0, 0x24, //Array length
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				*/
				0, //End
			};
			
			builder.addBytes(nbtHight);
			
			{
				PacketBuilder chunkData = new PacketBuilder();
				for(SubChunk subChunk : subchunks)
				{
					if(subChunk.isEmpty())
					{
						continue;
					}
					
					//Set 1 cause not empty.
					chunkData.addShort(1); //Non air blocks...
					
					chunkData.addByte(subChunk.getBpb());
					
					//TODO: Palette.
					
					long[] longs = subChunk.getData();
					//Write longs:
					chunkData.addCInt(longs.length);
					for(int i = 0; i < longs.length; i++)
					{
						chunkData.addLong(longs[i]);
					}
				}
				
				//Set biome map:
				//TODO: Store in chunk
				int[] biomeMap = new int[256];
				for(int i = 0; i < 256; i++)
				{
					chunkData.addInt(biomeMap[i]);
				}
				
				byte[] chunkDataBytes = chunkData.asBytes();
				builder.addCInt(chunkDataBytes.length);
				builder.addBytes(chunkDataBytes);
			}
			
		}
		else
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
	
	public static void sendSpawnPlayer(PacketSender sender, IdConverter idc, int eid, UUID uuid, double x, double y, double z, byte yaw, byte pitch, byte[] metadata)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCSpawnPlayer));
		builder.addCInt(eid);
		builder.addUUID(uuid);
		builder.addDouble(x);
		builder.addDouble(y);
		builder.addDouble(z);
		builder.addByte(yaw);
		builder.addByte(pitch);
		builder.addBytes(metadata);
		sender.sendPacket(builder.asBytes());
	}
	
//	SendHelper.sendProperties(p, eid,
//		new Property("generic.armor", 0D),
//		new Property("generic.maxHealth", 20D),
//		new Property("generic.luck", 0D),
//		new Property("generic.movementSpeed", 0.1D),
//		new Property("generic.armorToughness", 0D),
//		new Property("generic.attackSpeed", 4D));
	public static void sendProperties(PacketSender sender, IdConverter idc, int eid, Property... properties)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCEntityProperties));
		builder.addCInt(eid);
		builder.addInt(properties.length);
		for(Property prop : properties)
		{
			builder.addString(prop.name);
			builder.addDouble(prop.value);
			builder.addCInt(0);
		}
		sender.sendPacket(builder.asBytes());
	}
	
	public static class Property
	{
		private final String name;
		private final double value;
		
		public Property(String name, double value)
		{
			this.name = name;
			this.value = value;
		}
	}
	
//	SendHelper.sendMetadata(p, eid, new byte[] {0x00, 0x00, 0x00, 0x01, 0x01, (byte) 0xac, 0x02, 0x02, 0x05, 0x00, 0x03, 0x07, 0x00, 0x04, 0x07, 0x00, 0x05, 0x07, 0x00, 0x06, 0x00, 0x00, 0x07, 0x02, 0x41, (byte) 0xa0, 0x00, 0x00, 0x08, 0x01, 0x00, 0x09, 0x07, 0x00, 0x0a, 0x01, 0x00, 0x0b, 0x02, 0x00, 0x00, 0x00, 0x00, 0x0c, 0x01, (byte) 0xbd, (byte) 0xd4, (byte) 0xe7, 0x2a, 0x0d, 0x00, 0x00, 0x0e, 0x00, 0x01, 0x0f, 0x0e, 0x0a, 0x00, 0x00, 0x00, 0x10, 0x0e, 0x0a, 0x00, 0x00, 0x00, (byte) 0xff});
	public static void sendMetadata(PacketSender sender, IdConverter idc, int eid, byte[] data)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCEntityMetadata));
		builder.addCInt(eid);
		builder.addBytes(data);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendEntityMoveAndLook(PacketSender sender, IdConverter idc, int eid, int x, int y, int z, byte yaw, byte pitch, boolean onground)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCEntityLookAndRelativeMove));
		builder.addCInt(eid);
		builder.addShort(x);
		builder.addShort(y);
		builder.addShort(z);
		builder.addByte(yaw);
		builder.addByte(pitch);
		builder.addBoolean(onground);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendPlayerListAdd(PacketSender sender, IdConverter idc, UUID uuid, String username, int propertyAmount, int gamemode, int ping, String displayNameJson)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCPlayerInfo));
		builder.addCInt(0);
		builder.addCInt(1);
		builder.addUUID(uuid);
		builder.addString(username);
		builder.addCInt(propertyAmount);
		builder.addCInt(gamemode);
		builder.addCInt(ping);
		boolean bool = displayNameJson != null;
		builder.addBoolean(bool);
		if(bool)
		{
			builder.addString(displayNameJson);
		}
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendPlayerListGamemode(PacketSender sender, IdConverter idc, UUID uuid, int gamemode)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCPlayerInfo));
		builder.addCInt(1);
		builder.addCInt(1);
		builder.addUUID(uuid);
		builder.addCInt(gamemode);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendPlayerListPing(PacketSender sender, IdConverter idc, UUID uuid, int ping)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCPlayerInfo));
		builder.addCInt(2);
		builder.addCInt(1);
		builder.addUUID(uuid);
		builder.addCInt(ping);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendPlayerListDisplayName(PacketSender sender, IdConverter idc, UUID uuid, String displayNameJson)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCPlayerInfo));
		builder.addCInt(3);
		builder.addCInt(1);
		builder.addUUID(uuid);
		boolean bool = displayNameJson != null;
		builder.addBoolean(bool);
		if(bool)
		{
			builder.addString(displayNameJson);
		}
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendPlayerListRemove(PacketSender sender, IdConverter idc, UUID uuid)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCPlayerInfo));
		builder.addCInt(4);
		builder.addCInt(1);
		builder.addUUID(uuid);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendRemoveEntities(PacketSender sender, IdConverter idc, int... eids)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCDestroyEntities));
		builder.addCInt(eids.length);
		for(int eid : eids)
		{
			builder.addCInt(eid);
		}
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendEntityTeleport(PacketSender sender, IdConverter idc, int eid, double x, double y, double z, byte yaw, byte pitch, boolean onGround)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCEntityTeleport));
		builder.addCInt(eid);
		builder.addDouble(x);
		builder.addDouble(y);
		builder.addDouble(z);
		builder.addByte(yaw);
		builder.addByte(pitch);
		builder.addBoolean(onGround);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendEntityLook(PacketSender sender, IdConverter idc, int eid, byte yaw, byte pitch, boolean onGround)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCEntityLook));
		builder.addCInt(eid);
		builder.addByte(yaw);
		builder.addByte(pitch);
		builder.addBoolean(onGround);
		sender.sendPacket(builder.asBytes());
	}
	
	public static void sendEntityRotation(PacketSender sender, IdConverter idc, int eid, byte yaw)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(idc.getID(PacketsToClient.PtCEntityHeadLook));
		builder.addCInt(eid);
		builder.addByte(yaw);
		sender.sendPacket(builder.asBytes());
	}
}
