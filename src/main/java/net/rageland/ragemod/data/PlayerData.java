package net.rageland.ragemod.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.bukkit.Location;
import org.bukkit.World;

import net.rageland.ragemod.RageConfig;
import net.rageland.ragemod.RageMod;
import net.rageland.ragemod.RageZones;
import net.rageland.ragemod.RageZones.Zone;

// TODO: Create a colored player name that takes their data into account to be easily pulled by Commands, etc

// TODO: Should I be storing IDs for towns and such for all player data?  Then I would call the PlayerTowns hash
//		 every time I need to retrieve the name.

public class PlayerData 
{
	// ***** DATABASE VALUES *****
	
	// Basic data
	public int id_Player;
	public String name;
	public int id_Faction;
	public boolean isMember;
	public Date memberExpiration;
	public float bounty;
	public float extraBounty;
	
	// Home (used for capitol lots)
	public boolean home_IsSet;
	public World home_World;
	public int home_X;
	public int home_Y;
	public int home_Z;
	public Timestamp home_LastUsed;
	
	// Spawn (used for player town beds)
	public boolean spawn_IsSet;
	public World spawn_World;
	public int spawn_X;
	public int spawn_Y;
	public int spawn_Z;
	public Timestamp spawn_LastUsed;
	
	// Town info
	public String townName;
	public boolean isMayor;
	
	// World capitol city lots
	public ArrayList<Lot> lots = new ArrayList<Lot>();
	
	
	// ***** STATE (Non-DB) VALUES *****
	
	// Current location
	public Zone currentZone;
	public PlayerTown currentTown;
	public boolean isInCapitol;
	
	
	// Sets the spawn location when bed clicked
	public void setSpawn(Location location)
	{
		spawn_IsSet = true;
		spawn_World = location.getWorld();
		spawn_X = (int)location.getX();
		spawn_Y = (int)location.getY() + 2;
		spawn_Z = (int)location.getZ();
	}
	
	// Returns a Location object of the spawn location
	public Location getSpawnLocation()
	{
		return new Location(spawn_World, spawn_X + .5, spawn_Y, spawn_Z + .5);
	}
	
	// Sets the home location when bed clicked
	public void setHome(Location location)
	{
		home_IsSet = true;
		home_World = location.getWorld();
		home_X = (int)location.getX();
		home_Y = (int)location.getY() + 2;
		home_Z = (int)location.getZ();
	}
	
	// Returns a Location object of the home location
	public Location getHomeLocation()
	{
		return new Location(home_World, home_X + .5, home_Y, home_Z + .5);
	}
	
	// Checks whether the current location is inside one of the player's lots
	public boolean isInsideLot(Location location)
	{
		for( Lot lot : this.lots )
		{
			if( lot.isInside(location) )
			{
				return true;
			}
		}
		
		// The location was not in any of the player's lots
		return false;
	}
	
	// Clears the spawn location when bed is broken	
	public void clearSpawn() 
	{
		spawn_IsSet = false;
	}
	
	// Clears the home location when bed is broken	
	public void clearHome() 
	{
		home_IsSet = false;
	}
		
	
	
}
