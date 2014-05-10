package jp.co.headwaters.webappos.controller.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.cache.bean.ActionBean;
import jp.co.headwaters.webappos.controller.cache.bean.ProcedureInfoBean;
import jp.co.headwaters.webappos.controller.cache.bean.RelationKeyBean;
import jp.co.headwaters.webappos.controller.cache.bean.SchemaColumnBean;
import jp.co.headwaters.webappos.controller.cache.bean.SystemConstantBean;
import jp.co.headwaters.webappos.controller.cache.bean.UrlPatternBean;
import jp.co.headwaters.webappos.controller.utils.ControllerUtils;
import jp.co.headwaters.webappos.controller.utils.PropertyUtils;

public class WebAppOSCache implements Serializable {

	private static final long serialVersionUID = -5693627626077959225L;

	private static final WebAppOSCache _instance = new WebAppOSCache();
	private Map<String, ActionBean> _actionMap;
	private Map<String, UrlPatternBean> _urlPatternMap;
	private Map<String, ProcedureInfoBean> _procedureMap;
	private Map<String, SchemaColumnBean> _schemaColumnMap;
	private Map<String, Map<String, SystemConstantBean>> _systemConstantMap;
	private Map<String, List<RelationKeyBean>> _importedKeys;

	private WebAppOSCache() {
		setActionMap(new HashMap<String, ActionBean>());
		setUrlPatternMap(new HashMap<String, UrlPatternBean>());
		setProcedureMap(new LinkedHashMap<String, ProcedureInfoBean>());
		setSchemaColumnMap(new HashMap<String, SchemaColumnBean>());
		setSystemConstantMap(new HashMap<String, Map<String, SystemConstantBean>>());
	}

	public static WebAppOSCache getInstance() {
		return _instance;
	}

	public Map<String, ActionBean> getActionMap() {
		return this._actionMap;
	}

	public void setActionMap(Map<String, ActionBean> map) {
		this._actionMap = map;
	}

	public Map<String, UrlPatternBean> getUrlPatternMap() {
		return this._urlPatternMap;
	}

	public void setUrlPatternMap(Map<String, UrlPatternBean> map) {
		this._urlPatternMap = map;
	}

	public Map<String, ProcedureInfoBean> getProcedureMap() {
		return this._procedureMap;
	}

	public void setProcedureMap(Map<String, ProcedureInfoBean> map) {
		this._procedureMap = map;
	}

	public Map<String, SchemaColumnBean> getSchemaColumnMap() {
		return this._schemaColumnMap;
	}

	public void setSchemaColumnMap(Map<String, SchemaColumnBean> schemaColumnMap) {
		this._schemaColumnMap = schemaColumnMap;
	}

	public Map<String, Map<String, SystemConstantBean>> getSystemConstantMap() {
		return this._systemConstantMap;
	}

	public void setSystemConstantMap(Map<String, Map<String, SystemConstantBean>> systemConstantMap) {
		this._systemConstantMap = systemConstantMap;
	}

	public Map<String, List<RelationKeyBean>> getImportedKeys() {
		return this._importedKeys;
	}

	public void setImportedKeys(Map<String, List<RelationKeyBean>> importedKeys) {
		this._importedKeys = importedKeys;
	}

	public void load() throws FileNotFoundException, IOException, ClassNotFoundException {
		load(getInputFile());
	}

	public void load(File datFile) throws FileNotFoundException, IOException, ClassNotFoundException {
		try (
				FileInputStream fis = new FileInputStream(datFile);
				ObjectInputStream ois = new ObjectInputStream(fis)) {
			WebAppOSCache cache = (WebAppOSCache) ois.readObject();
			setActionMap(cache.getActionMap());
			setUrlPatternMap(cache.getUrlPatternMap());
			setProcedureMap(cache.getProcedureMap());
			setSchemaColumnMap(cache.getSchemaColumnMap());
			setSystemConstantMap(cache.getSystemConstantMap());
			setImportedKeys(cache.getImportedKeys());
		}
	}

	private static File getInputFile() {
		StringBuilder sb = new StringBuilder();
		sb.append(PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_WEBAPPS_PATH));
		if (!sb.toString().endsWith(ControllerUtils.getFileSparator())) {
			sb.append(ControllerUtils.getFileSparator());
		}
		sb.append(ControllerConstants.WEBAPPS_DAT_DIR);
		sb.append(ControllerUtils.getFileSparator());
		sb.append(ControllerConstants.WEBAPPS_DAT_FILE_NAME);
		return new File(sb.toString());
	}
}
