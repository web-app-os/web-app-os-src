package jp.co.headwaters.webappos.generator.mybatis.bean;

public class JoinElementInfo {

	private String includeRefId;
	private String joinTable;
	private String joinColumn;

	public String getIncludeRefId() {
		return this.includeRefId;
	}

	public void setIncludeRefId(String includeRefId) {
		this.includeRefId = includeRefId;
	}

	public String getJoinTable() {
		return this.joinTable;
	}

	public void setJoinTable(String joinTable) {
		this.joinTable = joinTable;
	}

	public String getJoinColumn() {
		return this.joinColumn;
	}

	public void setJoinColumn(String joinColumn) {
		this.joinColumn = joinColumn;
	}
}
