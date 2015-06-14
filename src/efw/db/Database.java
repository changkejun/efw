package efw.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import efw.efwException;
import efw.log.LogManager;
import efw.sql.Sql;
import efw.sql.SqlManager;

/**
 * データーベースに対する操作を行うクラス。
 * @author Chang Kejun
 */
public final class Database {
	/**
	 * データベースの接続。
	 */
	private Connection mConn;
	/**
	 * SQL実行するステートメントを格納する配列。
	 */
	private ArrayList<CallableStatement> mStmtAry;
	/**
	 * DatabaseManagerのopenにより初期化される。直接新規作成をしない。
	 * @param mConn データベース接続。
	 * @throws SQLException データベースアクセスエラー。
	 */
    protected Database(Connection mConn) throws SQLException {
    	this.mConn=mConn;
    	this.mConn.setAutoCommit(false);
    	mStmtAry=new ArrayList<CallableStatement>();
    }
    /**
     * 単一の ResultSetオブジェクトを返すSqlを実行する。
     * 
     * @param groupId　Sqlを外部化するXMLのファイル名（拡張子を除く）。
     * @param sqlId SqlXMLファイルのsqlタグに定義されるId。
     * @param params　Sqlに利用される引数を格納するマップ。
     * @return　指定されたクエリーによって作成されたデータを含む ResultSet オブジェクト。null にはならない。
     * @throws efwException フレームワークの実行時エラー。
     * @throws SQLException データベースアクセスエラー。
     */
    public ResultSet executeQuery(String groupId,String sqlId,Map<String,Object> params) throws efwException, SQLException{
    	try{
    		Sql sql=SqlManager.get(groupId, sqlId);
    		String sqlString=sql.getSqlString(params);
    		ArrayList<Object> sqlParams=sql.getSqlParams(params);
    		
    	    LogManager.CommDebug("sql =" , sqlString);
    	    CallableStatement mStmt = mConn.prepareCall(sqlString);
    	    mStmtAry.add(mStmt);
    	    setSQLParams(mStmt, sqlParams);
    	    ResultSet rs = mStmt.executeQuery();
    	    LogManager.CommDebug("Database.executeQuery");
    	    return rs;
    	}catch(efwException e){
    		e.printStackTrace();
    	    LogManager.ErrorDebug("Database.executeQuery");
    		throw e;
    	}catch(SQLException e){
    		e.printStackTrace();
    	    LogManager.ErrorDebug("Database.executeQuery");
    		throw e;
    	}
	}
    /**
     * INSERT文、UPDATE文、DELETE文を実行する。
     * @param groupId　Sqlを外部化するXMLのファイル名（拡張子を除く）。
     * @param sqlId SqlXMLファイルのsqlタグに定義されるId。
     * @param params　Sqlに利用される引数を格納するマップ。
     * @return　実行された行数を戻す。 
     * @throws efwException フレームワークの実行時エラー。
     * @throws SQLException データベースアクセスエラー。
     */
    public int executeUpdate(String groupId,String sqlId,Map<String,Object> params) throws efwException, SQLException{
    	try{
        	Sql sql=SqlManager.get(groupId, sqlId);
        	String sqlString=sql.getSqlString(params);
        	ArrayList<Object> sqlParams=sql.getSqlParams(params);
        	
            LogManager.CommDebug("sql =" , sqlString);
            CallableStatement mStmt = mConn.prepareCall(sqlString);
            mStmtAry.add(mStmt);
            setSQLParams(mStmt, sqlParams);
            int cnt = mStmt.executeUpdate();
            LogManager.CommDebug("Database.executeUpdate");
            
            return cnt;
    	}catch(efwException e){
    		e.printStackTrace();
    	    LogManager.ErrorDebug("Database.executeUpdate");
    		throw e;
    	}catch(SQLException e){
    		e.printStackTrace();
    	    LogManager.ErrorDebug("Database.executeUpdate");
    		throw e;
    	}
    }
    /**
     * 任意のSQL文を実行する。
     * @param groupId　Sqlを外部化するXMLのファイル名（拡張子を除く）。
     * @param sqlId SqlXMLファイルのsqlタグに定義されるId。
     * @param params　Sqlに利用される引数を格納するマップ。
     * @throws efwException フレームワークの実行時エラー。
     * @throws SQLException データベースアクセスエラー。
     */
    public void execute(String groupId,String sqlId,Map<String,Object> params) throws efwException, SQLException{
    	try{
        	Sql sql=SqlManager.get(groupId, sqlId);
        	String sqlString=sql.getSqlString(params);
        	ArrayList<Object> sqlParams=sql.getSqlParams(params);
            LogManager.CommDebug("sql =" , sqlString);
            CallableStatement mStmt = mConn.prepareCall(sqlString);
            mStmtAry.add(mStmt);
            setSQLParams(mStmt, sqlParams);
            mStmt.execute();
            LogManager.CommDebug("Database.execute");
    	}catch(efwException e){
    		e.printStackTrace();
    	    LogManager.ErrorDebug("Database.execute");
    		throw e;
    	}catch(SQLException e){
    		e.printStackTrace();
    	    LogManager.ErrorDebug("Database.execute");
    		throw e;
    	}
    }
    /**
     * データベースへの更新を無効とし、 データベース接続が保持するデータベースロックをすべて解除する。
     */
    public void rollback(){
    	try{
            if (null != mConn) {
                if (!mConn.isClosed()) {
                    mConn.rollback();
                }
            }
    	}catch(Exception e){e.printStackTrace();}
    }
    /**
     * データベースへの更新を有効とし、 データベース接続が保持するデータベースロックをすべて解除する。
     */
    public void commit(){
    	try{
            if (null != mConn) {
                if (!mConn.isClosed()) {
                    mConn.commit();
                }
            }
    	}catch(Exception e){e.printStackTrace();}
    }
    /**
     * ステートメントを全部閉じて、データベース接続をコミットして、閉じる。
     */
    public void close(){
    	try{
            if (!mConn.isClosed()) {
                if (null != mStmtAry) {
                    CallableStatement mStmt = null;
                    for (int i=0; i<mStmtAry.size(); i++) {
                        mStmt = mStmtAry.get(i);
                        mStmt.close();
                    }
                    while (mStmtAry.size() > 0) {
                        mStmtAry.remove(0);
                    }
                    mConn.commit();
                }
                mConn.close();
            }
    	}catch(Exception e){e.printStackTrace();}
    }
    /**
     * Sqlパラメータを配列から設定する。
     * @param mStmt ステートメント。
     * @param prms 配列に格納するパラメータ。
     * @throws SQLException データベースアクセスエラー。
     */
    private void setSQLParams(CallableStatement mStmt, ArrayList<Object> prms) throws SQLException {
    	try{
            if (null != prms) {
            	StringBuffer bf=new StringBuffer();
                for (int i=0; i<prms.size(); i++) {
                	if (i>0)bf.append(",");
                	bf.append(prms.get(i));
                    mStmt.setObject(i+1, prms.get(i));
                }
                LogManager.CommDebug("params = " , bf.toString());
            }
    	}catch(SQLException e){
    		e.printStackTrace();
    		throw e;
    	}
    }
    
}
