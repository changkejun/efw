package efw;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import efw.log.LogManager;
import efw.properties.PropertiesManager;
import efw.sql.SqlManager;

/**
 * imart7.xのWeb.xmlの設定で、起動と同時にフレームワークの初期化のみを行う。
 * サーブレットアノテーションは、imart7.xに利用できないから、web.xmlの設定が必要。
 * Ajax通信は、imartのjsspRpc方式を利用するから、サーブレットは送受信をしない。
 * @author Chang Kejun
 */
@SuppressWarnings("serial")
public final class efwServletForImart7 extends HttpServlet {
    /**
     * Sqlを外部化するXMLファイルの格納パス、
     * Webアプリケーションコンテキストからの相対パスで表す。
     * <br>efw.propertiesのefw.sql.folderで設定、
     * デフォルトは「/WEB-INF/efw/sql」。
     */
    private static String sqlFolder="/WEB-INF/efw/sql";
    /**
     * デバッグモードを制御するフラグ。
     * <br>efw.propertiesのefw.isdebugで設定、
     * デフォルトはfalse。
     * <br>trueの場合、Sqlの変更はリアルに反映する。falseの場合、Sqlの変更は再起動するまで反映しない。
     */
    private static boolean isDebug=false;
    /**
     * サーブレットの起動と同時に、
     * LogManager、SqlManagerの初期化を行う。
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
        	sqlFolder=this.getServletContext().getRealPath(PropertiesManager.getProperty(PropertiesManager.EFW_EVENT_SQL,sqlFolder));
        	LogManager.InitCommonDebug("sqlFolder = " + sqlFolder);
        	//check the define folders
        	if (!new File(sqlFolder).exists())throw new efwException(efwException.SqlFolderIsNotExistsException,sqlFolder);
        	//load definition from folders
    		SqlManager.init(sqlFolder,isDebug);
    		LogManager.InitCommonDebug("SqlsManager.init");
            LogManager.InitCommonDebug("efwServletForImart7.init");
    	} catch (IOException e) {
    		//if it is io, the error is from PropertiesManager or LogManager,
    		//so you can not use LogManager to log it.
    		e.printStackTrace();
		} catch (efwException e) {
			LogManager.InitErrorDebug(e.getMessage());
		}
    }
}
