package net.rageland.ragemod.commands;

import net.rageland.ragemod.RageZones;

import org.bukkit.entity.Player;

public class OtherCommands 
{
	// /zone
	public static void ZoneCheck(Player player)
	{
		player.sendMessage("Your current zone is " + RageZones.GetName(player.getLocation()) 
				+ " and distance from spawn is " + (int)RageZones.GetDistanceFromSpawn(player.getLocation()));
	}
}
