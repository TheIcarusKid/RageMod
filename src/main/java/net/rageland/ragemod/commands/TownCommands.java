package net.rageland.ragemod.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import net.rageland.ragemod.RageConfig;
import net.rageland.ragemod.RageMod;
import net.rageland.ragemod.RageZones;
import net.rageland.ragemod.RageZones.Action;
import net.rageland.ragemod.data.Factions;
import net.rageland.ragemod.data.Location2D;
import net.rageland.ragemod.data.PlayerData;
import net.rageland.ragemod.data.PlayerTown;
import net.rageland.ragemod.data.PlayerTowns;
import net.rageland.ragemod.data.Players;
import net.rageland.ragemod.data.TownLevel;

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
		PlayerData playerData = Players.Get(player.getName());
		PlayerData targetPlayerData = Players.Get(targetPlayerName);
		
		// Check to see if target player exists
		if( targetPlayerData == null )
		{
			player.sendMessage("Player " + targetPlayerName + " does not exist.");
			return;
		}
		// Ensure that the current player is the mayor
		if( !playerData.isMayor )
		{
			player.sendMessage("Only town mayors can use '/town add'.");
			return;
		}		
		// Ensure that the target player is not currently a resident of a town
		if( !targetPlayerData.townName.equals("") )
		{
			player.sendMessage(targetPlayerName + " is already a resident of '" + targetPlayerData.townName + "'.");
			return;
		}		
		// Ensure that the target player is the same faction as the mayor
		if( playerData.id_Faction != targetPlayerData.id_Faction )
		{
			player.sendMessage("You can only add players that are the same faction as you.");
			return;
		}
		if( PlayerTowns.get(playerData.townName).isFull() )
		{
			player.sendMessage("Your town already has the maximum number of residents for its level.");
			return;
		}
		
		// Add the target to the player's town
		RageMod.Database.townAdd(targetPlayerName, playerData.townName);
		
		// Update the playerData
		targetPlayerData.townName = playerData.townName;
		Players.Update(targetPlayerData);
		
		player.sendMessage(targetPlayerName + " is now a resident of " + playerData.townName + ".");		
	}
	
	// /town  create <town_name>
	public static void create(Player player, String townName)
	{		
		PlayerData playerData = Players.Get(player.getName());
		HashMap<String, Integer> nearbyTowns = PlayerTowns.checkForNearbyTowns(player.getLocation());
		Holdings holdings = iConomy.getAccount(player.getName()).getHoldings();
		int cost = RageConfig.townLevels.get(1).InitialCost;

		// Ensure that the player is not currently a resident of a town
		if( !playerData.townName.equals("") )
		{
			player.sendMessage("You are already a resident of '" + playerData.townName + "'; you must use '/town leave' before you can create a new town.");
			return;
		}		
		// Ensure that the town name is not taken
		if( PlayerTowns.get(townName) != null )
		{
			player.sendMessage("A town named " + townName + " already exists!");
			return;
		}
		// Ensure that the current zone is allowed to create towns
		if( !RageZones.checkPermission(player.getLocation(), Action.TOWN_CREATE) )
		{
			player.sendMessage("You cannot create a town in this zone.");
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
			player.sendMessage(message);
			player.sendMessage("Towns must be a minimum distance of " + RageConfig.Town_MIN_DISTANCE_BETWEEN + "m apart.");
			return;
		}
		// Check to see if the player has enough money to join the specified faction
		if( !holdings.hasEnough(cost) )
		{
			player.sendMessage("You need at least " + iConomy.format(cost) + " to create a " + RageConfig.townLevels.get(1).Name + ".");
			return;
		}
		
		// Subtract from player balance
		holdings.subtract(cost);
		
		// TODO: Check against NPC town names
		
		// Create the town if name selected, otherwise return message
		if( !townName.equals("") )
		{
			// Add the new town to the database
			int townID = RageMod.Database.townCreate(player, townName);
			
			// Update PlayerTowns
			PlayerTown playerTown = new PlayerTown();
			playerTown.id_PlayerTown = townID;
			playerTown.townName = townName;
			playerTown.centerPoint = new Location2D((int)player.getLocation().getX(), (int)player.getLocation().getZ());
			playerTown.id_Faction = playerData.id_Faction;
			playerTown.bankruptDate = null;
			playerTown.townLevel = RageConfig.townLevels.get(1);
			playerTown.treasuryBalance = RageConfig.townLevels.get(1).MinimumBalance;
			playerTown.mayor = playerData.name;
			playerTown.world = player.getWorld();
			
			playerTown.buildRegion();
			playerTown.createBorder();
			
			PlayerTowns.put(playerTown);
			
			// Update the playerData
			playerData.townName = townName;
			playerData.isMayor = true;
			playerData.currentTown = playerTown;
			Players.Update(playerData);
			
			player.sendMessage("Congratulations, you are the new mayor of " + townName + "!");		
		}
		else
		{
			player.sendMessage("This location is valid for a new town - to create one, type '/town create <town_name>'");
		}
	}
	
	// /town evict <player_name>
	public static void evict(Player player, String targetPlayerName)
	{
		PlayerData playerData = Players.Get(player.getName());
		PlayerData targetPlayerData = Players.Get(targetPlayerName);
		
		// Check to see if target player exists
		if( targetPlayerData == null )
		{
			player.sendMessage("Player " + targetPlayerName + " does not exist.");
			return;
		}
		// Ensure that the current player is the mayor
		if( !playerData.isMayor )
		{
			player.sendMessage("Only town mayors can use /town evict.");
			return;
		}		
		// Ensure that the target player is a resident of the mayor's town
		if( !targetPlayerData.townName.equals(playerData.townName) )
		{
			player.sendMessage(targetPlayerName + " is not a resident of " + playerData.townName + ".");
			return;
		}
		
		// Remove the target from the player's town
		RageMod.Database.townLeave(targetPlayerName);
		
		// Update the playerData
		targetPlayerData.townName = "";
		targetPlayerData.spawn_IsSet = false;
		Players.Update(targetPlayerData);
		
		player.sendMessage(targetPlayerName + " is no longer a resident of " + playerData.townName + ".");		
	}
	
	// /town info [town_name]
	public static void info(Player player, String townName) 
	{
		PlayerData playerData = Players.Get(player.getName());
		PlayerTown playerTown = PlayerTowns.get(townName);
		
		// Check to see if specified town exists
		if( playerTown == null )
		{
			player.sendMessage("The town '" + townName + "' does not exist.");
			return;
		}
		
		player.sendMessage("Info for " + townName + ":");
		player.sendMessage("   Faction: " + Factions.getName(playerTown.id_Faction));
		player.sendMessage("   Level: " + playerTown.getLevel().Name + " (" + playerTown.townLevel.Level + ")");
		player.sendMessage("   Mayor: " + playerTown.mayor);
		if( playerData.townName.equalsIgnoreCase(townName) )
		{
			player.sendMessage("   Treasury Balance: " + playerTown.treasuryBalance + " Coins");
			
			// TODO: Return info about player deposits 
		}
	}
	
	// /town leave
	public static void leave(Player player)
	{
		PlayerData playerData = Players.Get(player.getName());
		String townName = playerData.townName;

		// Ensure that the player is currently a resident of a town
		if( townName.equals("") )
		{
			player.sendMessage("You do not have a town to leave.");
			return;
		}		
		// Ensure that the player is not the mayor
		if( playerData.isMayor )
		{
			player.sendMessage("Town mayors cannot use '/town leave'; contact an admin to shut down your town.");
			return;
		}
		
		// Remove the player from the town in the database
		RageMod.Database.townLeave(playerData.name);
		
		// Update the playerData
		playerData.townName = "";
		playerData.isMayor = false;
		playerData.spawn_IsSet = false;
		Players.Update(playerData);
		
		player.sendMessage("You are no longer a resident of " + townName + ".");		
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
			player.sendMessage("List of all towns: (" + towns.size() + ")");
		else
			player.sendMessage("List of all towns for " + factionName + " faction:");
		
		for( PlayerTown town : towns )
		{
			if( Factions.getName(town.id_Faction).equalsIgnoreCase(factionName) || factionName.equals("") )
				player.sendMessage(Factions.getName(town.id_Faction) + ": " + town.townName + " (" + town.getLevel().Name + ")");
		}
	}
	
	// /town residents [town_name]
	public static void residents(Player player, String townName) 
	{
		PlayerTown playerTown = PlayerTowns.get(townName);
		boolean isMayor = true;
		
		// Check to see if specified town exists
		if( playerTown == null )
		{
			player.sendMessage("The town '" + townName + "' does not exist.");
			return;
		}
		
		player.sendMessage("Residents of " + playerTown.townName + ":");
		
		ArrayList<String> residents = RageMod.Database.listTownResidents(townName);
		
		for( String resident : residents )
		{
			if( isMayor )
			{
				player.sendMessage("   " + resident + " (mayor)");
				isMayor = false;
			}
			else
			{
				player.sendMessage("   " + resident);
			}
		}
	}
	
	// /town upgrade <confirm>
	public static void upgrade(Player player, boolean isConfirmed)
	{
		PlayerData playerData = Players.Get(player.getName());
		PlayerTown playerTown = PlayerTowns.get(playerData.townName);
		
		// Ensure that the current player is the mayor
		if( !playerData.isMayor )
		{
			player.sendMessage("Only town mayors can use '/town upgrade'.");
			return;
		}
		// Ensure that the town is not at its maximum level
		if( playerTown.isAtMaxLevel() )
		{
			player.sendMessage("Your town is already at its maximum level.");
			return;
		}
		
		// Load the data for the target town level
		TownLevel targetLevel = RageConfig.townLevels.get(playerTown.townLevel.Level + 1);
		
		// If the upgrade would make the current town a capitol...
		if( targetLevel.IsCapitol )
		{
			// ...check to see if the player's faction already has a capitol...
			if( PlayerTowns.doesFactionCapitolExist(playerData.id_Faction) )
			{
				player.sendMessage("Your faction already has a capitol; your town cannot be upgraded further.");
				return;
			}
			// ...and make sure it is not too close to enemy capitols.
			if( PlayerTowns.areEnemyCapitolsTooClose(playerTown) )
			{
				player.sendMessage("Your town is ineligible to be your faction's capitol; it is too close to an enemy capitol.");
				return;
			}
		}
		// Check treasury balance
		if( playerTown.treasuryBalance < targetLevel.InitialCost )
		{
			player.sendMessage("You need at least " + iConomy.format(targetLevel.InitialCost) + " in your treasury to upgrade your town to a " + targetLevel.Name + ".");
			return;
		}
		
		// Make the updates if confirm was typed
		if( isConfirmed ) 
		{
			// Update PlayerTowns; subtract balance from treasury; also add minimum balance
			playerTown.townLevel = RageConfig.townLevels.get(playerTown.townLevel.Level + 1);
			playerTown.treasuryBalance = playerTown.treasuryBalance - targetLevel.InitialCost + targetLevel.MinimumBalance;
			playerTown.buildRegion();
			playerTown.createBorder();
			PlayerTowns.put(playerTown);
			
			RageMod.Database.townUpgrade(playerTown.townName, (targetLevel.InitialCost - targetLevel.MinimumBalance));
			
			player.sendMessage("Congratulations, " + playerTown.townName + " has been upgraded to a " + targetLevel.Name + "!");
		}
		else
		{
			player.sendMessage("Your town is ready to be upgraded to a " + targetLevel.Name + "; type '/town upgrade confirm' to complete the upgrade.");
		}
		
	}
	
	

	



	
	
	
	
	
	
}
