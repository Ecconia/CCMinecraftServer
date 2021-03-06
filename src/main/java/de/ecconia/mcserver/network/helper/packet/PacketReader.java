package de.ecconia.mcserver.network.helper.packet;

import java.util.UUID;

import de.ecconia.mcserver.data.Position;

public class PacketReader
{
	private final byte[] data;
	private int offset = 0;
	
	public PacketReader(byte[] data)
	{
		this.data = data;
	}
	
	public int remaining()
	{
		return data.length - offset;
	}
	
	private int next()
	{
		return data[offset++] & 255;
	}
	
	public byte[] readBytes(int amount)
	{
		byte[] bytes = new byte[amount];
		
		System.arraycopy(data, offset, bytes, 0, amount);
		offset += amount;
		
		return bytes;
	}
	
	//Normal data type readers:
	
	public String readString()
	{
		return new String(readBytes(readCInt()));
	}
	
	public int readByte()
	{
		return next();
	}
	
	public int readShort()
	{
		int i = next();
		i = i << 8;
		i += next();
		return i;
	}
	
	public int readInt()
	{
		int i = next();
		i = i << 8;
		i += next();
		i = i << 8;
		i += next();
		i = i << 8;
		i += next();
		
		return i;
	}
	
	public long readLong()
	{
		long i = next();
		i = i << 8;
		i += next();
		i = i << 8;
		i += next();
		i = i << 8;
		i += next();
		i = i << 8;
		i += next();
		i = i << 8;
		i += next();
		i = i << 8;
		i += next();
		i = i << 8;
		i += next();
		
		return i;
	}
	
	public boolean readBoolean()
	{
		return next() == 1;
	}
	
	public double readDouble()
	{
		return Double.longBitsToDouble(readLong());
	}
	
	public float readFloat()
	{
		return Float.intBitsToFloat(readInt());
	}
	
	//MC custom types:
	
	public int readCInt()
	{
		int ret = 0;
		int iterations = 0;
		
		int read;
		do
		{
			read = next();
			ret |= (read & 127) << iterations++ * 7;
			
			if(iterations > 5)
			{
				throw new RuntimeException("VarInt too big");
			}
		}
		while((read & 128) == 128);
		
		return ret;
	}
	
	public int readUByte()
	{
		return next();
	}
	
	public UUID readUUID()
	{
		return new UUID(readLong(), readLong());
	}
	
	public Position readPosition(boolean newer)
	{
		long data = readLong();
		if(newer)
		{
			return new Position(
				(int) (data >> 38),
				(int) (data & 0xFFF),
				(int) ((data << 26) >> 38));
		}
		else
		{
			return new Position(
				(int) (data >> 38),
				(int) ((data >> 26) & 0xFFF),
				(int) (data << 38 >> 38));
		}
	}
	
	//Debug printing:
	
	private static final int charFix = (-'0' - 10 + 'a');
	
	@Override
	public String toString()
	{
		return format(data);
	}
	
	public static String format(byte[] data)
	{
		StringBuilder builder = new StringBuilder();
		
		for(int i = 0; i < data.length; i++)
		{
			byte b = data[i];
			
			char upper = (char) (((b >> 4) & 15) + '0');
			char lower = (char) ((b & 15) + '0');
			
			if(upper > '9')
			{
				upper += charFix;
			}
			
			if(lower > '9')
			{
				lower += charFix;
			}
			
			builder.append(upper);
			builder.append(lower);
			
			if(i != data.length - 1)
			{
				builder.append(' ');
			}
		}
		
		return builder.toString();
	}
}
