package jp.co.headwaters.webappos.controller.utils;

import static jp.co.headwaters.webappos.controller.utils.ControllerUtils.*;
import static jp.co.headwaters.webappos.controller.utils.ConvertDateTypeUtils.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.cache.SchemaColumnCache;
import jp.co.headwaters.webappos.controller.cache.bean.SchemaColumnBean;
import jp.co.headwaters.webappos.controller.enumation.CrudEnum;
import jp.co.headwaters.webappos.controller.exception.WebAppOSException;
import jp.co.headwaters.webappos.controller.model.CommonExample;
import jp.co.headwaters.webappos.controller.model.CommonExample.Criteria;
import jp.co.headwaters.webappos.controller.security.Cipher;
import jp.co.headwaters.webappos.controller.security.CipherFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;

public class AuthUtils {

	private static final Log _logger = LogFactory.getLog(AuthUtils.class);

	public static String getAuthColumnName(String columnName) {
		return getSchemaColumnKey(PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_TABLE_NAME), columnName);
	}

	public static void registerOAuthData(SqlSession sqlSession, String provider, String userId, String uid) throws WebAppOSException {
		Map<String, Object> daoParams = new HashMap<String, Object>();
		CommonExample example = new CommonExample();
		Criteria criteria = example.createCriteria();

		String mapperName = DaoUtils.getMapperName(CrudEnum.COUNT.getMethod(), ControllerConstants.TABLE_NAME_OAUTH_MANAGE);
		Object dbProvider = convertStringToDbType(provider,
				SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(ControllerConstants.TABLE_NAME_OAUTH_MANAGE, ControllerConstants.COLUMN_NAME_OAUTH_MANAGE_PROVIDER)));
		Object dbUserId = convertStringToDbType(userId,
				SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(ControllerConstants.TABLE_NAME_OAUTH_MANAGE, ControllerConstants.COLUMN_NAME_OAUTH_MANAGE_USER_ID)));
		Object dbUid = convertStringToDbType(uid,
				SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(ControllerConstants.TABLE_NAME_OAUTH_MANAGE, ControllerConstants.COLUMN_NAME_OAUTH_MANAGE_UID)));

		criteria.andEqualTo(ControllerConstants.COLUMN_NAME_OAUTH_MANAGE_PROVIDER, dbProvider);
		criteria.andEqualTo(ControllerConstants.COLUMN_NAME_OAUTH_MANAGE_UID, dbUid);
		Integer count = sqlSession.selectOne(mapperName, example);
		if (count == 0) {
			mapperName = DaoUtils.getMapperName(CrudEnum.INSERT.getMethod(), ControllerConstants.TABLE_NAME_OAUTH_MANAGE);
			daoParams.put(snakeToCamel(ControllerConstants.COLUMN_NAME_OAUTH_MANAGE_PROVIDER), dbProvider);
			daoParams.put(snakeToCamel(ControllerConstants.COLUMN_NAME_OAUTH_MANAGE_USER_ID), dbUserId);
			daoParams.put(snakeToCamel(ControllerConstants.COLUMN_NAME_OAUTH_MANAGE_UID), dbUid);
			daoParams.put(snakeToCamel(ControllerConstants.CREATED_COLUMN_NAME), new Timestamp(System.currentTimeMillis()));
			daoParams.put(snakeToCamel(ControllerConstants.UPDATED_COLUMN_NAME), new Timestamp(System.currentTimeMillis()));
			Map<String, Object> insertMap = new HashMap<String, Object>();
			insertMap.put(ControllerConstants.MYBATIS_MAP_KEY_RECORD, daoParams);
			sqlSession.insert(mapperName, insertMap);
		} else {
			// TODO:取り急ぎ、更新できない様にする
			throw new WebAppOSException("err.504",dbProvider.toString(), dbUid.toString()); //$NON-NLS-1$
//			mapperName = DaoUtils.getMapperName(CrudEnum.UPDATE.getMethod(), ControllerConstants.TABLE_NAME_OAUTH_MANAGE);
//			daoParams.put(snakeToCamel(ControllerConstants.COLUMN_NAME_OAUTH_MANAGE_USER_ID), dbUserId);
//			daoParams.put(snakeToCamel(ControllerConstants.UPDATED_COLUMN_NAME), new Timestamp(System.currentTimeMillis()));
//			Map<String, Object> updateMap = new HashMap<String, Object>();
//			updateMap.put(ControllerConstants.MYBATIS_MAP_KEY_RECORD, daoParams);
//			updateMap.put(ControllerConstants.MYBATIS_MAP_KEY_EXAMPLE, example);
//			sqlSession.update(mapperName, updateMap);
		}
	}

	public static Map<String, Object> refreshLoginUserData(SqlSession sqlSession, Map<String, Object> loginUser) throws WebAppOSException{
		Map<String, Object> user = null;
		String authKey = AuthUtils.getAuthColumnName(ControllerConstants.PK_COLUMN_NAME);
		if (loginUser == null) return null;

		String id = (String) loginUser.get(authKey);
		try{
			String mapperName = DaoUtils.getMapperName(CrudEnum.SELECT_BY_EXAMPLE.getMethod(), PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_TABLE_NAME));
			CommonExample example = new CommonExample();
			Criteria criteria = example.createCriteria();

			String authTableName = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_TABLE_NAME);

			Object dbId = convertStringToDbType(id, SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(authTableName, ControllerConstants.PK_COLUMN_NAME)));

			criteria.andEqualTo(ControllerConstants.PK_COLUMN_NAME, dbId);

			List<?> entityList = sqlSession.selectList(mapperName, example);
			if (entityList.size() == 1) {
				user = DaoUtils.convertEntityToMap(entityList.get(0), true);
			} else {
				throw new WebAppOSException("err.503");
			}
		} catch (Exception e) {
			_logger.error(e.getMessage(), e);
			throw new WebAppOSException("err.503");
		}
		return user;
	}

	public static boolean autoLogin(HttpServletRequest req, HttpServletResponse res) {
		SqlSession sqlSession = null;
		try {
			// 自動ログインチェック済であるか確認する
			HttpSession session = req.getSession(true);
			Object autoLoginCheck = session.getAttribute(ControllerConstants.SESSION_KEY_AUTO_LOGIN_CHECK);
			if (autoLoginCheck != null) {
				if (autoLoginCheck instanceof Boolean) {
					if ((boolean) autoLoginCheck){
						return true;
					}
				}
			}
			session.setAttribute(ControllerConstants.SESSION_KEY_AUTO_LOGIN_CHECK, true);

			// 既にログイン済であるかセッションを確認する
			String userId = null;
			Object loginUser = session.getAttribute(ControllerConstants.SESSION_KEY_LOGIN_USER);
			if (loginUser != null) {
				if (loginUser instanceof Map) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> user = (HashMap<String, Object>)loginUser;
					userId = (String)user.get(AuthUtils.getAuthColumnName(ControllerConstants.PK_COLUMN_NAME));
					if (userId != null) {
						// ログイン済
						return true;
					}
				}
			}

			// 未ログインであった場合、自動ログイン設定が存在するかクッキーを確認する
			String key = null;
			Cookie[] cookies = req.getCookies();
			if (cookies != null) {
				for (int i = 0; i < cookies.length; i++) {
					if (cookies[i].getName().equals(PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTOLOGIN_COOKIE_NAME_KEY))) {
						key = cookies[i].getValue();
					}
				}
			}

			if (key != null) {
				sqlSession = DaoUtils.openSqlSession(false);

				HashMap<String, Object> user = getUserDataByAutoLoginKey(sqlSession, key);
				if (user != null) {
					userId = removeAutoLoginKey(req, res, sqlSession);
					HttpSession httpSession = req.getSession(false);
					if (httpSession != null) {
						httpSession.invalidate();
					}
					httpSession = req.getSession(true);
					httpSession.setAttribute(ControllerConstants.SESSION_KEY_LOGIN_USER, user);

					addAutoLoginKey(req, res, sqlSession, user);
				} else {
					// TODO:不正利用の疑いあり。。
					userId = removeAutoLoginKey(req, res, null);
					return false;
				}
				DaoUtils.commit(sqlSession);
			}
		} catch (Exception e) {
			_logger.error(e.getMessage(), e);
			// ignore
		} finally {
			DaoUtils.closeSqlSession(sqlSession);
		}
		return true;
	}

	public static void addAutoLoginKey(HttpServletRequest req, HttpServletResponse res, SqlSession sqlSession, HashMap<String, Object> user) throws WebAppOSException {
		String userId = (String) user.get(AuthUtils.getAuthColumnName(ControllerConstants.PK_COLUMN_NAME));
		String loginId = (String) user.get(AuthUtils.getAuthColumnName(PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_EMAIL_COLUMN_NAME)));

		Cipher cipher = CipherFactory.create(ControllerConstants.CIPHER_TYPE_SMD5);
		String key = cipher.encrypt(loginId + RandomStringUtils.randomAlphanumeric(32));

		int expires = Integer.parseInt(PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTOLOGIN_COOKIE_NAME_EXPIRES));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, expires);
		int expiry = (int)((cal.getTimeInMillis() - System.currentTimeMillis())/1000);

		registerAutoLogin(sqlSession, userId, key, new Timestamp(cal.getTimeInMillis()));

		String cookieName = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTOLOGIN_COOKIE_NAME_KEY);
		Cookie cookieId = new Cookie(cookieName, key);
		cookieId.setPath("/");
		cookieId.setMaxAge(expiry);
		res.addCookie(cookieId);
	}

	public static void removeAutoLoginKey(HttpServletRequest req, HttpServletResponse res) throws WebAppOSException {
		SqlSession sqlSession = null;
		try {
			sqlSession = DaoUtils.openSqlSession(false);
			removeAutoLoginKey(req, res, sqlSession);
			DaoUtils.commit(sqlSession);
		} finally {
			DaoUtils.closeSqlSession(sqlSession);
		}
	}

	public static String removeAutoLoginKey(HttpServletRequest req, HttpServletResponse res, SqlSession sqlSession) throws WebAppOSException {
		String userId = null;
		String key = null;
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals(PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTOLOGIN_COOKIE_NAME_KEY))) {
					key = cookies[i].getValue();
					cookies[i].setPath("/");
					cookies[i].setMaxAge(0);
					res.addCookie(cookies[i]);
				}
			}
		}
		if (key != null && sqlSession != null) {
			userId = getUserId(sqlSession, key);
			removeAutoLoginByKey(sqlSession, key);
		}
		return userId;
	}

	private static HashMap<String, Object> getUserDataByAutoLoginKey(SqlSession sqlSession, String key)
			throws NoSuchMethodException, SecurityException, WebAppOSException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String mapperName = DaoUtils.getMapperName(CrudEnum.SELECT_BY_EXAMPLE.getMethod(), ControllerConstants.TABLE_NAME_AUTO_LOGIN);

		CommonExample example = new CommonExample();
		Criteria criteria = example.createCriteria();
		criteria.andEqualTo(ControllerConstants.COLUMN_NAME_AUTO_LOGIN_KEY, key);
		criteria.andGreaterThan(ControllerConstants.COLUMN_NAME_AUTO_EXPIRES_DATE, new Timestamp(System.currentTimeMillis()));
		List<?> entityList = sqlSession.selectList(mapperName, example);
		if (entityList.size() == 1) {
			Object autoLoginItem = entityList.get(0);
			Method method = autoLoginItem.getClass().getMethod("getUserId"); //$NON-NLS-1$
			SchemaColumnBean idColumn = SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(ControllerConstants.TABLE_NAME_OAUTH_MANAGE, ControllerConstants.PK_COLUMN_NAME));
			Object userId = convertStringToDbType(String.valueOf(method.invoke(autoLoginItem, (Object[])null)), idColumn);

			String authTableName = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_TABLE_NAME);
			mapperName = DaoUtils.getMapperName(CrudEnum.SELECT_ALL_BY_EXAMPLE.getMethod(), authTableName);
			example = new CommonExample();
			criteria = example.createCriteria();
			criteria.andEqualTo(authTableName + '.' + ControllerConstants.PK_COLUMN_NAME, userId);
			List<?> user = sqlSession.selectList(mapperName, example);
			if (user.size() == 1) {
				return DaoUtils.convertEntityToMap(user.get(0), true);
			}
		}
		return null;
	}

	private static String getUserId(SqlSession sqlSession, String key) {
		try {
			String mapperName = DaoUtils.getMapperName(CrudEnum.SELECT_BY_EXAMPLE.getMethod(), ControllerConstants.TABLE_NAME_AUTO_LOGIN);
			CommonExample example = new CommonExample();
			Criteria criteria = example.createCriteria();
			criteria.andEqualTo(ControllerConstants.COLUMN_NAME_AUTO_LOGIN_KEY, key);
			List<?> entityList = sqlSession.selectList(mapperName, example);
			if (entityList.size() > 0){
				Object autoLoginItem = entityList.get(0);
				Method method = autoLoginItem.getClass().getMethod("getUserId"); //$NON-NLS-1$
				return String.valueOf(method.invoke(autoLoginItem, (Object[])null));
			}
		} catch (Exception e) {
			_logger.error(e.getMessage(), e);
			return null;
		}
		return null;
	}

	private static void registerAutoLogin(SqlSession sqlSession, String userId, String key, Timestamp expires) throws WebAppOSException {
		Object dbUserId = convertStringToDbType(userId,
				SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(ControllerConstants.TABLE_NAME_AUTO_LOGIN, ControllerConstants.COLUMN_NAME_AUTO_LOGIN_USER_ID)));
		Object dbKey = convertStringToDbType(key,
				SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(ControllerConstants.TABLE_NAME_AUTO_LOGIN, ControllerConstants.COLUMN_NAME_AUTO_LOGIN_KEY)));
		String mapperName = DaoUtils.getMapperName(CrudEnum.INSERT.getMethod(), ControllerConstants.TABLE_NAME_AUTO_LOGIN);
		Map<String, Object> daoParams = new HashMap<String, Object>();
		daoParams.put(snakeToCamel(ControllerConstants.COLUMN_NAME_AUTO_LOGIN_USER_ID), dbUserId);
		daoParams.put(snakeToCamel(ControllerConstants.COLUMN_NAME_AUTO_LOGIN_KEY), dbKey);
		daoParams.put(snakeToCamel(ControllerConstants.COLUMN_NAME_AUTO_EXPIRES_DATE), expires);
		daoParams.put(snakeToCamel(ControllerConstants.CREATED_COLUMN_NAME), new Timestamp(System.currentTimeMillis()));
		daoParams.put(snakeToCamel(ControllerConstants.UPDATED_COLUMN_NAME), new Timestamp(System.currentTimeMillis()));
		Map<String, Object> insertMap = new HashMap<String, Object>();
		insertMap.put(ControllerConstants.MYBATIS_MAP_KEY_RECORD, daoParams);
		sqlSession.insert(mapperName, insertMap);
	}

	private static void removeAutoLoginByKey(SqlSession sqlSession, String key) throws WebAppOSException {
		Object dbKey = convertStringToDbType(key,
				SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(ControllerConstants.TABLE_NAME_AUTO_LOGIN, ControllerConstants.COLUMN_NAME_AUTO_LOGIN_KEY)));
		CommonExample example = new CommonExample();
		Criteria criteria = example.createCriteria();
		criteria.andEqualTo(ControllerConstants.COLUMN_NAME_AUTO_LOGIN_KEY, dbKey);
		String mapperName = DaoUtils.getMapperName(CrudEnum.DELETE.getMethod(), ControllerConstants.TABLE_NAME_AUTO_LOGIN);
		sqlSession.delete(mapperName, example);
	}
}
