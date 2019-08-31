package de.ecconia.mcserver.features;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import de.ecconia.mcserver.Player;
import de.ecconia.mcserver.network.helper.SendHelper;

public class GlobalPlayerList
{
	private final Map<UUID, PlayerCount> listedPlayer = new HashMap<>();
	private final ReentrantLock lock = new ReentrantLock();
	
	public void joined(Player player)
	{
		lock.lock();
		
		PlayerCount count = listedPlayer.get(player.getUUID());
		if(count == null)
		{
			//Send this new player, if he is not already online.
			for(PlayerCount p : listedPlayer.values())
			{
				SendHelper.sendPlayerListAdd(p.player, p.player.getIdConverter(), player.getUUID(), player.getUsername(), 0, 0, 0, null);
			}
			
			count = new PlayerCount(player);
			listedPlayer.put(player.getUUID(), count);
		}
		else
		{
			count.inc();
		}
		
		//Send ALL current players to that new client.
		for(PlayerCount p : listedPlayer.values())
		{
			//TODO: Send as one package.
			SendHelper.sendPlayerListAdd(player, player.getIdConverter(), p.player.getUUID(), p.player.getUsername(), 0, 0, 0, null);
		}
		
		lock.unlock();
	}
	
	public void left(Player player)
	{
		lock.lock();
		
		PlayerCount count = listedPlayer.get(player.getUUID());
		if(count.dec() == 0)
		{
			listedPlayer.remove(player.getUUID());
			for(PlayerCount p : listedPlayer.values())
			{
				if(p.player != player)
				{
					SendHelper.sendPlayerListRemove(p.player, p.player.getIdConverter(), player.getUUID());
				}
			}
		}
		
		lock.unlock();
	}
	
	private static class PlayerCount
	{
		private final Player player;
		private int count = 1;
		
		public PlayerCount(Player player)
		{
			this.player = player;
		}
		
		public int dec()
		{
			return --count;
		}
		
		public void inc()
		{
			count++;
		}
	}
}
