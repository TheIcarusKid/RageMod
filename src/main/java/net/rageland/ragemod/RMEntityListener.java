package net.rageland.ragemod;

// TODO: Bounty

// TODO: Fix onEntityDamageByEntity, I can't get it to fire on mob damage whatsoever

import net.rageland.ragemod.data.Factions;
import net.rageland.ragemod.data.PlayerData;
import net.rageland.ragemod.data.PlayerTown;
import net.rageland.ragemod.data.PlayerTowns;
import net.rageland.ragemod.data.Players;

import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityListener;

/**
 * RageMod entity listener
 * @author TheIcarusKid
 */
public class RMEntityListener extends EntityListener
{
	private final RageMod plugin;

    public RMEntityListener(final RageMod plugin) 
    {
        this.plugin = plugin;
    }
    
    // Called when an entity damages another entity
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) 
    {
    	System.out.println("DAMAGE!");
    	
    	Entity attackerEntity = event.getDamager();
        Entity defenderEntity = event.getEntity();
        
        // Handle PvP
        if( attackerEntity instanceof Player && defenderEntity instanceof Player ) 
        {
        	Player attacker = (Player)attackerEntity;
        	Player defender = (Player)defenderEntity;
        	PlayerData attackerData = Players.get(attacker.getName());
        	PlayerData defenderData = Players.get(defender.getName());
        	
        	// Always prevent allies from harming each other
			if( attackerData.id_Faction == defenderData.id_Faction )
			{
				event.setCancelled(true);
    			attacker.sendMessage(defenderData.name + " is your ally!");
    			return;
			}
        	
        	// Use the defender's position to determine behavior
        	// *** ZONE A (Neutral Zone) ***
        	if( RageZones.isInZoneA(defender.getLocation()) )
        	{
        		// No PvP in capitol
        		if( RageZones.isInCapitol(defender.getLocation()) )
        		{
        			event.setCancelled(true);
        			attacker.sendMessage("PvP is not allowed inside " + RageConfig.Capitol_Name + ".");
        			return;
        		}
        		else
        		{
        			// Only faction-faction PvP is allowed in neutral zone
        			if( defenderData.id_Faction == 0 )
        			{
        				event.setCancelled(true);
            			attacker.sendMessage("You cannot attack neutral players in " + RageConfig.Zone_NAME_A + ".");
            			return;
        			}
        			else if( attackerData.id_Faction == 0 )
        			{
        				event.setCancelled(true);
            			attacker.sendMessage("Neutral players cannot attack in " + RageConfig.Zone_NAME_A + ".");
            			return;
        			}
        		}
        	}
        	// *** ZONE B (War Zone) ***
        	else if( RageZones.isInZoneB(defender.getLocation()) )
        	{
        		PlayerTown playerTown = PlayerTowns.getCurrentTown(defender.getLocation());
        		
        		// Keep referees from participating in combat
        		if( RageMod.permissionHandler.has(attacker, "ragemod.referee.blockpvp") )
        		{
        			event.setCancelled(true);
        			attacker.sendMessage("Referees may not participate in combat.");
        			return;
        		}
        		// Protect referees 
        		else if( RageMod.permissionHandler.has(defender, "ragemod.referee.blockpvp") )
        		{
        			event.setCancelled(true);
        			attacker.sendMessage("You cannot harm a referee.");
        			return;
        		}
        		// Handle combat inside of towns
        		else if( playerTown != null )
        		{
	        		// Protect neutral players inside all towns
	        		if( defenderData.id_Faction == 0 )
	        		{
	        			event.setCancelled(true);
	        			attacker.sendMessage("You cannot harm neutral players inside of towns.");
	        			return;
	        		}
	        		// Keep neutral players from harming any players
	        		if( attackerData.id_Faction == 0 )
	        		{
	        			event.setCancelled(true);
	        			attacker.sendMessage("Neutral players cannot attack inside of towns.");
	        			return;
	        		}
	        		// Protect faction players inside of their own and allied towns
	        		if( defenderData.id_Faction == playerTown.id_Faction )
	        		{
	        			event.setCancelled(true);
	        			attacker.sendMessage("You cannot harm " + Factions.getName(defenderData.id_Faction) + " inside of their own towns.");
	        			return;
	        		}
        		}
        	}
        }
        else if( defenderEntity instanceof Player && attackerEntity instanceof Creature)
        {
        	// Automatically kill monsters who attack admins
        	Creature creature = (Creature)attackerEntity;
        	creature.damage(100);
        	System.out.println("Attempted to kill attacking creature");
        }
        else
        {
        	System.out.println(attackerEntity.getEntityId() + " entity caused damage.");
        }
    }
    
    // Called when creatures spawn
    public void onCreatureSpawn(CreatureSpawnEvent event) 
    {
        // Don't process code if event was cancelled by another plugin
    	if (event.isCancelled()) 
        {
            return;
        }
    	
    	// Don't let monsters spawn inside player towns or the capitol
    	if( (event.getCreatureType() == CreatureType.CREEPER || event.getCreatureType() == CreatureType.SKELETON ||
    			event.getCreatureType() == CreatureType.ZOMBIE || event.getCreatureType() == CreatureType.SPIDER) && 
    			(RageZones.isInCapitol(event.getLocation()) || PlayerTowns.getCurrentTown(event.getLocation()) != null) )
    	{
    		event.setCancelled(true);
    	}
    	
    	
    	
    	
    }
}

