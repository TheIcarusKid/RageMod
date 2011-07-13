package net.rageland.ragemod.data;

import java.util.ArrayList;
import java.util.HashMap;

import net.rageland.ragemod.RageConfig;
import net.rageland.ragemod.RageMod;

import org.bukkit.Location;

// TODO: Consider making this an Integer hash map for speed - implement a method to return PlayerTown by searching for name
// 		 I could also store a separate hash/array that stores ID to string for fast lookup (would this actually be faster?)

public class PlayerTowns {
	
	// Set up PlayerTowns as a static instance
	private static volatile PlayerTowns instance;
	
	private static HashMap<String, PlayerTown> towns;
	
    public static PlayerTowns getInstance() 
    {
		if (instance == null) 
		{
			instance = new PlayerTowns();
		}
		return instance;
	}
	
	// On startup, pull all the PlayerTown data from the DB into memory 
	public void loadPlayerTowns()
	{
		towns = RageMod.Database.loadPlayerTowns();	
	}
	
	// Insert/update town info
	public static void put(PlayerTown playerTown)
	{
		towns.put(playerTown.townName.toLowerCase(), playerTown);
	}
	
	// Gets the town from memory.  Returns NULL for non-existent towns
    public static PlayerTown get(String townName)
    {       	
    	if( towns.containsKey(townName.toLowerCase()) )
    		return towns.get(townName.toLowerCase());
    	else
    	{
    		System.out.println("Warning: PlayerTowns.Get called on non-existent town '" + townName + "'");
    		return null;
    	}
    }
    
    // Returns all towns
    public static ArrayList<PlayerTown> getAll()
    {
    	System.out.println("Number of towns: " + towns.values().size());
    	return new ArrayList<PlayerTown>(towns.values());
    }
    
    
    // Check for all nearby towns within minimum distance (for creating new towns)
 	public static HashMap<String, Integer> checkForNearbyTowns(Location location)
 	{
 		HashMap<String, Integer> townList = new HashMap<String, Integer>();
 		double distance; 
 		
 		for( PlayerTown town : towns.values() )
 		{
 			distance = town.centerPoint.distance(location);
 			if( distance < RageConfig.Town_MIN_DISTANCE_BETWEEN )
 				townList.put(town.townName, (int)distance);
 		}
 		
 		return townList;
 	}
    
    // Checks to see if the selected faction already has a capitol; used by /townupgrade
    public static boolean doesFactionCapitolExist(int faction)
    {
    	for( PlayerTown town : towns.values () )
    	{
    		if( town.id_Faction == faction && town.isCapitol() )
    			return true;
    	}
    	
    	return false;	// No faction capitols found
    }
    
    // Checks to see if nearby enemy capitols are too close; used by /townupgrade
    public static boolean areEnemyCapitolsTooClose(PlayerTown playerTown)
    {
    	for( PlayerTown town : towns.values () )
    	{
    		if( town.isCapitol() && town.centerPoint.distance(playerTown.centerPoint) < RageConfig.Town_MIN_DISTANCE_ENEMY_CAPITOL )
    		{
    			return true;
    		}
    	}
    	
    	return false;	// No too-close capitols found
    }

	public static PlayerTown getCurrentTown(Location location) 
	{
		for( PlayerTown town : towns.values() )
    	{
    		if( town.isInside(location) )
    		{
    			return town;
    		}
    	}
	
		// Location not inside any town; return null
		return null;
	}

}

