package de.ecconia.mcserver.network.handler;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import de.ecconia.java.json.JSONArray;
import de.ecconia.java.json.JSONObject;
import de.ecconia.mcserver.Core;
import de.ecconia.mcserver.Logger;
import de.ecconia.mcserver.multiversion.IdConverter;
import de.ecconia.mcserver.multiversion.ProtocolLib;
import de.ecconia.mcserver.multiversion.packets.PacketsToClient;
import de.ecconia.mcserver.multiversion.packets.PacketsToServer;
import de.ecconia.mcserver.network.ClientConnection;
import de.ecconia.mcserver.network.helper.packet.PacketBuilder;
import de.ecconia.mcserver.network.helper.packet.PacketReader;

public class StatusHandler implements Handler
{
	private final Core core;
	private final ClientConnection cc;
	private final HandshakeData data;
	private final IdConverter idConverter;
	
	public StatusHandler(Core core, ClientConnection cc, HandshakeData data)
	{
		this.cc = cc;
		this.core = core;
		this.data = data;
		this.idConverter = ProtocolLib.getOrDefault(data.getTargetVersion());
	}
	
	@Override
	public void handlePacket(byte[] bytes)
	{
		PacketReader reader = new PacketReader(bytes);
		
		int id = reader.readCInt();
		PacketsToServer type = idConverter.getStatusPacket(id);
		
		if(type == PacketsToServer.StSStatusRequest)
		{
			if(reader.remaining() > 0)
			{
				cc.debug("WARNING: Status packet not fully read! Bytes: " + reader.toString());
			}
			
			JSONObject root = new JSONObject();
			
			//Version section:
			JSONObject version = new JSONObject();
			version.put("protocol", 404);
			version.put("name", "Ecconia's Server 1.13.2");
			root.put("version", version);
			
			//Players section:
			JSONObject players = new JSONObject();
			players.put("online", core.getOnlineCount());
			players.put("max", -1);
			players.put("sample", new JSONArray());
			root.put("players", players);
			
			//Description:
			String otherLine = "Please join! Now!";
			if(data.getTargetVersion() != 404 && data.getTargetVersion() != 498)
			{
				otherLine = "Versions: 1.13.2 or 1.14.4";
			}
			else
			{
				String lastName = core.getIps().getLatestForIP(cc.getRemoteIP());
				if(lastName != null)
				{
					otherLine = "Hi, " + lastName + ".";
				}
			}
			
			JSONObject description = new JSONObject();
			description.put("text", "Ecc's cs. New: One shared world! See live what others are building! " + otherLine);
			root.put("description", description);
			
			//Favicon:
			BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = (Graphics2D) image.getGraphics();
			g.setColor(Color.pink);
			g.fillRect(0, 0, 64, 64);
			g.setColor(Color.orange);
			g.fillRect(20, 20, 24, 24);
			g.dispose();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try
			{
				ImageIO.write(image, "png", os);
				String encoded = Base64.getEncoder().encodeToString(os.toByteArray());
				root.put("favicon", "data:image/png;base64," + encoded);
			}
			catch(Exception e)
			{
				Logger.warning("Could not create favicon.", e);
			}
			
			String json = root.printJSON();
			cc.debug("Sending json: " + json);
			
			PacketBuilder mb = new PacketBuilder();
			mb.addCInt(idConverter.getID(PacketsToClient.StCStatusResponse));
			mb.addString(json);
			cc.sendPacket(mb.asBytes());
		}
		else if(type == PacketsToServer.StSPingRequest)
		{
			long pingID = reader.readLong();
			
			PacketBuilder mb = new PacketBuilder();
			mb.addCInt(idConverter.getID(PacketsToClient.StCPingResponse));
			mb.addLong(pingID);
			cc.sendPacket(mb.asBytes());
		}
		else
		{
			cc.debug("[SH] [WARNING] Unknown ID " + id + " Data: " + reader.toString());
			cc.close();
		}
	}
}
