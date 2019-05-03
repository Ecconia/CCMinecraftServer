package de.ecconia.mcserver.data;

import de.ecconia.mcserver.resourcegen.ItemToBlock;

public class ItemStack
{
	private final int id;
	private final int count;
	//TODO: NBT
	
	public ItemStack(int id, int count)
	{
		this.id = id;
		this.count = count;
	}
	
	public int getId()
	{
		return id;
	}
	
	public int getCount()
	{
		return count;
	}
	
	@Override
	public String toString()
	{
		return "ItemStack[id=" + id + " (" + ItemToBlock.itemID.get(id) + "), count=" + count + "]";
	}
}
