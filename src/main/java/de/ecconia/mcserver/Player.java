package de.ecconia.mcserver;

import java.util.UUID;

import de.ecconia.mcserver.data.ItemStack;
import de.ecconia.mcserver.multiversion.IdConverter;
import de.ecconia.mcserver.multiversion.ProtocolLib;
import de.ecconia.mcserver.network.ClientConnection;
import de.ecconia.mcserver.network.PacketSender;
import de.ecconia.mcserver.world.DefaultWorld;

public class Player implements PacketSender
{
	//The core in charge:
	private final Core core;
	//The network adapter, manages the connection:
	private final ClientConnection connection;
	
	//Info, where the player wanted to connect to:
	private final int targetVersion;
	private final IdConverter idConverter;
	private final String targetDomain;
	private final int targetPort;
	
	//Player infos:
	private final UUID uuid;
	private final String username;
	
	private final ItemStack[] hotbar = new ItemStack[9];
	private int hotbarSlot = 0;
	
	public Player(Core core, ClientConnection cc, int targetVersion, String targetDomain, int targetPort, UUID uuid, String username)
	{
		this.core = core;
		this.connection = cc;
		this.targetVersion = targetVersion;
		this.idConverter = ProtocolLib.get(targetVersion);
		this.targetDomain = targetDomain;
		this.targetPort = targetPort;
		this.uuid = uuid;
		this.username = username;
		
		for(int i = 0; i < 9; i++)
		{
			hotbar[i] = new ItemStack(0, 0);
		}
	}
	
	public int getProtocolVersion()
	{
		return targetVersion;
	}
	
	public IdConverter getIdConverter()
	{
		return idConverter;
	}
	
	@Override
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

	//TODO: Following content is implementation specific, should be removed and abstracted.
	
	private DefaultWorld world;
	
	public void setWorld(DefaultWorld world)
	{
		this.world = world;
	}
	
	public DefaultWorld getWorld()
	{
		return world;
	}
	
	//Inventory state:
	
	public void setInventory(int slot, int itemID, int count)
	{
		if(slot >= 36 && slot <= 44)
		{
			hotbar[slot - 36] = new ItemStack(itemID, count);
		}
	}
	
	public ItemStack getHotbar(int slot)
	{
		return hotbar[slot];
	}
	
	public void setHotbarSlot(int hotbarSlot)
	{
		this.hotbarSlot = hotbarSlot;
	}
	
	public int getHotbarSlot()
	{
		return hotbarSlot;
	}
	
	public ItemStack getCurrentItemStack()
	{
		return hotbar[hotbarSlot];
	}
}
