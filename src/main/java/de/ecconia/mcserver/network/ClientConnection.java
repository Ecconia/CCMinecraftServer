package de.ecconia.mcserver.network;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

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

public class ClientConnection implements PacketSender
{
	//Each connection gets one id:
	private static int clientID = 1;
	
	//Constant data for this connection: 
	private final Socket socket;
	private final Core core;
	private final int id;
	
	private OutputStream os;
	private Thread readingThread;
	private boolean sendingThreadMayRun = true;
	private Thread sendingThread;
	
	//Decoding/Encoding packets:
	private SyncCryptUnit crypter;
	private Compressor compressor;
	private Reader reader;
	
	//Incomming packet processing unit:
	private Handler handler;
	private List<Thread> threadsToClose = new ArrayList<>();
	private Thread waitingThread;
	private Object majicWaitingLock = new Object();
	
	//Error/Close handling:
	private boolean isClosing;
	
	//Outgoing packet buffer (threadsafe):
	private final BlockingQueue<byte[]> sendingQueue = new LinkedBlockingQueue<>();
	
	public ClientConnection(Core core, Socket socket)
	{
		this.core = core;
		this.socket = socket;
		this.id = clientID++;
		//TODO: Only do this, once the "connection" wants to join the server (and has been validated).
		
		debug("<<= " + getRemoteIP() + ':' + getRemotePort() + " -> [" + core.getIps().getForIP(getRemoteIP()).stream().collect(Collectors.joining(", ")) + "]");
		setHandler(new HandshakeHandler(this, core));
		
		try
		{
			reader = new StreamReader(socket.getInputStream());
			os = socket.getOutputStream();
		}
		catch(IOException e)
		{
			Logger.error("Could not open IO streams.", e);
			return;
		}
		
		//Reading Thread:
		
		readingThread = new Thread(() -> {
			try
			{
				readFirstPacket();
				
				while(true)
				{
					readPacket();
				}
			}
			catch(Exception e)
			{
				if(!isClosing)
				{
					close();
					
					if(e instanceof DisconnectException)
					{
						//Expected and properly handled by doing nothing!
						//Logger.error("Client broke connection.");
					}
					else if(e instanceof CipherException)
					{
						Logger.error("Could not create cipher.");
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
								Logger.error("Client resetted connection.");
							}
							else if("Socket closed".equals(message))
							{
								Logger.error("Socket has been closed internally.");
							}
							else
							{
								Logger.error("Unexpected SocketException (while reading):");
								ioe.printStackTrace(System.out);
							}
						}
						else
						{
							Logger.error("Unexpected IOException (while reading):");
							ioe.printStackTrace(System.out);
						}
					}
					else
					{
						Logger.error("Unexpected exception (while reading):");
						e.printStackTrace(System.out);
					}
				}
			}
		}, "ReadingThread");
		readingThread.setUncaughtExceptionHandler((t, e) -> {
			Logger.error(id + " R> Oops some exception slipped the catch on the reading thread. That should never happen!!");
			e.printStackTrace(System.out);
			close();
		});
		readingThread.start();
		
		//Sending Thread:
		
		sendingThread = new Thread(() -> {
			try
			{
				while(sendingThreadMayRun)
				{
					try
					{
						//Lets capture and ignore interrupt exceptions, they will cause the system to trigger the stuff behind the getter.
						flushPacket(sendingQueue.take());
					}
					catch(InterruptedException e)
					{
						//Don't do anything, this interruption was intentional.
					}
					
					if(waitingThread != null)
					{
						synchronized(majicWaitingLock)
						{
							if(sendingQueue.isEmpty())
							{
								majicWaitingLock.notify();
								waitingThread = null;
							}
						}
					}
				}
				
				debug("Sending thread shutted down.");
			}
			catch(Exception e)
			{
				if(!isClosing)
				{
					close();
					
					if(e instanceof SocketException)
					{
						String message = e.getMessage();
						if("Socket closed".equals(message))
						{
							Logger.error("Socket has been closed internally.");
						}
						else if("Broken pipe (Write failed)".equals(message))
						{
							//Client closed connection while sending.
							//Do nothing, just close.
							debug("Client closed connection, while sending (broken pipe).");
						}
						else if("Connection reset".equals(message))
						{
							//Client closed connection while sending.
							//Do nothing, just close.
							debug("Client closed connection, while sending (reset).");
						}
						else
						{
							Logger.error(id + " Unexpected SocketException (while sending):");
							e.printStackTrace(System.out);
						}
					}
					else
					{
						Logger.error(id + " Unexpected exception (while sending):");
						e.printStackTrace(System.out);
					}
				}
			}
		}, "SendingThread");
		sendingThread.setUncaughtExceptionHandler((t, e) -> {
			close();
			
			Logger.error(id + " S> Oops some exception slipped the catch on the reading thread. That should never happen!!");
			e.printStackTrace(System.out);
		});
		sendingThread.start();
	}
	
	//Internal methods:
	
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
	
	//Internal read/send methods:
	
	private void readFirstPacket()
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
			close();
			return;
		}
		
		int packetSize = readCInt((byte) firstByte);
		byte[] packet = reader.readBytes(packetSize);
		handler.handlePacket(packet);
	}
	
	private void readPacket()
	{
		int packetSize = readCInt();
		byte[] packet = reader.readBytes(packetSize);
		handler.handlePacket(packet);
	}
	
	private void flushPacket(byte[] packet) throws IOException
	{
//		PacketReader reader = new PacketReader(packet);
//		System.out.println(id + " Sending ID: 0x" + Integer.toHexString(reader.readCInt()) + " [" + reader.toString() + "]");
		
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
	}
	
	//API setter/methods:
	
	@Override
	public void sendPacket(byte[] packet)
	{
		try
		{
			sendingQueue.put(packet);
		}
		catch(InterruptedException e)
		{
			Logger.error("Interrupted while inserting packet to queue.", e);
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
		Logger.debug("Connection " + id + " is closing now.");
		isClosing = true;
		core.dump(id);
		
		for(Thread t : threadsToClose)
		{
			t.interrupt();
		}
		
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
	
	public void sendAndClose(byte[] lastPacket)
	{
		//Close the sending thread:
		sendingThreadMayRun = false;
		sendingThread.interrupt();
		
		try
		{
			//Wait for the sending thread to be off.
			sendingThread.join();
			flushPacket(lastPacket);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		close();
	}
	
	public void addThread(Thread thread)
	{
		threadsToClose.add(thread);
	}
	
	public void waitUntilQueueEmpty()
	{
		waitingThread = Thread.currentThread();
		
		try
		{
			synchronized(majicWaitingLock)
			{
				//TBI: How to ensure that this works? Its experimental rn.
				sendingThread.interrupt();
				majicWaitingLock.wait();
			}
		}
		catch(InterruptedException e)
		{
			Logger.error("Interrupt error while waiting for sendqueue to be empty.", e);
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
	
	public int getID()
	{
		return id;
	}
	
}
