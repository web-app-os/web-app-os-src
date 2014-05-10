package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;

public class ResultBean implements Serializable {

	private static final long serialVersionUID = 5319805844143190848L;

	private String name;
	private String value;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
