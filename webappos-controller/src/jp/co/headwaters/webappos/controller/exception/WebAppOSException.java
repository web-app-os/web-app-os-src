package jp.co.headwaters.webappos.controller.exception;

import jp.co.headwaters.webappos.controller.utils.MessageUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WebAppOSException extends Exception {

	private static final Log _logger = LogFactory.getLog(WebAppOSException.class);

	private static final long serialVersionUID = 4656133709626462425L;

	public WebAppOSException() {
		super();
	}

	public WebAppOSException(Exception e) {
		super(e);
	}

	public WebAppOSException(String key) {
		super(MessageUtils.getString(key));
		_logger.error(MessageUtils.getString(key));
	}

	public WebAppOSException(String key, String arg1) {
		super(MessageUtils.getString(key, arg1));
		_logger.error(MessageUtils.getString(key, arg1));
	}

	public WebAppOSException(String key, String arg1, String arg2) {
		super(MessageUtils.getString(key, arg1, arg2));
		_logger.error(MessageUtils.getString(key, arg1, arg2));
	}

	public WebAppOSException(String key, String arg1, String arg2, String arg3) {
		super(MessageUtils.getString(key, arg1, arg2, arg3));
		_logger.error(MessageUtils.getString(key, arg1, arg2, arg3));
	}
}
