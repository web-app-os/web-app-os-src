package jp.co.headwaters.webappos.controller.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.cache.WebAppOSCache;
import jp.co.headwaters.webappos.controller.cache.bean.UrlPatternBean;
import jp.co.headwaters.webappos.controller.utils.AuthUtils;
import jp.co.headwaters.webappos.controller.utils.ControllerUtils;
import jp.co.headwaters.webappos.controller.utils.MessageUtils;
import jp.co.headwaters.webappos.controller.utils.PropertyUtils;
import jp.co.headwaters.webappos.controller.utils.RegexUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RoutingFilter implements Filter {

	private static final Log _logger = LogFactory.getLog(RoutingFilter.class);
	private WebAppOSCache _webAppOSCache;

	@Override
	public void init(FilterConfig config) throws ServletException {
		this._webAppOSCache = WebAppOSCache.getInstance();
		if (this._webAppOSCache == null || this._webAppOSCache.getUrlPatternMap().size() == 0) {
			throw new ServletException(MessageUtils.getString("err.000")); //$NON-NLS-1$
		}
	}

	@Override
	public void destroy() {
		// nothing
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String uri = req.getRequestURI();

		String extension = ControllerUtils.getFileExtension(uri);
		if (extension != null) {
			chain.doFilter(request, response);
			return;
		}

		// append slash
		if (!uri.endsWith(ControllerConstants.PATH_DELIMITER)) {
			uri += ControllerConstants.PATH_DELIMITER;
		}

		// check uri pattern
		String urlPattern = null;
		boolean isMatch = false;
		String actionName = null;

		if (isApi(uri)) {
			for (String target : ControllerConstants.API_URL_PATTERNS) {
				urlPattern = getApiUrl(target);
				Pattern pattern = Pattern.compile(RegexUtils.getUrlPatternRegex(urlPattern));
				Matcher matcher = pattern.matcher(uri);
				if (matcher.find()) {
					isMatch = true;
					actionName = getApiActionName(urlPattern);
					break;
				}
			}
		} else {
			Map<String, UrlPatternBean> urlPatternMap = this._webAppOSCache.getUrlPatternMap();
			for (Entry<String, UrlPatternBean> target : urlPatternMap.entrySet()) {
				Pattern pattern = Pattern.compile(target.getValue().getPattern());
				Matcher matcher = pattern.matcher(uri);
				if (matcher.find()) {
					if (!AuthUtils.autoLogin(req, res)) {
						res.sendRedirect(ControllerUtils.getRedirectUri(req));
						return;
					}

					if (target.getValue().isAuthRequire()){
						if (!checkLogin(req, res)) {
							return;
						}
					}
					isMatch = true;
					request.setAttribute(ControllerConstants.ATTR_NAME_RESULT_NAME, target.getValue().getResultName());
					urlPattern = target.getKey();
					actionName = target.getValue().getActionName();
					String authUrl = ControllerUtils.getRedirectUri(PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_REQUEST_URI));
					if (!urlPattern.equalsIgnoreCase(authUrl) && !urlPattern.equalsIgnoreCase((String) req.getSession().getAttribute(ControllerConstants.SESSION_KEY_LOGIN_OAUTH_SUCCESS_URI))) {
						req.getSession().setAttribute(ControllerConstants.SESSION_KEY_LOGIN_REDIRECT_URI, null);
					}
					break;
				}
			}
		}

		if (!isMatch) {
			_logger.error(MessageUtils.getString("err.101", uri)); //$NON-NLS-1$
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// get parameter name
		String[] params = null;
		List<String> paramNames = new ArrayList<String>();
		Pattern pattern = Pattern.compile(ControllerConstants.REGEX_REQUEST_PARAM);
		Matcher matcher = pattern.matcher(urlPattern);
		while (matcher.find()) {
			params = matcher.group().split(ControllerConstants.PATH_DELIMITER);
			paramNames.add(params[0]);
		}

		// forward
		String forwardUri = getForwardUri(actionName, paramNames, uri);
		RequestDispatcher rd = request.getRequestDispatcher(forwardUri);
		rd.forward(request, response); // 転送する
	}

	private static String getForwardUri(String actionName, List<String> paramNames, String uri) {
		StringBuilder sb = new StringBuilder();
		sb.append(ControllerConstants.PATH_DELIMITER);
		sb.append(actionName);

		StringBuilder params = new StringBuilder();
		// append query string
		if (paramNames != null && paramNames.size() > 0) {
			String param = null;
			int beginIndex = 0;
			int endIndex = 0;
			for (String paramName : paramNames) {
				beginIndex = uri.indexOf("/" + paramName + "/") + paramName.length() + 2; //$NON-NLS-1$ //$NON-NLS-2$
				endIndex = uri.indexOf(ControllerConstants.PATH_DELIMITER, beginIndex);
				param = uri.substring(beginIndex, endIndex);
				if (param != null && param.length() > 0) {
					if (params.toString().length() > 0) {
						params.append("&"); //$NON-NLS-1$
					}
					params.append(paramName);
					params.append("="); //$NON-NLS-1$
					params.append(param);
				}
			}
		}

		if (params.toString().length() > 0) {
			sb.append("?"); //$NON-NLS-1$
			sb.append(params.toString());
		}
		return sb.toString();
	}

	private static boolean isApi(String uri) {
		String[] paths = uri.split(ControllerConstants.PATH_DELIMITER);
		String target = null;
		if (ControllerUtils.getContextMode()) {
			if (paths.length > 2) {
				target = paths[2];
			}
		} else {
			if (paths.length > 1) {
				target = paths[1];
			}
		}
		return ControllerConstants.API_PREFIX.equals(target) ? true : false;
	}

	private static String getApiUrl(String pattern) {
		StringBuilder sb = new StringBuilder();
		if (ControllerUtils.getContextMode()) {
			sb.append(ControllerConstants.PATH_DELIMITER);
			sb.append(ControllerUtils.getContextName());
		}
		sb.append(ControllerConstants.PATH_DELIMITER);
		sb.append(ControllerConstants.API_PREFIX);
		sb.append(pattern);
		return sb.toString();
	}

	private static String getApiActionName(String uri) {
		uri = uri.substring(uri.indexOf(ControllerConstants.API_PREFIX) + ControllerConstants.API_PREFIX.length() + 1);
		String[] paths = uri.split(ControllerConstants.PATH_DELIMITER);
		StringBuilder sb = new StringBuilder();
		sb.append(ControllerConstants.API_PREFIX);
		sb.append(ControllerConstants.PATH_DELIMITER);
		sb.append(uri.split(ControllerConstants.PATH_DELIMITER)[0]);
		if (paths.length > 1) {
			sb.append("!"); //$NON-NLS-1$
			sb.append(uri.split(ControllerConstants.PATH_DELIMITER)[1]);
		}
		return sb.toString();
	}

	private boolean checkLogin(HttpServletRequest req, HttpServletResponse res) throws IOException {
		boolean isLoggedin = false;
		HttpSession session = req.getSession();
		Object loginUser = session.getAttribute(ControllerConstants.SESSION_KEY_LOGIN_USER);
		if (loginUser != null) {
			if (loginUser instanceof Map) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> user = (HashMap<String, Object>)loginUser;
				String userId = (String)user.get(AuthUtils.getAuthColumnName(ControllerConstants.PK_COLUMN_NAME));
				if (userId != null){
					isLoggedin = true;
				}
			}
		}
		if (!isLoggedin) {
			session.setAttribute(ControllerConstants.SESSION_KEY_LOGIN_REDIRECT_URI, ControllerUtils.getRedirectUri(req));
			res.sendRedirect(ControllerUtils.getRedirectUri(PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_AUTH_REQUEST_URI)));
		}
		return isLoggedin;
	}
}