package net.rageland.ragemod;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handle events for all Player related events
 * @author BrokenTomato, TheIcarusKid
 */
public class RMPlayerListener extends PlayerListener {
    private final RageMod plugin;

    public RMPlayerListener(RageMod instance) {
        plugin = instance;
    }

    // Pull the player data from the DB and register in memory
    public void onPlayerJoin(PlayerJoinEvent event)
    {
    	Player p = event.getPlayer();    	
    	Players.PlayerLogin(p.getName());
    }
    
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) 
    {
    	Player player = event.getPlayer();
    	String playerName = player.getName();
    	PlayerData playerData = Players.Get(player.getName());
    	
    	String[] split = event.getMessage().split(" ");
    	
    	if(split[0].equalsIgnoreCase("/faction"))
    	{
    		player.sendMessage("Your current faction is " + playerData.Faction);
    	}
    	
    	// ********* TOWN COMMANDS *********
    	else if(split[0].equalsIgnoreCase("/townadd"))
    	{
    		if( split.length != 2)
    			player.sendMessage("Usage: /townadd <player_name>");
    		else
    			Commands.TownAdd(player, split[1]); 			
    	}
    	else if(split[0].equalsIgnoreCase("/towncreate"))
    	{
    		if( split.length != 2)
    			player.sendMessage("Usage: /towncreate <town_name>");
    		else
    			Commands.TownCreate(player, split[1]); 			
    	}
    	else if(split[0].equalsIgnoreCase("/townleave"))
    	{
    		Commands.TownLeave(player); 			
    	}

    }
}

