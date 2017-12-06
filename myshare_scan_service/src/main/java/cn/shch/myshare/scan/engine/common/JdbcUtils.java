package cn.shch.myshare.scan.engine.common;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class JdbcUtils { 
	private static Logger logger = Logger.getLogger(JdbcUtils.class);
	public static final Properties DBPROP = new Properties();

	static {
		InputStream is = JdbcUtils.class.getClassLoader().getResourceAsStream("db.properties");
		try {
			DBPROP.load(is);
			Class.forName(DBPROP.getProperty("driver"));
		} catch (Exception e) {
			logger.error(LoggerUtils.buildDebugMessage(e.getMessage()));
		}
	}
	public static Connection getConnection(){
		Connection con=null;
		try {
			con=DriverManager.getConnection(DBPROP.getProperty("url"), DBPROP.getProperty("user"), DBPROP.getProperty("pwd"));
			return con;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(LoggerUtils.buildDebugMessage(e.getMessage()));
		}
		return con;
	}
	public static void close(ResultSet rs, PreparedStatement pstmt, Connection con) {
		if(rs!=null){
			try {
				rs.close();
			} catch (SQLException e) {
				logger.error(LoggerUtils.buildDebugMessage(e.getMessage()));
			}finally{
				if(pstmt!=null){
					try {
						pstmt.close();
					} catch (SQLException e) {
						logger.error(LoggerUtils.buildDebugMessage(e.getMessage()));
					}finally{
						if(con!=null){
							try {
								con.close();
							} catch (SQLException e) {
								logger.error(LoggerUtils.buildDebugMessage(e.getMessage()));
							}
						}
					}
				}
			}
		}
		
	}
}
