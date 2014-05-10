package jp.co.headwaters.webappos.controller.session;

import org.scribe.model.Token;

public class OAuthBean {

	private final static String KEY = "jp.co.headwaters.webappos.controller.session.OAuthBean"; //$NON-NLS-1$
	public static String getKey() {
		return KEY;
	}

	private String successUri;
	private String failuretUri;
	private String notfoundUri;
	private String providerName;
	private Token requestToken;
	private Token accessToken;
	private String userId;
	private String uid;
	private boolean isAutoLogin;

	public String getSuccessUri() {
		return this.successUri;
	}

	public void setSuccessUri(String successUri) {
		this.successUri = successUri;
	}

	public String getFailuretUri() {
		return this.failuretUri;
	}

	public void setFailuretUri(String failuretUri) {
		this.failuretUri = failuretUri;
	}

	public String getNotfoundUri() {
		return this.notfoundUri;
	}

	public void setNotfoundUri(String notfoundUri) {
		this.notfoundUri = notfoundUri;
	}

	public String getProviderName() {
		return this.providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public Token getRequestToken() {
		return this.requestToken;
	}

	public void setRequestToken(Token requestToken) {
		this.requestToken = requestToken;
	}

	public Token getAccessToken() {
		return this.accessToken;
	}

	public void setAccessToken(Token accessToken) {
		this.accessToken = accessToken;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUid() {
		return this.uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public boolean isAutoLogin() {
		return this.isAutoLogin;
	}

	public void setAutoLogin(boolean isAutoLogin) {
		this.isAutoLogin = isAutoLogin;
	}
}
