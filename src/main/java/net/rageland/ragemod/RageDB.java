package net.rageland.ragemod;

// TODO: Refactor into connection pooling

// TODO: Prevent null pointer exception when database not found (!)

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import net.rageland.ragemod.data.Location2D;
import net.rageland.ragemod.data.Lot;
import net.rageland.ragemod.data.PlayerData;
import net.rageland.ragemod.data.PlayerTown;
import net.rageland.ragemod.data.PlayerTowns;
import net.rageland.ragemod.data.Players;
import net.rageland.ragemod.data.Region2D;

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
    	
    	url = plugin.Config.DB_URL;
    	databaseName = plugin.Config.DB_DatabaseName;
    	driver = plugin.Config.DB_Driver;
    	user = plugin.Config.DB_User;
    	password = plugin.Config.DB_Password;
    	
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
        
    	
        
        // Tells the database when a player logs into the server
        public void player_Login(String name)
        {
        	try
        	{
        		// Apparently prepareCall() is expensive - separate this out and
        		// reset the parameters
        		//CallableStatement cStmt = conn.prepareCall("{CALL rage_p_Login(?)}");

        		//cStmt.setString(1, name);
        		
        		//boolean hadResults = cStmt.execute();
        		
        		preparedStatement = conn.prepareStatement(
	    			"CALL rage_p_Login(?)");
        		preparedStatement.setString(1, name);
        		preparedStatement.executeUpdate();
        		
        	} catch (Exception e) {
        		System.out.println("Error in RageDB.player_Login: " + e.getMessage());
    		} finally {
    			close();
    		}        	
        }
        
        public String testName()
        {
        	try
        	{
        		preparedStatement = conn.prepareStatement(
        			"SELECT * FROM Players LIMIT 1");
	        	rs = preparedStatement.executeQuery();
	        	
	        	rs.next();
	        	return rs.getString("Name");
        	} catch (Exception e) {
        		System.out.println("Error in RageDB.testName: " + e.getMessage());
    		} finally {
    			close();
    		}
    		
    		return "";
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
    	public HashMap<String, PlayerTown> LoadPlayerTowns()
        {
    		HashMap<String, PlayerTown> towns = new HashMap<String, PlayerTown>();
    		PlayerTown currentTown = null;
    		
        	try
        	{
	        	preparedStatement = conn.prepareStatement(
    				"SELECT pt.ID_PlayerTown, pt.TownName, pt.XCoord, pt.ZCoord, " +
    				"	pt.ID_TownLevel as TownLevel, " +
    				"	IFNULL(f.Name, 'Neutral') as FactionName, pt.TreasuryBalance, pt.BankruptDate, p.Name AS Mayor " +
    				"FROM PlayerTowns pt " +
    				"LEFT JOIN Factions f ON pt.ID_Faction = f.ID_Faction " +
    				"INNER JOIN Players p ON pt.ID_PlayerTown = p.ID_PlayerTown " +
    				"WHERE p.IsMayor = 1 AND pt.IsDeleted = 0");
	        	
	        	rs = preparedStatement.executeQuery();
	        	
	        	while ( rs.next() ) 
	        	{	        		
	        		currentTown = new PlayerTown();
	        		currentTown.ID_PlayerTown = rs.getInt("ID_PlayerTown");
	        		currentTown.TownName = rs.getString("TownName");
	        		currentTown.Coords = new Location2D(rs.getInt("XCoord"), rs.getInt("ZCoord"));
	        		currentTown.Faction = rs.getString("FactionName");
	        		currentTown.TreasuryBalance = rs.getFloat("TreasuryBalance");
	        		currentTown.BankruptDate = rs.getDate("BankruptDate");
	        		currentTown.TownLevel = rs.getInt("TownLevel");
	        		currentTown.Mayor = rs.getString("Mayor");
	        			        		
	        		towns.put(rs.getString("TownName"), currentTown);	        		
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
    	public PlayerData PlayerLogin(String playerName)
        {
    		PlayerData playerData = null;
    		ResultSet rs = null;    		
    		
        	try
        	{
	        	String selectQuery = 
	        		"SELECT p.ID_Player, p.Name, IFNULL(f.Name, 'Neutral') as FactionName, p.IsMember, p.MemberExpiration, p.Bounty, p.ExtraBounty, " +
	        		"   (p.Home_XCoord IS NOT NULL) AS Home_IsSet, p.Home_XCoord, p.Home_YCoord, p.Home_ZCoord, p.Home_LastUsed, " +
	        		"	(p.Spawn_XCoord IS NOT NULL) AS Spawn_IsSet, p.Spawn_XCoord, p.Spawn_YCoord, p.Spawn_ZCoord, p.Spawn_LastUsed, " +
	        		"	IFNULL(pt.TownName, '') as TownName, p.IsMayor " +
	        		"FROM Players p " +
	        		"LEFT JOIN Factions f ON p.ID_Faction = f.ID_Faction " +
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
    	public PlayerData PlayerFetch(String playerName)
        {
    		PlayerData playerData = null;
    		ResultSet rs = null;    		
    		
        	try
        	{
	        	String selectQuery = 
	        		"SELECT p.ID_Player, p.Name, IFNULL(f.Name, 'Neutral') as FactionName, p.IsMember, p.MemberExpiration, p.Bounty, p.ExtraBounty, " +
	        		"   (p.Home_XCoord IS NOT NULL) AS Home_IsSet, p.Home_XCoord, p.Home_YCoord, p.Home_ZCoord, p.Home_LastUsed, " +
	        		"	(p.Spawn_XCoord IS NOT NULL) AS Spawn_IsSet, p.Spawn_XCoord, p.Spawn_YCoord, p.Spawn_ZCoord, p.Spawn_LastUsed, " +
	        		"	IFNULL(pt.TownName, '') as TownName, p.IsMayor " +
	        		"FROM Players p " +
	        		"LEFT JOIN Factions f ON p.ID_Faction = f.ID_Faction " +
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
    		
    		playerData.ID_Player = rs.getInt("ID_Player");
    		playerData.Name = rs.getString("Name");
    		playerData.Faction = rs.getString("FactionName");
    		playerData.IsMember = rs.getBoolean("IsMember");
    		playerData.MemberExpiration = rs.getDate("MemberExpiration");
    		playerData.Bounty = rs.getFloat("Bounty");
    		playerData.ExtraBounty = rs.getFloat("ExtraBounty");
    		playerData.TownName = rs.getString("TownName");
    		playerData.IsMayor = rs.getBoolean("IsMayor");
    		
    		playerData.Home_IsSet = rs.getBoolean("Home_IsSet");
    		playerData.Home_XCoord = rs.getInt("Home_XCoord");
    		playerData.Home_YCoord = rs.getInt("Home_YCoord");
    		playerData.Home_ZCoord = rs.getInt("Home_ZCoord");
    		playerData.Home_LastUsed = rs.getDate("Home_LastUsed");
    		
    		playerData.Spawn_IsSet = rs.getBoolean("Spawn_IsSet");
    		playerData.Spawn_XCoord = rs.getInt("Spawn_XCoord");
    		playerData.Spawn_YCoord = rs.getInt("Spawn_YCoord");
    		playerData.Spawn_ZCoord = rs.getInt("Spawn_ZCoord");
    		playerData.Spawn_LastUsed = rs.getDate("Spawn_LastUsed");
    		
        	return playerData;
    	}
    	
    	
    	// Load data from Players table on login if existing player - create new row if not 
    	public void TownAdd(String targetPlayerName, String townName)
        {
    		PlayerData playerData = Players.Get(targetPlayerName);
    		
        	try
        	{
        		// Update the Players table to create the town association
        		preparedStatement = conn.prepareStatement(
        				"UPDATE Players SET ID_PlayerTown = " +
        				"	(SELECT ID_PlayerTown FROM PlayerTowns WHERE TownName = '" + townName + "') " +
        				", IsMayor = 0 WHERE ID_Player = " + playerData.ID_Player);
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
    	public int TownCreate(Player player, String townName)
        {
    		ResultSet rs = null;   
    		PlayerData playerData = Players.Get(player.getName());
    		
        	try
        	{
        		// TODO: Set default treasury balance from config
        		// Insert the new town into the PlayerTowns table
        		preparedStatement = conn.prepareStatement(
        				"INSERT INTO PlayerTowns (TownName, XCoord, ZCoord, ID_Faction, TreasuryBalance, TownLevel) " +
        				"VALUES ('" + townName + "', " + (int)player.getLocation().getX() + ", " + (int)player.getLocation().getZ() + ", " +  
        				"(SELECT ID_Faction FROM Players WHERE ID_Player = " + playerData.ID_Player + "), " + 
        				RageConfig.TownLevels.get(1).MinimumBalance + ", 1)",
        				Statement.RETURN_GENERATED_KEYS);        		
        		preparedStatement.executeUpdate();
        		
        		// Retrieve the new auto-increment town ID 
        		rs = preparedStatement.getGeneratedKeys();
        		rs.next();
        		int townID = rs.getInt(1);
        		
        		// Update the Players table
        		preparedStatement = conn.prepareStatement(
        				"UPDATE Players SET ID_PlayerTown = " + townID + ", IsMayor = 1 " +
        				"WHERE ID_Player = " + playerData.ID_Player);        		
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
    	public void TownLeave(String playerName)
        { 
    		PlayerData playerData = Players.Get(playerName);
    		
        	try
        	{
        		// Update the Players table to remove the town association
        		preparedStatement = conn.prepareStatement(
        				"UPDATE Players SET ID_PlayerTown = NULL, IsMayor = 0 WHERE ID_Player = " + playerData.ID_Player);
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
    	public void TownUpgrade(String townName) 
    	{    		
        	try
        	{
        		// Update the Players table to remove the town association
        		preparedStatement = conn.prepareStatement(
        				"UPDATE PlayerTowns SET TownLevel = (TownLevel + 1) WHERE TownName = " + townName);
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
    	public int CountResidents(String townName) 
    	{
			ResultSet rs = null;   
    		PlayerTown playerTown = PlayerTowns.Get(townName);
    		
        	try
        	{
        		preparedStatement = conn.prepareStatement(
        				"SELECT COUNT ID_Player FROM Players WHERE ID_PlayerTown = " + playerTown.ID_PlayerTown);
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
    	public ArrayList<String> ListTownResidents(String townName) 
    	{
    		ResultSet rs = null;   
    		PlayerTown playerTown = PlayerTowns.Get(townName);
    		ArrayList<String> residents = new ArrayList<String>();
    		
        	try
        	{
        		preparedStatement = conn.prepareStatement(
        				"SELECT Name FROM Players p " +
        				"WHERE ID_PlayerTown = " + playerTown.ID_PlayerTown + " " +
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
    	
    	// Loads all of the lots in the city into memory
		public HashMap<String, Lot> LoadLots() 
		{
	
			HashMap<String, Lot> lots = new HashMap<String, Lot>();
			Lot currentLot = null;
			int mult = RageConfig.Lot_Multiplier;
			
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
	        		currentLot.region = new Region2D(
	        				(rs.getInt("XCoord") * mult) + RageConfig.Lot_XOffset,
	        				(rs.getInt("ZCoord") * mult) + RageConfig.Lot_ZOffset,
	        				(rs.getInt("XCoord") * mult) + RageConfig.Lot_XOffset + (rs.getInt("Length") * mult),
	        				(rs.getInt("ZCoord") * mult) + RageConfig.Lot_ZOffset + (rs.getInt("Width") * mult));
	    	        
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
		public void LotClaim(PlayerData playerData, Lot lot) 
		{
			try
        	{
        		// Update the Lots table to assign the owner
        		preparedStatement = conn.prepareStatement(
        				"UPDATE Lots SET ID_Player = " + playerData.ID_Player + " WHERE ID_Lot = " + lot.id_Lot);
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
		public void LotUnclaim(Lot lot) 
		{
			try
        	{
        		// Update the Lots table to assign the owner
        		preparedStatement = conn.prepareStatement(
        				"UPDATE Lots SET ID_Player = NULL WHERE ID_Lot = " + lot.id_Lot);
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


        
}
