package jp.co.headwaters.webappos.controller.oauth;

import static jp.co.headwaters.webappos.controller.utils.ControllerUtils.*;
import static jp.co.headwaters.webappos.controller.utils.ConvertDateTypeUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.cache.SchemaColumnCache;
import jp.co.headwaters.webappos.controller.cache.bean.AbstractExecuteBean;
import jp.co.headwaters.webappos.controller.cache.bean.SchemaColumnBean;
import jp.co.headwaters.webappos.controller.enumation.CrudEnum;
import jp.co.headwaters.webappos.controller.enumation.ServiceProviderEnum;
import jp.co.headwaters.webappos.controller.exception.WebAppOSException;
import jp.co.headwaters.webappos.controller.fuction.AbstractFunction;
import jp.co.headwaters.webappos.controller.model.CommonExample;
import jp.co.headwaters.webappos.controller.model.CommonExample.Criteria;
import jp.co.headwaters.webappos.controller.session.OAuthBean;
import jp.co.headwaters.webappos.controller.utils.AuthUtils;
import jp.co.headwaters.webappos.controller.utils.DaoUtils;
import jp.co.headwaters.webappos.controller.utils.MessageUtils;
import jp.co.headwaters.webappos.controller.utils.PropertyUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractOAuthClient extends AbstractFunction {

	protected static final Log _logger = LogFactory.getLog(AbstractOAuthClient.class);

	@Override
	protected void execute(AbstractExecuteBean function) throws Exception {}

	public abstract void oauth(HttpServletRequest request, HttpServletResponse response, OAuthBean oAuthData)
			throws Exception;

	public abstract Token getAccessToken(HttpServletRequest request, OAuthService service, OAuthBean oAuthData)
			throws Exception;

	public void callback(HttpServletRequest request, HttpServletResponse response, OAuthBean oAuthData)
			throws Exception {
		String provider = oAuthData.getProviderName();
		OAuthConsumer consumer = ConsumerProperties.getConsumer(provider);
		OAuthService service = new ServiceBuilder()
				.provider(this.providerApi)
				.apiKey(consumer.getApiKey())
				.apiSecret(consumer.getApiSecret())
				.callback(getCallbackUrl(request))
				.build();

		String redirectUri = (String) request.getSession().getAttribute(ControllerConstants.SESSION_KEY_LOGIN_REDIRECT_URI);

		Token accessToken = getAccessToken(request, service, oAuthData);
		if (accessToken == null) {
			_logger.warn(MessageUtils.getString("err.500")); //$NON-NLS-1$
			response.sendRedirect(oAuthData.getFailuretUri());
			return;
		}

		String uid = getUid(service, accessToken);
		if (uid == null) {
			response.sendRedirect(oAuthData.getFailuretUri());
			return;
		}

		String userId = oAuthData.getUserId();
		HttpSession httpSession = request.getSession(false);
		if (httpSession != null) {
			httpSession.invalidate();
		}
		httpSession = request.getSession(true);
		oAuthData.setUid(uid);
		oAuthData.setAccessToken(accessToken);
		oAuthData.setUserId(userId);
		httpSession.setAttribute(OAuthBean.getKey(), oAuthData);
		httpSession.setAttribute(ControllerConstants.SESSION_KEY_LOGIN_REDIRECT_URI, redirectUri);

		SqlSession sqlSession = null;
		try {
			sqlSession = DaoUtils.openSqlSession(false);
			if (oAuthData.getUserId() != null) {
				AuthUtils.registerOAuthData(sqlSession, provider, oAuthData.getUserId(), uid);
			}

			HashMap<String, Object> user = getUserData(sqlSession, provider, uid);
			if (user == null || user.get(AuthUtils.getAuthColumnName(ControllerConstants.PK_COLUMN_NAME)) == null) {
				response.sendRedirect(oAuthData.getNotfoundUri());
				return;
			}
			oAuthData.setUserId((String)user.get(AuthUtils.getAuthColumnName(ControllerConstants.PK_COLUMN_NAME)));
			httpSession.setAttribute(ControllerConstants.SESSION_KEY_LOGIN_USER, user);
			httpSession.setAttribute(ControllerConstants.SESSION_KEY_LOGIN_OAUTH_SUCCESS_URI, oAuthData.getSuccessUri());

			if (oAuthData.isAutoLogin()) {
				AuthUtils.removeAutoLoginKey(request, response, sqlSession);
				AuthUtils.addAutoLoginKey(request, response, sqlSession, user);
			}
			DaoUtils.commit(sqlSession);
		} finally {
			DaoUtils.closeSqlSession(sqlSession);
		}

		response.sendRedirect(oAuthData.getSuccessUri());
	}

	public void users(HttpServletRequest request, HttpServletResponse response, OAuthBean oAuthData) throws Exception {
		String provider = oAuthData.getProviderName();
		OAuthConsumer consumer = ConsumerProperties.getConsumer(provider);
		OAuthService service = new ServiceBuilder()
				.provider(this.providerApi)
				.apiKey(consumer.getApiKey())
				.apiSecret(consumer.getApiSecret())
				.build();
		OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, this.resourceUrl);
		service.signRequest(oAuthData.getAccessToken(), oAuthRequest);
		Response oauthResponse = oAuthRequest.send();
		response.setStatus(oauthResponse.getCode());
		response.setContentType(getContentType(oauthResponse.getHeaders()));
		try (InputStream in = oauthResponse.getStream()) {
			OutputStream out = response.getOutputStream();
			copyAll(in, out);
		}
	}

	private String getContentType(Map<String, String> headers){
		for (Entry<String, String> header : headers.entrySet()){
			if (header.getKey() != null && "content-type".equals(header.getKey().toLowerCase())){ //$NON-NLS-1$
				return header.getValue();
			}
		}
		return null;
	}

	public void disconnect(HttpServletRequest request, HttpServletResponse response, String provider, String userId) throws Exception {
		removeOAuthData(request, provider, userId);
	}

	private String getUid(OAuthService service, Token accessToken) {
		OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, this.resourceUrl);
		service.signRequest(accessToken, oAuthRequest);
		Response oauthResponse = oAuthRequest.send();
		if (oauthResponse.getCode() == HttpServletResponse.SC_OK) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				JsonNode node = mapper.readValue(oauthResponse.getBody(), JsonNode.class);
				return node.get("id").asText(); //$NON-NLS-1$
			} catch (Exception e) {
				_logger.error(MessageUtils.getString("err.501"), e); //$NON-NLS-1$
				return null;
			}
		}
		_logger.warn(MessageUtils.getString("err.502", String.valueOf(oauthResponse.getCode()))); //$NON-NLS-1$
		return null;
	}

	public HashMap<String, Object> getUserData(SqlSession sqlSession, String provider, String uid) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		try{
			String mapperName = DaoUtils.getMapperName(CrudEnum.SELECT_BY_EXAMPLE.getMethod(), ControllerConstants.TABLE_NAME_OAUTH_MANAGE);
			CommonExample example = new CommonExample();
			Criteria criteria = example.createCriteria();
			criteria.andEqualTo(ControllerConstants.COLUMN_NAME_OAUTH_MANAGE_PROVIDER, provider);
			criteria.andEqualTo(ControllerConstants.COLUMN_NAME_OAUTH_MANAGE_UID, uid);
			List<?> entityList = sqlSession.selectList(mapperName, example);
			if (entityList.size() == 1) {
				Object oauthManager = entityList.get(0);
				Method method = oauthManager.getClass().getMethod("getUserId"); //$NON-NLS-1$
				SchemaColumnBean addressColumn = SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(ControllerConstants.TABLE_NAME_OAUTH_MANAGE, ControllerConstants.PK_COLUMN_NAME));
				Object userId = convertStringToDbType(String.valueOf(method.invoke(oauthManager, (Object[])null)), addressColumn);

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
		} catch (Exception e) {
			_logger.error(e.getMessage(), e);
		}
		return null;
	}

	private void removeOAuthData(HttpServletRequest request, String provider, String userId) throws WebAppOSException {
		SqlSession sqlSession = null;
		try {
			sqlSession = DaoUtils.openSqlSession();
			CommonExample example = new CommonExample();
			Criteria criteria = example.createCriteria();
			String mapperName = DaoUtils.getMapperName(CrudEnum.DELETE.getMethod(), ControllerConstants.TABLE_NAME_OAUTH_MANAGE);

			Object dbProvider = convertStringToDbType(ControllerConstants.TABLE_NAME_OAUTH_MANAGE, ControllerConstants.COLUMN_NAME_OAUTH_MANAGE_PROVIDER, provider);
			Object dbUserId = convertStringToDbType(ControllerConstants.TABLE_NAME_OAUTH_MANAGE, ControllerConstants.COLUMN_NAME_OAUTH_MANAGE_USER_ID, userId);

			criteria.andEqualTo(ControllerConstants.COLUMN_NAME_OAUTH_MANAGE_PROVIDER, dbProvider);
			criteria.andEqualTo(ControllerConstants.COLUMN_NAME_OAUTH_MANAGE_USER_ID, dbUserId);
			sqlSession.delete(mapperName, example);

			HttpSession httpSession = request.getSession(false);
			if (httpSession != null) {
				@SuppressWarnings("unchecked")
				Map<String, Object> loginUser = (HashMap<String, Object>) httpSession.getAttribute(ControllerConstants.SESSION_KEY_LOGIN_USER);
				Map<String, Object> newUser = AuthUtils.refreshLoginUserData(sqlSession, loginUser);
				httpSession.setAttribute(ControllerConstants.SESSION_KEY_LOGIN_USER, newUser);
			}
		} finally {
			DaoUtils.closeSqlSession(sqlSession);
		}
	}

	protected String addParameter(String uri) throws MalformedURLException {
		StringBuilder sb = new StringBuilder(uri);
		OAuthConsumer consumer = ConsumerProperties.getConsumer(this.provider.getName());
		for (Entry<String, Object> param : consumer.getParameters().entrySet()) {
			sb.append("&"); //$NON-NLS-1$
			sb.append(param.getKey());
			sb.append("="); //$NON-NLS-1$
			sb.append(param.getValue());
		}
		return sb.toString();
	}

	protected String getCallbackUrl(HttpServletRequest request) {
		String requestURL = request.getRequestURL().toString();
		StringBuilder sb = new StringBuilder();
		sb.append(requestURL.substring(0, requestURL.lastIndexOf("/"))); //$NON-NLS-1$
		sb.append(ControllerConstants.OAUTH_CALLBACK_URL);
		sb.append(ControllerConstants.API_PARAM_OAUTH_SP);
		sb.append("/"); //$NON-NLS-1$
		sb.append(this.provider.getName());
		sb.append("/"); //$NON-NLS-1$
		return sb.toString();
	}

	protected void copyAll(InputStream from, OutputStream into) throws IOException {
		byte[] buffer = new byte[1024];
		for (int len; 0 < (len = from.read(buffer, 0, buffer.length));) {
			into.write(buffer, 0, len);
		}
	}

	protected ServiceProviderEnum provider;
	protected Api providerApi;
	protected String resourceUrl;
	public void setProvider(ServiceProviderEnum provider) {
		this.provider = provider;
	}
	public void setProviderApi(Api providerApi) {
		this.providerApi = providerApi;
	}
	public void setResourceUrl(String resourceUrl) {
		this.resourceUrl = resourceUrl;
	}
}