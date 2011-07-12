package net.rageland.ragemod;

import java.util.HashMap;

import net.rageland.ragemod.data.Lot;
import net.rageland.ragemod.data.Lots;
import net.rageland.ragemod.data.PlayerData;
import net.rageland.ragemod.data.PlayerTown;
import net.rageland.ragemod.data.PlayerTowns;
import net.rageland.ragemod.data.Players;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * RageMod block listener
 * @author TheIcarusKid
 */
public class RMBlockListener extends BlockListener 
{
    private final RageMod plugin;

    public RMBlockListener(final RageMod plugin) 
    {
        this.plugin = plugin;
    }

    // Prevent block breaking without permission
    public void onBlockBreak(BlockBreakEvent event) 
    {
    	Player player = event.getPlayer();
    	PlayerData playerData = Players.Get(player.getName());
    	Block block = event.getBlock();
    	
    	if (event.isCancelled()) 
        {
            return;
        }

    	// Perform generic edit permission handling
    	if( !canEditBlock(event, player) )
    	{
    		event.setCancelled(true);
    		return;
    	}
    	
    	// Bed breaking - clear spawn and home
    	if( block.getType() == Material.BED_BLOCK )
    	{
    		// /home: bed inside capitol lot
			if( RageZones.IsInZoneA(block.getLocation()) )
			{
				for( Lot lot : playerData.lots )
				{
					if( lot.canSetHome() && lot.isInside(block.getLocation()) )
					{
						playerData.clearHome();
		    			player.sendMessage("You no longer have a home point.");
		    			// Update both memory and database
		    			Players.Update(playerData);
		    			RageMod.Database.updatePlayer(playerData);
					}
				}
			}
			// /spawn: for beds in player towns
			else if( RageZones.IsInZoneB(block.getLocation()) )
			{
				PlayerTown playerTown = PlayerTowns.getCurrentTown(block.getLocation());

	    		if( playerTown != null && playerTown.townName.equals(playerData.townName) )
	    		{
	    			playerData.clearSpawn();
	    			player.sendMessage("You no longer have a spawn point.");
	    			// Update both memory and database
	    			Players.Update(playerData);
	    			RageMod.Database.updatePlayer(playerData);
	    		}
			}
    	}
    }
    
    // Prevent block placing without permission
    public void onBlockPlace(BlockPlaceEvent event) 
    {
    	Player player = event.getPlayer();
    	PlayerData playerData = Players.Get(player.getName());
    	Block block = event.getBlock();
    	
    	if (event.isCancelled()) 
        {
            return;
        }
    	
    	// Perform generic edit permission handling
    	if( !canEditBlock(event, player) )
    	{
    		event.setCancelled(true);
    		return;
    	}
    	
    	// Bed placement - set spawn and home
    	if( block.getType() == Material.BED_BLOCK )
    	{
    		// /home: bed inside capitol lot
			if( RageZones.IsInZoneA(block.getLocation()) && playerData.isMember )
			{
				for( Lot lot : playerData.lots )
				{
					if( lot.canSetHome() && lot.isInside(block.getLocation()) )
					{
						if( playerData.home_IsSet )
						{
							player.sendMessage("You already have a bed inside your lot.");
							event.setCancelled(true);
							return;
						}
						playerData.setHome(block.getLocation());
		    			player.sendMessage("Your home location has now been set.");
		    			// Update both memory and database
		    			Players.Update(playerData);
		    			RageMod.Database.updatePlayer(playerData);
					}
				}
			}
			// /spawn: for beds in player towns
			else if( RageZones.IsInZoneB(block.getLocation()) )
			{
				PlayerTown playerTown = PlayerTowns.getCurrentTown(block.getLocation());

	    		if( playerTown != null && playerTown.townName.equals(playerData.townName) )
	    		{
	    			if( playerData.spawn_IsSet )
					{
						player.sendMessage("You already have a bed inside your town.");
						event.setCancelled(true);
						return;
					}
	    			
	    			// Make sure the location is not too close to another player's spawn
	    			HashMap<String, Location> spawns = RageMod.Database.getSpawnLocations(playerTown.id_PlayerTown);
	    			for( String resident : spawns.keySet() )
	    			{
	    				if( block.getLocation().distance(spawns.get(resident)) < RageConfig.Town_DistanceBetweenBeds && !resident.equals(playerData.name) )
	    				{
	    					player.sendMessage("This bed is too close to " + resident + "'s bed - spawn not set.");
	    					event.setCancelled(true);
	    					return;
	    				}
	    			}
	    			
	    			playerData.setSpawn(block.getLocation());
	    			player.sendMessage("Your spawn location has now been set.");
	    			// Update both memory and database
	    			Players.Update(playerData);
	    			RageMod.Database.updatePlayer(playerData);
	    		}
			}
    	}
    }
    
    // Generic permission edit handler that handles multiple types of block editing
    private boolean canEditBlock(BlockEvent event, Player player)
    {
    	PlayerData playerData = Players.Get(player.getName());
    	Block block = event.getBlock();
    	Location location = block.getLocation();
    	
    	// *** ZONE A (Neutral Zone) ***
    	// See if player is in capitol
    	if( RageZones.IsInZoneA(location) && RageZones.IsInCapitol(location) )
    	{
    		// See if the player is inside a lot, and if they own it
    		if( !playerData.isInsideLot(location) && !RageMod.permissionHandler.has(player, "ragemod.build.capitol") )
    		{
    			Lot lot = Lots.findCurrentLot(location);
    			
    			if( lot == null )
    				player.sendMessage("You don't have permission to edit city infrastructure.");
    			else
    			{
    				if( lot.owner.equals("") )
    					player.sendMessage("You cannot edit unclaimed lots.");
    				else
    					player.sendMessage("This lot is owned by " + lot.owner + ".");
    			}
    			
    			return false;
    		}
    	}
    	// *** ZONE B (War Zone) ***
    	else if( RageZones.IsInZoneB(location) )
    	{
    		PlayerTown playerTown = PlayerTowns.getCurrentTown(location);
    		
    		// Players can only build inside their own towns
    		if( playerTown != null && !playerTown.townName.equals(playerData.townName) && !RageMod.permissionHandler.has(player, "ragemod.build.anytown") )
    		{
    			player.sendMessage("You can only build inside of your own town.");
    			return false;
    		}
    	}
    	
    	return true;
    }
    
    

}
