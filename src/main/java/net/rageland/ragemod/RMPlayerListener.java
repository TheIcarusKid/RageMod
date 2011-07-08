package net.rageland.ragemod;

import net.rageland.ragemod.commands.LotCommands;
import net.rageland.ragemod.commands.OtherCommands;
import net.rageland.ragemod.commands.TownCommands;
import net.rageland.ragemod.data.PlayerData;
import net.rageland.ragemod.data.Players;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.World;


/**
 * Handle events for all Player related events
 * @author TheIcarusKid
 */

// TODO: Make a Util class that parses player.sendMessage and highlights /commands, <required>, [optional], and (info)
//		 Also create a default color

public class RMPlayerListener extends PlayerListener {
    private final RageMod plugin;

    public RMPlayerListener(RageMod instance) {
        plugin = instance;
    }

    // Pull the player data from the DB and register in memory
    public void onPlayerJoin(PlayerJoinEvent event)
    {
    	Player player = event.getPlayer();    	
    	PlayerData playerData = Players.PlayerLogin(player.getName());    	  
    	
    	// Set the state info
    	playerData.CurrentZone = RageZones.GetCurrentZone(player.getLocation());
    	Players.Update(playerData);
    }
    
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) 
    {
    	Player player = event.getPlayer();
    	PlayerData playerData = Players.Get(player.getName());
    	
    	String[] split = event.getMessage().split(" ");
    	
    	if( split[0].equalsIgnoreCase("/faction") )
    	{
    		player.sendMessage("Your current faction is " + playerData.Faction);
    	}
    	
    	// ********* LOT COMMANDS *********
    	
    	// TODO: Figure out how to work the Donations table into this whole shebang
    	
    	else if( split[0].equalsIgnoreCase("/lot") )
    	{
    		if( split.length < 2 || split.length > 3 )
    		{
    			player.sendMessage("Lot commands: <required> [optional]");
    			if( true )
    				player.sendMessage("   /lot check              (returns info on the lot you are standing on)");
    			if( true )
    				player.sendMessage("   /lot claim [lot_code]   (claims the specified lot, or the one you are inside if blank)");
    			if( playerData.Lots.size() > 0 )
    				player.sendMessage("   /lot unclaim [lot_code] (claims the specified lot, or the one you are inside if blank)");
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
        			player.sendMessage("Usage: /lot claim [lot_code]"); 
    		}
    		else if( split[1].equalsIgnoreCase("unclaim") )
    		{
    			if( split.length == 2 )
    				LotCommands.unclaim(player, "");
    			else if( split.length == 3 )
    				LotCommands.unclaim(player, split[2]); 
    			else
        			player.sendMessage("Usage: /lot unclaim [lot_code]"); 
    		}
    		else
    			player.sendMessage("Type /lot to see a list of availalbe commands.");
    		
    	}
    			
    	
    	// ********* TOWN COMMANDS *********
    	else if( split[0].equalsIgnoreCase("/town") )
    	{
    		if( split.length < 2 || split.length > 3 )
    		{
    			player.sendMessage("Town commands: <required> [optional]");
    			if( playerData.IsMayor )
    				player.sendMessage("   /town add <player_name>     (adds a new resident to your town)");
    			if( playerData.TownName.equals("") )
    				player.sendMessage("   /town create [town_name]    (creates a new town - omit name to see if current location is valid)");
    			if( playerData.IsMayor )
    				player.sendMessage("   /town evict <player_name>   (removes a resident from your town)");
    			if( playerData.TownName.equals("") )
    				player.sendMessage("   /town info <town_name>      (gives info on selected town)");
    			else
    				player.sendMessage("   /town info [town_name]      (gives info on selected town; leave blank for your own town)");
    			if( !playerData.IsMayor && !playerData.TownName.equals("") )
    				player.sendMessage("   /town leave                 (leaves your current town)");
    			if( true )
    				player.sendMessage("   /town list [faction]        (lists all towns in the world or for a faction (including 'Neutral')");
    			if( playerData.TownName.equals("") )
    				player.sendMessage("   /town residents <town_name> (lists all residents of selected town)");
    			else
    				player.sendMessage("   /town residents [town_name] (lists all residents of selected town; leave blank for your own town)");
    			if( playerData.IsMayor )
    				player.sendMessage("   /town upgrade [confirm]     (upgrades your town - leave out confirm to see if upgrade available)");
    		}
    		else if( split[1].equalsIgnoreCase("add") )
    		{
    			if( split.length != 3 )
        			player.sendMessage("Usage: /town add <player_name>");
        		else
        			TownCommands.add(player, split[2]); 
    		}
    		else if( split[1].equalsIgnoreCase("create") )
    		{
    			if( split.length == 2 )
    				TownCommands.create(player, "");
    			else if( split.length == 3 )
    				TownCommands.create(player, split[2]); 
    			else
        			player.sendMessage("Usage: /town create [town_name]"); 
    		}
    		else if( split[1].equalsIgnoreCase("evict") )
    		{
    			if( split.length != 3 )
        			player.sendMessage("Usage: /town evict <player_name>");
        		else
        			TownCommands.evict(player, split[2]); 
    		}
    		else if( split[1].equalsIgnoreCase("info") )
    		{
    			if( split.length == 2 && !playerData.TownName.equals("") )
    				TownCommands.info(player, playerData.TownName);
    			else if( split.length == 3 )
    				TownCommands.info(player, split[2]);
        		else
        			player.sendMessage("Usage: /town info <town_name>");
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
        			player.sendMessage("Usage: /town list [faction]");
    		}
    		else if( split[1].equalsIgnoreCase("residents") )
    		{
    			if( split.length == 2 && !playerData.TownName.equals("") )
    				TownCommands.residents(player, playerData.TownName);
    			else if( split.length == 3 )
    				TownCommands.residents(player, split[2]);
        		else
        			player.sendMessage("Usage: /town residents <town_name>");
    		}
    		else if( split[1].equalsIgnoreCase("upgrade") )
    		{
    			if( split.length == 2 )
        			TownCommands.upgrade(player, false);
        		else if( split.length == 3 && split[1].equalsIgnoreCase("confirm"))
        			TownCommands.upgrade(player, true);
        		else
        			player.sendMessage("Usage: /town upgrade [confirm]");
    		}
    		else
    			player.sendMessage("Type /town to see a list of availalbe commands.");
    	}
    	
    	
    	// ********* ZONE COMMANDS **********
    	else if(split[0].equalsIgnoreCase("/zone"))
    	{
    		OtherCommands.ZoneCheck(player);
    	}
    }
    
    public void onPlayerMove(PlayerMoveEvent event) 
    {
        Player player = event.getPlayer();
        PlayerData playerData = Players.Get(player.getName());
        World world = player.getWorld();

        if( event.getFrom().getBlockX() != event.getTo().getBlockX() ||
        	event.getFrom().getBlockZ() != event.getTo().getBlockZ() )
        {
        	// Check to see if the player has changed zones
        	if( playerData.CurrentZone != RageZones.GetCurrentZone(player.getLocation()))
        	{
        		player.sendMessage("Player zone: " + playerData.CurrentZone + " GetCurrentZone(): " + RageZones.GetCurrentZone(player.getLocation()));
        		playerData.CurrentZone = RageZones.GetCurrentZone(player.getLocation());
        		player.sendMessage("Your current zone is now " + RageZones.GetName(playerData.CurrentZone));
        		Players.Update(playerData);
        	}
        }
        	
        
    }
}

