package net.rageland.ragemod.data;

import java.util.ArrayList;
import java.util.Date;

import net.rageland.ragemod.RageZones;
import net.rageland.ragemod.RageZones.Zone;

// TODO: Create a colored player name that takes their data into account to be easily pulled by Commands, etc

// TODO: Should I be storing IDs for towns and such for all player data?  Then I would call the PlayerTowns hash
//		 every time I need to retrieve the name.

public class PlayerData 
{
	// ***** DATABASE VALUES *****
	
	// Basic data
	public int ID_Player;
	public String Name;
	public String Faction;
	public boolean IsMember;
	public Date MemberExpiration;
	public float Bounty;
	public float ExtraBounty;
	
	// Home (used for capitol lots)
	public boolean Home_IsSet;
	public int Home_XCoord;
	public int Home_YCoord;
	public int Home_ZCoord;
	public Date Home_LastUsed;
	
	// Spawn (used for player town beds)
	public boolean Spawn_IsSet;
	public int Spawn_XCoord;
	public int Spawn_YCoord;
	public int Spawn_ZCoord;
	public Date Spawn_LastUsed;
	
	// Town info
	public String TownName;
	public boolean IsMayor;
	
	// World capitol city lots
	public ArrayList<Lot> Lots;
	
	
	// ***** STATE (Non-DB) VALUES *****
	
	// Current location
	public Zone CurrentZone;
	
	
	
	
	
	
}
