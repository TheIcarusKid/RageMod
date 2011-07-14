package net.rageland.ragemod.commands;

import java.sql.Timestamp;
import java.util.Date;

import net.rageland.ragemod.RageConfig;
import net.rageland.ragemod.RageMod;
import net.rageland.ragemod.RageZones;
import net.rageland.ragemod.Util;
import net.rageland.ragemod.data.PlayerData;
import net.rageland.ragemod.data.PlayerTowns;
import net.rageland.ragemod.data.Players;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Commands 
{
	// /zone
	public static void zone(Player player)
	{
		player.sendMessage("Your current zone is " + RageZones.getName(player.getLocation()) 
				+ " and distance from spawn is " + (int)RageZones.getDistanceFromSpawn(player.getLocation()));
	}

	// /home [player_name]
	public static void home(Player player, String targetPlayerName) 
	{
		PlayerData playerData = Players.get(player.getName());
		PlayerData targetPlayerData = Players.get(targetPlayerName);
		Timestamp now = new Timestamp(new Date().getTime());
		Location destination;
		
		// Check to see if target player exists
		if( targetPlayerData == null )
		{
			player.sendMessage("Player " + targetPlayerName + " does not exist.");
			return;
		}
		
		// Check permissions so only mods and admins can go to another player's home
		if( targetPlayerData.id_Player != playerData.id_Player && !RageMod.permissionHandler.has(player, "ragemod.referee.inspectspawn") )
		{
			player.sendMessage("Only mods and admins can teleport to other players' home points.");
			return;
		}
		
		// See if player has an active membership
		if( !playerData.isMember )
		{
			player.sendMessage("Only active Rageland members can use /home.");
			return;
		}
		
		// Check to see if home is on cooldown
		if( playerData.home_LastUsed != null )
		{
			int secondsSinceLastUse = (int)((now.getTime() - playerData.home_LastUsed.getTime()) / 1000);
			if( secondsSinceLastUse < RageConfig.Cooldown_Home )
			{
				player.sendMessage("Spell '/home' is not ready yet (" + Util.formatCooldown(RageConfig.Cooldown_Home - secondsSinceLastUse) + " left)");
				return;
			}
		}
		// Make sure the player has set a home
		if( !targetPlayerData.home_IsSet )			
		{
			player.sendMessage("You have not yet set a /home (place a bed inside your lot).");
			return;
		}
		
		player.sendMessage("Teleporting...");
		destination = new Location(player.getServer().getWorld("world"), targetPlayerData.home_X + .5, targetPlayerData.home_Y, targetPlayerData.home_Z + .5 );
		player.teleport(destination);
		playerData.home_LastUsed = now;
		Players.update(playerData);
		RageMod.database.updatePlayer(playerData);
	}
	
	// /spawn [player_name]
	public static void spawn(Player player, String targetPlayerName) 
	{
		PlayerData playerData = Players.get(player.getName());
		PlayerData targetPlayerData = Players.get(targetPlayerName);
		Timestamp now = new Timestamp(new Date().getTime());
		Location destination;
		
		// Check to see if target player exists
		if( targetPlayerData == null )
		{
			player.sendMessage("Player " + targetPlayerName + " does not exist.");
			return;
		}
		
		// Check permissions so only mods and admins can go to another player's spawn
		if( targetPlayerData.id_Player != playerData.id_Player && !RageMod.permissionHandler.has(player, "ragemod.referee.inspectspawn") )
		{
			player.sendMessage("Only mods and admins can teleport to other players' spawn points.");
			return;
		}
		
		// Check to see if spawn is on cooldown
		if( playerData.spawn_LastUsed != null )
		{
			int secondsSinceLastUse = (int)((now.getTime() - playerData.spawn_LastUsed.getTime()) / 1000);
			if( secondsSinceLastUse < RageConfig.Cooldown_Spawn )
			{
				player.sendMessage("Spell '/spawn' is not ready yet (" + Util.formatCooldown(RageConfig.Cooldown_Spawn - secondsSinceLastUse) + " left)");
				return;
			}
		}
		
		if( targetPlayerData.spawn_IsSet )			
			destination = new Location(player.getServer().getWorld("world"), targetPlayerData.spawn_X + .5, targetPlayerData.spawn_Y, targetPlayerData.spawn_Z + .5 );
		else
			destination = player.getWorld().getSpawnLocation();
		
		player.sendMessage("Teleporting...");
		player.teleport(destination);
		playerData.spawn_LastUsed = now;
		Players.update(playerData);
		RageMod.database.updatePlayer(playerData);
	}
	
	
}
