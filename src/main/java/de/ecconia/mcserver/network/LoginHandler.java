package de.ecconia.mcserver.network;

import java.util.Arrays;
import java.util.Random;

import javax.crypto.SecretKey;

import de.ecconia.mcserver.Core;
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
			String serverHash = AsyncCryptTools.generateHashFromBytes("", sharedKey.getEncoded(), core.getKeyPair().getPublic().getEncoded());
			
			AuthServer.hasJoin(username, serverHash, cc.getConnectingIP());
		}
		else
		{
			cc.debug("[LH] [WARNING] Unknown ID: " + id);
			cc.close();
		}
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
