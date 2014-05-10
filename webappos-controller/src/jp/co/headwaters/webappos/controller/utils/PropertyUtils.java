package jp.co.headwaters.webappos.controller.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import jp.co.headwaters.webappos.controller.ControllerConstants;

public class PropertyUtils {

	private static Properties _configuration = new Properties();

	private PropertyUtils() {
	}

	public static void load() throws IOException {
		try (
				InputStream inputStream = PropertyUtils.class.getClassLoader().
											getResourceAsStream(ControllerConstants.PROPERTY_FILE_NAME)) {
			_configuration.load(inputStream);
		}
	}

	public static String getProperty(String key) {
		return _configuration.getProperty(key);
	}

	public static void putProperty(String key, String value) {
		_configuration.put(key, value);
	}
}