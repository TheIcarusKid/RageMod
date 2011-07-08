package net.rageland.ragemod.data;

import java.util.Date;

import net.rageland.ragemod.RageConfig;
import net.rageland.ragemod.RageMod;

public class PlayerTown implements Comparable<PlayerTown> {
	
	// PlayerTowns table
	public int ID_PlayerTown;
	public String TownName;
	public Location2D Coords;
	public String Faction;
	public float TreasuryBalance;
	public Date BankruptDate;
	public String Mayor;				// Name of mayor
		
	public int TownLevel;				// Corresponds to the HashMap TownLevels in Config
	
	
	// Constructor: All data
//	public PlayerTown (int _id_PlayerTown, String _townName, int _xCoord, int _zCoord, String _faction, 
//			float _treasuryBalance, Date _bankruptDate, String _townLevel, float _upkeepCost, int _size, int _maxNPCs)
//	{
//		 ID_PlayerTown = _id_PlayerTown;
//		 TownName = _townName;
//		 XCoord = _xCoord;
//		 ZCoord = _zCoord;
//		 Faction = _faction;
//		 TreasuryBalance = _treasuryBalance;
//		 BankruptDate = _bankruptDate;
//			
//		 // TownLevels table
//		 TownLevel = _townLevel;
//		 UpkeepCost = _upkeepCost;
//		 Size = _size;
//		 MaxNPCs = _maxNPCs;
//	}
	
	// Constructor: Blank
	public PlayerTown ()
	{		
	}
	
	// Implementing Comparable for sorting purposes
	public int compareTo(PlayerTown otherTown)
	{
		return otherTown.TownLevel - this.TownLevel;
	}
	
	// Checks to see whether the town is already at maximum level; used by /townupgrade
	public boolean IsAtMaxLevel()
	{
		if( Faction.equals("Neutral") )
			return TownLevel >= RageConfig.Town_MaxLevel_Neutral;
		else
			return TownLevel >= RageConfig.Town_MaxLevel_Faction;
	}
	
	public boolean IsCapitol()
	{
		return RageConfig.TownLevels.get(TownLevel).IsCapitol;
	}

	// Checks to see if the town already has its maximum number of residents
	public boolean IsFull() 
	{
		int numberOfResidents = RageMod.Database.CountResidents(TownName);
		
		return numberOfResidents >= RageConfig.TownLevels.get(TownLevel).MaxResidents;
	}
	
	// Returns all of the info for the current level
	public TownLevel GetLevel()
	{
		return RageConfig.TownLevels.get(TownLevel);
	}
	
	
	

}
