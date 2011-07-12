package net.rageland.ragemod.commands;

import net.rageland.ragemod.RageConfig;
import net.rageland.ragemod.RageMod;
import net.rageland.ragemod.data.Lot;
import net.rageland.ragemod.data.Lot.LotCategory;
import net.rageland.ragemod.data.Lots;
import net.rageland.ragemod.data.PlayerData;
import net.rageland.ragemod.data.Players;

import org.bukkit.entity.Player;

//The Commands classes handle all the state checking for typed commands, then send them to the database if approved
public class LotCommands 
{
	// /lot check
	public static void check(Player player)
	{		
		// TODO: Make sure the player is in the capitol
		
		Lot lot = Lots.findCurrentLot(player.getLocation());
		
		if( lot != null )
		{
			player.sendMessage("You are currently in lot " + lot.getLotCode() + " (" + lot.getCategoryName() + ").");
			if( lot.owner.equals("") )
				player.sendMessage("This lot is unowned - type /lot claim to claim it.");
			else
				player.sendMessage("This lot is owned by " + lot.owner + ".");
		}
		else
		{
			player.sendMessage("You are not standing inside of a lot.");
		}
	}

	// /lot claim [lot_code]
	public static void claim(Player player, String lotCode) 
	{
		PlayerData playerData = Players.Get(player.getName());
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
				player.sendMessage("You are not standing on a valid lot.  (consult the online map)");
			else
				player.sendMessage(lotCode + " is not a valid lot code.  (consult the online map)");
			return;
		}
		// See if the lot is already claimed
		if( !lot.owner.equals("") )
		{
			if( lot.owner.equals(playerData.name) )
				player.sendMessage("You already own this lot!");
			else
				player.sendMessage("Lot " + lot.getLotCode() + " is already owned by " + lot.owner + ".");
			return;
		}
		// Make sure the player does not already own a lot of the current lot's category
		for( Lot ownedLot : playerData.lots )
		{
			if( ownedLot.category == lot.category && (lot.category == LotCategory.WARRENS || lot.category == LotCategory.MARKET) )
			{
				player.sendMessage("You can only own one " + lot.getCategoryName() + " lot at a time.");
				return;
			}
			else if( (lot.category == LotCategory.COAL || lot.category == LotCategory.IRON || lot.category == LotCategory.GOLD || lot.category == LotCategory.DIAMOND) &&
					 (ownedLot.category == LotCategory.COAL || ownedLot.category == LotCategory.IRON || ownedLot.category == LotCategory.GOLD || ownedLot.category == LotCategory.DIAMOND) )
			{
				player.sendMessage("You can only own one member lot at a time.");
				return;
			}
		}
		// If the player is claiming a member lot, see if they have donated the appropriate amount
		if( lot.isMemberLot() )
		{
			int donation = RageMod.Database.getRecentDonations(playerData.id_Player);
			
			if( donation < lot.getPrice() )
			{
				player.sendMessage("To claim this lot you must be a " + lot.getCategoryName() + "-level " + RageConfig.ServerName + " member.");
				player.sendMessage("Visit http://www.rageland.net/donate for more details.");
				return;
			}
		}
		
		// TODO: Check donation status (implement a 2nd currency for buying lots?)
		
		// All checks have succeeded - give the lot to the player
		RageMod.Database.lotClaim(playerData, lot);
		
		// Update the playerData
		playerData.lots.add(lot);
		Players.Update(playerData);
		
		// Update Lots to set the owner
		lot.owner = playerData.name;
		Lots.put(lot);
		
		player.sendMessage("You now own lot " + lot.getLotCode() + ".");
	}

	// /lot unclaim [lot_code]
	public static void unclaim(Player player, String lotCode) 
	{
		PlayerData playerData = Players.Get(player.getName());
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
				player.sendMessage("You are not standing on a valid lot.  (use /lot check and consult the online map)");
			else
				player.sendMessage(lotCode + " is not a valid lot code.  (consult the online map)");
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
			player.sendMessage("You do not own lot " + lot.getLotCode() + ".");
			return;
		}
		
		// All checks have succeeded - reset the lot owner
		RageMod.Database.lotUnclaim(lot);
		
		// Update the playerData
		playerData.lots.remove(lot);
		Players.Update(playerData);
		
		// Update Lots to remove the owner
		lot.owner = "";
		Lots.put(lot);
		
		player.sendMessage("You are no longer the owner of lot " + lot.getLotCode() + ".");
	}

	public static void list(Player player) 
	{
		PlayerData playerData = Players.Get(player.getName());
		
		// Make sure the player actually owns lots
		if( playerData.lots.size() == 0 )
		{
			player.sendMessage("You do not own any lots.");
			return;
		}
		
		player.sendMessage("You currently own the following lots:");
		
		for( Lot lot : playerData.lots )
		{
			player.sendMessage("   " + lot.getLotCode() + " (" + lot.getCategoryName() + ")  " + 
							   "x: " + (int)lot.region.nwCorner.getX() + "  z: " + (int)lot.region.nwCorner.getZ());
		}
				
	}
}
