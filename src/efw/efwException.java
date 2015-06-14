package efw;

import javax.servlet.ServletException;

/**
 * フレームワークの初期化と実行時発生する例外。
 * @author Chang Kejun
 */
@SuppressWarnings("serial")
public final class efwException extends ServletException {
	/**
	 * サーバー部品JavaScriptファイルの格納パスが存在しないエラー定数。
	 */
	public static final String ServerFolderDoesNotExistException="ServerFolderDoesNotExistException";
	/**
	 * イベントJavaScriptファイルの格納パスが存在しないエラー定数。
	 */
	public static final String EventFolderDoesNotExistException="EventFolderDoesNotExistException";
	/**
	 * Sql外部化XMLファイルの格納パスが存在しないエラー定数。
	 */
	public static final String SqlFolderIsNotExistsException="SqlFolderIsNotExistsException";
	/**
	 * データソースが取得できないエラー定数。
	 */
	public static final String DataSourceInitFailedException="DataSourceInitFailedException";
	/**
	 * システム初期化エラー定数。
	 */
	public static final String SystemInitFailedException="SystemInitFailedException";
	/**
	 * サーバーサイトJavaScript実行エラー定数。
	 */
	public static final String ScriptEvalFailedException="ScriptEvalFailedException";

	/**
	 * 指定IdのSqlを外部化XMLファイルが存在しないエラー定数。
	 */
	public static final String SqlGroupIdIsNotExistsException="SqlGroupIdIsNotExistsException";
	/**
	 * 指定IdのSqlが指定XMLファイルに存在しないエラー定数。
	 */
	public static final String SqlIdIsNotExistsException="SqlIdIsNotExistsException";
	/**
	 * 指定IdのSqlは指定XMLファイルに複数存在しているエラー定数。
	 */
	public static final String SqlIdIsDuplicateException="SqlIdIsDuplicateException";
	
	/**
	 * XMLファイルが正しくないエラー定数。
	 */
	public static final String XMLFileIsNotLegalException="XMLFileIsNotLegalException";
	/**
	 * XMLタグが正しくないエラー定数。
	 */
	public static final String XMLTagIsNotLegalException="XMLTagIsNotLegalException";
	/**
	 * efw例外コンストラクタ。
	 * @param msg メッセージ。
	 */
	public efwException(String msg){super(msg);}
	/**
	 * efw例外コンストラクタ。
	 * @param msg メッセージ。
	 * @param information 補足情報。
	 */
	public efwException(String msg,String information){super(msg +" "+information);}

}
