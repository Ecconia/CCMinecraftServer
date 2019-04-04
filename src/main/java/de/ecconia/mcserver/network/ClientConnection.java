package de.ecconia.mcserver.network;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.crypto.SecretKey;

import de.ecconia.mcserver.Core;
import de.ecconia.mcserver.network.helper.DecrytionWrapper;
import de.ecconia.mcserver.network.helper.Reader;
import de.ecconia.mcserver.network.helper.StreamWrapper;
import de.ecconia.mcserver.network.helper.SyncCryptUnit;

public class ClientConnection
{
	private static int clientID = 1;
	private OutputStream os;
	
	private final Socket socket;
	private final int id;
	
	private SyncCryptUnit crypter;
	private Reader reader;
	
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
			reader = new StreamWrapper(socket.getInputStream());
			os = socket.getOutputStream();
		}
		catch(IOException e)
		{
			e.printStackTrace(System.out);
			return;
		}
		
		Thread readingThread = new Thread(() -> {
			try
			{
				while(true)
				{
					int firstByte = reader.readByte();
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
					byte[] packet = reader.readBytes(packetSize);
					
					String packetMsg = packetSize + ":[";
					for(int i = 0; i < packet.length - 1; i++)
					{
						packetMsg += (packet[i] & 255) + ",";
					}
					//TODO: Throws an exception on empty packet.
					packetMsg += (packet[packetSize - 1] & 255) + "]";
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
					
					if(crypter != null)
					{
						packet = crypter.encryptBytes(packet);
					}
					
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
	
	public void debug(String message)
	{
		System.out.println(id + " " + message);
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
			read = reader.readByte();
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
		try
		{
			socket.close();
		}
		catch(IOException e)
		{
			//Actually idgaf
			e.printStackTrace(System.out);
		}
	}
	
	public String getConnectingIP()
	{
		return ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostString();
	}

	public void enableEncryption(SecretKey sharedKey)
	{
		crypter = new SyncCryptUnit(sharedKey);
		reader = new DecrytionWrapper(reader, crypter);
	}
}
