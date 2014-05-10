package jp.co.headwaters.webappos.controller.oauth;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class OAuthConsumer implements Serializable
{
	private static final long serialVersionUID = -205128824753498113L;
	private final Map<String, Object> parameters = new HashMap<String, Object>();
	private final String providerName;
	private final String apiKey;
	private final String apiSecret;

	public OAuthConsumer(String name, String key, String secret) {
		this.providerName = name;
		this.apiKey = key;
		this.apiSecret = secret;
	}

	public String getProviderName() {
		return this.providerName;
	}

	public String getApiKey() {
		return this.apiKey;
	}

	public String getApiSecret() {
		return this.apiSecret;
	}

	public Map<String, Object> getParameters() {
		return this.parameters;
	}

	public Object getParameter(String name) {
		return this.parameters.get(name);
	}

	public void setParameter(String name, Object value) {
		this.parameters.put(name, value);
	}
}