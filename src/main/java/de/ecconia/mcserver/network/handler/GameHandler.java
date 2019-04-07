package de.ecconia.mcserver.network.handler;

import de.ecconia.mcserver.Core;
import de.ecconia.mcserver.Player;
import de.ecconia.mcserver.network.ClientConnection;
import de.ecconia.mcserver.network.helper.packet.PacketReader;

public class GameHandler implements Handler
{
	private final Core core;
	private final Player player;
	private final ClientConnection cc;
	
	public GameHandler(Core core, ClientConnection cc, Player player)
	{
		this.cc = cc;
		this.core = core;
		this.player = player;
		
		core.playerJoinedGame(player);
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
			String message = reader.readString();
			cc.debug("[GH] Packet: Chat message: >" + message + "<");
			core.chat(player.getUsername() + ": " + message, "white");
			break;
		case 0x03:
			cc.debug("[GH] Packet: Client action");
			break;
		case 0x04:
			cc.debug("[GH] Packet: Client Settings");
			break;
		case 0x09:
			cc.debug("[GH] Packet: Close window");
			break;
		case 0x10:
			cc.debug("[GH] Packet: Player Position");
			break;
		case 0x0A:
			cc.debug("[GH] Packet: Plugin Message, channel: " + reader.readString());
			break;
		case 0x0E:
			cc.debug("[GH] Packet: Ping response");
			break;
		case 0x11:
			cc.debug("[GH] Packet: Player position and look");
			break;
		case 0x12:
			cc.debug("[GH] Packet: Player Look");
			break;
		case 0x17:
			cc.debug("[GH] Packet: Player Abilities");
			break;
		case 0x18:
			cc.debug("[GH] Packet: Player action");
			break;
		case 0x19:
			cc.debug("[GH] Packet: Entity action");
			break;
		case 0x1E:
			cc.debug("[GH] Packet: Advancement tab");
			break;
		case 0x21:
			cc.debug("[GH] Packet: Hand item change");
			break;
		case 0x24:
			cc.debug("[GH] Packet: Creative inventory action");
			break;
		case 0x27:
			cc.debug("[GH] Packet: Animation");
			break;
		case 0x29:
			cc.debug("[GH] Packet: Block Place");
			break;
		default:
			cc.debug("[GH] [WARNING] Unknown ID 0x" + Integer.toHexString(id) + " Data: " + reader.toString());
		}
	}
}
