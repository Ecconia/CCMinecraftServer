package de.ecconia.mcserver.network;

import de.ecconia.mcserver.Core;
import de.ecconia.mcserver.network.helper.PacketReader;

public class HandshakeHandler implements Handler
{
	private final Core core;
	private final ClientConnection cc;
	
	public HandshakeHandler(ClientConnection cc, Core core)
	{
		this.cc = cc;
		this.core = core;
	}
	
	@Override
	public void handlePacket(byte[] bytes)
	{
		PacketReader reader = new PacketReader(bytes);
		int id = reader.readCInt();
		
		if(id == 0)
		{
			int version = reader.readCInt();
			String domain = reader.readString();
			int port = reader.readShort();
			int target = reader.readCInt();
			
			cc.debug(" HL: Received Handshake: " + domain + ":" + port + " c-version " + version);
			
			if(target == 1)
			{
				cc.setHandler(new StatusHandler(core, cc));
			}
			else if(target == 2)
			{
				cc.setHandler(new LoginHandler(core, cc));
			}
			else
			{
				cc.debug(" HL: Unknown request: " + target);
				cc.close();
			}
			
			if(reader.remaining() > 0)
			{
				cc.debug("WARNING: Handshake packet not fully read! Bytes: " + reader.toString());
			}
		}
		else
		{
			cc.debug(" HL: Unknown ID " + id + " in Handshake stage. Data: " + reader.toString());
			cc.close();
		}
	}
}
