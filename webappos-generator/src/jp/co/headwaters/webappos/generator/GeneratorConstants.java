package jp.co.headwaters.webappos.generator;

import jp.co.headwaters.webappos.controller.action.GenericAction;
import jp.co.headwaters.webappos.controller.exception.ConflictException;
import jp.co.headwaters.webappos.controller.exception.ExecuteDeniedException;
import jp.co.headwaters.webappos.controller.exception.InvalidTokenException;
import jp.co.headwaters.webappos.controller.exception.NotFoundException;
import jp.co.headwaters.webappos.controller.model.AbstractEntity;
import jp.co.headwaters.webappos.controller.model.CommonExample;
import jp.co.headwaters.webappos.generator.mybatis.plugin.AddProcedureMapperPlugin;
import jp.co.headwaters.webappos.generator.mybatis.plugin.AddRelationPlugin;
import jp.co.headwaters.webappos.generator.mybatis.plugin.MapperConfigPlugin;

public class GeneratorConstants {

	// ----------------------------------------------------------
	// for input file
	// ----------------------------------------------------------
	/** inputルートディレクト名 */
	public static final String INPUT_DIR = "in"; //$NON-NLS-1$
	/** 読み込み対象のファイル拡張子 */
	public static final String[] INPUT_FILE_EXTENSION = { ".html", ".htm", ".HTML", ".HTM", ".xml", ".XML" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	/** htmlの格納ディレクト名 */
	public static final String INPUT_HTML_DIR = "html"; //$NON-NLS-1$
	/** propertiesファイルの格納ディレクト名 */
	public static final String INPUT_PROPERTY_DIR = "properties"; //$NON-NLS-1$
	/** mapperの格納ディレクト名 */
	public static final String INPUT_MAPPER_DIR = "mapper"; //$NON-NLS-1$
	/** 変換対象外とするhtml格納ディレクト名 */
	public static final String INPUT_HTML_EXCLUDE_DIR = "sysapi"; //$NON-NLS-1$
	/** エラー画面ファイルの格納ディレクトリ名（HTML） */
	public static final String INPUT_HTML_ERROR_PAGE_DIR = "error"; //$NON-NLS-1$
	/** HTMLファイルのエンコード */
	public static final String INPUT_HTML_FILE_ENCODING = "UTF-8"; //$NON-NLS-1$
	/** 必須エラーページ名 */
	public static final String[] REQUIRED_ERROR_PAGE_NAME = {"404","500"}; //$NON-NLS-1$ //$NON-NLS-2$
	/** 任意エラーページ名 */
	public static final String[] OPTION_ERROR_PAGE_NAME = {"403","409"}; //$NON-NLS-1$ //$NON-NLS-2$
	/** xmlルート */
	public static final String INPUT_XML_ROOT_TAG = "Container"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for output file
	// ----------------------------------------------------------
	/** outputルートディレクト名 */
	public static final String OUTPUT_DIR = "out"; //$NON-NLS-1$
	/** jspの格納ディレクト名 */
	public static final String OUTPUT_JSP_DIR = "jsp"; //$NON-NLS-1$
	/** srcの格納ディレクト名 */
	public static final String OUTPUT_SRC_DIR = "src"; //$NON-NLS-1$
	/** propertiesファイルの格納ディレクト名 */
	public static final String OUTPUT_PROPERTY_DIR = "properties"; //$NON-NLS-1$
	/** web関連ファイルの格納ディレクト名 */
	public static final String OUTPUT_WEB_ROOT_DIR = "web"; //$NON-NLS-1$
	/** datファイルの格納ディレクト名 */
	public static final String OUTPUT_DAT_DIR = "dat"; //$NON-NLS-1$

	/** WebAppOS用struts2設定ファイル名(struts.xmlにてinclude) */
	public static final String OUTPUT_STRUTS_CONFIG_FILE_NAME = "struts-webappos.xml"; //$NON-NLS-1$
	/** WebAppOS用のweb.xmlファイル名(web.xmlにてinclude) */
	public static final String OUTPUT_WEB_CONFIG_FILE_NAME = "web-webappos.xml"; //$NON-NLS-1$

	/** Javaファイルのエンコード */
	public static final String OUTPUT_JAVA_FILE_ENCODING = "UTF-8"; //$NON-NLS-1$
	/** XMLファイルのエンコード */
	public static final String OUTPUT_XML_FILE_ENCODING = "UTF-8"; //$NON-NLS-1$
	/** JSPファイルのエンコード */
	public static final String OUTPUT_JSP_FILE_ENCODING = "UTF-8"; //$NON-NLS-1$
	/** プロパティファイルのエンコード */
	public static final String OUTPUT_PROPERTY_FILE_ENCODING = "ISO8859_1"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for output property
	// ----------------------------------------------------------
	/** Webアプリ参照ディレクトリ名 */
	public static final String WEBAPPS_DIR = "webapps"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for parse html
	// ----------------------------------------------------------
	public static final String HTML_DATA_ATTR_PREFIX = "data-"; //$NON-NLS-1$
	public static final String HTML_DATA_ATTR_NAME_LOAD = "data-load"; //$NON-NLS-1$
	public static final String HTML_DATA_ATTR_NAME_FUNC = "data-func"; //$NON-NLS-1$
	public static final String HTML_DATA_ATTR_NAME_COND = "data-cond"; //$NON-NLS-1$
	public static final String HTML_DATA_ATTR_NAME_BIND_INPUT = "data-bind-in"; //$NON-NLS-1$
	public static final String HTML_DATA_ATTR_NAME_BIND_OUTPUT = "data-bind-out"; //$NON-NLS-1$
	public static final String HTML_DATA_ATTR_NAME_ITERATOR = "data-iterator"; //$NON-NLS-1$
	public static final String HTML_DATA_ATTR_NAME_CASE = "data-case"; //$NON-NLS-1$
	public static final String HTML_DATA_ATTR_NAME_ERASE = "data-erase"; //$NON-NLS-1$
	public static final String HTML_DATA_ATTR_NAME_URL = "data-url"; //$NON-NLS-1$
	public static final String HTML_DATA_ATTR_NAME_KEEP = "data-keep"; //$NON-NLS-1$
	public static final String HTML_DATA_ATTR_NAME_AUTH = "data-auth"; //$NON-NLS-1$
	public static final String HTML_DATA_ATTR_NAME_IMAGE = "data-image"; //$NON-NLS-1$

	// for data-bind
	public static final String HTML_BIND_TARGET_TEXT = "text"; //$NON-NLS-1$
	public static final String HTML_BIND_TARGET_INNER = "inner"; //$NON-NLS-1$
	// for data-case
	public static final String HTML_CASE_TYPE_SIZE = "size"; //$NON-NLS-1$
	public static final String HTML_CASE_TYPE_COMPARE = "compare"; //$NON-NLS-1$
	public static final String HTML_CASE_NULL = "null"; //$NON-NLS-1$
	public static final String HTML_CASE_OPERATOR_AND = "and"; //$NON-NLS-1$
	public static final String HTML_CASE_OPERATOR_OR = "or"; //$NON-NLS-1$
	// for erasure
	public static final String HTML_ERASURE_TYPE_OWN = "own"; //$NON-NLS-1$
	public static final String HTML_ERASURE_TYPE_CHILD = "child"; //$NON-NLS-1$

	public static final String HTML_ORIGINAL = "{_ORIGINAL}"; //$NON-NLS-1$
	public static final String HTML_ARRAY_START_STRING = "["; //$NON-NLS-1$
	public static final String HTML_GENERATOR_DELIMITER = "\\."; //$NON-NLS-1$
	public static final String HTML_REPLACE_STRING = "{0}"; //$NON-NLS-1$

	public static final String[] HTML_REPLACE_ATTR_KEY = { "href", "src", "data", "value", "data-original" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

	public static final String REGEX_TARGET_DATA_ATTR = "data-(load|func|cond|bind-in|bind-out|iterator|case|erasure|url)$"; //$NON-NLS-1$
	/** HTTPステータス別ファイルのファイル名を検査する正規表現 */
	public static final String REGEX_HTTP_STATUS_CODE = "^[4-5][0-9][0-9]$"; //$NON-NLS-1$
	/** 日付フォーマットの正規表現 */
	public static final String REGEX_DATE_FORMAT = "^DATE\\[(.+)\\]$"; //$NON-NLS-1$
	/** 数値フォーマットの正規表現 */
	public static final String REGEX_NUMBER_FORMAT = "^NUMBER\\[(.+)\\]$"; //$NON-NLS-1$

	public static final String DEVELOP_SUFFIX = "_dev"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for jsp
	// ----------------------------------------------------------
	public static final String JSP_PROPERTY_ESCAPE = " escape=\"false\" "; //$NON-NLS-1$
	public static final String JSP_ITERATOR_STATUS = "_status"; //$NON-NLS-1$
	public static final String JSP_ITERATOR_VAR = "_value"; //$NON-NLS-1$

	public static final String JSP_METHOD_GET_VALUE = "getValue({0},{1})"; //$NON-NLS-1$
	public static final String JSP_METHOD_GET_VALUE_WITH_DEFAULT = "getValue({0},{1},{2})"; //$NON-NLS-1$
	public static final String JSP_METHOD_NUMBER_FORMAT = "getValueWithNumberFormat({0}, {1})"; //$NON-NLS-1$
	public static final String JSP_METHOD_NUMBER_FORMAT_WITH_DEFAULT = "getValueWithNumberFormat({0}, {1}, {2})"; //$NON-NLS-1$
	public static final String JSP_METHOD_DATE_FORMAT = "getValueWithDateFormat({0}, {1})"; //$NON-NLS-1$
	public static final String JSP_METHOD_DATE_FORMAT_WITH_DEFAULT = "getValueWithDateFormat({0}, {1}, {2})"; //$NON-NLS-1$

	public static final String JSP_METHOD_GET_VALUE_FOR_LIST = "getValue(#_value,{0},{1})"; //$NON-NLS-1$
	public static final String JSP_METHOD_GET_VALUE_WITH_DEFAULT_FOR_LIST = "getValue(#_value,{0},{1},{2})"; //$NON-NLS-1$
	public static final String JSP_METHOD_NUMBER_FORMAT_FOR_LIST = "getValueWithNumberFormat(#_value,{0},{1})"; //$NON-NLS-1$
	public static final String JSP_METHOD_NUMBER_FORMAT_WITH_DEFAULT_FOR_LIST = "getValueWithNumberFormat(#_value,{0},{1},{2})"; //$NON-NLS-1$
	public static final String JSP_METHOD_DATE_FORMAT_FOR_LIST = "getValueWithDateFormat(#_value,{0},{1})"; //$NON-NLS-1$
	public static final String JSP_METHOD_DATE_FORMAT_WITH_DEFAULT_FOR_LIST = "getValueWithDateFormat(#_value,{0},{1},{2})"; //$NON-NLS-1$

	public static final String JSP_METHOD_GET_ENCODE_VALUE = "getEncodeValue({0})"; //$NON-NLS-1$
	public static final String JSP_METHOD_GET_ENCODE_VALUE_FOR_LIST = "getEncodeValue(#_value,{0})"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for struts2
	// ----------------------------------------------------------
	/** struts.xmlのSYSTEM_ID */
	public static final String STRUTS_CONFIG_SYSTEM_ID = "http://struts.apache.org/dtds/struts-2.0.dtd"; //$NON-NLS-1$
	/** struts.xmlのPUBLIC_ID */
	public static final String STRUTS_CONFIG_PUBLIC_ID = "-//Apache Software Foundation//DTD Struts Configuration 2.0//EN"; //$NON-NLS-1$

	/** result要素のPREFIX */
	public static final String STRUTS_RESULT_PREFIX = "result"; //$NON-NLS-1$

	/** action名デリミタ */
	public static final String ACTION_NAME_DELIMITER = "-"; //$NON-NLS-1$

	/** NotFoundExceptionクラス名 */
	public static final String RESULT_NAME_NOT_FOUND = "notfound"; //$NON-NLS-1$
	public static final String NOT_FOUND_EXCEPTION_CLASS = NotFoundException.class.getName();
	/** InvalidTokenExceptionクラス名 */
	public static final String RESULT_NAME_INVALID_TOKEN = "invalidtoken"; //$NON-NLS-1$
	public static final String INVALID_TOKEN_EXCEPTION_CLASS = InvalidTokenException.class.getName();
	/** ConflictExceptionConflictExceptionクラス名 */
	public static final String RESULT_NAME_CONFLICT = "conflict"; //$NON-NLS-1$
	public static final String CONFLICT_EXCEPTION_CLASS = ConflictException.class.getName();
	/** ExecuteDeniedExceptionクラス名 */
	public static final String RESULT_NAME_EXEC_DENIED = "execdenied"; //$NON-NLS-1$
	public static final String EXEC_DENIED_EXCEPTION_CLASS = ExecuteDeniedException.class.getName();

	public static final String RESULT_NAME_INTERNAL_SERVER_ERROR = "error"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for MyBatis
	// ----------------------------------------------------------
	public static final String[] MYBATIS_PLUGIN_CLASSES = {
														AddRelationPlugin.class.getName(),
														AddProcedureMapperPlugin.class.getName(),
														MapperConfigPlugin.class.getName()};
	/** MyBatis Generatorを使用する際に指定するtargetRuntime */
	public static final String MYBATIS_TARGET_RUNTIME = "MyBatis3"; //$NON-NLS-1$
	/** 自動生成するModelクラスの親クラス */
	public static final String MYBATIS_MODEL_ROOT_CLASS = AbstractEntity.class.getName();
	/** 検索条件用の汎用クラス */
	public static final String MYBATIS_EXAMPLE_CLASS = CommonExample.class.getName();
	/** mapperのmapパラメータのkey */
	public static final String MYBATIS_MAP_RECORD_KEY = "record"; //$NON-NLS-1$
	/** selectAll時のresultMapのID */
	public static final String MYBATIS_RESULT_MAP_ID_FULL = "FullResultMap"; //$NON-NLS-1$
	/** countAllByExampleのresultMapのID */
	public static final String MYBATIS_STATEMENT_ID_COUNT_ALL_BY_PRIMARY_KEY = "countAllByExample"; //$NON-NLS-1$
	/** selectAllByPrimaryKeyのresultMapのID */
	public static final String MYBATIS_STATEMENT_ID_SELECT_ALL_BY_PRIMARY_KEY = "selectAllByPrimaryKey"; //$NON-NLS-1$
	/** selectAllByExampleのresultMapのID */
	public static final String MYBATIS_STATEMENT_ID_SELECT_ALL_BY_EXAMPLE_KEY = "selectAllByExample"; //$NON-NLS-1$
	/** 他Mapperから参照されるresultMapのIDのプレフィックス */
	public static final String MYBATIS_RESULT_MAP_ID_REF_PREFIX = "ResultMapRef"; //$NON-NLS-1$
	/** Modelクラス、Mapperファイルを生成しないテーブル名 */
	public static final String[] MYBATIS_EXCLUDE_TABLE_NAMES = {"_webappos_system_constant","_webappos_schema_columns"}; //$NON-NLS-1$ //$NON-NLS-2$

	// ----------------------------------------------------------
	// for message
	// ----------------------------------------------------------
	public static final String MESSAGE_BUNDLE_NAME = "generator_messages"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for webappos-controller
	// ----------------------------------------------------------
	/** 汎用Action名 */
	public static final String SYSTEM_GENERIC_ACTION_NAME = GenericAction.class.getName();

	// ----------------------------------------------------------
	// for generator property
	// ----------------------------------------------------------
	/** プロパティファイル名 */
	public static final String PROPERTY_FILE_NAME = "generator.properties"; //$NON-NLS-1$
	/** JDBC接続文字列 */
	public static final String PROPERTY_KEY_CONNECTION_URL = "connection.url"; //$NON-NLS-1$
	/** generatorディレクトリ */
	public static final String PROPERTY_KEY_GENERATE_PATH = "generate.path"; //$NON-NLS-1$
	/** webappsディレクトリ */
	public static final String PROPERTY_KEY_WEBAPPS_PATH = "webapps.path"; //$NON-NLS-1$

	/** パッケージ */
	public static final String PROPERTY_KEY_ROOT_PACKAGE = "root.package"; //$NON-NLS-1$
	/** コンテキストモード */
	public static final String PROPERTY_KEY_CONTEXT_MODE = "context.mode"; //$NON-NLS-1$
	/** Modelクラスのパッケージ名 */
	public static final String PROPERTY_KEY_MODEL_PACKAGE = "model.package"; //$NON-NLS-1$
	/** Mapperファイルのパッケージ名 */
	public static final String PROPERTY_KEY_MAPPER_PACKAGE = "mapper.package"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for env
	// ----------------------------------------------------------
	public static final String ENV_NAME_GEN_PATH = "GEN_PATH"; //$NON-NLS-1$
	public static final String ENV_NAME_CONNECTION_URL = "CONNECTION_URL"; //$NON-NLS-1$
	public static final String ENV_NAME_WEB_APPS_PATH = "webapps_path"; //$NON-NLS-1$
	public static final String ENV_NAME_CONTEXT_MODE = "context.mode"; //$NON-NLS-1$
	public static final String ENV_NAME_ROOT_PACKAGE = "root.package"; //$NON-NLS-1$

}
