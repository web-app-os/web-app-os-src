package jp.co.headwaters.webappos.controller.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.cache.bean.ImageCacheBean;
import jp.co.headwaters.webappos.controller.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImageDataCache {

	private static final Log _logger = LogFactory.getLog(ImageDataCache.class);

	private static ImageDataCache imageDataCache = null;

	private Map<String, ImageCacheBean> imageCacheMap;

	private static int _limit = 500;

	private ImageDataCache() {
	}

	public static ImageDataCache getInstance() {
		if (imageDataCache == null) {
			imageDataCache = new ImageDataCache();
			String limit = PropertyUtils.getProperty(ControllerConstants.PROPERTY_KEY_IMAGE_CACHE_LIMIT);
			if (!StringUtils.isEmpty(limit)) {
				_limit = Integer.parseInt(limit);
			}
		}
		return imageDataCache;
	}

	public void refresh() throws Exception {
		try {
			Map<String, ImageCacheBean> tempImageCacheMap = new ConcurrentHashMap<String, ImageCacheBean>();

			synchronized (this) {
				this.imageCacheMap = tempImageCacheMap;
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	public ImageCacheBean getImageData(String key) {
		ImageCacheBean imageCacheBen = this.imageCacheMap.get(key);
		if (imageCacheBen == null) {
			return null;
		}

		if (imageCacheBen.isExipired()) {
			this.imageCacheMap.remove(key);
			return null;
		}
		return imageCacheBen;
	}

	public void addImageData(ImageCacheBean imageCacheBean) {
		try {
			if (_limit <= this.imageCacheMap.size()) {
				removeObj();
			}
			this.imageCacheMap.put(imageCacheBean.getKey(), imageCacheBean);
		} catch (Exception e) {
			_logger.warn(e.getMessage(), e);
			return;
		}
	}

	private void removeObj() {
		while (_limit <= this.imageCacheMap.size()) {
			ImageCacheBean deletedObj1 = getMinObj();
			if (deletedObj1 != null) {
				this.imageCacheMap.remove(deletedObj1.getKey());
			} else {
				return;
			}
		}
	}

	private ImageCacheBean getMinObj(){
		long minExpireDate = Long.MAX_VALUE;
		ImageCacheBean minBean = null;
		for(Map.Entry<String, ImageCacheBean> e : this.imageCacheMap.entrySet()) {
			ImageCacheBean bean = e.getValue();
			long expireDate = bean.getExpireDate();
			if( minExpireDate > expireDate ){
				minBean = bean;
				minExpireDate = expireDate;
			}
		}
		return minBean;
	}

	public int getCacheCount(){
		if(this.imageCacheMap == null){
			return 0;
		}
		return this.imageCacheMap.size();
	}
}
