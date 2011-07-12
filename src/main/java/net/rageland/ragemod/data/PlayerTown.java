package net.rageland.ragemod.data;

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
	public float treasuryBalance;
	public Date bankruptDate;
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
		return otherTown.townLevel.Level - this.townLevel.Level;
	}
	
	// Comparison
	public boolean equals(PlayerTown otherTown)
	{
		return otherTown.id_PlayerTown == this.id_PlayerTown;
	}
	
	// Creates the region
	public void buildRegion()
	{
		region = new Region2D(centerPoint.getX() - (townLevel.Size / 2), centerPoint.getZ() + (townLevel.Size / 2),
							  centerPoint.getX() + (townLevel.Size / 2), centerPoint.getZ() - (townLevel.Size / 2));
	}
	
	// Checks to see whether the town is already at maximum level; used by /townupgrade
	public boolean isAtMaxLevel()
	{
		if( id_Faction == 0 )
			return townLevel.Level >= RageConfig.Town_MaxLevel_Neutral;
		else
			return townLevel.Level >= RageConfig.Town_MaxLevel_Faction;
	}
	
	public boolean isCapitol()
	{
		return RageConfig.TownLevels.get(townLevel).IsCapitol;
	}

	// Checks to see if the town already has its maximum number of residents
	public boolean isFull() 
	{
		int numberOfResidents = RageMod.Database.countResidents(townName);
		
		return numberOfResidents >= townLevel.MaxResidents;
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
	
	// Puts a border of cobblestone on the edges of the town
	public void createBorder()
	{
		int x, z;
		
		for (x = (int)region.nwCorner.getX(); x <= (int)region.seCorner.getX(); x++) 
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
            	if( Material.getMaterial(lowerType) != Material.LEAVES 
            		&& Material.getMaterial(lowerType) != Material.TORCH)
            		world.getBlockAt(x, y, z).setType(Material.COBBLESTONE);
            	return;
            }
        }
	}
	
	
	
	
	

}
