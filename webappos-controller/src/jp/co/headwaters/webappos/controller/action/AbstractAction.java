package jp.co.headwaters.webappos.controller.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.utils.AuthUtils;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.util.TokenHelper;

import com.opensymphony.xwork2.ActionSupport;

@SuppressWarnings("serial")
public abstract class AbstractAction extends ActionSupport
		implements ServletRequestAware, ServletResponseAware, SessionAware {

	protected HttpServletRequest _request;
	protected HttpServletResponse _response;
	protected Map<String, Object> _session;

	@Override
	public void setServletRequest(HttpServletRequest httpServletRequest) {
		this._request = httpServletRequest;
	}

	@Override
	public void setServletResponse(HttpServletResponse httpServletResponse) {
		this._response = httpServletResponse;
	}

	@Override
	public void setSession(Map<String, Object> paramMap) {
		this._session = paramMap;
	}

	protected boolean validateToken() {
		HttpSession session = ServletActionContext.getRequest().getSession(true);
		synchronized (session) {
			if (!(TokenHelper.validToken())) {
				return false;
			}
		}
		return true;
	}

	protected boolean isLoggedIn() {
		if (getLoginUserInfo(ControllerConstants.PK_COLUMN_NAME) != null) {
			return true;
		}
		return false;
	}

	protected String getLoginUserInfo(String key) {
		Object loginUser = this._session.get(ControllerConstants.SESSION_KEY_LOGIN_USER);
		if (loginUser != null) {
			if (loginUser instanceof Map) {
				String authKey = AuthUtils.getAuthColumnName(key);
				if (((Map<?, ?>) loginUser).get(authKey) == null) {
					return "";
				}
				return String.valueOf(((Map<?, ?>) loginUser).get(authKey));
			}
		}
		return null;
	}

	protected String getLogMessage(String... args) {
		StringBuilder sb = new StringBuilder();
		for (String arg : args) {
			if (sb.length() > 0) {
				sb.append(","); //$NON-NLS-1$
			}
			sb.append(arg);
		}
		return sb.toString();
	}
}