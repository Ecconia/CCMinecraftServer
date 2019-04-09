package de.ecconia.mcserver.network.handler;

public class HandshakeData
{
	private final int port;
	private final int version;
	private String domain;
	
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

	public String[] extractBungee()
	{
		//Splitt on the official separator:
		String[] parts = domain.split("\00");
		//Restore the actual hostname, the array should be at least this long.
		domain = parts[0];
		
		String[] arguments = new String[parts.length - 1];
		System.arraycopy(parts, 1, arguments, 0, arguments.length);
		return arguments;
	}
}
