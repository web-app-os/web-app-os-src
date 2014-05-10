package jp.co.headwaters.webappos.generator.web;

import static jp.co.headwaters.webappos.controller.utils.ControllerUtils.*;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.cache.SystemConstantCache;
import jp.co.headwaters.webappos.controller.cache.WebAppOSCache;
import jp.co.headwaters.webappos.controller.cache.bean.AbstractExecuteBean;
import jp.co.headwaters.webappos.controller.cache.bean.ActionBean;
import jp.co.headwaters.webappos.controller.cache.bean.BindInputBean;
import jp.co.headwaters.webappos.controller.cache.bean.BindOutputBean;
import jp.co.headwaters.webappos.controller.cache.bean.CaseBean;
import jp.co.headwaters.webappos.controller.cache.bean.ConditionBean;
import jp.co.headwaters.webappos.controller.cache.bean.ExecuteBean;
import jp.co.headwaters.webappos.controller.cache.bean.FunctionBean;
import jp.co.headwaters.webappos.controller.cache.bean.ImageBean;
import jp.co.headwaters.webappos.controller.cache.bean.LoadBean;
import jp.co.headwaters.webappos.controller.cache.bean.ProcedureInfoBean;
import jp.co.headwaters.webappos.controller.cache.bean.ResultBean;
import jp.co.headwaters.webappos.controller.cache.bean.SystemConstantBean;
import jp.co.headwaters.webappos.controller.cache.bean.UrlPatternBean;
import jp.co.headwaters.webappos.controller.enumation.ContentTypeEnum;
import jp.co.headwaters.webappos.controller.enumation.CrudEnum;
import jp.co.headwaters.webappos.controller.enumation.DataTypeEnum;
import jp.co.headwaters.webappos.controller.enumation.FunctionEnum;
import jp.co.headwaters.webappos.controller.enumation.StretchTypeEnum;
import jp.co.headwaters.webappos.controller.utils.ControllerUtils;
import jp.co.headwaters.webappos.controller.utils.RegexUtils;
import jp.co.headwaters.webappos.generator.GeneratorConstants;
import jp.co.headwaters.webappos.generator.utils.FileUtils;
import jp.co.headwaters.webappos.generator.utils.GeneratorUtils;
import jp.co.headwaters.webappos.generator.utils.MessageUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.mybatis.generator.api.dom.OutputUtilities;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JspGenerator {

	private static final Log _logger = LogFactory.getLog(JspGenerator.class);
	private static String _webRootPath;
	private static String _htmlRootPath;
	private WebAppOSCache _webAppOSCache;
	private Map<String, IteratorInfo> _iteratorInfo;
	private boolean _isFullBuild;

	public JspGenerator(boolean isFullBuild){
		_isFullBuild = isFullBuild;
	}

	public boolean generate() {
		long lastBuildTime = -1;
		if (!_isFullBuild) {
			lastBuildTime = WebAppOSDatGenerator.getDatFile().lastModified();
		}

		List<String> warnings = new ArrayList<String>();

		// create action cache
		this._webAppOSCache = WebAppOSCache.getInstance();
		if (lastBuildTime < 0) {
			createActionCache(new File(GeneratorUtils.getInputHtmlPath()), null);

			// create jsp directory
			createJspDirectory();
		} else {
			try {
				this._webAppOSCache.load(WebAppOSDatGenerator.getDatFile());
			} catch (ClassNotFoundException | IOException e) {
				_logger.error(MessageUtils.getString("err.404"), e); //$NON-NLS-1$
				return false;
			}
		}

		// setting web and html root path
		setRootPath();

		for (ActionBean actionBean : this._webAppOSCache.getActionMap().values()) {
			File[] files = new File(getTargetHtmlPath(actionBean)).listFiles(FileUtils.getTargetFileFilter());
			for (File file : files) {
				try {
					if (lastBuildTime < file.lastModified()) {
						// parse html and generate jsp file
						_logger.debug(file.getAbsoluteFile());
						outputJspFile(actionBean, file, warnings);
					}
				} catch (IOException e) {
					_logger.error(MessageUtils.getString("err.400", file.getAbsolutePath()), e); //$NON-NLS-1$
					return false;
				}
			}
		}

		if (warnings.size() != 0) {
			for (String warning : warnings){
				_logger.error(warning);
			}
			return false;
		}
		return true;
	}

	private void createActionCache(File dir, String parent) {
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		String actionName = null;
		String actionPath = null;

		// for index.html
		ActionBean indexAction = new ActionBean("", ""); //$NON-NLS-1$ //$NON-NLS-2$
		indexAction.setSubmitExecuteMap(new HashMap<String, ExecuteBean>());
		indexAction.setLoadExecuteMap(new HashMap<String, ExecuteBean>());
		this._webAppOSCache.getActionMap().put("", indexAction); //$NON-NLS-1$

		for (File file : files) {
			if (file.isDirectory()) {
				if (!GeneratorConstants.INPUT_HTML_EXCLUDE_DIR.equalsIgnoreCase(file.getName())) {
					actionName = getActionName(parent, file.getName());

					// relative action directory path from context root
					actionPath = file.getAbsolutePath().replace(GeneratorUtils.getInputHtmlPath(), ""); //$NON-NLS-1$

					ActionBean action = new ActionBean(actionName, actionPath);
					action.setSubmitExecuteMap(new HashMap<String, ExecuteBean>());
					action.setLoadExecuteMap(new HashMap<String, ExecuteBean>());
					this._webAppOSCache.getActionMap().put(actionName, action);

					createActionCache(file, actionName);
				}
			}
		}
	}

	private String getActionName(String parent, String fileName){
		StringBuilder sb = new StringBuilder();
		if (parent != null) {
			sb.append(parent);
			sb.append(GeneratorConstants.ACTION_NAME_DELIMITER);
		}
		sb.append(fileName);
		return sb.toString();
	}

	private String getFormActionName(String attributeValue){
		StringBuilder sb = new StringBuilder();
		if (attributeValue.indexOf(_htmlRootPath) > 0) {
			attributeValue = attributeValue.substring(attributeValue.indexOf(_htmlRootPath) + _htmlRootPath.length());
			String[] path = attributeValue.split(ControllerConstants.PATH_DELIMITER);
			for (int i = 0; i < path.length - 1; i++) {
				if (sb.length() > 0) {
					sb.append(GeneratorConstants.ACTION_NAME_DELIMITER);
				}
				sb.append(path[i]);
			}
		}
		return sb.toString();
	}

	private void createJspDirectory() {
		StringBuilder sb = new StringBuilder();
		for (ActionBean actionBean : this._webAppOSCache.getActionMap().values()) {
			sb.setLength(0);
			sb.append(GeneratorUtils.getOutputJspPath());
			sb.append(actionBean.getHtmlPath());
			File file = new File(sb.toString());
			file.mkdirs();
		}
	}

	private static void setRootPath() {
		_htmlRootPath = getHtmlRootPath();
		_webRootPath = getWebRootPath();
	}

	private static String getHtmlRootPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(ControllerConstants.PATH_DELIMITER);
		sb.append(GeneratorConstants.INPUT_HTML_DIR);
		sb.append(ControllerConstants.PATH_DELIMITER);
		return sb.toString();
	}

	private static String getWebRootPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(ControllerConstants.PATH_DELIMITER);
		sb.append(GeneratorConstants.OUTPUT_WEB_ROOT_DIR);
		sb.append(ControllerConstants.PATH_DELIMITER);
		return sb.toString();
	}

	private static String getTargetHtmlPath(ActionBean actionBean) {
		StringBuilder sb = new StringBuilder();
		sb.append(GeneratorUtils.getInputHtmlPath());
		sb.append(actionBean.getHtmlPath());
		return sb.toString();
	}

	private static String getOutputPath(ActionBean actionBean, String fileName) {
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isEmpty(actionBean.getHtmlPath())){
			sb.append(actionBean.getHtmlPath());
			sb.append(getFileSparator());
		}
		sb.append(FileUtils.convertExtensionHtmlToJsp(fileName));
		return sb.toString();
	}

	private void outputJspFile(ActionBean actionBean, File inputFile, List<String> warnings) throws IOException {
		String outputPath = getOutputPath(actionBean, inputFile.getName());
		Document document = Jsoup.parse(inputFile, GeneratorConstants.INPUT_HTML_FILE_ENCODING);

		// iterator name list clear
		this._iteratorInfo = new HashMap<String, IteratorInfo>();

		ContentTypeEnum contentType = ContentTypeEnum.getContentType(FileUtils.getFileExtension(inputFile.getName()));
		if (contentType.equals(ContentTypeEnum.HTML)) {
			// replace path
			replacePath(document);
		}

		// parse data-erasure
		parseDataErasure(document);

		// parse data-load
		if (!GeneratorConstants.INPUT_HTML_ERROR_PAGE_DIR.equalsIgnoreCase(actionBean.getName())) {
			parseDataLoad(actionBean, outputPath, document);
		}

		if (contentType.equals(ContentTypeEnum.HTML)) {
			// parse data-func
			parseDataFunc(outputPath, document);
		}

		// parse data-image
		parseDataImage(actionBean, outputPath, document);

		// replace action path
		replaceHtmlPath(document.select("form[action]"), "action"); //$NON-NLS-1$ //$NON-NLS-2$

		// parse data-bind-out
		parseDataBindOutput(actionBean, outputPath, document);

		if (contentType.equals(ContentTypeEnum.HTML)) {
			// parse data-bind-in
			parseDataBindInput(document);
		}

		// parse data-iterator
		parseDataIterator(document);

		// parse data-case
		parseDataCase(document);

		// parse data-url
		if (!GeneratorConstants.INPUT_HTML_ERROR_PAGE_DIR.equalsIgnoreCase(actionBean.getName())) {
			parseDataUrl(document, actionBean.getName(), inputFile.getName());
		}

		// check Untreated data- Attribute
		checkUnParsedAttribute(document, inputFile.getAbsolutePath(), warnings);

		// save jsp file
		saveJspFile(document, outputPath, contentType);
	}

	private static void replacePath(Document document) {
		replaceWebPath(document);
		replaceHtmlPath(document.select("[href]"), "href"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static void replaceWebPath(Document document) {
		StringBuilder sb = new StringBuilder();
		String attributeValue = null;
		// TODO:とりあえず、data-originalも対応に
		for (String attributeKey : GeneratorConstants.HTML_REPLACE_ATTR_KEY) {
			for (Element element : document.getElementsByAttributeValueContaining(attributeKey, _webRootPath)) {
				sb.setLength(0);
				if (GeneratorUtils.getContextMode()) {
					sb.append(ControllerConstants.PATH_DELIMITER);
					sb.append(GeneratorUtils.getContextName());
				}
				attributeValue = element.attr(attributeKey);
				attributeValue = attributeValue.replace(GeneratorConstants.DEVELOP_SUFFIX, ""); //$NON-NLS-1$
				sb.append(ControllerConstants.PATH_DELIMITER);
				sb.append(attributeValue.substring(attributeValue.indexOf(_webRootPath) + _webRootPath.length()));
				sb.append("?var="); //$NON-NLS-1$
				sb.append(System.currentTimeMillis());
				element.attr(attributeKey, sb.toString());
			}
		}
	}

	private static void replaceHtmlPath(Elements elements, String attributeName) {
		StringBuilder sb = new StringBuilder();
		String value = null;
		String resultName = null;
		for (Element element : elements) {
			sb.setLength(0);
			value = element.attr(attributeName);
			if (value.indexOf(_htmlRootPath) > 0) {
				if (GeneratorUtils.getContextMode()) {
					sb.append(ControllerConstants.PATH_DELIMITER);
					sb.append(GeneratorUtils.getContextName());
				}
				value = value.substring(value.indexOf(_htmlRootPath) + _htmlRootPath.length());
				String[] path = value.split(ControllerConstants.PATH_DELIMITER);
				for (int i = 0; i < path.length; i++) {
					if (i == path.length - 1) {
						resultName = FileUtils.removeFileExtension(path[i]);
						break;
					}
					sb.append(ControllerConstants.PATH_DELIMITER);
					sb.append(path[i]);
				}
				sb.append(ControllerConstants.PATH_DELIMITER);
				if (!ControllerConstants.RESULT_NAME_DEFAULT.equalsIgnoreCase(resultName)) {
					sb.append(resultName);
					sb.append(ControllerConstants.PATH_DELIMITER);
				}
				element.attr(attributeName, sb.toString());
			}
		}
	}

	private void parseDataErasure(Document document) {
		Elements elements = document.getElementsByAttribute(GeneratorConstants.HTML_DATA_ATTR_NAME_ERASE);
		String type = null;
		for (Element element : elements) {
			type = element.attr(GeneratorConstants.HTML_DATA_ATTR_NAME_ERASE);
			if (GeneratorConstants.HTML_ERASURE_TYPE_OWN.equalsIgnoreCase(type)) {
				// remove own element
				elements.remove();
			} else if (GeneratorConstants.HTML_ERASURE_TYPE_CHILD.equalsIgnoreCase(type)) {
				// remove child element
				element.children().remove();
				// remove attribute
				element.removeAttr(GeneratorConstants.HTML_DATA_ATTR_NAME_ERASE);
			}
		}
	}

	private void parseDataImage(ActionBean actionBean, String outputPath, Document document)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		String params = null;
		ImageBean bean = null;

		Elements elements = document.getElementsByAttribute(GeneratorConstants.HTML_DATA_ATTR_NAME_IMAGE);
		for (Element element : elements) {
			params = element.attr(GeneratorConstants.HTML_DATA_ATTR_NAME_IMAGE);
			bean = mapper.readValue(params, ImageBean.class);

			if (!updateAttributeForImage(actionBean, outputPath, bean, element)){
				return;
			}

			// remove attribute
			element.removeAttr(GeneratorConstants.HTML_DATA_ATTR_NAME_IMAGE);
		}
	}

	private void parseDataLoad(ActionBean actionBean, String outputPath, Document document)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		List<AbstractExecuteBean> executeInfoList = null;
		String params = null;

		ExecuteBean executeInfo = new ExecuteBean();
		executeInfo.setResultInfo(createResultForLoad(outputPath));
		executeInfo.setExecuteInfoList(new ArrayList<AbstractExecuteBean>());
		actionBean.getLoadExecuteMap().put(outputPath, executeInfo);

		Element element = document.select(getQueryForLoad()).first();
		if (element != null) {
			// ---------------------------------------
			// parse data-load parameters
			// ---------------------------------------
			params = element.attr(GeneratorConstants.HTML_DATA_ATTR_NAME_LOAD);
			if (params.startsWith(GeneratorConstants.HTML_ARRAY_START_STRING)) {
				executeInfoList = mapper.readValue(params, new TypeReference<List<LoadBean>>() {});
			} else {
				executeInfoList = new ArrayList<AbstractExecuteBean>();
				executeInfoList.add(mapper.readValue(params, LoadBean.class));
			}

			// ---------------------------------------
			// set execute bean
			// ---------------------------------------
			executeInfo.getExecuteInfoList().addAll(executeInfoList);

			addIteratorResultNames(executeInfoList);

			// ---------------------------------------
			// remove attribute
			// ---------------------------------------
			element.removeAttr(GeneratorConstants.HTML_DATA_ATTR_NAME_LOAD);
		}
	}

	private void parseDataFunc(String outputPath, Document document)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = null;
		String params = null;
		Elements elements = document.select(getQueryForFunc());
		for (Element element : elements) {
			actionName = getFormActionName(element.attr("action")); //$NON-NLS-1$
			ActionBean actionBean = WebAppOSCache.getInstance().getActionMap().get(actionName);

			ExecuteBean executeInfo = new ExecuteBean();
			executeInfo.setResultInfo(createResultForSubmit(actionBean, element));
			executeInfo.setExecuteInfoList(new ArrayList<AbstractExecuteBean>());

			// ---------------------------------------
			// append form id element
			// ---------------------------------------
			String formId = addFormIdElement(outputPath, element);
			actionBean.getSubmitExecuteMap().put(formId, executeInfo);

			// ---------------------------------------
			// parse data-func parameters
			// ---------------------------------------
			List<AbstractExecuteBean> executeInfoList = null;
			params = element.attr(GeneratorConstants.HTML_DATA_ATTR_NAME_FUNC);
			if (params.startsWith(GeneratorConstants.HTML_ARRAY_START_STRING)) {
				executeInfoList = mapper.readValue(params, new TypeReference<List<FunctionBean>>() {});
			} else {
				executeInfoList = new ArrayList<AbstractExecuteBean>();
				executeInfoList.add(mapper.readValue(params, FunctionBean.class));
			}

			// ---------------------------------------
			// parse data-cond parameters
			// ---------------------------------------
			// form child element
			Elements condElements = element.select(getQueryForCond());
			for (Element condElement : condElements) {
				params = condElement.attr(GeneratorConstants.HTML_DATA_ATTR_NAME_COND);
				ConditionBean condition = mapper.readValue(params, ConditionBean.class);
				updateAttributeForCondition(condition, condElement);
				condElement.removeAttr(GeneratorConstants.HTML_DATA_ATTR_NAME_COND);

				// create operator map for request
				for (AbstractExecuteBean bean : executeInfoList) {
					if (bean.getResult().equalsIgnoreCase(condition.getResult())) {
						FunctionBean function = (FunctionBean)bean;
						if (function.getOperatorMap() == null) {
							function.setOperatorMap(new HashMap<String, String>());
						}
						function.getOperatorMap().putAll(createOperatorMap(condition));
					}
				}
			}

			// ---------------------------------------
			// set execute bean
			// ---------------------------------------
			executeInfo.getExecuteInfoList().addAll(executeInfoList);

			addIteratorResultNames(executeInfoList);

			// ---------------------------------------
			// append token element
			// ---------------------------------------
			for (AbstractExecuteBean bean : executeInfoList) {
				FunctionBean function = (FunctionBean)bean;
				if (function.isTokenValidation()){
					addTokenElement(element);
					break;
				}
			}

			// ---------------------------------------
			// remove attribute
			// ---------------------------------------
			element.removeAttr(GeneratorConstants.HTML_DATA_ATTR_NAME_FUNC);
		}
	}

	private void parseDataBindOutput(ActionBean actionBean, String outputPath, Document document)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		String params = null;
		List<BindOutputBean> binds = null;

		Elements elements = document.getElementsByAttribute(GeneratorConstants.HTML_DATA_ATTR_NAME_BIND_OUTPUT);
		for (Element element : elements) {
			params = element.attr(GeneratorConstants.HTML_DATA_ATTR_NAME_BIND_OUTPUT);
			if (params.startsWith(GeneratorConstants.HTML_ARRAY_START_STRING)) {
				binds = mapper.readValue(params, new TypeReference<List<BindOutputBean>>() {});
			} else {
				binds = new ArrayList<BindOutputBean>();
				binds.add(mapper.readValue(params, BindOutputBean.class));
			}

			for (BindOutputBean bind : binds) {
				if (!updateAttributeForBindOutput(actionBean, outputPath, bind, element)){
					return;
				}
			}
			// remove attribute
			element.removeAttr(GeneratorConstants.HTML_DATA_ATTR_NAME_BIND_OUTPUT);
		}
	}

	private void parseDataBindInput(Document document)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		String params = null;
		boolean isUpdated = false;
		List<BindInputBean> binds = null;

		Elements elements = document.getElementsByAttribute(GeneratorConstants.HTML_DATA_ATTR_NAME_BIND_INPUT);
		for (Element element : elements) {
			params = element.attr(GeneratorConstants.HTML_DATA_ATTR_NAME_BIND_INPUT);
			if (params.startsWith(GeneratorConstants.HTML_ARRAY_START_STRING)) {
				binds = mapper.readValue(params, new TypeReference<List<BindInputBean>>() {});
			} else {
				binds = new ArrayList<BindInputBean>();
				binds.add(mapper.readValue(params, BindInputBean.class));
			}

			for (BindInputBean bind : binds) {
				isUpdated = updateAttributeForBindInput(bind, element);
				if (!isUpdated) {
					return;
				}
			}
			// remove attribute
			element.removeAttr(GeneratorConstants.HTML_DATA_ATTR_NAME_BIND_INPUT);
		}
	}

	private void parseDataIterator(Document document) {
		for (IteratorInfo iteratorInfo : this._iteratorInfo.values()) {
			Elements elements = document.select(getQueryForIterator(iteratorInfo.getIteratorName()));
			for (Element element : elements) {
				removeSiblingElement(element);
				addIteratorElement(element, element.attr(GeneratorConstants.HTML_DATA_ATTR_NAME_ITERATOR), iteratorInfo);
				// remove attribute
				element.removeAttr(GeneratorConstants.HTML_DATA_ATTR_NAME_ITERATOR);
			}
		}
	}

	private void parseDataCase(Document document)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		String params = null;
		List<CaseBean> cases = null;

		StringBuilder sb = new StringBuilder();
		String expression = null;
		Elements elements = document.getElementsByAttribute(GeneratorConstants.HTML_DATA_ATTR_NAME_CASE);
		for (Element element : elements) {
			params = element.attr(GeneratorConstants.HTML_DATA_ATTR_NAME_CASE);
			if (params.startsWith(GeneratorConstants.HTML_ARRAY_START_STRING)) {
				cases = mapper.readValue(params, new TypeReference<List<CaseBean>>() {});
			} else {
				cases = new ArrayList<CaseBean>();
				cases.add(mapper.readValue(params, CaseBean.class));
			}

			for (CaseBean caseInfo : cases) {
				if (sb.length() > 0) {
					if (GeneratorConstants.HTML_CASE_OPERATOR_OR.equalsIgnoreCase(caseInfo.getOperator())) {
						sb.append(' ');
						sb.append(GeneratorConstants.HTML_CASE_OPERATOR_OR);
						sb.append(' ');
					} else {
						sb.append(' ');
						sb.append(GeneratorConstants.HTML_CASE_OPERATOR_AND);
						sb.append(' ');
					}
				}
				expression = getCaseExpression(caseInfo);
				if (!StringUtils.isEmpty(expression)) {
					sb.append(expression);
				} else {
					return;
				}
			}

			sb.insert(0, "<s:if test=\""); //$NON-NLS-1$
			sb.append("\">"); //$NON-NLS-1$
			sb.append("</s:if>"); //$NON-NLS-1$

			element.wrap(sb.toString());

			// remove attribute
			element.removeAttr(GeneratorConstants.HTML_DATA_ATTR_NAME_CASE);
			sb.setLength(0);
		}
	}

	private void parseDataUrl(Document document, String actionName, String fileName)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		List<String> patterns = null;
		String params = null;
		String defaultUrlPattern = null;
		UrlPatternBean urlPatternBean = null;
		String resultName = FileUtils.removeFileExtension(fileName);
		boolean isAuthRequire = false;

		defaultUrlPattern = getDefaultUrlPattern(actionName, resultName);

		Element authElement = document.select(getQueryForAuth()).first();
		if (authElement != null) {
			isAuthRequire = true;
		}

		Element element = document.select(getQueryForUrl()).first();
		if (element != null) {
			params = element.attr(GeneratorConstants.HTML_DATA_ATTR_NAME_URL);
			if (params.startsWith(GeneratorConstants.HTML_ARRAY_START_STRING)) {
				patterns = mapper.readValue(params, new TypeReference<List<String>>() {});
			} else {
				patterns = new ArrayList<String>();
				patterns.add(mapper.readValue(params, String.class));
			}

			StringBuilder sb = new StringBuilder();
			String pattern = null;
			for (String attr : patterns) {
				if (attr.contains(GeneratorConstants.HTML_ORIGINAL)) {
					// replace {_ORIGINAL}
					pattern = attr.replace(GeneratorConstants.HTML_ORIGINAL, defaultUrlPattern);
				} else {
					sb.setLength(0);
					if (GeneratorUtils.getContextMode()) {
						sb.append(ControllerConstants.PATH_DELIMITER);
						sb.append(GeneratorUtils.getContextName());
					}
					sb.append(attr);
					pattern = sb.toString();
				}
				pattern = pattern.replace(ControllerConstants.REGEX_REQUEST_CHAR, ControllerConstants.REGEX_REQUEST_PARAM_REPLACEMENT);
				urlPatternBean = new UrlPatternBean(RegexUtils.getUrlPatternRegex(pattern), actionName, resultName);
				urlPatternBean.setAuthRequire(isAuthRequire);
				if (this._webAppOSCache.getUrlPatternMap().containsKey(pattern)) {
					if (_isFullBuild) {
						_logger.warn(MessageUtils.getString("err.403", pattern)); //$NON-NLS-1$
					}
				} else {
					this._webAppOSCache.getUrlPatternMap().put(pattern, urlPatternBean);
				}
			}

			// remove attribute
			element.removeAttr(GeneratorConstants.HTML_DATA_ATTR_NAME_URL);
		} else {
			urlPatternBean = new UrlPatternBean(RegexUtils.getUrlPatternRegex(defaultUrlPattern), actionName, resultName);
			urlPatternBean.setAuthRequire(isAuthRequire);
			if (this._webAppOSCache.getUrlPatternMap().containsKey(defaultUrlPattern)) {
				if (_isFullBuild) {
					_logger.warn(MessageUtils.getString("err.403", defaultUrlPattern)); //$NON-NLS-1$
				}
			} else {
				this._webAppOSCache.getUrlPatternMap().put(defaultUrlPattern, urlPatternBean);
			}
		}
	}

	private String getDefaultUrlPattern(String actionName, String resultName){
		// set default url pattern
		StringBuilder sb = new StringBuilder();
		if (GeneratorUtils.getContextMode()) {
			sb.append(ControllerConstants.PATH_DELIMITER);
			sb.append(GeneratorUtils.getContextName());
		}
		sb.append(ControllerConstants.PATH_DELIMITER);
		if (!StringUtils.isEmpty(actionName)) {
			sb.append(actionName);
			sb.append(ControllerConstants.PATH_DELIMITER);
		}
		if (!ControllerConstants.RESULT_NAME_DEFAULT.equalsIgnoreCase(resultName)) {
			sb.append(resultName);
			sb.append(ControllerConstants.PATH_DELIMITER);
		}
		return sb.toString();
	}

	private void checkUnParsedAttribute(Document document, String inputFilePath, List<String> warnings) {
		Elements elements = document.select(getQueryForUnParsedAttribute());
		String key = null;
		for (Element element : elements) {
			Attributes attributes = element.attributes();
			for (Attribute attribute : attributes){
				key = attribute.getKey();
				Pattern pattern = Pattern.compile(GeneratorConstants.REGEX_TARGET_DATA_ATTR);
				Matcher matcher = pattern.matcher(key);
				if (matcher.find()) {
					warnings.add(MessageUtils.getString("err.401", inputFilePath, key, attribute.getValue())); //$NON-NLS-1$
				}
			}
		}
	}

	private static String getQueryForLoad() {
		StringBuilder sb = new StringBuilder();
		sb.append("["); //$NON-NLS-1$
		sb.append(GeneratorConstants.HTML_DATA_ATTR_NAME_LOAD);
		sb.append("]"); //$NON-NLS-1$
		return sb.toString();
	}

	private static String getQueryForFunc() {
		StringBuilder sb = new StringBuilder();
		sb.append("form["); //$NON-NLS-1$
		sb.append(GeneratorConstants.HTML_DATA_ATTR_NAME_FUNC);
		sb.append("]"); //$NON-NLS-1$
		return sb.toString();
	}

	private static String getQueryForCond() {
		StringBuilder sb = new StringBuilder();
		sb.append("input["); //$NON-NLS-1$
		sb.append(GeneratorConstants.HTML_DATA_ATTR_NAME_COND);
		sb.append("]"); //$NON-NLS-1$
		return sb.toString();
	}

	private static String getQueryForIterator(String resultName) {
		StringBuilder sb = new StringBuilder();
		sb.append("["); //$NON-NLS-1$
		sb.append(GeneratorConstants.HTML_DATA_ATTR_NAME_ITERATOR);
		sb.append("^="); //$NON-NLS-1$
		sb.append(resultName);
		sb.append("]"); //$NON-NLS-1$
		return sb.toString();
	}

	private static String getQueryForUnParsedAttribute() {
		StringBuilder sb = new StringBuilder();
		sb.append("[^"); //$NON-NLS-1$
		sb.append(GeneratorConstants.HTML_DATA_ATTR_PREFIX);
		sb.append("]"); //$NON-NLS-1$
		return sb.toString();
	}

	private static String getQueryForUrl() {
		StringBuilder sb = new StringBuilder();
		sb.append("["); //$NON-NLS-1$
		sb.append(GeneratorConstants.HTML_DATA_ATTR_NAME_URL);
		sb.append("]"); //$NON-NLS-1$
		return sb.toString();
	}

	private static String getQueryForAuth() {
		StringBuilder sb = new StringBuilder();
		sb.append("head["); //$NON-NLS-1$
		sb.append(GeneratorConstants.HTML_DATA_ATTR_NAME_AUTH);
		sb.append("]"); //$NON-NLS-1$
		return sb.toString();
	}

	private static ResultBean createResultForLoad(String outputPath) {
		StringBuilder sb = new StringBuilder();
		sb.append(getFileSparator());
		sb.append(GeneratorConstants.OUTPUT_JSP_DIR);
		sb.append(getFileSparator());
		sb.append(outputPath);

		ResultBean result = new ResultBean();
		String fileName = outputPath.substring(outputPath.indexOf(getFileSparator()) + 1);
		result.setName(fileName.replace(ControllerConstants.JSP_EXTENSION, "")); //$NON-NLS-1$
		result.setValue(sb.toString());
		return result;
	}

	private static ResultBean createResultForSubmit(ActionBean actionBean, Element formElement) {
		ResultBean result = new ResultBean();
		StringBuilder sb = new StringBuilder();

		String value = formElement.attr("action"); //$NON-NLS-1$
		int beginIndex = value.indexOf(_htmlRootPath) + _htmlRootPath.length();
		value = value.substring(beginIndex);
		value = FileUtils.convertExtensionHtmlToJsp(value);
		value = value.replace(ControllerConstants.PATH_DELIMITER, getFileSparator());

		sb.append(getFileSparator());
		sb.append(GeneratorConstants.OUTPUT_JSP_DIR);
		sb.append(getFileSparator());
		sb.append(value);
		result.setValue(sb.toString());

		sb.setLength(0);
		sb.append(GeneratorConstants.STRUTS_RESULT_PREFIX);
		sb.append(actionBean.getSubmitExecuteMap().size());
		result.setName(sb.toString());

		return result;
	}

	private static String addFormIdElement(String outputPath, Element formElement) {
		StringBuilder sb = new StringBuilder();
		sb.append(outputPath);
		sb.append(formElement.attr("id")); //$NON-NLS-1$
		String formId = String.valueOf(sb.toString().hashCode());

		Element element = formElement.appendElement("input"); //$NON-NLS-1$
		element.attr("type", "hidden");  //$NON-NLS-1$//$NON-NLS-2$
		element.attr("name", ControllerConstants.ELEMENT_NAME_FORM_ID); //$NON-NLS-1$
		element.attr("value", formId); //$NON-NLS-1$
		return formId;
	}

	private static void addTokenElement(Element formElement) {
		formElement.appendText("<s:token />"); //$NON-NLS-1$
	}

	private void updateAttributeForCondition(ConditionBean condition, Element element) {
		String[] column = condition.getColumnName().split(GeneratorConstants.HTML_GENERATOR_DELIMITER);
		StringBuilder sb = new StringBuilder();
		// name
		sb.append(condition.getResult());
		sb.append(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER);
		sb.append(ControllerConstants.REQUEST_PARAM_NAME_CRUD_CONDITION);
		sb.append(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER);
		sb.append(column[0]);
		sb.append(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER);
		sb.append(column[1]);
		element.attr("name", sb.toString()); //$NON-NLS-1$

		// value
		sb.setLength(0);
		sb.append("<s:property value=\""); //$NON-NLS-1$
		sb.append(getBindValue(null, element.attr("name"), condition.getValue(), null, false)); //$NON-NLS-1$
		sb.append("\"/>"); //$NON-NLS-1$
		element.attr("value", sb.toString()); //$NON-NLS-1$
	}

	private static Map<String, String> createOperatorMap(ConditionBean condition) {
		Map<String, String> operatorMap = new HashMap<String, String>();
		StringBuilder sb = new StringBuilder();
		sb.setLength(0);
		sb.append(condition.getResult());
		sb.append('.');
		sb.append(condition.getColumnName());
		operatorMap.put(sb.toString(), condition.getOperator());
		return operatorMap;
	}

	private boolean updateAttributeForImage(ActionBean actionBean, String outputPath, ImageBean imageBean, Element element)
			throws JsonParseException, JsonMappingException, IOException {
		String target = imageBean.getTarget();
		if (imageBean.getArg() == null) {
			return false;
		}

		if (target == null) {
			target = "src"; //$NON-NLS-1$
		}

		StringBuilder key = new StringBuilder();
		String[] args = imageBean.getArg().split(GeneratorConstants.HTML_GENERATOR_DELIMITER);
		if (args.length == 2) {
			key.append(args[0]);
			key.append(ControllerConstants.TABLE_COLUMN_DELIMITER);
			key.append(args[1]);
		} else {
			key.append(imageBean.getArg());
		}

		StringBuilder sb = new StringBuilder();
		if (GeneratorUtils.getContextMode()) {
			sb.append(ControllerConstants.PATH_DELIMITER);
			sb.append(GeneratorUtils.getContextName());
		}
		sb.append(ControllerConstants.API_GET_IMAGE_URL);
		sb.append("?"); //$NON-NLS-1$
		sb.append("path="); //$NON-NLS-1$
		sb.append("<s:property value=\""); //$NON-NLS-1$
		sb.append(getEncodeBindValue(imageBean.getResult(), key.toString()));
		sb.append("\""); //$NON-NLS-1$
		sb.append("/>"); //$NON-NLS-1$
		if (imageBean.getWidth() != null) {
			sb.append("&dw="); //$NON-NLS-1$
			sb.append(imageBean.getWidth());
		}
		if (imageBean.getHeight() != null) {
			sb.append("&dh="); //$NON-NLS-1$
			sb.append(imageBean.getHeight());
		}
		if (imageBean.getStretchType() != null) {
			sb.append("&type="); //$NON-NLS-1$
			StretchTypeEnum type = StretchTypeEnum.getStretchType(imageBean.getStretchType());
			sb.append(type.getKey());
		}

		List<Node> childs = null;
		// update text or attribute
		if (GeneratorConstants.HTML_BIND_TARGET_TEXT.equalsIgnoreCase(target)) {
			childs = element.childNodes();
			if (childs != null) {
				for (int i = childs.size() - 1; i >= 0; i--) {
					Node child = childs.get(i);
					if (child instanceof TextNode) {
						if (!((TextNode) child).text().contains("s:property")) { //$NON-NLS-1$
							child.remove();
						}
					}
				}
			}
			element.appendChild(new TextNode(sb.toString(), null));
		} else {
			element.attr(target, sb.toString());
		}
		return true;
	}

	private boolean updateAttributeForBindOutput(ActionBean actionBean, String outputPath, BindOutputBean bindBean, Element element)
			throws JsonParseException, JsonMappingException, IOException {
		StringBuilder sb = new StringBuilder();
		String format = null;
		// format
		format = bindBean.getFormat();
		if (element.hasText()) {
			if (element.text().contains(GeneratorConstants.HTML_REPLACE_STRING)) {
				format = element.text();
			}
		}
		if (StringUtils.isEmpty(format)) {
			format = GeneratorConstants.HTML_REPLACE_STRING;
		}
		if (!GeneratorConstants.HTML_BIND_TARGET_TEXT.equalsIgnoreCase(bindBean.getTarget())) {
			format = format.replace("\"", "''"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (format.contains(GeneratorConstants.HTML_ORIGINAL)) {
			// replace {_ORIGINAL}
			if (GeneratorConstants.HTML_BIND_TARGET_TEXT.equalsIgnoreCase(bindBean.getTarget())) {
				format = format.replace(GeneratorConstants.HTML_ORIGINAL, element.text());
			} else {
				format = format.replace(GeneratorConstants.HTML_ORIGINAL, element.attr(bindBean.getTarget()));
			}
		} else {
			if ("src".equalsIgnoreCase(bindBean.getTarget()) || "href".equalsIgnoreCase(bindBean.getTarget())) { //$NON-NLS-1$ //$NON-NLS-2$
				if (!GeneratorConstants.HTML_REPLACE_STRING.equals(format)) {
					if (GeneratorUtils.getContextMode()) {
						sb.append(ControllerConstants.PATH_DELIMITER);
						sb.append(GeneratorUtils.getContextName());
					}
					sb.append(format);
					format = sb.toString();
				}
			}
		}

		// arg or args
		String value = null;
		if (bindBean.getArgs() != null && bindBean.getArgs().size() > 0) {
			String[] args = new String[bindBean.getArgs().size()];
			for (int i = 0; i < bindBean.getArgs().size(); i++) {
				if (StringUtils.isEmpty(bindBean.getArgs().get(i))) {
					args[i] = "<s:property />"; //$NON-NLS-1$
				} else {
					args[i] = getBindPropertyForOutput(bindBean, bindBean.getArgs().get(i), null);
				}
			}
			value = MessageFormat.format(format, (Object[])args);
		} else {
			if (bindBean.getArg() == null) {
				value = MessageFormat.format(format, "<s:property />"); //$NON-NLS-1$
			} else if (StringUtils.isEmpty(bindBean.getArg())) {
				element.removeAttr(bindBean.getTarget());
				return true;
			} else {
				if (istPatternFormat(bindBean.getFormat())) {
					format = GeneratorConstants.HTML_REPLACE_STRING;
					if (element.hasText()) {
						if (element.text().contains(GeneratorConstants.HTML_REPLACE_STRING)) {
							format = element.text();
						}
					}
					value = MessageFormat.format(format, getBindPropertyForOutput(bindBean, bindBean.getArg(), bindBean.getFormat()));
				} else {
					value = MessageFormat.format(format, getBindPropertyForOutput(bindBean, bindBean.getArg(), null));
				}
			}
		}

		List<Node> childs = null;
		// update text or attribute
		if (GeneratorConstants.HTML_BIND_TARGET_TEXT.equalsIgnoreCase(bindBean.getTarget())) {
			childs = element.childNodes();
			if (childs != null) {
				for (int i = childs.size() - 1; i >= 0; i--) {
					Node child = childs.get(i);
					if (child instanceof TextNode) {
						if (!((TextNode) child).text().contains("s:property")) { //$NON-NLS-1$
							child.remove();
						}
					}
				}
			}
			element.prependChild(new TextNode(value, null));
		} else {
			if (GeneratorConstants.HTML_BIND_TARGET_INNER.equalsIgnoreCase(bindBean.getTarget())) {
				element.html(value);
			} else {
				if (bindBean.getTarget() != null) {
					if (!element.attr(bindBean.getTarget()).contains("s:property")) { //$NON-NLS-1$
						// clear attribute
						element.removeAttr(bindBean.getTarget());
					}
					sb.setLength(0);
					sb.append(element.attr(bindBean.getTarget()));
					sb.append(value);
					element.attr(bindBean.getTarget(), sb.toString());
				}
			}
		}
		return true;
	}

	private boolean updateAttributeForBindInput(BindInputBean bind, Element element)
			throws JsonParseException, JsonMappingException, IOException {
		if (!element.tagName().equalsIgnoreCase("input") && !element.tagName().equalsIgnoreCase("textarea")) { //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}

		String tagName = getBindInputTagName(bind);
		if (tagName == null) {
			return false;
		}
		element.attr("name", tagName); //$NON-NLS-1$

		// arg
		String value = null;
		if (bind.getArg() != null) {
			value = getBindPropertyForInput(bind, tagName);
		}

		// set value
		element.attr("value", element.attr("value") + value); //$NON-NLS-1$ //$NON-NLS-2$
		return true;
	}

	private static String getBindInputTagName(BindInputBean bind) {
		StringBuilder sb = new StringBuilder();
		String[] args = bind.getArg().split(GeneratorConstants.HTML_GENERATOR_DELIMITER);
		sb.append(bind.getResult());
		sb.append(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER);
		sb.append(ControllerConstants.REQUEST_PARAM_NAME_CRUD_COLUMN);
		sb.append(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER);
		if (args.length >= 2) {
			sb.append(args[1]);
		} else {
			sb.append(bind.getArg());
		}
		return sb.toString();
	}

	private String getBindPropertyForOutput(BindOutputBean bind, String arg, String pattern) {
		StringBuilder sb = new StringBuilder();
		StringBuilder key = new StringBuilder();
		boolean isEscape = Boolean.valueOf(bind.getEscape());
		boolean isEraseTag = Boolean.valueOf(bind.getEraseTag());
		String[] args = arg.split(GeneratorConstants.HTML_GENERATOR_DELIMITER);
		String type = args[0];
		if (ControllerConstants.RESULT_MAP_KEY_PAGER.equalsIgnoreCase(type)) {
			key.append(ControllerConstants.RESULT_MAP_KEY_PAGER);
			key.append(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER);
			key.append(bind.getResult());
			key.append('.');
			key.append(args[1]);

			sb.append("<s:property value=\""); //$NON-NLS-1$
			sb.append(getBindValue(null, key.toString(), null, null, isEraseTag));
			sb.append("\""); //$NON-NLS-1$
			sb.append("/>"); //$NON-NLS-1$
		} else if (GeneratorConstants.JSP_ITERATOR_STATUS.equalsIgnoreCase(type)) {
			sb.append("<s:property value=\""); //$NON-NLS-1$
			sb.append("#"); //$NON-NLS-1$
			sb.append(GeneratorConstants.JSP_ITERATOR_STATUS);
			sb.append('.');
			sb.append(args[1].toLowerCase());
			sb.append("\""); //$NON-NLS-1$
			sb.append("/>"); //$NON-NLS-1$
		} else {
			if (bind.getMapKeyName() == null &&
					(ControllerConstants.RESULT_MAP_KEY_REQUEST.equalsIgnoreCase(type)
					|| ControllerConstants.RESULT_MAP_KEY_CONSTANT.equalsIgnoreCase(type))) {
				key.append(type);
				key.append('.');
				key.append(args[1]);

				sb.append("<s:property value=\""); //$NON-NLS-1$
				if (pattern != null) {
					sb.append(getBindValueWithFomat(null, key.toString(), bind.getValue(), pattern, isEraseTag));
				} else {
					sb.append(getBindValue(null, key.toString(), bind.getValue(), null, isEraseTag));
				}
				sb.append("\""); //$NON-NLS-1$
				sb.append("/>"); //$NON-NLS-1$
				return sb.toString();
			} else if (args.length == 2
					&& !ControllerConstants.RESULT_MAP_KEY_SESSION.equalsIgnoreCase(type)
					&& !ControllerConstants.RESULT_MAP_KEY_REQUEST.equalsIgnoreCase(type)) {
				// tablename_columnname
				key.append(args[0]);
				key.append(ControllerConstants.TABLE_COLUMN_DELIMITER);
				key.append(args[1]);
			} else {
				key.append(arg);
			}

			sb.append("<s:property value=\""); //$NON-NLS-1$
			if (pattern != null) {
				sb.append(getBindValueWithFomat(bind.getResult(), key.toString(), bind.getValue(), pattern, isEraseTag));
			} else {
				sb.append(getBindValue(bind.getResult(), key.toString(), bind.getValue(), bind.getMapKeyName(), isEraseTag));
			}
			sb.append("\""); //$NON-NLS-1$
			if (!isEscape){
				sb.append(GeneratorConstants.JSP_PROPERTY_ESCAPE);
			}
			sb.append("/>"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	private String getBindPropertyForInput(BindInputBean bind, String tagName) {
		StringBuilder sb = new StringBuilder();
		StringBuilder key = new StringBuilder();
		String[] args = bind.getArg().split(GeneratorConstants.HTML_GENERATOR_DELIMITER);
		String type = args[0];
		if (ControllerConstants.RESULT_MAP_KEY_PAGER.equalsIgnoreCase(type)) {
			key.append(ControllerConstants.RESULT_MAP_KEY_PAGER);
			key.append(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER);
			key.append(bind.getResult());
			key.append('.');
			key.append(args[1]);

			sb.append("<s:property value=\""); //$NON-NLS-1$
			sb.append(getBindValue(null, key.toString(), null, null, false));
			sb.append("\""); //$NON-NLS-1$
			sb.append("/>"); //$NON-NLS-1$
		} else {
			if (ControllerConstants.RESULT_MAP_KEY_REQUEST.equalsIgnoreCase(type)
					|| ControllerConstants.RESULT_MAP_KEY_CONSTANT.equalsIgnoreCase(type)) {
				key.append(type);
				key.append('.');
				key.append(args[1]);
			} else {
				key.append(tagName);
			}
			sb.append("<s:property value=\""); //$NON-NLS-1$
			sb.append(getBindValue(ControllerConstants.REQUEST_PARAM_NAME_CRUD_COLUMN, key.toString(), bind.getValue(), null, false));
			sb.append("\""); //$NON-NLS-1$
			sb.append("/>"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	private String getBindValue(String resultName, String key, String defaultKey, String mapKeyName, boolean isEraseTag) {
		String mapKey = ""; //$NON-NLS-1$
		if (!StringUtils.isEmpty(mapKeyName)) {
			mapKey = mapKeyName;
		}

		String getValueString = null;
		if (resultName != null && this._iteratorInfo.containsKey(resultName)) {
			if (defaultKey == null) {
				getValueString = MessageFormat.format(GeneratorConstants.JSP_METHOD_GET_VALUE_FOR_LIST,
						"'" + key.toUpperCase() + "'", //$NON-NLS-1$ //$NON-NLS-2$
						"'" + mapKey + "'" //$NON-NLS-1$ //$NON-NLS-2$
						);
			} else {
				getValueString = MessageFormat.format(GeneratorConstants.JSP_METHOD_GET_VALUE_WITH_DEFAULT_FOR_LIST,
						"'" + key.toUpperCase() + "'", //$NON-NLS-1$ //$NON-NLS-2$
						"'" + defaultKey.toUpperCase() + "'", //$NON-NLS-1$ //$NON-NLS-2$
						"'" + mapKey + "'" //$NON-NLS-1$ //$NON-NLS-2$
						);
			}
		} else {
			StringBuilder sb = new StringBuilder();
			if (resultName == null) {
				sb.append(key.toUpperCase());
			} else {
				sb.append(resultName);
				sb.append('.');
				sb.append(key.toUpperCase());
			}

			if (defaultKey == null) {
				getValueString = MessageFormat.format(GeneratorConstants.JSP_METHOD_GET_VALUE,
						"'" + sb.toString() + "'", //$NON-NLS-1$ //$NON-NLS-2$
						"'" + mapKey + "'" //$NON-NLS-1$ //$NON-NLS-2$
						);

			} else {
				getValueString = MessageFormat.format(GeneratorConstants.JSP_METHOD_GET_VALUE_WITH_DEFAULT,
						"'" + sb.toString() + "'", //$NON-NLS-1$ //$NON-NLS-2$
						"'" + defaultKey.toUpperCase() + "'", //$NON-NLS-1$ //$NON-NLS-2$
						"'" + mapKey + "'" //$NON-NLS-1$ //$NON-NLS-2$
						);
			}
		}

		StringBuilder result = null;
		if (isEraseTag) {
			result = new StringBuilder();
			result.append("eraseTag(");
			result.append(getValueString);
			result.append(")");
		} else {
			result = new StringBuilder(getValueString);
		}
		return result.toString();
	}

	private String getEncodeBindValue(String resultName, String key) {
		if (resultName != null && this._iteratorInfo.containsKey(resultName)) {
			return MessageFormat.format(GeneratorConstants.JSP_METHOD_GET_ENCODE_VALUE_FOR_LIST,
					"'" + key.toUpperCase() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			StringBuilder sb = new StringBuilder();
			if (resultName == null) {
				sb.append(key.toUpperCase());
			} else {
				sb.append(resultName);
				sb.append('.');
				sb.append(key.toUpperCase());
			}
			return MessageFormat.format(GeneratorConstants.JSP_METHOD_GET_ENCODE_VALUE,
					"'" + sb.toString() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private String getBindValueWithFomat(String resultName, String key, String defaultKey, String pattern, boolean isEraseTag) {
		String datePattarn = getDateFormatPattern(pattern);
		if (resultName != null && this._iteratorInfo.containsKey(resultName)) {
			if (datePattarn != null){
				if (defaultKey == null) {
					return MessageFormat.format(GeneratorConstants.JSP_METHOD_DATE_FORMAT_FOR_LIST,
							"'" + key.toUpperCase() + "'", //$NON-NLS-1$ //$NON-NLS-2$
							"'" + datePattarn + "'"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					return MessageFormat.format(GeneratorConstants.JSP_METHOD_DATE_FORMAT_WITH_DEFAULT_FOR_LIST,
							"'" + key.toUpperCase() + "'", //$NON-NLS-1$ //$NON-NLS-2$
							"'" + defaultKey.toUpperCase() + "'", //$NON-NLS-1$ //$NON-NLS-2$
							"'" + datePattarn + "'"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else {
				String numberPattarn = getNumberFormatPattern(pattern);
				if (numberPattarn != null) {
					if (defaultKey == null) {
						return MessageFormat.format(GeneratorConstants.JSP_METHOD_NUMBER_FORMAT_FOR_LIST,
								"'" + key.toUpperCase() + "'", //$NON-NLS-1$ //$NON-NLS-2$
								"'" + numberPattarn + "'"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						return MessageFormat.format(GeneratorConstants.JSP_METHOD_NUMBER_FORMAT_WITH_DEFAULT_FOR_LIST,
								"'" + key.toUpperCase() + "'", //$NON-NLS-1$ //$NON-NLS-2$
								"'" + numberPattarn + "'"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		} else {
			StringBuilder sb = new StringBuilder();
			if (resultName == null) {
				sb.append(key.toUpperCase());
			} else {
				sb.append(resultName);
				sb.append('.');
				sb.append(key.toUpperCase());
			}
			if (datePattarn != null){
				if (defaultKey == null) {
					return MessageFormat.format(GeneratorConstants.JSP_METHOD_DATE_FORMAT,
							"'" + sb.toString() + "'", //$NON-NLS-1$ //$NON-NLS-2$
							"'" + datePattarn + "'"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					return MessageFormat.format(GeneratorConstants.JSP_METHOD_DATE_FORMAT_WITH_DEFAULT,
							"'" + sb.toString() + "'", //$NON-NLS-1$ //$NON-NLS-2$
							"'" + defaultKey.toUpperCase() + "'", //$NON-NLS-1$ //$NON-NLS-2$
							"'" + datePattarn + "'"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else {
				String numberPattarn = getNumberFormatPattern(pattern);
				if (numberPattarn != null) {
					if (defaultKey == null) {
						return MessageFormat.format(GeneratorConstants.JSP_METHOD_NUMBER_FORMAT,
								"'" + sb.toString() + "'", //$NON-NLS-1$ //$NON-NLS-2$
								"'" + numberPattarn + "'"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						return MessageFormat.format(GeneratorConstants.JSP_METHOD_NUMBER_FORMAT_WITH_DEFAULT,
								"'" + sb.toString() + "'", //$NON-NLS-1$ //$NON-NLS-2$
								"'" + numberPattarn + "'"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		}
		return getBindValue(resultName, key, defaultKey, null, isEraseTag);
	}

	private boolean istPatternFormat(String format) {
		if (format == null) return false;

		if (getDateFormatPattern(format) != null
				|| getNumberFormatPattern(format) != null){
			return true;
		}
		return false;
	}

	private String getDateFormatPattern(String format) {
		Pattern pattern = Pattern.compile(GeneratorConstants.REGEX_DATE_FORMAT);
		Matcher matcher = pattern.matcher(format);
		if (matcher.find()) {
			String target = matcher.group();
			return format.substring(target.indexOf("[") + 1, target.indexOf("]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return null;
	}

	private String getNumberFormatPattern(String format) {
		Pattern pattern = Pattern.compile(GeneratorConstants.REGEX_NUMBER_FORMAT);
		Matcher matcher = pattern.matcher(format);
		if (matcher.find()) {
			String target = matcher.group();
			return format.substring(target.indexOf("[") + 1, target.indexOf("]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return null;
	}

	private void addIteratorResultNames(List<AbstractExecuteBean> executeInfoList) {
		for (AbstractExecuteBean executeInfo : executeInfoList) {
			String method = executeInfo.getMethod();
			if (CrudEnum.SELECT_ALL_BY_EXAMPLE.getMethod().equalsIgnoreCase(method)
					|| CrudEnum.SELECT_BY_EXAMPLE.getMethod().equalsIgnoreCase(method)) {
				// Multiple Result
				if (!this._iteratorInfo.containsKey(executeInfo.getResult())) {
					IteratorInfo iteratorInfo = new IteratorInfo(executeInfo.getTarget().split(",")[0].trim(), executeInfo.getResult());
					this._iteratorInfo.put(executeInfo.getResult(), iteratorInfo);
				}
			} else if (FunctionEnum.FUNCTION_PROCEDURE.getFunctionName().equalsIgnoreCase(executeInfo.getType())) {
				ProcedureInfoBean procedureInfo = WebAppOSCache.getInstance().getProcedureMap().get(executeInfo.getTarget().toLowerCase());
				if (DataTypeEnum.DATA_TYPE_RECORD.getDataType().equalsIgnoreCase(procedureInfo.getRetType().getDataType())) {
					// Multiple Result
					if (!this._iteratorInfo.containsKey(executeInfo.getResult())) {
						IteratorInfo iteratorInfo = new IteratorInfo(executeInfo.getTarget(), executeInfo.getResult());
						this._iteratorInfo.put(executeInfo.getResult(), iteratorInfo);
					}
				}
			}
		}
	}

	private static void addIteratorElement(Element element, String params, IteratorInfo iteratorInfo) {
		params = params.toUpperCase();
		String[] args = params.split(ControllerConstants.REGEX_COLUMN_DELIMITER);

		StringBuilder sb = new StringBuilder();
		sb.append("<s:iterator value=\"%{"); //$NON-NLS-1$
		if (args.length > 1 && args[1].equalsIgnoreCase(ControllerConstants.CRUD_PAGER_PAGE_NO_LIST)) {
			// pager
			sb.append(ControllerConstants.RESULT_MAP_KEY_ROOT);
			sb.append('.');
			sb.append(ControllerConstants.RESULT_MAP_KEY_PAGER);
			sb.append(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER);
			sb.append(args[0]);
			sb.append('.');
			sb.append(ControllerConstants.CRUD_PAGER_PAGE_NO_LIST);
		} else {
			String subMapName = null;
			// result名.テーブル名
			if (args.length > 2) {
				subMapName = ControllerUtils.getResultMapKey(args[args.length - 2], args[args.length - 1]);
			}
			if (subMapName == null) {
				sb.append(ControllerConstants.RESULT_MAP_KEY_ROOT);
				sb.append('.');
				sb.append(iteratorInfo.getIteratorName());
				sb.append('.');
				sb.append(iteratorInfo.getTargetName());
			} else {
				sb.append(subMapName);
			}
		}
		sb.append("}\" "); //$NON-NLS-1$
		sb.append("status=\""); //$NON-NLS-1$
		sb.append(GeneratorConstants.JSP_ITERATOR_STATUS);
		sb.append("\" "); //$NON-NLS-1$
		sb.append("var=\""); //$NON-NLS-1$
		sb.append(GeneratorConstants.JSP_ITERATOR_VAR);
		sb.append("\">"); //$NON-NLS-1$
		sb.append("</s:iterator>"); //$NON-NLS-1$
		element.wrap(sb.toString());
	}

	private String getCaseExpression(CaseBean caseInfo) {
		StringBuilder sb = new StringBuilder();
		if (GeneratorConstants.HTML_CASE_TYPE_SIZE.equalsIgnoreCase(caseInfo.getType())) {
			String[] column = caseInfo.getArgs().get(0).split(GeneratorConstants.HTML_GENERATOR_DELIMITER);
			IteratorInfo iteratorInfo = this._iteratorInfo.get(column[0]);
			if (iteratorInfo == null){
				return null;
			}
			if (column.length == 1) {
				sb.append(ControllerConstants.RESULT_MAP_KEY_ROOT);
				sb.append('.');
				sb.append(iteratorInfo.getIteratorName());
			} else if (column.length == 2) {
				sb.append(ControllerConstants.RESULT_MAP_KEY_ROOT);
				sb.append('.');
				sb.append(caseInfo.getArgs().get(0));
			} else {
				sb.append(ControllerUtils.getResultMapKey(column[column.length - 2], column[column.length - 1]));
			}
			sb.append(".size"); //$NON-NLS-1$
			sb.append(getOperatorString(caseInfo.getArgs().get(1)));
			sb.append(caseInfo.getArgs().get(2));
		} else {
			StringBuilder key = new StringBuilder();
			String[] column = caseInfo.getArgs().get(0).split(GeneratorConstants.HTML_GENERATOR_DELIMITER);
			String type = column[0];

			if (!caseInfo.getArgs().get(2).equalsIgnoreCase("null") || ControllerConstants.RESULT_MAP_KEY_SESSION.equalsIgnoreCase(type)) { //$NON-NLS-1$
				if (ControllerConstants.RESULT_MAP_KEY_REQUEST.equalsIgnoreCase(type)
						|| ControllerConstants.RESULT_MAP_KEY_CONSTANT.equalsIgnoreCase(type)) {
					String keys = type + "." + column[1];
					key.append(MessageFormat.format(GeneratorConstants.JSP_METHOD_GET_VALUE,
							"'" + keys.toUpperCase() + "'", //$NON-NLS-1$ //$NON-NLS-2$
							"''" //$NON-NLS-1$
							));
				} else if (ControllerConstants.RESULT_MAP_KEY_SESSION.equalsIgnoreCase(type)) {
					key.append(MessageFormat.format(GeneratorConstants.JSP_METHOD_GET_VALUE,
							"'" + caseInfo.getArgs().get(0).toUpperCase() + "'", //$NON-NLS-1$ //$NON-NLS-2$
							"''" //$NON-NLS-1$
							));
				} else {
					if (column.length == 1) {
						key.append(column[0]);
					} else {
						if (ControllerConstants.RESULT_MAP_KEY_PAGER.equalsIgnoreCase(column[1])) {
							key.append(ControllerConstants.RESULT_MAP_KEY_ROOT);
							key.append('.');
							key.append(ControllerConstants.RESULT_MAP_KEY_PAGER);
							key.append(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER);
							key.append(column[0]);
							key.append('.');
							key.append(column[2]);
						} else {
							IteratorInfo iteratorInfo = this._iteratorInfo.get(column[0].toUpperCase());
							if (iteratorInfo == null) {
								key.append(ControllerConstants.RESULT_MAP_KEY_ROOT);
								key.append('.');
								key.append(type);
								key.append('.');
								key.append(column[column.length - 2]);
								key.append("_"); //$NON-NLS-1$
								key.append(column[column.length - 1]);
							} else {
								if (GeneratorConstants.JSP_ITERATOR_STATUS.equalsIgnoreCase(column[column.length - 2])) {
									key.append("#"); //$NON-NLS-1$
									key.append(column[column.length - 2].toLowerCase());
									key.append('.');
									key.append(column[column.length - 1].toLowerCase());
								} else if (GeneratorConstants.JSP_ITERATOR_VAR.equalsIgnoreCase(column[1])) {
									key.append("#"); //$NON-NLS-1$
									key.append(GeneratorConstants.JSP_ITERATOR_VAR.toLowerCase());
								} else {
									key.append(column[column.length - 2]);
									key.append("_"); //$NON-NLS-1$
									key.append(column[column.length - 1]);
								}
							}
						}
					}
				}
				sb.append(key.toString());

				sb.append(getOperatorString(caseInfo.getArgs().get(1)));

				String[] values = caseInfo.getArgs().get(2).split(GeneratorConstants.HTML_GENERATOR_DELIMITER);
				if (values[0].toUpperCase().startsWith(ControllerConstants.RESULT_MAP_KEY_CONSTANT)) {
					SystemConstantBean systemConstant = null;
					if (values.length == 2) {
						systemConstant = SystemConstantCache.getSystemConstant(values[1]);
					} else {
						systemConstant = SystemConstantCache.getSystemConstant(values[1], values[2]);
					}
					sb.append(systemConstant.getValue());
				} else if (values[0].toUpperCase().startsWith(ControllerConstants.RESULT_MAP_KEY_REQUEST)) {
					sb.append(MessageFormat.format(GeneratorConstants.JSP_METHOD_GET_VALUE,
							"'" + caseInfo.getArgs().get(2).toUpperCase() + "'", //$NON-NLS-1$ //$NON-NLS-2$
							"''" //$NON-NLS-1$
							));
				} else {
					sb.append(caseInfo.getArgs().get(2).replace("\"", "'"));
				}
			} else {
				if (caseInfo.getArgs().get(1).equalsIgnoreCase("eq")) { //$NON-NLS-1$
					key.append("!"); //$NON-NLS-1$
				}
				if (ControllerConstants.RESULT_MAP_KEY_REQUEST.equalsIgnoreCase(type)) {
					key.append(ControllerConstants.RESULT_MAP_KEY_ROOT);
					key.append('.');
					key.append(type);
					key.append(".containsKey('"); //$NON-NLS-1$
					key.append(column[1]);
					key.append("')"); //$NON-NLS-1$
				} else {
					IteratorInfo iteratorInfo = this._iteratorInfo.get(column[0].toUpperCase());
					if (iteratorInfo == null) {
						if (column.length >= 2) {
							key.append(ControllerConstants.RESULT_MAP_KEY_ROOT);
							key.append('.');
							key.append(type);
							key.append(".containsKey('"); //$NON-NLS-1$
							key.append(column[column.length - 2]);
							key.append("_"); //$NON-NLS-1$
							key.append(column[column.length - 1]);
							key.append("')"); //$NON-NLS-1$
						} else {
							key.append("containsKey('"); //$NON-NLS-1$
							key.append(type);
							key.append("')"); //$NON-NLS-1$
						}
					} else {
						key.append("containsKey('"); //$NON-NLS-1$
						key.append(column[column.length - 2]);
						key.append("_"); //$NON-NLS-1$
						key.append(column[column.length - 1]);
						key.append("')"); //$NON-NLS-1$
					}
				}
				sb.append(key.toString());
			}
		}
		return sb.toString();
	}

	private static void removeSiblingElement(Element element) {
		Elements siblings = element.siblingElements();
		for (Element e : siblings) {
			if (!e.tagName().equalsIgnoreCase("s:iterator")
					&& StringUtils.isEmpty(e.attr(GeneratorConstants.HTML_DATA_ATTR_NAME_ITERATOR))) {
				if (!e.hasAttr(GeneratorConstants.HTML_DATA_ATTR_NAME_KEEP)) {
					e.remove();
				} else {
					e.removeAttr(GeneratorConstants.HTML_DATA_ATTR_NAME_KEEP);
				}
			}
		}
	}

	private static String getOperatorString(String operator) {
		String result = "="; //$NON-NLS-1$
		if ("eq".equalsIgnoreCase(operator)) { //$NON-NLS-1$
			result = " == "; //$NON-NLS-1$
		} else if ("ne".equalsIgnoreCase(operator)) { //$NON-NLS-1$
			result = " != "; //$NON-NLS-1$
		} else if ("gt".equalsIgnoreCase(operator)) { //$NON-NLS-1$
			result = " > "; //$NON-NLS-1$
		} else if ("ge".equalsIgnoreCase(operator)) { //$NON-NLS-1$
			result = " >= "; //$NON-NLS-1$
		} else if ("lt".equalsIgnoreCase(operator)) { //$NON-NLS-1$
			result = " < "; //$NON-NLS-1$
		} else if ("le".equalsIgnoreCase(operator)) { //$NON-NLS-1$
			result = " <= "; //$NON-NLS-1$
		}
		return result;
	}

	private void saveJspFile(Document document, String outputPath, ContentTypeEnum contentType) throws IOException {
		File outputFile = getOutputFile(outputPath);
		OutputSettings outputSettings = new OutputSettings();
		outputSettings.indentAmount(2);
		outputSettings.prettyPrint(false);
		document.outputSettings(outputSettings);
		FileUtils.writeFile(outputFile, getContent(document, contentType), GeneratorConstants.OUTPUT_JSP_FILE_ENCODING);
	}

	private static File getOutputFile(String outputRelativePath) {
		StringBuilder sb = new StringBuilder();
		sb.append(GeneratorUtils.getOutputJspPath());
		sb.append(outputRelativePath);
		return new File(sb.toString());
	}

	private static String getContent(Document document, ContentTypeEnum contentType) {
		StringBuilder sb = new StringBuilder();

		if (contentType.equals(ContentTypeEnum.XML)) {
			// for xml
			sb.append("<?xml version=\"1.0\" encoding=\""); //$NON-NLS-1$
			sb.append(GeneratorConstants.OUTPUT_JSP_FILE_ENCODING);
			sb.append("\" ?>"); //$NON-NLS-1$
			OutputUtilities.newLine(sb);
		}

		// jsp directive
		sb.append("<%@ page contentType=\"text/"); //$NON-NLS-1$
		sb.append(contentType.getContentType());
		sb.append("; charset="); //$NON-NLS-1$
		sb.append(GeneratorConstants.OUTPUT_JSP_FILE_ENCODING);
		sb.append("\" "); //$NON-NLS-1$
		sb.append("pageEncoding=\""); //$NON-NLS-1$
		sb.append(GeneratorConstants.OUTPUT_JSP_FILE_ENCODING);
		sb.append("\"%>"); //$NON-NLS-1$
		OutputUtilities.newLine(sb);
		sb.append("<%@ taglib prefix=\"s\" uri=\"/struts-tags\"%>"); //$NON-NLS-1$
		OutputUtilities.newLine(sb);

		if (contentType.equals(ContentTypeEnum.XML)) {
			Element root = document.getElementsByTag(GeneratorConstants.INPUT_XML_ROOT_TAG).parents().first();
			// TODO:ContainerがないとNullPointerException。。。
			sb.append(StringEscapeUtils.unescapeXml(root.html()));
		} else {
			sb.append(StringEscapeUtils.unescapeXml(document.outerHtml()));
		}

		return sb.toString();
	}

	public class IteratorInfo {
		private String targetName;
		private String iteratorName;

		IteratorInfo(String targetName, String iteratorName) {
			this.targetName = targetName;
			this.iteratorName = iteratorName;
		}

		public String getTargetName() {
			return this.targetName;
		}

		public void setTargetName(String targetName) {
			this.targetName = targetName;
		}

		public String getIteratorName() {
			return this.iteratorName;
		}

		public void setIteratorName(String iteratorName) {
			this.iteratorName = iteratorName;
		}
	}
}
