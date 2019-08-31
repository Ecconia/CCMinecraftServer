package de.ecconia.mcserver.network.handler;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.crypto.SecretKey;

import de.ecconia.java.json.JSONNode;
import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.mcserver.Core;
import de.ecconia.mcserver.LoginType;
import de.ecconia.mcserver.Player;
import de.ecconia.mcserver.multiversion.IdConverter;
import de.ecconia.mcserver.multiversion.ProtocolLib;
import de.ecconia.mcserver.multiversion.packets.PacketsToClient;
import de.ecconia.mcserver.multiversion.packets.PacketsToServer;
import de.ecconia.mcserver.network.ClientConnection;
import de.ecconia.mcserver.network.helper.AuthServer;
import de.ecconia.mcserver.network.helper.packet.PacketBuilder;
import de.ecconia.mcserver.network.helper.packet.PacketReader;
import de.ecconia.mcserver.network.tools.encryption.AsyncCryptTools;

public class LoginHandler implements Handler
{
	private static final Random random = new Random();
	
	private final IdConverter idConverter;
	private final Core core;
	private final ClientConnection cc;
	private final byte[] validation = new byte[4];
	private final HandshakeData data;
	
	private Stage stage;
	private String username;
	
	public LoginHandler(Core core, ClientConnection cc, HandshakeData data)
	{
		this.cc = cc;
		this.core = core;
		this.data = data;
		stage = Stage.Username;
		//Create the validation token, for encryption:
		random.nextBytes(validation);
		
		idConverter = ProtocolLib.get(data.getTargetVersion());
	}
	
	private enum Stage
	{
		None,
		Username,
		Encryption,
		;
	}
	
	@Override
	public void handlePacket(byte[] bytes)
	{
		PacketReader reader = new PacketReader(bytes);
		
		int id = reader.readCInt();
		PacketsToServer type = idConverter.getLoginPacket(id);
		
		if(type == PacketsToServer.LtSStart)
		{
			if(stage != Stage.Username)
			{
				disconnect("Did not expect a username packet again.");
				return;
			}
			
			//Don't accept anything from here on:
			stage = Stage.None;
			
			username = reader.readString();
			cc.debug("username == " + username);
			
			if(reader.remaining() > 0)
			{
				cc.debug("WARNING: Login packet not fully read! Bytes: " + reader.toString());
			}
			
			//TODO: Check if a player may join at this point by using handshake data and username.
			
			if(core.getLoginType() == LoginType.Online)
			{
				PacketBuilder builder = new PacketBuilder();
				builder.addCInt(idConverter.getID(PacketsToClient.LtCEncryptionRequest)); //Encryption request.
				builder.addString(""); //Server code
				byte[] pubkey = core.getKeyPair().getPublic().getEncoded();
				builder.addCInt(pubkey.length);
				builder.addBytes(pubkey);
				builder.addCInt(validation.length);
				builder.addBytes(validation);
				stage = Stage.Encryption;
				cc.sendPacket(builder.asBytes());
			}
			else if(core.getLoginType() == LoginType.Offline)
			{
				UUID uuid = core.getIps().getUUIDforUsername(username);
				if(uuid == null)
				{
					disconnect("You never connected to this server. No entry.");
				}
				else
				{
					login(uuid, username, true);
				}
			}
			else if(core.getLoginType() == LoginType.Bungee)
			{
				String[] arguments = data.extractBungee();
				if(arguments.length == 2 || arguments.length == 3)
				{
					//TODO: Move the bugee-parsing to actual handshake stage. To fix the IP there.
					cc.debug("test: " + arguments[0] + " " + arguments[1]);
					//InetSocketAddress address = new InetSocketAddress(arguments[0], ((InetSocketAddress) b.getSocketAddress()).getPort());
					//UUID uuid = fixUUID(arguments[1]);
				}
				else
				{
					disconnect("You are not allowed to connect this way. Also tell the hoster, he is a noob.");
				}
			}
			else
			{
				throw new RuntimeException("Login type " + core.getLoginType().name() + " not implmented yet.");
			}
		}
		else if(type == PacketsToServer.LtSEncryptionReponse)
		{
			if(stage != Stage.Encryption)
			{
				disconnect("Did not expect an encryption packet.");
				return;
			}
			
			byte[] sharedKeyBytes = reader.readBytes(reader.readCInt());
			byte[] validationResponse = reader.readBytes(reader.readCInt());
			
			byte[] decryptedValidationResponse = AsyncCryptTools.decrypt(core.getKeyPair().getPrivate(), validationResponse);
			if(!Arrays.equals(validation, decryptedValidationResponse))
			{
				disconnect("Encryption validation is not correct.");
				return;
			}
			
			byte[] decryptedSharedKeyBytes = AsyncCryptTools.decrypt(core.getKeyPair().getPrivate(), sharedKeyBytes);
			SecretKey sharedKey = AsyncCryptTools.secredKeyFromBytes(decryptedSharedKeyBytes);
			
			//TBI: Enable encrytion now? Will the future error message be decrypted on the client side?
			cc.enableEncryption(sharedKey);
			
			String serverHash = AsyncCryptTools.generateHashFromBytes("", sharedKey.getEncoded(), core.getKeyPair().getPublic().getEncoded());
			
			//Yeeee nice! At this point we got the UUID! Lets not care further :P
			//TODO: Use this information!
			//It should be validated for sure. Cause else network attacks are possible.
			String json = AuthServer.hasJoin(username, serverHash, cc.getRemoteIP());
			if(json.isEmpty())
			{
				disconnect("Auth server did not approve your login, no details.");
				return;
			}
			
			JSONNode node = JSONParser.parse(json);
			if(node instanceof JSONObject)
			{
				JSONObject userObject = (JSONObject) node;
				Map<String, Object> entries = userObject.getEntries();
				if(entries.containsKey("id") && entries.containsKey("name") && entries.containsKey("properties"))
				{
					String stringUUID = (String) entries.get("id");
					UUID uuid = fixUUID(stringUUID);
					String name = (String) entries.get("name");
					
					login(uuid, name, true);
				}
				else
				{
					disconnect("Oops, something went wrong when asking the auth servers for your info. ArE yOu ReAl? Got a valid token?");
				}
			}
			else
			{
				disconnect("Oops, something went wrong when asking the auth servers for your info. ArE yOu ReAl? Got a valid token?");
			}
		}
		else
		{
			cc.debug("[LH] [WARNING] Unknown ID " + id + " Data: " + reader.toString());
			cc.close();
		}
	}
	
	private void login(UUID uuid, String name, boolean shouldCompress)
	{
		Player player = new Player(core, cc, data.getTargetVersion(), data.getTargetDomain(), data.getTargetPort(), uuid, name);
		
		if(shouldCompress)
		{
			//Send compression packet:
			int compression = 256;
			PacketBuilder pb = new PacketBuilder();
			pb.addCInt(idConverter.getID(PacketsToClient.LtCSetCompression));
			pb.addCInt(compression);
			cc.sendPacket(pb.asBytes());
			
			cc.waitUntilQueueEmpty();
			
			cc.enableCompression(compression);
		}
		
		//Set join allow:
		PacketBuilder pb = new PacketBuilder();
		pb.addCInt(idConverter.getID(PacketsToClient.LtCLoginSuccess));
		pb.addString(uuid.toString());
		pb.addString(name);
		cc.sendPacket(pb.asBytes());
		
		cc.setHandler(new GameHandler(core, cc, player));
	}
	
	private UUID fixUUID(String uuid)
	{
		String newUUIDString = uuid.substring(0, 9) + '-' + uuid.substring(9, 13) + '-' + uuid.substring(13, 17) + '-' + uuid.substring(17, 21) + '-' + uuid.substring(21);
		return UUID.fromString(newUUIDString);
	}
	
	private void disconnect(String message)
	{
		PacketBuilder pb = new PacketBuilder();
		pb.addCInt(idConverter.getID(PacketsToClient.LtCDisconnect));
		cc.debug("Disconnecting: " + message);
		pb.addString("{\"text\":\"" + message.replace("\"", "\\\"") + "\",\"color\":\"red\"}");
		cc.sendAndClose(pb.asBytes());
	}
}
