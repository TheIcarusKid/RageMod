package net.rageland.ragemod.data;

import java.util.HashMap;

import org.bukkit.Material;

public class Factions 
{
	private static volatile Factions instance;
	
	private static HashMap<Integer, String> factions = new HashMap<Integer, String>(); // this can be expanded from String to a Faction object if I add more data
	
    public static Factions getInstance() 
    {
		if (instance == null) 
		{
			instance = new Factions();
		}
		return instance;
	}
    
    // TODO: Eventually pull all of this stuff from the DB or config - since we only have 2 factions, keep this hardcoded for now
    public void loadFactions()
    {
    	factions.put(0, "Neutral");
    	factions.put(1, "Red");
    	factions.put(2, "Blue");
    }
    
    // Returns the faction name from ID
    public static String getName(int id)
    {
    	return factions.get(id);
    }
    
    // Returns the faction ID from name
    public static int getID(String name)
    {
    	for( int id : factions.keySet() )
    	{
    		if( factions.get(id).equalsIgnoreCase(name) )
    			return id;
    	}
    	
    	return 0;
    }
    
    // Returns the correct data value for the faction's wool color
    public static int getWoolColor(int id)
    {
    	switch(id)
    	{
    		case 1:		// red
    			return 14;
    		case 2:		// blue
    			return 11;
    		default:	// neutral
    			return 0;
    	}
    }

	public static Material getLiquidBlock(int id) 
	{
		switch(id)
    	{
    		case 1:		// red
    			return Material.LAVA;
    		case 2:		// blue
    			return Material.WATER;
    		default:	// neutral
    			return Material.ICE;
    	}
	}
	
	public static Material getSpecialBlock(int id) 
	{
		switch(id)
    	{
    		case 1:		// red
    			return Material.NETHERRACK;
    		case 2:		// blue
    			return Material.LAPIS_BLOCK;
    		default:	// neutral
    			return Material.SNOW_BLOCK;
    	}
	}
}
