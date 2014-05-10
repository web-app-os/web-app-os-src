package jp.co.headwaters.webappos.controller.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.headwaters.webappos.controller.cache.bean.SystemConstantBean;

public class SystemConstantCache {

	public static Map<String, Map<String, SystemConstantBean>> getAllSystemConstant() {
		return WebAppOSCache.getInstance().getSystemConstantMap();
	}

	public static Map<String, SystemConstantBean> getSystemConstantMap(String category) {
		Map<String, SystemConstantBean> result = new HashMap<String, SystemConstantBean>();
		if (WebAppOSCache.getInstance().getSystemConstantMap().containsKey(category.toUpperCase())) {
			result = WebAppOSCache.getInstance().getSystemConstantMap().get(category.toUpperCase());
		}
		return result;
	}

	public static List<SystemConstantBean> getSystemConstantList(String category) {
		List<SystemConstantBean> result = new ArrayList<SystemConstantBean>();
		if (WebAppOSCache.getInstance().getSystemConstantMap().containsKey(category.toUpperCase())) {
			for (SystemConstantBean entry : WebAppOSCache.getInstance().getSystemConstantMap().get(category.toUpperCase()).values()) {
				result.add(entry);
			}
		}
		return result;
	}

	public static SystemConstantBean getSystemConstant(String category, String key) {
		if (WebAppOSCache.getInstance().getSystemConstantMap().containsKey(category.toUpperCase())) {
			return WebAppOSCache.getInstance().getSystemConstantMap().get(category.toUpperCase()).get(key.toUpperCase());
		}
		return null;
	}

	public static SystemConstantBean getSystemConstant(String key) {
		return WebAppOSCache.getInstance().getSystemConstantMap().get("").get(key.toUpperCase()); //$NON-NLS-1$
	}
}
