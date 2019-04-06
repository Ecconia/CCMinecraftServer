package de.ecconia.mcserver.network;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.crypto.SecretKey;

import de.ecconia.mcserver.Core;
import de.ecconia.mcserver.Logger;
import de.ecconia.mcserver.network.handler.Handler;
import de.ecconia.mcserver.network.handler.HandshakeHandler;
import de.ecconia.mcserver.network.helper.reader.DecompressionReader;
import de.ecconia.mcserver.network.helper.reader.DecrytionReader;
import de.ecconia.mcserver.network.helper.reader.DisconnectException;
import de.ecconia.mcserver.network.helper.reader.Reader;
import de.ecconia.mcserver.network.helper.reader.StreamReader;
import de.ecconia.mcserver.network.tools.compression.Compressor;
import de.ecconia.mcserver.network.tools.encryption.CipherException;
import de.ecconia.mcserver.network.tools.encryption.SyncCryptUnit;

public class ClientConnection
{
	private static int clientID = 1;
	private OutputStream os;
	
	private final Socket socket;
	private final int id;
	
	private SyncCryptUnit crypter;
	private Compressor compressor;
	private Reader reader;
	
	private Handler handler;
	
	private boolean shouldDie;
	
	private final BlockingQueue<byte[]> sendingQueue = new LinkedBlockingQueue<>();
	
	public ClientConnection(Core core, Socket socket)
	{
		this.socket = socket;
		this.id = clientID++;
		//TODO: Only do this, once the "connection" wants to join the server (and has been validated).
		
		debug("== " + getRemoteIP() + ':' + getRemotePort());
		setHandler(new HandshakeHandler(this, core));
		
		try
		{
			reader = new StreamReader(socket.getInputStream());
			os = socket.getOutputStream();
		}
		catch(IOException e)
		{
			debug("Could not open IO streams: " + e.getMessage());
			return;
		}
		
		Thread readingThread = new Thread(() -> {
			try
			{
				//Read the first byte before the normal reading in the loop.
				//It may be the legacy ping indicator.
				int firstByte = reader.readByte();
				if(firstByte == -1)
				{
					//Connection broken.
					throw new DisconnectException();
				}
				
				if(firstByte == 254)
				{
					debug("aborted. Detected legacy ping packet, abort this connection.");
					try
					{
						socket.close();
					}
					catch(IOException e)
					{
						throw new UncheckedIOException(e);
					}
					return;
				}
				
				int packetSize = readCInt((byte) firstByte);
				byte[] packet = reader.readBytes(packetSize);
				handler.handlePacket(packet);
				
				while(true)
				{
					packetSize = readCInt();
					packet = reader.readBytes(packetSize);
					handler.handlePacket(packet);
				}
			}
			catch(Exception e)
			{
				if(e instanceof DisconnectException)
				{
					debug("Client broke connection.");
				}
				else if(e instanceof CipherException)
				{
					debug("Could not create cipher.");
				}
				else if(e instanceof UncheckedIOException)
				{
					IOException ioe = (IOException) e.getCause();
					
					if(ioe instanceof SocketException)
					{
						String message = ioe.getMessage();
						//TBI: What is "reset" even. (Caused in SocketInputStream#)
						if("Connection reset".equals(message))
						{
							debug("Client resetted connection.");
						}
						else if("Socket closed".equals(message))
						{
							debug("Socket has been closed internally.");
						}
						else
						{
							System.out.println("Unexpected SocketException (while reading):");
							ioe.printStackTrace(System.out);
						}
					}
					else
					{
						System.out.println("Unexpected IOException (while reading):");
						ioe.printStackTrace(System.out);
					}
				}
				else
				{
					System.out.println("Unexpected exception (while reading):");
					e.printStackTrace(System.out);
				}
				
				close();
			}
		}, "ReadingThread");
		readingThread.setUncaughtExceptionHandler((t, e) -> {
			Logger.warning("Oops some exception slipped the catch on the reading thread. That should never happen!!");
			e.printStackTrace(System.out);
			close();
		});
		readingThread.start();
		
		Thread sendingThread = new Thread(() -> {
			try
			{
				while(true)
				{
					byte[] packet = sendingQueue.take();
					
					if(compressor != null)
					{
						//Compress packet
						Compressor.IntBytes ret = compressor.compress(packet);
						//Prepend original size, or 0
						packet = prependCInt(ret.getBytes(), ret.getInt());
					}
					
					//Prepend size
					packet = prependCInt(packet, packet.length);
					
					if(crypter != null)
					{
						packet = crypter.encryptBytes(packet);
					}
					
					//Send packet
					os.write(packet);
					os.flush();
					
					if(shouldDie)
					{
						//At this point check if the sendQueue is empty, if so kill this socket.
						if(sendingQueue.isEmpty())
						{
							close();
						}
					}
				}
			}
			catch(Exception e)
			{
				if(e instanceof SocketException)
				{
					String message = e.getMessage();
					if("Socket closed".equals(message))
					{
						debug("Socket has been closed internally.");
					}
					else
					{
						System.out.println("Unexpected SocketException (while sending):");
						e.printStackTrace(System.out);
					}
				}
				else
				{
					System.out.println("Unexpected exception (while sending):");
					e.printStackTrace(System.out);
				}
				
				close();
			}
		}, "SendingThread");
		sendingThread.setUncaughtExceptionHandler((t, e) -> {
			debug("S> " + e.getClass().getSimpleName() + (e.getMessage() != null ? " " + e.getMessage() : ""));
			close();
		});
		sendingThread.start();
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
	
	private int readCInt(byte first)
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
	
	private int readCInt()
	{
		int value = 0;
		byte read = 0;
		int iteration = 0;
		
		do
		{
			read = reader.readByte();
			value |= (read & 127) << iteration++ * 7;
			
			if(iteration > 5)
			{
				throw new RuntimeException("VarInt too big");
			}
		}
		while((read & 128) == 128);
		
		return value;
	}
	
	//API setter/methods:
	
	public void sendPacket(byte[] packet)
	{
		if(!shouldDie)
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
	}
	
	public void setHandler(Handler handler)
	{
		debug("setHandler: " + handler.getClass().getSimpleName());
		this.handler = handler;
	}
	
	public void debug(String message)
	{
		Logger.debug(id + " " + message);
	}
	
	public void close()
	{
		try
		{
			socket.close();
		}
		catch(IOException e)
		{
			debug("Issue closing the socket:");
			e.printStackTrace(System.out);
		}
	}
	
	//Enable features:
	
	public void enableCompression(int maxUncompressed)
	{
		compressor = new Compressor(maxUncompressed);
		reader = new DecompressionReader(reader, compressor);
	}
	
	public void enableEncryption(SecretKey sharedKey)
	{
		crypter = new SyncCryptUnit(sharedKey);
		reader = new DecrytionReader(reader, crypter);
	}
	
	//API getter:
	
	public String getRemoteIP()
	{
		return ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostString();
	}
	
	public int getRemotePort()
	{
		return socket.getPort();
	}
	
	public boolean isConnected()
	{
		return !socket.isClosed();
	}
	
	public void sendAndClose()
	{
		shouldDie = true;
	}
}
