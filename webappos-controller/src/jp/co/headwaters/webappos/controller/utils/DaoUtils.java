package jp.co.headwaters.webappos.controller.utils;

import static jp.co.headwaters.webappos.controller.utils.ConvertDateTypeUtils.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.cache.SchemaColumnCache;
import jp.co.headwaters.webappos.controller.cache.bean.SchemaColumnBean;
import jp.co.headwaters.webappos.controller.enumation.CrudEnum;
import jp.co.headwaters.webappos.controller.exception.NotFoundException;
import jp.co.headwaters.webappos.controller.exception.WebAppOSException;
import jp.co.headwaters.webappos.controller.model.AbstractEntity;
import jp.co.headwaters.webappos.controller.model.CommonExample;
import jp.co.headwaters.webappos.controller.model.CommonExample.Criteria;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class DaoUtils {
	private SqlSessionFactory sessionFactory;
	private static final Log _logger = LogFactory.getLog(DaoUtils.class);
	private static DaoUtils inst = new DaoUtils();

	private DaoUtils() {
		this.sessionFactory = createSqlSessionFactory();
	}

	private static SqlSessionFactory getSqlSessionFactory() {
		return inst.sessionFactory;
	}

	private SqlSessionFactory createSqlSessionFactory() {
		SqlSessionFactory ssf = null;
		try {
			ssf = new SqlSessionFactoryBuilder().build(Resources
					.getResourceAsStream(ControllerConstants.MYBATIS_CONFIG_FILE_NAME));
		} catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}
		return ssf;
	}

	public static SqlSession openSqlSession() {
		return openSqlSession(true);
	}

	public static SqlSession openSqlSession(boolean autoCommit) {
		return getSqlSessionFactory().openSession(autoCommit);
	}

	public static void closeSqlSession(SqlSession session) {
		if (session != null) {
			session.rollback(true);
			session.close();
		}
		session = null;
	}

	public static void commit(SqlSession session) {
		if (session != null) {
			session.commit(true);
		}
	}

	public static void rollback(SqlSession session) {
		if (session != null) {
			session.rollback(true);
		}
	}

	public static String getMapperName(String method, String target) {
		StringBuilder sb = new StringBuilder();
		sb.append(PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_ROOT_PACKAGE));
		sb.append('.');
		sb.append(ControllerConstants.MYBATIS_MAPPER_PACKAGE);
		sb.append('.');
		sb.append(ControllerUtils.snakeToCamel(target, true));
		sb.append(ControllerConstants.MYBATIS_MAPPER_SUFFIX);
		sb.append('.');
		sb.append(CrudEnum.getCrud(method).getStatementId());
		return sb.toString();
	}

	public static HashMap<String, Object> convertEntityToMap(Object obj, boolean isRecursive)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		HashMap<String, Object> recordMap = new HashMap<String, Object>();

		if (obj == null)
			return null;

		String key = null;
		Object value = null;
		for (java.lang.reflect.Method method : obj.getClass().getMethods()) {
			if (method.getName().startsWith("get") && !method.getName().equals("getClass")) { //$NON-NLS-1$ //$NON-NLS-2$
				value = method.invoke(obj);
				if (value != null) {
					if (value instanceof List) {
						if (!isRecursive) {
							continue;
						}
						List<?> list = (List<?>) value;
						List<HashMap<String, Object>> multipleList = new ArrayList<HashMap<String, Object>>();
						key = ControllerUtils.getResultMapKey(
								obj.getClass().getSimpleName(),
								ControllerUtils.camelToSnake(StringUtils.substringAfter(method.getName(), "get"))); //$NON-NLS-1$
						recordMap.put(key.toUpperCase(), multipleList);
						for (int i = 0; i < list.size(); i++) {
							multipleList.add(convertEntityToMap(list.get(i), isRecursive));
						}
					} else if (value instanceof AbstractEntity) {
						if (!isRecursive) {
							continue;
						}
						recordMap.putAll(convertEntityToMap(value, isRecursive));
					} else {
						key = ControllerUtils.getResultMapKey(
								obj.getClass().getSimpleName(),
								ControllerUtils.camelToSnake(StringUtils.substringAfter(method.getName(), "get"))); //$NON-NLS-1$
						recordMap.put(key.toUpperCase(), convertDbTypeToString(value));
					}
				}
			}
		}
		return recordMap;
	}

	public static boolean isConflict(SqlSession sqlSession, SchemaColumnBean schemaColumn, Object value) throws WebAppOSException {
		String mapperName = DaoUtils.getMapperName(CrudEnum.COUNT.getMethod(), schemaColumn.getTableName());
		CommonExample example = new CommonExample();
		Criteria criteria = example.createCriteria();
		criteria.andEqualTo(schemaColumn.getColumnName(), value);
		Integer count = sqlSession.selectOne(mapperName, example);
		if (count > 0) {
			return true;
		}
		return false;
	}

	public static void createCriteria(String resultName, String target, String column, String operator, String[] values, Criteria criteria, int i)
			throws WebAppOSException {
		String col = null;
		String[] names = column.split(ControllerConstants.REGEX_COLUMN_DELIMITER);
		if (names.length == 2) {
			col = column.toLowerCase();
		} else {
			col = target.split(",")[0].toLowerCase() + '.' + column.toLowerCase();
		}

		SchemaColumnBean schemaColumn = SchemaColumnCache.getSchemaColumn(
				col.replace(".", ControllerConstants.TABLE_COLUMN_DELIMITER)); //$NON-NLS-1$

		Object value = null;
		if (!ControllerConstants.OPERATOR_NULL.equals(operator)
				&& !ControllerConstants.OPERATOR_NOT_NULL.equals(operator)) {
			if (StringUtils.isEmpty(values[i])) {
				return;
			}
		}

		if (ControllerConstants.OPERATOR_NULL.equals(operator)) {
			criteria.andIsNull(col);
		} else if (ControllerConstants.OPERATOR_NOT_NULL.equals(operator)) {
			criteria.andIsNotNull(col);
		} else if (ControllerConstants.OPERATOR_EQUAL.equals(operator)) {
			value = convertStringToDbType(values[i], schemaColumn);
			criteria.andEqualTo(col, value);
		} else if (ControllerConstants.OPERATOR_NOT_EQUAL.equals(operator)) {
			value = convertStringToDbType(values[i], schemaColumn);
			criteria.andNotEqualTo(col, value);
		} else if (ControllerConstants.OPERATOR_GREATER_THAN.equals(operator)) {
			value = convertStringToDbType(values[i], schemaColumn);
			criteria.andGreaterThan(col, value);
		} else if (ControllerConstants.OPERATOR_GREATER_OR_EQUAL.equals(operator)) {
			value = convertStringToDbType(values[i], schemaColumn);
			criteria.andGreaterThanOrEqualTo(col, value);
		} else if (ControllerConstants.OPERATOR_LESS_THAN.equals(operator)) {
			value = convertStringToDbType(values[i], schemaColumn);
			criteria.andLessThan(col, value);
		} else if (ControllerConstants.OPERATOR_LESS_OR_EQUAL.equals(operator)) {
			value = convertStringToDbType(values[i], schemaColumn);
			criteria.andLessThanOrEqualTo(col, value);
		} else if (ControllerConstants.OPERATOR_FRONT_LIKE.equals(operator)) {
			criteria.andLike(col, "%" + values[i]); //$NON-NLS-1$
		} else if (ControllerConstants.OPERATOR_MIDDLE_LIKE.equals(operator)) {
			criteria.andLike(col, "%" + values[i] + "%"); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (ControllerConstants.OPERATOR_BACK_LIKE.equals(operator)) {
			criteria.andLike(col, values[i] + "%"); //$NON-NLS-1$
		} else if (ControllerConstants.OPERATOR_FRONT_NOT_LIKE.equals(operator)) {
			criteria.andNotLike(col, "%" + values[i]); //$NON-NLS-1$
		} else if (ControllerConstants.OPERATOR_MIDDLE_NOT_LIKE.equals(operator)) {
			criteria.andNotLike(col, "%" + values[i] + "%"); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (ControllerConstants.OPERATOR_BACK_NOT_LIKE.equals(operator)) {
			criteria.andNotLike(col, values[i] + "%"); //$NON-NLS-1$
		} else if (ControllerConstants.OPERATOR_IN.equals(operator)) {
			List<Object> paramList = new ArrayList<Object>();
			for (String val1 : values) {
				for (String val2 : Arrays.asList(val1.split(ControllerConstants.CRUD_IN_DELIMITER))) {
					if (StringUtils.isEmpty(val2)) {
						continue;
					}
					paramList.add(convertStringToDbType(val2.trim(), schemaColumn));
				}
			}
			criteria.andIn(col, paramList);
		} else if (ControllerConstants.OPERATOR_NOT_IN.equals(operator)) {
			List<Object> paramList = new ArrayList<Object>();
			for (String val1 : values) {
				for (String val2 : Arrays.asList(val1.split(ControllerConstants.CRUD_IN_DELIMITER))) {
					if (StringUtils.isEmpty(val2)) {
						continue;
					}
					paramList.add(convertStringToDbType(val2.trim(), schemaColumn));
				}
			}
			criteria.andNotIn(col, paramList);
		} else {
			throw new NotFoundException("err.201", operator); //$NON-NLS-1$
		}
	}
}