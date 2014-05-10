package jp.co.headwaters.webappos.controller.exception;

public class ExecuteDeniedException extends WebAppOSException {

	private static final long serialVersionUID = 3854596405049097911L;

	public ExecuteDeniedException() {
		super();
	}

	public ExecuteDeniedException(String key) {
		super(key);
	}

	public ExecuteDeniedException(String key, String arg1) {
		super(key, arg1);
	}

	public ExecuteDeniedException(String key, String arg1, String arg2) {
		super(key, arg1, arg2);
	}

	public ExecuteDeniedException(String key, String arg1, String arg2, String arg3) {
		super(key, arg1, arg2, arg3);
	}
}
