package de.ecconia.mcserver.network.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

public class AuthServer
{
	public static String hasJoin(String username, String hash, String ip)
	{
		String link = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + username + "&serverId=" + hash + "&ip=" + ip;
		
		try
		{
			String response = request(link);
			
			System.out.println(response);
			
			return response;
			
//			if(!response.isEmpty())
//			{
//				if(response.equals("{\"error\":\"ForbiddenOperationException\",\"errorMessage\":\"Invalid token\"}"))
//				{
//					//TODO: Update somehow, this is no good solution, will do until the login process is this derpy.
//					System.err.println("Invalid access token, please update!");
//					System.err.println("Terminating!");
//					System.exit(0);
//				}
//				else
//				{
//					System.out.println("Auth server send something on join attempt: " + response);
//					System.out.println("Thats probably an error, termination incomming.");
//					throw new FatalException("Auth server couldn't shut up.");
//				}
//			}
		}
		catch(UnknownHostException e)
		{
			throw new FatalException("Could not connect to auth server. Online?");
		}
	}
	
	public static String request(String url) throws UnknownHostException
	{
		//TODO: Throw proper exception
		try
		{
			URLConnection con = new URL(url).openConnection();
			con.setDoInput(true);
			con.setRequestProperty("Accept-Charset", "UTF-8");
			
			BufferedReader inStream;
			int responseCode = ((HttpURLConnection) con).getResponseCode();
			if(responseCode == 200)
			{
				inStream = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			}
			else if(responseCode == 204)
			{ // 204 No content.
				return "";
			}
			else
			{
				inStream = new BufferedReader(new InputStreamReader(((HttpURLConnection) con).getErrorStream(), "UTF-8"));
			}
			String res = inStream.readLine();
			inStream.close();
			return res;
		}
		catch(UnknownHostException e)
		{
			throw e;
		}
		catch(IOException e)
		{
			e.printStackTrace(System.out);
			return null;
		}
	}
}
