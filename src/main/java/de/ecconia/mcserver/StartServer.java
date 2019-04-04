package de.ecconia.mcserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class StartServer
{
	public static void main(String[] args)
	{
		try(ServerSocket myGreatServer = new ServerSocket(25565))
		{
			List<ClientReceiver> clients = new ArrayList<>();
			while(true)
			{
				Socket s = myGreatServer.accept();
				clients.add(new ClientReceiver(s));
			}
		}
		catch(IOException e)
		{
			e.printStackTrace(System.out);
		}
	}
}
