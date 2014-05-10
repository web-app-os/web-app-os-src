package jp.co.headwaters.webappos.controller;

public class ControllerConstants {

	// ----------------------------------------------------------
	// Common
	// ----------------------------------------------------------
	public static final String PATH_DELIMITER = "/"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for config
	// ----------------------------------------------------------
	/** MyBatis設定ファイル名 */
	public static final String MYBATIS_CONFIG_FILE_NAME = "mybatis-config.xml"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for message
	// ----------------------------------------------------------
	public static final String MESSAGE_BUNDLE_NAME = "messages"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for properties
	// ----------------------------------------------------------
	/** プロパティファイル名 */
	public static final String PROPERTY_FILE_NAME = "application.properties"; //$NON-NLS-1$
	/** パッケージ */
	public static final String PROPERTY_KEY_ROOT_PACKAGE = "root.package"; //$NON-NLS-1$
	/** コンテキスト名 */
	public static final String PROPERTY_KEY_CONTEXT_NAME = "context.name"; //$NON-NLS-1$
	/** コンテキストモード */
	public static final String PROPERTY_KEY_CONTEXT_MODE = "context.mode"; //$NON-NLS-1$
	/** Webアプリ参照ディレクトリPath */
	public static final String PROPERTY_KEY_WEBAPPS_PATH = "webapps.path"; //$NON-NLS-1$
	/** 認証画面URI */
	public static final String PROPERTY_KEY_AUTH_REQUEST_URI = "auth.request.uri"; //$NON-NLS-1$
	/** 認証用テーブル名 */
	public static final String PROPERTY_KEY_AUTH_TABLE_NAME = "auth.table.name"; //$NON-NLS-1$
	/** 認証IDカラム名 */
	public static final String PROPERTY_KEY_AUTH_ID_COLUMN_NAME = "auth.column.id"; //$NON-NLS-1$
	/** パスワードカラム名 */
	public static final String PROPERTY_KEY_AUTH_PASSWORD_COLUMN_NAME = "auth.column.password"; //$NON-NLS-1$
	/** EMAILカラム名 */
	public static final String PROPERTY_KEY_AUTH_EMAIL_COLUMN_NAME = "auth.column.email"; //$NON-NLS-1$
	/** tokenカラム名 */
	public static final String PROPERTY_KEY_AUTH_TOKEN_COLUMN_NAME = "auth.column.token"; //$NON-NLS-1$
	/** 一時認証IDカラム名 */
	public static final String PROPERTY_KEY_AUTH_TEMP_ID_COLUMN_NAME = "auth.column.temp_id"; //$NON-NLS-1$
	/** 認証拡張条件 */
	public static final String PROPERTY_KEY_AUTH_COND_NAMES = "auth.conds.names"; //$NON-NLS-1$
	public static final String PROPERTY_KEY_AUTH_COND_OPERATORS = "auth.conds.operators"; //$NON-NLS-1$
	public static final String PROPERTY_KEY_AUTH_COND_VALUES = "auth.conds.values"; //$NON-NLS-1$
	/** 暗号化秘密鍵 */
	public static final String PROPERTY_KEY_SECURITY_CIPHER_KEY = "security.chipher.key"; //$NON-NLS-1$
	/** 暗号化アルゴリズム */
	public static final String PROPERTY_KEY_SECURITY_CIPHER_ALGORITHM = "security.chipher.algorithm"; //$NON-NLS-1$
	/** 暗号化アルゴリズム */
	public static final String PROPERTY_KEY_SECURITY_CIPHER_TRANSFORMATION = "security.chipher.transformation"; //$NON-NLS-1$
	/** 初期ベクトル */
	public static final String SECURITY_CIPHER_INIT_IV = "_hws_iv_"; //$NON-NLS-1$

	public static final String PROPERTY_KEY_IMAGE_CACHE_LIMIT = "image.cache.limit"; //$NON-NLS-1$
	public static final String PROPERTY_KEY_IMAGE_CACHE_EXPIRE = "image.cache.expire"; //$NON-NLS-1$

	/** 自動ログイン クッキー名 KEY */
	public static final String PROPERTY_KEY_AUTOLOGIN_COOKIE_NAME_KEY = "auth.autologin.cookie.name.key"; //$NON-NLS-1$
	/** 自動ログイン クッキー名 有効期限 */
	public static final String PROPERTY_KEY_AUTOLOGIN_COOKIE_NAME_EXPIRES = "auth.autologin.cookie.expires"; //$NON-NLS-1$
	/** 自動ログイン エラー画面URL */
	public static final String PROPERTY_KEY_AUTOLOGIN_ERROR_URI = "auth.autologin.error.requestURI"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for webapps
	// ----------------------------------------------------------
	/** dat格納ディレクトリ名 */
	public static final String WEBAPPS_DAT_DIR = "dat"; //$NON-NLS-1$
	/** datファイル名 */
	public static final String WEBAPPS_DAT_FILE_NAME = "webappos.dat"; //$NON-NLS-1$
	/** メールテンプレート格納ディレクトリ名 */
	public static final String WEBAPPS_MAIL_TEMPLATE_DIR = "templates"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for db
	// ----------------------------------------------------------
	/** PKカラム名 */
	public static final String PK_COLUMN_NAME = "id"; //$NON-NLS-1$
	/** 登録日時カラム名 */
	public static final String CREATED_COLUMN_NAME = "created_date"; //$NON-NLS-1$
	/** 更新日時カラム名 */
	public static final String UPDATED_COLUMN_NAME = "updated_date"; //$NON-NLS-1$

	/** MyBatisのMapperファイルのサフィックス */
	public static final String MYBATIS_MAPPER_SUFFIX = "Mapper"; //$NON-NLS-1$
	/** 自動生成するModelクラスのパッケージ名 */
	public static final String MYBATIS_MODEL_PACKAGE = "model"; //$NON-NLS-1$
	/** 自動生成するMapperファイルのパッケージ名 */
	public static final String MYBATIS_MAPPER_PACKAGE = "mapper"; //$NON-NLS-1$

	public static final String MYBATIS_MAP_KEY_RECORD = "record"; //$NON-NLS-1$
	public static final String MYBATIS_MAP_KEY_EXAMPLE = "example"; //$NON-NLS-1$

	/** プロシージャーMapper名 */
	public static final String PROCEDURE_MAPPER_NAME = "ProcedureMapper"; //$NON-NLS-1$
	/** プロシージャーMapperファイル名 */
	public static final String PROCEDURE_MAPPER_FILE_NAME = PROCEDURE_MAPPER_NAME + ".xml"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for system table
	// ----------------------------------------------------------
	public static final String TABLE_NAME_OAUTH_MANAGE = "_webappos_oauth_manager"; //$NON-NLS-1$
	public static final String COLUMN_NAME_OAUTH_MANAGE_USER_ID = "user_id"; //$NON-NLS-1$
	public static final String COLUMN_NAME_OAUTH_MANAGE_UID = "uid"; //$NON-NLS-1$
	public static final String COLUMN_NAME_OAUTH_MANAGE_PROVIDER = "provider"; //$NON-NLS-1$

	public static final String TABLE_NAME_AUTO_LOGIN = "_webappos_auto_login"; //$NON-NLS-1$
	public static final String COLUMN_NAME_AUTO_LOGIN_USER_ID = "user_id"; //$NON-NLS-1$
	public static final String COLUMN_NAME_AUTO_LOGIN_KEY = "login_key"; //$NON-NLS-1$
	public static final String COLUMN_NAME_AUTO_EXPIRES_DATE = "expires_date"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for cipher
	// ----------------------------------------------------------
	public static final String CIPHER_TYPE_BLOWFISH = "blowfish"; //$NON-NLS-1$
	public static final String CIPHER_TYPE_SMD5 = "smd5"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for request parse
	// ----------------------------------------------------------
	/** form識別子の要素名 */
	public static final String ELEMENT_NAME_FORM_ID = "_SYS_FORM_ID_"; //$NON-NLS-1$
	/** パラメータ名のデリミタ */
	public static final String REQUEST_PARAM_NAME_DELIMITER = "__"; //$NON-NLS-1$
	/** CRUDの登録、更新要素のパラメータ識別子 */
	public static final String REQUEST_PARAM_NAME_CRUD_COLUMN = "COL"; //$NON-NLS-1$
	/** CRUDの条件要素のパラメータ識別子 */
	public static final String REQUEST_PARAM_NAME_CRUD_CONDITION = "COND"; //$NON-NLS-1$
	/** CRUDのソート要素のパラメータ識別子 */
	public static final String REQUEST_PARAM_NAME_CRUD_SORT = "SORT"; //$NON-NLS-1$
	/** CRUDの登録、更新件数のパラメータ識別子 */
	public static final String REQUEST_PARAM_NAME_CRUD_COUNT = "COUNT"; //$NON-NLS-1$
	/** result識別子の属性名 */
	public static final String ATTR_NAME_RESULT_NAME = "_RESULT_NAME_"; //$NON-NLS-1$

	/** URIからパラメータを抽出する為の正規表現 */
	public static final String REGEX_REQUEST_PARAM = "[-_.!~*()a-zA-Z0-9%]+/\\(\\[\\^/\\]\\+\\)"; //$NON-NLS-1$

	public static final String REGEX_REQUEST_CHAR = "{0}"; //$NON-NLS-1$
	public static final String REGEX_REQUEST_PARAM_REPLACEMENT = "([^/]+)"; //$NON-NLS-1$

	/** colum名の区切り文字 */
	public static final String REGEX_COLUMN_DELIMITER = "\\."; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for response
	// ----------------------------------------------------------
	/** result省略時のデフォルト */
	public static final String RESULT_NAME_DEFAULT = "index"; //$NON-NLS-1$

	/** jspファイルの拡張子 */
	public static final String JSP_EXTENSION = ".jsp"; //$NON-NLS-1$

	public static final String RESULT_MAP_KEY_ROOT = "resultMap"; //$NON-NLS-1$
	public static final String RESULT_MAP_KEY_REQUEST = "_REQ"; //$NON-NLS-1$
	public static final String RESULT_MAP_KEY_PAGER = "_PAGER"; //$NON-NLS-1$
	public static final String RESULT_MAP_KEY_SESSION = "_SES"; //$NON-NLS-1$
	public static final String RESULT_MAP_KEY_CONSTANT = "_CONST"; //$NON-NLS-1$

	public static final String RESULT_VALUE_TRUE = "1"; //$NON-NLS-1$
	public static final String RESULT_VALUE_FALSE = "0"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for format
	// ----------------------------------------------------------
	public static final String FORMAT_PATTERN_DATE = "yyyy/MM/dd"; //$NON-NLS-1$
	public static final String FORMAT_PATTERN_TIMESTAMP = "yyyy/MM/dd HH:mm:ss.SSS"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for crud
	// ----------------------------------------------------------
	public static final String CRUD_IN_DELIMITER = ","; //$NON-NLS-1$

	public static final String CRUD_PAGER_RECORD_COUNT = "RECORD_COUNT"; //$NON-NLS-1$
	public static final String CRUD_PAGER_PER_PAGE = "PER_PAGE"; //$NON-NLS-1$
	public static final String CRUD_PAGER_RECORD_BEGIN_NO = "RECORD_BEGIN_NO"; //$NON-NLS-1$
	public static final String CRUD_PAGER_RECORD_END_NO = "RECORD_END_NO"; //$NON-NLS-1$
	public static final String CRUD_PAGER_PAGE_NO = "PAGE_NO"; //$NON-NLS-1$
	public static final String CRUD_PAGER_PREV_PAGE_NO = "PREV_PAGE_NO"; //$NON-NLS-1$
	public static final String CRUD_PAGER_NEXT_PAGE_NO = "NEXT_PAGE_NO"; //$NON-NLS-1$
	public static final String CRUD_PAGER_PAGING_BEGIN_NO = "PAGING_BEGIN_NO"; //$NON-NLS-1$
	public static final String CRUD_PAGER_PAGING_END_NO = "PAGING_END_NO"; //$NON-NLS-1$
	public static final String CRUD_PAGER_MAX_PAGE_NO = "MAX_PAGE_NO"; //$NON-NLS-1$
	public static final String CRUD_PAGER_PAGE_NO_LIST = "PAGE_NO_LIST"; //$NON-NLS-1$

	public static final String TABLE_COLUMN_DELIMITER = "_"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for pager
	// ----------------------------------------------------------
	public static final int DEFAULT_PEGE_NO = 1;
	public static final String DEFAULT_PAGE_NO_PARAM_NAME = "page"; //$NON-NLS-1$
	public static final String DEFAULT_PER_PAGE = "20"; //$NON-NLS-1$
	public static final String DEFAULT_PAGER_COUNT = "5"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for html attribute
	// ----------------------------------------------------------
	public static final String OPERATOR_NULL = "isnull"; //$NON-NLS-1$
	public static final String OPERATOR_NOT_NULL = "isnotnull"; //$NON-NLS-1$
	public static final String OPERATOR_EQUAL = "eq"; //$NON-NLS-1$
	public static final String OPERATOR_NOT_EQUAL = "nq"; //$NON-NLS-1$
	public static final String OPERATOR_GREATER_THAN = "gt"; //$NON-NLS-1$
	public static final String OPERATOR_GREATER_OR_EQUAL = "ge"; //$NON-NLS-1$
	public static final String OPERATOR_LESS_THAN = "lt"; //$NON-NLS-1$
	public static final String OPERATOR_LESS_OR_EQUAL = "le"; //$NON-NLS-1$
	public static final String OPERATOR_FRONT_LIKE = "%like"; //$NON-NLS-1$
	public static final String OPERATOR_MIDDLE_LIKE = "%like%"; //$NON-NLS-1$
	public static final String OPERATOR_BACK_LIKE = "like%"; //$NON-NLS-1$
	public static final String OPERATOR_FRONT_NOT_LIKE = "%notlike"; //$NON-NLS-1$
	public static final String OPERATOR_MIDDLE_NOT_LIKE = "%notlike%"; //$NON-NLS-1$
	public static final String OPERATOR_BACK_NOT_LIKE = "notlike%"; //$NON-NLS-1$
	public static final String OPERATOR_IN = "in"; //$NON-NLS-1$
	public static final String OPERATOR_NOT_IN = "notin"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for file
	// ----------------------------------------------------------
	public static final String PROPERTY_KEY_FILE_BASE_DIR = "base.dir"; //$NON-NLS-1$
//	public static final String PROPERTY_KEY_FILE_BASE_PREFIX = "base"; //$NON-NLS-1$
//	public static final String PROPERTY_KEY_FILE_LEVEL = ".permit_level"; //$NON-NLS-1$
//	public static final String PROPERTY_KEY_FILE_GID = ".permit_gid"; //$NON-NLS-1$
	public static final String PROPERTY_KEY_NO_IMAGE_PATH = "noimage.path"; //$NON-NLS-1$

	public static final String FILE_UPLOAD_DEST_DIR_DEFAULT = "anon"; //$NON-NLS-1$

	public static final String CONTENT_TYPE_COLUMN_SUFFIX = "_content_type"; //$NON-NLS-1$

	public static final String[] IMAGE_FILE_EXTENSION = { "jpg", "jpeg", "png", "gif", "bmp" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

	// ----------------------------------------------------------
	// for api
	// ----------------------------------------------------------
	public static final String API_PREFIX = "sysapi"; //$NON-NLS-1$
	public static final String[] API_URL_PATTERNS = { "/auth/oauth/sp/([^/]+)/", //$NON-NLS-1$
			"/auth/callback/sp/([^/]+)/", //$NON-NLS-1$
			"/auth/users/sp/([^/]+)/", //$NON-NLS-1$
			"/auth/connect/sp/([^/]+)/", //$NON-NLS-1$
			"/auth/disconnect/sp/([^/]+)/", //$NON-NLS-1$
			"/auth/login/", //$NON-NLS-1$
			"/auth/autologin/", //$NON-NLS-1$
			"/auth/clearAutoLogin/", //$NON-NLS-1$
			"/auth/logout/", //$NON-NLS-1$
			"/auth/resetpw/", //$NON-NLS-1$
			"/auth/modifyId/", //$NON-NLS-1$
			"/validation/unique/", //$NON-NLS-1$
			"/image/", //$NON-NLS-1$
			"/cacheRefresh/" //$NON-NLS-1$
			};

	public static final String API_GET_IMAGE_URL = "/sysapi/image/"; //$NON-NLS-1$

	public static final String API_PARAM_REDIRECT_URI = "redirect_uri"; //$NON-NLS-1$

	// for oauth
	public static final String API_PARAM_OAUTH_SUCCESS_REDIRECT_URI = "ok"; //$NON-NLS-1$
	public static final String API_PARAM_OAUTH_FAILURE_REDIRECT_URI = "noauth"; //$NON-NLS-1$
	public static final String API_PARAM_OAUTH_NOTFOUND_REDIRECT_URI = "nouser"; //$NON-NLS-1$
	public static final String API_PARAM_OAUTH_NG_REDIRECT_URI = "ng"; //$NON-NLS-1$
	public static final String API_PARAM_OAUTH_SP = "sp"; //$NON-NLS-1$

	// for login
	public static final String API_PARAM_LOGIN_ID = "id"; //$NON-NLS-1$
	public static final String API_PARAM_LOGIN_PASSWORD = "pw"; //$NON-NLS-1$
	public static final String API_PARAM_LOGIN_AUTO_LOGIN = "auto_login"; //$NON-NLS-1$

	// for reset password
	public static final String API_PARAM_EMAIL = "email"; //$NON-NLS-1$

	// for modify id
	public static final String API_PARAM_TEMP_ID = "temp_id"; //$NON-NLS-1$

	// for validation
	public static final String API_PARAM_TABLE_NAME = "tbl"; //$NON-NLS-1$
	public static final String API_PARAM_COLUMN_NAME = "col"; //$NON-NLS-1$
	public static final String API_PARAM_VALUE = "val"; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for oauth
	// ----------------------------------------------------------
	public static final String OAUTH_CALLBACK_URL = "/auth/callback/"; //$NON-NLS-1$

	public static final String OAUTH_PROPERTY_FILE_NAME = "consumer.properties"; //$NON-NLS-1$
	public static final String PROPERTY_KEY_OAUTH_API_KEY = ".apiKey"; //$NON-NLS-1$
	public static final String PROPERTY_KEY_OAUTH_API_SECRET = ".apiSecret"; //$NON-NLS-1$
	public static final String PROPERTY_KEY_OAUTH_PARAMS = ".params."; //$NON-NLS-1$

	// ----------------------------------------------------------
	// for session
	// ----------------------------------------------------------
	public static final String SESSION_KEY_LOGIN_USER = "LOGIN_USER"; //$NON-NLS-1$
	public static final String SESSION_KEY_LOGIN_OAUTH_SUCCESS_URI = "OAUTH_SUCCESS_URI"; //$NON-NLS-1$
	public static final String SESSION_KEY_LOGIN_REDIRECT_URI = "LOGIN_REDIRECT_URI"; //$NON-NLS-1$
	public static final String SESSION_KEY_AUTO_LOGIN_CHECK = "AUTO_LOGIN_CHECK"; //$NON-NLS-1$

}
