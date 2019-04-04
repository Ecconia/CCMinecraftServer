package de.ecconia.mcserver.network;

import de.ecconia.mcserver.Core;
import de.ecconia.mcserver.network.helper.PacketReader;

public class StatusHandler implements Handler
{
	private final Core core;
	private final ClientConnection cc;
	
	private static final String testJSON = "{\"version\":{\"protocol\":404,\"name\":\"Velocity 1.8-1.13.2\"},\"players\":{\"online\":0,\"max\":32,\"sample\":[]},\"description\":{\"text\":\"\",\"extra\":[{\"text\":\"Stym\u0027s Redstone Server \",\"color\":\"gold\"},{\"text\":\"[1.13.2]\",\"color\":\"dark_aqua\"}]},\"favicon\":\"data:image/png;base64,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\u003d\"}";
	
	public StatusHandler(Core core, ClientConnection cc)
	{
		this.cc = cc;
		this.core = core;
	}
	
	@Override
	public void handlePacket(byte[] bytes)
	{
		PacketReader reader = new PacketReader(bytes);
		int id = reader.readCInt();
		
		cc.debug("[SH] " + id + " -> " + reader.toString());
		if(id == 0)
		{
			//TODO: Answer with json.
		}
		else
		{
			cc.debug("[SH] [WARNING] Unknown ID: " + id);
			cc.close();
		}
	}
}
