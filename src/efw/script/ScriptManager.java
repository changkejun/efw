package efw.script;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;

/**
 * サーバーサイトJavaScriptの管理と実行を行うクラス。
 * @author Chang Kejun
 *
 */
public final class ScriptManager {
	/**
	 * スクリプトエンジン管理オブジェクト。
	 */
	private static ScriptEngineManager scriptEngineManager;
	/**
	 * サーバー部品JavaScriptファイルの格納パス。
	 * サーブレットから渡される。
	 */
    private static String serverFolder;
	/**
	 * イベントJavaScriptファイルの格納パス。
	 * サーブレットから渡される。
	 */
    private static String eventFolder;
    /**
     * デバッグモードを制御するフラグ。
	 * サーブレットから渡される。
     */
    private static boolean isDebug;
    /**
     * サーバーサイトJavaScriptファイルの文字セット定数。
     * 「UTF-8」に固定。
     */
    private static final String SCRIPT_CHAR_SET="UTF-8";
    /**
     * スクリプトエンジンに渡すリクエストオブジェクトのキー。
     * 「_request」に固定。
     */
    private static final String KEY_REQUEST="_request";
    /**
     * スクリプトエンジンに渡すサーバー部品JavaScriptファイルの格納パスのキー。
     * 「_eventfolder」に固定。
     */
    private static final String KEY_SERVERFOLDER="_serverfolder";
    /**
     * スクリプトエンジンに渡すイベントJavaScriptファイルの格納パスのキー。
     * 「_eventfolder」に固定。
     */
    private static final String KEY_EVENTFOLDER="_eventfolder";
    /**
     * スクリプトエンジンに渡すデバッグモード制御フラグのキー。
     * 「_isdebug」に固定。
     */
    private static final String KEY_ISDEBUG="_isdebug";
    /**
     * スクリプトエンジンに渡すスクリプトエンジン名称のキー。
     * 「_enginename」に固定。
     */
    private static final String KEY_ENGINENAME="_enginename";
    /**
     * スレッドにスクリプトエンジンを付けるため、ThreadLocalを定義する。
     */
    private static ThreadLocal<ScriptEngine> localScriptEngine=new ThreadLocal<ScriptEngine>();
    
	/**
	 * サーブレットから設定情報を受け取り、スクリプトエンジン管理オブジェクトを初期化する。
	 * @param serverFolder サーバー部品JavaScriptファイルの格納パス。
	 * @param eventFolder イベントJavaScriptファイルの格納パス。
	 * @param isDebug　デバッグモード制御フラグ。
	 */
	public static synchronized void init(String serverFolder,String eventFolder,boolean isDebug){
		ScriptManager.scriptEngineManager= new ScriptEngineManager();
		ScriptManager.serverFolder=serverFolder;
		ScriptManager.eventFolder=eventFolder;
		ScriptManager.isDebug=isDebug;
	}
	/**
	 * リクエストをサーバーサイトJavaScriptに転送する。
	 * もしスレッドにスクリプトエンジンが付けられていないなら、スクリプトエンジンを作成し、共通とするefw.server.jsを実行する。
	 * @param request JQueryがefwサーブレット へ要求したJSON内容を含む HttpServletRequest オブジェクト。
	 * @return 実行結果のJSON文字列を返す。
	 * @throws ScriptException スクリプトエラー。
	 * @throws IOException ファイル操作エラー。
	 */
	public static String doPost(HttpServletRequest request) throws ScriptException, IOException {
		ScriptEngine se=localScriptEngine.get();
		if (se==null){
			//if to create new engine, eval the commonscript to init the common functions			
			se=scriptEngineManager.getEngineByName("JavaScript");//
			localScriptEngine.set(se);
			se.put(KEY_SERVERFOLDER, serverFolder);
			se.put(KEY_EVENTFOLDER, eventFolder);
			se.put(KEY_ISDEBUG, isDebug);
			se.put(KEY_ENGINENAME, se.getFactory().getEngineName());
			load(serverFolder+"/efw.server.js");
		}
		se.put(KEY_REQUEST, request);

		BufferedReader rd=new BufferedReader(new InputStreamReader(request.getInputStream()));
		String requestString = new String(rd.readLine().getBytes(),SCRIPT_CHAR_SET);
		rd.close();
		
		return (String)se.eval(String.format("doPost(%s)",requestString));
	}

	/**
	 * 指定ファイル名のサーバーサイトJavaScriptファイルをロードする。
	 * JDK1.6 1.7のMozilla Rhinoエンジンに「load」関数を実装するため。
	 * @param fileName　サーバーサイトJavaScriptファイルの名称。
	 * @throws ScriptException スクリプトエラー。
	 * @throws IOException ファイル操作エラー。
	 */
	public static void load(String fileName) throws ScriptException, IOException {
		ScriptEngine se=localScriptEngine.get();
		BufferedReader rd=new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName),SCRIPT_CHAR_SET));
		se.eval(rd);
		rd.close();
	}
	  
}
