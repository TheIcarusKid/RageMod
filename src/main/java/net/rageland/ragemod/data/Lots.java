package net.rageland.ragemod.data;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;

import net.rageland.ragemod.RageMod;

// Stores a HashMap of all the Lots in the capitol
public class Lots 
{
	// Set up Lots as a static instance
	private static volatile Lots instance;
	
	// The String key is Category (single letter) + ID; eg. W12
	private static HashMap<String, Lot> lots;
	
    public static Lots getInstance() 
    {
		if (instance == null) 
		{
			instance = new Lots();
		}
		return instance;
	}
	
	// On startup, pull all the Lot data from the DB into memory 
	public void loadLots()
	{
		lots = RageMod.Database.LoadLots();	
	}
	
	// Insert/update town info
	public static void put(Lot lot)
	{
		lots.put(lot.getLotCode(), lot);
	}
	
	// Gets the lot from memory.  Returns NULL for non-existent lot
    public static Lot get(String lotCode)
    {       	
    	if( lots.containsKey(lotCode) )
    		return lots.get(lotCode);
    	else
    	{
    		System.out.println("Error: Lots.Get called on non-existent lot");
    		return null;
    	}
    }
    
    // Returns all lots
    public static ArrayList<Lot> getAll()
    {
    	return (ArrayList<Lot>) lots.values();
    }
    
    // Find which lot the player is standing in, if any
    public static Lot findCurrentLot(Location loc)
    {
    	for( Lot lot : (ArrayList<Lot>) lots.values() )
		{
			if( lot.isInside(loc) )
			{
				return lot;
			}
		}
    	
    	// If we reach this point, we did not find a lot
    	return null;
    }
    
	    

}
