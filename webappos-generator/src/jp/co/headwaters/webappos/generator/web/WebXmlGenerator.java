package jp.co.headwaters.webappos.generator.web;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.generator.GeneratorConstants;
import jp.co.headwaters.webappos.generator.utils.FileUtils;
import jp.co.headwaters.webappos.generator.utils.GeneratorUtils;
import jp.co.headwaters.webappos.generator.utils.MessageUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class WebXmlGenerator {

	private static final Log _logger = LogFactory.getLog(WebXmlGenerator.class);

	public static boolean generate() {
		List<String> warnings = new ArrayList<String>();
		try {
			File outputFile = getOutputFile();
			String content = getContent(warnings);
			FileUtils.writeFile(outputFile, content, GeneratorConstants.OUTPUT_XML_FILE_ENCODING);
		} catch (Exception e) {
			_logger.error(MessageUtils.getString("err.200"), e); //$NON-NLS-1$
			return false;
		}

		if (warnings.size() != 0) {
			for (String warning : warnings){
				_logger.error(warning);
			}
			return false;
		}
		return true;
	}

	private static File getOutputFile() {
		StringBuilder sb = new StringBuilder();
		sb.append(GeneratorUtils.getOutputWebInfPath());
		sb.append(GeneratorConstants.OUTPUT_WEB_CONFIG_FILE_NAME);
		return new File(sb.toString());
	}

	public static String getContent(List<String> warnings) {
		StringBuilder sb = new StringBuilder();
		File[] htmlFiles = new File(getErrorPageDir()).listFiles(FileUtils.getTargetFileFilter());
		String fileName = null;

		if (htmlFiles == null) return ""; //$NON-NLS-1$
		List<String> requiredPageList = Arrays.asList(GeneratorConstants.REQUIRED_ERROR_PAGE_NAME);
		int requiredPageCount = 0;
		List<String> optionPageList = Arrays.asList(GeneratorConstants.OPTION_ERROR_PAGE_NAME);
		int optionPageCount = 0;

		for (File file : htmlFiles) {
			fileName = getFileName(file.getName());
			if (isHttpStatusCode(fileName)) {
				if (requiredPageList.contains(fileName)){
					requiredPageCount++;
				}
				if (optionPageList.contains(fileName)){
					optionPageCount++;
				}
				XmlElement root = new XmlElement("error-page"); //$NON-NLS-1$

				XmlElement errorCode = new XmlElement("error-code"); //$NON-NLS-1$
				errorCode.addElement(new TextElement(fileName));
				root.addElement(errorCode);

				XmlElement location = new XmlElement("location"); //$NON-NLS-1$
				location.addElement(new TextElement(
						GeneratorUtils.getErrorPageLocation(fileName, ControllerConstants.PATH_DELIMITER)));
				root.addElement(location);

				OutputUtilities.newLine(sb);
				sb.append(root.getFormattedContent(0));
			} else {
				_logger.warn(MessageUtils.getString("warn.200", file.getName())); //$NON-NLS-1$
			}
		}

		if (optionPageCount != optionPageList.size()) {
			_logger.warn(MessageUtils.getString("warn.201", StringUtils.join(optionPageList, ","))); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (requiredPageCount != requiredPageList.size()) {
			warnings.add(MessageUtils.getString("err.201", StringUtils.join(requiredPageList, ","))); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return sb.toString();
	}

	private static String getErrorPageDir() {
		StringBuilder sb = new StringBuilder();
		sb.append(GeneratorUtils.getInputHtmlPath());
		sb.append(GeneratorConstants.INPUT_HTML_ERROR_PAGE_DIR);
		return sb.toString();
	}

	private static String getFileName(String target) {
		if (target == null) {
			return null;
		}
		int point = target.lastIndexOf("."); //$NON-NLS-1$
		if (point != -1) {
			return target.substring(0, point);
		}
		return target;
	}

	private static boolean isHttpStatusCode(String target) {
		Pattern pattern = Pattern.compile(GeneratorConstants.REGEX_HTTP_STATUS_CODE);
		Matcher matcher = pattern.matcher(target);
		return matcher.find();
	}
}
