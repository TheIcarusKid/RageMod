package net.rageland.ragemod.commands;

// TODO: Should the faction populations/costs be loaded upon startup, and updated in memory?

import java.util.HashMap;

import net.rageland.ragemod.RageConfig;
import net.rageland.ragemod.RageMod;
import net.rageland.ragemod.Util;
import net.rageland.ragemod.data.Factions;
import net.rageland.ragemod.data.PlayerData;
import net.rageland.ragemod.data.Players;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.Holdings;

public class FactionCommands 
{
	// /faction join
	public static void join(Player player, String factionName) 
	{
		PlayerData playerData = Players.get(player.getName());
		int id_Faction;
		Holdings balance = iConomy.getAccount(player.getName()).getHoldings();
		
		// Ensure the player is not already a member of a faction
		if( playerData.id_Faction != 0 )
		{
			Util.message(player, "You are already a member of a faction.");
			return;
		}
		
		// Calculate the cost to join each faction
		HashMap<Integer, Integer> populations = RageMod.database.getFactionPopulations();
		int lowestPopulation = 9999;
		for( int faction : populations.keySet() )
		{
			if( lowestPopulation > populations.get(faction) )
				lowestPopulation = populations.get(faction);
		}
		for( int faction : populations.keySet() )
		{
			populations.put(faction, (RageConfig.Faction_BaseJoinCost + ((populations.get(faction) - lowestPopulation) * RageConfig.Faction_JoinCostIncrease)));
		}
		
		// If the player did not type a faction name, return the cost to join each faction
		if( factionName.equals("") )
		{
			Util.message(player, "Current costs to join each faction (based on population):");
			for( int faction : populations.keySet() )
			{
				Util.message(player, "   " + Factions.getName(faction) + ": " + iConomy.format(populations.get(faction)));
			}
			return;
		}
		// Check to make sure the typed faction exists
		id_Faction = Factions.getID(factionName);
		if( id_Faction == 0 )
		{
			Util.message(player, "Faction '" + factionName + "' does not exist.");
			return;
		}
		// Check to see if the player has enough money to join the specified faction
		if( !balance.hasEnough(populations.get(id_Faction)) )
		{
			Util.message(player, "You need at least " + iConomy.format(populations.get(id_Faction)) + " to join the " + Factions.getName(id_Faction) + " faction.");
			return;
		}
		
		// Subtract from player balance
		balance.subtract(populations.get(id_Faction));
		
		// Set the player's faction
		playerData.id_Faction = id_Faction;
		Players.update(playerData);
		RageMod.database.updatePlayer(playerData);
		
		Util.message(player, "Congratulations, you are now a member of the " + Factions.getName(id_Faction) + " faction!");
	}
	
	// /faction leave
	public static void leave(Player player, boolean isConfirmed) 
	{
		PlayerData playerData = Players.get(player.getName());
		
		// Ensure the player is a member of a faction
		if( playerData.id_Faction == 0 )
		{
			Util.message(player, "You are not a member of a faction.");
			return;
		}
		
		// See if the player typed "confirm" or not
		if( !isConfirmed )
		{
			Util.message(player, "Are you sure?  You will need to pay the join fee again if you change your mind.");
			Util.message(player, "Type /faction leave confirm to leave your faction.");
		}
		else
		{
			// Reset the player's faction
			Util.message(player, "You are no longer a member of the " + Factions.getName(playerData.id_Faction) + " faction.");
			playerData.id_Faction = 0;
			Players.update(playerData);
			RageMod.database.updatePlayer(playerData);
		}
	}
	
	// /faction stats
	public static void stats(Player player)
	{
		HashMap<Integer, Integer> populations = RageMod.database.getFactionPopulations();
		
		Util.message(player, "Current faction populations (excluding inactive players):");
		for( int faction : populations.keySet() )
		{
			Util.message(player, "   " + Factions.getName(faction) + ": " + populations.get(faction) + " players");
		}
	}

}
