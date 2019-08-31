package de.ecconia.mcserver.network.helper.reader;

import de.ecconia.mcserver.network.tools.compression.Compressor;

public class DecompressionReader implements Reader
{
	private final Reader r;
	private final Compressor compressor;
	
	public DecompressionReader(Reader r, Compressor compressor)
	{
		this.r = r;
		this.compressor = compressor;
	}
	
	@Override
	public byte readByte()
	{
		return r.readByte();
	}
	
	@Override
	public byte[] readBytes(int amount)
	{
		Compressor.IntBytes ret = readCInt(r.readBytes(amount));
		return compressor.uncompress(ret.getInt(), ret.getBytes());
	}
	
	public static Compressor.IntBytes readCInt(byte[] bytes)
	{
		int pointer = 0;
		int value = 0;
		
		byte read;
		do
		{
			read = bytes[pointer];
			value |= (read & 127) << (pointer * 7);
			
			//Increment after calculation.
			if(pointer++ > 5)
			{
				throw new RuntimeException("VarInt too big");
			}
		}
		while((read & 128) == 128);
		
		//Remove first bytes.
		byte[] output = new byte[bytes.length - pointer];
		System.arraycopy(bytes, pointer, output, 0, output.length);
		
		return new Compressor.IntBytes(value, output);
	}
}
