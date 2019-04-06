package de.ecconia.mcserver.network.handler;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.crypto.SecretKey;

import de.ecconia.mcserver.Core;
import de.ecconia.mcserver.Player;
import de.ecconia.mcserver.json.JSONNode;
import de.ecconia.mcserver.json.JSONObject;
import de.ecconia.mcserver.json.JSONParser;
import de.ecconia.mcserver.network.ClientConnection;
import de.ecconia.mcserver.network.helper.AuthServer;
import de.ecconia.mcserver.network.helper.packet.PacketBuilder;
import de.ecconia.mcserver.network.helper.packet.PacketReader;
import de.ecconia.mcserver.network.tools.encryption.AsyncCryptTools;

public class LoginHandler implements Handler
{
	private static final Random random = new Random();
	
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
	}
	
	private enum Stage
	{
		Username,
		Encryption,
		Compression,
		Join,
		;
	}
	
	@Override
	public void handlePacket(byte[] bytes)
	{
		PacketReader reader = new PacketReader(bytes);
		int id = reader.readCInt();
		
		if(id == 0)
		{
			if(stage != Stage.Username)
			{
				disconnect("Did not expect a username packet again.");
				return;
			}
			
			username = reader.readString();
			cc.debug("username == " + username);
			
			if(reader.remaining() > 0)
			{
				cc.debug("WARNING: Login packet not fully read! Bytes: " + reader.toString());
			}
			
			//TODO: Check if a player may join at this point by using handshake data and username.
			
			PacketBuilder builder = new PacketBuilder();
			builder.addCInt(1); //Encryption request.
			builder.addString(""); //Server code
			byte[] pubkey = core.getKeyPair().getPublic().getEncoded();
			builder.addCInt(pubkey.length);
			builder.addBytes(pubkey);
			builder.addCInt(validation.length);
			builder.addBytes(validation);
			cc.sendPacket(builder.asBytes());
			stage = Stage.Encryption;
		}
		else if(id == 1)
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
					
					Player player = new Player(core, cc, data.getTargetVersion(), data.getTargetDomain(), data.getTargetPort(), uuid, name);
					
					//Send compression packet:
					PacketBuilder pb = new PacketBuilder();
					pb.addCInt(3);
					pb.addCInt(1024);
					cc.sendPacket(pb.asBytes());
					cc.enableCompression(1024);
					
					//Set join allow:
					pb = new PacketBuilder();
					pb.addCInt(2);
					pb.addString(uuid.toString());
					pb.addString(name);
					cc.sendPacket(pb.asBytes());
					
					cc.setHandler(new GameHandler(core, cc, player));
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
	
	private UUID fixUUID(String uuid)
	{
		String newUUIDString = uuid.substring(0, 9) + '-' + uuid.substring(9, 13) + '-' + uuid.substring(13, 17) + '-' + uuid.substring(17, 21) + '-' + uuid.substring(21);
		return UUID.fromString(newUUIDString);
	}
	
	private void disconnect(String message)
	{
		PacketBuilder pb = new PacketBuilder();
		pb.addCInt(0); //Disconnect ID
		cc.debug("Disconnecting: " + message);
		pb.addString("{\"text\":\"" + message.replace("\"", "\\\"") + "\",\"color\":\"red\"}");
		cc.sendPacket(pb.asBytes());
		cc.sendAndClose();
	}
}
