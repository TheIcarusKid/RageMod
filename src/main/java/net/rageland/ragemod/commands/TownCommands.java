package net.rageland.ragemod.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import net.rageland.ragemod.RageConfig;
import net.rageland.ragemod.RageMod;
import net.rageland.ragemod.RageZones;
import net.rageland.ragemod.RageZones.Action;
import net.rageland.ragemod.Util;
import net.rageland.ragemod.data.Factions;
import net.rageland.ragemod.data.Location2D;
import net.rageland.ragemod.data.PlayerData;
import net.rageland.ragemod.data.PlayerTown;
import net.rageland.ragemod.data.PlayerTowns;
import net.rageland.ragemod.data.Players;
import net.rageland.ragemod.data.TownLevel;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.iConomy.iConomy;
import com.iConomy.system.Holdings;

// TODO: Text colors for player feedback

// TODO: Keep towns from being created on zone borders

// The Commands classes handle all the state checking for typed commands, then send them to the database if approved
public class TownCommands 
{
	// /town add <player_name>
	public static void add(Player player, String targetPlayerName)
	{
		PlayerData playerData = Players.get(player.getName());
		PlayerData targetPlayerData = Players.get(targetPlayerName);
		
		// Check to see if target player exists
		if( targetPlayerData == null )
		{
			Util.message(player, "Player " + targetPlayerName + " does not exist.");
			return;
		}
		// Ensure that the current player is the mayor
		if( !playerData.isMayor )
		{
			Util.message(player, "Only town mayors can use '/town add'.");
			return;
		}		
		// Ensure that the target player is not currently a resident of a town
		if( !targetPlayerData.townName.equals("") )
		{
			Util.message(player, targetPlayerName + " is already a resident of '" + targetPlayerData.townName + "'.");
			return;
		}		
		// Ensure that the target player is the same faction as the mayor
		if( playerData.id_Faction != targetPlayerData.id_Faction )
		{
			Util.message(player, "You can only add players that are the same faction as you.");
			return;
		}
		if( PlayerTowns.get(playerData.townName).isFull() )
		{
			Util.message(player, "Your town already has the maximum number of residents for its level.");
			return;
		}
		
		// Add the target to the player's town
		RageMod.database.townAdd(targetPlayerName, playerData.townName);
		
		// Update the playerData
		targetPlayerData.townName = playerData.townName;
		// This will give the player's balance back if they were a previous resident of the town
		targetPlayerData.treasuryBalance = RageMod.database.getPlayerTreasuryBalance(targetPlayerData.id_Player, PlayerTowns.get(playerData.townName).id_PlayerTown);
		Players.update(targetPlayerData);
		
		Util.message(player, targetPlayerData.name + " is now a resident of " + playerData.townName + ".");		
	}
	
	// /town  create <town_name>
	public static void create(Player player, String townName)
	{		
		PlayerData playerData = Players.get(player.getName());
		HashMap<String, Integer> nearbyTowns = PlayerTowns.checkForNearbyTowns(player.getLocation());
		Holdings holdings = iConomy.getAccount(player.getName()).getHoldings();
		int cost = RageConfig.townLevels.get(1).initialCost;

		// Ensure that the player is not currently a resident of a town
		if( !playerData.townName.equals("") )
		{
			Util.message(player, "You are already a resident of '" + playerData.townName + "'; you must use '/town leave' before you can create a new town.");
			return;
		}		
		// Ensure that the town name is not taken
		if( PlayerTowns.get(townName) != null )
		{
			Util.message(player, "A town named " + townName + " already exists!");
			return;
		}
		// Ensure that the current zone is allowed to create towns
		if( !RageZones.checkPermission(player.getLocation(), Action.TOWN_CREATE) )
		{
			Util.message(player, "You cannot create a town in this zone.");
			return;
		}
		// Check for any towns that are too close to the current point - list all
		if( nearbyTowns.size() > 0 )
		{
			String message = "You are too close to the following towns: ";
			for( String nearbyTownName : nearbyTowns.keySet() )
			{
				message += nearbyTownName + " (" + nearbyTowns.get(nearbyTownName) + "m) ";
			}
			Util.message(player, message);
			Util.message(player, "Towns must be a minimum distance of " + RageConfig.Town_MIN_DISTANCE_BETWEEN + "m apart.");
			return;
		}
		// Check to see if the player has enough money to join the specified faction
		if( !holdings.hasEnough(cost) )
		{
			Util.message(player, "You need at least " + iConomy.format(cost) + " to create a " + RageConfig.townLevels.get(1).name + ".");
			return;
		}
		
		// Subtract from player balance
		holdings.subtract(cost);
		
		// TODO: Check against NPC town names
		
		// Create the town if name selected, otherwise return message
		if( !townName.equals("") )
		{
			// Add the new town to the database
			int townID = RageMod.database.townCreate(player, townName);
			
			// Update PlayerTowns
			PlayerTown playerTown = new PlayerTown();
			playerTown.id_PlayerTown = townID;
			playerTown.townName = townName;
			playerTown.centerPoint = new Location2D((int)player.getLocation().getX(), (int)player.getLocation().getZ());
			playerTown.id_Faction = playerData.id_Faction;
			playerTown.bankruptDate = null;
			playerTown.townLevel = RageConfig.townLevels.get(1);
			playerTown.treasuryBalance = RageConfig.townLevels.get(1).minimumBalance;
			playerTown.minimumBalance = RageConfig.townLevels.get(1).minimumBalance;
			playerTown.mayor = playerData.name;
			playerTown.world = player.getWorld();
			
			playerTown.buildRegion();
			playerTown.createBorder();
			
			PlayerTowns.put(playerTown);
			
			// Update the playerData
			playerData.townName = townName;
			playerData.isMayor = true;
			playerData.currentTown = playerTown;
			playerData.treasuryBalance = cost;
			Players.update(playerData);
			
			Util.message(player, "Congratulations, you are the new mayor of " + townName + "!");		
		}
		else
		{
			Util.message(player, "This location is valid for a new town - to create one, type '/town create <town_name>'");
		}
	}
	
	// /town deposit <amount>
	public static void deposit(Player player, String amountString)
	{
		PlayerData playerData = Players.get(player.getName());
		double amount;
		Holdings holdings = iConomy.getAccount(player.getName()).getHoldings();
		PlayerTown playerTown = PlayerTowns.get(playerData.townName);
		
		// Make sure the player is a resident of a town
		if( playerData.townName.equals("") )
		{
			Util.message(player, "Only town residents can use the deposit command.");
			return;
		}
		// Ensure that the typed amount is a valid number
		try
		{
			amount = Double.parseDouble(amountString);
		}
		catch( Exception ex )
		{
			Util.message(player, "Invalid amount.");
			return;
		}
		// Ensure that the amount is greater than 0 (no sneaky withdrawls!)
		if( amount <= 0 )
		{
			Util.message(player, "Invalid amount.");
			return;	
		}
		// Make sure the player has enough money to make the deposit
		if( !holdings.hasEnough(amount) )
		{
			Util.message(player, "You only have " + iConomy.format(holdings.balance()) + ".");
			return;
		}
		
		// Subtract the amount from the player's balance
		holdings.subtract(amount);
		
		// Update the database
		RageMod.database.townDeposit(playerTown.id_PlayerTown, playerData.id_Player, amount);
		
		// Update the town data
		playerTown.treasuryBalance += amount; 
		PlayerTowns.put(playerTown);
		
		// Update the player data
		playerData.treasuryBalance += amount;
		Players.update(playerData);
		
		Util.message(player, "Deposited " + iConomy.format(amount) + " into town treasury.");
	}
	
	// /town evict <player_name>
	public static void evict(Player player, String targetPlayerName)
	{
		PlayerData playerData = Players.get(player.getName());
		PlayerData targetPlayerData = Players.get(targetPlayerName);
		
		// Ensure that the current player is the mayor
		if( !playerData.isMayor )
		{
			Util.message(player, "Only town mayors can use /town evict.");
			return;
		}	
		// Check to see if target player exists
		if( targetPlayerData == null )
		{
			Util.message(player, "Player " + targetPlayerName + " does not exist.");
			return;
		}	
		// Ensure that the target player is a resident of the mayor's town
		if( !targetPlayerData.townName.equals(playerData.townName) )
		{
			Util.message(player, targetPlayerName + " is not a resident of " + playerData.townName + ".");
			return;
		}
		
		// Remove the target from the player's town
		RageMod.database.townLeave(targetPlayerName);
		
		// Update the playerData
		targetPlayerData.townName = "";
		targetPlayerData.spawn_IsSet = false;
		Players.update(targetPlayerData);
		
		Util.message(player, targetPlayerData.name + " is no longer a resident of " + playerData.townName + ".");		
	}
	
	// /town info [town_name]
	public static void info(Player player, String townName) 
	{
		PlayerData playerData = Players.get(player.getName());
		PlayerTown playerTown = PlayerTowns.get(townName);
		
		// Check to see if specified town exists
		if( playerTown == null )
		{
			Util.message(player, "The town '" + townName + "' does not exist.");
			return;
		}
		
		Util.message(player, "Info for " + townName + ":");
		Util.message(player, "   Faction: " + Factions.getName(playerTown.id_Faction));
		Util.message(player, "   Level: " + playerTown.getLevel().name + " (" + playerTown.townLevel.level + ")");
		Util.message(player, "   Mayor: " + playerTown.mayor);
		if( playerData.townName.equalsIgnoreCase(townName) )
		{
			Util.message(player, "   Total Balance: " + iConomy.format(playerTown.treasuryBalance));
			Util.message(player, "   Minimum Balance:  " + iConomy.format(playerTown.minimumBalance));
			Util.message(player, "   Your Balance:  " + iConomy.format(playerData.treasuryBalance));
		}
	}
	
	// /town leave
	public static void leave(Player player)
	{
		PlayerData playerData = Players.get(player.getName());
		String townName = playerData.townName;

		// Ensure that the player is currently a resident of a town
		if( townName.equals("") )
		{
			Util.message(player, "You do not have a town to leave.");
			return;
		}		
		// Ensure that the player is not the mayor
		if( playerData.isMayor )
		{
			Util.message(player, "Town mayors cannot use '/town leave'; contact an admin to shut down your town.");
			return;
		}
		
		// Remove the player from the town in the database
		RageMod.database.townLeave(playerData.name);
		
		// Update the playerData
		playerData.townName = "";
		playerData.isMayor = false;
		playerData.spawn_IsSet = false;
		playerData.treasuryBalance = 0;
		Players.update(playerData);
		
		Util.message(player, "You are no longer a resident of " + townName + ".");		
	}
	
	// /town list [faction]
	public static void list(Player player, String factionName) 
	{
		ArrayList<PlayerTown> towns = PlayerTowns.getAll();
		
		// TODO: Implement page # functionality 
		
		// TODO: Check to see if faction is valid
		
		// Sorts the towns by level
		Collections.sort(towns);
		
		if( factionName.equals("") )
			Util.message(player, "List of all towns: (" + towns.size() + ")");
		else
			Util.message(player, "List of all towns for " + factionName + " faction:");
		
		for( PlayerTown town : towns )
		{
			if( Factions.getName(town.id_Faction).equalsIgnoreCase(factionName) || factionName.equals("") )
				Util.message(player, Factions.getName(town.id_Faction) + ": " + town.townName + " (" + town.getLevel().name + ")");
		}
	}
	
	// /town minimum <amount>
	public static void minimum(Player player, String amountString)
	{
		PlayerData playerData = Players.get(player.getName());
		double amount;
		Holdings holdings = iConomy.getAccount(player.getName()).getHoldings();
		PlayerTown playerTown = PlayerTowns.get(playerData.townName);
		
		// Make sure the player is a resident of a town
		if( playerData.townName.equals("") )
		{
			Util.message(player, "Only town residents can use the deposit command.");
			return;
		}
		// Ensure that the current player is the mayor
		if( !playerData.isMayor )
		{
			Util.message(player, "Only town mayors can use /town minimum.");
			return;
		}	
		// Ensure that the typed amount is a valid number
		try
		{
			amount = Double.parseDouble(amountString);
		}
		catch( Exception ex )
		{
			Util.message(player, "Invalid amount.");
			return;
		}
		// Ensure that the amount is greater than 0
		if( amount <= 0 )
		{
			Util.message(player, "Invalid amount.");
			return;	
		}
		// Make sure the amount is not lower than the server-defined minimum balances
		if( amount < playerTown.townLevel.minimumBalance )
		{
			Util.message(player, "The lowest minimum balance allowed for a " + playerTown.townLevel.name + " is " + 
								iConomy.format(playerTown.townLevel.minimumBalance) + ".");
			return;
		}
		
		// Update the database
		RageMod.database.townSetMinimumBalance(playerTown.id_PlayerTown, amount);
		
		// Update the town data
		playerTown.minimumBalance = amount; 
		PlayerTowns.put(playerTown);
		
		Util.message(player, "Your town's treasury minimum balance is now " + iConomy.format(amount) + ".");
	}
	
	// /town residents [town_name]
	public static void residents(Player player, String townName) 
	{
		PlayerTown playerTown = PlayerTowns.get(townName);
		boolean isMayor = true;
		
		// Check to see if specified town exists
		if( playerTown == null )
		{
			Util.message(player, "The town '" + townName + "' does not exist.");
			return;
		}
		
		Util.message(player, "Residents of " + playerTown.townName + ":");
		
		ArrayList<String> residents = RageMod.database.listTownResidents(townName);
		
		for( String resident : residents )
		{
			if( isMayor )
			{
				Util.message(player, "   " + resident + " (mayor)");
				isMayor = false;
			}
			else
			{
				Util.message(player, "   " + resident);
			}
		}
	}
	
	// /town upgrade <confirm>
	public static void upgrade(Player player, boolean isConfirmed)
	{
		PlayerData playerData = Players.get(player.getName());
		PlayerTown playerTown = PlayerTowns.get(playerData.townName);
		
		// Ensure that the current player is the mayor
		if( !playerData.isMayor )
		{
			Util.message(player, "Only town mayors can use '/town upgrade'.");
			return;
		}
		// Ensure that the town is not at its maximum level
		if( playerTown.isAtMaxLevel() )
		{
			Util.message(player, "Your town is already at its maximum level.");
			return;
		}
		
		// Load the data for the target town level
		TownLevel targetLevel = RageConfig.townLevels.get(playerTown.townLevel.level + 1);
		
		// If the upgrade would make the current town a capitol...
		if( targetLevel.isCapitol )
		{
			// ...check to see if the player's faction already has a capitol...
			if( PlayerTowns.doesFactionCapitolExist(playerData.id_Faction) )
			{
				Util.message(player, "Your faction already has a capitol; your town cannot be upgraded further.");
				return;
			}
			// ...and make sure it is not too close to enemy capitols.
			if( PlayerTowns.areEnemyCapitolsTooClose(playerTown) )
			{
				Util.message(player, "Your town is ineligible to be your faction's capitol; it is too close to an enemy capitol.");
				return;
			}
		}
		// Check treasury balance
		if( playerTown.treasuryBalance < targetLevel.initialCost )
		{
			Util.message(player, "You need at least " + iConomy.format(targetLevel.initialCost) + " in your treasury to upgrade your town to a " + targetLevel.name + ".");
			return;
		}
		
		// Make the updates if confirm was typed
		if( isConfirmed ) 
		{
			// Update PlayerTowns; subtract balance from treasury; also add minimum balance
			playerTown.townLevel = RageConfig.townLevels.get(playerTown.townLevel.level + 1);
			playerTown.treasuryBalance = playerTown.treasuryBalance - targetLevel.initialCost + targetLevel.minimumBalance;
			playerTown.minimumBalance = targetLevel.minimumBalance;
			playerTown.buildRegion();
			playerTown.createBorder();
			PlayerTowns.put(playerTown);
			
			RageMod.database.townUpgrade(playerTown.townName, (targetLevel.initialCost - targetLevel.minimumBalance));
			
			Util.message(player, "Congratulations, " + playerTown.townName + " has been upgraded to a " + targetLevel.name + "!");
			Util.message(player, iConomy.format(targetLevel.initialCost) + " has been deducted from the town treasury.");
		}
		else
		{
			Util.message(player, "Your town is ready to be upgraded to a " + targetLevel.name + "; type '/town upgrade confirm' to complete the upgrade.");
		}
		
	}
	
	// /town withdrawl <amount>
	public static void withdrawl(Player player, String amountString)
	{
		PlayerData playerData = Players.get(player.getName());
		double amount;
		Holdings holdings = iConomy.getAccount(player.getName()).getHoldings();
		PlayerTown playerTown = PlayerTowns.get(playerData.townName);
		
		// Make sure the player is a resident of a town
		if( playerData.townName.equals("") )
		{
			Util.message(player, "Only town residents can use the deposit command.");
			return;
		}
		// Ensure that the typed amount is a valid number
		try
		{
			amount = Double.parseDouble(amountString);
		}
		catch( Exception ex )
		{
			Util.message(player, "Invalid amount.");
			return;
		}
		// Ensure that the amount is greater than 0
		if( amount <= 0 )
		{
			Util.message(player, "Invalid amount.");
			return;	
		}
		// Make sure the player has a high enough balance to make the withdrawl
		if( playerData.treasuryBalance < amount )
		{
			Util.message(player, "You only have " + iConomy.format(playerData.treasuryBalance) + " in the treasury.");
			return;
		}
		// Make sure the withdrawl wouldn't put the town below its minimum balance
		if( playerTown.treasuryBalance - amount < playerTown.minimumBalance )
		{
			Util.message(player, "This transaction would put the town below its minimum balance.");
			return;
		}
		
		// Add the amount to the player's balance
		holdings.add(amount);
		
		// Update the database
		RageMod.database.townDeposit(playerTown.id_PlayerTown, playerData.id_Player, (amount * -1));
		
		// Update the town data
		playerTown.treasuryBalance -= amount; 
		PlayerTowns.put(playerTown);
		
		// Update the player data
		playerData.treasuryBalance -= amount;
		Players.update(playerData);
		
		Util.message(player, "Withdrew " + iConomy.format(amount) + " from town treasury.");
	}
	
	

	



	
	
	
	
	
	
}
