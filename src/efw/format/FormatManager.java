package efw.format;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * 数字と日付のフォーマットを管理するクラス。
 * @author Chang Kejun
 *
 */
public final class FormatManager {
	/**
	 * ロケール情報（西暦用）。
	 * 初期化時、システムデフォルトロケールを設定する。
	 */
	private static Locale locale;
	/**
	 * ロケール情報（和暦用）。
	 * 初期化時、ja_JP_JPで作成する。
	 */
	private static Locale localeJ;

    /**
     * 数字フォーマット配列。
     * 作成済みフォーマットを格納する。
     */
    private static HashMap<String,DecimalFormat> numberFormats=new HashMap<String,DecimalFormat>();
    /**
     * 日付フォーマット配列。
     * 作成済みフォーマットを格納する。
     */
    private static HashMap<String,DateFormat> dateFormats=new HashMap<String,DateFormat>();
    
    /**
     * フォーマット管理を初期化する。
     */
    public static synchronized void init(){
    	FormatManager.locale = Locale.getDefault();
    	FormatManager.localeJ = new Locale("ja","JP","JP");
	}
    /**
     * 数字を指定フォーマットで文字列に変換する。
     * @param value　数字データ。
     * @param format 指定フォーマット。java.text.DecimalFormatを参照。
     * @return 変換後の文字列。
     */
    public static String formatNumber(Object value,String format){
    	DecimalFormat df;
    	synchronized(numberFormats){
	    	df=numberFormats.get(format);
	    	if(df == null){
	        	df=new DecimalFormat(format);
	        	numberFormats.put(format, df);
	    	}
    	}
		return df.format(value);    	
    }
    /**
     * フォーマットされた文字列をフォーマット前の数字に戻す。
     * @param value フォーマットされた文字列。
     * @param format　指定フォーマット。java.text.DecimalFormatを参照。
     * @return　フォーマット前の数字。
     * @throws ParseException　変換エラー。
     */
    public static Number parseNumber(String value,String format) throws ParseException{
    	DecimalFormat df;
    	synchronized(numberFormats){
	    	df=numberFormats.get(format);
	    	if(df == null){
	        	df=new DecimalFormat(format);
	        	numberFormats.put(format, df);
	    	}
    	}
		return df.parse(value);
    }
    /**
     * 日付を指定フォーマットで文字列に変換する。
     * もし指定フォーマットに"G"がある場合(※例：GGGGy/MM/dd)、和暦として変換する。
     * @param value 日付データ。
     * @param format　指定フォーマット。java.text.SimpleDateFormatを参照。
     * @return　変換後の文字列。
     */
    public static String formatDate(Object value,String format){
    	DateFormat df;
    	synchronized(dateFormats){
	    	df=dateFormats.get(format);
	    	if(df == null){
	    		if(format.indexOf("G")>-1){
		        	df=new SimpleDateFormat(format,FormatManager.localeJ);
	    		}else{
		        	df=new SimpleDateFormat(format,FormatManager.locale);
	    		}
	        	dateFormats.put(format, df);
	    	}
    	}
		return df.format(value);    	
    }
    /**
     * フォーマットされた文字列をフォーマット前の日付に戻す。
     * @param value フォーマットされた文字列。
     * @param format　指定フォーマット。java.text.SimpleDateFormatを参照。
     * @return　フォーマット前の数字。
     * @throws ParseException　変換エラー。
     */
    public static Date parseDate(String value,String format) throws ParseException{
    	DateFormat df;
    	synchronized(dateFormats){
	    	df=dateFormats.get(format);
	    	if(df == null){
	    		if(format.indexOf("G")>-1){
		        	df=new SimpleDateFormat(format,FormatManager.localeJ);
	    		}else{
		        	df=new SimpleDateFormat(format,FormatManager.locale);
	    		}
	        	dateFormats.put(format, df);
	    	}
    	}
    	Calendar calendar = Calendar.getInstance(FormatManager.locale);
    	calendar.setLenient(false);
		return df.parse(value);    	
    }

}
