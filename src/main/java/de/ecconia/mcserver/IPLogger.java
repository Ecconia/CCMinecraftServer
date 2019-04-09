package de.ecconia.mcserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import de.ecconia.mcserver.json.JSONArray;
import de.ecconia.mcserver.json.JSONObject;
import de.ecconia.mcserver.json.JSONParser;

public class IPLogger
{
	private JSONObject joinData;
	private JSONObject uuidData;
	
	public IPLogger()
	{
		try
		{
			File ipFile = new File("ip.json");
			
			if(!ipFile.exists())
			{
				ipFile.createNewFile();
			}
			
			BufferedReader br = new BufferedReader(new FileReader(ipFile));
			String json = br.readLine();
			br.close();
			
			if(json == null || json.isEmpty())
			{
				joinData = new JSONObject();
				uuidData = new JSONObject();
			}
			else
			{
				JSONObject data = (JSONObject) JSONParser.parse(json);
				
				joinData = (JSONObject) data.getEntries().get("join");
				uuidData = (JSONObject) data.getEntries().get("uuid");
			}
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public synchronized UUID getUUIDforUsername(String username)
	{
		for(Entry<String, Object> entry : uuidData.getEntries().entrySet())
		{
			if(username.equals(entry.getValue()))
			{
				return UUID.fromString(entry.getKey());
			}
		}
		
		return null;
	}
	
	public synchronized void addEntry(UUID uuid, String name, String ip)
	{
		if(joinData != null)
		{
			long time = System.currentTimeMillis();
			
			//Set or overwrite latest seen name.
			uuidData.getEntries().put(uuid.toString(), name);
			
			//Create an entry which has the time and the uuid stored
			JSONObject entry = new JSONObject();
			entry.put("uuid", uuid.toString());
			entry.put("time", time);
			
			JSONArray entries = (JSONArray) joinData.getEntries().get(ip);
			if(entries == null)
			{
				entries = new JSONArray();
				joinData.put(ip, entries);
			}
			
			entries.add(entry);
			save();
		}
	}
	
	public Set<String> getForIP(String ip)
	{
		List<String> uuids = new ArrayList<>();
		
		JSONArray arr = (JSONArray) joinData.getEntries().get(ip);
		
		if(arr == null)
		{
			return new HashSet<>();
		}
		
		for(Object obj : arr.getEntries())
		{
			JSONObject jo = (JSONObject) obj;
			uuids.add((String) jo.getEntries().get("uuid"));
		}
		
		Set<String> nameSet = new HashSet<>();
		for(String uuid : uuids)
		{
			nameSet.add((String) uuidData.getEntries().get(uuid));
		}
		
		return nameSet;
	}
	
	public String getLatestForIP(String ip)
	{
		JSONArray arr = (JSONArray) joinData.getEntries().get(ip);
		
		if(arr == null)
		{
			return null;
		}
		
		long newest = 0;
		String latestUUID = null;
		
		for(Object obj : arr.getEntries())
		{
			JSONObject jo = (JSONObject) obj;
			
			long time = ((Number) jo.getEntries().get("time")).longValue();
			if(time > newest)
			{
				newest = time;
				latestUUID = (String) jo.getEntries().get("uuid");
			}
		}
		
		return (String) uuidData.getEntries().get(latestUUID);
	}
	
	private void save()
	{
		JSONObject obj = new JSONObject();
		obj.put("join", joinData);
		obj.put("uuid", uuidData);
		
		String json = obj.printJSON();
		
		try
		{
			FileWriter writer = new FileWriter("ip.json", false);
			writer.write(json);
			writer.flush();
			writer.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
