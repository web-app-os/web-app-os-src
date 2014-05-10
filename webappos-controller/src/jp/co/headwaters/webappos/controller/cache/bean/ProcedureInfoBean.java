package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import jp.co.headwaters.webappos.controller.enumation.DataTypeEnum;
import jp.co.headwaters.webappos.controller.enumation.PgLanguageEnum;

public class ProcedureInfoBean implements Serializable {

	private static final long serialVersionUID = 7427875821896023673L;

	private String name;
	private PgLanguageEnum lang;
	private DataTypeEnum retType;
	private short argNum;
	private Map<String, DataTypeEnum> args;
	private List<String> outNames;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PgLanguageEnum getLang() {
		return this.lang;
	}

	public void setLang(PgLanguageEnum lang) {
		this.lang = lang;
	}

	public DataTypeEnum getRetType() {
		return this.retType;
	}

	public void setRetType(DataTypeEnum retType) {
		this.retType = retType;
	}

	public short getArgNum() {
		return this.argNum;
	}

	public void setArgNum(short argNum) {
		this.argNum = argNum;
	}

	public Map<String, DataTypeEnum> getArgs() {
		return this.args;
	}

	public void setArgs(Map<String, DataTypeEnum> args) {
		this.args = args;
	}

	public List<String> getOutNames() {
		return this.outNames;
	}

	public void setOutNames(List<String> outNames) {
		this.outNames = outNames;
	}
}
