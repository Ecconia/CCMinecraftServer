package de.ecconia.mcserver.network.handler;

public class HandshakeData
{
	private int version;
	private String domain;
	private int port;
	
	public HandshakeData(int version, String domain, int port)
	{
		this.version = version;
		this.domain = domain;
		this.port = port;
	}
	
	public int getTargetVersion()
	{
		return version;
	}
	
	public String getTargetDomain()
	{
		return domain;
	}
	
	public int getTargetPort()
	{
		return port;
	}
	
	@Override
	public String toString()
	{
		return "@'" + domain + ':' + port + "' v" + version;
	}
}
