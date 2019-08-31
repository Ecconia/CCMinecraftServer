package de.ecconia.mcserver.multiversion;

import de.ecconia.mcserver.multiversion.protocols.Protocol404;
import de.ecconia.mcserver.multiversion.protocols.Protocol498;

public class ProtocolLib
{
	private static IdConverter[] converters;
	
	static
	{
		converters = new IdConverter[500];
		converters[404] = new Protocol404();
		converters[498] = new Protocol498();
	}
	
	public static IdConverter get(int version)
	{
		if(version < converters.length)
		{
			return converters[version];
		}
		
		return null;
	}
	
	public static IdConverter getOrDefault(int version)
	{
		IdConverter converter = get(version);
		if(converter == null)
		{
			return converters[498];
		}
		
		return converter;
	}
}
