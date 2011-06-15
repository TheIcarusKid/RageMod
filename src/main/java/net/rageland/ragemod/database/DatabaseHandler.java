package net.rageland.ragemod.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class DatabaseHandler {

	private Connection connection = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	private String host;
	private int port;
	private String database;
	private String username;
	private String password;

	public DatabaseHandler(String host, int port, String database, String username,
			String password) {
		this.host = host;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;

		// Setup MySQL tables
		connect();
		setup();
	}

	public void connect() {
		try {
			// This will load the MySQL driver
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			;
			// Setup the connection with the DB
			connection = DriverManager.getConnection("jdbc:mysql://" + host
					+ ":" + port + "/" + database + "?" + "user=" + username
					+ "&password=" + password);

			// Statements allow to issue SQL queries to the database
			statement = connection.createStatement();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	// Setup up the tables, if they don't exist already
	public void setup() {
		try {
			if (connection.isClosed())
				connect();

			statement.executeUpdate(
				"CREATE TABLE IF NOT EXISTS PlayerTowns ("
				+ "ID_PlayerTown INT IDENTITY AUTO_INCREMENT,"
				+ "PRIMARY KEY(ID_PlayerTown),"
				+ "TownName CHAR(40), XCoord INT, ZCoord INT, RangeLimit INT)");
			statement.executeUpdate("INSERT IGNORE INTO PlayerTowns (TownName, XCoord, ZCoord, RangeLimit)" +
					" VALUES (oldtown, 0, 0, 1000)");

		} catch (Exception e) {
			
		}
	}
	
	public ResultSet getCurrentTowns() {
		resultSet = null;		
		try {
			if(connection == null || connection.isClosed()) {
				connect();
			}			
			resultSet = statement.executeQuery("SELECT TownName, ZCoord, XCoord, RangeLimit FROM PlayerTowns");
						
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultSet;		
	}	
	
	public void addTown(String townName, int xCoord, int zCoord, int rangeLimit) {
		try {
			if(connection.isClosed()) {
				connect();
			}
			statement.executeUpdate("INSERT IGNORE INTO PlayerTowns (TownName, XCoord, ZCoord, RangeLimit) " +
					"VALUES (" + townName + ", " + xCoord + ", " + zCoord + ", " + rangeLimit + ")");
			
		} catch(SQLException e) {
			
		}
	}
}
