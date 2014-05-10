package jp.co.headwaters.webappos.generator.web;

import java.io.File;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.cache.WebAppOSCache;
import jp.co.headwaters.webappos.controller.cache.bean.ActionBean;
import jp.co.headwaters.webappos.controller.cache.bean.ExecuteBean;
import jp.co.headwaters.webappos.controller.cache.bean.ResultBean;
import jp.co.headwaters.webappos.generator.GeneratorConstants;
import jp.co.headwaters.webappos.generator.utils.FileUtils;
import jp.co.headwaters.webappos.generator.utils.GeneratorUtils;
import jp.co.headwaters.webappos.generator.utils.MessageUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class StrutsXmlGenerator {

	private static final Log _logger = LogFactory.getLog(StrutsXmlGenerator.class);

	public static boolean generate() {
		try {
			File outputFile = getOutputFile();
			FileUtils.writeFile(outputFile, getContent(), GeneratorConstants.OUTPUT_XML_FILE_ENCODING);
		} catch (Exception e) {
			_logger.error(MessageUtils.getString("err.200"), e); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	private static File getOutputFile() {
		StringBuilder sb = new StringBuilder();
		sb.append(GeneratorUtils.getOutputPropertyPath());
		sb.append(GeneratorConstants.OUTPUT_STRUTS_CONFIG_FILE_NAME);
		return new File(sb.toString());
	}

	public static String getContent() {
		Document document = new Document(
				GeneratorConstants.STRUTS_CONFIG_PUBLIC_ID,
				GeneratorConstants.STRUTS_CONFIG_SYSTEM_ID);

		XmlElement root = new XmlElement("struts"); //$NON-NLS-1$
		document.setRootElement(root);

		XmlElement packageElement = new XmlElement("package"); //$NON-NLS-1$
		packageElement.addAttribute(new Attribute("name","WebAppOS")); //$NON-NLS-1$ //$NON-NLS-2$
		packageElement.addAttribute(new Attribute("extends","struts-default")); //$NON-NLS-1$ //$NON-NLS-2$
		packageElement.addAttribute(new Attribute("namespace",ControllerConstants.PATH_DELIMITER)); //$NON-NLS-1$
		root.addElement(packageElement);

		addExceitonElement(packageElement);

		for (ActionBean actionBean : WebAppOSCache.getInstance().getActionMap().values()) {
			if (GeneratorConstants.INPUT_HTML_ERROR_PAGE_DIR.equalsIgnoreCase(actionBean.getName())) {
				continue;
			}

			XmlElement actionElement = new XmlElement("action"); //$NON-NLS-1$
			actionElement.addAttribute(new Attribute("name",actionBean.getName())); //$NON-NLS-1$
			actionElement.addAttribute(new Attribute("class", GeneratorConstants.SYSTEM_GENERIC_ACTION_NAME)); //$NON-NLS-1$
			packageElement.addElement(actionElement);

			if (actionBean.getSubmitExecuteMap() != null) {
				for (ExecuteBean execInfo : actionBean.getSubmitExecuteMap().values()) {
					ResultBean resultInfo = execInfo.getResultInfo();
					XmlElement resultElement = new XmlElement("result"); //$NON-NLS-1$
					resultElement.addAttribute(new Attribute("name", resultInfo.getName())); //$NON-NLS-1$
					resultElement.addElement(new TextElement(resultInfo.getValue()));
					actionElement.addElement(resultElement);
				}
			}

			if (actionBean.getLoadExecuteMap() != null) {
				for (ExecuteBean execInfo : actionBean.getLoadExecuteMap().values()) {
					ResultBean resultInfo = execInfo.getResultInfo();
					XmlElement resultElement = new XmlElement("result"); //$NON-NLS-1$
					resultElement.addAttribute(new Attribute("name", resultInfo.getName())); //$NON-NLS-1$
					resultElement.addElement(new TextElement(resultInfo.getValue()));
					actionElement.addElement(resultElement);
				}
			}
		}

		return document.getFormattedContent();
	}

	private static void addExceitonElement(XmlElement parent) {
		XmlElement results = new XmlElement("global-results"); //$NON-NLS-1$

		XmlElement result = new XmlElement("result"); //$NON-NLS-1$
		result.addAttribute(new Attribute("name", GeneratorConstants.RESULT_NAME_NOT_FOUND)); //$NON-NLS-1$
		result.addAttribute(new Attribute("type", "httpheader")); //$NON-NLS-1$ //$NON-NLS-2$
		XmlElement error = new XmlElement("param"); //$NON-NLS-1$
		error.addAttribute(new Attribute("name", "error")); //$NON-NLS-1$ //$NON-NLS-2$
		error.addElement(new TextElement("404")); //$NON-NLS-1$
		result.addElement(error);
		results.addElement(result);

		result = new XmlElement("result"); //$NON-NLS-1$
		result.addAttribute(new Attribute("name", GeneratorConstants.RESULT_NAME_INVALID_TOKEN)); //$NON-NLS-1$
		result.addAttribute(new Attribute("type", "httpheader")); //$NON-NLS-1$ //$NON-NLS-2$
		error = new XmlElement("param"); //$NON-NLS-1$
		error.addAttribute(new Attribute("name", "error")); //$NON-NLS-1$ //$NON-NLS-2$
		error.addElement(new TextElement("403")); //$NON-NLS-1$
		result.addElement(error);
		results.addElement(result);

		result = new XmlElement("result"); //$NON-NLS-1$
		result.addAttribute(new Attribute("name", GeneratorConstants.RESULT_NAME_EXEC_DENIED)); //$NON-NLS-1$
		result.addAttribute(new Attribute("type", "httpheader")); //$NON-NLS-1$ //$NON-NLS-2$
		error = new XmlElement("param"); //$NON-NLS-1$
		error.addAttribute(new Attribute("name", "error")); //$NON-NLS-1$ //$NON-NLS-2$
		error.addElement(new TextElement("403")); //$NON-NLS-1$
		result.addElement(error);
		results.addElement(result);

		result = new XmlElement("result"); //$NON-NLS-1$
		result.addAttribute(new Attribute("name", GeneratorConstants.RESULT_NAME_CONFLICT)); //$NON-NLS-1$
		result.addAttribute(new Attribute("type", "httpheader")); //$NON-NLS-1$ //$NON-NLS-2$
		error = new XmlElement("param"); //$NON-NLS-1$
		error.addAttribute(new Attribute("name", "error")); //$NON-NLS-1$ //$NON-NLS-2$
		error.addElement(new TextElement("409")); //$NON-NLS-1$
		result.addElement(error);
		results.addElement(result);

		result = new XmlElement("result"); //$NON-NLS-1$
		result.addAttribute(new Attribute("name", GeneratorConstants.RESULT_NAME_INTERNAL_SERVER_ERROR)); //$NON-NLS-1$
		result.addAttribute(new Attribute("type", "httpheader")); //$NON-NLS-1$ //$NON-NLS-2$
		error = new XmlElement("param"); //$NON-NLS-1$
		error.addAttribute(new Attribute("name", "error")); //$NON-NLS-1$ //$NON-NLS-2$
		error.addElement(new TextElement("500")); //$NON-NLS-1$
		result.addElement(error);
		results.addElement(result);
		parent.addElement(results);

		XmlElement exceptionMappings = new XmlElement("global-exception-mappings"); //$NON-NLS-1$
		XmlElement exceptionMapping = new XmlElement("exception-mapping"); //$NON-NLS-1$
		exceptionMapping.addAttribute(new Attribute("result",GeneratorConstants.RESULT_NAME_NOT_FOUND)); //$NON-NLS-1$
		exceptionMapping.addAttribute(new Attribute("exception",GeneratorConstants.NOT_FOUND_EXCEPTION_CLASS)); //$NON-NLS-1$
		exceptionMappings.addElement(exceptionMapping);
		exceptionMapping = new XmlElement("exception-mapping"); //$NON-NLS-1$
		exceptionMapping.addAttribute(new Attribute("result", GeneratorConstants.RESULT_NAME_INVALID_TOKEN)); //$NON-NLS-1$
		exceptionMapping.addAttribute(new Attribute("exception", GeneratorConstants.INVALID_TOKEN_EXCEPTION_CLASS)); //$NON-NLS-1$
		exceptionMappings.addElement(exceptionMapping);
		exceptionMapping = new XmlElement("exception-mapping"); //$NON-NLS-1$
		exceptionMapping.addAttribute(new Attribute("result", GeneratorConstants.RESULT_NAME_CONFLICT)); //$NON-NLS-1$
		exceptionMapping.addAttribute(new Attribute("exception", GeneratorConstants.CONFLICT_EXCEPTION_CLASS)); //$NON-NLS-1$
		exceptionMappings.addElement(exceptionMapping);
		exceptionMapping = new XmlElement("exception-mapping"); //$NON-NLS-1$
		exceptionMapping.addAttribute(new Attribute("result", GeneratorConstants.RESULT_NAME_EXEC_DENIED)); //$NON-NLS-1$
		exceptionMapping.addAttribute(new Attribute("exception", GeneratorConstants.EXEC_DENIED_EXCEPTION_CLASS)); //$NON-NLS-1$
		exceptionMappings.addElement(exceptionMapping);
		exceptionMapping = new XmlElement("exception-mapping"); //$NON-NLS-1$
		exceptionMapping.addAttribute(new Attribute("result", GeneratorConstants.RESULT_NAME_INTERNAL_SERVER_ERROR)); //$NON-NLS-1$
		exceptionMapping.addAttribute(new Attribute("exception", Exception.class.getName())); //$NON-NLS-1$
		exceptionMappings.addElement(exceptionMapping);
		parent.addElement(exceptionMappings);
	}
}
