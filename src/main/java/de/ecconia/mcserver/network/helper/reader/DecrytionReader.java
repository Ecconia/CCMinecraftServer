package de.ecconia.mcserver.network.helper.reader;

import de.ecconia.mcserver.network.tools.encryption.SyncCryptUnit;

public class DecrytionReader implements Reader
{
	private final Reader reader;
	private final SyncCryptUnit crypter;
	
	public DecrytionReader(Reader reader, SyncCryptUnit crypter)
	{
		this.reader = reader;
		this.crypter = crypter;
	}
	
	@Override
	public byte readByte()
	{
		return crypter.decryptByte(reader.readByte());
	}
	
	@Override
	public byte[] readBytes(int amount)
	{
		return crypter.decryptBytes(reader.readBytes(amount));
	}
}
