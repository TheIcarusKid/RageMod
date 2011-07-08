package net.rageland.ragemod.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import net.rageland.ragemod.RageConfig;
import net.rageland.ragemod.RageMod;
import net.rageland.ragemod.RageZones;
import net.rageland.ragemod.RageZones.Action;
import net.rageland.ragemod.data.Location2D;
import net.rageland.ragemod.data.PlayerData;
import net.rageland.ragemod.data.PlayerTown;
import net.rageland.ragemod.data.PlayerTowns;
import net.rageland.ragemod.data.Players;
import net.rageland.ragemod.data.TownLevel;

import org.bukkit.entity.Player;

// TODO: Text colors for player feedback

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
		if( !playerData.IsMayor )
		{
			player.sendMessage("Only town mayors can use '/town add'.");
			return;
		}		
		// Ensure that the target player is not currently a resident of a town
		if( !targetPlayerData.TownName.equals("") )
		{
			player.sendMessage(targetPlayerName + " is already a resident of '" + targetPlayerData.TownName + "'.");
			return;
		}		
		// Ensure that the target player is the same faction as the mayor
		if( !playerData.Faction.equals(targetPlayerData.Faction) )
		{
			player.sendMessage("You can only add players that are the same faction as you.");
			return;
		}
		if( PlayerTowns.Get(playerData.TownName).IsFull() )
		{
			player.sendMessage("Your town already has the maximum number of residents for its level.");
			return;
		}
		
		// Add the target to the player's town
		RageMod.Database.TownAdd(targetPlayerName, playerData.TownName);
		
		// Update the playerData
		targetPlayerData.TownName = playerData.TownName;
		Players.Update(targetPlayerData);
		
		player.sendMessage(targetPlayerName + " is now a resident of " + playerData.TownName + ".");		
	}
	
	// /town  create <town_name>
	public static void create(Player player, String townName)
	{
		// TODO: Check for permission FIRST, then only process when player types /town  create <town_name>
		// ORRRRRR just check when typing /town create without a name.  Return all towns that are too close
		
		PlayerData playerData = Players.Get(player.getName());
		HashMap<String, Integer> nearbyTowns = PlayerTowns.CheckForNearbyTowns(player.getLocation());

		// Ensure that the player is not currently a resident of a town
		if( !playerData.TownName.equals("") )
		{
			player.sendMessage("You are already a resident of '" + playerData.TownName + "'; you must use '/town leave' before you can create a new town.");
			return;
		}		
		// Ensure that the town name is not taken
		if( PlayerTowns.Get(townName) != null )
		{
			player.sendMessage("A town named " + townName + " already exists!");
			return;
		}
		// Ensure that the current zone is allowed to create towns
		if( !RageZones.CheckPermission(player.getLocation(), Action.TOWN_CREATE) )
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
			return;
		}
		
		// TODO: Check player iConomy balance, subtract appropriate amount
		
		// TODO: Also check against NPC town names
		
		// Create the town if name selected, otherwise return message
		if( !townName.equals("") )
		{
			// Add the new town to the database
			int townID = RageMod.Database.TownCreate(player, townName);
			
			// Update the playerData
			playerData.TownName = townName;
			playerData.IsMayor = true;
			Players.Update(playerData);
			
			// Update PlayerTowns
			PlayerTown playerTown = new PlayerTown();
			playerTown.ID_PlayerTown = townID;
			playerTown.TownName = townName;
			playerTown.Coords = new Location2D((int)player.getLocation().getX(), (int)player.getLocation().getZ());
			playerTown.Faction = playerData.Faction;
			playerTown.BankruptDate = null;
			playerTown.TownLevel = 1;
			playerTown.TreasuryBalance = RageConfig.TownLevels.get(1).MinimumBalance;
			playerTown.Mayor = playerData.Name;
			PlayerTowns.Put(playerTown);
			
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
		if( !playerData.IsMayor )
		{
			player.sendMessage("Only town mayors can use /town evict.");
			return;
		}		
		// Ensure that the target player is a resident of the mayor's town
		if( !targetPlayerData.TownName.equals(playerData.TownName) )
		{
			player.sendMessage(targetPlayerName + " is not a resident of " + playerData.TownName + ".");
			return;
		}
		
		// Remove the target from the player's town
		RageMod.Database.TownLeave(targetPlayerName);
		
		// Update the playerData
		targetPlayerData.TownName = "";
		Players.Update(targetPlayerData);
		
		player.sendMessage(targetPlayerName + " is no longer a resident of " + playerData.TownName + ".");		
	}
	
	// /town info [town_name]
	public static void info(Player player, String townName) 
	{
		PlayerData playerData = Players.Get(player.getName());
		PlayerTown playerTown = PlayerTowns.Get(townName);
		
		// Check to see if specified town exists
		if( playerTown == null )
		{
			player.sendMessage("The town '" + townName + "' does not exist.");
			return;
		}
		
		player.sendMessage("Info for " + townName + ":");
		player.sendMessage("   Faction: " + playerTown.Faction);
		player.sendMessage("   Level: " + playerTown.GetLevel().Name + " (" + playerTown.TownLevel + ")");
		player.sendMessage("   Mayor: " + playerTown.Mayor);
		if( playerData.TownName.equalsIgnoreCase(townName) )
		{
			player.sendMessage("   Treasury Balance: " + playerTown.TreasuryBalance + " Coins");
			
			// TODO: Return info about player deposits 
		}
	}
	
	// /town leave
	public static void leave(Player player)
	{
		PlayerData playerData = Players.Get(player.getName());
		String townName = playerData.TownName;

		// Ensure that the player is currently a resident of a town
		if( townName.equals("") )
		{
			player.sendMessage("You do not have a town to leave.");
			return;
		}		
		// Ensure that the player is not the mayor
		if( playerData.IsMayor )
		{
			player.sendMessage("Town mayors cannot use '/town leave'; contact an admin to shut down your town.");
			return;
		}
		
		// Remove the player from the town in the database
		RageMod.Database.TownLeave(playerData.Name);
		
		// Update the playerData
		playerData.TownName = "";
		playerData.IsMayor = false;
		Players.Update(playerData);
		
		player.sendMessage("You are no longer a resident of " + townName + ".");		
	}
	
	// /town list [faction]
	public static void list(Player player, String factionName) 
	{
		ArrayList<PlayerTown> towns = PlayerTowns.GetAll();
		
		// TODO: Implement page # functionality 
		
		// TODO: Check to see if faction is valid
		
		// Sorts the towns by level
		Collections.sort(towns);
		
		if( factionName.equals("") )
			player.sendMessage("List of all towns:");
		else
			player.sendMessage("List of all towns for " + factionName + " faction:");
		
		for( PlayerTown town : towns )
		{
			if( town.Faction.equalsIgnoreCase(factionName) || factionName.equals("") )
				player.sendMessage(town.Faction + ": " + town.TownName + " (" + town.GetLevel() + ")");
		}
	}
	
	// /town residents [town_name]
	public static void residents(Player player, String townName) 
	{
		PlayerTown playerTown = PlayerTowns.Get(townName);
		boolean isMayor = true;
		
		// Check to see if specified town exists
		if( playerTown == null )
		{
			player.sendMessage("The town '" + townName + "' does not exist.");
			return;
		}
		
		player.sendMessage("Residents of " + playerTown.TownName + ":");
		
		ArrayList<String> residents = RageMod.Database.ListTownResidents(townName);
		
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
		PlayerTown playerTown = PlayerTowns.Get(playerData.TownName);
		
		// Ensure that the current player is the mayor
		if( !playerData.IsMayor )
		{
			player.sendMessage("Only town mayors can use '/town upgrade'.");
			return;
		}
		// Ensure that the town is not at its maximum level
		if( playerTown.IsAtMaxLevel() )
		{
			player.sendMessage("Your town is already at its maximum level.");
			return;
		}
		
		// Load the data for the target town level
		TownLevel targetLevel = RageConfig.TownLevels.get(playerTown.TownLevel + 1);
		
		// If the upgrade would make the current town a capitol...
		if( targetLevel.IsCapitol )
		{
			// ...check to see if the player's faction already has a capitol...
			if( PlayerTowns.DoesFactionCapitolExist(playerData.Faction) )
			{
				player.sendMessage("Your faction already has a capitol; your town cannot be upgraded further.");
				return;
			}
			// ...and make sure it is not too close to enemy capitols.
			if( PlayerTowns.AreEnemyCapitolsTooClose(playerTown) )
			{
				player.sendMessage("Your town is ineligible to be your faction's capitol; it is too close to an enemy capitol.");
				return;
			}
		}
		// Check treasury balance
		if( playerTown.TreasuryBalance < targetLevel.InitialCost )
		{
			player.sendMessage("You need at least " + targetLevel.InitialCost + " Coins to upgrade your town to a " + targetLevel.Name + ".");
			return;
		}
		
		// Make the updates if confirm was typed
		if( isConfirmed ) 
		{
			// Update PlayerTowns; subtract balance from treasury; also add minimum balance
			playerTown.TownLevel = playerTown.TownLevel + 1;
			playerTown.TreasuryBalance = playerTown.TreasuryBalance - targetLevel.InitialCost + targetLevel.MinimumBalance;
			PlayerTowns.Put(playerTown);
			
			RageMod.Database.TownUpgrade(playerTown.TownName);
			
			player.sendMessage("Congratulations, " + playerTown.TownName + " has been upgraded to a " + targetLevel.Name + "!");
		}
		else
		{
			player.sendMessage("Your town is ready to be upgraded to a " + targetLevel.Name + "; type '/town upgrade confirm' to complete the upgrade.");
		}
		
	}
	
	

	



	
	
	
	
	
	
}
