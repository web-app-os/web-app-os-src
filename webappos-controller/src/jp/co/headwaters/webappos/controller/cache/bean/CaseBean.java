package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CaseBean implements Serializable {

	private static final long serialVersionUID = 229656636451033590L;

	@JsonProperty("TYPE")
	private String type;
	@JsonProperty("OPE")
	private String operator;
	@JsonProperty("ARG")
	private List<String> args;

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type.toUpperCase();
	}

	public String getOperator() {
		return this.operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public List<String> getArgs() {
		return this.args;
	}

	public void setArgs(List<String> args) {
		args.set(0, args.get(0).toUpperCase());
		this.args = args;
	}
}
