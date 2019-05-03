package de.ecconia.mcserver.world;

import de.ecconia.mcserver.Logger;

public class SubChunk
{
	//Compressed data:
	private int bpb;
	private long[] data;
	private int[] dict;
	
	//Cache
	//TBI: Which order, xyz?
	private int[][][] blocks;
	private boolean dirty;
	
	//TODO: Add lock for all changing methods.
	public SubChunk()
	{
	}
	
	//Getters:
	
	public int getBpb()
	{
		return bpb;
	}
	
	public int[] getDict()
	{
		return dict;
	}
	
	public long[] getData()
	{
		return data;
	}
	
	public boolean isEmpty()
	{
		return bpb == 0;
	}
	
	//Block control:
	
	public void setBlock(int x, int y, int z, int blockstate)
	{
		dirty = true;
		
		if(blocks == null)
		{
			blocks = new int[16][16][16];
		}
		
		blocks[x][y][z] = blockstate;
	}
	
	public int getBlock(int x, int y, int z)
	{
		//If cache not created, create it.
		if(blocks == null)
		{
			createCache();
		}
		
		return blocks[x][y][z];
	}
	
	//Cache control:
	
	public void unDirty()
	{
		if(dirty)
		{
			updateCompressed();
			
			boolean empty = true;
			for(long l : data)
			{
				if(l != 0)
				{
					empty = false;
					break;
				}
			}
			
			if(empty)
			{
				//TBI: This ok?
				data = null;
				dict = null;
				bpb = 0;
			}
		}
	}
	
	public void createCache()
	{
		if(blocks == null)
		{
			blocks = new int[16][16][16];
			//TODO: convert.
		}
		else
		{
			//TBI: Ignore, or throw exception depending on usage.
			Logger.warning("Sub-chunk cache was already built.");
		}
	}
	
	public void updateCompressed()
	{
		if(blocks != null)
		{
			//TODO: Make variable.
			bpb = 14;
			int longAmount = 64 * bpb; //Btw this is equal: 16*16*16 * bpb / 64
			data = new long[longAmount];
			
			int maxBit = 1 << bpb;
			
			long longSetBit = 1;
			int longSetBitNumber = 1;
			int longNumber = 0;
			
			for(int y = 0; y < 16; y++)
			{
				for(int z = 0; z < 16; z++)
				{
					for(int x = 0; x < 16; x++)
					{
						int value = blocks[x][y][z];
						
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
								data[longNumber] |= longSetBit;
							}
							
							//Shift:
							longSetBit <<= 1;
							longSetBitNumber++;
						}
					}
				}
			}
			
			dirty = false;
		}
		else
		{
			//TODO: Throw exception, cause there are no blocks to compress.
		}
	}
	
	public void destroyCache()
	{
		//TBI: Check if cache was empty?
		blocks = null;
	}
}
