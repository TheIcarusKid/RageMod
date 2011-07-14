package net.rageland.ragemod.data;

import java.sql.Timestamp;
import java.util.Date;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import net.rageland.ragemod.RageConfig;
import net.rageland.ragemod.RageMod;

// TODO: Make this inherit from Region2D, along with NPCTown (make another class in between)

// TODO: Figure out how to make the constructor more graceful and safer - the current buildRegion() setup is asking for null pointer errors

public class PlayerTown implements Comparable<PlayerTown> {
	
	// PlayerTowns table
	public int id_PlayerTown;
	public String townName;
	public Location2D centerPoint;
	public int id_Faction;
	public double treasuryBalance;
	public double minimumBalance;
	public Timestamp bankruptDate;
	public String mayor;					// Name of mayor
		
	public TownLevel townLevel;				// Corresponds to the HashMap TownLevels in Config
	
	public Region2D region;
	public World world;
	
	
	// Constructor: All data
//	public PlayerTown (int _id_PlayerTown, String _townName, int _xCoord, int _zCoord, String _faction, 
//			float _treasuryBalance, Date _bankruptDate, String _townLevel, float _upkeepCost, int _size, int _maxNPCs)
//	{
//		 ID_PlayerTown = _id_PlayerTown;
//		 TownName = _townName;
//		 XCoord = _xCoord;
//		 ZCoord = _zCoord;
//		 Faction = _faction;
//		 TreasuryBalance = _treasuryBalance;
//		 BankruptDate = _bankruptDate;
//			
//		 // TownLevels table
//		 TownLevel = _townLevel;
//		 UpkeepCost = _upkeepCost;
//		 Size = _size;
//		 MaxNPCs = _maxNPCs;
//	}
	
	// Constructor: Blank
	public PlayerTown ()
	{		
	}
	
	// Implementing Comparable for sorting purposes
	public int compareTo(PlayerTown otherTown)
	{
		return otherTown.townLevel.level - this.townLevel.level;
	}
	
	// Comparison
	public boolean equals(PlayerTown otherTown)
	{
		return otherTown.id_PlayerTown == this.id_PlayerTown;
	}
	
	// Creates the region
	public void buildRegion()
	{
		region = new Region2D(centerPoint.getX() - (townLevel.size / 2), centerPoint.getZ() + (townLevel.size / 2),
							  centerPoint.getX() + (townLevel.size / 2), centerPoint.getZ() - (townLevel.size / 2));
	}
	
	// Checks to see whether the town is already at maximum level; used by /townupgrade
	public boolean isAtMaxLevel()
	{
		if( id_Faction == 0 )
			return townLevel.level >= RageConfig.Town_MAX_LEVEL_NEUTRAL;
		else
			return townLevel.level >= RageConfig.Town_MAXLEVEL_FACTION;
	}
	
	public boolean isCapitol()
	{
		return RageConfig.townLevels.get(townLevel).isCapitol;
	}

	// Checks to see if the town already has its maximum number of residents
	public boolean isFull() 
	{
		int numberOfResidents = RageMod.database.countResidents(townName);
		
		return numberOfResidents >= townLevel.maxResidents;
	}
	
	// Returns all of the info for the current level
	public TownLevel getLevel()
	{
		return townLevel;
	}
	
	// Returns whether or not the specified location is inside the region
	public boolean isInside(Location location)
	{
		return region.isInside(location);
	}
	
	// Returns a Location at the center of the town
	public Location getCenter()
	{
		return new Location(world, centerPoint.getX(), 65, centerPoint.getZ());
	}
	
	// Puts a border of cobblestone on the edges of the town
	public void createBorder()
	{
		int x, z;
		
		for (x = (int)region.nwCorner.getX() + 1; x <= (int)region.seCorner.getX() - 1; x++) 
		{
            // North Wall
			z = (int)region.nwCorner.getZ();
			placeOverlay(x, z);
			
			// South Wall
			z = (int)region.seCorner.getZ();
			placeOverlay(x, z);
        }
		
		for (z = (int)region.nwCorner.getZ(); z >= (int)region.seCorner.getZ(); z--) 
        {
			// West Wall
			x = (int)region.nwCorner.getX();
			placeOverlay(x, z);
			
			// East Wall
			x = (int)region.seCorner.getX();
			placeOverlay(x, z);
        }
	}
	
	// Part of createBorder()
	private void placeOverlay(int x, int z)
	{
		for (int y = 127; y >= 1; y--) 
        {
            int upperType = world.getBlockTypeIdAt(x, y, z);
            int lowerType = world.getBlockTypeIdAt(x, y-1, z);
            
            if( upperType == 0 && lowerType != 0 )
            {
            	// Replace grass with cobble
            	if( Material.getMaterial(lowerType) == Material.LONG_GRASS )
            	{
            		world.getBlockAt(x, y-1, z).setType(Material.COBBLESTONE);
            	}
            	// Don't place blocks on top of trees or torches
            	else if( Material.getMaterial(lowerType) != Material.LEAVES 
            		&& Material.getMaterial(lowerType) != Material.TORCH)
            	{
            		world.getBlockAt(x, y, z).setType(Material.COBBLESTONE);
            	}
            	return;
            }
        }
	}
	
	// Create the floor of the inner sanctum
	public void buildSanctumFloor()
	{
		
	}
	
	
	
	
	

}
