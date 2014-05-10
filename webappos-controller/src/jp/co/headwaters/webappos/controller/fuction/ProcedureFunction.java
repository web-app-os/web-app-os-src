package jp.co.headwaters.webappos.controller.fuction;

import static jp.co.headwaters.webappos.controller.utils.ConvertDateTypeUtils.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.cache.WebAppOSCache;
import jp.co.headwaters.webappos.controller.cache.bean.AbstractExecuteBean;
import jp.co.headwaters.webappos.controller.cache.bean.CrudBean;
import jp.co.headwaters.webappos.controller.cache.bean.FunctionBean;
import jp.co.headwaters.webappos.controller.cache.bean.LoadBean;
import jp.co.headwaters.webappos.controller.cache.bean.ProcedureArgBean;
import jp.co.headwaters.webappos.controller.cache.bean.ProcedureInfoBean;
import jp.co.headwaters.webappos.controller.enumation.DataTypeEnum;
import jp.co.headwaters.webappos.controller.exception.NotFoundException;
import jp.co.headwaters.webappos.controller.exception.WebAppOSException;
import jp.co.headwaters.webappos.controller.utils.ConvertDateTypeUtils;
import jp.co.headwaters.webappos.controller.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;

public class ProcedureFunction extends AbstractFunction {

	@Override
	protected void execute(AbstractExecuteBean function)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, WebAppOSException {
		CrudBean crudFunction = (CrudBean)function;
		ProcedureInfoBean procedureInfo = WebAppOSCache.getInstance().getProcedureMap().get(crudFunction.getTarget().toLowerCase());
		executeProcedure(crudFunction, procedureInfo);
	}

	private void executeProcedure(CrudBean function, ProcedureInfoBean procedureInfo) throws WebAppOSException {
		String resultName = function.getResult();
		String mapperName = getMapperName(procedureInfo.getName());
		Map<String, Object> params = createParams(function, procedureInfo);

		List<?> entityList = null;
		if (procedureInfo.getArgNum() == 0) {
			entityList = this._sqlSession.selectList(mapperName);
		} else {
			entityList = this._sqlSession.selectList(mapperName, params);
		}

		if (!DataTypeEnum.DATA_TYPE_VOID.equals(procedureInfo.getRetType())) {
			if (!entityList.isEmpty()) {
				if (DataTypeEnum.DATA_TYPE_RECORD.equals(procedureInfo.getRetType())) {
					HashMap<String, List<HashMap<String, Object>>> rootMap = new HashMap<String, List<HashMap<String, Object>>>();
					List<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
					rootMap.put(function.getTarget(), resultList);
					this._resultMap.put(resultName, rootMap);

					RowBounds rowBounds = null;
					try {
						rowBounds = createRowBounds(function, entityList.size());
					} catch (NotFoundException e) {
						if (function instanceof LoadBean){
							if (((LoadBean) function).isNotFooundError()) {
								throw new NotFoundException();
							}
						}
						this._resultMap.put(resultName, new ArrayList<HashMap<String, String>>());
						return;
					}
					if (rowBounds != null) {
						int fromIndex = entityList.size() < rowBounds.getOffset() ? entityList.size() : rowBounds.getOffset();
						int toIndex = entityList.size() < fromIndex + rowBounds.getLimit() ? entityList.size() : fromIndex + rowBounds.getLimit();
						entityList = entityList.subList(fromIndex, toIndex);
					}

					for (int i = 0; i < entityList.size(); i++) {
						HashMap<String, Object> record = new HashMap<String, Object>();
						@SuppressWarnings("unchecked")
						HashMap<String, Object> element = (HashMap<String, Object>) entityList.get(i);
						for (Entry<String, Object> obj : element.entrySet()) {
							record.put(obj.getKey().toUpperCase(), obj.getValue());
						}
						resultList.add(record);
					}
				} else {
					HashMap<String, String> record = new HashMap<String, String>();
					if (procedureInfo.getOutNames().size() == 0) {
						record.put(procedureInfo.getName().toUpperCase(), convertDbTypeToString(entityList.get(0)));
					} else {
						record.put(procedureInfo.getOutNames().get(0).toUpperCase(), convertDbTypeToString(entityList.get(0)));
					}
					this._resultMap.put(resultName, record);
				}
			} else {
				createRowBounds(function, 0);
				this._resultMap.put(resultName, new ArrayList<HashMap<String, String>>());
				if (function instanceof LoadBean){
					if (((LoadBean) function).isNotFooundError()) {
						throw new NotFoundException();
					}
				}
			}
		}
	}

	private String getMapperName(String target) {
		StringBuilder sb = new StringBuilder();
		sb.append(PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_ROOT_PACKAGE));
		sb.append('.');
		sb.append(ControllerConstants.MYBATIS_MAPPER_PACKAGE);
		sb.append('.');
		sb.append(ControllerConstants.PROCEDURE_MAPPER_NAME);
		sb.append('.');
		sb.append(target);
		return sb.toString();
	}

	private Map<String, Object> createParams(CrudBean function, ProcedureInfoBean procedureInfo)
			throws WebAppOSException {
		Map<String, Object> params = new HashMap<String, Object>();
		if (!createParamFromCache(function, procedureInfo, params)) {
			return null;
		}

		if (function instanceof FunctionBean) {
			createParamFromRequest((CrudBean) function, procedureInfo, params);
		}
		return params;
	}

	private boolean createParamFromCache(CrudBean function, ProcedureInfoBean procedureInfo, Map<String, Object> params)
			throws WebAppOSException {
		if (function.getArgs() != null) {
			for (ProcedureArgBean arg : function.getArgs()) {
				String value = arg.getValue();
				value = getParameter(value);
				if (value == null) {
					//throw new WebAppOSException("err.400", null); //$NON-NLS-1$
				}
				if (!StringUtils.isEmpty(value)) {
					params.put(arg.getName().toLowerCase(),
							ConvertDateTypeUtils.convertStringToDbType(value, procedureInfo.getArgs().get(arg.getName().toLowerCase()).getDataType()));
				} else {
					params.put(arg.getName().toLowerCase(), null);
				}
			}
		}

		return true;
	}

	private void createParamFromRequest(CrudBean function, ProcedureInfoBean procedureInfo, Map<String, Object> params)
			throws WebAppOSException {
		for (Map.Entry<String, String[]> reqParam : this._requestParams.entrySet()) {
			String[] values = reqParam.getValue();
			String[] keys = reqParam.getKey().split(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER, 5);
			if (keys.length < 3) {
				continue;
			}
			if (keys[0].equals(function.getResult())) {
				if (ControllerConstants.REQUEST_PARAM_NAME_CRUD_COLUMN.equals(keys[1])) {
					params.put(keys[2].toLowerCase(),
							ConvertDateTypeUtils.convertStringToDbType(values[0], procedureInfo.getArgs().get(keys[2].toLowerCase()).getDataType()));
				}
			}
		}
	}
}
