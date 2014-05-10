package jp.co.headwaters.webappos.controller.action;

import static jp.co.headwaters.webappos.controller.utils.ControllerUtils.*;
import static jp.co.headwaters.webappos.controller.utils.ConvertDateTypeUtils.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.SystemConstantKeys;
import jp.co.headwaters.webappos.controller.cache.SchemaColumnCache;
import jp.co.headwaters.webappos.controller.cache.SystemConstantCache;
import jp.co.headwaters.webappos.controller.cache.WebAppOSCache;
import jp.co.headwaters.webappos.controller.cache.bean.AbstractExecuteBean;
import jp.co.headwaters.webappos.controller.cache.bean.ActionBean;
import jp.co.headwaters.webappos.controller.cache.bean.ExecuteBean;
import jp.co.headwaters.webappos.controller.cache.bean.FunctionBean;
import jp.co.headwaters.webappos.controller.cache.bean.LoadBean;
import jp.co.headwaters.webappos.controller.cache.bean.ResultBean;
import jp.co.headwaters.webappos.controller.cache.bean.SchemaColumnBean;
import jp.co.headwaters.webappos.controller.cache.bean.SystemConstantBean;
import jp.co.headwaters.webappos.controller.enumation.DataTypeEnum;
import jp.co.headwaters.webappos.controller.exception.ConflictException;
import jp.co.headwaters.webappos.controller.exception.InvalidTokenException;
import jp.co.headwaters.webappos.controller.exception.NotFoundException;
import jp.co.headwaters.webappos.controller.exception.WebAppOSException;
import jp.co.headwaters.webappos.controller.fuction.AbstractFunction;
import jp.co.headwaters.webappos.controller.fuction.FunctionFactory;
import jp.co.headwaters.webappos.controller.security.Cipher;
import jp.co.headwaters.webappos.controller.security.CipherFactory;
import jp.co.headwaters.webappos.controller.utils.ControllerUtils;
import jp.co.headwaters.webappos.controller.utils.DaoUtils;
import jp.co.headwaters.webappos.controller.utils.MessageUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;

import com.opensymphony.xwork2.ActionContext;

@SuppressWarnings("serial")
public class GenericAction extends AbstractAction {

	private static final Log _logger = LogFactory.getLog(GenericAction.class);

	/** リクエストパラメータを保持するMap */
	private Map<String, String[]> _requestParams;
	/** 処理結果を保持するMap */
	private Map<String, Object> _resultMap = new HashMap<String, Object>();
	/** action別実行情報 */
	private ActionBean _actionBean;
	/** アップロードファイル情報 */
	private List<File> _uploadFiles;

	private Cipher _cipher = CipherFactory.create("blowfish"); //$NON-NLS-1$

	@SuppressWarnings("resource")
	public String execute() throws Exception {
		try {
			this._uploadFiles = new ArrayList<>();

			// ------------------------------------------------------------
			// リクエストパラメータを取得する
			// ------------------------------------------------------------
			this._requestParams = new HashMap<String, String[]>(this._request.getParameterMap());
			// リクエストパラメータを返却Mapにputする
			setResultMapFromRequestParams();

			// ------------------------------------------------------------
			// 実行情報および遷移先名を取得する
			// ------------------------------------------------------------
			String actionName = ActionContext.getContext().getName();
			String resultName = (String) this._request.getAttribute(ControllerConstants.ATTR_NAME_RESULT_NAME);
			this._actionBean = WebAppOSCache.getInstance().getActionMap().get(actionName);
			if (this._actionBean == null || resultName == null) {
				throw new NotFoundException();
			}

			// 実行、返却情報を保持する変数を初期化する
			ExecuteBean submitExecuteInfo = null;
			ExecuteBean loadExecuteInfo = null;
			ResultBean result = null;
			String formId = null;

			// form識別子を取得する
			if (this._requestParams != null && this._requestParams.get(ControllerConstants.ELEMENT_NAME_FORM_ID) != null){
				formId = this._requestParams.get(ControllerConstants.ELEMENT_NAME_FORM_ID)[0];
			}

			if (!StringUtils.isEmpty(formId)) {
				// リクエストパラメータにform識別子が存在した場合、sumbitされたと判断する
				submitExecuteInfo = this._actionBean.getSubmitExecuteMap().get(formId);
				if (submitExecuteInfo == null){
					throw new NotFoundException();
				}
				result = submitExecuteInfo.getResultInfo();

				loadExecuteInfo = getLoadExecute(resultName);
				if (loadExecuteInfo != null) {
					result = loadExecuteInfo.getResultInfo();
				}
			} else {
				loadExecuteInfo = getLoadExecute(resultName);
				if (loadExecuteInfo != null) {
					result = loadExecuteInfo.getResultInfo();
				}
			}

			if (result == null) {
				throw new NotFoundException();
			}

			// ------------------------------------------------------------
			// 命令実行(submit時)
			// ------------------------------------------------------------
			HashMap<String, Map<Integer, Integer>> ids = new HashMap<String, Map<Integer, Integer>>();
			if (submitExecuteInfo != null) {
				SqlSession sqlSession = null;
				try {
					if (submitExecuteInfo.getExecuteInfoList().size() > 0) {
						sqlSession = DaoUtils.openSqlSession(false);

						if (!uploadFiles()) {
							removeUploadFiles();
							// TODO:エラー処理
						}
						for (int i = 0; i < submitExecuteInfo.getExecuteInfoList().size(); i++) {
							FunctionBean info = (FunctionBean) submitExecuteInfo.getExecuteInfoList().get(i);
							if (i == 0 && info.isTokenValidation()) {
								if (!validateToken()) {
									throw new InvalidTokenException("err.901"); //$NON-NLS-1$
								}
							}
							AbstractFunction function = FunctionFactory.create(info.getType());
							function.setSqlSession(sqlSession);
							function.setHttpSession(this._session);
							function.setIds(ids);
							function.execute(this._requestParams, this._resultMap, info);
							if (info.getTarget() != null) {
								ids.put(getSchemaColumnKey(info.getTarget(), ControllerConstants.PK_COLUMN_NAME), info.getId());
							}
						}
						DaoUtils.commit(sqlSession);
					}
				} catch (Exception e) {
					DaoUtils.rollback(sqlSession);
					removeUploadFiles();
					throw e;
				} finally {
					DaoUtils.closeSqlSession(sqlSession);
				}
			}

			// ------------------------------------------------------------
			// 命令実行(load時)
			// ------------------------------------------------------------
			if (loadExecuteInfo != null) {
				SqlSession sqlSession = null;
				try {
					if (loadExecuteInfo.getExecuteInfoList().size() > 0) {
						sqlSession = DaoUtils.openSqlSession(false);
						for (AbstractExecuteBean executeInfo : loadExecuteInfo.getExecuteInfoList()) {
							LoadBean info = (LoadBean) executeInfo;
							AbstractFunction function = FunctionFactory.create(info.getType());
							function.setSqlSession(sqlSession);
							function.setHttpSession(this._session);
							function.execute(this._requestParams, this._resultMap, info);
							ids.put(getSchemaColumnKey(info.getTarget(), ControllerConstants.PK_COLUMN_NAME), info.getId());
						}
						DaoUtils.commit(sqlSession);
					}
				} catch (Exception e) {
					DaoUtils.rollback(sqlSession);
					throw e;
				} finally {
					DaoUtils.closeSqlSession(sqlSession);
				}
			}

			// ------------------------------------------------------------
			// const bind
			// ------------------------------------------------------------
			setResultMapFromConstant();

			_logger.debug("result:" + result.getName()); //$NON-NLS-1$
			return result.getName();
		} catch (InvalidTokenException e ) {
			throw e;
		} catch (ConflictException e) {
			throw e;
		} catch (NotFoundException e) {
			_logger.error(e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			_logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private void setResultMapFromRequestParams() {
		if (!this._resultMap.containsKey(ControllerConstants.RESULT_MAP_KEY_REQUEST)){
			this._resultMap.put(ControllerConstants.RESULT_MAP_KEY_REQUEST, new HashMap<String ,Object>());
		}

		for (Entry<String, String[]> entry : this._requestParams.entrySet()) {
			String[] keys = entry.getKey().split(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER, 5);
			String[] values = entry.getValue();
			if (keys.length <= 2) {
				// cond、bind以外のパラメータと判断
				@SuppressWarnings("unchecked")
				HashMap<String ,Object> reqMap = (HashMap<String, Object>) this._resultMap.get(ControllerConstants.RESULT_MAP_KEY_REQUEST);
				if (values.length > 1) {
					reqMap.put(entry.getKey().toUpperCase(), values);
				} else if (values.length == 1) {
					reqMap.put(entry.getKey().toUpperCase(), values[0]);
				}
				continue;
			}
			this._resultMap.put(entry.getKey(), values);
		}
	}

	private ExecuteBean getLoadExecute(String resultName) {
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isEmpty(this._actionBean.getHtmlPath())){
			sb.append(this._actionBean.getHtmlPath());
			sb.append(getFileSparator());
		}
		sb.append(resultName);
		sb.append(ControllerConstants.JSP_EXTENSION);
		return this._actionBean.getLoadExecuteMap().get(sb.toString());
	}

	private void setResultMapFromConstant(){
		HashMap<String ,String> constMap = new HashMap<String, String>();
		List<SystemConstantBean> viewConsts = SystemConstantCache.getSystemConstantList(SystemConstantKeys.CATEGORY_VIEW);
		for (SystemConstantBean viewConst : viewConsts) {
			constMap.put(viewConst.getKey(), viewConst.getValue());
		}
		this._resultMap.put(ControllerConstants.RESULT_MAP_KEY_CONSTANT, constMap);
	}

	private boolean uploadFiles() throws IOException{
		if (this._request instanceof MultiPartRequestWrapper){
			MultiPartRequestWrapper multiWrapper = (MultiPartRequestWrapper) ServletActionContext.getRequest();
			if (multiWrapper.hasErrors()) {
				Collection<?> errors = multiWrapper.getErrors();
				Iterator<?> i = errors.iterator();
				while (i.hasNext()) {
					addActionError((String) i.next());
				}
				return false;
			}

			Enumeration<?> e = multiWrapper.getFileParameterNames();
			while (e.hasMoreElements()) {
				String inputValue = (String) e.nextElement();
				String[] fileNames = multiWrapper.getFileNames(inputValue);
				String[] fileSystemNames = multiWrapper.getFileSystemNames(inputValue);
				String[] contentTypes = multiWrapper.getContentTypes(inputValue);
				File[] files = multiWrapper.getFiles(inputValue);
				String[] uploadFileNames = new String[files.length];
				String keys[] = inputValue.split(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER);
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if (file == null) {
						addActionError("Error uploading: " + multiWrapper.getFileNames(inputValue)); //$NON-NLS-1$
						return false;
					}
					File uploadFile = saveFile(file, keys[0].toLowerCase(), fileSystemNames[i], fileNames[i]);
					uploadFileNames[i] = getSaveFilePath(uploadFile);
				}
				this._requestParams.put(inputValue, uploadFileNames);
				this._requestParams.put(inputValue + ControllerConstants.CONTENT_TYPE_COLUMN_SUFFIX, contentTypes);
			}
		}
		return true;
	}

	private File saveFile(File reqFile, String dest, String fileSystemName, String fileName) throws IOException {
		String destFilePath = getDestFilePath(dest, fileSystemName, fileName);
		_logger.debug(destFilePath);
		File destFile = new File(destFilePath);
		FileUtils.copyFile(reqFile, destFile);
		this._uploadFiles.add(destFile);
		return destFile;
	}

	private String getSaveFilePath(File uploadFile) {
		StringBuilder baseDir = new StringBuilder();
		baseDir.append(getText(ControllerConstants.PROPERTY_KEY_FILE_BASE_DIR));
		if (!baseDir.toString().endsWith(getFileSparator())) {
			baseDir.append(getFileSparator());
		}

		StringBuilder sb = new StringBuilder();
		sb.append(getFileSparator());
		sb.append(uploadFile.getAbsolutePath().substring(baseDir.toString().length()));
		return sb.toString();
	}

	private void removeUploadFiles(){
		for (File file : this._uploadFiles) {
			file.delete();
		}
	}

	private String getDestFilePath(String dest, String fileSystemName, String fileName) {
		StringBuilder sb = new StringBuilder();
		sb.append(getText(ControllerConstants.PROPERTY_KEY_FILE_BASE_DIR));
		if (!sb.toString().endsWith(getFileSparator())) {
			sb.append(getFileSparator());
		}
		if (dest != null) {
			sb.append(dest);
		}
		if (!sb.toString().endsWith(getFileSparator())) {
			sb.append(getFileSparator());
		}
		if (isLoggedIn()) {
			sb.append(getLoginUserInfo(ControllerConstants.PK_COLUMN_NAME));
		} else {
			sb.append(ControllerConstants.FILE_UPLOAD_DEST_DIR_DEFAULT);
		}
		sb.append(getFileSparator());
		sb.append(ControllerUtils.removeFileExtension(fileSystemName));
		sb.append('.');
		sb.append(ControllerUtils.getFileExtension(fileName));
		return sb.toString();
	}

	// ---------------- for jsp ----------------
	public Map<String, Object> getResultMap() {
		return this._resultMap;
	}

	public void setResultMap(Map<String, Object> _resultMap) {
		this._resultMap = _resultMap;
	}

	public String eraseTag(String value) {
		if (value == null) return null;
		return value.replaceAll("<.+?>", "");
	}

	public String getValue(String key, String mapKey) {
		return getValue(this._resultMap, key, mapKey);
	}

	public String getEncodeValue(String key) throws UnsupportedEncodingException {
		String result = null;
		try {
			result = this._cipher.encrypt(getValue(this._resultMap, key, null, null));
		} catch (WebAppOSException e) {
			_logger.error(e.getMessage(),e);
		}
		return URLEncoder.encode(result, "UTF-8"); //$NON-NLS-1$
	}

	public String getValue(String key, String defaultKey, String mapKey) {
		return getValue(this._resultMap, key, defaultKey, mapKey);
	}

	public String getValueWithDateFormat(String key, String pattern) {
		return getValueWithDateFormat(key, null, pattern);
	}

	public String getValueWithDateFormat(String key, String defaultKey, String pattern) {
		String value = getValue(key, defaultKey, null);
		if (value == null) {
			return null;
		}
		return getDateFormatValue(value, pattern);
	}

	public String getValueWithNumberFormat(String key, String pattern) {
		return getValueWithNumberFormat(key, null, pattern);
	}

	public String getValueWithNumberFormat(String key, String defaultKey, String pattern) {
		String value = getValue(key, defaultKey, null);
		if (value == null) {
			return null;
		}
		return getNumberFormatValue(value, DataTypeEnum.DATA_TYPE_NUMERIC.getDataType(), pattern);
	}

	public String getValue(Map<?, ?> map, String key, String mapKey) {
		return getValue(map, key, null, mapKey);
	}

	public String getEncodeValue(Map<?, ?> map, String key) throws UnsupportedEncodingException {
		String result = null;
		try {
			result = this._cipher.encrypt(getValue(map, key, null, null));
		} catch (WebAppOSException e) {
			_logger.error(e.getMessage(),e);
		}
		return URLEncoder.encode(result, "UTF-8"); //$NON-NLS-1$
	}

	public String getValue(Map<?, ?> map, String key, String defaultKey, String mapKey) {
		String[] keys = key.split(ControllerConstants.REGEX_COLUMN_DELIMITER);
		String result = null;

		if (ControllerConstants.RESULT_MAP_KEY_SESSION.equalsIgnoreCase(keys[0])) {
			if (ControllerConstants.SESSION_KEY_LOGIN_USER.equalsIgnoreCase(keys[1])) {
				return getLoginUserInfo(keys[keys.length - 1]);
			} else if (ControllerConstants.SESSION_KEY_LOGIN_REDIRECT_URI.equalsIgnoreCase(keys[1])) {
				if (this._session.containsKey(ControllerConstants.SESSION_KEY_LOGIN_REDIRECT_URI)) {
					return (String) this._session.get(ControllerConstants.SESSION_KEY_LOGIN_REDIRECT_URI);
				}
			}
		} else {
			if (map.containsKey(keys[0])) {
				Object obj = map.get(keys[0]);
				if (obj instanceof Map) {
					result = getValue((Map<?, ?>) obj, key, 1);
				} else {
					if (obj instanceof Object[]) {
						Object[] array = (Object[])obj;
						if (array.length == 1){
							result = String.valueOf(array[0]);
						}
					} else {
						result = String.valueOf(obj);
					}
				}
			}
		}

		if (result == null && defaultKey != null) {
			String[] defaultKeys = defaultKey.split(ControllerConstants.REGEX_COLUMN_DELIMITER);
			Object obj = this._resultMap.get(defaultKeys[0]);
			if (obj instanceof Map) {
				result = getValue((Map<?, ?>) obj, defaultKey, 1);
			} else {
				if (obj != null) {
					result = String.valueOf(obj);
				} else {
					result = defaultKey;
				}
			}
		}

		if (!StringUtils.isEmpty(mapKey)) {
			result = getValueFromConstMap(result, mapKey);
		}
		return result;
	}

	private String getValueFromConstMap(String target, String mapKey) {
		String result = target;
		if (mapKey != null) {
			if (mapKey.toUpperCase().startsWith(ControllerConstants.RESULT_MAP_KEY_CONSTANT)) {
				String[] keys = mapKey.split(ControllerConstants.REGEX_COLUMN_DELIMITER);
				SystemConstantBean systemConstant = SystemConstantCache.getSystemConstant(keys[1], keys[2]);
				if (systemConstant.getRealValue() instanceof Map) {
					HashMap<?, ?> map = (HashMap<?, ?>)systemConstant.getRealValue();
					result = (String) map.get(target);
				}
			}
		}
		return result;
	}

	public String getValueWithDateFormat(Map<?, ?> map, String key, String pattern) {
		return getValueWithDateFormat(map, key, null, pattern);
	}

	public String getValueWithDateFormat(Map<?, ?> map, String key, String defaultKey, String pattern) {
		String value = getValue(map, key, defaultKey, null);
		if (value == null) {
			return null;
		}
		return getDateFormatValue(value, pattern);
	}

	public String getValueWithNumberFormat(Map<?, ?> map, String key, String pattern) {
		return getValueWithNumberFormat(map, key, null, pattern);
	}

	public String getValueWithNumberFormat(Map<?, ?> map, String key, String defaultKey, String pattern) {
		String value = getValue(map, key, defaultKey, null);
		if (value == null) {
			return null;
		}
		String dataType = DataTypeEnum.DATA_TYPE_NUMERIC.getDataType();
		String[] keys = key.split(ControllerConstants.TABLE_COLUMN_DELIMITER);
		if (keys.length == 2){
			SchemaColumnBean schemaColumn = SchemaColumnCache.getSchemaColumn(getSchemaColumnKey(keys[0], keys[1]));
			if (schemaColumn != null) {
				dataType = schemaColumn.getDataType();
			}
		}
		return getNumberFormatValue(value, dataType, pattern);
	}

	private String getValue(Map<?, ?> map, String key, int depth) {
		String[] keys = key.split(ControllerConstants.REGEX_COLUMN_DELIMITER);
		if (keys.length < depth) {
			return null;
		}
		if (!map.containsKey(keys[depth])){
			return null;
		}
		Object obj = map.get(keys[depth]);
		if (obj instanceof Map) {
			return getValue((Map<?, ?>)obj, key, ++depth);
		} else {
			return String.valueOf(obj);
		}
	}

	private String getDateFormatValue(String value, String pattern) {
		try {
			if (pattern.toUpperCase().startsWith(ControllerConstants.RESULT_MAP_KEY_CONSTANT)) {
				pattern = getValue(pattern.toUpperCase(), null, null);
			}
			SimpleDateFormat formatter = new SimpleDateFormat();
			formatter.applyPattern(pattern);
			return formatter.format(convertStringToDate(value));
		} catch (Exception e) {
			_logger.warn(MessageUtils.getString("warn.103", value), e); //$NON-NLS-1$
		}
		return value;
	}

	private String getNumberFormatValue(String value, String dataType, String pattern) {
		try {
			if (pattern.toUpperCase().startsWith(ControllerConstants.RESULT_MAP_KEY_CONSTANT)) {
				pattern = getValue(pattern.toUpperCase(), null, null);
			}
			DecimalFormat formatter = new DecimalFormat();
			formatter.applyPattern(pattern);
			return formatter.format(convertStringToNumber(value, dataType));
		} catch (Exception e) {
			_logger.warn(MessageUtils.getString("warn.103", value), e); //$NON-NLS-1$
		}
		return value;
	}
}