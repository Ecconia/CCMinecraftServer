package de.ecconia.mcserver.network;

import de.ecconia.mcserver.Core;
import de.ecconia.mcserver.network.helper.PacketReader;

public class GameHandler implements Handler
{
	private final Core core;
	private final ClientConnection cc;
	
	public GameHandler(Core core, ClientConnection cc)
	{
		this.cc = cc;
		this.core = core;
		
		core.playerJoinedGame(cc);
	}
	
	@Override
	public void handlePacket(byte[] bytes)
	{
		PacketReader reader = new PacketReader(bytes);
		int id = reader.readCInt();
		
		switch(id)
		{
		case 0x00:
			cc.debug("[GH] Packet: Teleport confirm");
			break;
		case 0x02:
			cc.debug("[GH] Packet: Chat message: >" + reader.readString() + "<");
			break;
		case 0x04:
			cc.debug("[GH] Packet: Client Settings");
			break;
		case 0x0A:
			cc.debug("[GH] Packet: Plugin Message");
			System.out.println("Channel: " + reader.readString());
			break;
		case 0x0E:
			cc.debug("[GH] Packet: Ping response");
			break;
		case 0x11:
			cc.debug("[GH] Packet: Player position and look");
			break;
		default:
			cc.debug("[GH] [WARNING] Unknown ID: 0x" + Integer.toHexString(id));
		}
	}
}
