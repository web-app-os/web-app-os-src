package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FunctionBean extends CrudBean implements Serializable {

	private static final long serialVersionUID = -8826821816343233277L;

	/**
	 * テーブル.カラム別に演算子を保持するMap<br>
	 * 属性のJSONパラメータを元に生成する<br>
	 * Submit時にパラメータに含まれるテーブル名、カラム名から演算子を取得する為に使用する<br>
	 * セキュリティ保護の為、パラメータ名によって演算子は導出せず、キャッシュから取得する
	 */
	private Map<String, String> operatorMap;

	@JsonProperty("ADDRESS_COL")
	private String toAddressColumnName;
	@JsonProperty("ID_COL")
	private String toIdColumnName;
	@JsonProperty("TO")
	private List<String> toList;
	@JsonProperty("CC")
	private List<String> ccList;
	@JsonProperty("BCC")
	private List<String> bccList;

	@JsonProperty("TOKEN")
	private boolean isTokenValidation = true;

	private String[] toIds = null;

	public Map<String, String> getOperatorMap() {
		return this.operatorMap;
	}

	public void setOperatorMap(Map<String, String> operatorMap) {
		this.operatorMap = operatorMap;
	}

	public String getToAddressColumnName() {
		return this.toAddressColumnName;
	}

	public void setToAddressColumnName(String toAddressColumnName) {
		this.toAddressColumnName = toAddressColumnName;
	}

	public String getToIdColumnName() {
		return this.toIdColumnName;
	}

	public void setToIdColumnName(String toIdColumnName) {
		this.toIdColumnName = toIdColumnName;
	}

	public List<String> getToList() {
		return this.toList;
	}

	public void setToList(List<String> toList) {
		this.toList = toList;
	}

	public List<String> getCcList() {
		return this.ccList;
	}

	public void setCcList(List<String> ccList) {
		this.ccList = ccList;
	}

	public List<String> getBccList() {
		return this.bccList;
	}

	public void setBccList(List<String> bccList) {
		this.bccList = bccList;
	}

	public boolean isTokenValidation() {
		return this.isTokenValidation;
	}

	public void setTokenValidation(boolean isTokenValidation) {
		this.isTokenValidation = isTokenValidation;
	}

	public String[] getToIds() {
		return this.toIds;
	}

	public void setToIds(String[] toIds) {
		this.toIds = toIds;
	}
}
