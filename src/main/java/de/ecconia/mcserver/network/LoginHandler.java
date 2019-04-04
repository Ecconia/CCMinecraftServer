package de.ecconia.mcserver.network;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.crypto.SecretKey;

import de.ecconia.mcserver.Core;
import de.ecconia.mcserver.json.JSONNode;
import de.ecconia.mcserver.json.JSONObject;
import de.ecconia.mcserver.json.JSONParser;
import de.ecconia.mcserver.network.helper.AsyncCryptTools;
import de.ecconia.mcserver.network.helper.AuthServer;
import de.ecconia.mcserver.network.helper.PacketBuilder;
import de.ecconia.mcserver.network.helper.PacketReader;

public class LoginHandler implements Handler
{
	private static final Random random = new Random();
	
	private final Core core;
	private final ClientConnection cc;
	private final byte[] validation = new byte[4];
	
	private Stage stage;
	private String username;
	
	public LoginHandler(Core core, ClientConnection cc)
	{
		this.cc = cc;
		this.core = core;
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
			
			if(reader.remaining() > 0)
			{
				cc.debug("WARNING: Login packet not fully read! Bytes: " + reader.toString());
			}
			
			cc.debug("U-S-E-R-N-A-M-E: " + username);
			if(!username.equals("Ecconia"))
			{
				disconnect("You can't join, cause someone told me: \"" + username + "\" is a noob!");
			}
			else
			{
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
			if(decryptedValidationResponse == null)
			{
				disconnect("Could decrypt your message (validation).");
				return;
			}
			
			boolean valid = Arrays.equals(validation, decryptedValidationResponse);
			if(!valid)
			{
				disconnect("Encryption validation is not correct.");
				return;
			}
			
			byte[] decryptedSharedKeyBytes = AsyncCryptTools.decrypt(core.getKeyPair().getPrivate(), sharedKeyBytes);
			if(decryptedSharedKeyBytes == null)
			{
				disconnect("Could decrypt your message.");
				return;
			}
			
			SecretKey sharedKey = AsyncCryptTools.secredKeyFromBytes(decryptedSharedKeyBytes);
			
			//TBI: Enable encrytion now? Will the future error message be decrypted on the client side?
			cc.enableEncryption(sharedKey);
			
			String serverHash = AsyncCryptTools.generateHashFromBytes("", sharedKey.getEncoded(), core.getKeyPair().getPublic().getEncoded());
			
			//Yeeee nice! At this point we got the UUID! Lets not care further :P
			//TODO: Use this information!
			//It should be validated for sure. Cause else network attacks are possible.
			String json = AuthServer.hasJoin(username, serverHash, cc.getConnectingIP());
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
					
					//Send compression packet:
					//TODO: Enable and support compression!
//					PacketBuilder pb = new PacketBuilder();
//					pb.addCInt(3);
//					pb.addCInt(1024);
//					cc.sendPacket(pb.asBytes());
					
					//Set join allow:
					PacketBuilder pb = new PacketBuilder();
					pb.addCInt(2);
					pb.addString(uuid.toString());
					pb.addString(name);
					cc.sendPacket(pb.asBytes());
					//TODO: Yay we got to this point, lets request compression.
					
					cc.setHandler(new GameHandler(core, cc));
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
			cc.debug("[LH] [WARNING] Unknown ID: " + id);
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
		cc.close();
	}
}
