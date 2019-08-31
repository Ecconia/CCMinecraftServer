package de.ecconia.mcserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import de.ecconia.mcserver.network.ClientConnection;
import de.ecconia.mcserver.resourcegen.ItemToBlock;

public class StartServer
{
	public static void main(String[] args)
	{
		System.out.println("Loading...");
		ItemToBlock.init();
		Core core = new Core();
		
		System.out.println("Starting...");
		try(ServerSocket myGreatServer = new ServerSocket(25565))
		{
			while(true)
			{
				Socket s = myGreatServer.accept();
				new ClientConnection(core, s);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace(System.out);
		}
	}
}
