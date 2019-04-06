package de.ecconia.mcserver.network.helper.packet;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

public class PacketBuilder
{
	private final LinkedList<Byte> bytes = new LinkedList<>();
	
	public void addBytes(byte[] bytes)
	{
		for(byte b : bytes)
		{
			this.bytes.addLast(b);
		}
	}
	
	public byte[] asBytes()
	{
		byte[] bytes = new byte[this.bytes.size()];
		Iterator<Byte> it = this.bytes.iterator();
		for(int i = 0; i < bytes.length; i++)
		{
			byte b = it.next();
			bytes[i] = b;
		}
		
		return bytes;
	}
	
	//########################################################
	//MC Custom:
	
	public void addLocation(int x, int y, int z)
	{
		addLong(
			(((long) x & 0x3FFFFFF) << 38) |
				(((long) y & 0xFFF) << 26) |
				((long) z & 0x3FFFFFF));
	}
	
	public void addCInt(int i)
	{
		while((i & -128) != 0)
		{
			addByte(i & 127 | 128);
			i >>>= 7;
		}
		
		addByte(i);
	}
	
	public void addCLong(long value)
	{
		do
		{
			byte temp = (byte) (value & 0b01111111);
			// Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
			value >>>= 7;
			if(value != 0)
			{
				temp |= 0b10000000;
			}
			addByte(temp);
		}
		while(value != 0);
	}
	
	//########################################################
	//Native types:
	
	public void addString(String s)
	{
		byte[] string = s.getBytes();
		
		addCInt(string.length);
		addBytes(string);
	}
	
	public void addByte(int b)
	{
		bytes.addLast((byte) b);
	}
	
	public void addShort(int i)
	{
		bytes.add((byte) ((i >> 8) & 255));
		bytes.add((byte) (i & 255));
	}
	
	public void addInt(int i)
	{
		bytes.add((byte) ((i >> 24) & 255));
		bytes.add((byte) ((i >> 16) & 255));
		bytes.add((byte) ((i >> 8) & 255));
		bytes.add((byte) (i & 255));
	}
	
	public void addBoolean(boolean b)
	{
		addByte(b ? 1 : 0);
	}
	
	public void addDouble(double d)
	{
		long bits = Double.doubleToRawLongBits(d);
		addLong(bits);
	}
	
	public void addLong(long value)
	{
		bytes.add((byte) ((value >> 56) & 255));
		bytes.add((byte) ((value >> 48) & 255));
		bytes.add((byte) ((value >> 40) & 255));
		bytes.add((byte) ((value >> 32) & 255));
		bytes.add((byte) ((value >> 24) & 255));
		bytes.add((byte) ((value >> 16) & 255));
		bytes.add((byte) ((value >> 8) & 255));
		bytes.add((byte) (value & 255));
	}
	
	public void addFloat(float f)
	{
		int bits = Float.floatToRawIntBits(f);
		addInt(bits);
	}
	
	public void addUUID(UUID uuid)
	{
		addLong(uuid.getMostSignificantBits());
		addLong(uuid.getLeastSignificantBits());
	}
}
