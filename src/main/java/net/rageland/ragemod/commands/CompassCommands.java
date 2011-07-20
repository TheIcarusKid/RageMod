package net.rageland.ragemod.commands;

import net.rageland.ragemod.Util;
import net.rageland.ragemod.data.Lot;
import net.rageland.ragemod.data.Lots;
import net.rageland.ragemod.data.PlayerTown;
import net.rageland.ragemod.data.PlayerTowns;

import org.bukkit.entity.Player;

public class CompassCommands 
{

	// /compass lot <lot_code>
	public static void lot(Player player, String lotCode) 
	{
		Lot lot = Lots.get(lotCode);
		
		// lot will be null if code is invalid
		if( lot == null )
		{	
			Util.message(player, lotCode + " is not a valid lot code.  (consult the online map)");
			return;
		}
		
		player.setCompassTarget(lot.getCenter());
		Util.message(player, "Compass target set to lot " + lot.getLotCode() + ".");
	}

	// /compass spawn
	public static void spawn(Player player) 
	{
		player.setCompassTarget(player.getServer().getWorld("world").getSpawnLocation());
		Util.message(player, "Compass target set to world spawn.");
	}

	// /compass town <town_name>
	public static void town(Player player, String townName) 
	{
		PlayerTown playerTown = PlayerTowns.get(townName);
		
		// Check to see if specified town exists
		if( playerTown == null )
		{
			Util.message(player, "The town '" + townName + "' does not exist.");
			return;
		}
		
		player.setCompassTarget(playerTown.getCenter());
		Util.message(player, "Compass target set to " + playerTown.townName + ".");
		
	}

}
