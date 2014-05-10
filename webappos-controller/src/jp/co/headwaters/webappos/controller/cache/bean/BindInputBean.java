package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BindInputBean implements Serializable {

	private static final long serialVersionUID = -3288551829799841002L;

	@JsonProperty("RESULT")
	private String result;
	@JsonProperty("ARG")
	private String arg;
	@JsonProperty("VALUE")
	private String value;

	public String getResult() {
		return this.result;
	}

	public void setResult(String result) {
		this.result = result.toUpperCase();
	}

	public String getArg() {
		return this.arg;
	}

	public void setArg(String arg) {
		this.arg = arg.toUpperCase();
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
