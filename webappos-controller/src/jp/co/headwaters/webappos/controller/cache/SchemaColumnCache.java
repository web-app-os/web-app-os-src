package jp.co.headwaters.webappos.controller.cache;

import jp.co.headwaters.webappos.controller.cache.bean.SchemaColumnBean;

public class SchemaColumnCache {

	public static SchemaColumnBean getSchemaColumn(String colName) {
		return WebAppOSCache.getInstance().getSchemaColumnMap().get(colName.toUpperCase());
	}
}
