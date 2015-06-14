package efw.db;

import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import efw.efwException;
import efw.log.LogManager;
import efw.properties.PropertiesManager;

/**
 * データベース接続を管理するクラス。
 * @author Chang Kejun
 *
 */
public final class DatabaseManager {
	/**
	 * ネーミング操作の開始コンテキストの名称。
	 * 「java:comp/env」に固定。
	 */
	private static final String JAVA_INITCONTEXT_NAME="java:comp/env";
	/**
	 * フレームワークに利用するjdbcリソースの名称。
	 * <br>efw.propertiesのefw.jdbc.resourceで設定、
	 * デフォルトは「jdbc/efw」。
	 */
	private static String jdbcResourceName="jdbc/efw";
	/**
	 * フレームワークに利用するデータソース。
	 */
	private static DataSource dataSource;
	/**
	 * フレームワークに利用するデータソースを初期化する。
	 * @throws efwException データソース初期化失敗のエラー。
	 */
	public static synchronized void init() throws efwException{
		try {
	        Context envContext = (Context) new InitialContext().lookup(JAVA_INITCONTEXT_NAME);
	        jdbcResourceName=PropertiesManager.getProperty(PropertiesManager.EFW_JDBC_RESOURCE,jdbcResourceName);
	        dataSource=(DataSource) envContext.lookup(jdbcResourceName);
		} catch (NamingException e) {
			e.printStackTrace();
    		throw new efwException(efwException.DataSourceInitFailedException,jdbcResourceName);
		}
	}
    ///////////////////////////////////////////////////////////////////////////
	/**
	 * フレームワーク用データソースからデータベース接続を取得する。
	 * @return データベース接続を戻す。
	 * @throws SQLException データベースアクセスエラー。
	 */
    public static Database open() throws SQLException{
        Database database = null; 
    	try{
            database = new Database(dataSource.getConnection());
            LogManager.CommDebug("DatabaseManager.open");
            return database;
    	}catch(SQLException e){
    		LogManager.ErrorDebug("DatabaseManager.open");
    		e.printStackTrace();
    		throw e;
    	}
    }
    /**
     * jdbcリソース名称によりデータベース接続を取得する。
     * @param jdbcResourceName jdbcリソース名称
     * @return　データベース接続を戻す。
     * @throws NamingException　名称不正のエラー。　
     * @throws SQLException　データベースアクセスエラー。
     */
    public static Database open(String jdbcResourceName) throws NamingException, SQLException{
        Database database = null; 
		try {
	        Context initContext = new InitialContext();
	        Context envContext = (Context) initContext.lookup(JAVA_INITCONTEXT_NAME);
	        DataSource ds = (DataSource) envContext.lookup(jdbcResourceName);
	        database = new Database(ds.getConnection());
            LogManager.CommDebug("DatabaseManager.open",jdbcResourceName);
	        return database;
		} catch (NamingException e) {
            LogManager.ErrorDebug("DatabaseManager.open",jdbcResourceName);
			e.printStackTrace();
			throw e;
		} catch (SQLException e) {
            LogManager.ErrorDebug("DatabaseManager.open",jdbcResourceName);
			e.printStackTrace();
			throw e;
		}
    }

}
