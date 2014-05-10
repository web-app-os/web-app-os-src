package jp.co.headwaters.webappos.controller.cache.bean;

import java.io.Serializable;

public class SchemaColumnBean implements Serializable {

	private static final long serialVersionUID = 5894487642298109772L;

	private String tableName;
	private String columnName;
	private boolean isNullable;
	private String dataType;
	private ColumnCommentBean columnComment;
	private boolean isUnique;

	public String getTableName() {
		return this.tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return this.columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public boolean isNullable() {
		return this.isNullable;
	}

	public void setNullable(boolean isNullable) {
		this.isNullable = isNullable;
	}

	public String getDataType() {
		return this.dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public ColumnCommentBean getColumnComment() {
		return this.columnComment;
	}

	public void setColumnComment(ColumnCommentBean columnComment) {
		this.columnComment = columnComment;
	}

	public boolean isUnique() {
		return this.isUnique;
	}

	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}
}
