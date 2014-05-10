package jp.co.headwaters.webappos.controller.fuction;

import java.util.HashMap;
import java.util.Map;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.cache.SystemConstantCache;
import jp.co.headwaters.webappos.controller.cache.bean.AbstractExecuteBean;
import jp.co.headwaters.webappos.controller.cache.bean.CrudBean;
import jp.co.headwaters.webappos.controller.cache.bean.PagerBean;
import jp.co.headwaters.webappos.controller.cache.bean.SystemConstantBean;
import jp.co.headwaters.webappos.controller.exception.NotFoundException;
import jp.co.headwaters.webappos.controller.exception.WebAppOSException;
import jp.co.headwaters.webappos.controller.utils.AuthUtils;
import jp.co.headwaters.webappos.controller.utils.ControllerUtils;
import jp.co.headwaters.webappos.controller.utils.DaoUtils;
import jp.co.headwaters.webappos.controller.utils.PagingUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

public abstract class AbstractFunction {

	/** リクエストパラメータを保持するMap */
	protected Map<String, String[]> _requestParams;
	/** 処理結果を保持するMap */
	protected Map<String, Object> _resultMap;
	/** SqlSession */
	protected SqlSession _sqlSession;
	/** HttpSession */
	protected Map<String, Object> _httpSession;
	/** Each table id */
	protected Map<String, Map<Integer, Integer>> _ids = new HashMap<String, Map<Integer, Integer>>();

	public void execute(Map<String, String[]> requestParams, Map<String, Object> resultMap, AbstractExecuteBean function) throws Exception {

		this._requestParams = requestParams;
		this._resultMap = resultMap;

		initialize();

		if (this._sqlSession == null){
			this._sqlSession = DaoUtils.openSqlSession();
		}

		execute(function);
	}

	public void setSqlSession(SqlSession session) {
		this._sqlSession = session;
	}

	public void setHttpSession(Map<String, Object> session) {
		this._httpSession = session;
	}

	public void setIds(Map<String, Map<Integer, Integer>> ids) {
		this._ids = ids;
	}

	protected void initialize() throws Exception {
		// nothing
	}

	protected abstract void execute(AbstractExecuteBean function) throws Exception;

	public String getParameter(String key) throws WebAppOSException {
		String value = null;
		if (key == null) {
			return null;
		}

		if (key.startsWith(ControllerConstants.RESULT_MAP_KEY_CONSTANT)) {
			String[] keys = key.split(ControllerConstants.REGEX_COLUMN_DELIMITER);
			if (keys.length < 2) {
				return null;
			}
			if (keys.length == 2) {
				SystemConstantBean systemConstant = SystemConstantCache.getSystemConstant(keys[1]);
				if (systemConstant == null) {
					throw new WebAppOSException("err.001", "", keys[1]); //$NON-NLS-1$ //$NON-NLS-2$
				}
				value = SystemConstantCache.getSystemConstant(keys[1]).getValue();
			} else {
				SystemConstantBean systemConstant = SystemConstantCache.getSystemConstant(keys[1], keys[2]);
				if (systemConstant == null) {
					throw new WebAppOSException("err.001", keys[1], keys[2]); //$NON-NLS-1$
				}
				value = SystemConstantCache.getSystemConstant(keys[1], keys[2]).getValue();
			}
		} else if (key.startsWith(ControllerConstants.RESULT_MAP_KEY_REQUEST)) {
			String[] keys = key.split(ControllerConstants.REGEX_COLUMN_DELIMITER);
			if (keys.length < 1) {
				return null;
			}
			if (this._requestParams.get(keys[1]) != null) {
				value = this._requestParams.get(keys[1])[0];
			}
		} else if (key.startsWith(ControllerConstants.RESULT_MAP_KEY_SESSION)) {
			String[] keys = key.split(ControllerConstants.REGEX_COLUMN_DELIMITER);
			if (keys.length != 3) {
				return null;
			}
			if (ControllerConstants.SESSION_KEY_LOGIN_USER.equalsIgnoreCase(keys[1])) {
				Object loginUser = this._httpSession.get(ControllerConstants.SESSION_KEY_LOGIN_USER);
				if (loginUser != null) {
					if (loginUser instanceof Map) {
						String authKey = AuthUtils.getAuthColumnName(keys[keys.length - 1]);
						value = String.valueOf(((Map<?, ?>) loginUser).get(authKey));
					}
				}
			}
		} else {
			if (key.split("\\.").length == 3) {
				try {
					key = key.toUpperCase();
					String[] keys = key.split("\\.");
					if (keys[1].equalsIgnoreCase(ControllerConstants.RESULT_MAP_KEY_PAGER)) {
						StringBuilder sb = new StringBuilder();
						sb.append(ControllerConstants.RESULT_MAP_KEY_PAGER);
						sb.append(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER);
						sb.append(keys[0].toUpperCase());
						if (this._resultMap.containsKey(sb.toString())) {
							Object map = this._resultMap.get(sb.toString());
							if (map instanceof Map) {
								value = ((Map<?, ?>) map).get(keys[2]).toString();
							}
						}
					} else {
						if (this._resultMap.containsKey(keys[0])) {
							Object map = this._resultMap.get(keys[0]);
							if (map instanceof Map) {
								if (!StringUtils.isEmpty(keys[1])) {
									value = (String) ((Map<?, ?>) map).get(ControllerUtils.getResultMapKey(keys[1], keys[2]));
								} else {
									value = (String) ((Map<?, ?>) map).get(keys[2]);
								}
							}
						}
					}
				} catch (Exception e){
					value = null;
				}
			} else {
				value = key;
			}
		}
		return value;
	}

	protected RowBounds createRowBounds(CrudBean function, Integer count) throws NumberFormatException, NotFoundException {
		RowBounds rowBounds = null;
		int offset = 0;
		int limit = 0;
		PagerBean pager = function.getPager();
		if (pager != null) {
			int pageNo = 0;
			String paramName = function.getPager().getParamName();
			if (this._requestParams.get(paramName) != null
					&& this._requestParams.get(paramName).length > 0
					&& !StringUtils.isEmpty(this._requestParams.get(paramName)[0])) {
				pageNo = Integer.parseInt(this._requestParams.get(paramName)[0]);
			} else {
				pageNo = ControllerConstants.DEFAULT_PEGE_NO;
			}
			PagingUtils pageInfo = new PagingUtils(
											this._resultMap,
											function.getResult(),
											count,
											pageNo,
											Integer.parseInt(pager.getPerPage()),
											Integer.parseInt(pager.getPagerCount()));
			offset = pageInfo.recordBeginNo - 1;
			limit = pageInfo.perPage;
		} else {
			if (!StringUtils.isEmpty(function.getOffset())) {
				offset = Integer.parseInt(function.getOffset());
			}
			if (!StringUtils.isEmpty(function.getLimit())) {
				limit = Integer.parseInt(function.getLimit());
			}
		}
		if (offset != 0 || limit != 0) {
			rowBounds = new RowBounds(offset, limit);
		}
		return rowBounds;
	}
}
