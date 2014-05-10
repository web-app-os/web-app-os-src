package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProcedureArgBean implements Serializable {

	private static final long serialVersionUID = -9196372820140517814L;

	@JsonProperty("NAME")
	private String name;
	@JsonProperty("VALUE")
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
