package net.rageland.ragemod;

import java.util.Date;

// TODO: Create a colored player name that takes their data into account to be easily pulled by Commands, etc

// TODO: Should I be storing IDs for towns and such for all player data?  Then I would call the PlayerTowns hash
//		 every time I need to retrieve the name.

public class PlayerData 
{
	public int ID_Player;
	public String Name;
	public String Faction;
	public boolean IsMember;
	public Date MemberExpiration;
	public float Bounty;
	public float ExtraBounty;
	
	public boolean Home_IsSet;
	public int Home_XCoord;
	public int Home_YCoord;
	public int Home_ZCoord;
	public Date Home_LastUsed;
	
	public boolean Spawn_IsSet;
	public int Spawn_XCoord;
	public int Spawn_YCoord;
	public int Spawn_ZCoord;
	public Date Spawn_LastUsed;
	
	public String TownName;
	public boolean IsMayor;
	
	// Ugly and deprecated.  
	public PlayerData(int _id_Player, String _name, String _faction, boolean _isMember, Date _memberExpiration, float _bounty, float _extraBounty, 
			boolean _home_isSet, int _home_XCoord, int _home_YCoord,	int _home_ZCoord, Date _home_LastUsed, 
			boolean _spawn_isSet, int _spawn_XCoord, int _spawn_YCoord, int _spawn_ZCoord, Date _spawn_LastUsed, 
			String _townName, boolean _isMayor)
	{
		 ID_Player = _id_Player;
		 Name = _name;
		 Faction = _faction;
		 IsMember = _isMember;
		 MemberExpiration = _memberExpiration;
		 Bounty = _bounty;
		 ExtraBounty = _extraBounty;
		 
		 Home_IsSet = _home_isSet;
		 Home_XCoord = _home_XCoord;
		 Home_YCoord = _home_YCoord;
		 Home_ZCoord = _home_ZCoord;
		 Home_LastUsed = _home_LastUsed;
		 
		 Spawn_IsSet = _spawn_isSet;
		 Spawn_XCoord = _spawn_XCoord;
		 Spawn_YCoord = _spawn_YCoord;
		 Spawn_ZCoord = _spawn_ZCoord;
		 Spawn_LastUsed = _spawn_LastUsed;
		 
		 TownName = _townName;
		 IsMayor = _isMayor;
	}
	
	public PlayerData()
	{
		
	}
	
	
}
