package jp.co.headwaters.webappos.controller.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

	public static String extractMatchString(String regex, String target) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(target);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	public static String getUrlPatternRegex(String pattern) {
		StringBuilder sb = new StringBuilder();
		sb.append("^"); //$NON-NLS-1$
		sb.append(pattern);
		sb.append("$"); //$NON-NLS-1$
		return sb.toString();
	}
}
