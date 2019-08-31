package de.ecconia.mcserver.features;

import java.util.HashMap;
import java.util.Map;

import de.ecconia.mcserver.Player;
import de.ecconia.mcserver.network.helper.SendHelper;

public class PlayerPosUpdater
{
	//TODO: Scoping.
	private static int entityID = 1;
	private final Map<Player, Integer> playerEID = new HashMap<>();
	
	public void join(Player player)
	{
		for(Player p : playerEID.keySet())
		{
			SendHelper.sendSpawnPlayer(player, player.getIdConverter(), playerEID.get(p), p.getUUID(), 0D, 64D, 0D, (byte) 0, (byte) 0, new byte[] {(byte) 0xff});
		}
		
		int eid = entityID++;
		
		for(Player p : playerEID.keySet())
		{
			SendHelper.sendSpawnPlayer(p, p.getIdConverter(), eid, player.getUUID(), 0D, 64D, 0D, (byte) 0, (byte) 0, new byte[] {(byte) 0xff});
		}
		
		playerEID.put(player, eid);
	}
	
	public void leave(Player player)
	{
		int eid = playerEID.remove(player);
		for(Player p : playerEID.keySet())
		{
			SendHelper.sendRemoveEntities(p, p.getIdConverter(), eid);
		}
	}
	
	public void update(Player player, double x, double y, double z, float yaw, float pitch, boolean onGround)
	{
		Integer eid = playerEID.get(player);
		if(eid != null)
		{
			for(Player p : playerEID.keySet())
			{
				if(player != p)
				{
					byte aYaw = angle(yaw);
					byte aPitch = angle(pitch);
					SendHelper.sendEntityTeleport(p, p.getIdConverter(), eid, x, y, z, aYaw, aPitch, onGround);
					SendHelper.sendEntityLook(p, p.getIdConverter(), eid, aYaw, aPitch, onGround);
					SendHelper.sendEntityRotation(p, p.getIdConverter(), eid, aYaw);
				}
			}
		}
	}
	
	private static byte angle(float angle)
	{
		angle = angle % 360;
		if(angle < 0)
		{
			angle += 360;
		}
		return (byte) (angle / 360 * 255);
	}
}
