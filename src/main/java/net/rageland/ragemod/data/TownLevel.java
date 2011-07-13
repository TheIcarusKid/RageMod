package net.rageland.ragemod.data;

import java.util.ArrayList;

public class TownLevel 
{
	public int Level;						// 1-5
	public String Name;						// Settlement, etc
	public int Size;						// Length on a side; forms a square
	
	public int InitialCost;					// Cost to create/upgrade - minimum balance is 10% of this value
	public int UpkeepCost;					// Total amount of coins subtracted from treasury daily
	public int MinimumBalance;				// Automatically deposited into treasury
	
	public int MaxResidents;				// Maximum number of players that can belong to this town
	public int MaxNPCs;						// Number of randomly spawning NPCs
	
	public boolean IsCapitol = false;		// Capitols can only be attained by factions and have special requirements
	
	public ArrayList<String> sanctumFloor;	// Encoded layout of inner sanctum floor (char = block)
	

}
