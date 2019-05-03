package de.ecconia.mcserver.world;

import de.ecconia.mcserver.data.Position;

public class Chunk
{
	private final int x, z;
	private final SubChunk[] subChunks;
	
	public Chunk(int x, int z)
	{
		this.x = x;
		this.z = z;
		
		subChunks = new SubChunk[16];
		for(int i = 0; i < subChunks.length; i++)
		{
			subChunks[i] = new SubChunk();
		}
	}
	
	public void setBlock(Position position, int blockstate)
	{
		int subchunk = position.getChunkY();
		subChunks[subchunk].setBlock(position.getInnerX(x), position.getInnerY(subchunk), position.getInnerZ(z), blockstate);
	}
	
	public void setBlock(int x, int y, int z, int blockstate)
	{
		int subChunk = y / 16;
		int subChunkY = y - offsets[subChunk];
		
		subChunks[subChunk].setBlock(x, subChunkY, z, blockstate);
	}
	
	public int getBlock(int x, int y, int z)
	{
		int subChunk = y / 16;
		int subChunkY = y - offsets[subChunk];
		
		return subChunks[subChunk].getBlock(x, subChunkY, z);
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getZ()
	{
		return z;
	}
	
	public SubChunk[] getSubChunks()
	{
		return subChunks;
	}
	
	//Static stuff:
	
	//TBI: Is an offset table faster or a different operation.
	private static final int[] offsets;
	
	static
	{
		//TBI: Is the order correct?
		offsets = new int[16];
		int offset = 0;
		for(int i = 0; i < 16; i++)
		{
			offsets[i] = offset;
			offset += 16;
		}
	}
}
