package de.ecconia.mcserver.resourcegen;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.ecconia.mcserver.json.JSONArray;
import de.ecconia.mcserver.json.JSONNode;
import de.ecconia.mcserver.json.JSONObject;
import de.ecconia.mcserver.json.JSONParser;

public class StartItemBlockReader
{
	public static void main(String[] args)
	{
		Map<String, Integer> items = parseItems();
		Set<String> blocks = parseBlocks();
		Map<String, Integer> blockDefaults = parseBlockDefaults();
		
		for(Entry<String, Integer> entry : items.entrySet())
		{
			String block = "";
			
			if(blockDefaults.containsKey(entry.getKey()))
			{
				block = entry.getKey() + " " + blockDefaults.get(entry.getKey());
				blockDefaults.remove(entry.getKey());
			}
			
			System.out.println(pad3(entry.getValue()) + " " + entry.getKey() + " " + block);
		}
		
		System.out.println();
		System.out.println();
		
		for(Entry<String, Integer> entry : blockDefaults.entrySet())
		{
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
		
//		for(String block : blocks)
//		{
//			if(!items.containsKey(block))
//			{
//				if(!block.contains("wall") && !block.startsWith("potted_"))
//				{
//					System.out.println(block + " has no matching item.");
//				}
//			}
//		}
	}
	
	public static String pad3(int i)
	{
		String s = Integer.toString(i);
		while(s.length() < 3)
		{
			s = '0' + s;
		}
		return s;
	}
	
	private static Set<String> parseBlocks()
	{
		File f = new File("files/v1.13.2/blocks.json");
		if(!f.exists())
		{
			die("Item file does not exist.");
		}
		
		String jsonString = readFile(f);
		JSONNode node = JSONParser.parse(jsonString);
		if(!(node instanceof JSONObject))
		{
			die("File does not have a JSONObject as root element.");
		}
		JSONObject items = (JSONObject) node;
		
		Set<String> entries = new HashSet<>();
		for(Entry<String, Object> entry : items.getEntries().entrySet())
		{
			String block = entry.getKey().substring("minecraft:".length());
			entries.add(block);
		}
		
		return entries;
	}
	
	private static Map<String, Integer> parseBlockDefaults()
	{
		File f = new File("files/v1.13.2/blocks.json");
		if(!f.exists())
		{
			die("Item file does not exist.");
		}
		
		String jsonString = readFile(f);
		JSONNode node = JSONParser.parse(jsonString);
		if(!(node instanceof JSONObject))
		{
			die("File does not have a JSONObject as root element.");
		}
		JSONObject items = (JSONObject) node;
		
		Map<String, Integer> entries = new HashMap<>();
		for(Entry<String, Object> entry : items.getEntries().entrySet())
		{
			String block = entry.getKey().substring("minecraft:".length());
			
			Integer defID = null;
			JSONArray states = (JSONArray) ((JSONObject) entry.getValue()).getEntries().get("states");
			for(Object o : states.getEntries())
			{
				JSONObject state = (JSONObject) o;
				if(state.getEntries().containsKey("default"));
				{
					defID = ((Number) state.getEntries().get("id")).intValue();
				}
			}
			
			if(defID == null)
			{
				System.out.println("ERROR: " + block + " has no default state.");
			}
			
			entries.put(block, defID);
		}
		
		return entries;
	}
	
	private static Map<String, Integer> parseItems()
	{
		File f = new File("files/v1.13.2/items.json");
		if(!f.exists())
		{
			die("Item file does not exist.");
		}
		
		String jsonString = readFile(f);
		JSONNode node = JSONParser.parse(jsonString);
		if(!(node instanceof JSONObject))
		{
			die("File does not have a JSONObject as root element.");
		}
		JSONObject items = (JSONObject) node;
		
		Map<String, Integer> entries = new HashMap<>();
		for(Entry<String, Object> entry : items.getEntries().entrySet())
		{
			JSONObject inner = (JSONObject) entry.getValue();
			int id = ((Number) inner.getEntries().get("protocol_id")).intValue();
			String item = entry.getKey().substring("minecraft:".length());
			entries.put(item, id);
		}
		
		return entries;
	}
	
	private static String readFile(File file)
	{
		try(FileReader reader = new FileReader(file))
		{
			//Assume this fits
			int fileSize = (int) file.length();
			//Assume that each byte could be a character.
			char[] data = new char[fileSize];
			int amountRead = reader.read(data);
			//If smaller: UFT was expanded, if same: All content has been read.
			//Errr does not make much sense?
			if(amountRead <= 0 || amountRead > fileSize)
			{
				die("The file has size " + fileSize + " but " + amountRead + " chars have been read, either there was the use of >8 UFT or ¿it could not read the file? -> Anyway improve/confirm implmentation!");
			}
			
			return new String(data, 0, amountRead);
		}
		catch(IOException e)
		{
			die("IOException, while reading the blockdata file: " + e.getMessage());
			return null; //Compiler statisfaction.
		}
	}
	
	public static void die(String message)
	{
		System.out.println("Terminating: " + message);
		System.exit(1);
	}
}
