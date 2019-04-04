package de.ecconia.mcserver.network;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import de.ecconia.mcserver.Core;
import de.ecconia.mcserver.json.JSONObject;
import de.ecconia.mcserver.network.helper.PacketBuilder;
import de.ecconia.mcserver.network.helper.PacketReader;

public class StatusHandler implements Handler
{
	@SuppressWarnings("unused")
	private final Core core; //TODO: "use" -> Get the information for the server ping from there.
	private final ClientConnection cc;
	
	public StatusHandler(Core core, ClientConnection cc)
	{
		this.cc = cc;
		this.core = core;
	}
	
	@Override
	public void handlePacket(byte[] bytes)
	{
		PacketReader reader = new PacketReader(bytes);
		int id = reader.readCInt();
		
		if(id == 0)
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
//			JSONObject players = new JSONObject();
//			players.put("online", 0);
//			players.put("max", 0);
//			players.put("sample", new JSONArray());
//			root.put("players", players);
			
			//Description:
			JSONObject description = new JSONObject();
			description.put("text", "Ecconia's Custom Server 1.13.2");
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
				e.printStackTrace();
			}
			
			String json = root.printJSON();
			cc.debug("Sending json: " + json);
			
			PacketBuilder mb = new PacketBuilder();
			mb.addCInt(0);
			mb.addString(json);
			cc.sendPacket(mb.asBytes());
		}
		else if(id == 1)
		{
			long pingID = reader.readLong();
			
			PacketBuilder mb = new PacketBuilder();
			mb.addCInt(1);
			mb.addLong(pingID);
			cc.sendPacket(mb.asBytes());
		}
		else
		{
			cc.debug("[SH] [WARNING] Unknown ID: " + id);
			cc.close();
		}
	}
}
