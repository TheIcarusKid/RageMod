package net.rageland.ragemod.commands;

import net.rageland.ragemod.RageConfig;
import net.rageland.ragemod.RageMod;
import net.rageland.ragemod.RageZones;
import net.rageland.ragemod.Util;
import net.rageland.ragemod.data.Lot;
import net.rageland.ragemod.data.Lot.LotCategory;
import net.rageland.ragemod.data.Lots;
import net.rageland.ragemod.data.PlayerData;
import net.rageland.ragemod.data.Players;

import org.bukkit.entity.Player;

//The Commands classes handle all the state checking for typed commands, then send them to the database if approved
public class LotCommands 
{
	// /lot allow <player_name>
	public static void allow(Player player, String targetPlayerName) 
	{
		PlayerData playerData = Players.get(player.getName());
		PlayerData targetPlayerData = Players.get(targetPlayerName);

		// Check to see if target player exists
		if( targetPlayerData == null )
		{
			Util.message(player, "Player " + targetPlayerName + " does not exist.");
			return;
		}
		// Check to see if the target player already has permission
		if( playerData.lotPermissions.contains(targetPlayerData.name) )
		{
			Util.message(player, targetPlayerData.name + " already has permission to build in your lots.");
			return;
		}
		
		// All checks have succeeded - update the DB
		RageMod.database.lotAllow(playerData.id_Player, targetPlayerData.id_Player);
		
		// Update the playerData
		playerData.lotPermissions.add(targetPlayerData.name);
		Players.update(playerData);
		
		Util.message(player, targetPlayerData.name + " is now allowed to build in your lots.");
	}
	
	
	// /lot assign <lot_code> <player_name>
	public static void assign(Player player, String lotCode, String targetPlayerName) 
	{
		PlayerData targetPlayerData = Players.get(targetPlayerName);
		Lot lot = Lots.get(lotCode);
		
		// Make sure the player has permission to perform this command
		if( !RageMod.permissionHandler.has(player, "ragemod.lot.assign") )
		{
			Util.message(player, "You do not have permission to perform that command.");
			return;
		}
		// Check to see if target player exists
		if( targetPlayerData == null )
		{
			Util.message(player, "Player " + targetPlayerName + " does not exist.");
			return;
		}
		// lot will be null if code is invalid
		if( lot == null )
		{	
			Util.message(player, lotCode + " is not a valid lot code.  (consult the online map)");
			return;
		}
		// See if the lot is already claimed
		if( !lot.owner.equals("") )
		{
			Util.message(player, "Lot " + lot.getLotCode() + " is already owned by " + lot.owner + ".");
			return;
		}
		
		// All checks have succeeded - give the lot to the player
		RageMod.database.lotClaim(targetPlayerData, lot);
		
		// Update the playerData
		targetPlayerData.lots.add(lot);
		Players.update(targetPlayerData);
		
		// Update Lots to set the owner
		lot.owner = targetPlayerData.name;
		Lots.put(lot);
		
		Util.message(player, targetPlayerData.name + " now owns lot " + lot.getLotCode() + ".");
	}
	
	// /lot check
	public static void check(Player player)
	{		
		// Make sure the player is in the capitol
		if( !RageZones.isInCapitol(player.getLocation()) )
		{
			Util.message(player, "You must be in " + RageConfig.Capitol_Name + " to use this command.");
		}
		
		Lot lot = Lots.findCurrentLot(player.getLocation());
		
		if( lot != null )
		{
			Util.message(player, "You are currently in lot " + lot.getLotCode() + " (" + lot.getCategoryName() + ").");
			if( lot.owner.equals("") )
				Util.message(player, "This lot is unowned - type /lot claim to claim it.");
			else
				Util.message(player, "This lot is owned by " + lot.owner + ".");
		}
		else
		{
			Util.message(player, "You are not standing inside of a lot.");
		}
	}

	// /lot claim [lot_code]
	public static void claim(Player player, String lotCode) 
	{
		PlayerData playerData = Players.get(player.getName());
		Lot lot;
		
		// Get the current lot, whether blank (current location) or typed
		if( lotCode.equals("") )
			lot = Lots.findCurrentLot(player.getLocation());
		else
			lot = Lots.get(lotCode);
		
		// lot will be null if either of the above methods failed
		if( lot == null )
		{
			if( lotCode.equals("") )
				Util.message(player, "You are not standing on a valid lot.  (consult the online map)");
			else
				Util.message(player, lotCode + " is not a valid lot code.  (consult the online map)");
			return;
		}
		// See if the lot is already claimed
		if( !lot.owner.equals("") )
		{
			if( lot.owner.equals(playerData.name) )
				Util.message(player, "You already own this lot!");
			else
				Util.message(player, "Lot " + lot.getLotCode() + " is already owned by " + lot.owner + ".");
			return;
		}
		// Make sure the player does not already own a lot of the current lot's category
		for( Lot ownedLot : playerData.lots )
		{
			if( ownedLot.category == lot.category && (lot.category == LotCategory.WARRENS || lot.category == LotCategory.MARKET) )
			{
				Util.message(player, "You can only own one " + lot.getCategoryName() + " lot at a time.");
				return;
			}
			else if( (lot.category == LotCategory.COAL || lot.category == LotCategory.IRON || lot.category == LotCategory.GOLD || lot.category == LotCategory.DIAMOND) &&
					 (ownedLot.category == LotCategory.COAL || ownedLot.category == LotCategory.IRON || ownedLot.category == LotCategory.GOLD || ownedLot.category == LotCategory.DIAMOND) )
			{
				Util.message(player, "You can only own one member lot at a time.");
				return;
			}
		}
		// If the player is claiming a member lot, see if they have donated the appropriate amount
		if( lot.isMemberLot() )
		{
			int donation = RageMod.database.getRecentDonations(playerData.id_Player);
			
			if( donation < lot.getPrice() )
			{
				Util.message(player, "To claim this lot you must be a " + lot.getCategoryName() + "-level " + RageConfig.ServerName + " member.");
				Util.message(player, "Visit http://www.rageland.net/donate for more details.");
				return;
			}
		}
		
		// All checks have succeeded - give the lot to the player
		RageMod.database.lotClaim(playerData, lot);
		
		// Update the playerData
		playerData.lots.add(lot);
		Players.update(playerData);
		
		// Update Lots to set the owner
		lot.owner = playerData.name;
		Lots.put(lot);
		
		Util.message(player, "You now own lot " + lot.getLotCode() + ".");
	}
	
	// /lot disallow <player_name/all>
	public static void disallow(Player player, String targetPlayerName) 
	{
		PlayerData playerData = Players.get(player.getName());

		if( targetPlayerName.equalsIgnoreCase("all") )
		{
			if( playerData.lotPermissions.size() == 0 )
			{
				Util.message(player, "You have no permissions to disallow.");
				return;
			}
			
			// All checks have succeeded - update the database
			RageMod.database.lotDisallow(playerData.id_Player, 0);
			
			// Update the playerData
			playerData.lotPermissions.clear();
			Players.update(playerData);
			
			Util.message(player, "All of your lot permissions have been cleared.");
		}
		else
		{
			PlayerData targetPlayerData = Players.get(targetPlayerName);
			
			// Check to see if target player exists
			if( targetPlayerData == null )
			{
				Util.message(player, "Player " + targetPlayerName + " does not exist.");
				return;
			}
			// Check to see if the target player already has permission
			if( !playerData.lotPermissions.contains(targetPlayerData.name) )
			{
				Util.message(player, targetPlayerData.name + " does not have permission to build in your lots.");
				return;
			}
			
			// All checks have succeeded - update the database
			RageMod.database.lotDisallow(playerData.id_Player, targetPlayerData.id_Player);
			
			// Update the playerData
			playerData.lotPermissions.remove(targetPlayerData.name);
			Players.update(playerData);
			
			Util.message(player, targetPlayerData.name + " is no longer allowed to build in your lots.");
		}
		
	}
	
	// /lot assign <lot_code> <player_name>
	public static void evict(Player player, String lotCode) 
	{
		Lot lot = Lots.get(lotCode);
		
		// Make sure the player has permission to perform this command
		if( !RageMod.permissionHandler.has(player, "ragemod.lot.evict") )
		{
			Util.message(player, "You do not have permission to perform that command.");
			return;
		}		
		// lot will be null if invalid
		if( lot == null )
		{	
			Util.message(player, lotCode + " is not a valid lot code.  (consult the online map)");
			return;
		}
		// Make sure the lot is already claimed
		if( lot.owner.equals("") )
		{
			Util.message(player, "Lot " + lot.getLotCode() + " is already unclaimed.");
			return;
		}
		
		// All checks have succeeded - remove the lot owner
		RageMod.database.lotUnclaim(lot);
		
		// Update the playerData
		PlayerData targetPlayerData = Players.get(lot.owner);
		targetPlayerData.lots.remove(lot);
		Players.update(targetPlayerData);
		
		// Update Lots to set the owner
		lot.owner = "";
		Lots.put(lot);
		
		Util.message(player, targetPlayerData.name + " has been evicted from lot " + lot.getLotCode() + ".");
	}
	
	// /lot list
	public static void list(Player player) 
	{
		PlayerData playerData = Players.get(player.getName());
		
		// Make sure the player actually owns lots
		if( playerData.lots.size() == 0 )
		{
			Util.message(player, "You do not own any lots.");
			return;
		}
		
		Util.message(player, "You currently own the following lots:");
		
		for( Lot lot : playerData.lots )
		{
			Util.message(player, "   " + lot.getLotCode() + " (" + lot.getCategoryName() + ")  " + 
							   "x: " + (int)lot.region.nwCorner.getX() + "  z: " + (int)lot.region.nwCorner.getZ());
		}
		
		// List all players allowed to build in their lots, if any
		if( playerData.lotPermissions.size() > 0 )
		{
			Util.message(player, "The following players are allowed to build in your lots:");
			String nameList = "";
			for( String name : playerData.lotPermissions )
			{
				if( nameList.equals("") )
					nameList += name;
				else
					nameList += ", " + name;
			}
			Util.message(player, "   " + nameList);
		}		
	}

	// /lot unclaim [lot_code]
	public static void unclaim(Player player, String lotCode) 
	{
		PlayerData playerData = Players.get(player.getName());
		Lot lot;
		boolean isLotOwned = false;
		
		lotCode = lotCode.toUpperCase();
		
		// Get the current lot, whether blank (current location) or typed
		if( lotCode.equals("") )
			lot = Lots.findCurrentLot(player.getLocation());
		else
			lot = Lots.get(lotCode);
		
		// lot will be null if either of the above methods failed
		if( lot == null )
		{
			if( lotCode.equals("") )
				Util.message(player, "You are not standing on a valid lot.  (use /lot check and consult the online map)");
			else
				Util.message(player, lotCode + " is not a valid lot code.  (consult the online map)");
			return;
		}
		// Make sure the player owns the specified lot
		for( Lot ownedLot : playerData.lots )
		{
			if( ownedLot.id_Lot == lot.id_Lot )
				isLotOwned = true;
		}
		if( !isLotOwned )
		{
			Util.message(player, "You do not own lot " + lot.getLotCode() + ".");
			return;
		}
		
		// All checks have succeeded - reset the lot owner
		RageMod.database.lotUnclaim(lot);
		
		// Update the playerData
		playerData.lots.remove(lot);
		Players.update(playerData);
		
		// Update Lots to remove the owner
		lot.owner = "";
		Lots.put(lot);
		
		Util.message(player, "You are no longer the owner of lot " + lot.getLotCode() + ".");
	}



	
	
}
