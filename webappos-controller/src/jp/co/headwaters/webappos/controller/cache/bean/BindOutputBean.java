package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BindOutputBean implements Serializable {

	private static final long serialVersionUID = -4882163175523051410L;

	@JsonProperty("RESULT")
	private String result;
	@JsonProperty("TARGET")
	private String target;
	@JsonProperty("FORMAT")
	private String format;
	@JsonProperty("ARG")
	private String arg;
	@JsonProperty("ARGS")
	private List<String> args;
	@JsonProperty("VALUE")
	private String value;
	@JsonProperty("MAP_KEY")
	private String mapKeyName;
	@JsonProperty("ESCAPE")
	private String escape = "true"; //$NON-NLS-1$
	@JsonProperty("ERASE_TAG")
	private String eraseTag = "false"; //$NON-NLS-1$

	public String getResult() {
		return this.result;
	}

	public void setResult(String result) {
		this.result = result.toUpperCase();
	}

	public String getTarget() {
		return this.target;
	}

	public void setTarget(String target) {
		this.target = target.toUpperCase();
	}

	public String getFormat() {
		return this.format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getArg() {
		return this.arg;
	}

	public void setArg(String arg) {
		this.arg = arg;
	}

	public List<String> getArgs() {
		return this.args;
	}

	public void setArgs(List<String> args) {
		this.args = args;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getMapKeyName() {
		return this.mapKeyName;
	}

	public void setMapKeyName(String mapKeyName) {
		this.mapKeyName = mapKeyName;
	}

	public String getEscape() {
		return this.escape;
	}

	public void setEscape(String escape) {
		this.escape = escape;
	}

	public String getEraseTag() {
		return this.eraseTag;
	}

	public void setEraseTag(String eraseTag) {
		this.eraseTag = eraseTag;
	}
}
