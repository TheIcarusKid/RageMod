package net.rageland.ragemod.data;

import java.util.ArrayList;

public class TownLevel 
{
	public int level;						// 1-5
	public String name;						// Settlement, etc
	public int size;						// Length on a side; forms a square
	
	public int initialCost;					// Cost to create/upgrade - minimum balance is 10% of this value
	public int upkeepCost;					// Total amount of coins subtracted from treasury daily
	public int minimumBalance;				// Automatically deposited into treasury
	
	public int maxResidents;				// Maximum number of players that can belong to this town
	public int maxNPCs;						// Number of randomly spawning NPCs
	
	public boolean isCapitol = false;		// Capitols can only be attained by factions and have special requirements
	
	public ArrayList<String> sanctumFloor;	// Encoded layout of inner sanctum floor (char = block)
	

}
