package de.ecconia.mcserver.multiversion.packets;

public enum PacketsToServer
{
	//Handshake:
	HtSHandshake, //States the destination and intention.
	//Status:
	StSStatusRequest, //Requests the status.
	StSPingRequest, //Requests a ping.
	//Login:
	LtSStart, //Sent to start login.
	LtSEncryptionReponse, //Sent to finish setup of encryption.
	LtSPluginResponse, //Sent to send custom data.
	//Play:
	PtSTeleportConfirm,
	PtSQueryBlockNBT,
	PtSSetDifficulty,
	PtSChatMessage,
	PtSClientStatus,
	PtSClientSettings,
	PtSTabComplete,
	PtSConfirmTransaction,
	PtSEnchantItem,
	PtSClickWindowButton,
	PtSClickWindow,
	PtSCloseWindow,
	PtSPluginMessage,
	PtSEditBook,
	PtSQueryEntityNBT,
	PtSUseEntity,
	PtSKeepAlive,
	PtSLockDifficulty,
	PtSPlayer,
	PtSPlayerPosition,
	PtSPlayerPositionAndLook,
	PtSPlayerLook,
	PtSVehicleMove,
	PtSSteerBoat,
	PtSPickItem,
	PtSCraftReciptRequest,
	PtSPlayerAbilities,
	PtSPlayerDigging,
	PtSEntityAction,
	PtSSteerVehicle,
	PtSReciptBookData,
	PtSNameItem,
	PtSResourcePackStatus,
	PtSAdvancementTab,
	PtSSelectTrade,
	PtSSetBeaconEffect,
	PtSHeldItemChange,
	PtSUpdateCommandBlock,
	PtSUpdateCommandBlockMinecart,
	PtSCreativeInventoryAction,
	PtSUpdateJigsawBlock,
	PtSUpdateStructureBlock,
	PtSUpdateSign,
	PtSAnimation,
	PtSSpectate,
	PtSPlayerBlockPlacement,
	PtSUseItem,
	;
}
