package de.ecconia.mcserver;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

import de.ecconia.java.json.JSONObject;
import de.ecconia.mcserver.network.helper.SendHelper;
import de.ecconia.mcserver.network.tools.encryption.AsyncCryptTools;
import de.ecconia.mcserver.world.DefaultWorld;

public class Core
{
	private final KeyPair keyPair;
	private final IPLogger ips;
	private final LoginType loginType;
	
	//TODO: Dump default world and add many worlds...
	private final DefaultWorld defaultWorld;
	
	public Core()
	{
		defaultWorld = new DefaultWorld();
		//TODO: Import from some settings system.
		loginType = LoginType.Online;
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
					SendHelper.sendPing(player, player.getIdConverter(), 0);
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
		SendHelper.sendChat(player, player.getIdConverter(), json.printJSON(), SendHelper.chatBox);
		
		broadcast(player.getUsername() + " joined this test-server.", "yellow");
		
		defaultWorld.join(player);
	}
	
	public IPLogger getIps()
	{
		return ips;
	}
	
	public int getOnlineCount()
	{
		return players.size();
	}
	
	public void broadcast(String message, String color)
	{
		JSONObject json = new JSONObject();
		json.put("text", message);
		json.put("color", color);
		
		for(Player player : players.values())
		{
			SendHelper.sendChat(player, player.getIdConverter(), json.printJSON(), SendHelper.chatBox);
		}
	}
	
	//Online players:
	
	private final Map<Integer, Player> players = new HashMap<>();
	
	public void register(Player player, int id)
	{
		players.put(id, player);
	}
	
	public LoginType getLoginType()
	{
		return loginType;
	}
	
	public void dump(int id)
	{
		Player player = players.remove(id);
		if(player != null)
		{
			defaultWorld.leave(player);
			broadcast(player.getUsername() + " left.", "yellow");
		}
	}
}
