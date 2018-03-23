package com.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class DBQueryUtil{

	private static final Logger log = Logger.getLogger(DBQueryUtil.class);

	private static Connection con;
	private Statement statement;

	public DBQueryUtil(String url, String user, String pass){
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");

			con = DriverManager.getConnection(url, user, pass);
			statement = con.createStatement();
			log.info("Connection Established");
		} catch (ClassNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (SQLException e){
			log.error(e.getMessage(), e);
			try{
				con.close();
				log.info("Connection Closed");
			}catch (SQLException sqle){
				log.error(sqle.getMessage(), sqle);
			}
		}
	}

	public ResultSet query(String query) throws SQLException{
		return statement.executeQuery(query);
	}

	public void closeConnection(){
		try {
			con.close();
			log.info("Connection Closed");
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
	}
}