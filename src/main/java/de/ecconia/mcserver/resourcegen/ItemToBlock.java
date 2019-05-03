package de.ecconia.mcserver.resourcegen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.ecconia.mcserver.Logger;

public class ItemToBlock
{
	public static final Map<Integer, String> itemID = new ConcurrentHashMap<>();
	public static final Map<String, Integer> blockNames = new ConcurrentHashMap<>();
	public static final Map<Integer, String> itemIDToBlock = new ConcurrentHashMap<>();
	
	public static void init()
	{
		File f = new File("files/v1.13.2/ItemToBlock.txt");
		if(!f.exists())
		{
			Logger.error("ItemToBlock.txt file does not exist.");
			System.exit(1);
		}
		
		try(BufferedReader reader = new BufferedReader(new FileReader(f)))
		{
			String line;
			while((line = reader.readLine()) != null)
			{
				String[] parts = line.split(" ");
				
				Integer itemID = Integer.parseInt(parts[0]);
				String itemName = parts[1];
				//Register item id->name
				ItemToBlock.itemID.put(itemID, itemName);
				
				if(parts.length == 4)
				{
					String blockName = parts[2];
					Integer blockID = Integer.parseInt(parts[3]);
					//Register blockname -> id
					ItemToBlock.blockNames.put(blockName, blockID);
					//Register itemID->Blockname for later lookup
					ItemToBlock.itemIDToBlock.put(itemID, blockName);
				}
			}
		}
		catch(IOException e)
		{
			Logger.error("Error while parsing ItemToBlock.txt", e);
			System.exit(1);
		}
	}
}
