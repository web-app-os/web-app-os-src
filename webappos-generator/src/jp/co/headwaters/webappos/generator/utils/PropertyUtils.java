package jp.co.headwaters.webappos.generator.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import jp.co.headwaters.webappos.generator.GeneratorConstants;

import org.apache.commons.lang3.StringUtils;

public class PropertyUtils {

	private static Properties _configuration = new Properties();

	private PropertyUtils() {
	}

	public static void load() throws IOException {
		InputStream inputStream = null;
		try {
			if (StringUtils.isEmpty(System.getenv(GeneratorConstants.ENV_NAME_GEN_PATH))) {
				inputStream = PropertyUtils.class.getClassLoader().
						getResourceAsStream(GeneratorConstants.PROPERTY_FILE_NAME);
				_configuration.load(inputStream);
			} else {
				_configuration.put(GeneratorConstants.PROPERTY_KEY_CONNECTION_URL, System.getenv(GeneratorConstants.ENV_NAME_CONNECTION_URL));
				_configuration.put(GeneratorConstants.PROPERTY_KEY_GENERATE_PATH, System.getenv(GeneratorConstants.ENV_NAME_GEN_PATH));
				_configuration.put(GeneratorConstants.PROPERTY_KEY_WEBAPPS_PATH, System.getenv(GeneratorConstants.ENV_NAME_WEB_APPS_PATH));
			}
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

	public static String getProperty(String key) {
		return _configuration.getProperty(key);
	}

	public static void putProperty(String key, String value) {
		_configuration.put(key, value);
	}
}