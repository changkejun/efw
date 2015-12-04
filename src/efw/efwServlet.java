package efw;

import java.io.File;
import java.io.IOException;

import javax.script.ScriptException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import efw.db.DatabaseManager;
import efw.format.FormatManager;
import efw.log.LogManager;
import efw.properties.PropertiesManager;
import efw.script.ScriptManager;
import efw.sql.SqlManager;

/**
 * サーブレットアノテーション設定で、起動と同時にフレームワークの初期化を行う。
 * JQueryからのAjax通信をサーバーサイトJavaScriptへ転送する。
 * @author Chang Kejun
 */
@SuppressWarnings("serial")
@WebServlet(name="efwServlet",loadOnStartup=1,urlPatterns={"/efwServlet"})
public final class efwServlet extends HttpServlet {
	/**
	 * 初期化成功か否かを表すフラグ。
	 */
	private static boolean initSuccessFlag=false;
	/**
	 * サーバー部品JavaScriptファイルの格納パス、
	 * Webアプリケーションコンテキストからの相対パスで表す。
	 * <br>efw.propertiesのefw.server.folderで設定する。
     * デフォルトは「/WEB-INF/efw/server」。
	 */
    private static String serverFolder="/WEB-INF/efw/server";
	/**
	 * イベントJavaScriptファイルの格納パス、
	 * Webアプリケーションコンテキストからの相対パスで表す。
	 * <br>efw.propertiesのefw.event.folderで設定する。
     * デフォルトは「/WEB-INF/efw/event」。
	 */
    private static String eventFolder="/WEB-INF/efw/event";
    /**
     * Sql外部化XMLファイルの格納パス、
     * Webアプリケーションコンテキストからの相対パスで表す。
     * <br>efw.propertiesのefw.sql.folderで設定、
     * デフォルトは「/WEB-INF/efw/sql」。
     */
    private static String sqlFolder="/WEB-INF/efw/sql";
    /**
     * デバッグモードを制御するフラグ。
     * <br>efw.propertiesのefw.isdebugで設定、
     * デフォルトはfalse。
     * <br>trueの場合、SqlとScriptの変更はリアルに反映する。falseの場合、SqlとScriptの変更は再起動するまで反映しない。
     */
    private static boolean isDebug=false;
    /**
     * レスポンスの文字セット定数、XMLHttpRequestのデフォルトに合わせ、「UTF-8」に固定。
     */
    private static final String RESPONSE_CHAR_SET="UTF-8";
    /**
     * サーブレットの起動と同時に、
     * LogManager、SqlManager、ScriptManagerの初期化を行う。
     * <br>初期化成功の場合、initSuccessFlagをtrueに設定する。失敗の場合、false。
     */
    public void init() throws ServletException {
    	try {
        	//call the orgin init function
        	super.init();
        	// begin to init efw
        	PropertiesManager.init();
            LogManager.init();
            LogManager.InitCommonDebug("PropertiesManager.init");
            LogManager.InitCommonDebug("LogManager.init");
            //get attrs from properties or context
            isDebug=PropertiesManager.getBooleanProperty(PropertiesManager.EFW_ISDEBUG,isDebug);
        	LogManager.InitCommonDebug("isDebug = " + isDebug);
        	serverFolder=this.getServletContext().getRealPath(PropertiesManager.getProperty(PropertiesManager.EFW_SEVER_FOLDER,serverFolder));
        	LogManager.InitCommonDebug("serverFolder = " + serverFolder);
        	eventFolder=this.getServletContext().getRealPath(PropertiesManager.getProperty(PropertiesManager.EFW_EVENT_FOLDER,eventFolder));
        	LogManager.InitCommonDebug("eventFolder = " + eventFolder);
        	sqlFolder=this.getServletContext().getRealPath(PropertiesManager.getProperty(PropertiesManager.EFW_EVENT_SQL,sqlFolder));
        	LogManager.InitCommonDebug("sqlFolder = " + sqlFolder);
        	//check the define folders
        	if (!new File(serverFolder).exists())throw new efwException(efwException.ServerFolderDoesNotExistException,serverFolder);
        	if (!new File(eventFolder).exists())throw new efwException(efwException.EventFolderDoesNotExistException,eventFolder);
        	if (!new File(sqlFolder).exists())throw new efwException(efwException.SqlFolderIsNotExistsException,sqlFolder);
        	//load definition from folders
    		SqlManager.init(sqlFolder,isDebug);
    		LogManager.InitCommonDebug("SqlsManager.init");
			DatabaseManager.init();
    		LogManager.InitCommonDebug("DatabaseManager.init");
            ScriptManager.init(serverFolder,eventFolder,isDebug);
    		LogManager.InitCommonDebug("ScriptManager.init");
    		FormatManager.init();
    		LogManager.InitCommonDebug("FormatManager.init");
    		//init is success
    		initSuccessFlag=true;
            LogManager.InitCommonDebug("efwServlet.init");
    	} catch (IOException e) {
    		//if it is io, the error is from PropertiesManager or LogManager,
    		//so you can not use LogManager to log it.
    		e.printStackTrace();
		} catch (efwException e) {
			LogManager.InitErrorDebug(e.getMessage());
		}
    }

	/**
	 * JQueryからのAjax通信をサーバーサイトJavaScriptへ転送し、その実行結果をレスポンスする。
	 * <br>efwサーブレット が初期化失敗の場合、SystemInitFailedExceptionのエラー情報をレスポンスする。
	 * <br>サーバーサイトJavaScriptが実行エラーの場合、ScriptEvalFailedExceptionのエラー情報をレスポンスする。
	 * @param request JQueryがefwサーブレット へ要求したJSON内容を含む HttpServletRequest オブジェクト。
	 * @param response efwサーブレットがJQueryに返すJSON内容を含む HttpServletResponse オブジェクト 。
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
        response.setCharacterEncoding(RESPONSE_CHAR_SET);
        String errorFormart="{\"error\":{\"errorType\":\"%s\",\"canContinue\":false}}";
		//--------------------------------------------------------------------
        //if init is failed, return the info instead of throw exception
		if (!efwServlet.initSuccessFlag){
			response.getWriter().print(String.format(errorFormart, efwException.SystemInitFailedException));
			return;
		}

		try {
			response.getWriter().print(ScriptManager.doPost(request));
			LogManager.CommDebug("efwServlet.doPost");
		} catch (ScriptException e) {
			LogManager.ErrorDebug("efwServlet.doPost");
			e.printStackTrace();
			response.getWriter().print(String.format(errorFormart, efwException.ScriptEvalFailedException));
		}
	}
}
