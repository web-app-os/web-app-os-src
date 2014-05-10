package jp.co.headwaters.webappos.generator.db;

import static jp.co.headwaters.webappos.generator.utils.DataBaseUtils.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.headwaters.webappos.controller.cache.WebAppOSCache;
import jp.co.headwaters.webappos.controller.cache.bean.ColumnCommentBean;
import jp.co.headwaters.webappos.controller.cache.bean.SchemaColumnBean;
import jp.co.headwaters.webappos.controller.cache.bean.SystemConstantBean;
import jp.co.headwaters.webappos.controller.enumation.DataTypeEnum;
import jp.co.headwaters.webappos.controller.exception.WebAppOSException;
import jp.co.headwaters.webappos.controller.utils.ControllerUtils;
import jp.co.headwaters.webappos.controller.utils.ConvertDateTypeUtils;
import jp.co.headwaters.webappos.generator.utils.MessageUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.generator.config.JDBCConnectionConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SystemTableLoader {

	private static final Log _logger = LogFactory.getLog(SystemTableLoader.class);

	private static final String SELECT_SCHEMA_COLUMNS = "select * from _webappos_schema_columns order by table_name, column_name"; //$NON-NLS-1$

	private static final String SELECT_SYSTEM_CONSTANT = "select" + //$NON-NLS-1$
														"  coalesce(category,'') as category, " + //$NON-NLS-1$
														"  key, " + //$NON-NLS-1$
														"  value, " + //$NON-NLS-1$
														"  data_type, " + //$NON-NLS-1$
														"  display_order " + //$NON-NLS-1$
														"from " + //$NON-NLS-1$
														"  _webappos_system_constant " + //$NON-NLS-1$
														"order by category, display_order"; //$NON-NLS-1$

	public boolean load(JDBCConnectionConfiguration config) {
		if (!getSchemaColumn(config)) {
			return false;
		}
		if (!getSystemConstant(config)) {
			return false;
		}
		return true;
	}

	private boolean getSchemaColumn(JDBCConnectionConfiguration config) {
		Map<String, SchemaColumnBean> schemaColumnMap = new HashMap<String, SchemaColumnBean>();
		WebAppOSCache.getInstance().setSchemaColumnMap(schemaColumnMap);
		ObjectMapper mapper = new ObjectMapper();

		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		String key = null;
		try {
			connection = getConnection(config);
			stmt = connection.createStatement();
			rs = stmt.executeQuery(SELECT_SCHEMA_COLUMNS);
			while (rs.next()) {
				SchemaColumnBean schemaColumn = new SchemaColumnBean();
				schemaColumn.setTableName(rs.getString("table_name")); //$NON-NLS-1$
				schemaColumn.setColumnName(rs.getString("column_name")); //$NON-NLS-1$
				schemaColumn.setNullable(rs.getBoolean("is_nullable")); //$NON-NLS-1$
				schemaColumn.setDataType(rs.getString("data_type")); //$NON-NLS-1$
				if (!StringUtils.isEmpty(rs.getString("column_comment"))){ //$NON-NLS-1$
					schemaColumn.setColumnComment(mapper.readValue(rs.getString("column_comment"), ColumnCommentBean.class)); //$NON-NLS-1$
				}
				schemaColumn.setUnique(rs.getBoolean("is_unique")); //$NON-NLS-1$
				key = ControllerUtils.getSchemaColumnKey(schemaColumn.getTableName(), schemaColumn.getColumnName());
				schemaColumnMap.put(key, schemaColumn);
			}
		} catch (Exception e) {
			_logger.error(MessageUtils.getString("err.103"), e); //$NON-NLS-1$
			return false;
		} finally {
			closeResultSet(rs);
			closeStatement(stmt);
			closeConnection(connection);
		}
		return true;
	}

	private boolean getSystemConstant(JDBCConnectionConfiguration config) {
		Map<String, Map<String, SystemConstantBean>> systemConstantMap = new HashMap<String, Map<String, SystemConstantBean>>();
		WebAppOSCache.getInstance().setSystemConstantMap(systemConstantMap);

		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			connection = getConnection(config);
			stmt = connection.createStatement();
			rs = stmt.executeQuery(SELECT_SYSTEM_CONSTANT);
			while (rs.next()) {
				SystemConstantBean systemConstant = new SystemConstantBean();
				systemConstant.setCategory(rs.getString("category")); //$NON-NLS-1$
				systemConstant.setKey(rs.getString("key")); //$NON-NLS-1$
				systemConstant.setValue(rs.getString("value")); //$NON-NLS-1$
				systemConstant.setDataType(rs.getString("data_type")); //$NON-NLS-1$
				systemConstant.setDisplayOrder(rs.getInt("display_order")); //$NON-NLS-1$
				setRealValue(systemConstant);
				if (!systemConstantMap.containsKey(systemConstant.getCategory())) {
					systemConstantMap.put(systemConstant.getCategory(), new HashMap<String, SystemConstantBean>());
				}
				systemConstantMap.get(systemConstant.getCategory()).put(systemConstant.getKey(), systemConstant);
			}
		} catch (Exception e) {
			_logger.error(MessageUtils.getString("err.104"), e); //$NON-NLS-1$
			return false;
		} finally {
			closeResultSet(rs);
			closeStatement(stmt);
			closeConnection(connection);
		}
		return true;
	}

	private void setRealValue(SystemConstantBean systemConstant) throws WebAppOSException {
		Object realValue = null;
		if (!StringUtils.isEmpty(systemConstant.getValue())) {
			if (DataTypeEnum.DATA_TYPE_MAP.getDataType().equals(systemConstant.getDataType().toLowerCase())) {
				Map<String, String> map = new HashMap<String, String>();
				String[] array = systemConstant.getValue().split(","); //$NON-NLS-1$
				for (String e : array) {
					String[] value = e.split("="); //$NON-NLS-1$
					map.put(value[0], value[1]);
				}
				realValue = map;
			} else if (DataTypeEnum.DATA_TYPE_MAP.getDataType().equals(systemConstant.getDataType().toLowerCase())) {
				List<String> list = new ArrayList<String>();
				String[] array = systemConstant.getValue().split(","); //$NON-NLS-1$
				for (String e : array) {
					list.add(e);
				}
				realValue = list;
			} else {
				realValue = ConvertDateTypeUtils.convertStringToDbType(systemConstant.getValue(), systemConstant.getDataType());
			}
		}
		systemConstant.setRealValue(realValue);
	}
}
