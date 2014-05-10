package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;

import jp.co.headwaters.webappos.controller.ControllerConstants;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PagerBean implements Serializable {

	private static final long serialVersionUID = 1966177978523964414L;

	@JsonProperty("PARAM_NAME")
	private String paramName = ControllerConstants.DEFAULT_PAGE_NO_PARAM_NAME;
	@JsonProperty("PERPAGE")
	private String perPage = ControllerConstants.DEFAULT_PER_PAGE;
	@JsonProperty("PAGERCOUNT")
	private String pagerCount = ControllerConstants.DEFAULT_PAGER_COUNT;

	public String getParamName() {
		return this.paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getPerPage() {
		return this.perPage;
	}

	public void setPerPage(String perPage) {
		this.perPage = perPage;
	}

	public String getPagerCount() {
		return this.pagerCount;
	}

	public void setPagerCount(String pagerCount) {
		this.pagerCount = pagerCount;
	}
}
