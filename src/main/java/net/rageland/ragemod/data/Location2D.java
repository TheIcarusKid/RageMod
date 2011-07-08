package net.rageland.ragemod.data;

import org.bukkit.Location;

// Simplified version of location checking that ignores Y coordinates - for zones and towns
public class Location2D 
{
	private double x;
	private double z;
	
	public Location2D( double X, double Z )
	{
		x = X;
		z = Z;
	}
	
	public Location2D( Location location )
	{
		x = location.getX();
		z = location.getZ();
	}
	
	public double getX() { return x; }
	public double getZ() { return x; }
	
	public double distance( Location location )
	{
		return ( Math.sqrt( Math.pow(x-location.getX(),2) + Math.pow(z-location.getZ(),2) ));
	}
	
	public double distance( Location2D location )
	{
		return ( Math.sqrt( Math.pow(x-location.getX(),2) + Math.pow(z-location.getZ(),2) ));
	}
	
	// What's the correct naming convention for this one?  <_<
	public static double Distance( Location loc1, Location loc2 )
	{
		return ( Math.sqrt( Math.pow(loc1.getX()-loc2.getX(),2) + Math.pow(loc1.getZ()-loc2.getZ(),2) ));
	}

}
