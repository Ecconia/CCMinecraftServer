package de.ecconia.mcserver.multiversion.protocols;

import de.ecconia.mcserver.Logger;
import de.ecconia.mcserver.multiversion.IdConverter;
import de.ecconia.mcserver.multiversion.packets.PacketsToClient;
import de.ecconia.mcserver.multiversion.packets.PacketsToServer;

//Mc: 1.13.2
public class Protocol404 extends IdConverter
{
	@Override
	public int getVersion()
	{
		return 404;
	}
	
	@Override
	public Integer getID(PacketsToClient packet)
	{
		switch(packet)
		{
		case StCStatusResponse:
			return 0x00;
		case StCPingResponse:
			return 0x01;
		
		case LtCDisconnect:
			return 0x00;
		case LtCEncryptionRequest:
			return 0x01;
		case LtCLoginSuccess:
			return 0x02;
		case LtCSetCompression:
			return 0x03;
		case LtCPluginRequest:
			return 0x04;
		
		case PtCSpawnObject:
			return 0x00;
		case PtCSpawnXPOrb:
			return 0x01;
		case PtCSpawnGlobalEntity:
			return 0x02;
		case PtCSpawnMob:
			return 0x03;
		case PtCSpawnPainting:
			return 0x04;
		case PtCSpawnPlayer:
			return 0x05;
		case PtCAnimation:
			return 0x06;
		case PtCStatistics:
			return 0x07;
		case PtCBlockBreakAnimation:
			return 0x08;
		case PtCUpdateBlockEntity:
			return 0x09;
		case PtCBlockAction:
			return 0x0A;
		case PtCBlockChange:
			return 0x0B;
		case PtCBossBar:
			return 0x0C;
		case PtCServerDifficulty:
			return 0x0D;
		case PtCChatMessage:
			return 0x0E;
		case PtCMultiBlockChange:
			return 0x0F;
		case PtCTabComplete:
			return 0x10;
		case PtCDeclareCommands:
			return 0x11;
		case PtCConfirmTransaction:
			return 0x12;
		case PtCCloseWindow:
			return 0x13;
		case PtCOpenWindow:
			return 0x14;
		case PtCWindowItems:
			return 0x15;
		case PtCWindowProperty:
			return 0x16;
		case PtCSetSlot:
			return 0x17;
		case PtCSetCooldown:
			return 0x18;
		case PtCPluginMessage:
			return 0x19;
		case PtCNamedSoundEffect:
			return 0x1A;
		case PtCDisconnect:
			return 0x1B;
		case PtCEntityStatus:
			return 0x1C;
		case PtCNBTQueryResponse:
			return 0x1D;
		case PtCExplosion:
			return 0x1E;
		case PtCUnloadChunk:
			return 0x1F;
		case PtCChangeGameState:
			return 0x20;
		case PtCKeepAlive:
			return 0x21;
		case PtCChunkData:
			return 0x22;
		case PtCEffect:
			return 0x23;
		case PtCParticles:
			return 0x24;
		case PtCJoinGame:
			return 0x25;
		case PtCMapData:
			return 0x26;
		case PtCEntity:
			return 0x27;
		case PtCEntityRelativeMove:
			return 0x28;
		case PtCEntityLookAndRelativeMove:
			return 0x29;
		case PtCEntityLook:
			return 0x2A;
		case PtCVehicleMove:
			return 0x2B;
		case PtCOpenSignEditor:
			return 0x2C;
		case PtCCraftReceiptResponse:
			return 0x2D;
		case PtCPlayerAbilities:
			return 0x2E;
		case PtCCombatEvent:
			return 0x2F;
		case PtCPlayerInfo:
			return 0x30;
		case PtCFacePlayer:
			return 0x31;
		case PtCPlayerPositionAndLook:
			return 0x32;
		case PtCUseBed:
			return 0x33;
		case PtCUnlockRecipes:
			return 0x34;
		case PtCDestroyEntities:
			return 0x35;
		case PtCRemoveEntityEffect:
			return 0x36;
		case PtCResourcePackSend:
			return 0x37;
		case PtCRespawn:
			return 0x38;
		case PtCEntityHeadLook:
			return 0x39;
		case PtCSlectAdvancementTab:
			return 0x3A;
		case PtCWorldBorder:
			return 0x3B;
		case PtCCamera:
			return 0x3C;
		case PtCHeldItemChange:
			return 0x3D;
		case PtCDisplayScoreboard:
			return 0x3E;
		case PtCEntityMetadata:
			return 0x3F;
		case PtCAttachEntity:
			return 0x40;
		case PtCEntityVelocity:
			return 0x41;
		case PtCEntityEquipment:
			return 0x42;
		case PtCSetExperience:
			return 0x43;
		case PtCUpdateHealth:
			return 0x44;
		case PtCScoreboardObjective:
			return 0x45;
		case PtCSetPassengers:
			return 0x46;
		case PtCTeams:
			return 0x47;
		case PtCUpdateScore:
			return 0x48;
		case PtCSpawnPosition:
			return 0x49;
		case PtCTimeUpdate:
			return 0x4A;
		case PtCTitle:
			return 0x4B;
		case PtCStopSound:
			return 0x4C;
		case PtCSoundEffect:
			return 0x4D;
		case PtCPlayerListHeaderAndFooter:
			return 0x4E;
		case PtCCollectItem:
			return 0x4F;
		case PtCEntityTeleport:
			return 0x50;
		case PtCAdvancements:
			return 0x51;
		case PtCEntityProperties:
			return 0x52;
		case PtCEntityEffect:
			return 0x53;
		case PtCDeclareReceipts:
			return 0x54;
		case PtCTags:
			return 0x55;
		default:
			throw new RuntimeException("That packet is not supported by this client... YAY!");
		}
	}
	
	@Override
	public PacketsToServer getStatusPacket(int id)
	{
		switch(id)
		{
		case 0x00:
			return PacketsToServer.StSStatusRequest;
		case 0x01:
			return PacketsToServer.StSPingRequest;
		default:
			Logger.warning("Received packet in Status stage with unknown ID: " + id + " " + getClass().getSimpleName());
			return null;
		}
	}
	
	@Override
	public PacketsToServer getLoginPacket(int id)
	{
		switch(id)
		{
		case 0x00:
			return PacketsToServer.LtSStart;
		case 0x01:
			return PacketsToServer.LtSEncryptionReponse;
		case 0x02:
			return PacketsToServer.LtSPluginResponse;
		default:
			Logger.warning("Received packet in Login stage with unknown ID: " + id + " " + getClass().getSimpleName());
			return null;
		}
	}
	
	@Override
	public PacketsToServer getPlayPacket(int id)
	{
		switch(id)
		{
		case 0x00:
			return PacketsToServer.PtSTeleportConfirm;
		case 0x01:
			return PacketsToServer.PtSQueryBlockNBT;
		case 0x02:
			return PacketsToServer.PtSChatMessage;
		case 0x03:
			return PacketsToServer.PtSClientStatus;
		case 0x04:
			return PacketsToServer.PtSClientSettings;
		case 0x05:
			return PacketsToServer.PtSTabComplete;
		case 0x06:
			return PacketsToServer.PtSConfirmTransaction;
		case 0x07:
			return PacketsToServer.PtSEnchantItem;
		case 0x08:
			return PacketsToServer.PtSClickWindow;
		case 0x09:
			return PacketsToServer.PtSCloseWindow;
		case 0x0A:
			return PacketsToServer.PtSPluginMessage;
		case 0x0B:
			return PacketsToServer.PtSEditBook;
		case 0x0C:
			return PacketsToServer.PtSQueryEntityNBT;
		case 0x0D:
			return PacketsToServer.PtSUseEntity;
		case 0x0E:
			return PacketsToServer.PtSKeepAlive;
		case 0x0F:
			return PacketsToServer.PtSPlayer;
		case 0x10:
			return PacketsToServer.PtSPlayerPosition;
		case 0x11:
			return PacketsToServer.PtSPlayerPositionAndLook;
		case 0x12:
			return PacketsToServer.PtSPlayerLook;
		case 0x13:
			return PacketsToServer.PtSVehicleMove;
		case 0x14:
			return PacketsToServer.PtSSteerBoat;
		case 0x15:
			return PacketsToServer.PtSPickItem;
		case 0x16:
			return PacketsToServer.PtSCraftReciptRequest;
		case 0x17:
			return PacketsToServer.PtSPlayerAbilities;
		case 0x18:
			return PacketsToServer.PtSPlayerDigging;
		case 0x19:
			return PacketsToServer.PtSEntityAction;
		case 0x1A:
			return PacketsToServer.PtSSteerVehicle;
		case 0x1B:
			return PacketsToServer.PtSReciptBookData;
		case 0x1C:
			return PacketsToServer.PtSNameItem;
		case 0x1D:
			return PacketsToServer.PtSResourcePackStatus;
		case 0x1E:
			return PacketsToServer.PtSAdvancementTab;
		case 0x1F:
			return PacketsToServer.PtSSelectTrade;
		case 0x20:
			return PacketsToServer.PtSSetBeaconEffect;
		case 0x21:
			return PacketsToServer.PtSHeldItemChange;
		case 0x22:
			return PacketsToServer.PtSUpdateCommandBlock;
		case 0x23:
			return PacketsToServer.PtSUpdateCommandBlockMinecart;
		case 0x24:
			return PacketsToServer.PtSCreativeInventoryAction;
		case 0x25:
			return PacketsToServer.PtSUpdateStructureBlock;
		case 0x26:
			return PacketsToServer.PtSUpdateSign;
		case 0x27:
			return PacketsToServer.PtSAnimation;
		case 0x28:
			return PacketsToServer.PtSSpectate;
		case 0x29:
			return PacketsToServer.PtSPlayerBlockPlacement;
		case 0x2A:
			return PacketsToServer.PtSUseItem;
		default:
			Logger.warning("Received packet in Play stage with unknown ID: " + id + " " + getClass().getSimpleName());
			return null;
		}
	}
}
