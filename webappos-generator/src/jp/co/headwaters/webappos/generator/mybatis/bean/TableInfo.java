package jp.co.headwaters.webappos.generator.mybatis.bean;

public class TableInfo {

	private String tableName;
	private boolean isView;

	public TableInfo(String tableName, boolean isView) {
		this.tableName = tableName;
		this.setView(isView);
	}

	public String getTableName() {
		return this.tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public boolean isView() {
		return this.isView;
	}

	public void setView(boolean isView) {
		this.isView = isView;
	}
}
