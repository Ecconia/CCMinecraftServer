package de.ecconia.mcserver.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.ecconia.mcserver.Core;

public class ClientConnection
{
	private static int clientID = 1;
	private InputStream is;
	private OutputStream os;
	
	private final Socket socket;
	private final int id;
	
	private Handler handler;
	
	private final BlockingQueue<byte[]> sendingQueue = new LinkedBlockingQueue<>();
	
	public ClientConnection(Core core, Socket socket)
	{
		this.socket = socket;
		this.id = clientID++;
		//TODO: Only do this, once the "connection" wants to join the server (and has been validated).
		core.addClient(this);
		
		debug("<<<< " + ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostString());
		setHandler(new HandshakeHandler(this, core));
		
		try
		{
			//TODO: Wrapper class for accessing this stream:
			is = socket.getInputStream();
			os = socket.getOutputStream();
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
						debug("closed.");
						return;
					}
					
					//TODO: Move outside of this Thread, its okay to read on the client thread!
					//Should only be checked for the very very very first byte received!
					if(firstByte == 254)
					{
						debug("aborted. Detected legacy ping packet, abort this connection.");
						socket.close();
						return;
					}
					
					int packetSize = readCInt((byte) firstByte);
					byte[] packet = readBytes(packetSize);
					
					String packetMsg = packetSize + ":[";
					for(int i = 0; i < packet.length - 1; i++)
					{
						packetMsg += (packet[i] & 255) + ",";
					}
					packetMsg += packet[packetSize - 1] + "]";
					debug("{Packet} " + packetMsg);
					
					handler.handlePacket(packet);
				}
			}
			catch(IOException e)
			{
				e.printStackTrace(System.out);
			}
		}, "ReadingThread");
		readingThread.setUncaughtExceptionHandler((t, e) -> {
			debug("> " + e.getClass().getSimpleName() + (e.getMessage() != null ? " " + e.getMessage() : ""));
		});
		readingThread.start();
		
		Thread sendingThread = new Thread(() -> {
			while(true)
			{
				try
				{
					byte[] packet = sendingQueue.take();
					//Prepend size
					packet = prependCInt(packet, packet.length);
					
					try
					{
						//Send packet
						os.write(packet);
						os.flush();
					}
					catch(IOException e)
					{
						e.printStackTrace(System.out);
					}
				}
				catch(InterruptedException e1)
				{
					e1.printStackTrace();
				}
			}
		}, "SendingThread");
		sendingThread.start();
	}
	
	public void sendPacket(byte[] packet)
	{
		try
		{
			sendingQueue.put(packet);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace(System.out);
		}
	}
	
	public static byte[] prependCInt(byte[] bytes, int i)
	{
		byte[] buffer = new byte[6];
		int pointer = 0;
		
		while((i & -128) != 0)
		{
			buffer[pointer++] = (byte) (i & 127 | 128);
			i >>>= 7;
		}
		
		buffer[pointer++] = (byte) i;
		
		byte[] output = new byte[bytes.length + pointer];
		System.arraycopy(buffer, 0, output, 0, pointer);
		System.arraycopy(bytes, 0, output, pointer, bytes.length);
		
		return output;
	}
	
	public void setHandler(Handler handler)
	{
		debug("<< setHandler: " + handler.getClass().getSimpleName());
		this.handler = handler;
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
					debug("> Nuuuut 1");
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
	
	public void debug(String message)
	{
		System.out.println(id + " " + message);
	}
	
	public int getId()
	{
		return id;
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
	
	public void close()
	{
		//TODO: Socket.close();
	}
	
	public String getConnectingIP()
	{
		return ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostString();
	}
}
