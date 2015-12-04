package efw.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
/**
 * initCoreタグを処理するクラス。
 * <efw:initCore/>
 * headタグ内に追加すれば、efwの基本機能を利用できる。
 * @author Chang Kejun
 *
 */
public class InitCore extends SimpleTagSupport {
	/**
	 * タグ処理。
	 *efwの基本機能を利用するため、複数のcssとjsを取り込むタグを作成する。
	 */
	@Override
	public void doTag() throws JspException, IOException {
		super.doTag();
		JspWriter out = this.getJspContext().getOut();
		out.print("<link href=\"./efw/jquery-ui.min.css\" rel=\"stylesheet\">");
		out.print("<link href=\"./efw/jquery-ui.structure.min.css\" rel=\"stylesheet\">");
		out.print("<link href=\"./efw/jquery-ui.theme.min.css\" rel=\"stylesheet\">");
		out.print("<link href=\"./efw/efw.loading.css\" rel=\"stylesheet\">");
		out.print("<script src=\"./efw/jquery-min.js\"></script>");
		out.print("<script src=\"./efw/jquery-ui.min.js\"></script>");
		out.print("<script src=\"./efw/efw.client.js\"></script>");
		out.print("<script src=\"./efw/efw.client.messages.js\"></script>");
	}
}
