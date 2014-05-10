package jp.co.headwaters.webappos.generator.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.utils.ControllerUtils;
import jp.co.headwaters.webappos.generator.GeneratorConstants;
import jp.co.headwaters.webappos.generator.utils.GeneratorUtils;
import jp.co.headwaters.webappos.generator.utils.MessageUtils;
import jp.co.headwaters.webappos.generator.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyGenerator {

	private static final Log _logger = LogFactory.getLog(PropertyGenerator.class);

	public boolean generate() {
		try {
			Properties conf = new Properties();
			try (
					InputStream inputStream = new FileInputStream(getInputFile());
					) {
				conf.load(inputStream);

				if (!StringUtils.isEmpty(System.getenv(GeneratorConstants.ENV_NAME_CONTEXT_MODE))) {
					conf.setProperty(ControllerConstants.PROPERTY_KEY_CONTEXT_MODE, System.getenv(GeneratorConstants.ENV_NAME_CONTEXT_MODE));
					conf.setProperty(ControllerConstants.PROPERTY_KEY_ROOT_PACKAGE, System.getenv(GeneratorConstants.PROPERTY_KEY_ROOT_PACKAGE));
				}
				String contextName = GeneratorUtils.getContextName();
				conf.setProperty(ControllerConstants.PROPERTY_KEY_CONTEXT_NAME, contextName);
				conf.setProperty(ControllerConstants.PROPERTY_KEY_WEBAPPS_PATH, getWebAppsPath(contextName));
				conf.store(new FileOutputStream(getOutputFile()), getComments(contextName));

				PropertyUtils.putProperty(GeneratorConstants.PROPERTY_KEY_ROOT_PACKAGE,
						conf.getProperty(ControllerConstants.PROPERTY_KEY_ROOT_PACKAGE));
				PropertyUtils.putProperty(GeneratorConstants.PROPERTY_KEY_CONTEXT_MODE,
						conf.getProperty(ControllerConstants.PROPERTY_KEY_CONTEXT_MODE));
			}
		} catch (Exception e) {
			_logger.error(MessageUtils.getString("err.300"), e); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	private String getWebAppsPath(String contextName) {
		StringBuilder sb = new StringBuilder();
		sb.append(PropertyUtils.getProperty(GeneratorConstants.PROPERTY_KEY_WEBAPPS_PATH));
		if (!sb.toString().endsWith(ControllerUtils.getFileSparator())) {
			sb.append(ControllerUtils.getFileSparator());
		}
		sb.append(contextName);
		sb.append(ControllerUtils.getFileSparator());
		sb.append(GeneratorConstants.WEBAPPS_DIR);
		sb.append(ControllerUtils.getFileSparator());
		return sb.toString();
	}

	private String getComments(String contextName) {
		StringBuilder sb = new StringBuilder();
		sb.append(" for "); //$NON-NLS-1$
		sb.append(contextName);
		sb.append(" properties"); //$NON-NLS-1$
		return sb.toString();
	}

	private static File getInputFile() {
		StringBuilder sb = new StringBuilder();
		sb.append(GeneratorUtils.getIputPropertyPath());
		sb.append(ControllerConstants.PROPERTY_FILE_NAME);
		return new File(sb.toString());
	}

	private static File getOutputFile() {
		StringBuilder sb = new StringBuilder();
		sb.append(GeneratorUtils.getOutputPropertyPath());
		sb.append(ControllerConstants.PROPERTY_FILE_NAME);
		return new File(sb.toString());
	}
}
