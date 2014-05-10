package jp.co.headwaters.webappos.controller.fuction;

import static jp.co.headwaters.webappos.controller.utils.ControllerUtils.*;
import static jp.co.headwaters.webappos.controller.utils.ConvertDateTypeUtils.*;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.cache.SchemaColumnCache;
import jp.co.headwaters.webappos.controller.cache.WebAppOSCache;
import jp.co.headwaters.webappos.controller.cache.bean.AbstractExecuteBean;
import jp.co.headwaters.webappos.controller.cache.bean.ConditionBean;
import jp.co.headwaters.webappos.controller.cache.bean.CrudBean;
import jp.co.headwaters.webappos.controller.cache.bean.FunctionBean;
import jp.co.headwaters.webappos.controller.cache.bean.LoadBean;
import jp.co.headwaters.webappos.controller.cache.bean.RelationKeyBean;
import jp.co.headwaters.webappos.controller.cache.bean.SchemaColumnBean;
import jp.co.headwaters.webappos.controller.enumation.CrudEnum;
import jp.co.headwaters.webappos.controller.exception.ExecuteDeniedException;
import jp.co.headwaters.webappos.controller.exception.NotFoundException;
import jp.co.headwaters.webappos.controller.exception.WebAppOSException;
import jp.co.headwaters.webappos.controller.model.CommonExample;
import jp.co.headwaters.webappos.controller.model.CommonExample.Criteria;
import jp.co.headwaters.webappos.controller.session.OAuthBean;
import jp.co.headwaters.webappos.controller.utils.AuthUtils;
import jp.co.headwaters.webappos.controller.utils.DaoUtils;
import jp.co.headwaters.webappos.controller.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;

public class CrudFunction extends AbstractFunction {

	@Override
	protected void execute(AbstractExecuteBean function)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, WebAppOSException {
		int resultCount = 0;
		CrudBean crudFunction = (CrudBean)function;
		if (CrudEnum.SELECT_BY_EXAMPLE.getMethod().equals(function.getMethod())
				|| CrudEnum.SELECT_ALL_BY_EXAMPLE.getMethod().equals(function.getMethod())) {
			resultCount = executeSelectByExample(crudFunction);
		} else if (CrudEnum.SELECT_BY_PK.getMethod().equals(function.getMethod())) {
			resultCount = executeSelectByPrimaryKey(crudFunction);
		} else if (CrudEnum.INSERT.getMethod().equals(function.getMethod())) {
			int count = getExecuteCount(crudFunction);
			for (int i = 0; i <= count; i++) {
				resultCount += executeInsert(crudFunction, i);
			}
		} else if (CrudEnum.UPDATE.getMethod().equals(function.getMethod())
				|| CrudEnum.UPDATE_ALL.getMethod().equals(function.getMethod())) {
			int count = getExecuteCount(crudFunction);
			for (int i = 0; i <= count; i++) {
				resultCount += executeUpdate(crudFunction, i);
			}
		} else if (CrudEnum.DELETE.getMethod().equals(function.getMethod())) {
			resultCount = executeDelete(crudFunction);
		}
		crudFunction.setResultCount(resultCount);
	}

	private int getExecuteCount(CrudBean function) {
		String[] param = this._requestParams.get(getExecuteCountName(function));
		if (param == null || param.length == 0) {
			return 0;
		} else {
			return Integer.parseInt(param[0]) - 1;
		}
	}

	private String getExecuteCountName(CrudBean function) {
		StringBuilder sb = new StringBuilder();
		sb.append(function.getResult());
		sb.append(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER);
		sb.append(ControllerConstants.REQUEST_PARAM_NAME_CRUD_COUNT);
		return sb.toString();
	}

	private int executeSelectByExample(CrudBean function)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, WebAppOSException {
		String resultName = function.getResult();
		String target = function.getTarget().split(",")[0];
		String mapperName = DaoUtils.getMapperName(function.getMethod(), target);

		CommonExample example = createExample(function, 0);
		if (example == null) {
			this._resultMap.put(resultName, new ArrayList<HashMap<String, Object>>());
			return 0;
		}
		example.setJoinTables(Arrays.asList(function.getTarget().toLowerCase().split(",")));

		String countMapperName = null;
		if (CrudEnum.SELECT_ALL_BY_EXAMPLE.getMethod().equalsIgnoreCase(function.getMethod())) {
			countMapperName = DaoUtils.getMapperName(CrudEnum.COUNT_ALL.getMethod(), target);
		} else {
			countMapperName = DaoUtils.getMapperName(CrudEnum.COUNT.getMethod(), target);
		}
		Integer count = this._sqlSession.selectOne(countMapperName, example);
		if (count == 0) {
			createRowBounds(function, 0);
			if (function instanceof LoadBean){
				if (((LoadBean) function).isNotFooundError()) {
					throw new NotFoundException();
				}
			}
			this._resultMap.put(resultName, new ArrayList<HashMap<String, Object>>());
			return 0;
		}

		RowBounds rowBounds = null;
		try {
			rowBounds = createRowBounds(function, count);
		} catch (NotFoundException e) {
			if (function instanceof LoadBean){
				if (((LoadBean) function).isNotFooundError()) {
					throw new NotFoundException();
				}
			}
			return 0;
		}

		List<?> entityList = null;
		HashMap<String, List<HashMap<String, Object>>> rootMap = new HashMap<String, List<HashMap<String, Object>>>();
		List<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
		rootMap.put(target, resultList);
		this._resultMap.put(resultName, rootMap);
		if (CrudEnum.SELECT_BY_EXAMPLE.getMethod().equals(function.getMethod())) {
			if (rowBounds == null) {
				entityList = this._sqlSession.selectList(mapperName, example);
			} else {
				entityList = this._sqlSession.selectList(mapperName, example, rowBounds);
			}
			for (int i = 0; i < entityList.size(); i++) {
				resultList.add(DaoUtils.convertEntityToMap(entityList.get(i), false));
			}
		} else if (CrudEnum.SELECT_ALL_BY_EXAMPLE.getMethod().equals(function.getMethod())) {
			entityList = this._sqlSession.selectList(mapperName, example);
			if (rowBounds != null) {
				int fromIndex = entityList.size() < rowBounds.getOffset() ? entityList.size() : rowBounds.getOffset();
				int toIndex = entityList.size() < fromIndex + rowBounds.getLimit() ? entityList.size() : fromIndex + rowBounds.getLimit();
				entityList = entityList.subList(fromIndex, toIndex);
			}
			for (int i = 0; i < entityList.size(); i++) {
				resultList.add(DaoUtils.convertEntityToMap(entityList.get(i), true));
			}
		}
		return entityList.size();
	}

	private int executeSelectByPrimaryKey(CrudBean function)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, WebAppOSException {
		String resultName = function.getResult();
		String mapperName = DaoUtils.getMapperName(function.getMethod(), function.getTarget());

		CommonExample example = createExample(function, 0);
		if (example == null
				|| example.getOredCriteria().size() == 0
				|| example.getOredCriteria().get(0).getCriteria().size() == 0) {
			throw new WebAppOSException("err.200"); //$NON-NLS-1$
		}

		Integer count = this._sqlSession.selectOne(DaoUtils.getMapperName(CrudEnum.COUNT.getMethod(), function.getTarget()), example);
		if (count == 0) {
			if (function instanceof LoadBean){
				if (((LoadBean) function).isNotFooundError()) {
					throw new NotFoundException();
				}
			}
			this._resultMap.put(resultName, new HashMap<String, Object>());
			return 0;
		} else if (count != 1) {
			throw new WebAppOSException();
		}

		List<?> entityList = null;
		entityList = this._sqlSession.selectList(mapperName, example);
		this._resultMap.put(resultName, DaoUtils.convertEntityToMap(entityList.get(0), false));
		return entityList.size();
	}

	private int executeInsert(CrudBean function, int i)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, WebAppOSException {
		int result = 0;
		String mapperName = DaoUtils.getMapperName(function.getMethod(), function.getTarget());

		Map<String, Object> daoParams = new HashMap<String, Object>();
		createDaoParamFromCache(function, daoParams);
		createDaoParamFromRequest(function, daoParams, i);
		String fkColumnName = null;
		Map<String, List<RelationKeyBean>> importedKeys = WebAppOSCache.getInstance().getImportedKeys();
		if (importedKeys != null) {
			List<RelationKeyBean> importedKey = importedKeys.get(function.getTarget().toLowerCase());
			if (importedKey != null) {
				for (RelationKeyBean relation : importedKey) {
					fkColumnName = getSchemaColumnKey(relation.getPkTableName(), ControllerConstants.PK_COLUMN_NAME);
					if (this._ids.containsKey(fkColumnName)) {
						if (this._ids.get(fkColumnName).get(0) != null) {
							daoParams.put(snakeToCamel(fkColumnName.toLowerCase()), this._ids.get(fkColumnName).get(0));
						}
					}
				}
			}
		}
		if (daoParams.size() == 0) {
			return 0;
		}
		daoParams.put(snakeToCamel(ControllerConstants.CREATED_COLUMN_NAME), new Timestamp(System.currentTimeMillis()));
		daoParams.put(snakeToCamel(ControllerConstants.UPDATED_COLUMN_NAME), new Timestamp(System.currentTimeMillis()));

		Map<String, Object> insertMap = new HashMap<String, Object>();
		insertMap.put(ControllerConstants.MYBATIS_MAP_KEY_RECORD, daoParams);

		result = this._sqlSession.insert(mapperName, insertMap);
		@SuppressWarnings("unchecked")
		Object id = ((Map<String, Object>)insertMap.get(ControllerConstants.MYBATIS_MAP_KEY_RECORD)).get(ControllerConstants.PK_COLUMN_NAME);
		function.putId(i, Integer.valueOf(id.toString()));

		if (function.getTarget().equalsIgnoreCase(
				PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_TABLE_NAME))) {
			// 認証テーブル登録時は、システムテーブルも同時に登録する
			OAuthBean oAuthData = (OAuthBean) this._httpSession.get(OAuthBean.getKey());
			if (oAuthData != null) {
				if (oAuthData.getProviderName() != null && oAuthData.getUid() != null){
					oAuthData.setUserId(id.toString());
					AuthUtils.registerOAuthData(this._sqlSession, oAuthData.getProviderName(), oAuthData.getUserId(), oAuthData.getUid());
				}
			}
		}
		return result;
	}

	private int executeUpdate(CrudBean function, int i) throws WebAppOSException {
		int result = 0;
		String mapperName = DaoUtils.getMapperName(function.getMethod(), function.getTarget());

		Map<String, Object> daoParams = new HashMap<String, Object>();
		createDaoParamFromCache(function, daoParams);
		createDaoParamFromRequest(function, daoParams, i);
		if (daoParams.size() == 0) {
			return 0;
		}
		daoParams.put(snakeToCamel(ControllerConstants.UPDATED_COLUMN_NAME), new Timestamp(System.currentTimeMillis()));

		Map<String, Object> updateMap = new HashMap<String, Object>();
		updateMap.put(ControllerConstants.MYBATIS_MAP_KEY_RECORD, daoParams);

		CommonExample example = createExample(function, i);
		if (example == null
			|| example.getOredCriteria().size() == 0
			|| example.getOredCriteria().get(0).getCriteria().size() == 0) {
			throw new WebAppOSException("err.202"); //$NON-NLS-1$
		}
		updateMap.put(ControllerConstants.MYBATIS_MAP_KEY_EXAMPLE, example);
		result = this._sqlSession.update(mapperName, updateMap);
		if (function.getTarget().equalsIgnoreCase(
				PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_TABLE_NAME))) {
			@SuppressWarnings("unchecked")
			Map<String, Object> loginUser = (HashMap<String, Object>) this._httpSession.get(ControllerConstants.SESSION_KEY_LOGIN_USER);
			Map<String, Object> newUser = AuthUtils.refreshLoginUserData(this._sqlSession, loginUser);
			this._httpSession.put(ControllerConstants.SESSION_KEY_LOGIN_USER, newUser);
		}
		return result;
	}

	private int executeDelete(CrudBean function)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, WebAppOSException {
		String mapperName = DaoUtils.getMapperName(function.getMethod(), function.getTarget());

		CommonExample example = createExample(function, 0);
		if (example == null
				|| example.getOredCriteria().size() == 0
				|| example.getOredCriteria().get(0).getCriteria().size() == 0) {
			throw new WebAppOSException("err.202"); //$NON-NLS-1$
		}
		return this._sqlSession.delete(mapperName, example);
	}

	private CommonExample createExample(CrudBean function, int i)
			throws WebAppOSException {
		CommonExample example = new CommonExample();
		Criteria criteria = example.createCriteria();
		String target = function.getTarget().split(",")[0];
		Map<Integer, Integer> ids = this._ids.get(getSchemaColumnKey(target, ControllerConstants.PK_COLUMN_NAME));
		if (ids != null && ids.size() > 0) {
			String col = target + '.' + ControllerConstants.PK_COLUMN_NAME;
			List<Object> paramList = new ArrayList<Object>();
			for (Integer id : ids.values()) {
				paramList.add(id);
			}
			criteria.andIn(col, paramList);
		}

		if (!createConditionFromCache(function, example, criteria)) {
			return null;
		}

		if (function instanceof FunctionBean) {
			createConditionFromRequest((CrudBean) function, example, criteria, i);
		}

		// sort
		StringBuilder sb = new StringBuilder();
		String value = null;
		List<String> sortList = function.getSorts();
		if (sortList != null && sortList.size() > 0) {
			for (String sort : sortList) {
				value = getParameter(sort);
				if (!StringUtils.isEmpty(value)) {
					value.replace(";", "");
					if (sb.length() > 0) {
						sb.append(',');
					}
					if (value.endsWith("_")) { //$NON-NLS-1$
						sb.append(value.substring(0, value.length() - 1));
						sb.append(" desc"); //$NON-NLS-1$
					} else {
						sb.append(value);
					}
				}
			}
			example.setOrderByClause(sb.toString());
		}

		return example;
	}

	private boolean createConditionFromCache(CrudBean function, CommonExample example, Criteria criteria)
			throws WebAppOSException {
		if (function.getConds() != null) {
			for (ConditionBean cond : function.getConds()) {
				String[] values = null;
				String value = cond.getValue();
				value = getParameter(value);

				if (cond.getColumnName().startsWith(ControllerConstants.RESULT_MAP_KEY_SESSION)) {
					String[] keys = cond.getColumnName().split(ControllerConstants.REGEX_COLUMN_DELIMITER);
					String target = null;
					if (keys.length != 3) {
						throw new ExecuteDeniedException("err.204"); //$NON-NLS-1$
					}
					if (ControllerConstants.SESSION_KEY_LOGIN_USER.equalsIgnoreCase(keys[1])) {
						Object loginUser = this._httpSession.get(ControllerConstants.SESSION_KEY_LOGIN_USER);
						if (loginUser != null) {
							if (loginUser instanceof Map) {
								String authKey = AuthUtils.getAuthColumnName(keys[keys.length - 1]);
								if (((Map<?, ?>) loginUser).get(authKey) != null) {
									target = String.valueOf(((Map<?, ?>) loginUser).get(authKey));
								}
							}
							if (!isExecutable(target, value, cond.getOperator())) {
								throw new ExecuteDeniedException("err.204"); //$NON-NLS-1$
							}
							continue;
						}
					} else {
						throw new ExecuteDeniedException("err.204"); //$NON-NLS-1$
					}
				}

				values = new String[1];
				values[0] = value;
				DaoUtils.createCriteria(function.getResult(),
						function.getTarget(),
						cond.getColumnName(),
						cond.getOperator(),
						values,
						criteria,
						0);
			}
		}
		return true;
	}

	private void createConditionFromRequest(CrudBean function, CommonExample example, Criteria criteria, int i) throws WebAppOSException {
		for (Map.Entry<String, String[]> param : this._requestParams.entrySet()) {
			String[] values = param.getValue();
			String[] keys = param.getKey().split(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER, 5);
			if (keys.length < 4) {
				continue;
			}
			if (keys[0].equals(function.getResult())) {
				if (ControllerConstants.REQUEST_PARAM_NAME_CRUD_CONDITION.equals(keys[1])) {
					createCriteria(keys, values, function, criteria, i);
				}
			}
		}
	}

	private boolean isExecutable(String target, String value, String operator) throws NotFoundException {
		if (ControllerConstants.OPERATOR_NULL.equals(operator)) {
			if (target == null) {
				return true;
			}
		} else if (ControllerConstants.OPERATOR_NOT_NULL.equals(operator)) {
			if (target != null) {
				return true;
			}
		} else if (ControllerConstants.OPERATOR_EQUAL.equals(operator)) {
			if (target == null && value != null) {
				return false;
			}
			if (target == null && value == null) {
				return true;
			}
			if (target.equalsIgnoreCase(value)) {
				return true;
			}
		} else if (ControllerConstants.OPERATOR_NOT_EQUAL.equals(operator)) {
			if (target == null && value != null) {
				return true;
			}
			if (target == null && value == null) {
				return false;
			}
			if (!target.equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}

	private void createCriteria(String[] key, String[] values, CrudBean function, Criteria criteria, int i) throws WebAppOSException {
		// key=0:result識別子,1:cond,2:テーブル名,3:カラム名
		String resultName = key[0];
		String target = key[2];
		String column = target + '.' +  key[3];
		String operator = ((FunctionBean)function).getOperatorMap().get(resultName + '.' + column);
		DaoUtils.createCriteria(resultName, target, column, operator, values, criteria, i);
	}

	private void createDaoParamFromCache(CrudBean function, Map<String, Object> daoParams)
			throws WebAppOSException {
		if (function.getCols() != null) {
			for (ConditionBean col : function.getCols()) {
				String column = col.getColumnName();
				String value = col.getValue();
				value = getParameter(value);

				String[] names = column.split(ControllerConstants.REGEX_COLUMN_DELIMITER);
				if (names.length == 2) {
					column = column.toLowerCase();
				} else {
					column = function.getTarget().toLowerCase() + '.' + column.toLowerCase();
				}

				SchemaColumnBean schemaColumn = SchemaColumnCache.getSchemaColumn(
						column.replace(".", ControllerConstants.TABLE_COLUMN_DELIMITER)); //$NON-NLS-1$

				String daoParamKey = snakeToCamel(names[names.length - 1]);
				Object daoParamValue = convertStringToDbType(value, schemaColumn);
				daoParams.put(daoParamKey, daoParamValue);
			}
		}
	}

	private void createDaoParamFromRequest(CrudBean function, Map<String, Object> daoParams, int i)
			throws WebAppOSException {
		for (Map.Entry<String, String[]> param : this._requestParams.entrySet()) {
			String[] values = param.getValue();
			String[] keys = param.getKey().split(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER, 5);
			String daoParamKey = null;
			Object daoParamValue = null;
			if (keys.length < 3) {
				continue;
			}
			if (keys[0].equals(function.getResult())) {
				if (ControllerConstants.REQUEST_PARAM_NAME_CRUD_COLUMN.equals(keys[1])) {
					daoParamKey = snakeToCamel(keys[2]);
					SchemaColumnBean schemaColumn = SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(function.getTarget(), keys[2]));
					if (schemaColumn == null) {
						// TODO:schemaColumnがnullの場合、ワーニング表示
						continue;
					}
					String value = null;
					if (values == null || values.length == 0) {
						value = null;
					} else {
						if (values.length <= i) {
							value = null;
						} else if (values[i] == null) {
							value = null;
						} else if (values[i].length() == 0) {
							value = "";
						} else {
							value = values[i];
						}
					}
					daoParamValue = convertStringToDbType(value, schemaColumn);

					// TODO:一旦コメント。後で復活。
//					if (schemaColumn.isUnique()) {
//						if (DaoUtils.isConflict(this._sqlSession, schemaColumn, daoParamValue)) {
//							throw new ConflictException(
//									"err.203", schemaColumn.getTableName(), schemaColumn.getColumnName()); //$NON-NLS-1$
//						}
//					}
					daoParams.put(daoParamKey, daoParamValue);
				}
			}
		}
	}
}
