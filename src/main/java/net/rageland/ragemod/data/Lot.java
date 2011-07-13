package net.rageland.ragemod.data;

import net.rageland.ragemod.RageConfig;

import org.bukkit.Location;
import org.bukkit.World;

// May want to consider having this extend the Region2D class

// The Lot class is used to store data about player lots in the world's capitol city
public class Lot 
{
	public enum LotCategory
	{
		WARRENS,
		MARKET,
		COAL,
		IRON,
		GOLD,
		DIAMOND;
	}
	
	public int id_Lot;
	public int number;
	public LotCategory category;
	public Region2D region;
	public World world;
	public String owner;			// Only necessary when loaded into the Lots global
	
	// Blank constructor 
	public Lot()
	{
		
	}
	
	// Returns the plaintext version of the lot category
	public String getCategoryName()
	{
		if( category == LotCategory.WARRENS )
			return "Warrens";
		else if( category == LotCategory.MARKET )
			return "Market";
		else if( category == LotCategory.COAL )
			return "Coal";
		else if( category == LotCategory.IRON )
			return "Iron";
		else if( category == LotCategory.GOLD )
			return "Gold";
		else if( category == LotCategory.DIAMOND )
			return "Diamond";
		
		System.out.println("Error: Invalid lot type called in GetCategoryName()");
		return "Error: Invalid lot category";
	}
	
	// Returns the plaintext version of the lot category
	public char getCategoryPrefix()
	{
		if( category == LotCategory.WARRENS )
			return 'W';
		else if( category == LotCategory.MARKET )
			return 'M';
		else if( category == LotCategory.COAL )
			return 'A';
		else if( category == LotCategory.IRON )
			return 'B';
		else if( category == LotCategory.GOLD )
			return 'C';
		else if( category == LotCategory.DIAMOND )
			return 'D';
		
		System.out.println("Error: Invalid lot type called in GetCategoryPrefix()");
		return ' ';
	}
	
	// Gets the Prefix + Number lot code
	public String getLotCode()
	{
		return this.getCategoryPrefix() + String.valueOf(this.number);
	}
	
	// Sets the lot's category based on the category character
	public void setCategory(String prefix)
	{
		if( prefix.equals("W") )
			category = LotCategory.WARRENS;
		else if( prefix.equals("M") )
			category = LotCategory.MARKET;
		else if( prefix.equals("A") )
			category = LotCategory.COAL;
		else if( prefix.equals("B") )
			category = LotCategory.IRON;
		else if( prefix.equals("C") )
			category = LotCategory.GOLD;
		else if( prefix.equals("D") )
			category = LotCategory.DIAMOND;
		else
			System.out.println("Error: Lot.SetCategory() called on invalid string: " + prefix);
	}
	
	// Return whether or not the player can use the /home set command inside this lot
	public boolean canSetHome()
	{
		return isMemberLot();
	}
	
	// Returns whether or not the current location is inside the Lot
	public boolean isInside(Location loc)
	{
		return region.isInside(loc);
	}
	
	// Returns whether or not the lot is one of the categories of member lot
	public boolean isMemberLot()
	{
		return category == LotCategory.COAL || category == LotCategory.IRON ||
			   category == LotCategory.GOLD || category == LotCategory.DIAMOND; 
	}
	
	// Returns the price for this lot as defined in the config
	public int getPrice()
	{
		if( category == LotCategory.COAL )
			return RageConfig.Lot_PRICE_COAL;
		else if( category == LotCategory.IRON )
			return RageConfig.Lot_PRICE_IRON;
		else if( category == LotCategory.GOLD )
			return RageConfig.Lot_PRICE_GOLD;
		else if( category == LotCategory.DIAMOND )
			return RageConfig.Lot_PRICE_DIAMOND;
		else
			return 0;
	}
	
	// Returns a location in the center of the region for compass target
	public Location getCenter()
	{
		return new Location(world, region.nwCorner.getX() - ((region.nwCorner.getX() - region.seCorner.getX()) / 2), 65,
								   region.seCorner.getZ() - ((region.seCorner.getZ() - region.nwCorner.getZ()) / 2));
	}
	
	
	
	

}
