package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;
import java.util.Map;

public class ActionBean implements Serializable {

	private static final long serialVersionUID = 263202800143736000L;

	/** action名 */
	private String name;
	/** HTML格納パス(相対パス) */
	private String htmlPath;
	/** jspファイル毎の画面ロード時実行情報 */
	private Map<String, ExecuteBean> loadExecuteMap;
	/** form識別子毎の実行情報 */
	private Map<String, ExecuteBean> submitExecuteMap;

	public ActionBean(String name, String htmlPath) {
		this.name = name;
		this.htmlPath = htmlPath;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHtmlPath() {
		return this.htmlPath;
	}

	public void setHtmlPath(String htmlPath) {
		this.htmlPath = htmlPath;
	}

	public Map<String, ExecuteBean> getLoadExecuteMap() {
		return this.loadExecuteMap;
	}

	public void setLoadExecuteMap(Map<String, ExecuteBean> loadExecuteMap) {
		this.loadExecuteMap = loadExecuteMap;
	}

	public Map<String, ExecuteBean> getSubmitExecuteMap() {
		return this.submitExecuteMap;
	}

	public void setSubmitExecuteMap(Map<String, ExecuteBean> submitExecuteMap) {
		this.submitExecuteMap = submitExecuteMap;
	}
}
