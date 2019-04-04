package de.ecconia.mcserver.network.helper;

public class DecrytionWrapper implements Reader
{
	private final Reader reader;
	private final SyncCryptUnit crypter;
	
	public DecrytionWrapper(Reader reader, SyncCryptUnit crypter)
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
