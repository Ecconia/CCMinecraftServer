package de.ecconia.mcserver.data;

public class Position
{
	private final int x, y, z;
	
	public Position(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public int getZ()
	{
		return z;
	}
	
	@Override
	public String toString()
	{
		return x + ", " + y + ", " + z;
	}
	
	public int getChunkX()
	{
		return toChunkPos(x);
	}
	
	public int getChunkY()
	{
		return y / 16;
	}
	
	public int getChunkZ()
	{
		return toChunkPos(z);
	}
	
	private int toChunkPos(int position)
	{
		if(position < 0)
		{
			position -= 15;
		}
		
		return position / 16;
	}
	
	public boolean isHeightInBounds()
	{
		return y >= 0 && y < 256;
	}
	
	public int getInnerX(int chunk)
	{
		return x - (chunk * 16);
	}
	
	public int getInnerY(int subchunk)
	{
		return y - (subchunk * 16);
	}
	
	public int getInnerZ(int chunk)
	{
		return z - (chunk * 16);
	}
	
	public Position transform(Face face)
	{
		return new Position(x + face.getX(), y + face.getY(), z + face.getZ());
	}
}
