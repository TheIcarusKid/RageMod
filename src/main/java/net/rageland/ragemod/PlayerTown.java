package net.rageland.ragemod;

import java.util.Date;

public class PlayerTown {
	
	// PlayerTowns table
	public int ID_PlayerTown;
	public String TownName;
	public int XCoord;
	public int ZCoord;
	public String Faction;
	public float TreasuryBalance;
	public Date BankruptDate;
		
	// TownLevels table
	public String TownLevel;
	public float UpkeepCost;
	public int Size;
	public int MaxNPCs;
	
	
	// Constructor: All data
	public PlayerTown (int _id_PlayerTown, String _townName, int _xCoord, int _zCoord, String _faction, 
			float _treasuryBalance, Date _bankruptDate, String _townLevel, float _upkeepCost, int _size, int _maxNPCs)
	{
		 ID_PlayerTown = _id_PlayerTown;
		 TownName = _townName;
		 XCoord = _xCoord;
		 ZCoord = _zCoord;
		 Faction = _faction;
		 TreasuryBalance = _treasuryBalance;
		 BankruptDate = _bankruptDate;
			
		 // TownLevels table
		 TownLevel = _townLevel;
		 UpkeepCost = _upkeepCost;
		 Size = _size;
		 MaxNPCs = _maxNPCs;
	}
	
	// Constructor: Blank
	public PlayerTown ()
	{		
	}
	
	
	
	

}
