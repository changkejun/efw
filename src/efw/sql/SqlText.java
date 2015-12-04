package efw.sql;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
/**
 * sqlタグにifタグに分割されるSql文の部品を表すクラス。
 * @author Chang Kejun
 *
 */
final class SqlText {
	/**
	 * Sql文の部品によりオブジェクトを作成する。
	 * @param text Sql文部品。
	 */
	protected SqlText(String text){
		this.text=text+"\n";//必ず１改行を入れる。[^\\w]のため
	}
	/**
	 * 代入されるSql文部品そのもの。
	 */
	private String text;
	/**
	 * 代入されるSql文部品をそのまま取得する。
	 * @return Sql文部品。
	 */
	protected String getText() {
		return text;
	}
	/**
	 * Sql文部品に含まれるパラメーターキーを取得する。
	 * キーは、　「:　＋　キーの単語」で構成する。
	 * @return　出現順番どおりにキーの単語を格納する配列を戻す。
	 */
	protected ArrayList<String> getParamKeys(){
		ArrayList<String> ret=new ArrayList<String>();
		Pattern p = Pattern.compile("(\\:[\\w]*)[^\\w]");//20150925 (\\:[\\w]*)[\\s] -> (\\:[\\w]*)[^\\w]
		Matcher m;
		String tmp;
		String subsql=text.replaceAll("//.*\\n", "\n");//コメント行を認識するため
		subsql=subsql.replaceAll("\\-\\-.*\\n", "\n");//コメント行を認識するため
		subsql=subsql.replaceAll("/\\*/?([^/]|[^*]/)*\\*/", "");//コメント行を認識するため
		m= p.matcher(subsql);
		while(m.find()){
			tmp=m.group();
			tmp=tmp.substring(1, tmp.length()-1);
			ret.add(tmp);
		}
		return ret;
	}
	/**
	 * 代入されるSql文部品を通常書き方に変換する。
	 * 「:　＋　キーの単語」を「 ? 」に変換する。
	 * @return　変換後の文字列を戻す。
	 */
	protected String getSQL(){
		String subsql=text.replaceAll("//.*\\n", "\n");//コメント行を認識するため
		subsql=subsql.replaceAll("\\-\\-.*\\n", "\n");//コメント行を認識するため
		subsql=subsql.replaceAll("/\\*/?([^/]|[^*]/)*\\*/", "");//コメント行を認識するため
		subsql=subsql.replaceAll("(\\:[\\w]*)", " ? ");//20150925 (\\:[\\w]*)[\\s] -> (\\:[\\w]*)[^\\w]
		return subsql;
	}
	
}
