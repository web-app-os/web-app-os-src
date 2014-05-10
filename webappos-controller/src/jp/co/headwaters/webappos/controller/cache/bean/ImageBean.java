package jp.co.headwaters.webappos.controller.cache.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImageBean extends BindOutputBean {

	private static final long serialVersionUID = -3308782720898201207L;

	@JsonProperty("W")
	private String width;
	@JsonProperty("H")
	private String height;
	@JsonProperty("TYPE")
	private String stretchType;

	public String getWidth() {
		return this.width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getHeight() {
		return this.height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getStretchType() {
		return this.stretchType;
	}

	public void setStretchType(String stretchType) {
		this.stretchType = stretchType;
	}
}
