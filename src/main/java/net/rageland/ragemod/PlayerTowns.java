package net.rageland.ragemod;

import java.util.HashMap;

public class PlayerTowns {
	
	// Set up PlayerTowns as a static instance
	private static volatile PlayerTowns instance;
	
	public static HashMap<String, PlayerTown> Towns;
	
    public static PlayerTowns GetInstance() 
    {
		if (instance == null) 
		{
			instance = new PlayerTowns();
		}
		return instance;
	}
	
	// On startup, pull all the PlayerTown data from the DB into memory 
	public void LoadPlayerTowns()
	{
		Towns = RageMod.Database.LoadPlayerTowns();
								
	}

}

