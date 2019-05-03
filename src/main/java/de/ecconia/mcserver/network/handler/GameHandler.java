package de.ecconia.mcserver.network.handler;

import de.ecconia.mcserver.Core;
import de.ecconia.mcserver.Player;
import de.ecconia.mcserver.data.Face;
import de.ecconia.mcserver.data.ItemStack;
import de.ecconia.mcserver.data.Position;
import de.ecconia.mcserver.json.JSONObject;
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
			core.broadcast(player.getUsername() + ": " + message, "white");
			if(message.equals("quit"))
			{
				disconnect("You requested to quit.");
			}
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
		{
			cc.debug("[GH] Packet: Player action/digging");
			int status = reader.readCInt();
			Position position = reader.readPosition();
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
		case 0x19:
			cc.debug("[GH] Packet: Entity action");
			break;
		case 0x1E:
			cc.debug("[GH] Packet: Advancement tab");
			break;
		case 0x21:
		{
			int slot = reader.readShort();
			cc.debug("[GH] Packet: Inventory slot: " + slot);
			//TODO: Check valid slot number.
			player.setHotbarSlot(slot);
			break;
		}
		case 0x24:
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
					
					SendHelper.sendChat(player, "Uff, you tried to import an itemstack with NBT data, well oops this server can't parse NBT yet. Bear with it.");
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
		case 0x27:
			cc.debug("[GH] Packet: Animation");
			break;
		case 0x29:
		{
			cc.debug("[GH] Packet: Block Place");
			Position position = reader.readPosition();
			Face face = Face.fromNumber(reader.readCInt());
			int hand = reader.readCInt();
			float xPos = reader.readFloat();
			float yPos = reader.readFloat();
			float zPos = reader.readFloat();
			
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
		case 0x2A:
			cc.debug("[GH] Packet: Use Item");
			break;
		default:
			cc.debug("[GH] [WARNING] Unknown ID 0x" + Integer.toHexString(id) + " Data: " + reader.toString());
		}
	}
	
	private void disconnect(String message)
	{
		PacketBuilder pb = new PacketBuilder();
		pb.addCInt(0x1B); //Disconnect ID
		cc.debug("Disconnecting: " + message);
		JSONObject json = new JSONObject();
		json.put("text", message);
		json.put("color", "red");
		pb.addString(json.printJSON());
		cc.sendAndClose(pb.asBytes());
	}
}
