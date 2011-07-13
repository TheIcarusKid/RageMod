package net.rageland.ragemod;

// TODO: Refactor into connection pooling

// TODO: Prevent null pointer exception when database not found (!)

import java.sql.*;
import java.util.ArrayList;
import java.sql.Date;
import java.util.HashMap;

import net.rageland.ragemod.data.Location2D;
import net.rageland.ragemod.data.Lot;
import net.rageland.ragemod.data.Lots;
import net.rageland.ragemod.data.PlayerData;
import net.rageland.ragemod.data.PlayerTown;
import net.rageland.ragemod.data.PlayerTowns;
import net.rageland.ragemod.data.Players;
import net.rageland.ragemod.data.Region2D;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RageDB {

    protected Connection conn;
    protected String url;
    protected String databaseName;
    protected String driver;
    protected String user;
    protected String password;
    
    private PreparedStatement preparedStatement = null;
    private ResultSet rs = null;
    
    private RageMod plugin;

    public RageDB(RageMod instance)
    {
    	plugin = instance;
    	
    	url = RageConfig.DB_URL;
    	databaseName = RageConfig.DB_NAME;
    	driver = RageConfig.DB_DRIVER;
    	user = RageConfig.DB_USER;
    	password = RageConfig.DB_PASSWORD;
    	
        try
        {
          Class.forName(driver).newInstance();
           conn = DriverManager.getConnection(url + databaseName, user, password);
        }
        catch(Exception e)
        {
           System.out.println(e);
        }
    }

    public Connection getConnection()
    {
      return conn;
    }
        
    
    
    // You need to close the resultSet
	private void close() {
		try {
			if (rs != null) {
				rs.close();
			}

			if (preparedStatement != null) {
				preparedStatement.close();
			}

			if (conn != null) {
				//conn.close();
			}
		} catch (Exception e) {

		}
	}
	
	// Load all PlayerTown data
	public HashMap<String, PlayerTown> loadPlayerTowns()
    {
		HashMap<String, PlayerTown> towns = new HashMap<String, PlayerTown>();
		PlayerTown currentTown = null;
		
    	try
    	{
        	preparedStatement = conn.prepareStatement(
				"SELECT pt.ID_PlayerTown, pt.TownName, pt.XCoord, pt.ZCoord, " +
				"	pt.TownLevel, " +
				"	IFNULL(f.ID_Faction, 0) as ID_Faction, pt.TreasuryBalance, pt.BankruptDate, p.Name AS Mayor " +
				"FROM PlayerTowns pt " +
				"LEFT JOIN Factions f ON pt.ID_Faction = f.ID_Faction " +
				"INNER JOIN Players p ON pt.ID_PlayerTown = p.ID_PlayerTown " +
				"WHERE p.IsMayor = 1 AND pt.IsDeleted = 0");
        	
        	rs = preparedStatement.executeQuery();
        	
        	while ( rs.next() ) 
        	{	        		
        		currentTown = new PlayerTown();
        		currentTown.id_PlayerTown = rs.getInt("ID_PlayerTown");
        		currentTown.townName = rs.getString("TownName");
        		currentTown.centerPoint = new Location2D(rs.getInt("XCoord"), rs.getInt("ZCoord"));
        		currentTown.id_Faction = rs.getInt("ID_Faction");
        		currentTown.treasuryBalance = rs.getFloat("TreasuryBalance");
        		currentTown.bankruptDate = rs.getDate("BankruptDate");
        		currentTown.townLevel = RageConfig.townLevels.get(rs.getInt("TownLevel"));
        		currentTown.mayor = rs.getString("Mayor");
        		currentTown.world = plugin.getServer().getWorld("world");
        		
        		currentTown.buildRegion();	        		
        		towns.put(rs.getString("TownName").toLowerCase(), currentTown);	 
        	}
        		
        	return towns;				
        		        	
    	} catch (Exception e) {
    		System.out.println("Error in RageDB.LoadPlayerTowns: " + e.getMessage());
		} finally {
			close();
		}
		
		return null;
    }
	
	// Load data from Players table on login if existing player - create new row if not 
	public PlayerData playerLogin(String playerName)
    {
		PlayerData playerData = null;
		ResultSet rs = null;    		
		
    	try
    	{
        	String selectQuery = 
        		"SELECT p.ID_Player, p.Name, IFNULL(p.ID_Faction, 0) as ID_Faction, p.IsMember, p.MemberExpiration, p.Bounty, p.ExtraBounty, " +
        		"   (p.Home_XCoord IS NOT NULL) AS Home_IsSet, p.Home_XCoord, p.Home_YCoord, p.Home_ZCoord, p.Home_LastUsed, " +
        		"	(p.Spawn_XCoord IS NOT NULL) AS Spawn_IsSet, p.Spawn_XCoord, p.Spawn_YCoord, p.Spawn_ZCoord, p.Spawn_LastUsed, " +
        		"	IFNULL(pt.TownName, '') as TownName, p.IsMayor " +
        		"FROM Players p " +
        		"LEFT JOIN PlayerTowns pt ON p.ID_PlayerTown = pt.ID_PlayerTown " +
        		"WHERE p.Name = '" + playerName + "'";
    		
    		preparedStatement = conn.prepareStatement(selectQuery);	        		        	
        	rs = preparedStatement.executeQuery();
        	
        	// Test to see if result set was empty
        	if( rs.next() )
        	{
        		playerData = fillPlayerData(rs);
        		
        		// Set LastLogin time
        		preparedStatement = conn.prepareStatement("UPDATE Players SET LastLogin = NOW() WHERE Name = '" + playerName + "'");
        		preparedStatement.executeUpdate();
        	}
        	else
        	{
        		// Insert new data into DB - default DB values will take care of the data
        		preparedStatement = conn.prepareStatement("INSERT INTO Players (Name, LastLogin) VALUES ('" + playerName + "', NOW())");
        		preparedStatement.executeUpdate();
        		
        		// Pull the DB-created defaults into memory
        		preparedStatement = conn.prepareStatement(selectQuery);		
        		rs = preparedStatement.executeQuery();
        		
        		rs.next();
        		playerData = fillPlayerData(rs);
        	}	        	
        	
        	return playerData;				
        		        	
    	} catch (SQLException e) {
    		System.out.println("Error in RageDB.PlayerLogin(): " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		} finally {
			close();
		}
		
		return null;
    }
	
	
	// Load data from Players table for specified player; returns NULL if not found 
	public PlayerData playerFetch(String playerName)
    {
		PlayerData playerData = null;
		ResultSet rs = null;    		
		
    	try
    	{
        	String selectQuery = 
        		"SELECT p.ID_Player, p.Name, IFNULL(p.ID_Faction, 0) as ID_Faction, p.IsMember, p.MemberExpiration, p.Bounty, p.ExtraBounty, " +
        		"   (p.Home_XCoord IS NOT NULL) AS Home_IsSet, p.Home_XCoord, p.Home_YCoord, p.Home_ZCoord, p.Home_LastUsed, " +
        		"	(p.Spawn_XCoord IS NOT NULL) AS Spawn_IsSet, p.Spawn_XCoord, p.Spawn_YCoord, p.Spawn_ZCoord, p.Spawn_LastUsed, " +
        		"	IFNULL(pt.TownName, '') as TownName, p.IsMayor " +
        		"FROM Players p " +
        		"LEFT JOIN PlayerTowns pt ON p.ID_PlayerTown = pt.ID_PlayerTown " +
        		"WHERE p.Name = '" + playerName + "'";
    		
    		preparedStatement = conn.prepareStatement(selectQuery);	        		        	
        	rs = preparedStatement.executeQuery();
        	
        	// Test to see if result set was empty - return null if not 
        	if( rs.next() )
        	{
        		playerData = fillPlayerData(rs);
	        	return playerData;
        	}
        		        	
    	} catch (SQLException e) {
    		System.out.println("Error in RageDB.PlayerFetch(): " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		} finally {
			close();
		}
		
		return null;
    }
	
	// Fill the PlayerData class with data from a result set
	private PlayerData fillPlayerData(ResultSet rs) throws SQLException
	{
		PlayerData playerData = new PlayerData();
		
		playerData.id_Player = rs.getInt("ID_Player");
		playerData.name = rs.getString("Name");
		playerData.id_Faction = rs.getInt("ID_Faction");
		playerData.isMember = rs.getBoolean("IsMember");
		playerData.memberExpiration = rs.getDate("MemberExpiration");
		playerData.bounty = rs.getFloat("Bounty");
		playerData.extraBounty = rs.getFloat("ExtraBounty");
		playerData.townName = rs.getString("TownName");
		playerData.isMayor = rs.getBoolean("IsMayor");
		
		playerData.home_IsSet = rs.getBoolean("Home_IsSet");
		playerData.home_X = rs.getInt("Home_XCoord");
		playerData.home_Y = rs.getInt("Home_YCoord");
		playerData.home_Z = rs.getInt("Home_ZCoord");
		playerData.home_LastUsed = rs.getTimestamp("Home_LastUsed");
		
		playerData.spawn_IsSet = rs.getBoolean("Spawn_IsSet");
		playerData.spawn_X = rs.getInt("Spawn_XCoord");
		playerData.spawn_Y = rs.getInt("Spawn_YCoord");
		playerData.spawn_Z = rs.getInt("Spawn_ZCoord");
		playerData.spawn_LastUsed = rs.getTimestamp("Spawn_LastUsed");
		
		playerData.lots = getLots(playerData.id_Player);
		
    	return playerData;
	}
	
	// Make a separate query to get all lots owned by the player
	private ArrayList<Lot> getLots(int id_Player) 
	{
		ResultSet rs = null;    
		ArrayList<Lot> playerLots = new ArrayList<Lot>();
	
    	try
    	{
        	String selectQuery = 
        		"SELECT ID_Lot FROM Lots WHERE ID_Player = " + id_Player;
    		
    		preparedStatement = conn.prepareStatement(selectQuery);	        		        	
        	rs = preparedStatement.executeQuery();
        	
        	// Test to see if result set was empty - return null if not 
        	while( rs.next() )
        	{
        		playerLots.add(Lots.get(rs.getInt("ID_Lot")));	// We already have the lot info in memory; save the DB some work
        	}
        		        	
    	} catch (SQLException e) {
    		System.out.println("Error in RageDB.PlayerFetch(): " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		} finally {
			close();
		}
    	
    	return playerLots;
	}
	
	// Update the database with the player data stored in memory (skips town info)
	public void updatePlayer(PlayerData playerData)
	{
		String updateString = "";
		try
    	{
    		// Update the Players table to create the town association
    		updateString = 
				"UPDATE Players SET " +
				"ID_Faction = " + (playerData.id_Faction == 0 ? "null" : playerData.id_Faction) + ", " +
				"IsMember = " + (playerData.isMember ? 1 : 0) + ", " +
				"MemberExpiration = " + playerData.memberExpiration + ", " +
				"Bounty = " + playerData.bounty + ", " +
				"ExtraBounty = " + playerData.extraBounty + ", " +
    			"Home_LastUsed = " + (playerData.home_LastUsed == null ? "null" : "'" + playerData.home_LastUsed + "'") + ", " +
    			"Spawn_LastUsed = " + (playerData.spawn_LastUsed == null ? "null" : "'" + playerData.spawn_LastUsed + "'") + ", ";
    		
    		if( playerData.home_IsSet )
    		{
    			updateString += "Home_XCoord = " + playerData.home_X + ", " +
					"Home_YCoord = " + playerData.home_Y + ", " +
					"Home_ZCoord = " + playerData.home_Z + ", ";
    		}
    		else
    		{
    			updateString += "Home_XCoord = NULL, " +
					"Home_YCoord = NULL, " +
					"Home_ZCoord = NULL, ";
    		}
    		if( playerData.spawn_IsSet )
    		{
    			updateString += "Spawn_XCoord = " + playerData.spawn_X + ", " +
					"Spawn_YCoord = " + playerData.spawn_Y + ", " +
					"Spawn_ZCoord = " + playerData.spawn_Z + " ";
    		}
    		else
    		{
    			updateString += "Spawn_XCoord = NULL, " +
					"Spawn_YCoord = NULL, " +
					"Spawn_ZCoord = NULL ";
    		}
    		
    		updateString += "WHERE ID_Player = " + playerData.id_Player;
    		
    		preparedStatement = conn.prepareStatement(updateString);
    		preparedStatement.executeUpdate();	
        		        		        	
    	} catch (SQLException e) {
    		System.out.println("Error in RageDB.updatePlayer(): " + e.getMessage());
    		System.out.println("updateString: " + updateString);
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		} finally {
			close();
		}
	}
	
	
	
	// Load data from Players table on login if existing player - create new row if not 
	public void townAdd(String targetPlayerName, String townName)
    {
		PlayerData playerData = Players.Get(targetPlayerName);
		
    	try
    	{
    		// Update the Players table to create the town association
    		preparedStatement = conn.prepareStatement(
    				"UPDATE Players SET ID_PlayerTown = " +
    				"	(SELECT ID_PlayerTown FROM PlayerTowns WHERE TownName = '" + townName + "') " +
    				", IsMayor = 0 WHERE ID_Player = " + playerData.id_Player);
    		preparedStatement.executeUpdate();	
        		        		        	
    	} catch (SQLException e) {
    		System.out.println("Error in RageDB.TownAdd(): " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		} finally {
			close();
		}
    }
	
	
   	// Load data from Players table on login if existing player - create new row if not 
	public int townCreate(Player player, String townName)
    {
		ResultSet rs = null;   
		PlayerData playerData = Players.Get(player.getName());
		
    	try
    	{
    		// TODO: Set default treasury balance from config
    		// Insert the new town into the PlayerTowns table
    		preparedStatement = conn.prepareStatement(
    				"INSERT INTO PlayerTowns (TownName, XCoord, ZCoord, ID_Faction, TreasuryBalance, TownLevel, DateCreated) " +
    				"VALUES ('" + townName + "', " + (int)player.getLocation().getX() + ", " + (int)player.getLocation().getZ() + ", " +  
    				"(SELECT ID_Faction FROM Players WHERE ID_Player = " + playerData.id_Player + "), " + 
    				RageConfig.townLevels.get(1).MinimumBalance + ", 1, NOW())",
    				Statement.RETURN_GENERATED_KEYS);        		
    		preparedStatement.executeUpdate();
    		
    		// Retrieve the new auto-increment town ID 
    		rs = preparedStatement.getGeneratedKeys();
    		rs.next();
    		int townID = rs.getInt(1);
    		
    		// Update the Players table
    		preparedStatement = conn.prepareStatement(
    				"UPDATE Players SET ID_PlayerTown = " + townID + ", IsMayor = 1 " +
    				"WHERE ID_Player = " + playerData.id_Player);        		
    		preparedStatement.executeUpdate();
    		
    		return townID;
        		        		        	
    	} catch (SQLException e) {
    		System.out.println("Error in RageDB.TownCreate(): " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		} finally {
			close();
		}
    	
    	System.out.println("Error: RageDB.TownCreate() returned -1");
    	return -1;
    }

	// Reset the player's town affiliation - used by both Leave and Evict
	public void townLeave(String playerName)
    { 
		PlayerData playerData = Players.Get(playerName);
		
    	try
    	{
    		// Update the Players table to remove the town association
    		preparedStatement = conn.prepareStatement(
    				"UPDATE Players SET ID_PlayerTown = NULL, IsMayor = 0, Spawn_IsSet = 0 WHERE ID_Player = " + playerData.id_Player);
    		preparedStatement.executeUpdate();	
    	} 
    	catch (SQLException e) {
    		System.out.println("Error in RageDB.TownLeave(): " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		} finally {
			close();
		}
    }
	
	// Increments the TownLevel value by 1
	public void townUpgrade(String townName, int cost) 
	{    		
    	try
    	{
    		// Update the Players table to remove the town association
    		preparedStatement = conn.prepareStatement(
    				"UPDATE PlayerTowns SET TownLevel = (TownLevel + 1), TreasuryBalance = (TreasuryBalance - " + cost + ") " +
    				"WHERE TownName = '" + townName + "'" +
    				"");
    		preparedStatement.executeUpdate();	
    	} 
    	catch (SQLException e) {
    		System.out.println("Error in RageDB.TownUpgrade(): " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		} finally {
			close();
		}
	}

	// Returns the number of players associated to the specified town
	public int countResidents(String townName) 
	{
		ResultSet rs = null;   
		PlayerTown playerTown = PlayerTowns.get(townName);
		
    	try
    	{
    		preparedStatement = conn.prepareStatement(
    				"SELECT COUNT ID_Player FROM Players WHERE ID_PlayerTown = " + playerTown.id_PlayerTown);
    		rs = preparedStatement.executeQuery();
    		rs.next();
    		return rs.getInt(1);
    	} 
    	catch (SQLException e) {
    		System.out.println("Error in RageDB.CountResidents(): " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		} finally {
			close();
		}
    	
    	System.out.println("Error: RageDB.CountResidents() returned -1");
    	return -1;
	}
	
	// Returns all residents for a particular town, with mayor first
	public ArrayList<String> listTownResidents(String townName) 
	{
		ResultSet rs = null;   
		PlayerTown playerTown = PlayerTowns.get(townName);
		ArrayList<String> residents = new ArrayList<String>();
		
    	try
    	{
    		preparedStatement = conn.prepareStatement(
    				"SELECT Name FROM Players p " +
    				"WHERE ID_PlayerTown = " + playerTown.id_PlayerTown + " " +
    				"ORDER BY IsMayor DESC ");
    		rs = preparedStatement.executeQuery();
        	
        	while ( rs.next() ) 
        	{	        	
        		residents.add(rs.getString("Name"));	        		
        	}
        		
        	return residents;	
    	} 
    	catch (SQLException e) {
    		System.out.println("Error in RageDB.ListTownResidents(): " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		} finally {
			close();
		}
    	
    	return null;
	}
	
	// Returns all spawn points held by residents of a town, to see if any are too close
	public HashMap<String, Location> getSpawnLocations(int id_PlayerTown) 
	{
		ResultSet rs = null;   
		HashMap<String, Location> spawns = new HashMap<String, Location>();
		
    	try
    	{
    		preparedStatement = conn.prepareStatement(
    				"SELECT p.Name, p.Spawn_XCoord, p.Spawn_YCoord, p.Spawn_ZCoord " +
    				"FROM Players p " +
    				"WHERE p.Spawn_XCoord IS NOT NULL AND ID_PlayerTown = " + id_PlayerTown);
    		rs = preparedStatement.executeQuery();
        	
        	while ( rs.next() ) 
        	{	        	
        		spawns.put(rs.getString("Name"), 
        				new Location(plugin.getServer().getWorld("world"), 
        						rs.getInt("Spawn_XCoord"),
        						rs.getInt("Spawn_YCoord"),
        						rs.getInt("Spawn_ZCoord")));	        		
        	}
        		
        	return spawns;	
    	} 
    	catch (SQLException e) {
    		System.out.println("Error in RageDB.getSpawnLocations(): " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		} finally {
			close();
		}
    	
    	return null;
	}
	
	// Loads all of the lots in the city into memory
	public HashMap<String, Lot> loadLots() 
	{
		HashMap<String, Lot> lots = new HashMap<String, Lot>();
		Lot currentLot = null;
		int mult = RageConfig.Lot_MULTIPLIER;
		
    	try
    	{
        	preparedStatement = conn.prepareStatement(
        		"SELECT l.ID_Lot, l.Category, l.Number, IFNULL(p.Name, '') as Owner, l.XCoord, l.ZCoord, " +
        		"l.Width, l.Height " +
        		"FROM Lots l " +
        		"LEFT JOIN Players p ON l.ID_Player = p.ID_Player");	
        	
        	rs = preparedStatement.executeQuery();
        	
        	while ( rs.next() ) 
        	{
        		currentLot = new Lot();
        		
        		currentLot.id_Lot = rs.getInt("ID_Lot");
        		currentLot.setCategory(rs.getString("Category"));
        		currentLot.number = rs.getInt("Number");
        		currentLot.owner = rs.getString("Owner");
        		// X and Z are reversed >:(
        		currentLot.region = new Region2D(
        				((rs.getInt("ZCoord")-1) * mult) + RageConfig.Lot_X_OFFSET,
        				((rs.getInt("XCoord")-1) * mult * -1) + RageConfig.Lot_Z_OFFSET,
        				((rs.getInt("ZCoord")-1) * mult) + RageConfig.Lot_X_OFFSET + (rs.getInt("Height") * mult),
        				((rs.getInt("XCoord")-1) * mult * -1) + RageConfig.Lot_Z_OFFSET - (rs.getInt("Width") * mult));
        		currentLot.world = plugin.getServer().getWorld("world");
    	        
        		lots.put(currentLot.getLotCode(), currentLot);	        		
        	}
        		
        	return lots;				
        		        	
    	} catch (Exception e) {
    		System.out.println("Error in RageDB.LoadLots(): " + e.getMessage());
		} finally {
			close();
		}
		
		return null;
	}

	// Assign a lot to a player
	public void lotClaim(PlayerData playerData, Lot lot) 
	{
		try
    	{
    		// Update the Lots table to assign the owner
    		preparedStatement = conn.prepareStatement(
    				"UPDATE Lots SET ID_Player = " + playerData.id_Player + ", DateClaimed = NOW() WHERE ID_Lot = " + lot.id_Lot);
    		preparedStatement.executeUpdate();	
    	} 
    	catch (SQLException e) {
    		System.out.println("Error in RageDB.LotClaim(): " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		} finally {
			close();
		}
	}

	// Reset a lot's owner
	public void lotUnclaim(Lot lot) 
	{
		try
    	{
    		// Update the Lots table to assign the owner
    		preparedStatement = conn.prepareStatement(
    				"UPDATE Lots SET ID_Player = NULL, DateClaimed = NULL WHERE ID_Lot = " + lot.id_Lot);
    		preparedStatement.executeUpdate();	
    	} 
    	catch (SQLException e) {
    		System.out.println("Error in RageDB.LotUnclaim(): " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		} finally {
			close();
		}
	}
	
	// Return the amount the player has donated in the past month
	public int getRecentDonations(int id_Player)
	{
		try
    	{
        	preparedStatement = conn.prepareStatement(
        		"SELECT SUM(Amount) as Amount FROM Donations WHERE ID_Player = " + id_Player + 
        		" AND DATE_SUB(CURDATE(),INTERVAL 30 DAY) <= Date");
        		
        	rs = preparedStatement.executeQuery();
        	rs.next();
        	
        	return rs.getInt("Amount");  			        	
    	} 
		catch (Exception e) {
    		System.out.println("Error in RageDB.getRecentDonations(): " + e.getMessage());
		} finally {
			close();
		}
		
		return 0;
	}
	
	// Return the population of each faction
	public HashMap<Integer, Integer> getFactionPopulations()
	{
		HashMap<Integer, Integer> populations = new HashMap<Integer, Integer>();
		
		try
    	{
        	preparedStatement = conn.prepareStatement(
        		"SELECT p.ID_Faction, COUNT(*) as Population FROM Factions f " +
        		"INNER JOIN Players p ON p.ID_Faction = f.ID_Faction " +
        		"WHERE DATE_SUB(CURDATE(),INTERVAL 30 DAY) <= p.LastLogin " +
        		"GROUP BY f.ID_Faction");
        		
        	rs = preparedStatement.executeQuery();
        	while ( rs.next() ) 
        	{
        		populations.put(rs.getInt("ID_Faction"), rs.getInt("Population"));       		
        	}
        	
        	return populations;
    	} 
		catch (Exception e) {
    		System.out.println("Error in RageDB.getRecentDonations(): " + e.getMessage());
		} finally {
			close();
		}
		
		return null;
	}


        
}
