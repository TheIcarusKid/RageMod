package net.rageland.ragemod;

import java.util.HashMap;

import net.rageland.ragemod.data.TownLevel;

// Stores and loads configuration values
public class RageConfig {
	
	/* TODO: 
	 *  - Faction info?
	 * 
	 */
	
	// Database settings
	public static String DB_URL = "jdbc:mysql://localhost:3306/";
    public static String DB_DatabaseName = "johnz2_ragemod";
    public static String DB_Driver = "com.mysql.jdbc.Driver";
    public static String DB_User = "johnz2_ragemod";
    public static String DB_Password = "ragemod";
    
    // Town settings
    public static int Town_MinDistanceBetween = 400;
    public static int Town_MinDistanceEnemyCapitol = 2000;
    public static int Town_MinDistanceSpawn = 1000;  // This leaves the issue of towns hanging into the neutral zone
    public static int Town_MaxLevel_Neutral = 4;
    public static int Town_MaxLevel_Faction = 5;
    public static HashMap<Integer, TownLevel> TownLevels;
    
    // Zone settings
    public static String ZoneA_Name = "Neutral Zone";
    public static int ZoneA_Border = 100;  // Distance from spawn
    public static String ZoneB_Name = "War Zone";
    public static int ZoneB_Border = 200;  // Distance from spawn
    public static String ZoneC_Name = "The Wilds";
    public static int ZoneC_Border = 250;  // Distance from spawn

    // Capitol/lot settings
    public static int Lot_XOffset = -100;			// How to convert the web X coordinates to in-game coords
    public static int Lot_ZOffset = -200;
    public static int Lot_Multiplier = 16;			// The lot grid is based on 16x16 chunks

    
    

    private static volatile RageConfig instance;
	
    public static RageConfig GetInstance() 
    {
		if (instance == null) 
		{
			instance = new RageConfig();
		}
		return instance;
	}
    
    public RageConfig ()
    {
    	loadDefaultTownLevels();
    }
    
    private void loadDefaultTownLevels()
    {
    	TownLevels = new HashMap<Integer, TownLevel>();
    	
    	TownLevel townLevel = new TownLevel();
    	townLevel.Level = 1;
    	townLevel.Name = "Settlement";
    	townLevel.Size = 80;
    	townLevel.InitialCost = 1000;
    	townLevel.UpkeepCost = 5;
    	townLevel.MinimumBalance = 100;
    	townLevel.MaxResidents = 5;
    	townLevel.MaxNPCs = 1;
    	TownLevels.put(1, townLevel);
    	
    	townLevel = new TownLevel();
    	townLevel.Level = 2;
    	townLevel.Name = "Village";
    	townLevel.Size = 120;
    	townLevel.InitialCost = 2000;
    	townLevel.UpkeepCost = 10;
    	townLevel.MinimumBalance = 200;
    	townLevel.MaxResidents = 10;
    	townLevel.MaxNPCs = 2;
    	TownLevels.put(2, townLevel);
    	
    	townLevel = new TownLevel();
    	townLevel.Level = 3;
    	townLevel.Name = "Town";
    	townLevel.Size = 180;
    	townLevel.InitialCost = 3000;
    	townLevel.UpkeepCost = 15;
    	townLevel.MinimumBalance = 300;
    	townLevel.MaxResidents = 15;
    	townLevel.MaxNPCs = 3;
    	TownLevels.put(3, townLevel);
    	
    	townLevel = new TownLevel();
    	townLevel.Level = 4;
    	townLevel.Name = "City";
    	townLevel.Size = 270;
    	townLevel.InitialCost = 5000;
    	townLevel.UpkeepCost = 25;
    	townLevel.MinimumBalance = 500;
    	townLevel.MaxResidents = 25;
    	townLevel.MaxNPCs = 4;
    	TownLevels.put(3, townLevel);
    	
    	townLevel = new TownLevel();
    	townLevel.Level = 5;
    	townLevel.Name = "Capitol";
    	townLevel.Size = 400;
    	townLevel.InitialCost = 10000;
    	townLevel.UpkeepCost = 50;
    	townLevel.MinimumBalance = 1000;
    	townLevel.MaxResidents = 50;
    	townLevel.MaxNPCs = 6;
    	townLevel.IsCapitol = true;
    	TownLevels.put(3, townLevel);
    	
    	
    }

}
