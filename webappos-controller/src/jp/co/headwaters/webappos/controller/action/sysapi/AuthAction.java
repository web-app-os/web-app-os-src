package jp.co.headwaters.webappos.controller.action.sysapi;

import static jp.co.headwaters.webappos.controller.utils.ControllerUtils.*;
import static jp.co.headwaters.webappos.controller.utils.ConvertDateTypeUtils.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.action.AbstractAction;
import jp.co.headwaters.webappos.controller.cache.SchemaColumnCache;
import jp.co.headwaters.webappos.controller.cache.bean.FunctionBean;
import jp.co.headwaters.webappos.controller.enumation.CrudEnum;
import jp.co.headwaters.webappos.controller.enumation.FunctionEnum;
import jp.co.headwaters.webappos.controller.fuction.SendMailFunction;
import jp.co.headwaters.webappos.controller.model.CommonExample;
import jp.co.headwaters.webappos.controller.model.CommonExample.Criteria;
import jp.co.headwaters.webappos.controller.oauth.AbstractOAuthClient;
import jp.co.headwaters.webappos.controller.oauth.OAuthClientFactory;
import jp.co.headwaters.webappos.controller.session.OAuthBean;
import jp.co.headwaters.webappos.controller.utils.AuthUtils;
import jp.co.headwaters.webappos.controller.utils.ControllerUtils;
import jp.co.headwaters.webappos.controller.utils.DaoUtils;
import jp.co.headwaters.webappos.controller.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;

@SuppressWarnings("serial")
public class AuthAction extends AbstractAction {

	private static final Log _logger = LogFactory.getLog(AuthAction.class);

	public String oauth() throws Exception {
		authorization(null);
		return null;
	}

	public String callback() throws Exception {
		OAuthBean oAuthData = (OAuthBean) this._session.get(OAuthBean.getKey());
		String provider = oAuthData.getProviderName();
		AbstractOAuthClient client = OAuthClientFactory.create(provider);
		client.callback(this._request, this._response, oAuthData);
		return null;
	}

	public String users() throws Exception {
		String provider = this._request.getParameter(ControllerConstants.API_PARAM_OAUTH_SP);
		OAuthBean oAuthData = (OAuthBean) this._session.get(OAuthBean.getKey());
		if (oAuthData == null) {
			this._response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
		AbstractOAuthClient client = OAuthClientFactory.create(provider);
		client.users(this._request, this._response, oAuthData);
		return null;
	}

	public String connect() throws Exception {
		@SuppressWarnings("unchecked")
		HashMap<String, Object> user = (HashMap<String, Object>) this._session.get(ControllerConstants.SESSION_KEY_LOGIN_USER);
		if (user == null) {
			this._response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
		String userId = (String)user.get(AuthUtils.getAuthColumnName(ControllerConstants.PK_COLUMN_NAME));
		if (userId == null) {
			this._response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
		authorization(userId);
		return null;
	}

	public String disconnect() throws Exception {
//		@SuppressWarnings("unchecked")
//		HashMap<String, Object> user = (HashMap<String, Object>) this._session.get(ControllerConstants.SESSION_KEY_LOGIN_USER);
//		if (user == null) {
//			this._response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//			return null;
//		}
//		String userId = (String)user.get(AuthUtils.getAuthColumnName(ControllerConstants.PK_COLUMN_NAME));
//		if (userId == null) {
//			this._response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//			return null;
//		}
//		String provider = this._request.getParameter(ControllerConstants.API_PARAM_OAUTH_SP);
//		OAuthBean oAuthData = (OAuthBean) this._session.get(OAuthBean.getKey());
//		if (oAuthData != null) {
//			oAuthData.setAccessToken(null);
//		}
//		AbstractOAuthClient client = OAuthClientFactory.create(provider);
//		client.disconnect(this._request, this._response, provider, userId);
		return null;
	}

	public String login() throws Exception {
		String id = this._request.getParameter(ControllerConstants.API_PARAM_LOGIN_ID);
		String pw = this._request.getParameter(ControllerConstants.API_PARAM_LOGIN_PASSWORD);
		String auto = this._request.getParameter(ControllerConstants.API_PARAM_LOGIN_AUTO_LOGIN);

		String succsessUri = this._request.getParameter(ControllerConstants.API_PARAM_OAUTH_SUCCESS_REDIRECT_URI);
		String failuretUri = this._request.getParameter(ControllerConstants.API_PARAM_OAUTH_FAILURE_REDIRECT_URI);
		String redirectUri = (String) this._session.get(ControllerConstants.SESSION_KEY_LOGIN_REDIRECT_URI);

		if (StringUtils.isEmpty(id) || StringUtils.isEmpty(pw)) {
			this._response.sendRedirect(ControllerUtils.getRedirectUri(failuretUri));
			return null;
		}

		SqlSession sqlSession = null;
		try {
			sqlSession = DaoUtils.openSqlSession();
			String mapperName = DaoUtils.getMapperName(CrudEnum.SELECT_BY_EXAMPLE.getMethod(),
					PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_TABLE_NAME));
			CommonExample example = new CommonExample();
			Criteria criteria = example.createCriteria();

			String authTableName = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_TABLE_NAME);
			String authIdColName = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_ID_COLUMN_NAME);
			String authPwColName = PropertyUtils
					.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_PASSWORD_COLUMN_NAME);

			Object dbId = convertStringToDbType(id,
					SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(authTableName, authIdColName)));
			Object dbPw = convertStringToDbType(pw,
					SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(authTableName, authPwColName)));

			criteria.andEqualTo(authIdColName, dbId);
			criteria.andEqualTo(authPwColName, dbPw);

			String condName = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_COND_NAMES);
			String condOperator = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_COND_OPERATORS);
			String condValue = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_COND_VALUES);

			if (condName != null && condValue != null && condOperator != null) {
				String[] condNames = condName.split(","); //$NON-NLS-1$
				String[] condOperators = condOperator.split(","); //$NON-NLS-1$
				String[] condValues = condValue.split(","); //$NON-NLS-1$
				for (int i = 0; i < condNames.length; i++) {
					String name = condNames[i];
					String operator = condOperators[i];
					String value = condValues[i];
					DaoUtils.createCriteria(null, authTableName, name, operator, new String[] { value }, criteria, 0);
				}
			}

			List<?> entityList = sqlSession.selectList(mapperName, example);
			HashMap<String, Object> user = null;
			if (entityList.size() == 1) {
				user = DaoUtils.convertEntityToMap(entityList.get(0), true);
				HttpSession httpSession = this._request.getSession(false);
				if (httpSession != null) {
					httpSession.invalidate();
				}
				httpSession = this._request.getSession(true);
				httpSession.setAttribute(ControllerConstants.SESSION_KEY_LOGIN_USER, user);
			}
			if (user == null) {
				this._response.sendRedirect(ControllerUtils.getRedirectUri(failuretUri));
				return null;
			} else {
				if (redirectUri == null) {
					redirectUri = ControllerUtils.getRedirectUri(succsessUri);
				} else {
					redirectUri = ControllerUtils.getRedirectUri(redirectUri);
				}

				if (!StringUtils.isEmpty(auto)) {
					AuthUtils.addAutoLoginKey(this._request, this._response, sqlSession, user);
				}
			}
			DaoUtils.commit(sqlSession);
			this._response.sendRedirect(redirectUri);
		} finally {
			DaoUtils.closeSqlSession(sqlSession);
		}
		return null;
	}

	public String autologin() throws Exception {
		OAuthBean oAuthData = (OAuthBean) this._session.get(OAuthBean.getKey());
		String provider = oAuthData.getProviderName();
		String uid = oAuthData.getUid();
		AbstractOAuthClient client = OAuthClientFactory.create(provider);
		SqlSession sqlSession = null;
		try {
			sqlSession = DaoUtils.openSqlSession();
			HashMap<String, Object> user = client.getUserData(sqlSession, provider, uid);
			if (user == null || user.get(AuthUtils.getAuthColumnName(ControllerConstants.PK_COLUMN_NAME)) == null) {
				this._response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return null;
			}
			oAuthData.setUserId((String) user.get(AuthUtils.getAuthColumnName(ControllerConstants.PK_COLUMN_NAME)));
			this._session.put(ControllerConstants.SESSION_KEY_LOGIN_USER, user);
		} finally {
			DaoUtils.closeSqlSession(sqlSession);
		}
		return null;
	}

	public String logout() throws Exception {
		try {
			AuthUtils.removeAutoLoginKey(this._request, this._response);
			HttpSession httpSession = this._request.getSession(false);
			if (httpSession != null) {
				httpSession.invalidate();
			}
			this._response.sendRedirect("/"); //$NON-NLS-1$
		} catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}
		return null;
	}

	public String resetpw() throws Exception {
		if (!validateToken()) {
			this._response.sendRedirect("/");
			return null;
		}

		String email = this._request.getParameter(ControllerConstants.API_PARAM_EMAIL);
		String succsessUri = this._request.getParameter(ControllerConstants.API_PARAM_OAUTH_SUCCESS_REDIRECT_URI);
		String notfoundUri = this._request.getParameter(ControllerConstants.API_PARAM_OAUTH_NOTFOUND_REDIRECT_URI);

		if (StringUtils.isEmpty(email)) {
			this._response.sendRedirect(ControllerUtils.getRedirectUri(notfoundUri));
			return null;
		}

		SqlSession sqlSession = null;
		try {
			sqlSession = DaoUtils.openSqlSession(false);

			String authTableName = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_TABLE_NAME);
			String emailColName = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_EMAIL_COLUMN_NAME);
			String tokenColName = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_TOKEN_COLUMN_NAME);

			// トークンを更新
			String mapperName = DaoUtils.getMapperName(CrudEnum.UPDATE.getMethod(), authTableName);
			Map<String, Object> daoParams = new HashMap<String, Object>();
			Object dbEmail = convertStringToDbType(email,
					SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(authTableName, emailColName)));
			Object dbToken = convertStringToDbType(null,
					SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(authTableName, tokenColName)));

			daoParams.put(snakeToCamel(tokenColName), dbToken);
			daoParams.put(snakeToCamel(ControllerConstants.UPDATED_COLUMN_NAME),
					new Timestamp(System.currentTimeMillis()));
			Map<String, Object> updateMap = new HashMap<String, Object>();
			updateMap.put(ControllerConstants.MYBATIS_MAP_KEY_RECORD, daoParams);

			CommonExample example = new CommonExample();
			Criteria criteria = example.createCriteria();
			criteria.andEqualTo(emailColName, dbEmail);
			updateMap.put(ControllerConstants.MYBATIS_MAP_KEY_EXAMPLE, example);

			int result = sqlSession.update(mapperName, updateMap);
			if (result == 0) {
				this._response.sendRedirect(ControllerUtils.getRedirectUri(notfoundUri));
				return null;
			}

			// ユーザ情報を取得
			mapperName = DaoUtils.getMapperName(CrudEnum.SELECT_BY_EXAMPLE.getMethod(),
					PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_TABLE_NAME));
			example = new CommonExample();
			criteria = example.createCriteria();
			criteria.andEqualTo(emailColName, dbEmail);
			List<?> entityList = sqlSession.selectList(mapperName, example);
			HashMap<String, Object> user = null;
			if (entityList.size() == 1) {
				user = DaoUtils.convertEntityToMap(entityList.get(0), true);
			}
			if (user == null) {
				this._response.sendRedirect(ControllerUtils.getRedirectUri(notfoundUri));
				DaoUtils.rollback(sqlSession);
			}

			// メールを送信
			FunctionBean bean = new FunctionBean();
			bean.setType(FunctionEnum.FUNCTION_SENDMAIL.getFunctionName());
			bean.setToIdColumnName(authTableName + '.' + ControllerConstants.PK_COLUMN_NAME);
			bean.setToAddressColumnName(authTableName + '.' + emailColName);
			bean.setToIds(new String[] { (String) user.get(getSchemaColumnKey(authTableName,
					ControllerConstants.PK_COLUMN_NAME)) });
			bean.setMethod("resetpassword".toUpperCase());
			SendMailFunction function = new SendMailFunction();
			function.setSqlSession(sqlSession);
			function.execute(null, null, bean);
			DaoUtils.commit(sqlSession);
			this._response.sendRedirect(succsessUri);
		} finally {
			DaoUtils.closeSqlSession(sqlSession);
		}
		return null;
	}

	public String clearAutoLogin() throws Exception {
		@SuppressWarnings("unchecked")
		HashMap<String, Object> user = (HashMap<String, Object>) this._session.get(ControllerConstants.SESSION_KEY_LOGIN_USER);
		if (user == null) {
			this._response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
		String userId = (String)user.get(AuthUtils.getAuthColumnName(ControllerConstants.PK_COLUMN_NAME));
		if (userId == null) {
			this._response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		SqlSession sqlSession = null;
		try {
			sqlSession = DaoUtils.openSqlSession(false);

			Object dbUserId = convertStringToDbType(userId,
					SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(ControllerConstants.TABLE_NAME_AUTO_LOGIN, ControllerConstants.COLUMN_NAME_AUTO_LOGIN_USER_ID)));
			CommonExample example = new CommonExample();
			Criteria criteria = example.createCriteria();
			criteria.andEqualTo(ControllerConstants.COLUMN_NAME_AUTO_LOGIN_USER_ID, dbUserId);
			String mapperName = DaoUtils.getMapperName(CrudEnum.DELETE.getMethod(), ControllerConstants.TABLE_NAME_AUTO_LOGIN);
			sqlSession.delete(mapperName, example);
			DaoUtils.commit(sqlSession);
		} finally {
			DaoUtils.closeSqlSession(sqlSession);
		}
		return null;
	}

	public String modifyId() throws Exception {
		if (!validateToken()) {
			this._response.sendRedirect("/");
			return null;
		}

		@SuppressWarnings("unchecked")
		HashMap<String, Object> user = (HashMap<String, Object>) this._session
				.get(ControllerConstants.SESSION_KEY_LOGIN_USER);
		String userId = (String) user.get(AuthUtils.getAuthColumnName(ControllerConstants.PK_COLUMN_NAME));
		String tempId = this._request.getParameter(ControllerConstants.API_PARAM_TEMP_ID);
		String succsessUri = this._request.getParameter(ControllerConstants.API_PARAM_OAUTH_SUCCESS_REDIRECT_URI);
		String ngUri = this._request.getParameter(ControllerConstants.API_PARAM_OAUTH_NG_REDIRECT_URI);

		SqlSession sqlSession = null;
		try {
			sqlSession = DaoUtils.openSqlSession(false);

			String authTableName = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_TABLE_NAME);
			String emailColName = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_EMAIL_COLUMN_NAME);
			Object dbEmail = convertStringToDbType(tempId,
					SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(authTableName, emailColName)));

			String mapperName = DaoUtils.getMapperName(CrudEnum.SELECT_BY_EXAMPLE.getMethod(), authTableName);
			CommonExample example = new CommonExample();
			Criteria criteria = example.createCriteria();
			criteria.andEqualTo(emailColName, dbEmail);
			List<?> entityList = sqlSession.selectList(mapperName, example);
			if (entityList.size() > 0) {
				this._response.sendRedirect(ControllerUtils.getRedirectUri(ngUri));
				DaoUtils.rollback(sqlSession);
				return null;
			}

			// トークンを更新
			String tempIdColName = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_TEMP_ID_COLUMN_NAME);
			String tokenColName = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_TOKEN_COLUMN_NAME);
			mapperName = DaoUtils.getMapperName(CrudEnum.UPDATE.getMethod(), authTableName);
			Map<String, Object> daoParams = new HashMap<String, Object>();
			Object dbId = convertStringToDbType(userId, SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(
					authTableName, ControllerConstants.PK_COLUMN_NAME)));
			Object dbTempId = convertStringToDbType(tempId,
					SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(authTableName, tempIdColName)));
			Object dbToken = convertStringToDbType(null,
					SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(authTableName, tokenColName)));
			daoParams.put(snakeToCamel(tempIdColName), dbTempId);
			daoParams.put(snakeToCamel(tokenColName), dbToken);
			daoParams.put(snakeToCamel(ControllerConstants.UPDATED_COLUMN_NAME),
					new Timestamp(System.currentTimeMillis()));
			Map<String, Object> updateMap = new HashMap<String, Object>();
			updateMap.put(ControllerConstants.MYBATIS_MAP_KEY_RECORD, daoParams);
			example = new CommonExample();
			criteria = example.createCriteria();
			criteria.andEqualTo(ControllerConstants.PK_COLUMN_NAME, dbId);
			updateMap.put(ControllerConstants.MYBATIS_MAP_KEY_EXAMPLE, example);
			sqlSession.update(mapperName, updateMap);

			// ユーザ情報を取得
			mapperName = DaoUtils.getMapperName(CrudEnum.SELECT_BY_EXAMPLE.getMethod(),
					PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_TABLE_NAME));
			example = new CommonExample();
			criteria = example.createCriteria();
			criteria.andEqualTo(ControllerConstants.PK_COLUMN_NAME, dbId);
			entityList = sqlSession.selectList(mapperName, example);
			user = null;
			if (entityList.size() == 1) {
				user = DaoUtils.convertEntityToMap(entityList.get(0), true);
			}
			if (user == null) {
				DaoUtils.rollback(sqlSession);
			}

			// メールを送信
			FunctionBean bean = new FunctionBean();
			bean.setType(FunctionEnum.FUNCTION_SENDMAIL.getFunctionName());
			bean.setToIdColumnName(authTableName + '.' + ControllerConstants.PK_COLUMN_NAME);
			bean.setToAddressColumnName(authTableName + '.' + tempIdColName);
			bean.setToIds(new String[] { (String) user.get(getSchemaColumnKey(authTableName,
					ControllerConstants.PK_COLUMN_NAME)) });
			bean.setMethod("modifyEmail".toUpperCase());
			SendMailFunction function = new SendMailFunction();
			function.setSqlSession(sqlSession);
			function.execute(null, null, bean);
			DaoUtils.commit(sqlSession);

			HttpSession httpSession = this._request.getSession(false);
			if (httpSession != null) {
				httpSession.invalidate();
			}
			this._response.sendRedirect(succsessUri);
		} finally {
			DaoUtils.closeSqlSession(sqlSession);
		}
		return null;
	}

	private void authorization(String userId) throws Exception {
		String provider = this._request.getParameter(ControllerConstants.API_PARAM_OAUTH_SP);
		String succsessUri = this._request.getParameter(ControllerConstants.API_PARAM_OAUTH_SUCCESS_REDIRECT_URI);
		String failuretUri = this._request.getParameter(ControllerConstants.API_PARAM_OAUTH_FAILURE_REDIRECT_URI);
		String notfoundUri = this._request.getParameter(ControllerConstants.API_PARAM_OAUTH_NOTFOUND_REDIRECT_URI);
		String auto = this._request.getParameter(ControllerConstants.API_PARAM_LOGIN_AUTO_LOGIN);
		succsessUri = ControllerUtils.getRedirectUri(succsessUri);
		failuretUri = ControllerUtils.getRedirectUri(failuretUri);
		notfoundUri = ControllerUtils.getRedirectUri(notfoundUri);

		OAuthBean oAuthData = new OAuthBean();
		oAuthData.setSuccessUri(succsessUri);
		oAuthData.setFailuretUri(failuretUri);
		oAuthData.setNotfoundUri(notfoundUri);
		oAuthData.setProviderName(provider);
		oAuthData.setUserId(userId);
		oAuthData.setAutoLogin(!StringUtils.isEmpty(auto));

		this._session.put(OAuthBean.getKey(), oAuthData);
		AbstractOAuthClient client = OAuthClientFactory.create(provider);
		client.oauth(this._request, this._response, oAuthData);
	}
}