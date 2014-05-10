package jp.co.headwaters.webappos.controller.oauth;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import jp.co.headwaters.webappos.controller.ControllerConstants;

public class ConsumerProperties {

	private static Properties _configuration  = new Properties();
	private static Map<String, OAuthConsumer> _pool;

	private ConsumerProperties(){}

	public static void load() throws IOException {
		try (
				InputStream inputStream = ConsumerProperties.class.getClassLoader().
							getResourceAsStream(ControllerConstants.OAUTH_PROPERTY_FILE_NAME)) {
			if (inputStream != null) {
				_configuration.load(inputStream);
			}
		}
		_pool = new HashMap<String, OAuthConsumer>();
	}

	public static OAuthConsumer getConsumer(String name) throws MalformedURLException
	{
		OAuthConsumer consumer;
		synchronized (_pool) {
			consumer = (OAuthConsumer) _pool.get(name);
			if (consumer == null) {
				consumer = newConsumer(name);
				_pool.put(name, consumer);
			}
		}
		return consumer;
	}

	private static OAuthConsumer newConsumer(String name) throws MalformedURLException {
		OAuthConsumer consumer = new OAuthConsumer(name,
				_configuration.getProperty(name + ControllerConstants.PROPERTY_KEY_OAUTH_API_KEY),
				_configuration.getProperty(name + ControllerConstants.PROPERTY_KEY_OAUTH_API_SECRET));
		for (Entry<Object, Object> prop : _configuration.entrySet()) {
			String propName = (String) prop.getKey();
			if (propName.startsWith(name + ControllerConstants.PROPERTY_KEY_OAUTH_PARAMS)) {
				String c = propName.substring(name.length() + ControllerConstants.PROPERTY_KEY_OAUTH_PARAMS.length());
				consumer.setParameter(c, prop.getValue());
			}
		}
		return consumer;
	}
}