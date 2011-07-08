package net.rageland.ragemod;

import net.rageland.ragemod.data.Location2D;

import org.bukkit.Location;

// TODO: Calculate locations with y = 64  >_<

// Contains static info and utility methods for processing zone code
public class RageZones {
	
    public static String ZoneA_Name;
    public static int ZoneA_Border; 
    public static String ZoneB_Name;
    public static int ZoneB_Border; 
    public static String ZoneC_Name;
    public static int ZoneC_Border;
    
    public static Location2D WorldSpawn;
    
	public enum Action {
		TOWN_CREATE;
	}
	
	public enum Zone {
		A,
		B,
		C,
		OUTSIDE;
	}
	
	private RageMod plugin;
	
    
    public RageZones (RageMod ragemod)
    {
    	// TODO: This feels redundant.  Maybe it will make more sense when the config is loading from a file.
    	ZoneA_Name = RageConfig.ZoneA_Name;
    	ZoneA_Border = RageConfig.ZoneA_Border;
    	ZoneB_Name = RageConfig.ZoneB_Name;
    	ZoneB_Border = RageConfig.ZoneB_Border;
    	ZoneC_Name = RageConfig.ZoneC_Name;
    	ZoneC_Border = RageConfig.ZoneC_Border;
    	
    	plugin = ragemod;
    	
    	WorldSpawn = new Location2D(plugin.getServer().getWorld("world").getSpawnLocation());
    }
    
    // Returns the name of the zone the Location is currently in
    // TODO: Remove this and make calls to it use a combination of GetCurrentZone and GetName
    public static String GetName(Location location) 
    {
    	double distanceFromSpawn = WorldSpawn.distance(location);
    	
    	if( distanceFromSpawn >= 0 && distanceFromSpawn <= ZoneA_Border )
    		return ZoneA_Name;
    	else if( distanceFromSpawn <= ZoneB_Border )
    		return ZoneB_Name;
    	else if( distanceFromSpawn <= ZoneC_Border )
    		return ZoneC_Name;
    	else if( distanceFromSpawn > ZoneC_Border )
    		return "Outside All Zones";
    	else
    		return "Error: Distance from spawn returned negative";
    }
    
    // Return the name of the Zone matching the Zone enum
    public static String GetName(Zone zone)
    {
    	if( zone == Zone.A )
    		return ZoneA_Name;
    	else if( zone == Zone.B )
    		return ZoneB_Name;
    	else if( zone == Zone.C )
    		return ZoneC_Name;
    	else if( zone == Zone.OUTSIDE )
    		return "Outside All Zones";
    	else
    		return "Error: Zone unrecognized";
    }
    
    // Calculates the player's current zone based on their location
    public static Zone GetCurrentZone(Location location)
    {
		double distanceFromSpawn = WorldSpawn.distance(location);
    	
    	if( distanceFromSpawn >= 0 && distanceFromSpawn <= ZoneA_Border )
    		return Zone.A;
    	else if( distanceFromSpawn <= ZoneB_Border )
    		return Zone.B;
    	else if( distanceFromSpawn <= ZoneC_Border )
    		return Zone.C;
    	else if( distanceFromSpawn > ZoneC_Border )
    		return Zone.OUTSIDE;
    	else
    		return null;
    }
    
    public static double GetDistanceFromSpawn(Location location)
    {
    	return WorldSpawn.distance(location);
    }
    
    // Returns whether or not the location is in Zone A
    public static boolean IsInZoneA(Location location)
    {
    	return ( WorldSpawn.distance(location) >= 0 && WorldSpawn.distance(location) <= ZoneA_Border );
    }
    
    // Returns whether or not the location is in Zone A
    public static boolean IsInZoneB(Location location)
    {
    	return ( WorldSpawn.distance(location) > ZoneA_Border && WorldSpawn.distance(location) <= ZoneB_Border );
    }
    
    // Returns whether or not the location is in Zone A
    public static boolean IsInZoneC(Location location)
    {
    	return ( WorldSpawn.distance(location) > ZoneB_Border && WorldSpawn.distance(location) <= ZoneC_Border );
    }
    
    // Checks whether a specified action is allowed in the zone specified by 'location'
    public static boolean CheckPermission(Location location, Action action)
    {
    	// Put the most frequently called checks at the beginning.  On that note, would it be 
    	// better to split this method into multiple methods to prevent having to do so many comparisons?
    	if(action == Action.TOWN_CREATE)
    		return IsInZoneB(location);
    	
    	// If we haven't recognized the action, return false.  Should this throw an exception?
    	return false;
    }
    

}
