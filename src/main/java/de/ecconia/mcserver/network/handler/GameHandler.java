package de.ecconia.mcserver.network.handler;

import de.ecconia.java.json.JSONObject;
import de.ecconia.mcserver.Core;
import de.ecconia.mcserver.Logger;
import de.ecconia.mcserver.Player;
import de.ecconia.mcserver.data.Face;
import de.ecconia.mcserver.data.ItemStack;
import de.ecconia.mcserver.data.Position;
import de.ecconia.mcserver.multiversion.IdConverter;
import de.ecconia.mcserver.multiversion.ProtocolLib;
import de.ecconia.mcserver.multiversion.packets.PacketsToClient;
import de.ecconia.mcserver.multiversion.packets.PacketsToServer;
import de.ecconia.mcserver.network.ClientConnection;
import de.ecconia.mcserver.network.helper.SendHelper;
import de.ecconia.mcserver.network.helper.packet.PacketBuilder;
import de.ecconia.mcserver.network.helper.packet.PacketReader;
import de.ecconia.mcserver.resourcegen.ItemToBlock;

public class GameHandler implements Handler
{
	private final Core core;
	private final Player player;
	private final ClientConnection cc;
	private final IdConverter idConverter;
	
	public GameHandler(Core core, ClientConnection cc, Player player)
	{
		this.cc = cc;
		this.core = core;
		this.player = player;
		this.idConverter = ProtocolLib.get(player.getProtocolVersion());
		
		core.playerJoinedGame(player);
	}
	
	@Override
	public void handlePacket(byte[] bytes)
	{
		PacketReader reader = new PacketReader(bytes);
		
		int id = reader.readCInt();
		PacketsToServer type = idConverter.getPlayPacket(id);
		
		switch(type)
		{
		case PtSTeleportConfirm:
			cc.debug("[GH] Packet: Teleport confirm");
			break;
		case PtSChatMessage:
			String message = reader.readString();
			cc.debug("[GH] Packet: Chat message: >" + message + "<");
			core.broadcast(player.getUsername() + ": " + message, "white");
			if(message.equals("quit"))
			{
				disconnect("You requested to quit.");
			}
			break;
		case PtSClientStatus:
			cc.debug("[GH] Packet: Client action");
			break;
		case PtSClientSettings:
			cc.debug("[GH] Packet: Client Settings");
			break;
		case PtSCloseWindow:
			cc.debug("[GH] Packet: Close window");
			break;
		case PtSPluginMessage:
			cc.debug("[GH] Packet: Plugin Message, channel: " + reader.readString());
			break;
		case PtSKeepAlive:
			cc.debug("[GH] Packet: Ping response");
			break;
		case PtSPlayer:
		{
			cc.debug("[GH] Packet: Player Leviation");
			
			boolean onGround = reader.readBoolean();
			if(reader.remaining() > 0)
			{
				Logger.warning("Player Leviation packet had more bytes to read.");
			}
			
			player.setPosition(onGround);
			break;
		}
		case PtSPlayerPosition:
		{
			cc.debug("[GH] Packet: Player Position");
			
			double x = reader.readDouble();
			double y = reader.readDouble();
			double z = reader.readDouble();
			boolean onGround = reader.readBoolean();
			if(reader.remaining() > 0)
			{
				Logger.warning("Player Position packet had more bytes to read.");
			}
			
			player.setPosition(x, y, z, onGround);
			break;
		}
		case PtSPlayerPositionAndLook:
		{
			cc.debug("[GH] Packet: Player position and look");
			
			double x = reader.readDouble();
			double y = reader.readDouble();
			double z = reader.readDouble();
			float yaw = reader.readFloat();
			float pitch = reader.readFloat();
			boolean onGround = reader.readBoolean();
			if(reader.remaining() > 0)
			{
				Logger.warning("Player Position packet had more bytes to read.");
			}
			
			player.setPosition(x, y, z, yaw, pitch, onGround);
			break;
		}
		case PtSPlayerLook:
			cc.debug("[GH] Packet: Player Look");
			
			float yaw = reader.readFloat();
			float pitch = reader.readFloat();
			boolean onGround = reader.readBoolean();
			if(reader.remaining() > 0)
			{
				Logger.warning("Player Position packet had more bytes to read.");
			}
			
			player.setPosition(yaw, pitch, onGround);
			
			break;
		case PtSPlayerAbilities:
			cc.debug("[GH] Packet: Player Abilities");
			break;
		case PtSPlayerDigging:
		{
			cc.debug("[GH] Packet: Player action/digging");
			int status = reader.readCInt();
			Position position = reader.readPosition(idConverter.getVersion() > 404);
			//TODO: use: Face face = reader.readFace();
			if(status == 0)
			{
				cc.debug("Destroys block: " + position);
				player.getWorld().destroyBlock(player, position);
				player.getWorld().update(position);
			}
			else
			{
				//TODO: Other
			}
			break;
		}
		case PtSEntityAction:
			cc.debug("[GH] Packet: Entity action");
			break;
		case PtSAdvancementTab:
			cc.debug("[GH] Packet: Advancement tab");
			break;
		case PtSHeldItemChange:
		{
			int slot = reader.readShort();
			cc.debug("[GH] Packet: Inventory slot: " + slot);
			//TODO: Check valid slot number.
			player.setHotbarSlot(slot);
			break;
		}
		case PtSCreativeInventoryAction:
		{
			cc.debug("[GH] Packet: Creative inventory action");
			
			int slot = reader.readShort();
			boolean present = reader.readBoolean();
			if(present)
			{
				int itemID = reader.readCInt();
				int count = reader.readByte();
				
				cc.debug("Slot " + slot + " = ID: " + itemID + " (" + ItemToBlock.itemID.get(itemID) + ") * " + count);
				
				int nbtStart = reader.readByte();
				if(nbtStart != 0)
				{
					byte[] nbtData = reader.readBytes(reader.remaining());
					cc.debug("NBT bytes: (" + (nbtData.length + 1) + ") 01 " + PacketReader.format(nbtData));
					
					SendHelper.sendChat(player, player.getIdConverter(), "Uff, you tried to import an itemstack with NBT data, well oops this server can't parse NBT yet. Bear with it.");
				}
				
				player.setInventory(slot, itemID, count);
			}
			else
			{
				cc.debug("Slot " + slot + " = ID: AIR");
				
				if(reader.remaining() > 0)
				{
					System.out.println("PERROR: Remaining bytes in empty nbt item");
				}
			}
			
			break;
		}
		case PtSAnimation:
			cc.debug("[GH] Packet: Animation");
			break;
		case PtSPlayerBlockPlacement:
		{
			cc.debug("[GH] Packet: Block Place");
			Integer hand = null;
			if(idConverter.getVersion() == 498)
			{
				hand = reader.readCInt();
			}
			Position position = reader.readPosition(idConverter.getVersion() > 404);
			Face face = Face.fromNumber(reader.readCInt());
			if(idConverter.getVersion() == 404)
			{
				hand = reader.readCInt();
			}
			float xPos = reader.readFloat();
			float yPos = reader.readFloat();
			float zPos = reader.readFloat();
			if(idConverter.getVersion() == 498)
			{
				//Wether playerhead is inside block - lol
				//boolean insideBlock = 
				reader.readBoolean();
			}
			
			ItemStack placementItem = player.getCurrentItemStack();
			Position placementPosition = position.transform(face);
			
			cc.debug("Placement at position: " + placementPosition + " to " + placementItem);
			cc.debug(" Hand: " + hand + " XYZ: " + xPos + " " + yPos + " " + zPos);
			
			String blockname = ItemToBlock.itemIDToBlock.get(placementItem.getId());
			if(blockname != null)
			{
				player.getWorld().setBlock(placementPosition, ItemToBlock.blockNames.get(blockname));
				player.getWorld().update(placementPosition);
			}
			
			break;
		}
		case PtSUseItem:
			cc.debug("[GH] Packet: Use Item");
			break;
		default:
		{
			cc.debug("[GH] [WARNING] Unknown ID 0x" + Integer.toHexString(id) + " " + (type == null ? "" : "(" + IdConverter.getName(type) + ") ") + "Data: " + reader.toString());
		}
		}
	}
	
	private void disconnect(String message)
	{
		PacketBuilder pb = new PacketBuilder();
		pb.addCInt(idConverter.getID(PacketsToClient.PtCDisconnect)); //Disconnect ID
		cc.debug("Disconnecting: " + message);
		JSONObject json = new JSONObject();
		json.put("text", message);
		json.put("color", "red");
		pb.addString(json.printJSON());
		cc.sendAndClose(pb.asBytes());
	}
}
