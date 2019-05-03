package de.ecconia.mcserver.data;

public enum Face
{
	Up(0, 1, 0),
	Down(0, -1, 0),
	PX(1, 0, 0),
	PZ(0, 0, 1),
	MX(-1, 0, 0),
	MZ(0, 0, -1),
	;
	
	private final int x;
	private final int y;
	private final int z;
	
	private Face(int x, int y, int z)
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
	
	public static Face fromNumber(int id)
	{
		switch(id)
		{
		case 0:
			return Face.Down;
		case 1:
			return Face.Up;
		case 2:
			return Face.MZ;
		case 3:
			return Face.PZ;
		case 4:
			return Face.MX;
		case 5:
			return Face.PX;
		default:
			return null;
		}
	}
}
