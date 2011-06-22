package icarus.RageMod;

import org.bukkit.entity.Player;

// TODO: Text colors for player feedback

// The Commands class handles all the state checking for typed commands, then sends them to the database if approved
public class Commands 
{
	// /townadd <player_name>
	public static void TownAdd(Player player, String targetPlayerName)
	{
		PlayerData playerData = Players.Get(player.getName());
		PlayerData targetPlayerData = Players.Get(targetPlayerName);
		
		// Check to see if target player exists
		if( targetPlayerData == null )
		{
			player.sendMessage("Player " + targetPlayerName + " does not exist.");
			return;
		}
		// Ensure that the current player is not the mayor
		if( !playerData.IsMayor )
		{
			player.sendMessage("Only town mayors can use /townadd.");
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
		
		// Add the target to the player's town
		RageMod.Database.TownAdd(targetPlayerName, playerData.TownName);
		
		// Update the playerData
		targetPlayerData.TownName = playerData.TownName;
		
		player.sendMessage(targetPlayerName + " is now a resident of " + playerData.TownName + ".");		
	}
	
	// /towncreate <town_name>
	public static void TownCreate(Player player, String townName)
	{
		PlayerData playerData = Players.Get(player.getName());

		// Ensure that the player is not currently a resident of a town
		if( !playerData.TownName.equals("") )
		{
			player.sendMessage("You are already a resident of '" + playerData.TownName + "'; you must use /townleave before you can create a new town.");
			return;
		}		
		// Ensure that the town name is not taken
		if( PlayerTowns.Towns.containsKey(townName))
		{
			player.sendMessage("A town named " + townName + " already exists!");
			return;
		}
		
		// TODO: Town minimum distance check
		
		// TODO: Check player iConomy balance, subtract appropriate amount
		
		// TODO: Add error handling
		
		// Add the new town to the database
		RageMod.Database.TownCreate(player, townName);
		
		// Update the playerData
		playerData.TownName = townName;
		playerData.IsMayor = true;
		
		player.sendMessage("Congratulations, you are the new mayor of " + townName + "!");		
	}
	
	// /townleave
	public static void TownLeave(Player player)
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
			player.sendMessage("Town mayors cannot use /townleave; contact an admin to shut down your town.");
			return;
		}
		
		// Remove the player from the town in the database
		RageMod.Database.TownLeave(player);
		
		// Update the playerData
		playerData.TownName = "";
		playerData.IsMayor = false;
		
		player.sendMessage("You are no longer a resident of " + townName + ".");		
	}
	
}
