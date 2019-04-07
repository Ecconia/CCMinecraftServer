package de.ecconia.mcserver;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.ecconia.mcserver.json.JSONObject;
import de.ecconia.mcserver.network.helper.SendHelper;
import de.ecconia.mcserver.network.helper.packet.PacketBuilder;
import de.ecconia.mcserver.network.tools.encryption.AsyncCryptTools;

public class Core
{
	private final KeyPair keyPair;
	private final IPLogger ips;
	
	public Core()
	{
		ips = new IPLogger();
		keyPair = AsyncCryptTools.generateKeyPair();
	}
	
	public KeyPair getKeyPair()
	{
		return keyPair;
	}
	
	public void playerJoinedGame(Player player)
	{
		{
			new Thread(() -> {
				ips.addEntry(player.getUUID(), player.getUsername(), player.getConnection().getRemoteIP());
			}).start();
		}
		
		register(player, player.getConnection().getID());
		
		//Ping thread to keep the connection alive. Should ping every 5 seconds.
		Thread pingThread = new Thread(() -> {
			try
			{
				while(!Thread.currentThread().isInterrupted())
				{
					SendHelper.sendPing(player, 0);
					Thread.sleep(5000);
				}
			}
			catch(InterruptedException e)
			{
				//Called intentional.
			}
			
			player.getConnection().debug("Shutting down ping thread.");
		}, "Ping thread for " + player.getUsername());
		player.getConnection().addThread(pingThread);
		pingThread.start();
		
		JSONObject json = new JSONObject();
		json.put("text", "Welcome to this custom server, hope ya'll like what ya see!");
		json.put("color", "yellow");
		SendHelper.sendChat(player, json.printJSON(), SendHelper.chatBox);
		
		broadcast(player.getUsername() + " joined this test-server.", "yellow");
		
		//Creative + Overworld + Peaceful
		SendHelper.sendJoinGame(player, 0, 1, 0, 0, 0, "default", false);
		SendHelper.sendPositionAndLook(player, 0, 64, 0, 0, 0, (byte) 0, 0);
		
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
	
	public IPLogger getIps()
	{
		return ips;
	}
	
	public int getOnlineCount()
	{
		return 0;
	}
	
	public void broadcast(String message, String color)
	{
		JSONObject json = new JSONObject();
		json.put("text", message);
		json.put("color", color);
		
		for(Player player : players.values())
		{
			SendHelper.sendChat(player, json.printJSON(), SendHelper.chatBox);
		}
	}
	
	//Online players:
	
	private final Map<Integer, Player> players = new HashMap<>();
	
	public void register(Player player, int id)
	{
		players.put(id, player);
	}
	
	public void dump(int id)
	{
		Player player = players.remove(id);
		if(player != null)
		{
			broadcast(player.getUsername() + " left.", "yellow");
		}
	}
}
