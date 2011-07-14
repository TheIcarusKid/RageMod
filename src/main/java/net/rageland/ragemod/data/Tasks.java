package net.rageland.ragemod.data;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;

import com.iConomy.iConomy;
import com.iConomy.system.Holdings;

import net.rageland.ragemod.RageConfig;
import net.rageland.ragemod.RageMod;
import net.rageland.ragemod.Util;


public class Tasks {
	
	// Set up PlayerTowns as a static instance
	private static volatile Tasks instance;
	
	private static HashMap<String, Timestamp> tasks;
	
    public static Tasks getInstance() 
    {
		if (instance == null) 
		{
			instance = new Tasks();
		}
		return instance;
	}
	
	// On startup, pull all records of when tasks ran last
	public void loadTaskTimes()
	{
		tasks = RageMod.database.loadTaskTimes();	
	}
	
	// Log task as complete
	public static void setComplete(String taskName)
	{
		tasks.put(taskName, Util.now());
		RageMod.database.setComplete(taskName);
	}
	
	// Get number of seconds since last task run
	public static int getSeconds(String taskName)
	{
		// If we have a null value, the task has never been run, so return a huge value to force it to run
		if( !tasks.containsKey(taskName) || tasks.get(taskName) == null )
			return Integer.MAX_VALUE;
		else
			return (int)((Util.now().getTime() - tasks.get(taskName).getTime()) / 1000);
	}
	
	// *** TASK CODE ***
	
	// Charge taxes for player towns
	public static void processTownUpkeeps()
	{
		double remaining;
		double cost = RageConfig.Town_UPKEEP_PER_PLAYER;
		Holdings holdings;
		PlayerData playerData;
		
		System.out.println("Beginning town upkeep processing...");
		
		for( PlayerTown town : PlayerTowns.getAll() )
		{
			// Set the total amount needed to be collected
			remaining = town.getLevel().upkeepCost;
			
			// Move through each town resident to collect money
			for( String playerName : RageMod.database.listTownResidents(town.townName))
			{
				holdings = iConomy.getAccount(playerName).getHoldings();
				playerData = Players.get(playerName);
				
				// Attempt to collect from player's iConomy balance first
				if( holdings.hasEnough(cost) )
				{
					holdings.subtract(cost);
					remaining -= cost;
				}
				// If they don't have enough on hand, see if they have deposits in the treasury
				else if( playerData.treasuryBalance >= cost )
				{
					playerData.treasuryBalance -= cost;
					town.treasuryBalance -= cost;
					remaining -= cost;
					Players.update(playerData);
					RageMod.database.updatePlayer(playerData);
				}
				// If the player doesn't have the funds in either area, evict their freeloading ass
				else if( !playerData.isMayor )
				{
					playerData.townName = "";
					playerData.spawn_IsSet = false;
					playerData.treasuryBalance = 0;
					Players.update(playerData);
					
					// TODO: Inform the player of their eviction somehow
				}
			}
			
			// At this point we will have collected an amount of money less, equal to, or more than the town's upkeep
			town.treasuryBalance -= remaining;
			
			// If the treasury balance is in the negative, the town is bankrupt
			if( town.treasuryBalance < 0 )
			{
				if( town.bankruptDate == null )
				{
					System.out.println("The town of " + town.townName + " is now bankrupt.");
					town.bankruptDate = Util.now();
				}
				else
				{
					// At this point the town has been bankrupt for some time - delete after specified time
					if( Util.daysBetween(Util.now(), town.bankruptDate) > RageConfig.Town_MAX_BANKRUPT_DAYS )
					{
						// TODO: Finish writing this
					}
				}
				
			}
			else
			{
				// TODO: Set bankrupt date null
			}
			
			PlayerTowns.put(town);
			
			
		}
		

	}
	
	
}

