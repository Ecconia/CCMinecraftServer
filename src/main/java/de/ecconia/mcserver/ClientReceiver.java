package de.ecconia.mcserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientReceiver
{
	private static int clientID = 1;
	private InputStream is;
//	private OutputStream os;
	private final int id;
	
	public ClientReceiver(Socket socket)
	{
		this.id = clientID++;
		System.out.println(id + " <<<< " + ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostString());
		
		try
		{
			is = socket.getInputStream();
//			os = socket.getOutputStream();
		}
		catch(IOException e)
		{
			e.printStackTrace(System.out);
		}
		Thread readingThread = new Thread(() -> {
			try
			{
				while(true)
				{
					int firstByte = is.read();
					if(firstByte == -1)
					{
						//Connection broken.
						System.out.println(id + " closed.");
						return;
					}
					
					if(firstByte == 254)
					{
						System.out.println(id + " aborted. Detected legacy ping packet, abort this connection.");
						socket.close();
						return;
					}
					
					int packetSize = readCInt((byte) firstByte);
					byte[] packet = readBytes(packetSize);
					
					String packetMsg = packetSize + " : [";
					for(int i = 0; i < packet.length - 1; i++)
					{
						packetMsg += "" + (packet[i] & 255) + ",";
					}
					packetMsg += packet[packetSize - 1] + "]";
					System.out.println(id + " >>> " + packetMsg);
				}
			}
			catch(IOException e)
			{
				e.printStackTrace(System.out);
			}
		}, "ReadingThread");
		readingThread.setUncaughtExceptionHandler((t, e) -> {
			System.out.println(id + " > " + e.getClass().getSimpleName() + (e.getMessage() != null ? " " + e.getMessage() : ""));
		});
		readingThread.start();
		
//		Thread sendingThread = new Thread(() -> {
//		}, "SendingThread");
	}
	
	public byte[] readBytes(int amount) throws IOException
	{
		try
		{
			byte[] bytes = new byte[amount];
			
			int pointer = 0;
			int remaining = amount;
			
			while(remaining > 0)
			{
				int amountRead = is.read(bytes, pointer, remaining);
				if(amountRead == -1)
				{
					System.out.println(id + " > Nuuuut 1");
//					throw new IOException("Nuuuut 1");
					return new byte[0];
				}
				
				remaining -= amountRead;
				pointer += amountRead;
			}
			
//			bytesRead += amount;
			return bytes;
		}
		catch(IOException e)
		{
			throw e;//new DirtyIOException(e);
		}
	}
	
	private int readCInt(byte first) throws IOException
	{
		int value = 0;
		
		//Take the first byte from the parameter
		byte read = first;
		int iteration = 0;
		value |= (read & 127) << iteration++ * 7;
		
		while((read & 128) == 128)
		{
			read = (byte) is.read();
			value |= (read & 127) << iteration++ * 7;
			
			if(iteration > 5)
			{
				throw new RuntimeException("VarInt too big");
			}
		}
		
		return value;
	}
}
