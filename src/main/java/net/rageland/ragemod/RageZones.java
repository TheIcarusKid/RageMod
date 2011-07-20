package net.rageland.ragemod;

import net.rageland.ragemod.data.Location2D;
import net.rageland.ragemod.data.Region2D;
import net.rageland.ragemod.data.Region3D;

import org.bukkit.Location;
import org.bukkit.World;

// TODO: Calculate locations with y = 64  >_<

// Contains static info and utility methods for processing zone code
public class RageZones {
	
    public static String ZoneA_Name;
    public static int ZoneA_Border; 
    public static String ZoneB_Name;
    public static int ZoneB_Border; 
    public static String ZoneC_Name;
    public static int ZoneC_Border;
    
    public static World world;
    public static Location2D WorldSpawn;
    
    private static Region2D Capitol_RegionA;
    private static Region2D Capitol_RegionB;
    
    public static Region3D Capitol_SandLot;
    
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
    	plugin = ragemod;
    	world = plugin.getServer().getWorld("world");
    	
    	// TODO: This feels redundant.  Maybe it will make more sense when the config is loading from a file.
    	ZoneA_Name = RageConfig.Zone_NAME_A;
    	ZoneA_Border = RageConfig.Zone_BORDER_A;
    	ZoneB_Name = RageConfig.Zone_NAME_B;
    	ZoneB_Border = RageConfig.Zone_BORDER_B;
    	ZoneC_Name = RageConfig.Zone_NAME_C;
    	ZoneC_Border = RageConfig.Zone_BORDER_C;
    	
    	// Load the capitol regions
    	Capitol_RegionA = new Region2D(RageConfig.Capitol_X1a, RageConfig.Capitol_Z1a, RageConfig.Capitol_X2a, RageConfig.Capitol_Z2a);
    	Capitol_RegionB = new Region2D(RageConfig.Capitol_X1b, RageConfig.Capitol_Z1b, RageConfig.Capitol_X2b, RageConfig.Capitol_Z2b);
    	Capitol_SandLot = new Region3D(world, RageConfig.Capitol_SANDLOT);
    	
    	WorldSpawn = new Location2D(world.getSpawnLocation());
    	
    }
    
    // Returns the name of the zone the Location is currently in
    // TODO: Remove this and make calls to it use a combination of GetCurrentZone and GetName
    public static String getName(Location location) 
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
    public static String getName(Zone zone)
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
    public static Zone getCurrentZone(Location location)
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
    
    public static double getDistanceFromSpawn(Location location)
    {
    	return WorldSpawn.distance(location);
    }
    
    // Returns whether or not the location is in Zone A
    public static boolean isInZoneA(Location location)
    {
    	return ( WorldSpawn.distance(location) >= 0 && WorldSpawn.distance(location) <= ZoneA_Border );
    }
    
    // Returns whether or not the location is in Zone A
    public static boolean isInZoneB(Location location)
    {
    	return ( WorldSpawn.distance(location) > ZoneA_Border && WorldSpawn.distance(location) <= ZoneB_Border );
    }
    
    // Returns whether or not the location is in Zone A
    public static boolean isInZoneC(Location location)
    {
    	return ( WorldSpawn.distance(location) > ZoneB_Border && WorldSpawn.distance(location) <= ZoneC_Border );
    }
    
    // Returns whether the player is in the world capitol
    public static boolean isInCapitol(Location location)
    {
    	return ( Capitol_RegionA.isInside(location) || Capitol_RegionB.isInside(location) );
    }
    
    // Checks whether a specified action is allowed in the zone specified by 'location'
    public static boolean checkPermission(Location location, Action action)
    {
    	// Put the most frequently called checks at the beginning.  On that note, would it be 
    	// better to split this method into multiple methods to prevent having to do so many comparisons?
    	if(action == Action.TOWN_CREATE)
    		return isInZoneB(location);
    	
    	// If we haven't recognized the action, return false.  Should this throw an exception?
    	return false;
    }
  
    // Checks to see whether the location is inside the sand lot
    public static boolean isInSandlot(Location location)
    {
    	return ( Capitol_SandLot.isInside(location) );
    }
    

}
