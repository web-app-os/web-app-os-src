package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConditionBean implements Serializable {

	private static final long serialVersionUID = 7654264045835853203L;

	@JsonProperty("RESULT")
	private String result;
	@JsonProperty("COL")
	private String columnName;
	@JsonProperty("OPE")
	private String operator;
	@JsonProperty("VALUE")
	private String value;

	public String getResult() {
		return this.result;
	}

	public void setResult(String result) {
		this.result = result.toUpperCase();
	}

	public String getColumnName() {
		return this.columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName.toUpperCase();
	}

	public String getOperator() {
		return this.operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
