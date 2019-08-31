package de.ecconia.mcserver.world;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.ecconia.mcserver.Logger;
import de.ecconia.mcserver.Player;
import de.ecconia.mcserver.data.Position;
import de.ecconia.mcserver.network.helper.SendHelper;

public class DefaultWorld
{
	private final Set<Player> players = new HashSet<>();
	private final XYStorage<Chunk> chunkMap = new XYStorage<>();
	
	public DefaultWorld()
	{
		//Create default world.
		
		for(int x = -8; x < 9; x++)
		{
			for(int z = -8; z < 9; z++)
			{
				Chunk chunk = new Chunk(x, z);
				
				int block = 1;
				for(int ix = 0; ix < 16; ix++)
				{
					for(int iz = 0; iz < 16; iz++)
					{
						chunk.setBlock(ix, 1, iz, block++);
						chunk.setBlock(ix, 0, iz, 33);
					}
				}
				
				chunkMap.put(x, z, chunk);
			}
		}
	}
	
	public void join(Player player)
	{
		player.setWorld(this);
		players.add(player);
		
		//Creative + Overworld + Peaceful
		SendHelper.sendJoinGame(player, player.getIdConverter(), 0, 1, 0, 0, 0, "default", false);
		SendHelper.sendPositionAndLook(player, player.getIdConverter(), 0, 64, 0, 0, 0, (byte) 0, 0);
		
		for(Iterator<Chunk> it = chunkMap.iterator(); it.hasNext();)
		{
			Chunk chunk = it.next();
			SendHelper.sendLoadChunk(player, player.getIdConverter(), chunk);
		}
		
		//TBI: What are all these values doing? I just want a border!
		int borderDist = 210;
		SendHelper.sendWorldBorderInit(player, player.getIdConverter(), 0, 0, borderDist, borderDist, 0, borderDist, 0, 0);
	}
	
	public void destroyBlock(Player player, Position position)
	{
		Chunk chunk = chunkMap.get(position.getChunkX(), position.getChunkZ());
		if(chunk == null)
		{
			Logger.warning("Player managed to edit non existing chunk, at: " + position);
			//No such chunk loaded, ignore.
			return;
		}
		
		if(!position.isHeightInBounds())
		{
			Logger.warning("Player managed to edit outside accepted Height range, at: " + position);
			return;
		}
		
		chunk.setBlock(position, 0);
	}
	
	public void setBlock(Position position, int id)
	{
		Chunk chunk = chunkMap.get(position.getChunkX(), position.getChunkZ());
		if(chunk == null)
		{
			Logger.warning("Player managed to edit non existing chunk, at: " + position);
			//No such chunk loaded, ignore.
			return;
		}
		
		if(!position.isHeightInBounds())
		{
			Logger.warning("Player managed to edit outside accepted Height range, at: " + position);
			return;
		}
		
		chunk.setBlock(position, id);
	}
	
	public void update(Position position)
	{
		Chunk chunk = chunkMap.get(position.getChunkX(), position.getChunkZ());
		for(Player player : players)
		{
			SendHelper.sendLoadChunk(player, player.getIdConverter(), chunk);
		}
	}
}
