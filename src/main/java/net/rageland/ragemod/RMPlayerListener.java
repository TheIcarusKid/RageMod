package net.rageland.ragemod;

// TODO: Make a Util class that parses player.sendMessage and highlights /commands, <required>, [optional], and (info)
// Also create a default color

// TODO: Check whether we need to clear any homes or spawns when a bed is destroyed

// TODO: Make sure location checks check the world that the player is in

// TODO: Add a /lot invite command to let players work together on buildings (also uninvite)

import java.util.HashMap;

import net.rageland.ragemod.RageZones.Zone;
import net.rageland.ragemod.commands.CompassCommands;
import net.rageland.ragemod.commands.DebugCommands;
import net.rageland.ragemod.commands.FactionCommands;
import net.rageland.ragemod.commands.LotCommands;
import net.rageland.ragemod.commands.Commands;
import net.rageland.ragemod.commands.TownCommands;
import net.rageland.ragemod.data.Factions;
import net.rageland.ragemod.data.Lot;
import net.rageland.ragemod.data.PlayerData;
import net.rageland.ragemod.data.PlayerTown;
import net.rageland.ragemod.data.PlayerTowns;
import net.rageland.ragemod.data.Players;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.World;

/**
 * Handle events for all Player related events
 * @author TheIcarusKid
 */
public class RMPlayerListener extends PlayerListener 
{
    private final RageMod plugin;

    public RMPlayerListener(RageMod instance) 
    {
        plugin = instance;
    }

    // Pull the player data from the DB and register in memory
    public void onPlayerJoin(PlayerJoinEvent event)
    {
    	Player player = event.getPlayer();    	
    	PlayerData playerData = Players.playerLogin(player.getName());    	  
    	
    	// Set the state info
    	playerData.currentZone = RageZones.getCurrentZone(player.getLocation());
    	playerData.currentTown = PlayerTowns.getCurrentTown(player.getLocation());
    	playerData.isInCapitol = RageZones.isInCapitol(player.getLocation());
    	Players.update(playerData);
    }
    
    // Process commands
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) 
    {
    	Player player = event.getPlayer();
    	PlayerData playerData = Players.get(player.getName());
    	
    	String[] split = event.getMessage().split(" ");
    	
    	// ********* BASIC COMMANDS *********
    	if( split[0].equalsIgnoreCase("/spawn") )
    	{
    		if( split.length == 1 )
    			Commands.spawn(player, playerData.name);
    		else if( split.length == 2 )
    			Commands.spawn(player, split[1]);
    		else
    			Util.message(player, "Usage: /spawn [player_name]");
    		event.setCancelled(true);
    	}
    	else if( split[0].equalsIgnoreCase("/home") )
    	{
    		if( split.length == 1 )
    			Commands.home(player, playerData.name);
    		else if( split.length == 2 )
    			Commands.home(player, split[1]);
    		else
    			Util.message(player, "Usage: /home [player_name]");
    		event.setCancelled(true);
    	}
    	else if(split[0].equalsIgnoreCase("/zone"))
    	{
    		Commands.zone(player);
    		event.setCancelled(true);
    	}
    	
    	// ********* COMPASS COMMANDS *********
    	else if( split[0].equalsIgnoreCase("/compass") )
    	{
    		if( split.length < 2 || split.length > 3 )
    		{
    			Util.message(player, "Compass commands: <required> [optional]");
    			if( true )
    				Util.message(player, "   /compass lot <lot_code>   (points compass to specified lot)");
    			if( true )
    				Util.message(player, "   /compass spawn   (points compass to world spawn)");
    			if( playerData.townName.equals("") )
    				Util.message(player, "   /compass town <town_name>   (points compass to specified town)");
    			else
    				Util.message(player, "   /compass town [town_name]   (points compass to town)");
    		}
    		else if( split[1].equalsIgnoreCase("lot") )
    		{
    			if( split.length == 3 )
    				CompassCommands.lot(player, split[2]); 
    			else
        			Util.message(player, "Usage: /compass lot <lot_code>"); 
    		}
    		else if( split[1].equalsIgnoreCase("spawn") )
    		{
    			CompassCommands.spawn(player);
    		}
    		else if( split[1].equalsIgnoreCase("town") )
    		{
    			if( split.length == 2 && !playerData.townName.equals("") )
    				CompassCommands.town(player, playerData.townName);
    			else if( split.length == 3 )
    				CompassCommands.town(player, split[2]);
        		else
        			Util.message(player, "Usage: /town info <town_name>");
    		}
    		else
    			Util.message(player, "Type /compass to see a list of available commands.");
    		event.setCancelled(true);
    	}
    	
    	// ********* LOT COMMANDS *********
    	else if( split[0].equalsIgnoreCase("/lot") )
    	{
    		if( split.length < 2 || split.length > 4 )
    		{
    			Util.message(player, "Lot commands: <required> [optional]");
    			if( playerData.lots.size() > 0 )
    				Util.message(player, "   /lot allow <player_name> (allow player to build in your lots)");
    			if( RageMod.permissionHandler.has(player, "ragemod.lot.assign") )
    				Util.message(player, "   /lot assign <lot_code> <player_name>  (gives lot to player)");
    			if( true )
    				Util.message(player, "   /lot check   (returns info on the current lot)");
    			if( true )
    				Util.message(player, "   /lot claim [lot_code]   (claims the specified or current lot)");
    			if( playerData.lots.size() > 0 )
    				Util.message(player, "   /lot disallow <player_name/all> (removes permissions)");
    			if( RageMod.permissionHandler.has(player, "ragemod.lot.evict") )
    				Util.message(player, "   /lot evict <lot_code>   (sets specified lot to 'unclaimed')");
    			if( playerData.lots.size() > 0 )
    				Util.message(player, "   /lot list   (lists all lots you own)");
    			if( playerData.lots.size() > 0 )
    				Util.message(player, "   /lot unclaim [lot_code]  (unclaims the specified lot)");
    		}
    		else if( split[1].equalsIgnoreCase("allow") )
    		{
    			if( split.length == 3 )
    				LotCommands.allow(player, split[2]); 
    			else
        			Util.message(player, "Usage: /lot allow <player_name>"); 
    		}
    		else if( split[1].equalsIgnoreCase("assign") )
    		{
    			if( split.length == 4 )
    				LotCommands.assign(player, split[2], split[3]); 
    			else
        			Util.message(player, "Usage: /lot assign <lot_code> <player_name>"); 
    		}
    		else if( split[1].equalsIgnoreCase("check") )
    		{
    			LotCommands.check(player);
    		}
    		else if( split[1].equalsIgnoreCase("claim") )
    		{
    			if( split.length == 2 )
    				LotCommands.claim(player, "");
    			else if( split.length == 3 )
    				LotCommands.claim(player, split[2]); 
    			else
        			Util.message(player, "Usage: /lot claim [lot_code]"); 
    		}
    		else if( split[1].equalsIgnoreCase("disallow") )
    		{
    			if( split.length == 3 )
    				LotCommands.disallow(player, split[2]); 
    			else
        			Util.message(player, "Usage: /lot disallow <player_name/all>"); 
    		}
    		else if( split[1].equalsIgnoreCase("evict") )
    		{
    			if( split.length == 3 )
    				LotCommands.evict(player, split[2]); 
    			else
        			Util.message(player, "Usage: /lot evict <lot_code>"); 
    		}
    		else if( split[1].equalsIgnoreCase("list") )
    		{
    			LotCommands.list(player);
    		}
    		else if( split[1].equalsIgnoreCase("unclaim") )
    		{
    			if( split.length == 2 )
    				LotCommands.unclaim(player, "");
    			else if( split.length == 3 )
    				LotCommands.unclaim(player, split[2]); 
    			else
        			Util.message(player, "Usage: /lot unclaim [lot_code]"); 
    		}
    		else
    			Util.message(player, "Type /lot to see a list of available commands.");
    		event.setCancelled(true);
    	}
    	
    	// ********* TOWN COMMANDS *********
    	else if( split[0].equalsIgnoreCase("/town") )
    	{
    		if( split.length < 2 || split.length > 3 )
    		{
    			Util.message(player, "Town commands: <required> [optional]");
    			if( playerData.isMayor )
    				Util.message(player, "   /town add <player_name>   (adds a new resident)");
    			if( playerData.townName.equals("") )
    				Util.message(player, "   /town create [town_name]   (creates a new town)");
    			if( !playerData.townName.equals("") )
    				Util.message(player, "   /town deposit <amount>   (deposits into town treasury)");
    			if( playerData.isMayor )
    				Util.message(player, "   /town evict <player_name>   (removes a resident)");
    			if( playerData.townName.equals("") )
    				Util.message(player, "   /town info <town_name>   (gives info on selected town)");
    			else
    				Util.message(player, "   /town info [town_name]   (gives info on selected town)");
    			if( !playerData.isMayor && !playerData.townName.equals("") )
    				Util.message(player, "   /town leave   (leaves your current town)");
    			if( true )
    				Util.message(player, "   /town list [faction]   (lists all towns in the world)");
    			if( playerData.isMayor )
    				Util.message(player, "   /town minimum <amount>   (sets the min. treasury balance)");
    			if( playerData.townName.equals("") )
    				Util.message(player, "   /town residents <town_name>   (lists all residents of town)");
    			else
    				Util.message(player, "   /town residents [town_name]   (lists all residents of town)");
    			if( playerData.isMayor )
    				Util.message(player, "   /town upgrade [confirm]   (upgrades your town)");
    			if( !playerData.townName.equals("") )
    				Util.message(player, "   /town withdrawl <amount>   (withdrawls from town treasury)");
    		}
    		else if( split[1].equalsIgnoreCase("add") )
    		{
    			if( split.length == 3 )
        			TownCommands.add(player, split[2]); 
        		else
        			Util.message(player, "Usage: /town add <player_name>");
    		}
    		else if( split[1].equalsIgnoreCase("create") )
    		{
    	
    			// TODO: Support 2+ word town names
    			
    			if( split.length == 2 )
    				TownCommands.create(player, "");
    			else if( split.length == 3 )
    				TownCommands.create(player, split[2]); 
    			else
        			Util.message(player, "Usage: /town create [town_name] (use 'quotes' for multiple-word town names)"); 
    		}
    		else if( split[1].equalsIgnoreCase("deposit") )
    		{
    			if( split.length == 3 )
    				TownCommands.deposit(player, split[2]); 
        		else
        			Util.message(player, "Usage: /town deposit <amount>");
    		}
    		else if( split[1].equalsIgnoreCase("evict") )
    		{
    			if( split.length == 3 )
    				TownCommands.evict(player, split[2]); 
        		else
        			Util.message(player, "Usage: /town evict <player_name>");
    		}
    		else if( split[1].equalsIgnoreCase("info") )
    		{
    			if( split.length == 2 && !playerData.townName.equals("") )
    				TownCommands.info(player, playerData.townName);
    			else if( split.length == 3 )
    				TownCommands.info(player, split[2]);
        		else
        			Util.message(player, "Usage: /town info <town_name>");
    		}
    		else if( split[1].equalsIgnoreCase("leave") )
    		{
    			TownCommands.leave(player); 	 
    		}
    		else if( split[1].equalsIgnoreCase("list") )
    		{
    			if( split.length == 2 )
    				TownCommands.list(player, "");
    			else if( split.length == 3 )
    				TownCommands.list(player, split[2]);
        		else
        			Util.message(player, "Usage: /town list [faction]");
    		}
    		else if( split[1].equalsIgnoreCase("minimum") )
    		{
    			if( split.length == 3 )
    				TownCommands.minimum(player, split[2]); 
        		else
        			Util.message(player, "Usage: /town minimum <amount>");
    		}
    		else if( split[1].equalsIgnoreCase("residents") )
    		{
    			if( split.length == 2 && !playerData.townName.equals("") )
    				TownCommands.residents(player, playerData.townName);
    			else if( split.length == 3 )
    				TownCommands.residents(player, split[2]);
        		else
        			Util.message(player, "Usage: /town residents <town_name>");
    		}
    		else if( split[1].equalsIgnoreCase("upgrade") )
    		{
    			if( split.length == 2 )
        			TownCommands.upgrade(player, false);
        		else if( split.length == 3 && split[2].equalsIgnoreCase("confirm"))
        			TownCommands.upgrade(player, true);
        		else
        			Util.message(player, "Usage: /town upgrade [confirm]");
    		}
    		else if( split[1].equalsIgnoreCase("withdrawl") )
    		{
    			if( split.length == 3 )
    				TownCommands.withdrawl(player, split[2]); 
        		else
        			Util.message(player, "Usage: /town withdrawl <amount>");
    		}
    		else
    			Util.message(player, "Type /town to see a list of available commands.");
    		event.setCancelled(true);
    	}
    	
    	// ********* FACTION COMMANDS **********
    	else if(split[0].equalsIgnoreCase("/faction") )
    	{
    		if( split.length < 2 || split.length > 3 )
    		{
    			Util.message(player, "Faction commands: <required> [optional]");
    			if( playerData.id_Faction == 0 )
    				Util.message(player, "   /faction join     (used to join a faction)");
    			if( playerData.id_Faction != 0 )
    				Util.message(player, "   /faction leave    (leaves your faction)");
    			if( true )
    				Util.message(player, "   /faction stats    (displays stats on each faction)");
    		}
    		else if( split[1].equalsIgnoreCase("join") )
    		{
    			if( split.length == 2 )
    				FactionCommands.join(player, "");
    			else if( split.length == 3 )
    				FactionCommands.join(player, split[2]); 
    			else
        			Util.message(player, "Usage: /faction join [faction_name]"); 
    		}
    		else if( split[1].equalsIgnoreCase("leave") )
    		{
    			if( split.length == 2 )
    				FactionCommands.leave(player, false);
        		else if( split.length == 3 && split[2].equalsIgnoreCase("confirm"))
        			FactionCommands.leave(player, true);
        		else
        			Util.message(player, "Usage: /faction leave [confirm]");
    		}
    		else if( split[1].equalsIgnoreCase("stats") )
    		{
    			FactionCommands.stats(player);
    		}
    		else
    			Util.message(player, "Type /faction to see a list of available commands.");
    		event.setCancelled(true);
    	}
    	
    	// ********* DEBUG COMMANDS **********
    	else if(split[0].equalsIgnoreCase("/debug") && RageMod.permissionHandler.has(player, "ragemod.debug") )
    	{
    		if( split.length < 2 || split.length > 3 )
    		{
    			Util.message(player, "Debug commands: <required> [optional]");
    			if( true )
    				Util.message(player, "   /debug donation        (displays amount of donations in last month)");
    			if( true )
    				Util.message(player, "   /debug sanctum <level> (attempts to build sanctum floor in your location)");
    		}
    		else if( split[1].equalsIgnoreCase("donation") )
    		{
    			DebugCommands.donation(player);
    		}
    		else if( split[1].equalsIgnoreCase("sanctum") )
    		{
    			if( split.length == 3 )
    				DebugCommands.sanctum(player, split[2]); 
    			else
        			Util.message(player, "Usage: /debug sanctum <level>"); 
    		}
    		else
    			Util.message(player, "Type /debug to see a list of available commands.");
    		event.setCancelled(true);
    	}
    }
    
    // Player movement
    public void onPlayerMove(PlayerMoveEvent event) 
    {
        Player player = event.getPlayer();
        PlayerData playerData = Players.get(player.getName());
        World world = player.getWorld();

        if( event.getFrom().getBlockX() != event.getTo().getBlockX() ||
        	event.getFrom().getBlockZ() != event.getTo().getBlockZ() )
        {
        	// Check to see if the player has changed zones
        	if( playerData.currentZone != RageZones.getCurrentZone(player.getLocation()))
        	{
        		playerData.currentZone = RageZones.getCurrentZone(player.getLocation());
        		Util.message(player, "Your current zone is now " + RageZones.getName(playerData.currentZone));
        		Players.update(playerData);
        	}
        	
        	// *** ZONE A (Neutral Zone) ***
        	if( playerData.currentZone == Zone.A )
        	{
        		// Check to see if the player has entered or left the capitol
        		if( playerData.isInCapitol )
        		{
        			if( !RageZones.isInCapitol(player.getLocation()) )
        			{
        				Util.message(player, "Now leaving the capitol of " + RageConfig.Capitol_Name);
        				playerData.isInCapitol = false;
        				Players.update(playerData);
        			}
        		}
        		else
        		{
        			if( RageZones.isInCapitol(player.getLocation()) )
        			{
        				Util.message(player, "Now entering the capitol of " + RageConfig.Capitol_Name);
        				playerData.isInCapitol = true;
        				Players.update(playerData);
        			}
        		}
        	}
        	// *** ZONE B (War Zone) ***
        	else if( playerData.currentZone == Zone.B )
        	{
	        	// See if the player has entered or left a PlayerTown
	        	if( playerData.currentTown == null )
	        	{
	        		PlayerTown currentTown = PlayerTowns.getCurrentTown(player.getLocation());
	        		if( currentTown != null )
	        		{
	        			Util.message(player, "Now entering the " + currentTown.townLevel.name.toLowerCase() + " of " + currentTown.townName);
	        			playerData.currentTown = currentTown;
	        			Players.update(playerData);
	        		}
	        	}
	        	else
	        	{
	        		PlayerTown currentTown = PlayerTowns.getCurrentTown(player.getLocation());
	        		if( currentTown == null )
	        		{
	        			Util.message(player, "Now leaving the " + playerData.currentTown.townLevel.name.toLowerCase() + " of " + playerData.currentTown.townName);
	        			playerData.currentTown = null;
	        			Players.update(playerData);
	        		}
	        	}
        	}
        }
    }
    
    // Player interacts with objects (right-clicking, etc.)
    public void onPlayerInteract(PlayerInteractEvent event) 
    {
    	Player player = event.getPlayer();
    	PlayerData playerData = Players.get(player.getName());
    	Action action = event.getAction();
    	Block block = event.getClickedBlock();

    	if(action == Action.RIGHT_CLICK_BLOCK)
    	{
    		
    	}	
    }
    
    // Player respawn
    public void onPlayerRespawn(PlayerRespawnEvent event) 
    {
    	Player player = event.getPlayer();
    	PlayerData playerData = Players.get(player.getName());
    	
    	if( playerData.spawn_IsSet )
    	{
    		event.setRespawnLocation(playerData.getSpawnLocation());
    	}
    	
    	
    }
    
    
    
}

