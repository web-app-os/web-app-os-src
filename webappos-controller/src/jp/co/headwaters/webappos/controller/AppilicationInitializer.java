package jp.co.headwaters.webappos.controller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import jp.co.headwaters.webappos.controller.cache.ImageDataCache;
import jp.co.headwaters.webappos.controller.cache.WebAppOSCache;
import jp.co.headwaters.webappos.controller.oauth.ConsumerProperties;
import jp.co.headwaters.webappos.controller.utils.PropertyUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AppilicationInitializer implements ServletContextListener {

	private static final Log _logger = LogFactory.getLog(AppilicationInitializer.class);

	public void contextInitialized(ServletContextEvent event) {
		try {
			_logger.info("WEB APP OS contextInitialize start"); //$NON-NLS-1$

			_logger.info("Property Load start"); //$NON-NLS-1$
			PropertyUtils.load();
			ConsumerProperties.load();
			_logger.info("Property Load end"); //$NON-NLS-1$

			_logger.info("Web App OS Cache Load start"); //$NON-NLS-1$
			WebAppOSCache webAppOSCache = WebAppOSCache.getInstance();
			webAppOSCache.load();
			_logger.info("Web App OS Cache Load end"); //$NON-NLS-1$

			ImageDataCache.getInstance().refresh();

			_logger.info("WEB APP OS contextInitialize end"); //$NON-NLS-1$
		} catch (Exception e) {
			// Webアプリケーション起動を中止する
			throw new Error(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// nothing
	}
}