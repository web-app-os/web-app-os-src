package jp.co.headwaters.webappos.controller.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import jp.co.headwaters.webappos.controller.ControllerConstants;

import org.apache.commons.lang3.StringUtils;

public class ControllerUtils {

	public static String getFileSparator() {
		return System.getProperty("file.separator"); //$NON-NLS-1$
	}

	public static String getSchemaColumnKey(String tableName, String columnName) {
		StringBuilder sb = new StringBuilder();
		sb.append(tableName.toUpperCase());
		sb.append(ControllerConstants.TABLE_COLUMN_DELIMITER);
		sb.append(columnName.toUpperCase());
		return sb.toString();
	}

	public static String getResultMapKey(String tableName, String columnName) {
		StringBuilder sb = new StringBuilder();
		sb.append(camelToSnake(tableName).toUpperCase());
		sb.append(ControllerConstants.TABLE_COLUMN_DELIMITER);
		sb.append(camelToSnake(columnName).toUpperCase());
		return sb.toString();
	}

	public static boolean getContextMode() {
		return Boolean.valueOf(PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_CONTEXT_MODE));
	}

	public static String getContextName() {
		return PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_CONTEXT_NAME);
	}

	public static String getRedirectUri(String uri) {
		StringBuilder sb = new StringBuilder();
		if (ControllerUtils.getContextMode()) {
			sb.append(ControllerConstants.PATH_DELIMITER);
			sb.append(ControllerUtils.getContextName());
		}
		if (uri == null) {
			sb.append(ControllerConstants.PATH_DELIMITER);
		} else {
			sb.append(uri);
		}
		return sb.toString();
	}

	public static String getRedirectUri(HttpServletRequest req) {
		StringBuilder sb = new StringBuilder();
		sb.append(req.getServletPath());

		StringBuilder params = new StringBuilder();
		if (req.getParameterMap() != null && req.getParameterMap().size() > 0) {
			for (Entry<String, String[]> param : req.getParameterMap().entrySet()) {
				String[] values = param.getValue();
				for (int i = 0; i < values.length; i++) {
					if (params.toString().length() > 0) {
						params.append("&"); //$NON-NLS-1$
					}
					params.append(param.getKey());
					params.append("="); //$NON-NLS-1$
					params.append(values[i]);
				}
			}
		}
		if (params.toString().length() > 0) {
			sb.append("?"); //$NON-NLS-1$
			sb.append(params.toString());
		}
		return sb.toString();
	}

	public static String snakeToCamel(String target) {
		return snakeToCamel(target, false);
	}

	public static String snakeToCamel(String target, boolean firstCharacterUppercase) {
		Pattern p = Pattern.compile("_([a-z])"); //$NON-NLS-1$
		Matcher m = p.matcher(target.toLowerCase());

		StringBuffer sb = new StringBuffer(target.length());
		while (m.find()) {
			m.appendReplacement(sb, m.group(1).toUpperCase());
		}
		m.appendTail(sb);
		if (firstCharacterUppercase) {
			sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		}
		return sb.toString();
	}

	public static String camelToSnake(String target) {
		String convertedStr = target
				.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2") //$NON-NLS-1$ //$NON-NLS-2$
				.replaceAll("([a-z])([A-Z])", "$1_$2");  //$NON-NLS-1$//$NON-NLS-2$
		return convertedStr.toLowerCase();
	}

	public static String getFileExtension(String target) {
		if (target == null)
			return null;
		int point = target.lastIndexOf("."); //$NON-NLS-1$
		if (point != -1) {
			return target.substring(point + 1);
		}
		return null;
	}

	public static String removeFileExtension(String filename) {
		if (StringUtils.isEmpty(filename)) {
			return filename;
		}

		int lastDotPos = filename.lastIndexOf('.');
		if (lastDotPos == -1) {
			return filename;
		} else if (lastDotPos == 0) {
			return filename;
		} else {
			return filename.substring(0, lastDotPos);
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> toHtmlString(Map<String, Object> obj) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (Entry<String, Object> entry : obj.entrySet()) {
			if (entry.getValue() instanceof Map) {
				result.put(entry.getKey(), toHtmlString((Map<String, Object>) entry.getValue()));
			} else {
				result.put(entry.getKey(), toHtmlString(ConvertDateTypeUtils.convertDbTypeToString(entry.getValue())));
			}
		}
		return result;
	}

	public static String toHtmlString(String value){
		String s = "";
		for (int i = 0; i < value.length(); i++) {
			Character c = new Character(value.charAt(i));
			switch (c.charValue()) {
//			case '&' :
//				s = s.concat("&amp;");
//				break;
			case '<' :
				s = s.concat("&lt;");
				break;
			case '>' :
				s = s.concat("&gt;");
				break;
			case '"' :
				s = s.concat("&quot;");
				break;
			case '\'' :
				s = s.concat("&#39;");
				break;
			default :
				s = s.concat(c.toString());
				break;
			}
		}
		return s;
	}
}
