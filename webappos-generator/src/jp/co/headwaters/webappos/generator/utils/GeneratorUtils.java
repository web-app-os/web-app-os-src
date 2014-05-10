package jp.co.headwaters.webappos.generator.utils;

import static jp.co.headwaters.webappos.controller.utils.ControllerUtils.*;
import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.generator.GeneratorConstants;
import jp.co.headwaters.webappos.generator.Main;

import org.apache.commons.lang3.StringUtils;

public class GeneratorUtils {

	public static String getContextName() {
		return Main.getContextName();
	}

	public static boolean getContextMode() {
		return Boolean.valueOf(PropertyUtils.getProperty(GeneratorConstants.PROPERTY_KEY_CONTEXT_MODE));
	}

	public static String getRootPackage() {
		return PropertyUtils.getProperty(GeneratorConstants.PROPERTY_KEY_ROOT_PACKAGE);
	}

	public static String getInputPath() {
		StringBuilder sb = new StringBuilder(PropertyUtils.getProperty(GeneratorConstants.PROPERTY_KEY_GENERATE_PATH));
		if (!sb.toString().endsWith(getFileSparator())) {
			sb.append(getFileSparator());
		}
		if (StringUtils.isEmpty(System.getenv(GeneratorConstants.ENV_NAME_GEN_PATH))) {
			sb.append(getContextName());
			sb.append(getFileSparator());
			sb.append(GeneratorConstants.INPUT_DIR);
			sb.append(getFileSparator());
		} else {
			sb.append(GeneratorConstants.INPUT_DIR);
			sb.append(getFileSparator());
		}
		return sb.toString();
	}

	public static String getInputHtmlPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(getInputPath());
		sb.append(GeneratorConstants.INPUT_HTML_DIR);
		sb.append(getFileSparator());
		return sb.toString();
	}

	public static String getIputPropertyPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(getInputPath());
		sb.append(GeneratorConstants.INPUT_PROPERTY_DIR);
		sb.append(getFileSparator());
		return sb.toString();
	}

	public static String getInputMapperPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(getInputPath());
		sb.append(GeneratorConstants.INPUT_MAPPER_DIR);
		sb.append(getFileSparator());
		return sb.toString();
	}

	public static String getOutputPath() {
		StringBuilder sb = new StringBuilder(PropertyUtils.getProperty(GeneratorConstants.PROPERTY_KEY_GENERATE_PATH));
		if (!sb.toString().endsWith(getFileSparator())) {
			sb.append(getFileSparator());
		}
		if (StringUtils.isEmpty(System.getenv(GeneratorConstants.ENV_NAME_GEN_PATH))) {
			sb.append(getContextName());
			sb.append(getFileSparator());
			sb.append(GeneratorConstants.OUTPUT_DIR);
			sb.append(getFileSparator());
		} else {
			sb.append(GeneratorConstants.OUTPUT_DIR);
			sb.append(getFileSparator());
		}
		return sb.toString();
	}

	public static String getOutputJspPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(getOutputPath());
		sb.append(GeneratorConstants.OUTPUT_WEB_ROOT_DIR);
		sb.append(getFileSparator());
		sb.append(GeneratorConstants.OUTPUT_JSP_DIR);
		sb.append(getFileSparator());
		return sb.toString();
	}

	public static String getOutputWebInfPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(getOutputPath());
		sb.append(GeneratorConstants.OUTPUT_WEB_ROOT_DIR);
		sb.append(getFileSparator());
		sb.append("WEB-INF"); //$NON-NLS-1$
		sb.append(getFileSparator());
		return sb.toString();
	}

	public static  String getOutputSrcPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(getOutputPath());
		sb.append(GeneratorConstants.OUTPUT_SRC_DIR);
		return sb.toString();
	}

	public static String getOutputPropertyPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(getOutputPath());
		sb.append(GeneratorConstants.OUTPUT_PROPERTY_DIR);
		sb.append(getFileSparator());
		return sb.toString();
	}

	public static String getOutputDatPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(getOutputPath());
		sb.append(GeneratorConstants.OUTPUT_DAT_DIR);
		sb.append(getFileSparator());
		return sb.toString();
	}

	public static String getErrorPageLocation(String status, String separator) {
		StringBuilder sb = new StringBuilder();
		sb.append(separator);
		sb.append(GeneratorConstants.OUTPUT_JSP_DIR);
		sb.append(separator);
		sb.append(GeneratorConstants.INPUT_HTML_ERROR_PAGE_DIR);
		sb.append(separator);
		sb.append(status);
		sb.append(ControllerConstants.JSP_EXTENSION);
		return sb.toString();
	}

	public static String getLinesSparator() {
		String separator = System.getProperty("line.separator"); //$NON-NLS-1$
		if (separator == null) {
			separator = "\n"; //$NON-NLS-1$
		}
		return separator;
	}
}
