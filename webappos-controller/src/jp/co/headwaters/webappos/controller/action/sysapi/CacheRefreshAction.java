package jp.co.headwaters.webappos.controller.action.sysapi;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.headwaters.webappos.controller.action.AbstractAction;
import jp.co.headwaters.webappos.controller.cache.WebAppOSCache;
import jp.co.headwaters.webappos.controller.utils.MessageUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

@SuppressWarnings("serial")
public class CacheRefreshAction extends AbstractAction {

	private static final Log _logger = LogFactory.getLog(CacheRefreshAction.class);

	@Override
	public String execute() throws IOException {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		String ipAddress = request.getRemoteAddr(); //IPアドレス
		if (!"127.0.0.1".equals(ipAddress) && !"0:0:0:0:0:0:0:1".equals(ipAddress) && !ipAddress.startsWith("192.168.")) {
			_logger.warn("外部サイトからのキャッシュ更新リクエストを拒否しました。ipAddress=" + ipAddress);
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}

		_logger.info("***** キャッシュ更新開始 *****");
		try {
			WebAppOSCache.getInstance().load();
		} catch (Exception e) {
			_logger.error(MessageUtils.getString("err.002"), e); //$NON-NLS-1$
		}
		_logger.info("***** キャッシュ更新終了 *****");
		return null;
	}
}
