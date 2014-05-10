package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractExecuteBean implements Serializable {

	private static final long serialVersionUID = -5243751052360622630L;

	@JsonProperty("TYPE")
	private String type;
	@JsonProperty("METHOD")
	private String method;
	@JsonProperty("TARGET")
	private String target;
	@JsonProperty("RESULT")
	private String result;

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMethod() {
		return this.method;
	}

	public void setMethod(String method) {
		this.method = method.toUpperCase();
	}

	public String getTarget() {
		return this.target;
	}

	public void setTarget(String target) {
		this.target = target.toUpperCase();
	}

	public String getResult() {
		return this.result;
	}

	public void setResult(String result) {
		this.result = result.toUpperCase();
	}
}
