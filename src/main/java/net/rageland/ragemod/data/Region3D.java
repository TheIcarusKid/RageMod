package net.rageland.ragemod.data;

import org.bukkit.Location;
import org.bukkit.World;

// TODO: Make the coords ambidextrous (ie. write an isBetween method)

public class Region3D 
{
	public Location nwCorner;
	public Location seCorner;
	
	public Region3D( World world, String coords )
	{
		String[] split = coords.split(",");
		try
		{
			nwCorner = new Location(world, Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
			seCorner = new Location(world, Double.parseDouble(split[3]), Double.parseDouble(split[4]), Double.parseDouble(split[5]));
		}
		catch( Exception ex )
		{
			System.out.println("ERROR: Invalid coordinate string passed to Region3D constructor");
		}
	}
	
	// Tests to see whether the current Location is inside the region
	public boolean isInside(Location loc)
	{
		return ((loc.getX() >= nwCorner.getX() && loc.getX() < (seCorner.getX())) &&
				(loc.getY() >= nwCorner.getY() && loc.getY() < (seCorner.getY())) &&
				(loc.getZ() <= nwCorner.getZ() && loc.getZ() > (seCorner.getZ())));   // Stupid Z axis is reversed
	}
	




}
