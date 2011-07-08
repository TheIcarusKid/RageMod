package net.rageland.ragemod.data;

import java.util.ArrayList;
import java.util.HashMap;

import net.rageland.ragemod.RageConfig;
import net.rageland.ragemod.RageMod;

import org.bukkit.Location;

// TODO: Consider making this an Integer hash map for speed - implement a method to return PlayerTown by searching for name

public class PlayerTowns {
	
	// Set up PlayerTowns as a static instance
	private static volatile PlayerTowns instance;
	
	private static HashMap<String, PlayerTown> towns;
	
    public static PlayerTowns GetInstance() 
    {
		if (instance == null) 
		{
			instance = new PlayerTowns();
		}
		return instance;
	}
	
	// On startup, pull all the PlayerTown data from the DB into memory 
	public void LoadPlayerTowns()
	{
		towns = RageMod.Database.LoadPlayerTowns();	
	}
	
	// Insert/update town info
	public static void Put(PlayerTown playerTown)
	{
		towns.put(playerTown.TownName, playerTown);
	}
	
	// Gets the town from memory.  Returns NULL for non-existent towns
    public static PlayerTown Get(String townName)
    {       	
    	if( towns.containsKey(townName) )
    		return towns.get(townName);
    	else
    	{
    		System.out.println("Error: PlayerTowns.Get called on non-existent town");
    		return null;
    	}
    }
    
    // Returns all towns
    public static ArrayList<PlayerTown> GetAll()
    {
    	return (ArrayList<PlayerTown>) towns.values();
    }
    
    
    // Check for all nearby towns within minimum distance (for creating new towns)
 	public static HashMap<String, Integer> CheckForNearbyTowns(Location location)
 	{
 		HashMap<String, Integer> townList = new HashMap<String, Integer>();
 		double distance; 
 		
 		for( PlayerTown town : towns.values() )
 		{
 			distance = town.Coords.distance(location);
 			if( distance < RageConfig.Town_MinDistanceBetween )
 				townList.put(town.TownName, (int)distance);
 		}
 		
 		return townList;
 	}
    
    // Checks to see if the selected faction already has a capitol; used by /townupgrade
    public static boolean DoesFactionCapitolExist(String faction)
    {
    	for( PlayerTown town : towns.values () )
    	{
    		if( town.Faction == faction && town.IsCapitol() )
    			return true;
    	}
    	
    	return false;	// No faction capitols found
    }
    
    // Checks to see if nearby enemy capitols are too close; used by /townupgrade
    public static boolean AreEnemyCapitolsTooClose(PlayerTown playerTown)
    {
    	for( PlayerTown town : towns.values () )
    	{
    		if( town.IsCapitol() && town.Coords.distance(playerTown.Coords) < RageConfig.Town_MinDistanceEnemyCapitol )
    		{
    			return true;
    		}
    	}
    	
    	return false;	// No too-close capitols found
    }

}

