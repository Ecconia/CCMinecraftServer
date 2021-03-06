package de.ecconia.mcserver.network.handler;

import de.ecconia.mcserver.Core;
import de.ecconia.mcserver.network.ClientConnection;
import de.ecconia.mcserver.network.helper.packet.PacketReader;

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
			
			//TODO: Handle BungeeCord requests.
			
			HandshakeData data = new HandshakeData(version, domain, port);
			cc.debug("[HH] Received Handshake: " + data);
			
			if(target == 1)
			{
				cc.setHandler(new StatusHandler(core, cc, data));
			}
			else if(target == 2)
			{
				cc.setHandler(new LoginHandler(core, cc, data));
			}
			else
			{
				cc.debug("[HH] Unknown request: " + target);
				cc.close();
			}
			
			if(reader.remaining() > 0)
			{
				cc.debug("WARNING: Handshake packet not fully read! Bytes: " + reader.toString());
			}
		}
		else
		{
			cc.debug("[HH] [WARNING] Unknown ID " + id + " Data: " + reader.toString());
			cc.close();
		}
	}
}
