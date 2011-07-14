package net.rageland.ragemod;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;

import net.rageland.ragemod.data.TownLevel;

// Stores and loads configuration values
public class RageConfig {
	
	/* TODO: 
	 *  - Faction info?
	 * 
	 */
	
	// General settings
	public static String ServerName = "Rageland";
	
	// Database settings
	public static String DB_URL = "jdbc:mysql://localhost:3306/";
    public static String DB_NAME = "johnz2_ragemod";
    public static String DB_DRIVER = "com.mysql.jdbc.Driver";
    public static String DB_USER = "johnz2_ragemod";
    public static String DB_PASSWORD = "ragemod";
    
    // Town settings
    public static int Town_MIN_DISTANCE_BETWEEN = 400;
    public static int Town_MIN_DISTANCE_ENEMY_CAPITOL = 2000;
    public static int Town_MIN_DISTANCE_SPAWN = 1000;  // This leaves the issue of towns hanging into the neutral zone
    public static int Town_MAX_LEVEL_NEUTRAL = 4;
    public static int Town_MAXLEVEL_FACTION = 5;
    public static int Town_DISTANCE_BETWEEN_BEDS = 6;
    public static double Town_UPKEEP_PER_PLAYER = 1.00;
    public static int Town_MAX_BANKRUPT_DAYS = 7;
    
    public static HashMap<Integer, TownLevel> townLevels;
    
    // Zone settings
    public static String Zone_NAME_A = "the Neutral Zone";
    public static int Zone_BORDER_A = 1000;  // Distance from spawn
    public static String Zone_NAME_B = "the War Zone";
    public static int Zone_BORDER_B = 2000;  // Distance from spawn
    public static String Zone_NAME_C = "The Wilds";
    public static int Zone_BORDER_C = 2500;  // Distance from spawn

    // Lot settings
    public static int Lot_X_OFFSET = -384;			// How to convert the web X coordinates to in-game coords
    public static int Lot_Z_OFFSET = 144;
    public static int Lot_MULTIPLIER = 16;			// The lot grid is based on 16x16 chunks
    public static int Lot_PRICE_COAL = 10;			// Price for Coal-level member lot in USD
    public static int Lot_PRICE_IRON = 20;			// Price for Iron-level member lot in USD
    public static int Lot_PRICE_GOLD = 30;			// Price for Gold-level member lot in USD
    public static int Lot_PRICE_DIAMOND = 40;		// Price for Diamond-level member lot in USD
    
    // Capitol settings
    public static int Capitol_X1a = -384;			// The NW corner of region A for capitol
    public static int Capitol_Z1a = 144;
    public static int Capitol_X2a = 100;			// The SE corner of region A for capitol
    public static int Capitol_Z2a = -244;
    public static String Capitol_Name = "Rage City";
    
    // Cooldowns (in seconds)
    public static int Cooldown_Spawn = 30;
    public static int Cooldown_Home = 30;
    
    // Faction settings
    public static int Faction_BaseJoinCost = 100;		// Initial cost in coins to join a faction
    public static int Faction_JoinCostIncrease = 10; 	// Amount the join cost will go up due to population imbalance
    
    // Task frequencies (in seconds)
    // 1 hour: 3600
    // 1 day:  86400
    public static int Task_TOWN_UPKEEP = 86400;			// Charge taxes for player towns

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
    	townLevels = new HashMap<Integer, TownLevel>();
    	
    	TownLevel townLevel = new TownLevel();
    	townLevel.level = 1;
    	townLevel.name = "Settlement";
    	townLevel.size = 80;
    	townLevel.initialCost = 1000;
    	townLevel.upkeepCost = 5;
    	townLevel.minimumBalance = 100;
    	townLevel.maxResidents = 5;
    	townLevel.maxNPCs = 1;
    	townLevel.sanctumFloor = buildSanctumFloor(townLevel.level);
    	townLevels.put(1, townLevel);
    	
    	townLevel = new TownLevel();
    	townLevel.level = 2;
    	townLevel.name = "Village";
    	townLevel.size = 120;
    	townLevel.initialCost = 2000;
    	townLevel.upkeepCost = 10;
    	townLevel.minimumBalance = 200;
    	townLevel.maxResidents = 10;
    	townLevel.maxNPCs = 2;
    	townLevels.put(2, townLevel);
    	
    	townLevel = new TownLevel();
    	townLevel.level = 3;
    	townLevel.name = "Town";
    	townLevel.size = 180;
    	townLevel.initialCost = 3000;
    	townLevel.upkeepCost = 15;
    	townLevel.minimumBalance = 300;
    	townLevel.maxResidents = 15;
    	townLevel.maxNPCs = 3;
    	townLevels.put(3, townLevel);
    	
    	townLevel = new TownLevel();
    	townLevel.level = 4;
    	townLevel.name = "City";
    	townLevel.size = 270;
    	townLevel.initialCost = 5000;
    	townLevel.upkeepCost = 25;
    	townLevel.minimumBalance = 500;
    	townLevel.maxResidents = 25;
    	townLevel.maxNPCs = 4;
    	townLevels.put(4, townLevel);
    	
    	townLevel = new TownLevel();
    	townLevel.level = 5;
    	townLevel.name = "Capitol";
    	townLevel.size = 400;
    	townLevel.initialCost = 10000;
    	townLevel.upkeepCost = 50;
    	townLevel.minimumBalance = 1000;
    	townLevel.maxResidents = 50;
    	townLevel.maxNPCs = 6;
    	townLevel.isCapitol = true;
    	townLevels.put(5, townLevel);
    }
    
    //		c: Cobblestone
    //		d: Dirt
    //		p: Wood Planks
    //		s: Stone
    //		t: Tile (slab)
    //		o: Obsidian
	//		O: Obsidian stack 2-high w/ glowstone on top
    //		g: Glowstone
	//		G: Glowstone stack 2-high
    //		w: Wool (of appropriate color)
    //		-: Portal (inside)
    //		|: Portal (outside)
    //		b: Bedrock
    //		L: Liquid (water/lava)
    //		n: Snow
    //		i: Iron Block
    //		S: Special faction block (lapis/netherrack)
    public static ArrayList<String> buildSanctumFloor(int level)
    {
    	ArrayList<String> floor = new ArrayList<String>();
    	
    	if( level == 1 )
    	{
    		floor.add("cccccccccccccccccccc"); 
    		floor.add("cddddddddccddddddddc");
    		floor.add("cddddddddccddddddddc");
    		floor.add("cddddddddccddddddddc");
    		floor.add("cddddddddccddddddddc"); // 5
    		floor.add("cddddddddccddddddddc"); 
    		floor.add("cddddddddccddddddddc");
    		floor.add("cdddddccccccccdddddc");
    		floor.add("cdddddcwwwwwwcdddddc");
    		floor.add("cccccccwoggowccccccc"); // 10
    		floor.add("cccccccwoggowccccccc"); 
    		floor.add("cdddddcwwwwwwcdddddc");
    		floor.add("cdddddccccccccdddddc");
    		floor.add("cddddddddccddddddddc");
    		floor.add("cddddddddccddddddddc"); // 15
    		floor.add("cddddddddccddddddddc"); 
    		floor.add("cddddddddccddddddddc");
    		floor.add("cddddddddccddddddddc");
    		floor.add("cddddddddccddddddddc");
    		floor.add("cccccccccccccccccccc"); // 20
    	}
    	else if( level == 2 )
    	{
    		floor.add("ssssssssssssssssssss");
    		floor.add("sppppppppsspppppppps");
    		floor.add("sppppppppsspppppppps");
    		floor.add("sppppppppsspppppppps");
    		floor.add("sppppppppsspppppppps"); // 5
    		floor.add("sppppppppsspppppppps");
    		floor.add("spppppssssssssppppps");
    		floor.add("sppppsswwwwwwsspppps");
    		floor.add("sppppswwwwwwwwspppps");
    		floor.add("sssssswgOggOgwssssss"); // 10
    		floor.add("sssssswgOggOgwssssss");
    		floor.add("sppppswwwwwwwwspppps");
    		floor.add("sppppsswwwwwwsspppps");
    		floor.add("spppppssssssssppppps");
    		floor.add("sppppppppsspppppppps"); // 15
    		floor.add("sppppppppsspppppppps");
    		floor.add("sppppppppsspppppppps");
    		floor.add("sppppppppsspppppppps");
    		floor.add("sppppppppsspppppppps");
    		floor.add("ssssssssssssssssssss"); // 20
    	}
    	else if( level == 3 )
    	{
    		floor.add("bbbbbbbbbbbbbbbbbbbb");
    		floor.add("bssssssssbbssssssssb");
    		floor.add("bssssssssbbssssssssb");
    		floor.add("bssssssssbbssssssssb");
    		floor.add("bssssssssbbssssssssb"); // 5
    		floor.add("bsssssbbbbbbbbsssssb");
    		floor.add("bssssbbwwwwwwbbssssb");
    		floor.add("bsssbbwwwwwwwwbbsssb");
    		floor.add("bsssbwwwggggwwwbsssb");
    		floor.add("bbbbbwwg|--|gwwbbbbb"); // 10
    		floor.add("bbbbbwwg|--|gwwbbbbb");
    		floor.add("bsssbwwwggggwwwbsssb");
    		floor.add("bsssbbwwwwwwwwbbsssb");
    		floor.add("bssssbbwwwwwwbbssssb");
    		floor.add("bsssssbbbbbbbbsssssb"); // 15
    		floor.add("bssssssssbbssssssssb");
    		floor.add("bssssssssbbssssssssb");
    		floor.add("bssssssssbbssssssssb");
    		floor.add("bssssssssbbssssssssb");
    		floor.add("bbbbbbbbbbbbbbbbbbbb"); // 20
    	}
    	else if( level == 4 )
    	{
    		floor.add("tttttttttttttttttttt");
    		floor.add("tnnnnnnntwwtnnnnnnnt");
    		floor.add("tnnnnnnntwwtnnnnnnnt");
    		floor.add("tnnnnnnntwwtnnnnnnnt");
    		floor.add("tnnnnntttwwtttnnnnnt"); // 5
    		floor.add("tnnnnttwwwwwwttnnnnt");
    		floor.add("tnnnttwwwwwwwwttnnnt");
    		floor.add("tnnttwwwwwwwwwtttnnt");
    		floor.add("ttttwwwggggggwwwtttt");
    		floor.add("twwwwwwg|--|gwwwwwwt"); // 10
    		floor.add("twwwwwwg|--|gwwwwwwt");
    		floor.add("ttttwwwggggggwwwtttt");
    		floor.add("tnnttwwwwwwwwwwttnnt");
    		floor.add("tnnnttwwwwwwwwttnnnt");
    		floor.add("tnnnnttwwwwwwttnnnnt"); // 15
    		floor.add("tnnnnntttwwtttnnnnnt");
    		floor.add("tnnnnnnntwwtnnnnnnnt");
    		floor.add("tnnnnnnntwwtnnnnnnnt");
    		floor.add("tnnnnnnntwwtnnnnnnnt");
    		floor.add("tttttttttttttttttttt"); // 20
    	}
    	else if( level == 5 )
    	{
    		floor.add("iiiiiiiiiiiiiiiiiiii");
    		floor.add("iLLLLLLLiwwiLLLLLLLi");
    		floor.add("iLLLLLLLiwwiLLLLLLLi");
    		floor.add("iLLLLLiiiwwiiiLLLLLi");
    		floor.add("iLLLLiiwwwwwwiiLLLLi"); // 5
    		floor.add("iLLLiiwwwwwwwwiiLLLi");
    		floor.add("iLLiiwwwwwwwwwwiiLLi");
    		floor.add("iLiiwwggggggggwwiiLi");
    		floor.add("iiiwwwgSSSSSSgwwwiii");
    		floor.add("iwwwwwgS|--|Sgwwwwwi"); // 10
    		floor.add("iwwwwwgS|--|Sgwwwwwi");
    		floor.add("iiiwwwgSSSSSSgwwwiii");
    		floor.add("iLiiwwggggggggwwiiLi");
    		floor.add("iLLiiwwwwwwwwwwiiLLi");
    		floor.add("iLLLiiwwwwwwwwiiLLLi");
    		floor.add("iLLLLiiwwwwwwiiLLLLi");
    		floor.add("iLLLLLiiiwwiiiLLLLLi");
    		floor.add("iLLLLLLLiwwiLLLLLLLi");
    		floor.add("iLLLLLLLiwwiLLLLLLLi");
    		floor.add("iiiiiiiiiiiiiiiiiiii");
    		
    	}
    	
    	return floor;
    }

    
    
    
    
    
    
}