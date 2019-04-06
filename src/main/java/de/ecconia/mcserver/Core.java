package de.ecconia.mcserver;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.ecconia.mcserver.network.helper.packet.PacketBuilder;
import de.ecconia.mcserver.network.tools.encryption.AsyncCryptTools;

public class Core
{
	private final List<Player> players = new ArrayList<>();
	
	private final KeyPair keyPair;
	
	public Core()
	{
		keyPair = AsyncCryptTools.generateKeyPair();
	}
	
	public KeyPair getKeyPair()
	{
		return keyPair;
	}
	
	public void playerJoinedGame(Player player)
	{
		players.add(player);
		
		//Ping thread to keep the connection alive. Should ping every 5 seconds.
		new Thread(() -> {
			while(player.isConnected())
			{
				PacketBuilder builder = new PacketBuilder();
				builder.addCInt(0x21);
				builder.addLong(0); //ID of this ping.
				player.sendPacket(builder.asBytes());
				
				try
				{
					Thread.sleep(5000);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace(System.out);
				}
			}
			player.getConnection().debug("Shutting down ping thread.");
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
		player.sendPacket(builder.asBytes());
		
		builder = new PacketBuilder();
		builder.addCInt(0x32); //Player position and look
		builder.addDouble(0); //X
		builder.addDouble(64); //Y
		builder.addDouble(0); //Z
		builder.addFloat(0); //Yaw (Rotation)
		builder.addFloat(0); //Pitch (Neck)
		builder.addByte(0); //All absolute
		builder.addCInt(0); //Teleport ID
		player.sendPacket(builder.asBytes());
		
		builder = new PacketBuilder();
		builder.addCInt(0x0E); //Chat Packet
		builder.addString("{\"text\":\"Welcome to this custom server, hope ya'll like what ya see!\",\"color\":\"yellow\"}"); //JSON chat message
		builder.addByte(0);
		player.sendPacket(builder.asBytes());
		
		for(int x = 0; x < 16; x++)
		{
			for(int z = 0; z < 16; z++)
			{
				sendChunk(player, x - 8, z - 8);
			}
		}
	}
	
	private static void sendChunk(Player player, int x, int z)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(0x22); //Chunk Packet ID
		builder.addInt(x); //X
		builder.addInt(z); //Z
		builder.addBoolean(true); //Whole chunk
		builder.addCInt(1); //Subchunk map
		
		PacketBuilder chunkData = new PacketBuilder();
		//Set bits per block (global palette)
		int bitsPerBlock = 14;
		chunkData.addByte(bitsPerBlock);
		
		//Create subchunk:
		int[][][] subChunk = new int[16][16][16];
		int block = 1; //skip air
		for(int xi = 0; xi < 16; xi++)
		{
			for(int zi = 0; zi < 16; zi++)
			{
				subChunk[xi][zi][0] = block++; 
			}
		}
		long[] longs = createLongsFromBlockArray(subChunk, bitsPerBlock);
		
		//Write longs:
		chunkData.addCInt(longs.length);
		for(int i = 0; i < longs.length; i++)
		{
			chunkData.addLong(longs[i]);
		}
		
		//Set light level stuff:
		byte[] lightLevel = new byte[2048];
		Arrays.fill(lightLevel, (byte) 255);
		chunkData.addBytes(lightLevel);
		
		//Set sky light (we are in overworld:
		byte[] skylightLevel = new byte[2048];
		Arrays.fill(skylightLevel, (byte) 255);
		chunkData.addBytes(skylightLevel);
		
		//Set biome map:
		int[] biomeMap = new int[256];
		for(int i = 0; i < 256; i++)
		{
			chunkData.addInt(biomeMap[i]);
		}
		
		byte[] chunkDataBytes = chunkData.asBytes();
		
		builder.addCInt(chunkDataBytes.length);
		builder.addBytes(chunkDataBytes);
		builder.addCInt(0); //NBT amount
		player.sendPacket(builder.asBytes());
	}
	
	private static long[] createLongsFromBlockArray(int[][][] blocks, int bitsPerBlock)
	{
		int longAmount = 64 * bitsPerBlock;
		long[] longs = new long[longAmount];
		
		int maxBit = 1 << bitsPerBlock;
		
		long longSetBit = 1;
		int longSetBitNumber = 1;
		int longNumber = 0;
		
		for(int y = 0; y < 16; y++)
		{
			for(int z = 0; z < 16; z++)
			{
				for(int x = 0; x < 16; x++)
				{
					int value = blocks[x][z][y];
					
					for(int cBit = 1; cBit < maxBit; cBit <<= 1)
					{
						if(longSetBitNumber > 64)
						{
							longSetBit = 1;
							longSetBitNumber = 1;
							longNumber++;
						}
						
						if((value & cBit) != 0)
						{
							longs[longNumber] |= longSetBit;
						}
						
						//Shift:
						longSetBit <<= 1;
						longSetBitNumber++;
					}
				}
			}
		}
		
		return longs;
	}
	
	public int getOnlineCount()
	{
		return 0;
	}
	
	public void chat(String message)
	{
		PacketBuilder builder = new PacketBuilder();
		builder.addCInt(0x0E); //Chat Packet
		builder.addString("{\"text\":\"" + message + "\",\"color\":\"white\"}"); //JSON chat message
		builder.addByte(0);
		byte[] bytes = builder.asBytes();
		
		for(Player player : players)
		{
			player.sendPacket(bytes);
		}
	}
}
