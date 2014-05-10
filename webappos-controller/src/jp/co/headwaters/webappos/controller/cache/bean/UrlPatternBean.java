package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;

public class UrlPatternBean implements Serializable {

	private static final long serialVersionUID = 6271162749295580564L;

	/** url */
	private String pattern;
	/** for struts2 action */
	private String actionName;
	/** for struts2 result */
	private String resultName;
	/** Authentication */
	private boolean isAuthRequire = false;

	@SuppressWarnings("unused")
	private UrlPatternBean() {
	}

	public UrlPatternBean(String pattern, String actionName, String resultName) {
		this.pattern = pattern;
		this.actionName = actionName;
		this.resultName = resultName;
	}

	public String getPattern() {
		return this.pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getActionName() {
		return this.actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getResultName() {
		return this.resultName;
	}

	public void setResultName(String resultName) {
		this.resultName = resultName;
	}

	public boolean isAuthRequire() {
		return this.isAuthRequire;
	}

	public void setAuthRequire(boolean isAuthRequire) {
		this.isAuthRequire = isAuthRequire;
	}
}
